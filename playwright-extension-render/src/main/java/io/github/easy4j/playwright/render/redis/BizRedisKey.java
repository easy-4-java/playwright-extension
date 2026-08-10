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
package io.github.easy4j.playwright.render.redis;

/**
 * Render state key builder. Simplified from the original enum (which depended
 * on {@code RedisKey.getKeyStr}) to a plain string concatenation, so the render
 * module has zero Spring/Redis dependencies.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public final class BizRedisKey {

    private BizRedisKey() {
    }

    /**
     * Build the full Redis key for a render-state hash.
     *
     * @param taskId render task id
     * @return key like {@code "pdf-render:state:{taskId}"}
     */
    public static String renderStateKey(String taskId) {
        return BizRedisKeyConstant.RENDER_STATE_KEY + ":" + taskId;
    }
}