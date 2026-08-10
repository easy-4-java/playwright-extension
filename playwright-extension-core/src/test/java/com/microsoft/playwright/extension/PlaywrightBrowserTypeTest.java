package com.microsoft.playwright.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PlaywrightBrowserType}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class PlaywrightBrowserTypeTest {

    @Test
    void chromiumShouldExist() {
        assertThat(PlaywrightBrowserType.chromium).isNotNull();
    }

    @Test
    void firefoxShouldExist() {
        assertThat(PlaywrightBrowserType.firefox).isNotNull();
    }

    @Test
    void webkitShouldExist() {
        assertThat(PlaywrightBrowserType.webkit).isNotNull();
    }

    @Test
    void valuesShouldContainAllTypes() {
        assertThat(PlaywrightBrowserType.values()).hasSize(3);
    }

    @Test
    void valueOfShouldReturnCorrectEnum() {
        assertThat(PlaywrightBrowserType.valueOf("chromium")).isEqualTo(PlaywrightBrowserType.chromium);
        assertThat(PlaywrightBrowserType.valueOf("firefox")).isEqualTo(PlaywrightBrowserType.firefox);
        assertThat(PlaywrightBrowserType.valueOf("webkit")).isEqualTo(PlaywrightBrowserType.webkit);
    }
}
