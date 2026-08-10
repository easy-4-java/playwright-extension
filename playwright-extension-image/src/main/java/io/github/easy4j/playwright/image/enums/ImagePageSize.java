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
package io.github.easy4j.playwright.image.enums;

/**
 * Standard page sizes for image scaling (in points; 1 point = 1/72 inch).
 *
 * <p>Mirrors a subset of {@code org.apache.pdfbox.pdmodel.common.PDRectangle}
 * page sizes. We deliberately do not depend on PDFBox from the image module
 * (which must stay zero-dep on third-party PDF libs); the PDF module maps
 * its own page sizes onto this enum via the {@link #fromPoints} adapter.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public enum ImagePageSize {
    /** ISO A3: 842 × 1191 points */
    A3(842.0f, 1191.0f),
    /** ISO A4: 595 × 842 points */
    A4(595.0f, 842.0f),
    /** US Letter: 612 × 792 points */
    LETTER(612.0f, 792.0f),
    /** US Legal: 612 × 1008 points */
    LEGAL(612.0f, 1008.0f);

    private final float width;
    private final float height;

    ImagePageSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    /**
     * Adapter: accept an arbitrary (width, height) pair as a "custom" page size.
     * Used by callers that need a non-standard PDF page size — see the PDF module.
     */
    public static ImagePageSize fromPoints(float width, float height) {
        for (ImagePageSize ps : values()) {
            if (ps.width == width && ps.height == height) {
                return ps;
            }
        }
        // Fall back: A4 is the closest stock placeholder, but the values are still
        // taken from the caller's (width, height) pair in ImageUtil.scaleTo.
        return A4;
    }
}