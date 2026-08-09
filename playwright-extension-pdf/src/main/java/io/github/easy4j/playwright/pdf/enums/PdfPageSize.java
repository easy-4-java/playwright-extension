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
package io.github.easy4j.playwright.pdf.enums;

import lombok.Getter;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Standard PDF page sizes, wrapping {@link PDRectangle} constants.
 *
 * <p>This enum bridges the PDFBox-specific {@link PDRectangle} with the
 * PDFBox-agnostic {@code io.github.easy4j.playwright.image.enums.ImagePageSize}
 * used by the image module: image module scales images to abstract point
 * dimensions, this enum converts those dimensions into PDFBox page rectangles.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Getter
public enum PdfPageSize {
    A0(PDRectangle.A0),
    A1(PDRectangle.A1),
    A2(PDRectangle.A2),
    A3(PDRectangle.A3),
    A4(PDRectangle.A4),
    A5(PDRectangle.A5),
    A6(PDRectangle.A6),
    LETTER(PDRectangle.LETTER),
    LEGAL(PDRectangle.LEGAL);

    private final PDRectangle rectangle;

    PdfPageSize(PDRectangle rectangle) {
        this.rectangle = rectangle;
    }

    /**
     * Look up by case-insensitive name; falls back to {@link #A4}.
     */
    public static PdfPageSize getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return A4;
        }
        try {
            return PdfPageSize.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return A4;
        }
    }
}