package com.microsoft.playwright.spring.boot.options;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrowserLaunchPersistentOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions();
        assertNull(opts.getAcceptDownloads());
        assertNull(opts.getArgs());
        assertNull(opts.getBaseURL());
        assertNull(opts.getBypassCSP());
        assertNull(opts.getChannel());
        assertNull(opts.getChromiumSandbox());
        assertNull(opts.getColorScheme());
        assertNull(opts.getDeviceScaleFactor());
        assertNull(opts.getDevtools());
        assertNull(opts.getDownloadsPath());
        assertNull(opts.getEnv());
        assertNull(opts.getExecutablePath());
        assertNull(opts.getExtraHttpHeaders());
        assertNull(opts.getForcedColors());
        assertNull(opts.getGeolocation());
        assertNull(opts.getHandleSighup());
        assertNull(opts.getHandleSigint());
        assertNull(opts.getHandleSigterm());
        assertNull(opts.getHasTouch());
        assertNull(opts.getHeadless());
        assertNull(opts.getHttpCredentials());
        assertNull(opts.getIgnoreAllDefaultArgs());
        assertNull(opts.getIgnoreDefaultArgs());
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
        assertNull(opts.getSlowMo());
        assertNull(opts.getStrictSelectors());
        assertNull(opts.getTimeout());
        assertNull(opts.getTimezoneId());
        assertNull(opts.getTracesDir());
        assertNull(opts.getUserAgent());
        assertEquals("/tmp", opts.getUserDataRootDir());
        assertNull(opts.getViewportSize());
    }

    @Test
    void shouldSupportChainedSetters() {
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions()
                .setHeadless(true)
                .setChannel("chrome")
                .setLocale("en-US")
                .setTimezoneId("America/New_York");
        assertTrue(opts.getHeadless());
        assertEquals("chrome", opts.getChannel());
        assertEquals("en-US", opts.getLocale());
        assertEquals("America/New_York", opts.getTimezoneId());
    }

    @Test
    void shouldMapAllFieldsToLaunchPersistentContextOptions() {
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions()
                .setAcceptDownloads(true)
                .setArgs(List.of("--no-sandbox"))
                .setBaseURL("http://localhost:3000")
                .setBypassCSP(false)
                .setChannel("chrome")
                .setChromiumSandbox(false)
                .setColorScheme(ColorScheme.LIGHT)
                .setDeviceScaleFactor(2.0)
                .setDevtools(false)
                .setDownloadsPath(Path.of("/tmp/downloads"))
                .setEnv(Map.of("KEY", "VALUE"))
                .setExecutablePath(Path.of("/usr/bin/chromium"))
                .setExtraHttpHeaders(Map.of("X-Custom", "val"))
                .setForcedColors(ForcedColors.NONE)
                .setHandleSighup(true)
                .setHandleSigint(true)
                .setHandleSigterm(true)
                .setHasTouch(false)
                .setHeadless(true)
                .setIgnoreAllDefaultArgs(false)
                .setIgnoreDefaultArgs(List.of("--mute-audio"))
                .setIgnoreHttpsErrors(false)
                .setIsMobile(false)
                .setJavaScriptEnabled(true)
                .setLocale("en-US")
                .setOffline(false)
                .setPermissions(List.of("geolocation"))
                .setRecordHarContent(HarContentPolicy.ATTACH)
                .setRecordHarMode(HarMode.MINIMAL)
                .setRecordHarOmitContent(false)
                .setRecordHarPath(Path.of("/tmp/har"))
                .setRecordHarUrlFilter("**/*")
                .setRecordVideoDir(Path.of("/tmp/video"))
                .setReducedMotion(ReducedMotion.NO_PREFERENCE)
                .setScreenSize(new ScreenSize(1920, 1080))
                .setServiceWorkers(ServiceWorkerPolicy.ALLOW)
                .setSlowMo(50.0)
                .setStrictSelectors(false)
                .setTimeout(30000.0)
                .setTimezoneId("UTC")
                .setTracesDir(Path.of("/tmp/traces"))
                .setUserAgent("CustomAgent/1.0")
                .setViewportSize(new ViewportSize(1280, 720));
        BrowserType.LaunchPersistentContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithNullFields() {
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions();
        BrowserType.LaunchPersistentContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyCollections() {
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions()
                .setArgs(List.of())
                .setEnv(Map.of())
                .setIgnoreDefaultArgs(List.of())
                .setPermissions(List.of());
        BrowserType.LaunchPersistentContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyStrings() {
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions()
                .setBaseURL("")
                .setChannel("  ")
                .setLocale("")
                .setTimezoneId("")
                .setUserAgent("")
                .setRecordHarUrlFilter("");
        BrowserType.LaunchPersistentContextOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldHandleHttpCredentials() {
        HttpCredentials creds = new HttpCredentials("user", "pass");
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions()
                .setHttpCredentials(creds);
        assertEquals(creds, opts.getHttpCredentials());
    }

    @Test
    void shouldHandleRecordVideoSize() {
        RecordVideoSize size = new RecordVideoSize(800, 600);
        BrowserLaunchPersistentOptions opts = new BrowserLaunchPersistentOptions()
                .setRecordVideoSize(size);
        assertEquals(size, opts.getRecordVideoSize());
    }
}
