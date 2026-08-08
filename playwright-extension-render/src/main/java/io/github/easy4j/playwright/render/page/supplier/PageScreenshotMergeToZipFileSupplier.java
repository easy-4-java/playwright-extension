package io.github.easy4j.playwright.render.page.supplier;

import io.github.easy4j.playwright.render.PlaywrightRenderProperties;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class PageScreenshotMergeToZipFileSupplier implements Supplier<WkhtmlRenderResultVO> {

    @Getter
    protected PlaywrightRenderProperties playwrightRenderProperties;
    @Getter
    protected WkhtmlRenderBO renderBO;
    @Getter
    protected List<PageRenderBO> screenshots;

    public PageScreenshotMergeToZipFileSupplier(PlaywrightRenderProperties playwrightRenderProperties,
                                                WkhtmlRenderBO renderBO,
                                                List<PageRenderBO> screenshots) {
        this.playwrightRenderProperties = playwrightRenderProperties;
        this.renderBO = renderBO;
        this.screenshots = screenshots;
    }

    @Override
    public WkhtmlRenderResultVO get() {
        String zipFileName = renderBO.getTaskId() + ".zip";
        log.debug("Merging screenshots to ZIP: {}", zipFileName);
        File zipFile = new File(playwrightRenderProperties.getTmpDir(), zipFileName);
        try ( ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))){
            // 将所有截图写入ZIP文件
            screenshots.sort(Comparator.comparingInt(PageRenderBO::getIndex));
            // 循环截图列表
            int zipAmount = 0;
            for (PageRenderBO screenshot : screenshots) {
                String fileName = screenshot.getName();
                // 读取图片文件并写入 ZipOutputStream
                try (FileInputStream fileInput = new FileInputStream(screenshot.getPath())) {
                    // 创建 ZipEntry 对象
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOutputStream.putNextEntry(zipEntry);
                    // 将截图文件写入 ZipOutputStream
                    IOUtils.copy(fileInput, zipOutputStream);
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
            if(screenshots.size() != zipAmount){
                return new WkhtmlRenderResultVO()
                        .setRenderState(RenderState.FAIL)
                        .setRenderFailedReason("压缩成功的文件数与截图数不一致，存在压缩失败！")
                        .setFileName(zipFileName)
                        .setFilePath(zipFile.getAbsolutePath())
                        .setFileSize((zipFile.length() / 1024L));
            }
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.SUCCESS)
                    .setFileName(zipFileName)
                    .setFilePath(zipFile.getAbsolutePath())
                    .setFileSize((zipFile.length() / 1024L));
        } catch (Exception e) {
            throw new TaskRuntimeException("Failed to pack ZIP File : " + zipFileName, e);
        }
    }
}
