/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.capture.CaptureService;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.config.TaskIdGenerator;
import io.github.easy4j.playwright.render.enums.RenderType;

import java.util.List;

/**
 * Per-page PDF → merged PDF in memory.
 *
 * <p>Unlike {@link WkhtmlToPdfBufferRenderStrategy}, this strategy captures
 * each page as a single-page PDF (via {@link com.microsoft.playwright.Page#pdf}),
 * then the starter merges them with PDFBox's {@code PDFMergerUtility}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class WkhtmlToPdfMergerBufferRenderStrategy extends AbstractPlaywrightRenderStrategy<WkhtmlRenderBO> {

    private final CaptureService captureService;

    public WkhtmlToPdfMergerBufferRenderStrategy(RenderConfig config,
                                                    TaskIdGenerator taskIdGenerator,
                                                    CaptureService captureService) {
        super(config, taskIdGenerator);
        this.captureService = captureService;
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_PDF_MERGER_BUFFER;
    }

    @Override
    protected List<PageRenderBO> doGenerate(WkhtmlRenderBO renderBO) {
        return config.isAsync()
                ? captureService.captureAsync(renderBO, config, renderBO.getUrls())
                : captureService.captureSync(renderBO, config, renderBO.getUrls());
    }

    @Override
    protected io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO doPacking(
            WkhtmlRenderBO renderBO, List<PageRenderBO> pages) {
        // Minimal packing: return the first page's output as the merged result.
        // Starter-bound Suppliers (PDF module's PagePdfMergeToPdfSupplier) perform the
        // actual multi-PDF merge; this default keeps the render module PDFBox-free.
        if (pages == null || pages.isEmpty()) {
            return new io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO()
                    .setRenderState(io.github.easy4j.playwright.render.enums.RenderState.FAIL)
                    .setRenderFailedReason("PDF保存全部失败")
                    .setFileSize(0L);
        }
        PageRenderBO first = pages.get(0);
        String fileName = "document_" + renderBO.getTaskId() + ".pdf";
        return new io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO()
                .setRenderState(io.github.easy4j.playwright.render.enums.RenderState.SUCCESS)
                .setFileName(fileName)
                .setFilePath(first.getPath())
                .setFileBuffer(first.getBuffer())
                .setFileSize(first.getFileSize())
                .setPages(pages);
    }
}