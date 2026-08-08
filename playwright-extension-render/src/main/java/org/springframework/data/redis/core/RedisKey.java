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

/**
 * Shim for the original {@code RedisKey} helper from
 * {@code redistpl-plus-spring-boot-starter}.
 *
 * <p>Only the {@link #getKeyStr} method is used by the migrated render code
 * (via {@code BizRedisKey.RENDER_STATE}).</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public final class RedisKey {

    private RedisKey() {}

    /**
     * Build a colon-separated Redis key from a prefix and parts.
     */
    public static String getKeyStr(String prefix, Object... parts) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object p : parts) {
            sb.append(":").append(p);
        }
        return sb.toString();
    }
}