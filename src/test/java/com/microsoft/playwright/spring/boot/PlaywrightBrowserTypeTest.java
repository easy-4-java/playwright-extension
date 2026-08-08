package com.microsoft.playwright.spring.boot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaywrightBrowserTypeTest {

    @Test
    void shouldContainChromium() {
        assertNotNull(PlaywrightBrowserType.chromium);
        assertEquals("chromium", PlaywrightBrowserType.chromium.name());
    }

    @Test
    void shouldContainFirefox() {
        assertNotNull(PlaywrightBrowserType.firefox);
        assertEquals("firefox", PlaywrightBrowserType.firefox.name());
    }

    @Test
    void shouldContainWebkit() {
        assertNotNull(PlaywrightBrowserType.webkit);
        assertEquals("webkit", PlaywrightBrowserType.webkit.name());
    }

    @Test
    void shouldHaveExactlyThreeValues() {
        PlaywrightBrowserType[] values = PlaywrightBrowserType.values();
        assertEquals(3, values.length);
    }

    @Test
    void shouldResolveValueFromString() {
        assertEquals(PlaywrightBrowserType.chromium, PlaywrightBrowserType.valueOf("chromium"));
        assertEquals(PlaywrightBrowserType.firefox, PlaywrightBrowserType.valueOf("firefox"));
        assertEquals(PlaywrightBrowserType.webkit, PlaywrightBrowserType.valueOf("webkit"));
    }

    @Test
    void shouldThrowOnInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> PlaywrightBrowserType.valueOf("invalid"));
    }
}
