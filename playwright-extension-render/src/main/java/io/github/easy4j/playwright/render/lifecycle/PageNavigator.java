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
package io.github.easy4j.playwright.render.lifecycle;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;

import java.util.function.BiFunction;

/**
 * Abstracts the page lifecycle: navigate, wait, event-listen, retry, then
 * invoke the caller's capture callback.
 *
 * <p>Extracted from {@code AbstractPlaywrightRenderStrategy#loadPageWithCallback}
 * (~230 lines) so the retry / event-listen logic can be tested in isolation.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface PageNavigator {

    /**
     * Drive a single page through its lifecycle (navigate → wait → optional reload loop → capture).
     *
     * @param context    borrowed browser context (caller manages lifecycle)
     * @param page       the page to drive (caller creates / closes)
     * @param config     render configuration
     * @param pageBO     page state BO (mutated in place: renderState / checkState / fileSize / ...)
     * @param rendeId    render task id (for logging)
     * @param callback   the screenshot / pdf capture function
     * @return the (possibly mutated) pageBO
     */
    PageRenderBO navigate(BrowserContext context,
                            Page page,
                            RenderConfig config,
                            PageRenderBO pageBO,
                            String rendeId,
                            BiFunction<Page, PageRenderBO, PageRenderBO> callback);
}