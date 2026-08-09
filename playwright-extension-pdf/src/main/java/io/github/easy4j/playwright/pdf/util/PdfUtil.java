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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * PDF utilities: metadata, full-page hyperlinks, simple visible links,
 * remote PDF loading. Pure Java (no Spring / Redis / Playwright dependency).
 *
 * <p>Adapted from ddd4j-cloud-cmpt-playwright's {@code PdfUtil}. The original
 * method {@code information(WkhtmlRenderBO)} is generalised to
 * {@link #information(PdfMeta)} so the PDF module can stand alone.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
/**
 * Utility class for PDF operations including merging and page manipulation.
 */
public final class PdfUtil {

    private PdfUtil() {
        // utility class
    }

    /**
     * Build a {@link PDDocumentInformation} from a {@link PdfMeta}.
     */
    public static PDDocumentInformation information(PdfMeta meta) {
        PDDocumentInformation info = new PDDocumentInformation();
        info.setAuthor(defaultIfNull(meta.getAuthor()));
        info.setKeywords(defaultIfNull(meta.getKeywords()));
        info.setSubject(defaultIfNull(meta.getSubject()));
        info.setTitle(defaultIfNull(meta.getTitle()));
        info.setProducer(meta.getProducer());
        info.setCreator(defaultIfNull(meta.getCreator()));
        info.setCreationDate(Calendar.getInstance());
        info.setModificationDate(Calendar.getInstance());
        return info;
    }

    /**
     * Add a clickable link annotation covering the whole page.
     *
     * @param pdDocument     target document (must not be null)
     * @param pdPage         target page (must not be null)
     * @param url            link URL (skipped silently when blank)
     * @param pdPageSize     page size used to compute the link rectangle
     * @param showVisibleLink {@code true} to draw a 1-pt underline, {@code false} for an invisible link
     * @throws IOException if the link cannot be added
     */
    public static void addFullPageLink(PDDocument pdDocument, PDPage pdPage, String url,
                                        PdfPageSize pdPageSize, boolean showVisibleLink) throws IOException {
        if (pdDocument == null) {
            throw new IllegalArgumentException("PDF文档不能为空");
        }
        if (pdPage == null) {
            throw new IllegalArgumentException("PDF页面不能为空");
        }
        if (url == null || url.trim().isEmpty()) {
            log.warn("URL为空，跳过整页链接添加");
            return;
        }
        if (pdPageSize == null || pdPageSize.getRectangle() == null) {
            throw new IllegalArgumentException("页面尺寸不能为空");
        }
        try {
            PDAnnotationLink annotationLink = buildFullPageLink(url, pdPageSize, showVisibleLink);
            pdPage.getAnnotations().add(annotationLink);
            log.debug("成功为页面添加整页链接: {}, 可见性: {}", url, showVisibleLink);
        } catch (Exception e) {
            log.error("添加整页链接失败: url={}", url, e);
        }
    }

    private static PDAnnotationLink buildFullPageLink(String url, PdfPageSize pdPageSize, boolean showVisibleLink) {
        PDActionURI actionURI = new PDActionURI();
        actionURI.setURI(url);

        PDAnnotationLink annotationLink = new PDAnnotationLink();
        annotationLink.setAction(actionURI);

        PDRectangle pageRect = pdPageSize.getRectangle();
        annotationLink.setRectangle(new PDRectangle(0, 0, pageRect.getWidth(), pageRect.getHeight()));
        annotationLink.setContents("点击访问源页面: " + url);

        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        if (showVisibleLink) {
            borderStyle.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            borderStyle.setWidth(1);
        } else {
            borderStyle.setStyle(PDBorderStyleDictionary.STYLE_SOLID);
            borderStyle.setWidth(0);
        }
        annotationLink.setBorderStyle(borderStyle);
        return annotationLink;
    }

    /**
     * Load a remote PDF via HEAD-then-GET pattern with size validation.
     *
     * @return the loaded document, or {@code null} on any error
     */
    public static PDDocument loadRemotePdf(String fileUrl, long connectTimeout, long readTimeout) {
        HttpURLConnection headConnection = null;
        HttpURLConnection getConnection = null;
        try {
            URL url = new URL(fileUrl);

            headConnection = (HttpURLConnection) url.openConnection();
            headConnection.setConnectTimeout((int) connectTimeout);
            headConnection.setReadTimeout((int) readTimeout);
            headConnection.setRequestMethod("HEAD");

            int responseCode = headConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("Source PDF file not found or not accessible. Response code: {}", responseCode);
                return null;
            }

            String contentType = headConnection.getContentType();
            if (contentType == null || !contentType.toLowerCase().contains("application/pdf")) {
                log.warn("Invalid file type. Expected PDF but got: {}", contentType);
                return null;
            }

            int fileSize = headConnection.getContentLength();
            if (fileSize <= 0) {
                log.warn("Invalid file size: {}", fileSize);
                return null;
            }
            log.debug("Source PDF file size: {} bytes", fileSize);

            headConnection.disconnect();
            headConnection = null;

            getConnection = (HttpURLConnection) url.openConnection();
            getConnection.setConnectTimeout((int) connectTimeout);
            getConnection.setReadTimeout((int) readTimeout);

            try (BufferedInputStream in = new BufferedInputStream(getConnection.getInputStream(), 8192)) {
                byte[] pdfBytes = IOUtils.toByteArray(in);
                if (pdfBytes.length != fileSize) {
                    log.warn("File size mismatch. Expected: {}, Actual: {}", fileSize, pdfBytes.length);
                    return null;
                }
                return Loader.loadPDF(pdfBytes);
            }
        } catch (Exception e) {
            log.warn("Failed to load remote PDF file: {}", fileUrl, e);
            return null;
        } finally {
            if (headConnection != null) {
                headConnection.disconnect();
            }
            if (getConnection != null) {
                getConnection.disconnect();
            }
        }
    }

    /**
     * Add a simple visible link (blue underlined text + hyperlink) to a page.
     */
    public static void addLinkSimple(PDDocument document, PDPage pdPage, String text, String url,
                                      float x, float y, float fontSize) throws IOException {
        if (document == null || pdPage == null) {
            throw new IllegalArgumentException("PDF文档和页面不能为空");
        }
        if (text == null || text.trim().isEmpty() || url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("链接文本和URL不能为空");
        }
        if (fontSize <= 0) {
            fontSize = 12;
        }
        try {
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float pageHeight = pdPage.getMediaBox().getHeight();
            float textWidth = text.length() * fontSize * 0.6f;
            float textHeight = fontSize;
            float y1 = pageHeight - y - textHeight;

            try (PDPageContentStream content = new PDPageContentStream(document, pdPage,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                content.beginText();
                content.setFont(font, fontSize);
                content.setNonStrokingColor(Color.BLUE);
                content.newLineAtOffset(x, y1);
                content.showText(text);
                content.endText();

                content.setStrokingColor(Color.BLUE);
                content.setLineWidth(0.5f);
                content.moveTo(x, y1 - 2);
                content.lineTo(x + textWidth, y1 - 2);
                content.stroke();
            }

            PDAnnotationLink link = new PDAnnotationLink();
            link.setRectangle(new PDRectangle(x, y1, textWidth, textHeight));
            link.setContents(text);

            PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
            borderStyle.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            link.setBorderStyle(borderStyle);

            PDActionURI action = new PDActionURI();
            action.setURI(url);
            link.setAction(action);
            pdPage.getAnnotations().add(link);
        } catch (Exception e) {
            log.error("添加链接失败: text={}, url={}", text, url, e);
            throw new IOException("添加链接失败", e);
        }
    }

    /** {@link #addLinkSimple(PDDocument, PDPage, String, String, float, float, float)} with default coords. */
    public static void addLinkSimple(PDDocument document, PDPage pdPage, String text, String url) throws IOException {
        addLinkSimple(document, pdPage, text, url, 10, 20, 12);
    }

    private static String defaultIfNull(String value) {
        return value == null ? "" : value;
    }
}