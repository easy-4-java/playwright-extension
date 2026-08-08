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
package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.enums.RenderType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Routes a {@link RenderType} to the corresponding {@link PlaywrightRenderStrategy}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class PlaywrightRenderStrategyRouter {

    private final Map<RenderType, PlaywrightRenderStrategy<?>> strategies;

    public PlaywrightRenderStrategyRouter(Collection<? extends PlaywrightRenderStrategy<?>> strategies) {
        Map<RenderType, PlaywrightRenderStrategy<?>> map = new HashMap<>();
        for (PlaywrightRenderStrategy<?> s : strategies) {
            map.put(s.getRenderType(), s);
        }
        this.strategies = map;
    }

    @SuppressWarnings("unchecked")
    public <B extends io.github.easy4j.playwright.render.bo.WkhtmlRenderBO>
    PlaywrightRenderStrategy<B> route(RenderType type) {
        PlaywrightRenderStrategy<?> s = strategies.get(type);
        if (s == null) {
            throw new IllegalArgumentException("No strategy registered for " + type);
        }
        return (PlaywrightRenderStrategy<B>) s;
    }

    public int size() {
        return strategies.size();
    }
}