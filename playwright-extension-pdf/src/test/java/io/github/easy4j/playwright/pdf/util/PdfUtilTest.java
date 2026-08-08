/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.easy4j.playwright.pdf.util;

import io.github.easy4j.playwright.pdf.bo.PdfMeta;
import io.github.easy4j.playwright.pdf.enums.PdfPageSize;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PdfUtil}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class PdfUtilTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("information() populates PDDocumentInformation from PdfMeta")
    void information_populatesAllFields() {
        PdfMeta meta = PdfMeta.builder()
                .author("Loong Wan")
                .keywords("playwright,pdf")
                .subject("rendering")
                .title("demo")
                .creator("easy4j")
                .build();

        PDDocumentInformation info = PdfUtil.information(meta);

        assertEquals("Loong Wan", info.getAuthor());
        assertEquals("playwright,pdf", info.getKeywords());
        assertEquals("rendering", info.getSubject());
        assertEquals("demo", info.getTitle());
        assertEquals("easy4j", info.getCreator());
        assertEquals("Playwright PDF Generator", info.getProducer());
        assertNotNull(info.getCreationDate());
        assertNotNull(info.getModificationDate());
    }

    @Test
    @DisplayName("information() tolerates null fields")
    void information_toleratesNullFields() {
        PdfMeta meta = PdfMeta.builder().build();
        PDDocumentInformation info = PdfUtil.information(meta);
        assertEquals("", info.getAuthor());
        assertEquals("", info.getKeywords());
        assertEquals("", info.getSubject());
        assertEquals("", info.getTitle());
        assertEquals("", info.getCreator());
    }

    @Test
    @DisplayName("addFullPageLink adds one annotation when URL is provided")
    void addFullPageLink_addsAnnotation() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PdfPageSize.A4.getRectangle());
            document.addPage(page);
            int before = page.getAnnotations().size();

            PdfUtil.addFullPageLink(document, page, "https://example.com", PdfPageSize.A4, false);

            assertEquals(before + 1, page.getAnnotations().size());
        }
    }

    @Test
    @DisplayName("addFullPageLink is a no-op when URL is blank")
    void addFullPageLink_blankUrlIsNoOp() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PdfPageSize.A4.getRectangle());
            document.addPage(page);
            int before = page.getAnnotations().size();

            PdfUtil.addFullPageLink(document, page, "  ", PdfPageSize.A4, false);

            assertEquals(before, page.getAnnotations().size());
        }
    }

    @Test
    @DisplayName("addFullPageLink throws on null document")
    void addFullPageLink_nullDocThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> PdfUtil.addFullPageLink(null, new PDPage(), "u", PdfPageSize.A4, false));
    }

    @Test
    @DisplayName("addLinkSimple adds one visible link annotation")
    void addLinkSimple_addsAnnotation() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PdfPageSize.A4.getRectangle());
            document.addPage(page);

            PdfUtil.addLinkSimple(document, page, "click", "https://example.com");

            assertEquals(1, page.getAnnotations().size());
        }
    }

    @Test
    @DisplayName("PdfPageSize.getByName falls back to A4 on unknown input")
    void pdfPageSize_fallsBackToA4() {
        assertEquals(PdfPageSize.A4, PdfPageSize.getByName(null));
        assertEquals(PdfPageSize.A4, PdfPageSize.getByName(""));
        assertEquals(PdfPageSize.A4, PdfPageSize.getByName("garbage"));
        assertEquals(PdfPageSize.A3, PdfPageSize.getByName("A3"));
        assertEquals(PdfPageSize.A3, PdfPageSize.getByName("a3"));
    }

    @Test
    @DisplayName("round-trip: write document to temp file and re-read")
    void roundTrip_writeAndRead() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.setDocumentInformation(PdfUtil.information(
                    PdfMeta.builder().title("round-trip").build()));
            PDPage page = new PDPage(PdfPageSize.A4.getRectangle());
            document.addPage(page);
            Path out = tempDir.resolve("test.pdf");
            document.save(out.toFile());
        }

        try (PDDocument reloaded = org.apache.pdfbox.Loader.loadPDF(tempDir.resolve("test.pdf").toFile())) {
            assertThat(reloaded).isNotNull();
            assertEquals(1, reloaded.getNumberOfPages());
            assertEquals("round-trip", reloaded.getDocumentInformation().getTitle());
        }
    }
}