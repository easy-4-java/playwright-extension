package com.microsoft.playwright.extension.options;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for option classes in the Playwright extension framework.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class OptionsTests {

    @Test
    void browserLaunchOptionsShouldCreateInstance() {
        BrowserLaunchOptions options = new BrowserLaunchOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void browserConnectOptionsShouldCreateInstance() {
        BrowserConnectOptions options = new BrowserConnectOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void browserNewContextOptionsShouldCreateInstance() {
        BrowserNewContextOptions options = new BrowserNewContextOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void pageNavigateOptionsShouldCreateInstance() {
        PageNavigateOptions options = new PageNavigateOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void pageScreenshotOptionsShouldCreateInstance() {
        PageScreenshotOptions options = new PageScreenshotOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void pagePdfOptionsShouldCreateInstance() {
        PagePdfOptions options = new PagePdfOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void elementScreenshotOptionsShouldCreateInstance() {
        ElementScreenshotOptions options = new ElementScreenshotOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void pageWaitForSelectorOptionsShouldCreateInstance() {
        PageWaitForSelectorOptions options = new PageWaitForSelectorOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void pageEmulateMediaOptionsShouldCreateInstance() {
        PageEmulateMediaOptions options = new PageEmulateMediaOptions();
        assertThat(options).isNotNull();
    }

    @Test
    void browserLaunchPersistentOptionsShouldCreateInstance() {
        BrowserLaunchPersistentOptions options = new BrowserLaunchPersistentOptions();
        assertThat(options).isNotNull();
    }
}
