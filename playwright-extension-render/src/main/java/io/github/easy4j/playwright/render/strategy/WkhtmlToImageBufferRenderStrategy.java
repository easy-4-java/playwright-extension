package io.github.easy4j.playwright.render.strategy;


import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.page.supplier.PageScreenshotMergeToZipOutputStreamSupplier;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 使用 Playwright 渲染引擎将 HTML 渲染为各种图像格式
 */
@Slf4j
/**
 * Strategy for rendering web pages to image buffers using wkhtmltoimage.
 */
public class WkhtmlToImageBufferRenderStrategy extends AbstractPlaywrightRenderStrategy<WkhtmlRenderBO> {

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_IMAGE_BUFFER;
    }

    @Override
    protected List<PageRenderBO> doGenerate(WkhtmlRenderBO renderBO) throws IOException {
        log.debug("Capturing screenshots for urls: {}", renderBO.getUrls().stream().map(PageRenderBO::getUrl).collect(Collectors.toList()));
        if(renderBO.isAsync()){
            return this.captureScreenshotAsync(renderBO);
        } else {
            return this.captureScreenshotSync(renderBO);
        }
    }

    @Override
    protected List<PageRenderBO> doCompress(WkhtmlRenderBO renderBO, List<PageRenderBO> screenshots) {
        // 1、获取压缩质量，如果压缩质量不在范围内，则不压缩
        Integer quality = renderBO.getQuality();
        if((quality >= MAX_QUALITY || quality <= MIN_QUALITY)){
            log.debug("Compressing screenshot ignore. quality: {}", quality);
            return screenshots;
        }
        // 2、异步压缩图片
        return compressScreenshots(renderBO, screenshots, quality, (pageRenderBO) -> {
            if (Objects.isNull(pageRenderBO.getBuffer()) || pageRenderBO.getBuffer().length == 0) {
                // 如果截图的buffer为空，则不压缩
                log.debug("Compressing screenshot ignore, buffer is empty: {}", pageRenderBO.getName());
                return true;
            }
            return false;
        });
    }


    /**
     * 定义一个图片压缩方法
     * @param screenshots 截图列表
     * @param quality 压缩质量
     * @return 压缩后的截图
     */
    protected List<PageRenderBO> compressScreenshots(WkhtmlRenderBO renderBO, List<PageRenderBO> screenshots, Integer quality, Function<PageRenderBO, Boolean> filter) {
        if((quality >= MAX_QUALITY || quality <= MIN_QUALITY)){
            log.debug("Compressing screenshot ignore. quality : {}", quality);
            return screenshots;
        }
        List<CompletableFuture<PageRenderBO>> futureList = new ArrayList<>();
        for (PageRenderBO pageRenderBO : screenshots) {
            if(filter.apply(pageRenderBO)){
                continue;
            }
            CompletableFuture<PageRenderBO> completableFuture = this.compressScreenshot(pageRenderBO, quality);
            // 4、异步截图任务执行完成
            completableFuture.whenComplete((pageScreenshot, e) -> {
                if (Objects.nonNull(e)) {
                    pageScreenshot.setRenderState(RenderState.FAIL);
                    pageScreenshot.setRenderFailedReason("图片压缩失败，失败原因: " + e.getMessage());
                    log.error("图片压缩任务执行异常，异常信息：", e);
                } else {
                    log.debug("图片压缩任务执行完成，TaskId: {}, url : {}, pageName: {}, fileSize: {}", renderBO.getTaskId(), pageScreenshot.getUrl(), pageScreenshot.getName(),
                            ((pageScreenshot.getFileSize() / 1024)));
                }
            });
            futureList.add(completableFuture);
        }
        log.debug("等待图片压缩异步任务完成...");
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        log.debug("异步图片压缩任务执行完毕.");
        return screenshots;
    }

    /**
     * 定义一个图片压缩方法
     * @param screenshot 截图
     * @param quality 压缩质量
     * @return 压缩后的截图
     */
    protected CompletableFuture<PageRenderBO> compressScreenshot(PageRenderBO screenshot, Integer quality) {
        // 判断压缩质量和压缩比例是否在范围内
        if(quality < MAX_QUALITY && quality > MIN_QUALITY){
            return CompletableFuture.supplyAsync(() -> {
                log.debug("Compressing screenshot buffer: {}", screenshot.getName());
                // 使用Thumbnails进行图片压缩
                try(ByteArrayInputStream input = new ByteArrayInputStream(screenshot.getBuffer());
                    ByteArrayOutputStream output = new ByteArrayOutputStream() ){
                    // 从图片流中读取图片
                    Thumbnails.of(input)
                            .allowOverwrite(true)
                            .scale(1f)
                            .outputQuality(quality / 100f)
                            .toOutputStream(output);
                    screenshot.setBuffer(output.toByteArray());
                    log.debug("Compressing screenshot buffer success : {}", screenshot.getName());
                } catch (Exception e) {
                    throw new TaskRuntimeException("Compressing screenshot file error: " +  e.getMessage());
                }
                return screenshot;
            }, dtpToImageCompressExecutor);
        }
        log.debug("Compressing screenshot ignore: {}", screenshot.getName());
        return CompletableFuture.completedFuture(screenshot);
    }

    @Override
    public WkhtmlRenderResultVO doPacking(WkhtmlRenderBO renderBO, List<PageRenderBO> screenshots) throws IOException {
        if ((screenshots == null || screenshots.isEmpty())) {
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.FAIL)
                    .setRenderFailedReason("截图全部失败，参数可能异常，请重试！")
                    .setFileSize(0L);
        }
        return this.mergeScreenshotsToZip(renderBO, screenshots).join();
    }

    /**
     * 定义一个图片合并为Zip方法
     * @param renderBO 渲染参数
     * @param screenshots 截图列表
     * @return 打包后的Zip文件
     */
    protected CompletableFuture<WkhtmlRenderResultVO> mergeScreenshotsToZip(WkhtmlRenderBO renderBO, List<PageRenderBO> screenshots) {
        if(screenshots.size() == 1){
            PageRenderBO screenshot = screenshots.get(0);
            String imageFileName = renderBO.getTaskId() + "." + FilenameUtils.getExtension(screenshot.getName());
            return CompletableFuture.completedFuture(new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.SUCCESS)
                    .setFileName(imageFileName)
                    .setFilePath(screenshot.getPath())
                    .setFileBuffer(screenshot.getBuffer())
                    .setFileSize(screenshot.getFileSize()));
        }
        return CompletableFuture.supplyAsync(new PageScreenshotMergeToZipOutputStreamSupplier(renderOptions, renderBO, screenshots), dtpToImageZipExecutor);
    }

}
