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

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Pure-JDK {@link TaskStateStore} backed by a nested {@link ConcurrentHashMap}.
 *
 * <p>Suitable for:
 * <ul>
 *   <li>Unit tests (no external dependencies)</li>
 *   <li>Single-node deployments where Redis is undesirable</li>
 *   <li>Embedded scenarios (Spring Boot without distributed coordination)</li>
 * </ul>
 *
 * <p>The TTL parameter is accepted but ignored — InMemory state lives until
 * {@link #clear(String)} is called or the JVM exits. If you need bounded
 * retention, use the Caffeine- or Redis-backed implementation.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class InMemoryTaskStateStore implements TaskStateStore {

    /**
     * Outer key = taskId, inner key = uniqueId, value = state name.
     */
    private final ConcurrentMap<String, ConcurrentMap<String, String>> store = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getAllStates(String taskId) {
        if (taskId == null) {
            return Collections.emptyMap();
        }
        ConcurrentMap<String, String> inner = store.get(taskId);
        if (inner == null) {
            return Collections.emptyMap();
        }
        // Defensive copy wrapped as unmodifiable — caller cannot mutate store internals.
        return Collections.unmodifiableMap(new HashMap<>(inner));
    }

    @Override
    public String getState(String taskId, String uniqueId) {
        if (taskId == null || uniqueId == null) {
            return null;
        }
        ConcurrentMap<String, String> inner = store.get(taskId);
        return inner == null ? null : inner.get(uniqueId);
    }

    @Override
    public void setState(String taskId, String uniqueId, String state) {
        if (taskId == null || uniqueId == null || state == null) {
            return;
        }
        store.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>())
                .put(uniqueId, state);
    }

    @Override
    public void setAllStates(String taskId, Map<String, String> stateMap, Duration ttl) {
        if (taskId == null || stateMap == null || stateMap.isEmpty()) {
            return;
        }
        ConcurrentMap<String, String> inner =
                store.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>());
        stateMap.forEach((k, v) -> {
            if (k != null && v != null) {
                inner.put(k, v);
            }
        });
        // ttl intentionally ignored — InMemory has no expiration. See class javadoc.
    }

    @Override
    public void clear(String taskId) {
        if (taskId == null) {
            return;
        }
        store.remove(taskId);
    }

    @Override
    public void expire(String taskId, Duration ttl) {
        // ttl intentionally ignored — InMemory has no expiration.
        // The argument is reserved for future scheduled-eviction support.
    }
}