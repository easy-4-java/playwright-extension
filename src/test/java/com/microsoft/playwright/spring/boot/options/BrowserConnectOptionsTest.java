package com.microsoft.playwright.spring.boot.options;

import com.microsoft.playwright.BrowserType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrowserConnectOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        BrowserConnectOptions opts = new BrowserConnectOptions();
        assertNull(opts.getHeaders());
        assertEquals(0.0, opts.getSlowMo());
        assertEquals(0.0, opts.getTimeout());
    }

    @Test
    void shouldSupportChainedSetters() {
        BrowserConnectOptions opts = new BrowserConnectOptions()
                .setHeaders(Map.of("Authorization", "Bearer token"))
                .setSlowMo(100.0)
                .setTimeout(5000.0);
        assertEquals(Map.of("Authorization", "Bearer token"), opts.getHeaders());
        assertEquals(100.0, opts.getSlowMo());
        assertEquals(5000.0, opts.getTimeout());
    }

    @Test
    void shouldMapToConnectOptionsWithAllFields() {
        BrowserConnectOptions opts = new BrowserConnectOptions()
                .setHeaders(Map.of("X-Custom", "val"))
                .setSlowMo(50.0)
                .setTimeout(10000.0);
        BrowserType.ConnectOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapToConnectOptionsWithNullFields() {
        BrowserConnectOptions opts = new BrowserConnectOptions()
                .setHeaders(null)
                .setSlowMo(null)
                .setTimeout(null);
        BrowserType.ConnectOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapToConnectOptionsWithDefaults() {
        BrowserConnectOptions opts = new BrowserConnectOptions();
        BrowserType.ConnectOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
