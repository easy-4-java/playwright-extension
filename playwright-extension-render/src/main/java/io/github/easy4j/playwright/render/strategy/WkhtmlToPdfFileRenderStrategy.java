package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Playwright 渲染引擎将 HTML 渲染为 PDF 和各种图像格式
 */
@Slf4j
public class WkhtmlToPdfFileRenderStrategy extends WkhtmlToImageFileRenderStrategy {

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_PDF_FILE;
    }

    @Override
    public WkhtmlRenderResultVO doPacking(WkhtmlRenderBO renderBO, List<PageRenderBO> screenshots) throws IOException {
        if ((screenshots == null || screenshots.isEmpty())) {
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.FAIL)
                    .setRenderFailedReason("PDF保存全部失败，参数可能异常，请重试！")
                    .setFileSize(0L);
        }
        return this.mergeScreenshotsToPDF(renderBO, screenshots).join();
    }

    /**
     * 定义一个图片合并为PDF方法
     * @param renderBO 渲染参数
     * @param screenshots 截图列表
     * @return 合并后的PDF文件
     */
    protected CompletableFuture<WkhtmlRenderResultVO> mergeScreenshotsToPDF(WkhtmlRenderBO renderBO,
                                                                    List<PageRenderBO> screenshots) {
        return this.mergeScreenshotsToPDF(renderBO, screenshots, (pdDocument, renderList) -> {
            String pdfFileName = "document_" + renderBO.getTaskId() + ".pdf";
            log.debug("Merging screenshots to PDF: {}", pdfFileName);
            File pdfFile = new File(renderOptions.getTmpDir(), pdfFileName);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(pdfFile.toPath()), 2048)) {
                pdDocument.save(outputStream);
                if(screenshots.size() != pdDocument.getNumberOfPages()){
                    return new WkhtmlRenderResultVO()
                                .setRenderState(RenderState.FAIL)
                                .setRenderFailedReason("PDF页码与截图数不一致，截图丢失！")
                                .setFileName(pdfFileName)
                                .setFilePath(pdfFile.getAbsolutePath())
                                .setFileSize((pdfFile.length() / 1024L));
                }
                return new WkhtmlRenderResultVO()
                            .setRenderState(RenderState.SUCCESS)
                            .setFileName(pdfFileName)
                            .setFilePath(pdfFile.getAbsolutePath())
                            .setFileSize((pdfFile.length() / 1024L));
            } catch (IOException e) {
                throw new TaskRuntimeException("Failed to merge screenshots to PDF File : " + pdfFileName, e);
            }
        });
    }


}
