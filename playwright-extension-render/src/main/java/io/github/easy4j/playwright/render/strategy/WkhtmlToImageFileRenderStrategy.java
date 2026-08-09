package io.github.easy4j.playwright.render.strategy;


import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.page.supplier.PageScreenshotMergeToZipFileSupplier;
import io.github.easy4j.playwright.render.page.supplier.PageScreenshotPackToZipFileSupplier;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 使用 Playwright 渲染引擎将 HTML 渲染为各种图像格式
 */
@Slf4j
public class WkhtmlToImageFileRenderStrategy extends WkhtmlToImageBufferRenderStrategy {

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_IMAGE_FILE;
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
            log.debug("Compressing screenshot ignore. quality : {}", quality);
            return screenshots;
        }
        // 2、异步压缩图片
        return compressScreenshots(renderBO, screenshots, quality, (pageRenderBO) -> {
            // 如果截图的路径为空，则不压缩
            if (StringUtils.isBlank(pageRenderBO.getPath())) {
                log.debug("Compressing screenshot ignore, screenshot path is empty: {}", pageRenderBO.getName());
                return true;
            }
            // 如果截图的文件不存在，则不压缩
            File file = new File(pageRenderBO.getPath());
            if (!file.exists()) {
                log.debug("Compressing screenshot ignore, screenshot file not exists: {}", pageRenderBO.getName());
                return true;
            }
            return false;
        });
    }

    /**
     * 定义一个图片压缩方法
     * @param screenshot 截图
     * @param quality 压缩质量
     * @return 压缩后的截图
     */
    @Override
    protected CompletableFuture<PageRenderBO> compressScreenshot(PageRenderBO screenshot, Integer quality) {
        // 判断压缩质量是否在范围内
        if(quality < MAX_QUALITY && quality > MIN_QUALITY){
            return CompletableFuture.supplyAsync(() -> {
                try{
                    log.debug("Compressing screenshot file : {}", screenshot.getPath());
                    File sourceFile = new File(screenshot.getPath());
                    File outFile = new File(renderOptions.getTmpDir(), screenshot.getName());
                    Thumbnails.of(sourceFile)
                            .allowOverwrite(true)
                            .scale(1f)
                            .outputQuality(quality / 100f)
                            .toFile(outFile);
                    screenshot.setPath(outFile.getAbsolutePath());
                    log.debug("Compressing screenshot file success : {}", screenshot.getName());
                } catch (Exception e) {
                    throw new TaskRuntimeException("Compressing screenshot file error: " +  e.getMessage());
                }
                return screenshot ;
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
        // 1、判断操作系统，如果是windows，则使用mergeScreenshotsToZip方法，否则使用packScreenshotsToZip方法
        if(SystemUtils.IS_OS_WINDOWS){
            return this.mergeScreenshotsToZip(renderBO, screenshots).join();
        } else {
            return this.packScreenshotsToZip(renderBO, screenshots).join();
        }
    }

    /**
     * 定义一个图片打包为Zip方法
     * @param renderBO 渲染参数
     * @param screenshots 截图列表
     * @return 打包后的Zip文件
     */
    @Override
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
        return CompletableFuture.supplyAsync(new PageScreenshotMergeToZipFileSupplier(renderOptions, renderBO, screenshots), dtpToImageZipExecutor);
    }

    /**
     * 定义一个图片打包为Zip方法
     * @param renderBO 渲染参数
     * @param screenshots 截图列表
     * @return 打包后的Zip文件
     */
    protected CompletableFuture<WkhtmlRenderResultVO> packScreenshotsToZip(WkhtmlRenderBO renderBO, List<PageRenderBO> screenshots) {
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
        return CompletableFuture.supplyAsync(new PageScreenshotPackToZipFileSupplier(renderOptions, renderBO, screenshots), dtpToImageZipExecutor);
    }

}
