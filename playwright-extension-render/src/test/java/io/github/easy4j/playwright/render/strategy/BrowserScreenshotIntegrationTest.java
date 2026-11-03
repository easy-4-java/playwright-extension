package io.github.easy4j.playwright.render.strategy;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.extension.options.ElementScreenshotOptions;
import com.microsoft.playwright.extension.options.PageScreenshotOptions;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the actual page screenshot callbacks with a local Chromium page. */
class BrowserScreenshotIntegrationTest {

    @Test
    void capturesWholePageAndElementAndReportsMissingElement() throws IOException {
        WkhtmlToImageBufferRenderStrategy strategy = new WkhtmlToImageBufferRenderStrategy();
        strategy.pageOptionsProvider = optionsProvider();
        strategy.renderOptions = bufferRenderOptions();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch()) {
            Page page = browser.newPage();
            page.setContent("<html><body><div id='target' style='width:40px;height:30px;background:red'></div></body></html>");

            PageRenderBO wholePage = strategy.doPageScreenShot("render-1", null)
                    .apply(page, new PageRenderBO().setIndex(0));
            assertEquals("0.png", wholePage.getName());
            assertNotNull(wholePage.getBuffer());
            assertTrue(wholePage.getBuffer().length > 0);
            assertNotNull(ImageIO.read(new ByteArrayInputStream(wholePage.getBuffer())));

            PageRenderBO element = strategy.doPageScreenShot("render-1", "#target")
                    .apply(page, new PageRenderBO().setIndex(1));
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(element.getBuffer()));
            assertNotNull(image);
            assertEquals(40, image.getWidth());
            assertEquals(30, image.getHeight());
            assertEquals(0xFF0000, image.getRGB(20, 15) & 0xFFFFFF);

            PageRenderBO missing = strategy.doPageScreenShot("render-1", "#missing")
                    .apply(page, new PageRenderBO().setIndex(2));
            assertEquals(RenderState.FAIL, missing.getRenderState());
            assertTrue(missing.getRenderFailedReason().contains("#missing"));
        }
    }

    private static PageOptionsProvider optionsProvider() {
        return (PageOptionsProvider) Proxy.newProxyInstance(
                PageOptionsProvider.class.getClassLoader(), new Class<?>[]{PageOptionsProvider.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPageScreenshotOptions" -> new PageScreenshotOptions();
                    case "getElementScreenshotOptions" -> new ElementScreenshotOptions();
                    default -> throw new AssertionError("Unexpected option: " + method.getName());
                });
    }

    private static RenderOptions bufferRenderOptions() {
        return (RenderOptions) Proxy.newProxyInstance(
                RenderOptions.class.getClassLoader(), new Class<?>[]{RenderOptions.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("isWriteToFile")) {
                        return false;
                    }
                    throw new AssertionError("Unexpected render option: " + method.getName());
                });
    }
}
