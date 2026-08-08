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

import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.config.TaskIdGenerator;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PlaywrightRenderStrategyRouter}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class PlaywrightRenderStrategyRouterTest {

    private static class StubStrategy extends AbstractPlaywrightRenderStrategy<WkhtmlRenderBO> {
        private final RenderType type;
        StubStrategy(RenderType type) {
            super(RenderConfig.builder().build(), () -> "1");
            this.type = type;
        }
        @Override public RenderType getRenderType() { return type; }
        @Override protected java.util.List<io.github.easy4j.playwright.render.bo.PageRenderBO> doGenerate(WkhtmlRenderBO b) { return Collections.emptyList(); }
        @Override protected WkhtmlRenderResultVO doPacking(WkhtmlRenderBO b, java.util.List<io.github.easy4j.playwright.render.bo.PageRenderBO> p) { return new WkhtmlRenderResultVO(); }
    }

    @Test
    @DisplayName("Router dispatches by RenderType")
    void route_dispatchesByType() {
        StubStrategy image = new StubStrategy(RenderType.TO_IMAGE_BUFFER);
        StubStrategy pdf = new StubStrategy(RenderType.TO_PDF_FILE);
        PlaywrightRenderStrategyRouter router =
                new PlaywrightRenderStrategyRouter(Arrays.asList(image, pdf));

        assertEquals(2, router.size());
        assertEquals(image, router.route(RenderType.TO_IMAGE_BUFFER));
        assertEquals(pdf, router.route(RenderType.TO_PDF_FILE));
    }

    @Test
    @DisplayName("Router throws on unknown RenderType")
    void route_unknownType_throws() {
        PlaywrightRenderStrategyRouter router =
                new PlaywrightRenderStrategyRouter(Collections.emptyList());
        assertThrows(IllegalArgumentException.class,
                () -> router.route(RenderType.TO_IMAGE_FILE));
    }
}