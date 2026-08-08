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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default {@link TaskIdGenerator} implementations.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public final class TaskIdGenerators {

    private TaskIdGenerators() {}

    /** Monotonically-increasing long-as-string. Suitable for single-node use. */
    public static TaskIdGenerator sequential() {
        AtomicLong counter = new AtomicLong(System.currentTimeMillis());
        return () -> Long.toString(counter.incrementAndGet());
    }

    /** UUID-based generator. Suitable for distributed scenarios. */
    public static TaskIdGenerator uuid() {
        return () -> UUID.randomUUID().toString().replace("-", "");
    }
}