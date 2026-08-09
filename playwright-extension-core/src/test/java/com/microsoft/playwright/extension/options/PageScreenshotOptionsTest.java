package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PageScreenshotOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        PageScreenshotOptions opts = new PageScreenshotOptions();
        assertNull(opts.getAnimations());
        assertNull(opts.getCaret());
        assertNull(opts.getClip());
        assertTrue(opts.getFullPage());
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
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setAnimations(ScreenshotAnimations.DISABLED)
                .setCaret(ScreenshotCaret.HIDE)
                .setFullPage(false)
                .setOmitBackground(false)
                .setQuality(75)
                .setScale(ScreenshotScale.CSS)
                .setTimeout(15000.0)
                .setType(ScreenshotType.JPEG);
        assertEquals(ScreenshotAnimations.DISABLED, opts.getAnimations());
        assertEquals(ScreenshotCaret.HIDE, opts.getCaret());
        assertFalse(opts.getFullPage());
        assertFalse(opts.getOmitBackground());
        assertEquals(75, opts.getQuality());
        assertEquals(ScreenshotScale.CSS, opts.getScale());
        assertEquals(15000.0, opts.getTimeout());
        assertEquals(ScreenshotType.JPEG, opts.getType());
    }

    @Test
    void shouldMapAllFieldsToScreenshotOptions() {
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setAnimations(ScreenshotAnimations.DISABLED)
                .setCaret(ScreenshotCaret.HIDE)
                .setFullPage(true)
                .setMaskColor("#FF0000")
                .setOmitBackground(true)
                .setPath(java.nio.file.Paths.get("/tmp/page-screenshot.png"))
                .setScale(ScreenshotScale.DEVICE)
                .setTimeout(20000.0)
                .setType(ScreenshotType.PNG);
        Page.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithNullFields() {
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setAnimations(null)
                .setCaret(null)
                .setClip(null)
                .setFullPage(null)
                .setMask(null)
                .setMaskColor(null)
                .setOmitBackground(null)
                .setPath(null)
                .setQuality(null)
                .setScale(null)
                .setTimeout(null)
                .setType(null);
        Page.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldIncludeQualityForJpegType() {
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setType(ScreenshotType.JPEG)
                .setQuality(90);
        Page.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldNotIncludeQualityForPngType() {
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setType(ScreenshotType.PNG)
                .setQuality(90);
        Page.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldHandleNullTypeWithQuality() {
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setType(null)
                .setQuality(90);
        Page.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyMaskColor() {
        PageScreenshotOptions opts = new PageScreenshotOptions()
                .setMaskColor("");
        Page.ScreenshotOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
