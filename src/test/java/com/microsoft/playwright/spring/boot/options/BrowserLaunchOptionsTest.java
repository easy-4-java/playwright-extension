package com.microsoft.playwright.spring.boot.options;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.Proxy;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrowserLaunchOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions();
        assertNull(opts.getArgs());
        assertNull(opts.getChannel());
        assertNull(opts.getChromiumSandbox());
        assertNull(opts.getDevtools());
        assertNull(opts.getDownloadsPath());
        assertNull(opts.getEnv());
        assertNull(opts.getExecutablePath());
        assertNull(opts.getFirefoxUserPrefs());
        assertNull(opts.getHandleSighup());
        assertNull(opts.getHandleSigint());
        assertNull(opts.getHandleSigterm());
        assertNull(opts.getHeadless());
        assertNull(opts.getIgnoreAllDefaultArgs());
        assertNull(opts.getIgnoreDefaultArgs());
        assertNull(opts.getProxy());
        assertNull(opts.getSlowMo());
        assertNull(opts.getTimeout());
        assertNull(opts.getTracesDir());
    }

    @Test
    void shouldSupportChainedSetters() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setChannel("chrome")
                .setHeadless(true)
                .setSlowMo(100.0)
                .setTimeout(30000.0);
        assertEquals("chrome", opts.getChannel());
        assertTrue(opts.getHeadless());
        assertEquals(100.0, opts.getSlowMo());
        assertEquals(30000.0, opts.getTimeout());
    }

    @Test
    void shouldMapAllFieldsToLaunchOptions() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setArgs(List.of("--no-sandbox"))
                .setChannel("chrome")
                .setChromiumSandbox(false)
                .setDevtools(false)
                .setDownloadsPath(Path.of("/tmp/downloads"))
                .setEnv(Map.of("KEY", "VALUE"))
                .setExecutablePath(Path.of("/usr/bin/chromium"))
                .setFirefoxUserPrefs(Map.of("key", (Object) "val"))
                .setHandleSighup(true)
                .setHandleSigint(true)
                .setHandleSigterm(true)
                .setHeadless(true)
                .setIgnoreAllDefaultArgs(false)
                .setIgnoreDefaultArgs(List.of("--mute-audio"))
                .setProxy(new Proxy("http://proxy:8080"))
                .setSlowMo(50.0)
                .setTimeout(60000.0)
                .setTracesDir(Path.of("/tmp/traces"));
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapToLaunchOptionsWithNullFields() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions();
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyArgsList() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setArgs(List.of());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyEnvMap() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setEnv(Map.of());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyFirefoxUserPrefs() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setFirefoxUserPrefs(Map.of());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyIgnoreDefaultArgs() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setIgnoreDefaultArgs(List.of());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
