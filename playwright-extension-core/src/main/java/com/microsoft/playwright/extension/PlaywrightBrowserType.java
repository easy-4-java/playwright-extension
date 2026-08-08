package com.microsoft.playwright.extension;

/**
 * Enumeration of browser types supported by the Playwright automation framework.
 * Each value corresponds to a specific browser engine that Playwright can control.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.microsoft.playwright.BrowserType
 */
public enum PlaywrightBrowserType {

    /** Chromium-based browser engine (Google Chrome, Microsoft Edge). */
    chromium,

    /** Mozilla Firefox browser engine. */
    firefox,

    /** WebKit browser engine (Safari). */
    webkit

}
