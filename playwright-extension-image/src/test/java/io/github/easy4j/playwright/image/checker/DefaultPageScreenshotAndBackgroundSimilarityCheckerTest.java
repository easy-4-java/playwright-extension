package io.github.easy4j.playwright.image.checker;

import io.github.easy4j.playwright.image.enums.ImagePageSize;
import io.github.easy4j.playwright.image.util.ImageUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPageScreenshotAndBackgroundSimilarityCheckerTest {

    @TempDir Path temporaryDirectory;

    private DefaultPageScreenshotAndBackgroundSimilarityChecker checker() {
        return new DefaultPageScreenshotAndBackgroundSimilarityChecker(Duration.ofMinutes(1), 1, 4);
    }

    @Test
    void missingBackgroundSkipsComparisonButNullPageIsRejectedWhenConfigured() {
        DefaultPageScreenshotAndBackgroundSimilarityChecker checker = checker();
        Context noBackground = new Context(null, 75);
        assertTrue(checker.beforePdfPageAdd(noBackground, null, ImagePageSize.A4));

        Context configured = new Context(temporaryDirectory.resolve("missing.png").toUri().toString(), 75);
        assertFalse(checker.beforePdfPageAdd(configured, null, ImagePageSize.A4));
        assertTrue(checker.beforePdfPageAdd(configured,
                ImageUtil.getImageOnlyColor(2, 2, Color.RED), ImagePageSize.A4));
    }

    @Test
    void matchingBackgroundRejectsPageAndDifferentBackgroundAcceptsIt() throws Exception {
        Path file = temporaryDirectory.resolve("background.png");
        assertTrue(ImageIO.write(ImageUtil.getImageOnlyColor(2, 2, Color.RED), "png", file.toFile()));
        DefaultPageScreenshotAndBackgroundSimilarityChecker checker = checker();
        String backgroundUrl = file.toUri().toString();
        BufferedImage red = ImageUtil.getImageOnlyColor(2, 2, Color.RED);
        BufferedImage blue = ImageUtil.getImageOnlyColor(2, 2, Color.BLUE);

        Context same = new Context(backgroundUrl, 0);
        assertFalse(checker.beforePdfPageAdd(same, red, ImagePageSize.A4));
        assertEquals(DefaultPageScreenshotAndBackgroundSimilarityChecker.REASON_BACKGROUND_TOO_SIMILAR,
                same.getRenderFailedReason());

        Context different = new Context(backgroundUrl, 75);
        assertTrue(checker.beforePdfPageAdd(different, blue, ImagePageSize.A4));
        assertNull(different.getRenderFailedReason());
    }

    private static final class Context implements PageScreenshotChecker.CheckContext {
        private final String backgroundUrl;
        private final double maximumSimilarity;
        private String renderState;
        private String reason;

        Context(String backgroundUrl, double maximumSimilarity) {
            this.backgroundUrl = backgroundUrl;
            this.maximumSimilarity = maximumSimilarity;
        }

        @Override public String uniqueId() { return "page"; }
        @Override public String backgroundUrl() { return backgroundUrl; }
        @Override public double maxSimilarity() { return maximumSimilarity; }
        @Override public String getRenderState() { return renderState; }
        @Override public void setRenderState(String state) { renderState = state; }
        @Override public String getRenderFailedReason() { return reason; }
        @Override public void setRenderFailedReason(String value) { reason = value; }
    }
}
