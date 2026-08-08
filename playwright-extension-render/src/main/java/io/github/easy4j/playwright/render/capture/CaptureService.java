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
package io.github.easy4j.playwright.render.capture;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;

import java.util.List;

/**
 * Page capture service (screenshot or single-page PDF).
 *
 * <p>Replaces {@code captureScreenshotAsync/Sync} and
 * {@code pageToPdfFutureAsync/Sync} from the original God Class. The two
 * implementations differ only in the callback factory they use
 * ({@code ScreenshotCallbackFactory} vs {@code PdfCallbackFactory}); both
 * share the same borrow-context → navigate → callback → return-context
 * template.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface CaptureService {

    /**
     * Borrow browser contexts in parallel (one per page) and capture each.
     *
     * @param renderBO render request (provides taskId / selector)
     * @param config   render configuration
     * @param pages    page specs (mutated in place)
     * @return list of pageBOs that produced non-null output (in arbitrary order)
     */
    List<PageRenderBO> captureAsync(WkhtmlRenderBO renderBO, RenderConfig config, List<PageRenderBO> pages);

    /**
     * Capture pages sequentially using a single borrowed browser context.
     */
    List<PageRenderBO> captureSync(WkhtmlRenderBO renderBO, RenderConfig config, List<PageRenderBO> pages);
}