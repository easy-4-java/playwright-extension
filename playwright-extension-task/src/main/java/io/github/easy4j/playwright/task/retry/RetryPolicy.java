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
package io.github.easy4j.playwright.task.retry;

import java.time.Duration;

/**
 * Retry policy for the rendering pipeline.
 *
 * <p>Encapsulates the original ddd4j-cloud-cmpt-playwright
 * {@code playwrightRenderProperties.reloadLimit / reloadWait} logic in an
 * injectable abstraction, so render code doesn't depend on Spring properties.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface RetryPolicy {

    /** Maximum number of retries before giving up. */
    int maxRetries();

    /** Wait time between retries. */
    Duration retryInterval();

    /**
     * Compute the next attempt's timeout based on the original 0.75× backoff
     * from ddd4j-cloud-cmpt-playwright's {@code TimeUtil}.
     *
     * @param attempt       1-based attempt number (1 = first retry)
     * @param initialTimeout base timeout before any backoff
     * @return timeout to use for the given attempt
     */
    Duration nextTimeout(int attempt, Duration initialTimeout);
}