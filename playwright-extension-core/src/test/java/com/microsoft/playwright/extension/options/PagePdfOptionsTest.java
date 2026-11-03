package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PagePdfOptionsTest {

    @Test
    void defaultsLeaveOptionalPlaywrightFieldsUnset() {
        Page.PdfOptions options = new PagePdfOptions().toOptions();

        assertNull(options.displayHeaderFooter);
        assertNull(options.path);
        assertNull(options.scale);
    }

    @Test
    void mapsExplicitValuesAndSkipsBlankText() {
        PagePdfOptions settings = new PagePdfOptions();
        Path output = Path.of("report.pdf");
        settings.setDisplayHeaderFooter(false);
        settings.setFormat("A4");
        settings.setFooterTemplate("   ");
        settings.setPath(output);
        settings.setScale(0.5);

        Page.PdfOptions options = settings.toOptions();

        assertFalse(options.displayHeaderFooter);
        assertEquals("A4", options.format);
        assertNull(options.footerTemplate);
        assertEquals(output, options.path);
        assertEquals(0.5, options.scale);
    }
}
