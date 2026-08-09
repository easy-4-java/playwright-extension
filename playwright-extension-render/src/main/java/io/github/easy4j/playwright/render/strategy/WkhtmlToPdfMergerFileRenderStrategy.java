package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Playwright 渲染引擎将 HTML 渲染为 PDF 和各种图像格式
 */
@Slf4j
/**
 * Strategy for merging multiple wkhtmltopdf outputs into a single PDF file.
 */
public class WkhtmlToPdfMergerFileRenderStrategy extends WkhtmlToPdfMergerBufferRenderStrategy {

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_PDF_MERGER_FILE;
    }

    @Override
    protected List<PageRenderBO> doGenerate(WkhtmlRenderBO renderBO) {
        log.debug("Generate PDF for urls: {}", renderBO.getUrls().stream().map(PageRenderBO::getUrl).collect(Collectors.toList()));
        if(renderBO.isAsync()){
            return this.pageToPdfFutureAsync(renderBO);
        } else {
            return this.pageToPdfFutureSync(renderBO);
        }
    }

    /**
     * 定义一个PDF合并为PDF方法
     * @param renderBO 渲染参数
     * @param pdfs Pdf 列表
     * @return 合并后的PDF文件
     */
    @Override
    protected CompletableFuture<WkhtmlRenderResultVO> mergePdfsToPDF(WkhtmlRenderBO renderBO,
                                                                     List<PageRenderBO> pdfs) {
        return this.mergePdfsToPDF(renderBO, pdfs, (mergePdf, renderList) -> {
            String pdfFileName = "document_" + renderBO.getTaskId() + ".pdf";
            try {
                // 设置合并生成pdf文件名称
                String pdfFilePath = renderOptions.getTmpDir() + File.separator + pdfFileName;
                mergePdf.setDestinationFileName(pdfFilePath);
                // 使用主内存进行PDF合并处理
                //mergePdf.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                // 或者使用磁盘临时文件进行处理
                //mergePdf.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                // 或者结合使用主内存和磁盘临时文件进行处理（这里设置8MB）
                //mergePdf.mergeDocuments(MemoryUsageSetting.setupMixed(8 * 1024 * 1024));
                // Since v3.0.2
                mergePdf.mergeDocuments(null);
                // 返回合并后的pdf文件
                return new WkhtmlRenderResultVO()
                        .setRenderState(RenderState.SUCCESS)
                        .setFileName(pdfFileName)
                        .setFilePath(pdfFilePath)
                        .setFileSize((new File(pdfFilePath).length() / 1024L));
            }  catch (IOException e) {
                throw new TaskRuntimeException("Failed to merge PDF File : " + pdfFileName, e);
            }
        });
    }

}
