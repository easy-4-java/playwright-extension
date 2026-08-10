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

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;

/**
 * Sequential pixel-by-pixel comparator. Slower than {@link FastImageComparator}
 * but works directly on {@link BufferedImage} without the cache step.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public final class PixelComparator {

    private PixelComparator() {
        // utility
    }

    /**
     * Compare two images and return a similarity percentage in {@code [0, 100]}.
     * Throws {@link IllegalArgumentException} when sizes differ.
     */
    public static double compareImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("图片尺寸必须相同");
        }
        int width = img1.getWidth();
        int height = img1.getHeight();
        long diff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
            }
        }
        double totalPixels = width * height * 3.0;
        double avgDiff = diff / totalPixels;
        return 100 - (avgDiff / 255.0) * 100;
    }
}