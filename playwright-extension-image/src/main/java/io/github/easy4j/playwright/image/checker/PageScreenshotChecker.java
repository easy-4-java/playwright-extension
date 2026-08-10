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

import java.awt.image.BufferedImage;

/**
 * Page screenshot quality checker.
 *
 * <p>Extracted from ddd4j-cloud-cmpt-playwright. The original Spring
 * {@code Ordered} contract is preserved here as a plain Java method so that
 * the image module has zero Spring dependencies.</p>
 *
 * <p>Ordering between multiple checkers is decided externally (by the
 * caller / Spring auto-config) using {@link #getOrder()}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface PageScreenshotChecker extends Comparable<PageScreenshotChecker> {

    /**
     * Lower values run first.
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Post-screenshot check (file/buffer written, before compression/packing).
     *
     * @param context provides the uniqueId and lets checkers set state/reason
     * @return {@code true} if the screenshot passes this checker's criteria.
     */
    default boolean afterPageScreenShot(CheckContext context) {
        return true;
    }

    /**
     * Pre-PDF-page-add check (after image is read back, before drawing onto the PDF page).
     *
     * @param context  provides the uniqueId and lets checkers set state/reason
     * @param pdfImage the image that will be drawn onto the PDF page
     * @param pageSize target page size
     * @return {@code true} if the image is acceptable as a PDF page.
     */
    default boolean beforePdfPageAdd(CheckContext context, BufferedImage pdfImage, ImagePageSize pageSize) {
        return true;
    }

    @Override
    default int compareTo(PageScreenshotChecker other) {
        return Integer.compare(this.getOrder(), other.getOrder());
    }

    /**
     * Mutable handle for state and failure reason.
     * Implementations use {@link #setRenderState(String)} / {@link #setRenderFailedReason(String)}
     * to mark rejections; the caller reads these via getters afterwards.
     */
    interface CheckContext {

        String uniqueId();

        /** Optional URL of the background image to compare against. May be {@code null}. */
        String backgroundUrl();

        /**
         * Maximum allowed similarity percentage (0-100). Values ≤ 0 are
         * interpreted as "use the checker's default".
         */
        double maxSimilarity();

        String getRenderState();

        void setRenderState(String state);

        String getRenderFailedReason();

        void setRenderFailedReason(String reason);
    }
}