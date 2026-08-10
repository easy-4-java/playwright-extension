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
package io.github.easy4j.playwright.render.bo;

import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.RenderState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link PageRenderBO} chainable setters and defaults.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class PageRenderBOTest {

    @Test
    @DisplayName("Chainable setters return the same instance")
    void chainableSetters_returnSameInstance() {
        PageRenderBO bo = new PageRenderBO();
        PageRenderBO result = bo.setIndex(0).setUrl("https://x").setRenderState(RenderState.WAITING);
        assertSame(bo, result);
        assertEquals(0, bo.getIndex());
        assertEquals("https://x", bo.getUrl());
        assertEquals(RenderState.WAITING, bo.getRenderState());
    }

    @Test
    @DisplayName("Default resourceLoadState is an empty non-null map")
    void defaultResourceLoadState_isEmptyMap() {
        PageRenderBO bo = new PageRenderBO();
        assertNotNull(bo.getResourceLoadState());
        assertEquals(0, bo.getResourceLoadState().size());
    }

    @Test
    @DisplayName("CheckState / RenderState round-trip through setters")
    void enumRoundTrip() {
        PageRenderBO bo = new PageRenderBO()
                .setCheckState(CheckState.IMG_CHECK_FAIL)
                .setRenderState(RenderState.FAIL)
                .setCheckFailedReason("bad")
                .setRenderFailedReason("oops");
        assertEquals(CheckState.IMG_CHECK_FAIL, bo.getCheckState());
        assertEquals(RenderState.FAIL, bo.getRenderState());
        assertEquals("bad", bo.getCheckFailedReason());
        assertEquals("oops", bo.getRenderFailedReason());
    }
}