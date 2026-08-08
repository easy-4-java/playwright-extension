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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultRetryPolicy}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class DefaultRetryPolicyTest {

    @Test
    @DisplayName("defaults: maxRetries=3, retryInterval=3s, loadFactor=0.75")
    void defaults_matchOriginalPlaywrightRenderProperties() {
        DefaultRetryPolicy policy = DefaultRetryPolicy.builder().build();
        assertEquals(3, policy.maxRetries());
        assertEquals(Duration.ofSeconds(3), policy.retryInterval());
        assertEquals(0.75, policy.getLoadFactor(), 0.0001);
    }

    @Test
    @DisplayName("nextTimeout returns the initial timeout for attempt 1")
    void nextTimeout_firstAttemptReturnsInitial() {
        DefaultRetryPolicy policy = DefaultRetryPolicy.builder().build();
        assertEquals(Duration.ofSeconds(30),
                policy.nextTimeout(1, Duration.ofSeconds(30)));
    }

    @Test
    @DisplayName("nextTimeout scales by loadFactor on subsequent attempts")
    void nextTimeout_scalesByLoadFactor() {
        DefaultRetryPolicy policy = DefaultRetryPolicy.builder().build();
        Duration initial = Duration.ofSeconds(10);
        // attempt=2: 10 * (1 + 0.75 * 1) = 17.5s
        assertEquals(Duration.ofMillis(17_500),
                policy.nextTimeout(2, initial));
        // attempt=3: 10 * (1 + 0.75 * 2) = 25s
        assertEquals(Duration.ofMillis(25_000),
                policy.nextTimeout(3, initial));
        // attempt=4: 10 * (1 + 0.75 * 3) = 32.5s
        assertEquals(Duration.ofMillis(32_500),
                policy.nextTimeout(4, initial));
    }

    @Test
    @DisplayName("custom loadFactor changes the backoff shape")
    void nextTimeout_customLoadFactor() {
        DefaultRetryPolicy policy = DefaultRetryPolicy.builder()
                .loadFactor(1.0)
                .build();
        Duration initial = Duration.ofSeconds(10);
        // attempt=2 with loadFactor=1.0: 10 * (1 + 1.0 * 1) = 20s
        assertEquals(Duration.ofSeconds(20),
                policy.nextTimeout(2, initial));
    }

    @Test
    @DisplayName("timeout always increases (positive backoff)")
    void nextTimeout_monotonicallyIncreasing() {
        DefaultRetryPolicy policy = DefaultRetryPolicy.builder().build();
        Duration initial = Duration.ofSeconds(5);
        Duration prev = initial;
        for (int attempt = 2; attempt <= 10; attempt++) {
            Duration next = policy.nextTimeout(attempt, initial);
            assertTrue(next.compareTo(prev) > 0,
                    "timeout must increase: attempt=" + attempt + " prev=" + prev + " next=" + next);
            prev = next;
        }
    }
}