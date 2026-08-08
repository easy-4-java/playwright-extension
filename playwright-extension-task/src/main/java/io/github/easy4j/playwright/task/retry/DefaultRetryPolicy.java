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

import lombok.Builder;

import java.time.Duration;

/**
 * Default {@link RetryPolicy} with linear backoff at a 0.75 load factor.
 *
 * <p>The 0.75 multiplier matches the original {@code TimeUtil.getRetryTimeout}
 * formula in ddd4j-cloud-cmpt-playwright:
 * each retry increases the timeout by {@code loadFactor × attempt}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Builder
public class DefaultRetryPolicy implements RetryPolicy {

    public static final double DEFAULT_LOAD_FACTOR = 0.75;

    @Builder.Default
    private final int maxRetries = 3;

    @Builder.Default
    private final Duration retryInterval = Duration.ofSeconds(3);

    @Builder.Default
    private final double loadFactor = DEFAULT_LOAD_FACTOR;

    /** No-arg constructor uses all defaults. */
    public DefaultRetryPolicy() {
        this(3, Duration.ofSeconds(3), DEFAULT_LOAD_FACTOR);
    }

    /** All-args constructor for direct instantiation. */
    public DefaultRetryPolicy(int maxRetries, Duration retryInterval, double loadFactor) {
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval == null ? Duration.ofSeconds(3) : retryInterval;
        this.loadFactor = loadFactor;
    }

    @Override
    public int maxRetries() {
        return maxRetries;
    }

    @Override
    public Duration retryInterval() {
        return retryInterval;
    }

    public double getLoadFactor() {
        return loadFactor;
    }

    @Override
    public Duration nextTimeout(int attempt, Duration initialTimeout) {
        if (attempt <= 1) {
            return initialTimeout;
        }
        long boosted = (long) (initialTimeout.toMillis() * (1.0 + loadFactor * (attempt - 1)));
        return Duration.ofMillis(boosted);
    }
}