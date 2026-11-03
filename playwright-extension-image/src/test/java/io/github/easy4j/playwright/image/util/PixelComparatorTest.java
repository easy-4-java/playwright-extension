package io.github.easy4j.playwright.image.util;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PixelComparatorTest {

    @Test
    void identicalAndOppositeImagesHaveExpectedSimilarity() {
        BufferedImage black = ImageUtil.getImageOnlyColor(2, 2, Color.BLACK);
        BufferedImage white = ImageUtil.getImageOnlyColor(2, 2, Color.WHITE);

        assertEquals(100.0, PixelComparator.compareImages(black, black), 0.0001);
        assertEquals(0.0, PixelComparator.compareImages(black, white), 0.0001);
    }

    @Test
    void differentImageDimensionsAreRejected() {
        BufferedImage square = ImageUtil.getWhiteImage(2, 2);
        BufferedImage rectangle = ImageUtil.getWhiteImage(2, 3);

        assertThrows(IllegalArgumentException.class, () -> PixelComparator.compareImages(square, rectangle));
    }
}
