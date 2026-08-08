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

/**
 * Abstraction for generating per-render task IDs.
 *
 * <p>The original ddd4j-cloud-cmpt-playwright code injected
 * {@code io.ddd4j.boot.core.sequence.Sequence}; this interface lets the render
 * module stay free of that boot framework.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@FunctionalInterface
public interface TaskIdGenerator {
    String next();
}