package io.github.easy4j.playwright.render;

import io.github.easy4j.playwright.render.page.checker.DefaultPageScreenshotChecker;
import io.github.easy4j.playwright.render.page.checker.PageScreenshotChecker;
import io.github.easy4j.playwright.render.strategy.PlaywrightRenderStrategy;
import io.github.easy4j.playwright.render.strategy.PlaywrightRenderStrategyRouter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlaywrightRenderConfigurationTest {

    @Test
    void bindsRenderPropertiesAndRegistersStrategies() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "playwright.page-render-options.wait-for-selector", "#ready",
                    "playwright.page-render-options.load-wait-duration", "5s",
                    "playwright.page-render-options.cache.maximum-size", "25")));
            context.register(PlaywrightRenderConfiguration.class);
            context.refresh();

            PlaywrightRenderProperties properties = context.getBean(PlaywrightRenderProperties.class);
            assertEquals("#ready", properties.getWaitForSelector());
            assertEquals(Duration.ofSeconds(5), properties.getLoadWaitDuration());
            assertEquals(25, properties.getCache().getMaximumSize());
            assertEquals(6, context.getBeansOfType(PlaywrightRenderStrategy.class).size());
            assertNotNull(context.getBean(PlaywrightRenderStrategyRouter.class));
            assertInstanceOf(DefaultPageScreenshotChecker.class, context.getBean(PageScreenshotChecker.class));
        }
    }
}
