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
package org.springframework.data.redis.core;

import java.time.Duration;
import java.util.Map;

/**
 * Shim interface for the original {@code RedisOperationTemplate} from
 * {@code redistpl-plus-spring-boot-starter}.
 *
 * <p>Declares only the two hash operations actually used by the migrated
 * render code ({@code hmGet} / {@code hmSet}). The Spring Boot starter
 * supplies the real Redis-backed implementation; this interface lets the
 * render module compile and run without forcing the Redis starter on every
 * consumer.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface RedisOperationTemplate {

    /**
     * Get all fields of a hash.
     *
     * @param key hash key
     * @return field→value map (empty map if key does not exist); never {@code null}
     */
    Map<Object, Object> hmGet(String key);

    /**
     * Set multiple fields of a hash, optionally with TTL.
     *
     * @param key      hash key
     * @param values   field→value map to write
     * @param ttl      time-to-live; {@code null} to leave expiration unchanged
     */
    void hmSet(String key, Map<?, ?> values, Duration ttl);
}