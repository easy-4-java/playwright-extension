package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ElementScreenshotOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions();
        assertNull(opts.getAnimations());
        assertNull(opts.getCaret());
        assertNull(opts.getMask());
        assertNull(opts.getMaskColor());
        assertTrue(opts.getOmitBackground());
        assertNull(opts.getPath());
        assertEquals(100, opts.getQuality());
        assertEquals(ScreenshotScale.DEVICE, opts.getScale());
        assertEquals(30000.0, opts.getTimeout());
        assertEquals(ScreenshotType.PNG, opts.getType());
    }

    @Test
    void shouldSupportChainedSetters() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setAnimations(ScreenshotAnimations.DISABLED)
                .setCaret(ScreenshotCaret.HIDE)
                .setOmitBackground(false)
                .setQuality(80)
                .setScale(ScreenshotScale.CSS)
                .setTimeout(10000.0)
                .setType(ScreenshotType.JPEG);
        assertEquals(ScreenshotAnimations.DISABLED, opts.getAnimations());
        assertEquals(ScreenshotCaret.HIDE, opts.getCaret());
        assertFalse(opts.getOmitBackground());
        assertEquals(80, opts.getQuality());
        assertEquals(ScreenshotScale.CSS, opts.getScale());
        assertEquals(10000.0, opts.getTimeout());
        assertEquals(ScreenshotType.JPEG, opts.getType());
    }

    @Test
    void shouldMapAllFieldsToScreenshotOptions() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setAnimations(ScreenshotAnimations.DISABLED)
                .setCaret(ScreenshotCaret.HIDE)
                .setMaskColor("#FF0000")
                .setOmitBackground(true)
                .setPath(java.nio.file.Paths.get("/tmp/screenshot.png"))
                .setScale(ScreenshotScale.CSS)
                .setTimeout(15000.0)
                .setType(ScreenshotType.PNG);
        ElementHandle.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithNullFields() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setAnimations(null)
                .setCaret(null)
                .setMask(null)
                .setMaskColor(null)
                .setPath(null);
        ElementHandle.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldIncludeQualityForJpegType() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setType(ScreenshotType.JPEG)
                .setQuality(85);
        ElementHandle.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldNotIncludeQualityForPngType() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setType(ScreenshotType.PNG)
                .setQuality(85);
        ElementHandle.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldHandleNullTypeWithQuality() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setType(null)
                .setQuality(85);
        ElementHandle.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyMaskColor() {
        ElementScreenshotOptions opts = new ElementScreenshotOptions()
                .setMaskColor("")
                .setType(ScreenshotType.PNG);
        ElementHandle.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
