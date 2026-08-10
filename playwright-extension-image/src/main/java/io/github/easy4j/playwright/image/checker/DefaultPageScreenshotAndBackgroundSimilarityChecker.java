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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.easy4j.playwright.image.enums.ImagePageSize;
import io.github.easy4j.playwright.image.util.FastImageComparator;
import io.github.easy4j.playwright.image.util.ImagePixelCache;
import io.github.easy4j.playwright.image.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Background-similarity checker that uses Caffeine to cache background images
 * across multiple renderings.
 *
 * <p>For each page, if {@code context.backgroundUrl} is set, the background
 * image is fetched (cached on first miss) and compared against the rendered
 * page. If similarity exceeds {@code context.maxSimilarity} (a 0-100 ratio),
 * the page is rejected.</p>
 *
 * <p>This checker requires the optional Caffeine dependency to be on the
 * classpath at runtime. The image module declares Caffeine as an optional
 * dependency so users without Caffeine can still build the module.</p>
 *
 * <p>Order is {@code 0} so it runs after {@link DefaultPageScreenshotChecker}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public class DefaultPageScreenshotAndBackgroundSimilarityChecker implements PageScreenshotChecker {

    /** Threshold above which a page is considered "too similar to the background". */
    public static final double DEFAULT_MAX_SIMILARITY = 75.0;

    public static final String REASON_BACKGROUND_TOO_SIMILAR =
            "[{\"initiatorType\":\"resource\",\"name\":\"截图图片检查未通过，背景图片相似度过高！\",\"responseStatus\":500}]";

    private final LoadingCache<String, Optional<BufferedImage>> imageCaches;

    public DefaultPageScreenshotAndBackgroundSimilarityChecker(Duration expireAfterWrite,
                                                              int initialCapacity,
                                                              int maximumSize) {
        this.imageCaches = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite)
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .recordStats()
                .removalListener(new RemovalListener<String, Optional<BufferedImage>>() {
                    @Override
                    public void onRemoval(String imageUrl,
                                            Optional<BufferedImage> value,
                                            @NonNull RemovalCause cause) {
                        log.debug("The BufferedImage cache of {} was removed, cause is {}",
                                imageUrl, cause);
                    }
                })
                .build(new CacheLoader<String, Optional<BufferedImage>>() {
                    @Override
                    public Optional<BufferedImage> load(String imageUrl) throws Exception {
                        if (imageUrl == null || imageUrl.isBlank()) {
                            return Optional.empty();
                        }
                        try {
                            URL url = new URL(imageUrl);
                            try (InputStream in = url.openStream()) {
                                BufferedImage background = ImageIO.read(in);
                                return Optional.ofNullable(background);
                            }
                        } catch (Exception e) {
                            log.error("Image URL {} load error: {}", imageUrl, e.getMessage());
                            return Optional.empty();
                        }
                    }
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean beforePdfPageAdd(CheckContext context, BufferedImage pdfImage, ImagePageSize pageSize) {
        String backgroundUrl = context.backgroundUrl();
        if (backgroundUrl == null || backgroundUrl.isBlank()) {
            return Boolean.TRUE;
        }
        if (Objects.isNull(pdfImage)) {
            return Boolean.FALSE;
        }
        Optional<BufferedImage> optional = imageCaches.get(backgroundUrl);
        if (!optional.isPresent()) {
            log.warn("Background Image {} not found", backgroundUrl);
            return Boolean.TRUE;
        }
        try {
            BufferedImage background = ImageUtil.scaleTo(optional.get(), pageSize, 100);
            BufferedImage pdfScaled = ImageUtil.scaleTo(pdfImage, pageSize, 100);
            double similarity = FastImageComparator.compare(
                    new ImagePixelCache(background),
                    new ImagePixelCache(pdfScaled));
            double maxSimilarity = context.maxSimilarity();
            if (maxSimilarity <= 0) {
                maxSimilarity = DEFAULT_MAX_SIMILARITY;
            }
            if (similarity > maxSimilarity) {
                context.setRenderFailedReason(REASON_BACKGROUND_TOO_SIMILAR);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("Background Image {} load error: {}", backgroundUrl, e.getMessage());
            context.setRenderFailedReason(DefaultPageScreenshotChecker.REASON_NULL_IMAGE);
            return Boolean.FALSE;
        }
    }
}