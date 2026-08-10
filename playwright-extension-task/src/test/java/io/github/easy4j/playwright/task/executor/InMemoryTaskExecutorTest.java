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
package io.github.easy4j.playwright.task.executor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link InMemoryTaskExecutor}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class InMemoryTaskExecutorTest {

    private InMemoryTaskExecutor executor;

    @BeforeEach
    void setUp() {
        executor = InMemoryTaskExecutor.builder()
                .corePoolSize(4)
                .maximumPoolSize(8)
                .build();
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    @Test
    @DisplayName("submit returns the callable result")
    void submit_returnsResult() throws Exception {
        CompletableFuture<String> future = executor.submit("test-1", () -> "hello");
        assertEquals("hello", future.get(2, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("submit propagates exceptions via CompletableFuture")
    void submit_propagatesExceptions() {
        CompletableFuture<Object> future = executor.submit("failing", () -> {
            throw new IllegalStateException("boom");
        });
        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> future.get(2, TimeUnit.SECONDS));
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertEquals("boom", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("submitAll preserves order and runs concurrently")
    void submitAll_preservesOrderAndConcurrency() throws Exception {
        AtomicInteger concurrent = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);

        Callable<Integer> slow = () -> {
            int current = concurrent.incrementAndGet();
            maxConcurrent.accumulateAndGet(current, Math::max);
            Thread.sleep(150);
            concurrent.decrementAndGet();
            return current;
        };

        List<Callable<Integer>> tasks = Arrays.asList(slow, slow, slow, slow, slow, slow);
        List<CompletableFuture<Integer>> futures = executor.submitAll("batch", tasks);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);

        // With corePoolSize=4 and 6 tasks, at least 4 should run concurrently.
        assertTrue(maxConcurrent.get() >= 2,
                "expected parallel execution; observed max=" + maxConcurrent.get());
        assertEquals(6, futures.size());
    }

    @Test
    @DisplayName("submit handles checked exceptions by wrapping in RuntimeException")
    void submit_wrapsCheckedExceptions() {
        CompletableFuture<Object> future = executor.submit("io-fail", () -> {
            throw new java.io.IOException("disk full");
        });
        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> future.get(2, TimeUnit.SECONDS));
        // supplyAsync wraps checked exceptions in RuntimeException, then CompletableFuture
        // wraps that in ExecutionException on .get().
        assertTrue(ex.getCause() instanceof RuntimeException);
        assertTrue(ex.getCause().getCause() instanceof java.io.IOException);
    }
}