package io.github.easy4j.playwright.render.page.supplier;

import io.github.easy4j.playwright.render.strategy.RenderOptions;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.page.checker.PageScreenshotChecker;
import io.github.easy4j.playwright.render.util.PdfUtil;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Slf4j
public class PagePdfMergeToPdfSupplier implements Supplier<WkhtmlRenderResultVO> {

    @Getter
    protected RenderOptions renderOptions;
    @Getter
    protected WkhtmlRenderBO renderBO;
    @Getter
    protected List<PageRenderBO> pdfs;
    @Getter
    protected List<PageScreenshotChecker> pageScreenshotCheckers;
    @Getter
    protected BiFunction<PDFMergerUtility, List<PageRenderBO>, WkhtmlRenderResultVO> biFunction;

    public PagePdfMergeToPdfSupplier(RenderOptions renderOptions,
                                     WkhtmlRenderBO renderBO,
                                     List<PageRenderBO> pdfs,
                                     List<PageScreenshotChecker> pageScreenshotCheckers,
                                     BiFunction<PDFMergerUtility, List<PageRenderBO>, WkhtmlRenderResultVO> biFunction) {
        this.renderOptions = renderOptions;
        this.renderBO = renderBO;
        this.pdfs = pdfs;
        this.pageScreenshotCheckers = pageScreenshotCheckers;
        this.biFunction = biFunction;
    }

    @Override
    public WkhtmlRenderResultVO get() {
        String pdfFileName = "document_" + renderBO.getTaskId() + ".pdf";
        log.debug("Merging pdf buffers/files to PDF: {}", pdfFileName);
        // 单个文件直接返回
        if (pdfs.size() == 1) {
            PageRenderBO screenshot = pdfs.get(0);
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.SUCCESS)
                    .setFileName(pdfFileName)
                    .setFilePath(screenshot.getPath())
                    .setFileBuffer(screenshot.getBuffer())
                    .setFileSize(screenshot.getFileSize());
        }
        // 如果有多个文件，则合并pdf文件
        try{
            /*
             * org.apache.pdfbox.util.PDFMergerUtility：pdf合并工具类
             * https://blog.csdn.net/qq_38998209/article/details/127983909
             */
            PDFMergerUtility mergePdf = new PDFMergerUtility();
            // 设置PDF属性
            mergePdf.setDestinationDocumentInformation(PdfUtil.information(renderBO));
            // 设置合并模式为压缩资源模式
            mergePdf.setDocumentMergeMode(PDFMergerUtility.DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
            // 合并pdf文件路径
            pdfs.sort(Comparator.comparingInt(PageRenderBO::getIndex));
            for (PageRenderBO buffer : pdfs) {
                if (Objects.nonNull(buffer.getBuffer())) {
                    mergePdf.addSource(new RandomAccessReadBuffer(buffer.getBuffer()));
                    log.debug("Merging pdf buffer {} to PDF {} succeed.", buffer.getName(), pdfFileName);
                } else if (Objects.nonNull(buffer.getPath())) {
                    // 如果是文件路径，则直接添加文件
                    mergePdf.addSource(new File(buffer.getPath()));
                    log.debug("Merging pdf file {} to PDF {} succeed.", buffer.getPath(), pdfFileName);
                }
            }
            return biFunction.apply(mergePdf, pdfs);
        } catch (Exception e) {
            throw new TaskRuntimeException("Failed to merge PDF File : " + pdfFileName, e);
        }
    }

}
