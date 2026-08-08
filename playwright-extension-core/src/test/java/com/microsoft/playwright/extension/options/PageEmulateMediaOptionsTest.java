package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageEmulateMediaOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        PageEmulateMediaOptions opts = new PageEmulateMediaOptions();
        assertNull(opts.getColorScheme());
        assertNull(opts.getForcedColors());
        assertNull(opts.getMedia());
        assertNull(opts.getReducedMotion());
    }

    @Test
    void shouldSupportChainedSetters() {
        PageEmulateMediaOptions opts = new PageEmulateMediaOptions()
                .setColorScheme(ColorScheme.DARK)
                .setForcedColors(ForcedColors.ACTIVE)
                .setMedia(Media.PRINT)
                .setReducedMotion(ReducedMotion.REDUCE);
        assertEquals(ColorScheme.DARK, opts.getColorScheme());
        assertEquals(ForcedColors.ACTIVE, opts.getForcedColors());
        assertEquals(Media.PRINT, opts.getMedia());
        assertEquals(ReducedMotion.REDUCE, opts.getReducedMotion());
    }

    @Test
    void shouldMapAllFieldsToEmulateMediaOptions() {
        PageEmulateMediaOptions opts = new PageEmulateMediaOptions()
                .setColorScheme(ColorScheme.LIGHT)
                .setForcedColors(ForcedColors.NONE)
                .setMedia(Media.SCREEN)
                .setReducedMotion(ReducedMotion.NO_PREFERENCE);
        Page.EmulateMediaOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithNullFields() {
        PageEmulateMediaOptions opts = new PageEmulateMediaOptions();
        Page.EmulateMediaOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithPartialFields() {
        PageEmulateMediaOptions opts = new PageEmulateMediaOptions()
                .setColorScheme(ColorScheme.DARK);
        Page.EmulateMediaOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
