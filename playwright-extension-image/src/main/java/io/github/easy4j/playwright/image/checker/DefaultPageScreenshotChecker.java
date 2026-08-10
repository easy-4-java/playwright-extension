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
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Default {@link PageScreenshotChecker}: rejects null images and all-white pages.
 *
 * <p>Order is {@code -1} so that this checker runs before the (more expensive)
 * background-similarity checker.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public class DefaultPageScreenshotChecker implements PageScreenshotChecker {

    /** Error payload used when the image is null. */
    public static final String REASON_NULL_IMAGE =
            "[{\"initiatorType\":\"resource\",\"name\":\"页面截图失败，访问资源不存或接口报错！\",\"responseStatus\":500}]";
    /** Error payload used when the image is all-white. */
    public static final String REASON_WHITE_IMAGE =
            "[{\"initiatorType\":\"resource\",\"name\":\"截图图片检查未通过，截图为白色图片！\",\"responseStatus\":500}]";

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public boolean beforePdfPageAdd(CheckContext context, BufferedImage pdfImage, ImagePageSize pageSize) {
        if (Objects.isNull(pdfImage)) {
            log.warn("PDF Image is null, uniqueId={}", context.uniqueId());
            context.setRenderFailedReason(REASON_NULL_IMAGE);
            return Boolean.FALSE;
        }
        if (ImageUtil.isWhiteImage(pdfImage)) {
            context.setRenderFailedReason(REASON_WHITE_IMAGE);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}