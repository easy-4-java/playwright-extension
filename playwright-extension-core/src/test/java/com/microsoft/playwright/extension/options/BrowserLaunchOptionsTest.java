package com.microsoft.playwright.extension.options;

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
                .setArgs(java.util.Collections.singletonList("--no-sandbox"))
                .setChannel("chrome")
                .setChromiumSandbox(false)
                .setDevtools(false)
                .setDownloadsPath(java.nio.file.Paths.get("/tmp/downloads"))
                .setEnv(java.util.Collections.singletonMap("KEY", "VALUE"))
                .setExecutablePath(java.nio.file.Paths.get("/usr/bin/chromium"))
                .setFirefoxUserPrefs(java.util.Collections.singletonMap("key", (Object) "val"))
                .setHandleSighup(true)
                .setHandleSigint(true)
                .setHandleSigterm(true)
                .setHeadless(true)
                .setIgnoreAllDefaultArgs(false)
                .setIgnoreDefaultArgs(java.util.Collections.singletonList("--mute-audio"))
                .setProxy(new Proxy("http://proxy:8080"))
                .setSlowMo(50.0)
                .setTimeout(60000.0)
                .setTracesDir(java.nio.file.Paths.get("/tmp/traces"));
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
                .setArgs(java.util.Collections.emptyList());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyEnvMap() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setEnv(java.util.Collections.emptyMap());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyFirefoxUserPrefs() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setFirefoxUserPrefs(java.util.Collections.emptyMap());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyIgnoreDefaultArgs() {
        BrowserLaunchOptions opts = new BrowserLaunchOptions()
                .setIgnoreDefaultArgs(java.util.Collections.emptyList());
        BrowserType.LaunchOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
