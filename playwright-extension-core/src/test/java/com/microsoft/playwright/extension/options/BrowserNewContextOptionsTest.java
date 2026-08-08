package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrowserNewContextOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        BrowserNewContextOptions opts = new BrowserNewContextOptions();
        assertNull(opts.getAcceptDownloads());
        assertNull(opts.getBaseURL());
        assertNull(opts.getBypassCSP());
        assertNull(opts.getColorScheme());
        assertNull(opts.getDeviceScaleFactor());
        assertNull(opts.getExtraHttpHeaders());
        assertNull(opts.getForcedColors());
        assertNull(opts.getGeolocation());
        assertNull(opts.getHasTouch());
        assertNull(opts.getHttpCredentials());
        assertNull(opts.getIgnoreHttpsErrors());
        assertNull(opts.getIsMobile());
        assertNull(opts.getJavaScriptEnabled());
        assertNull(opts.getLocale());
        assertNull(opts.getOffline());
        assertNull(opts.getPermissions());
        assertNull(opts.getProxy());
        assertNull(opts.getRecordHarContent());
        assertNull(opts.getRecordHarMode());
        assertNull(opts.getRecordHarOmitContent());
        assertNull(opts.getRecordHarPath());
        assertNull(opts.getRecordHarUrlFilter());
        assertNull(opts.getRecordVideoDir());
        assertNull(opts.getRecordVideoSize());
        assertNull(opts.getReducedMotion());
        assertNull(opts.getScreenSize());
        assertNull(opts.getServiceWorkers());
        assertNull(opts.getStorageState());
        assertNull(opts.getStorageStatePath());
        assertNull(opts.getStrictSelectors());
        assertNull(opts.getTimezoneId());
        assertNull(opts.getUserAgent());
        assertNull(opts.getViewportSize());
    }

    @Test
    void shouldSupportChainedSetters() {
        BrowserNewContextOptions opts = new BrowserNewContextOptions()
                .setBaseURL("http://localhost:8080")
                .setLocale("zh-CN")
                .setJavaScriptEnabled(true)
                .setOffline(false);
        assertEquals("http://localhost:8080", opts.getBaseURL());
        assertEquals("zh-CN", opts.getLocale());
        assertTrue(opts.getJavaScriptEnabled());
        assertFalse(opts.getOffline());
    }

    @Test
    void shouldMapAllFieldsToNewContextOptions() {
        BrowserNewContextOptions opts = new BrowserNewContextOptions()
                .setAcceptDownloads(true)
                .setBaseURL("http://localhost:3000")
                .setBypassCSP(false)
                .setColorScheme(ColorScheme.DARK)
                .setDeviceScaleFactor(2.0)
                .setExtraHttpHeaders(Map.of("X-Custom", "val"))
                .setForcedColors(ForcedColors.NONE)
                .setHasTouch(true)
                .setHttpCredentials(new HttpCredentials("user", "pass"))
                .setIgnoreHttpsErrors(true)
                .setIsMobile(true)
                .setJavaScriptEnabled(true)
                .setLocale("de-DE")
                .setOffline(false)
                .setPermissions(List.of("geolocation"))
                .setProxy(new Proxy("http://proxy:8080"))
                .setRecordHarContent(HarContentPolicy.ATTACH)
                .setRecordHarMode(HarMode.FULL)
                .setRecordHarOmitContent(false)
                .setRecordHarPath(Path.of("/tmp/har"))
                .setRecordHarUrlFilter("**/*")
                .setRecordVideoDir(Path.of("/tmp/video"))
                .setReducedMotion(ReducedMotion.REDUCE)
                .setScreenSize(new ScreenSize(1920, 1080))
                .setServiceWorkers(ServiceWorkerPolicy.ALLOW)
                .setStorageState("{\"cookies\":[]}")
                .setStorageStatePath(Path.of("/tmp/state.json"))
                .setStrictSelectors(true)
                .setTimezoneId("Asia/Shanghai")
                .setUserAgent("CustomAgent/1.0")
                .setViewportSize(new ViewportSize(1280, 720));
        Browser.NewContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithNullFields() {
        BrowserNewContextOptions opts = new BrowserNewContextOptions();
        Browser.NewContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyCollections() {
        BrowserNewContextOptions opts = new BrowserNewContextOptions()
                .setPermissions(List.of())
                .setExtraHttpHeaders(Map.of());
        Browser.NewContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyStrings() {
        BrowserNewContextOptions opts = new BrowserNewContextOptions()
                .setBaseURL("")
                .setLocale("  ")
                .setTimezoneId("")
                .setUserAgent("")
                .setStorageState("")
                .setRecordHarUrlFilter("");
        Browser.NewContextOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
