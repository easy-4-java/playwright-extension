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

import java.time.Duration;

/**
 * Render configuration abstraction.
 *
 * <p>Replaces direct dependency on {@code PlaywrightRenderProperties} (which lives
 * in the Spring module) so the render module stays Spring-free. The Spring module's
 * {@code PlaywrightRenderProperties} implements this interface.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface RenderOptions {

    boolean isIsolated();

    String getWaitForSelector();

    boolean isWriteToFile();

    String getTmpDir();

    String getUrlPrefix();

    boolean isUseUrlPrefix();

    boolean isLoadWait();

    Duration getLoadWaitDuration();

    boolean isReloadAble();

    Integer getReloadLimit();

    boolean isReloadWait();

    Duration getReloadWaitDuration();

    boolean isUseCustomCheck();
}