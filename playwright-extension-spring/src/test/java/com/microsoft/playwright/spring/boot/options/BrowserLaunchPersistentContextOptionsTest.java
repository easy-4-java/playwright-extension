package com.microsoft.playwright.spring.boot.options;

import com.microsoft.playwright.BrowserType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class BrowserLaunchPersistentContextOptionsTest {

    @Test
    void defaultsDoNotSetNullablePlaywrightOptions() {
        BrowserType.LaunchPersistentContextOptions options = new BrowserLaunchPersistentContextOptions().toOptions();

        assertNull(options.acceptDownloads);
        assertNull(options.headless);
        assertEquals(30_000.0, options.timeout);
    }

    @Test
    void explicitFalseAndZeroValuesAreMapped() {
        BrowserLaunchPersistentContextOptions settings = new BrowserLaunchPersistentContextOptions();
        settings.setHeadless(false);
        settings.setAcceptDownloads(false);
        settings.setTimeout(0.0);
        settings.setSlowMo(0.0);

        BrowserType.LaunchPersistentContextOptions options = settings.toOptions();

        assertFalse(options.headless);
        assertFalse(options.acceptDownloads);
        assertEquals(0.0, options.timeout);
        assertEquals(0.0, options.slowMo);
    }
}
