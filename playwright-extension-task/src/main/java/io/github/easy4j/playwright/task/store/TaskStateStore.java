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
import java.util.Map;

/**
 * Abstract rendering task state store.
 *
 * <p>Tracks per-{@code uniqueId} render state under a {@code taskId} namespace.
 * Original implementation used Redis Hash; this interface abstracts it so that:
 * <ul>
 *   <li>{@link InMemoryTaskStateStore} can be used in tests and single-node scenarios</li>
 *   <li>A Redis-backed implementation can be wired in via Spring Boot starter</li>
 *   <li>A Caffeine-backed implementation can be wired for embedded scenarios</li>
 * </ul>
 *
 * <p><b>Null-safety:</b> all methods tolerate {@code null} parameters —
 * {@code null} taskId / uniqueId / stateMap / value are no-ops on writes and
 * yield empty / {@code null} on reads. This matches the original code's defensive
 * behaviour around Redis client errors.
 *
 * <p><b>Immutability:</b> {@link #getAllStates(String)} returns an immutable
 * view; callers cannot bypass the store by mutating the returned map.
 *
 * <p><b>Thread-safety:</b> implementations must be thread-safe. Concurrent
 * writes to different keys, and concurrent writes to the same key, must all
 * be safe.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface TaskStateStore {

    /**
     * Get all {@code uniqueId → state} pairs for the given task.
     *
     * @param taskId task identifier; {@code null} returns an empty map
     * @return immutable view of the state map; never {@code null}
     */
    Map<String, String> getAllStates(String taskId);

    /**
     * Get the state of a single {@code uniqueId} within a task.
     *
     * @param taskId    task identifier; {@code null} returns {@code null}
     * @param uniqueId  page-level identifier; {@code null} returns {@code null}
     * @return the stored state, or {@code null} if never set
     */
    String getState(String taskId, String uniqueId);

    /**
     * Set the state of a single {@code uniqueId}.
     *
     * <p>No-op when any argument is {@code null}.</p>
     *
     * @param taskId    task identifier
     * @param uniqueId  page-level identifier
     * @param state     the state value (typically a {@code RenderState.name()})
     */
    void setState(String taskId, String uniqueId, String state);

    /**
     * Bulk-set multiple {@code uniqueId} states, then apply a TTL.
     *
     * <p>Implementations should make this atomic from the caller's perspective:
     * a single network round-trip in the Redis case, a single {@code putAll}
     * in the InMemory case. Entries with {@code null} keys or {@code null}
     * values are skipped.</p>
     *
     * @param taskId    task identifier; {@code null} is no-op
     * @param stateMap  entries to write; {@code null} or empty is no-op
     * @param ttl       time-to-live for the whole task entry; {@code null}
     *                  means do not change expiration
     */
    void setAllStates(String taskId, Map<String, String> stateMap, Duration ttl);

    /**
     * Delete all state for the given task.
     *
     * @param taskId task identifier; {@code null} is no-op
     */
    void clear(String taskId);

    /**
     * Set or extend the TTL for the given task.
     *
     * <p>For implementations that don't support TTL (e.g. InMemory), this is
     * a no-op — it exists so callers can keep their code agnostic to the
     * implementation choice.</p>
     *
     * @param taskId task identifier; {@code null} is no-op
     * @param ttl    new time-to-live; {@code null} is no-op
     */
    void expire(String taskId, Duration ttl);
}