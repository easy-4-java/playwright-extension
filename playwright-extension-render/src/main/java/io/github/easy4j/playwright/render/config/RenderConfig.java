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
package io.github.easy4j.playwright.render.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Immutable configuration for the render pipeline.
 *
 * <p>Replaces the original {@code PlaywrightProperties} +
 * {@code PlaywrightRenderProperties} Spring-bound duo with a single POJO
 * passed into the strategy constructors. The Spring Boot starter is
 * responsible for translating its own {@code @ConfigurationProperties} into
 * this class.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
public class RenderConfig {

    // ---------- Page lifecycle ----------
    /** CSS selector to wait for before invoking the capture callback (null = skip). */
    @Builder.Default
    private final String waitForSelector = null;

    /** Extra wait after navigation before capturing. */
    @Builder.Default
    private final Duration loadWaitDuration = Duration.ofSeconds(3);

    /** Whether to attempt reload on partial failures. */
    @Builder.Default
    private final boolean reloadAble = true;

    /** Max reload attempts per page. */
    @Builder.Default
    private final int reloadLimit = 3;

    /** Wait between reload attempts. */
    @Builder.Default
    private final Duration reloadWaitDuration = Duration.ofSeconds(3);

    /** Initial navigation timeout (ms). */
    @Builder.Default
    private final long navigateTimeoutMs = 30_000L;

    // ---------- Output ----------
    /** Whether to write to disk (true) or return buffer (false). */
    @Builder.Default
    private final boolean writeToFile = true;

    /** Temp dir for on-disk output. */
    @Builder.Default
    private final String tmpDir = "/tmp";

    /** Whether to invoke user-supplied PageScreenshotCheckers. */
    @Builder.Default
    private final boolean useCustomCheck = false;

    /** Render-specific URL prefix substitution. */
    @Builder.Default
    private final String urlPrefix = "";

    @Builder.Default
    private final boolean useUrlPrefix = false;

    /** Render async (one task per page) vs sync (single context, sequential). */
    @Builder.Default
    private final boolean async = true;
}