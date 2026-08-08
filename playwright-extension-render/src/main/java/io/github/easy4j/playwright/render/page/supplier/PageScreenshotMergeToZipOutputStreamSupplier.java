package io.github.easy4j.playwright.render.page.supplier;

import io.github.easy4j.playwright.render.PlaywrightRenderProperties;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class PageScreenshotMergeToZipOutputStreamSupplier implements Supplier<WkhtmlRenderResultVO> {

    /**
     * 默认编码，使用平台相关编码
     */
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    @Getter
    protected PlaywrightRenderProperties playwrightRenderProperties;
    @Getter
    protected WkhtmlRenderBO renderBO;
    @Getter
    protected List<PageRenderBO> screenshots;

    public PageScreenshotMergeToZipOutputStreamSupplier(PlaywrightRenderProperties playwrightRenderProperties,
                                                        WkhtmlRenderBO renderBO,
                                                        List<PageRenderBO> screenshots) {
        this.playwrightRenderProperties = playwrightRenderProperties;
        this.renderBO = renderBO;
        this.screenshots = screenshots;
    }

    @Override
    public WkhtmlRenderResultVO get() {
        if(screenshots.size() == 1){
            PageRenderBO screenshot = screenshots.get(0);
            String imageFileName = renderBO.getTaskId() + "." + FilenameUtils.getExtension(screenshot.getName());
            screenshot.setName(imageFileName);
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.SUCCESS)
                    .setFileName(imageFileName)
                    .setFilePath(screenshot.getPath())
                    .setFileBuffer(screenshot.getBuffer())
                    .setFileSize(screenshot.getFileSize());
        }
        String zipFileName = renderBO.getTaskId() + ".zip";
        log.debug("Merging screenshots to ZIP: {}", zipFileName);
        try (ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(zipOutput, DEFAULT_CHARSET);) {
            screenshots.sort(Comparator.comparingInt(PageRenderBO::getIndex));
            // 循环截图列表
            int zipAmount = 0;
            // 将所有截图写入ZIP文件
            for (PageRenderBO screenshot : screenshots) {
                String fileName = screenshot.getName();
                try (ByteArrayInputStream bufferInput = new ByteArrayInputStream(screenshot.getBuffer())){
                    // 创建 ZipEntry 对象
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOutputStream.putNextEntry(zipEntry);
                    // 将截图缓存写入 ZipOutputStream
                    IOUtils.copy(bufferInput, zipOutputStream);
                    // 关闭当前 ZipEntry
                    zipOutputStream.closeEntry();
                    zipOutputStream.flush();
                    zipAmount ++;
                    log.debug("Merging screenshot {} to ZIP: {} Success", fileName, zipFileName);
                } catch (Exception e) {
                    log.error("Merging screenshot {} to ZIP: {} Failed", fileName, zipFileName);
                }
            }
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(zipOutput);
            if(screenshots.size() != zipAmount){
                return new WkhtmlRenderResultVO()
                        .setRenderState(RenderState.FAIL)
                        .setRenderFailedReason("压缩成功的文件数与截图数不一致，存在压缩失败！")
                        .setFileName(zipFileName)
                        .setFileBuffer(zipOutput.toByteArray())
                        .setFileSize(((long)(zipOutput.size() / 1024L)));
            }
            //metrics.playwright_zip_total_requset_success_count.inc(1);
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.SUCCESS)
                    .setFileName(zipFileName)
                    .setFileBuffer(zipOutput.toByteArray())
                    .setFileSize(((long)(zipOutput.size() / 1024L)));
        } catch (Exception e) {
            //metrics.playwright_zip_total_requset_error_count.inc(1);
            throw new TaskRuntimeException("Merging screenshots to ZIP error: {}", e);
        }
    }
}
