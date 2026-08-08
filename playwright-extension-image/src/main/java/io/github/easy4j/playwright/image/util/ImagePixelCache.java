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

import lombok.Data;

import java.awt.image.BufferedImage;

/**
 * Pre-computes the per-channel (R/G/B) byte arrays of a {@link BufferedImage},
 * so that repeated comparisons don't have to re-decode the image each time.
 *
 * <p>The cache is immutable after construction: every field is {@code final}
 * via Lombok {@code @Data}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Data
public class ImagePixelCache {

    private final int width;
    private final int height;
    private final byte[] redChannel;
    private final byte[] greenChannel;
    private final byte[] blueChannel;

    public ImagePixelCache(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        int size = width * height;
        this.redChannel = new byte[size];
        this.greenChannel = new byte[size];
        this.blueChannel = new byte[size];
        cacheImageData(image);
    }

    private void cacheImageData(BufferedImage image) {
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                redChannel[index] = (byte) ((rgb >> 16) & 0xFF);
                greenChannel[index] = (byte) ((rgb >> 8) & 0xFF);
                blueChannel[index] = (byte) (rgb & 0xFF);
                index++;
            }
        }
    }
}