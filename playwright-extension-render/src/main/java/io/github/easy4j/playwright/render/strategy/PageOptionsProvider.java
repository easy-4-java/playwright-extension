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

import com.microsoft.playwright.extension.options.BrowserLaunchOptions;
import com.microsoft.playwright.extension.options.BrowserNewContextOptions;
import com.microsoft.playwright.extension.options.ElementScreenshotOptions;
import com.microsoft.playwright.extension.options.PageNavigateOptions;
import com.microsoft.playwright.extension.options.PagePdfOptions;
import com.microsoft.playwright.extension.options.PageScreenshotOptions;
import com.microsoft.playwright.extension.options.PageWaitForSelectorOptions;

/**
 * Provides Playwright option POJOs to the render strategies.
 *
 * <p>Replaces direct dependency on {@code PlaywrightProperties} (which lives
 * in the Spring module). The Spring module's {@code PlaywrightProperties}
 * implements this interface.</p>
 *
 * <p>The returned types are the core module's option POJOs (not Playwright SDK
 * types), because the render code calls both {@code .toOptions()} (to convert
 * to SDK objects) and individual getters like {@code .getTimeout()}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface PageOptionsProvider {

    PageNavigateOptions getPageNavigateOptions();

    PageScreenshotOptions getPageScreenshotOptions();

    com.microsoft.playwright.extension.options.PageEmulateMediaOptions getPageEmulateMediaOptions();

    PagePdfOptions getPagePdfOptions();

    PageWaitForSelectorOptions getPageWaitForSelectorOptions();

    ElementScreenshotOptions getElementScreenshotOptions();

    BrowserLaunchOptions getLaunchOptions();

    BrowserNewContextOptions getNewContextOptions();
}