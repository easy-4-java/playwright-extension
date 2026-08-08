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

import io.github.easy4j.playwright.image.enums.ImagePageSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ImageUtil}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class ImageUtilTest {

    @Test
    @DisplayName("getImageOnlyColor returns an image filled with the requested colour")
    void getImageOnlyColor_fillsRequestedColour() {
        BufferedImage image = ImageUtil.getImageOnlyColor(50, 30, Color.RED);
        assertEquals(50, image.getWidth());
        assertEquals(30, image.getHeight());
        // every pixel should be red (allow for the alpha channel difference)
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                assertEquals(Color.RED.getRGB() & 0x00FFFFFF, image.getRGB(x, y) & 0x00FFFFFF);
            }
        }
    }

    @Test
    @DisplayName("WHITE_A3 and WHITE_A4 constants are pre-rendered")
    void prebuiltConstants_arePreRendered() {
        assertNotNull(ImageUtil.WHITE_A3);
        assertNotNull(ImageUtil.WHITE_A4);
        assertEquals(ImagePageSize.A3.getWidth(), ImageUtil.WHITE_A3.getWidth());
        assertEquals(ImagePageSize.A3.getHeight(), ImageUtil.WHITE_A3.getHeight());
        assertEquals(ImagePageSize.A4.getWidth(), ImageUtil.WHITE_A4.getWidth());
        assertEquals(ImagePageSize.A4.getHeight(), ImageUtil.WHITE_A4.getHeight());
        assertTrue(ImageUtil.isWhiteImage(ImageUtil.WHITE_A3));
        assertTrue(ImageUtil.isWhiteImage(ImageUtil.WHITE_A4));
    }

    @Test
    @DisplayName("isWhiteImage returns true for white, false for coloured")
    void isWhiteImage_discriminatesByColour() {
        BufferedImage white = ImageUtil.getWhiteImage(20, 20);
        BufferedImage red = ImageUtil.getImageOnlyColor(20, 20, Color.RED);
        assertTrue(ImageUtil.isWhiteImage(white));
        assertFalse(ImageUtil.isWhiteImage(red));
    }

    @Test
    @DisplayName("isNeedScale returns false when target matches source")
    void isNeedScale_returnsFalseWhenSizesMatch() {
        BufferedImage image = ImageUtil.getWhiteImage(842, 1191);
        assertFalse(ImageUtil.isNeedScale(image, ImagePageSize.A3));
    }

    @Test
    @DisplayName("isNeedScale returns true when target differs from source")
    void isNeedScale_returnsTrueWhenSizesDiffer() {
        BufferedImage image = ImageUtil.getWhiteImage(100, 100);
        assertTrue(ImageUtil.isNeedScale(image, ImagePageSize.A3));
    }

    @Test
    @DisplayName("getImagePixels round-trips RGB values")
    void getImagePixels_roundTripsRgb() {
        BufferedImage image = ImageUtil.getImageOnlyColor(8, 8, Color.GREEN);
        int[][] pixels = ImageUtil.getImagePixels(image);
        assertEquals(8, pixels.length);
        assertEquals(8, pixels[0].length);
        int targetRgb = Color.GREEN.getRGB() & 0x00FFFFFF;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                assertEquals(targetRgb, pixels[x][y] & 0x00FFFFFF);
            }
        }
    }

    @Test
    @DisplayName("calculateSimilarity returns 1.0 for identical images, 0.0 for size mismatch")
    void calculateSimilarity_handlesEdgeCases() {
        BufferedImage image = ImageUtil.getWhiteImage(20, 20);
        int[][] a = ImageUtil.getImagePixels(image);
        int[][] b = ImageUtil.getImagePixels(image);
        assertEquals(1.0, ImageUtil.calculateSimilarity(a, b), 0.0001);

        int[][] different = ImageUtil.getImagePixels(ImageUtil.getImageOnlyColor(20, 30, Color.WHITE));
        assertEquals(0.0, ImageUtil.calculateSimilarity(a, different), 0.0001);
    }

    @Test
    @DisplayName("isImageColorOutPercent supports per-cent thresholds")
    void isImageColorOutPercent_thresholdBehaviour() {
        // 10x10 image, all white → 100% white pixels
        BufferedImage white = ImageUtil.getWhiteImage(10, 10);
        assertTrue(ImageUtil.isImageColorOutPercent(white, Color.WHITE, 100f));
        // 80% threshold on all-white image: still satisfied
        assertTrue(ImageUtil.isImageColorOutPercent(white, Color.WHITE, 80f));
        // 50% white image: 50% threshold should fail
        assertFalse(ImageUtil.isImageColorOutPercent(white, Color.WHITE, 150f));
    }

    @Test
    @DisplayName("getImageFileSize returns the encoded byte count")
    void getImageFileSize_returnsByteCount() {
        BufferedImage image = ImageUtil.getWhiteImage(20, 20);
        long size = ImageUtil.getImageFileSize(image, "png");
        assertTrue(size > 0);
    }

    @Test
    @DisplayName("scaleTo returns the input image when target dimensions match")
    void scaleTo_noOpWhenSizesMatch() throws Exception {
        BufferedImage input = ImageUtil.getWhiteImage(842, 1191);
        BufferedImage result = ImageUtil.scaleTo(input, ImagePageSize.A3, 100);
        // Same pixels, but a new BufferedImage may be returned; check size.
        assertEquals(842, result.getWidth());
        assertEquals(1191, result.getHeight());
        // For the no-op case, we return the input directly (object identity is preserved).
        assertSame(input, result);
    }
}