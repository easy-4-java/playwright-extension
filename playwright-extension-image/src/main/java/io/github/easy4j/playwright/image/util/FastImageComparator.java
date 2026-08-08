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

import java.util.stream.IntStream;

/**
 * Channel-wise image comparator that runs in parallel for large images.
 *
 * <p>Returns a similarity percentage in {@code [0, 100]}: 100 means identical,
 * 0 means maximal difference. Both inputs must be the same dimensions or
 * an {@link IllegalArgumentException} is thrown.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public final class FastImageComparator {

    /** Above this pixel count we switch to a parallel reduction. */
    private static final int PARALLEL_THRESHOLD = 10_000;

    private FastImageComparator() {
        // utility
    }

    public static double compare(ImagePixelCache cache1, ImagePixelCache cache2) {
        if (cache1.getWidth() != cache2.getWidth() || cache1.getHeight() != cache2.getHeight()) {
            throw new IllegalArgumentException("图片尺寸不一致");
        }
        int totalPixels = cache1.getWidth() * cache1.getHeight();
        if (totalPixels == 0) {
            return 100.0;
        }
        double rDiff = channelDiff(cache1.getRedChannel(), cache2.getRedChannel(), totalPixels);
        double gDiff = channelDiff(cache1.getGreenChannel(), cache2.getGreenChannel(), totalPixels);
        double bDiff = channelDiff(cache1.getBlueChannel(), cache2.getBlueChannel(), totalPixels);

        // Weighted RGB mean (0 = identical, 255 = maximal difference)
        double avgDiff = rDiff * 0.299 + gDiff * 0.587 + bDiff * 0.114;
        // Convert difference to similarity percentage.
        // 100 = identical, 0 = maximal difference.
        double similarity = 100.0 - (avgDiff / 255.0) * 100.0;
        return Math.max(0.0, Math.min(100.0, similarity));
    }

    private static double channelDiff(byte[] channel1, byte[] channel2, int length) {
        if (length > PARALLEL_THRESHOLD) {
            return IntStream.range(0, length).parallel()
                    .mapToDouble(i -> Math.abs((channel1[i] & 0xFF) - (channel2[i] & 0xFF)))
                    .average().orElse(0);
        } else {
            double sum = 0;
            for (int i = 0; i < length; i++) {
                sum += Math.abs((channel1[i] & 0xFF) - (channel2[i] & 0xFF));
            }
            return sum / length;
        }
    }
}