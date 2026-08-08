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

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Default {@link TaskExecutor} backed by a JDK {@link ThreadPoolExecutor}.
 *
 * <p>Each instance owns its pool and shuts it down on {@link #close()}. Pools
 * are sized via the constructor parameters; the work queue is unbounded by
 * default (matching the original dynamic-tp {@code queue-capacity=20000}
 * behaviour), with a {@code CallerRunsPolicy} so the submitter slows down
 * instead of dropping work.</p>
 *
 * <p>Instances are intended to be created once and reused (e.g. one per
 * named pool). Use the {@link #builder()} for ergonomics.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public class InMemoryTaskExecutor implements TaskExecutor, AutoCloseable {

    private final ThreadPoolExecutor pool;

    public InMemoryTaskExecutor(int corePoolSize, int maxPoolSize) {
        this(corePoolSize, maxPoolSize, Integer.MAX_VALUE);
    }

    public InMemoryTaskExecutor(int corePoolSize, int maxPoolSize, int queueCapacity) {
        this.pool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public <T> CompletableFuture<T> submit(String taskName, Callable<T> task) {
        log.debug("Submitting task '{}'", taskName);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, pool);
    }

    @Override
    public <T> List<CompletableFuture<T>> submitAll(String taskName, List<? extends Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = new ArrayList<>(tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            futures.add(submit(taskName + "-" + i, tasks.get(i)));
        }
        return futures;
    }

    @Override
    public void close() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Convenience builder mirroring dynamic-tp's pool config shape. */
    public static final class Builder {
        private int corePoolSize = 4;
        private int maximumPoolSize = 8;
        private int queueCapacity = Integer.MAX_VALUE;

        public Builder corePoolSize(int v) { this.corePoolSize = v; return this; }
        public Builder maximumPoolSize(int v) { this.maximumPoolSize = v; return this; }
        public Builder queueCapacity(int v) { this.queueCapacity = v; return this; }

        public InMemoryTaskExecutor build() {
            return new InMemoryTaskExecutor(corePoolSize, maximumPoolSize, queueCapacity);
        }
    }
}