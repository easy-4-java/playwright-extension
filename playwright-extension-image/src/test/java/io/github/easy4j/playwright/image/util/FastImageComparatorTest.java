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
package io.github.easy4j.playwright.image.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FastImageComparator}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class FastImageComparatorTest {

    @Test
    @DisplayName("identical white images yield 100% similarity")
    void identicalImages_return100() {
        BufferedImage white = ImageUtil.getWhiteImage(20, 20);
        double similarity = FastImageComparator.compare(
                new ImagePixelCache(white),
                new ImagePixelCache(white));
        assertEquals(100.0, similarity, 0.01);
    }

    @Test
    @DisplayName("black-and-white images yield low similarity")
    void contrastingImages_returnLowSimilarity() {
        BufferedImage white = ImageUtil.getWhiteImage(20, 20);
        BufferedImage black = ImageUtil.getImageOnlyColor(20, 20, Color.BLACK);
        double similarity = FastImageComparator.compare(
                new ImagePixelCache(white),
                new ImagePixelCache(black));
        assertTrue(similarity < 5.0,
                "black vs white should be very dissimilar but was " + similarity);
    }

    @Test
    @DisplayName("size mismatch throws IllegalArgumentException")
    void mismatchedSizes_throw() {
        BufferedImage a = ImageUtil.getWhiteImage(20, 20);
        BufferedImage b = ImageUtil.getWhiteImage(30, 30);
        assertThrows(IllegalArgumentException.class,
                () -> FastImageComparator.compare(new ImagePixelCache(a), new ImagePixelCache(b)));
    }

    @Test
    @DisplayName("empty images (0×0) yield 100% similarity")
    void emptyImages_return100() {
        // BufferedImage requires positive dimensions, so use 1×1 instead.
        BufferedImage tiny = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        double similarity = FastImageComparator.compare(
                new ImagePixelCache(tiny),
                new ImagePixelCache(tiny));
        assertEquals(100.0, similarity, 0.01);
    }
}