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
package io.github.easy4j.playwright.task.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract test for {@link TaskStateStore}.
 *
 * <p>This test suite is run against every implementation (InMemory, Redis, etc.)
 * via {@code @Suite} or by manually extending this abstract class. It enforces
 * all behavioral requirements derived from the original ddd4j-cloud-cmpt-playwright
 * use cases plus boundary conditions.</p>
 *
 * <p>Subclasses provide the concrete store via {@link #newStore()}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
abstract class TaskStateStoreContractTest {

    /** Subclass hook for the concrete store under test. */
    protected abstract TaskStateStore newStore();

    // ====================================================================
    // Scenario 1: 首次渲染 — empty hash → put one entry → set ttl
    // (Mirrors ddd4j-cloud AbstractPlaywrightRenderStrategy#render L144-L211)
    // ====================================================================

    @Test
    @DisplayName("First-time render: getAllStates on fresh taskId returns empty Map")
    void firstTimeRender_getAllStatesIsEmpty() {
        TaskStateStore store = newStore();
        Map<String, String> states = store.getAllStates("task-fresh-1");
        assertNotNull(states, "getAllStates must never return null");
        assertTrue(states.isEmpty(), "fresh task must have no states");
    }

    @Test
    @DisplayName("First-time render: setState + getState roundtrip")
    void firstTimeRender_setAndGet() {
        TaskStateStore store = newStore();
        store.setState("task-1", "page-A", "WAITING");
        assertEquals("WAITING", store.getState("task-1", "page-A"));
    }

    @Test
    @DisplayName("First-time render: setAllStates + getAllStates roundtrip with ttl")
    void firstTimeRender_setAllAndGetAll() {
        TaskStateStore store = newStore();
        Map<String, String> incoming = new HashMap<>();
        incoming.put("page-A", "WAITING");
        incoming.put("page-B", "WAITING");
        incoming.put("page-C", "WAITING");

        store.setAllStates("task-1", incoming, Duration.ofDays(2));

        Map<String, String> readBack = store.getAllStates("task-1");
        assertEquals(incoming, readBack);
    }

    // ====================================================================
    // Scenario 2: 重试渲染 — task already exists, mutate single page
    // (Mirrors L200-L211 retry path)
    // ====================================================================

    @Test
    @DisplayName("Retry render: existing state must be preserved when merging")
    void retryRender_preservesExistingState() {
        TaskStateStore store = newStore();

        // 首次渲染
        store.setState("task-retry-1", "page-A", "WAITING");
        store.setState("task-retry-1", "page-B", "WAITING");

        // 模拟 page-A 完成,page-B 仍然 WAITING
        store.setState("task-retry-1", "page-A", "SUCCESS");

        Map<String, String> after = store.getAllStates("task-retry-1");
        assertEquals("SUCCESS", after.get("page-A"),
            "page-A must reflect updated state");
        assertEquals("WAITING", after.get("page-B"),
            "page-B must keep original state");
        assertEquals(2, after.size(), "no other pages should appear");
    }

    @Test
    @DisplayName("Retry render: bulk update merges with existing data")
    void retryRender_bulkMergeKeepsUntouched() {
        TaskStateStore store = newStore();
        // 已存在
        store.setState("task-bulk", "p1", "WAITING");
        store.setState("task-bulk", "p2", "SUCCESS");

        // 批量写入 p3=WAITING, 应不破坏 p1/p2
        store.setAllStates("task-bulk",
            Collections.singletonMap("p3", "WAITING"),
            Duration.ofDays(2));

        Map<String, String> after = store.getAllStates("task-bulk");
        assertEquals("WAITING", after.get("p1"));
        assertEquals("SUCCESS", after.get("p2"));
        assertEquals("WAITING", after.get("p3"));
        assertEquals(3, after.size());
    }

    // ====================================================================
    // Scenario 3: PDF merge 阶段 — hmGet 查询, hmSet 更新
    // (Mirrors PageScreenshotMergeToPdfSupplier L138 / L207)
    // ====================================================================

    @Test
    @DisplayName("PDF merge: query single state after bulk write")
    void pdfMerge_queryAfterBulkWrite() {
        TaskStateStore store = newStore();
        Map<String, String> bulk = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            bulk.put("page-" + i, "SUCCESS");
        }
        store.setAllStates("task-pdf-merge", bulk, Duration.ofDays(2));

        for (int i = 0; i < 5; i++) {
            assertEquals("SUCCESS", store.getState("task-pdf-merge", "page-" + i));
        }
    }

    // ====================================================================
    // Boundary: null-safety on all inputs
    // ====================================================================

    @Test
    @DisplayName("Null-safety: null taskId is no-op for writes, returns empty for reads")
    void nullSafety_nullTaskId() {
        TaskStateStore store = newStore();
        assertDoesNotThrow(() -> store.setState(null, "k", "v"));
        assertDoesNotThrow(() -> store.setAllStates(null, Collections.singletonMap("k", "v"), Duration.ofDays(1)));
        assertDoesNotThrow(() -> store.getState(null, "k"));
        assertDoesNotThrow(() -> store.clear(null));
        assertDoesNotThrow(() -> store.expire(null, Duration.ofDays(1)));
        assertTrue(store.getAllStates(null).isEmpty());
    }

    @Test
    @DisplayName("Null-safety: null uniqueId/key is no-op")
    void nullSafety_nullUniqueId() {
        TaskStateStore store = newStore();
        assertDoesNotThrow(() -> store.setState("t1", null, "v"));
        assertDoesNotThrow(() -> store.setState("t1", "k", null));
        assertNull(store.getState("t1", null));
        assertNull(store.getState("t1", "k"));
    }

    @Test
    @DisplayName("Null-safety: null stateMap is no-op for bulk write")
    void nullSafety_nullStateMap() {
        TaskStateStore store = newStore();
        assertDoesNotThrow(() -> store.setAllStates("t1", null, Duration.ofDays(1)));
    }

    @Test
    @DisplayName("Null-safety: null ttl is allowed (means never expire)")
    void nullSafety_nullTtl() {
        TaskStateStore store = newStore();
        assertDoesNotThrow(() -> store.setAllStates("t1", Collections.singletonMap("k", "v"), null));
        assertDoesNotThrow(() -> store.expire("t1", null));
    }

    @Test
    @DisplayName("Null-safety: getAllStates returns immutable view")
    void nullSafety_getAllStatesIsImmutable() {
        TaskStateStore store = newStore();
        store.setState("t1", "k", "v");
        Map<String, String> view = store.getAllStates("t1");
        assertThrows(UnsupportedOperationException.class,
            () -> view.put("k2", "v2"),
            "getAllStates must return immutable map to prevent callers bypassing the store");
    }

    private static void assertThrows(Class<? extends Throwable> expected,
                                      org.junit.jupiter.api.function.Executable runnable,
                                      String msg) {
        try {
            org.junit.jupiter.api.Assertions.assertThrows(expected, runnable);
        } catch (AssertionError e) {
            throw new AssertionError(msg, e);
        }
    }

    // ====================================================================
    // Boundary: task isolation — different taskIds don't see each other
    // ====================================================================

    @Test
    @DisplayName("Task isolation: state in task A is invisible from task B")
    void taskIsolation_independentStores() {
        TaskStateStore store = newStore();
        store.setState("task-A", "page-1", "SUCCESS");
        store.setState("task-B", "page-1", "FAIL");

        assertEquals("SUCCESS", store.getState("task-A", "page-1"));
        assertEquals("FAIL", store.getState("task-B", "page-1"));
        assertEquals(1, store.getAllStates("task-A").size());
        assertEquals(1, store.getAllStates("task-B").size());
    }

    @Test
    @DisplayName("Task isolation: clear on task A does not affect task B")
    void taskIsolation_clearOnePreservesOther() {
        TaskStateStore store = newStore();
        store.setState("task-A", "p", "SUCCESS");
        store.setState("task-B", "p", "SUCCESS");

        store.clear("task-A");

        assertTrue(store.getAllStates("task-A").isEmpty());
        assertEquals("SUCCESS", store.getState("task-B", "p"));
    }

    // ====================================================================
    // Boundary: clearing and re-creating a task with the same id
    // ====================================================================

    @Test
    @DisplayName("Lifecycle: clear then re-set works without leftover state")
    void lifecycle_clearThenReuse() {
        TaskStateStore store = newStore();
        store.setState("task-reuse", "p1", "WAITING");
        store.setState("task-reuse", "p2", "SUCCESS");
        store.clear("task-reuse");

        // 再次使用相同 taskId
        store.setState("task-reuse", "p3", "WAITING");
        Map<String, String> after = store.getAllStates("task-reuse");

        assertAll("after reuse, only new state should remain",
            () -> assertEquals(1, after.size()),
            () -> assertEquals("WAITING", after.get("p3")),
            () -> assertFalse(after.containsKey("p1")),
            () -> assertFalse(after.containsKey("p2"))
        );
    }

    // ====================================================================
    // Boundary: empty taskId is treated as a valid key (not collapsed)
    // ====================================================================

    @Test
    @DisplayName("Edge case: empty-string taskId is a valid, distinct key")
    void edgeCase_emptyTaskId() {
        TaskStateStore store = newStore();
        store.setState("", "p1", "WAITING");
        assertEquals("WAITING", store.getState("", "p1"));
        store.clear("");
        assertTrue(store.getAllStates("").isEmpty());
    }

    // ====================================================================
    // Concurrency: same key set concurrently must not lose data
    // ====================================================================

    @Test
    @DisplayName("Concurrency: parallel setState on different uniqueIds must all succeed")
    void concurrency_parallelSetState() throws Exception {
        TaskStateStore store = newStore();
        int threadCount = 16;
        int keysPerThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger(0);

        try {
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < keysPerThread; i++) {
                            store.setState("task-concurrent",
                                "thread-" + threadId + "-key-" + i,
                                "VALUE-" + i);
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                });
            }
            start.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS),
                "concurrent writes timed out");

            assertEquals(0, errors.get(), "no thread should fail");
            Map<String, String> result = store.getAllStates("task-concurrent");
            assertEquals(threadCount * keysPerThread, result.size(),
                "all keys must be persisted");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("Concurrency: parallel setState on same uniqueId converges to one value")
    void concurrency_parallelSetStateSameKey() throws Exception {
        TaskStateStore store = newStore();
        int threadCount = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                pool.submit(() -> {
                    try {
                        start.await();
                        // Same key, different values
                        store.setState("task-converge", "shared-key",
                            "VALUE-from-thread-" + threadId);
                    } catch (InterruptedException ignored) {
                    }
                });
            }
            start.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

            String finalValue = store.getState("task-converge", "shared-key");
            assertNotNull(finalValue, "exactly one value must survive");
            assertTrue(finalValue.startsWith("VALUE-from-thread-"),
                "value must be one of the writes: " + finalValue);
        } finally {
            pool.shutdownNow();
        }
    }

    // ====================================================================
    // Behavioral: empty stateMap is no-op (matches hmSet semantics)
    // ====================================================================

    @Test
    @DisplayName("Empty stateMap does not create the task")
    void emptyStateMap_doesNotCreateTask() {
        TaskStateStore store = newStore();
        store.setAllStates("task-empty", Collections.emptyMap(), Duration.ofDays(1));
        assertTrue(store.getAllStates("task-empty").isEmpty(),
            "empty bulk write must not materialize a task entry");
    }

    @Test
    @DisplayName("Empty stateMap with null values is filtered")
    void stateMapWithNullValues_filtered() {
        TaskStateStore store = newStore();
        Map<String, String> mixed = new HashMap<>();
        mixed.put("good", "WAITING");
        mixed.put("null-value", null);
        mixed.put(null, "WAITING");

        store.setAllStates("task-mixed", mixed, Duration.ofDays(1));

        Map<String, String> after = store.getAllStates("task-mixed");
        assertEquals(1, after.size(), "only valid entries should be persisted");
        assertEquals("WAITING", after.get("good"));
    }

    // ====================================================================
    // Behavioral: getAllStates reflects in-flight changes
    // ====================================================================

    @Test
    @DisplayName("Read-after-write consistency: getAllStates sees the just-written entries")
    void readAfterWriteConsistency() {
        TaskStateStore store = newStore();

        store.setState("t1", "p1", "A");
        store.setState("t1", "p2", "B");
        store.setState("t1", "p3", "C");

        Map<String, String> view = store.getAllStates("t1");
        assertEquals(3, view.size());
        assertEquals("A", view.get("p1"));
        assertEquals("B", view.get("p2"));
        assertEquals("C", view.get("p3"));
    }
}