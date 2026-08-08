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
package io.github.easy4j.playwright.image.checker;

import io.github.easy4j.playwright.image.enums.ImagePageSize;
import io.github.easy4j.playwright.image.util.ImageUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultPageScreenshotChecker}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class DefaultPageScreenshotCheckerTest {

    private PageScreenshotChecker.CheckContext context(String id) {
        AtomicReference<String> state = new AtomicReference<>("WAITING");
        AtomicReference<String> reason = new AtomicReference<>(null);
        return new PageScreenshotChecker.CheckContext() {
            @Override public String uniqueId() { return id; }
            @Override public String backgroundUrl() { return null; }
            @Override public double maxSimilarity() { return 0; }
            @Override public String getRenderState() { return state.get(); }
            @Override public void setRenderState(String s) { state.set(s); }
            @Override public String getRenderFailedReason() { return reason.get(); }
            @Override public void setRenderFailedReason(String r) { reason.set(r); }
        };
    }

    @Test
    @DisplayName("rejects null images")
    void rejectsNullImage() {
        DefaultPageScreenshotChecker checker = new DefaultPageScreenshotChecker();
        PageScreenshotChecker.CheckContext ctx = context("page-1");
        boolean ok = checker.beforePdfPageAdd(ctx, null, ImagePageSize.A4);
        assertFalse(ok);
        assertEquals(DefaultPageScreenshotChecker.REASON_NULL_IMAGE, ctx.getRenderFailedReason());
    }

    @Test
    @DisplayName("rejects all-white images")
    void rejectsWhiteImage() {
        DefaultPageScreenshotChecker checker = new DefaultPageScreenshotChecker();
        BufferedImage white = ImageUtil.getWhiteImage(20, 20);
        PageScreenshotChecker.CheckContext ctx = context("page-2");
        boolean ok = checker.beforePdfPageAdd(ctx, white, ImagePageSize.A4);
        assertFalse(ok);
        assertEquals(DefaultPageScreenshotChecker.REASON_WHITE_IMAGE, ctx.getRenderFailedReason());
    }

    @Test
    @DisplayName("accepts non-white, non-null images")
    void acceptsGoodImage() {
        DefaultPageScreenshotChecker checker = new DefaultPageScreenshotChecker();
        BufferedImage red = ImageUtil.getImageOnlyColor(20, 20, Color.RED);
        PageScreenshotChecker.CheckContext ctx = context("page-3");
        boolean ok = checker.beforePdfPageAdd(ctx, red, ImagePageSize.A4);
        assertTrue(ok);
    }

    @Test
    @DisplayName("order is -1 (runs before background-similarity checker)")
    void orderIsMinusOne() {
        DefaultPageScreenshotChecker checker = new DefaultPageScreenshotChecker();
        assertEquals(-1, checker.getOrder());
    }
}