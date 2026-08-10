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
package io.github.easy4j.playwright.render.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for render enums (sanity / lookup).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class EnumsTest {

    @Test
    @DisplayName("RenderType: 6 known values + desc round-trip")
    void renderType_values() {
        assertEquals(6, RenderType.values().length);
        assertEquals("PDF output to file", RenderType.TO_PDF_FILE.getDesc());
        assertEquals("Screenshot output to in-memory buffer", RenderType.TO_IMAGE_BUFFER.getDesc());
    }

    @Test
    @DisplayName("RenderState: lookup by int code")
    void renderState_lookup() {
        assertEquals(RenderState.WAITING, RenderState.getRenderState(0));
        assertEquals(RenderState.GENERATING, RenderState.getRenderState(1));
        assertEquals(RenderState.SUCCESS, RenderState.getRenderState(2));
        assertEquals(RenderState.FAIL, RenderState.getRenderState(3));
        assertNull(RenderState.getRenderState(99));
    }

    @Test
    @DisplayName("CheckState: lookup by int code")
    void checkState_lookup() {
        assertEquals(CheckState.SUCCESS, CheckState.getByName(1));
        assertEquals(CheckState.WEB_CHECK_FAIL, CheckState.getByName(2));
        assertEquals(CheckState.IMG_CHECK_FAIL, CheckState.getByName(3));
        assertNull(CheckState.getByName(0));
    }

    @Test
    @DisplayName("ResourceType.getByName: known values + fallback to 'other'")
    void resourceType_lookup() {
        assertEquals(ResourceType.stylesheet, ResourceType.getByName("stylesheet"));
        assertEquals(ResourceType.xhr, ResourceType.getByName("xhr"));
        assertEquals(ResourceType.other, ResourceType.getByName("garbage"));
        assertEquals(ResourceType.other, ResourceType.getByName(null));
    }

    @Test
    @DisplayName("ResourceType.isNeedRetry: true for stylesheet/script/xhr/fetch")
    void resourceType_isNeedRetry() {
        assertTrue(ResourceType.stylesheet.isNeedRetry());
        assertTrue(ResourceType.script.isNeedRetry());
        assertTrue(ResourceType.xhr.isNeedRetry());
        assertTrue(ResourceType.fetch.isNeedRetry());
        assertFalse(ResourceType.image.isNeedRetry());
        assertFalse(ResourceType.document.isNeedRetry());
    }

    @Test
    @DisplayName("ResourceType.isNeedRecord404: true for media-bearing types")
    void resourceType_isNeedRecord404() {
        assertTrue(ResourceType.image.isNeedRecord404());
        assertTrue(ResourceType.font.isNeedRecord404());
        assertTrue(ResourceType.stylesheet.isNeedRecord404());
        assertFalse(ResourceType.document.isNeedRecord404());
        assertFalse(ResourceType.websocket.isNeedRecord404());
    }
}