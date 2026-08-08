package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Playwright 渲染引擎将 HTML 渲染为 PDF 和各种图像格式
 */
@Slf4j
public class WkhtmlToPdfMergerBufferRenderStrategy extends AbstractPlaywrightRenderStrategy<WkhtmlRenderBO> {

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_PDF_MERGER_BUFFER;
    }

    @Override
    protected List<PageRenderBO> doGenerate(WkhtmlRenderBO renderBO) throws IOException {
        log.debug("Generate PDF for urls: {}", renderBO.getUrls().stream().map(PageRenderBO::getUrl).collect(Collectors.toList()));
        if(renderBO.isAsync()){
            return this.pageToPdfFutureAsync(renderBO);
        } else {
            return this.pageToPdfFutureSync(renderBO);
        }
    }

    @Override
    protected List<PageRenderBO> doCompress(WkhtmlRenderBO renderBO, List<PageRenderBO> pdfs) {
        // 1、获取压缩质量，如果压缩质量不在范围内，则不压缩
        Integer quality = renderBO.getQuality();
        if((quality >= MAX_QUALITY || quality <= MIN_QUALITY)){
            log.debug("Compressing pdf ignore. quality：{}", quality);
            return pdfs;
        }
        // 2、异步压缩pdf
        return pdfs;
    }

    @Override
    protected WkhtmlRenderResultVO doPacking(WkhtmlRenderBO renderBO, List<PageRenderBO> pdfs) {
        if (CollectionUtils.isEmpty(pdfs)) {
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.FAIL)
                    .setRenderFailedReason("PDF保存全部失败，参数可能异常，请重试！")
                    .setFileSize(0L);
        }
        return this.mergePdfsToPDF(renderBO, pdfs).join();
    }

    /**
     * 定义一个PDF合并为PDF方法
     * @param renderBO 渲染参数
     * @param pdfs Pdf 列表
     * @return 合并后的PDF文件
     */
    protected CompletableFuture<WkhtmlRenderResultVO> mergePdfsToPDF(WkhtmlRenderBO renderBO, List<PageRenderBO> pdfs) {
        return this.mergePdfsToPDF(renderBO, pdfs, (mergePdf, renderList) -> {
            // 设置合并生成pdf文件名称
            String pdfFileName = "document_" + renderBO.getTaskId() + ".pdf";
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                mergePdf.setDestinationStream(outputStream);
                // 使用主内存进行PDF合并处理
                //mergePdf.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                // 或者使用磁盘临时文件进行处理
                //mergePdf.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                // 或者结合使用主内存和磁盘临时文件进行处理（这里设置8MB）
                //mergePdf.mergeDocuments(MemoryUsageSetting.setupMixed(8 * 1024 * 1024));
                // Since v3.0.2
                mergePdf.mergeDocuments(null);
                byte[] bytes = outputStream.toByteArray();
                // 返回合并后的pdf文件
                return new WkhtmlRenderResultVO()
                        .setRenderState(RenderState.SUCCESS)
                        .setFileName(pdfFileName)
                        .setFileBuffer(bytes)
                        .setFileSize(((long)(bytes.length / 1024L)));
            }  catch (IOException e) {
                throw new TaskRuntimeException("Failed to merge PDF File : " + pdfFileName, e);
            }
        });
    }

}
