package com.microsoft.playwright.spring.boot.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.spring.boot.PlaywrightBrowserType;
import lombok.extern.slf4j.Slf4j;
import com.microsoft.playwright.spring.boot.options.OptionMapper;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Utility class providing common Playwright operations such as cookie management,
 * localStorage clearing, slider interactions, browser type resolution, and page lifecycle.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see PlaywrightBrowserType
 */
@Slf4j
public class PlaywrightUtil {

    /**
     * Shared {@link OptionMapper} instance for conditional value mapping.
     */
    protected static final OptionMapper map = OptionMapper.get().alwaysApplyingWhenNonNull();

    /**
     * Thread-local storage for user data directory paths, using Alibaba TTL for
     * transmittable thread-local support.
     */
    private static ThreadLocal<String> ttl = new TransmittableThreadLocal<String>();

    /**
     * Sets the user data directory path for the current thread.
     *
     * @param userDataDir the user data directory path to store
     */
    public static void setUerDataDir(String userDataDir) {
        ttl.set(userDataDir);
    }

    /**
     * Gets the user data directory path for the current thread.
     *
     * @return the user data directory path, or {@code null} if not set
     */
    public static String getUerDataDir() {
        return ttl.get();
    }

    private static final String TOKEN_SPLITTER = ";";

    /**
     * Extracts all cookies from the given page's browser context and returns them
     * as a semicolon-delimited string in {@code "name=value"} format.
     *
     * @param page the Playwright page to extract cookies from
     * @return a semicolon-delimited cookie string
     */
    public static String getCookies(Page page) {
        return cookieToString(page.context().cookies());
    }

    /**
     * Converts a list of {@link Cookie} objects into a semicolon-delimited string
     * in {@code "name=value"} format.
     *
     * @param cookies the list of cookies to convert
     * @return a semicolon-delimited cookie string, or an empty string if the list is empty
     */
    public static String cookieToString(List<Cookie> cookies) {
        return cookies.stream().map(cookie -> cookie.name + "=" + cookie.value).collect(Collectors.joining(TOKEN_SPLITTER));
    }

    /**
     * Clears all items from the browser's {@code localStorage} for the given page.
     *
     * @param page the Playwright page whose localStorage should be cleared
     */
    public static void clearLocalStorage(Page page) {
        page.evaluate("window.localStorage.clear();");
    }

    /**
     * Performs a slider drag operation by locating the element via a CSS selector
     * and dragging it horizontally.
     *
     * @param page             the Playwright page
     * @param slideElementPath the CSS selector for the slider element
     * @param slideLength      the horizontal distance to drag in pixels
     * @param steps            the number of intermediate steps for the drag motion
     */
    public static void slide(Page page, String slideElementPath, int slideLength, int steps) {
        slide(page, page.waitForSelector(slideElementPath, new Page.WaitForSelectorOptions().setTimeout(TimeUnit.SECONDS.toMillis(5))), slideLength, steps);
    }

    /**
     * Performs a slider drag operation on the given element handle.
     *
     * @param page          the Playwright page
     * @param elementHandle the slider element to drag
     * @param slideLength   the horizontal distance to drag in pixels
     * @param steps         the number of intermediate steps for the drag motion
     */
    public static void slide(Page page, ElementHandle elementHandle, int slideLength, int steps) {
        Mouse mouse = page.mouse();
        mouse.move(elementHandle.boundingBox().x, elementHandle.boundingBox().y);
        mouse.down(new Mouse.DownOptions().setButton(MouseButton.LEFT));
        mouse.move(elementHandle.boundingBox().x + slideLength, elementHandle.boundingBox().y, new Mouse.MoveOptions().setSteps(steps));
        mouse.up();
    }

    /**
     * Resolves the appropriate {@link BrowserType} from a {@link Playwright} instance
     * based on the given {@link PlaywrightBrowserType} enum value.
     *
     * @param playwright  the Playwright instance
     * @param browserType the browser type to resolve
     * @return the corresponding {@link BrowserType}
     * @throws IllegalArgumentException if the browser type is not supported (e.g., {@code null})
     */
    public static BrowserType getBrowserType(Playwright playwright, PlaywrightBrowserType browserType) {
        switch (browserType) {
            case chromium:
                return playwright.chromium();
            case webkit:
                return playwright.webkit();
            case firefox:
                return playwright.firefox();
            default:
                throw new IllegalArgumentException("browserType is not supported");
        }
    }

    /**
     * Safely closes a Playwright page. If the page is {@code null} or already closed,
     * this method does nothing. Any exception during closure is logged and swallowed.
     *
     * @param page the page to close, may be {@code null}
     */
    public static void closePage(Page page) {
        try {
            if (Objects.nonNull(page) && !page.isClosed()){
                page.close();
            }
        } catch (Exception e) {
            log.error("Close Page Error.", e);
            // ignore error
        }
    }

}
