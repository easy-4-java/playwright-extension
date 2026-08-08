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

import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.capture.CaptureService;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.config.TaskIdGenerator;
import io.github.easy4j.playwright.render.enums.RenderType;

/**
 * Screenshot → single PDF (each screenshot becomes a PDF page).
 *
 * <p>The actual PDF merge is performed by the starter-bound Suppliers (in the
 * PDF module) so this render module stays PDFBox-free. The starter wires the
 * appropriate {@code PageScreenshotMergeToPdfSupplier} into the strategy via
 * the {@code doPacking} override.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class WkhtmlToPdfBufferRenderStrategy extends WkhtmlToImageBufferRenderStrategy {

    public WkhtmlToPdfBufferRenderStrategy(RenderConfig config,
                                              TaskIdGenerator taskIdGenerator,
                                              CaptureService captureService) {
        super(config, taskIdGenerator, captureService);
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.TO_PDF_BUFFER;
    }
}