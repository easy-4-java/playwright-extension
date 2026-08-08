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
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Screenshot → in-memory buffer (or ZIP if multi-page).
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class WkhtmlToImageBufferRenderStrategy extends AbstractPlaywrightRenderStrategy<WkhtmlRenderBO> {

    private final CaptureService captureService;

    public WkhtmlToImageBufferRenderStrategy(RenderConfig config,
                                                TaskIdGenerator taskIdGenerator,
                                                CaptureService captureService) {
        super(config, taskIdGenerator);
        this.captureService = captureService;
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_IMAGE_BUFFER;
    }

    @Override
    protected List<PageRenderBO> doGenerate(WkhtmlRenderBO renderBO) {
        return config.isAsync()
                ? captureService.captureAsync(renderBO, config, renderBO.getUrls())
                : captureService.captureSync(renderBO, config, renderBO.getUrls());
    }

    @Override
    protected WkhtmlRenderResultVO doPacking(WkhtmlRenderBO renderBO, List<PageRenderBO> pages) {
        if (pages == null || pages.isEmpty()) {
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.FAIL)
                    .setRenderFailedReason("截图全部失败")
                    .setFileSize(0L);
        }
        // Single page → return as-is; multi-page → caller responsibility (ZIP packing
        // is provided by the starter-bound Suppliers; this minimal render keeps the
        // contract simple).
        PageRenderBO first = pages.get(0);
        String fileName = renderBO.getTaskId() + "." + FilenameUtils.getExtension(first.getName());
        return new WkhtmlRenderResultVO()
                .setRenderState(RenderState.SUCCESS)
                .setFileName(fileName)
                .setFilePath(first.getPath())
                .setFileBuffer(first.getBuffer())
                .setFileSize(first.getFileSize())
                .setPages(pages);
    }
}