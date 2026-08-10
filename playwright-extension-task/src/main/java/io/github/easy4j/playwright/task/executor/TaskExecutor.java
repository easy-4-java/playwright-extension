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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction for submitting asynchronous tasks.
 *
 * <p>Replaces the original ddd4j-cloud-cmpt-playwright pattern of
 * {@code @Resource ThreadPoolExecutor dtpToImageExecutor} field injection,
 * so the render module can run without Spring / dynamic-tp.</p>
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@link InMemoryTaskExecutor} — wraps a JDK {@link java.util.concurrent.ThreadPoolExecutor}
 *       created on demand. Used in tests and single-node scenarios.</li>
 *   <li>Spring Boot starter provides an adapter for dynamic-tp named pools.</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface TaskExecutor {

    /**
     * Submit a single task.
     *
     * @param taskName logical name (for logging / metrics; not required to be unique)
     * @param task     the work to execute
     * @param <T>      result type
     * @return a future that completes with the task result, or exceptionally on error
     */
    <T> CompletableFuture<T> submit(String taskName, Callable<T> task);

    /**
     * Submit multiple tasks and return a future that completes when all of them finish
     * (successfully or exceptionally). Individual results are accessible via the returned
     * list of futures.
     *
     * @param taskName logical name applied to every task
     * @param tasks    the work to execute
     * @param <T>      result type
     * @return list of futures in the same order as {@code tasks}
     */
    <T> List<CompletableFuture<T>> submitAll(String taskName, List<? extends Callable<T>> tasks);
}