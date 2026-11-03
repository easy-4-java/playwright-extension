package com.microsoft.playwright.spring.boot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaywrightPropertiesTest {

    @Test
    void bindsCoreBrowserProperties() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "playwright.memory-threshold", "0.70",
                "playwright.isolated", "true",
                "playwright.browser-type", "firefox")));

        PlaywrightProperties properties = binder.bind("playwright", Bindable.of(PlaywrightProperties.class)).get();

        assertEquals(0.70, properties.getMemoryThreshold());
        assertTrue(properties.isIsolated());
        assertEquals(PlaywrightProperties.BrowserTypeEnum.firefox, properties.getBrowserType());
    }
}
