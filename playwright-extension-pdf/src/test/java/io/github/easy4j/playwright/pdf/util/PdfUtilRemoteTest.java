package io.github.easy4j.playwright.pdf.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PdfUtilRemoteTest {

    @Test
    void loadsValidPdfAfterHeadAndGet() throws Exception {
        byte[] pdf = singlePagePdf();
        AtomicInteger getRequests = new AtomicInteger();
        HttpServer server = server(exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                getRequests.incrementAndGet();
            }
            respond(exchange, 200, "application/pdf", pdf, pdf.length);
        });
        try {
            try (PDDocument document = PdfUtil.loadRemotePdf(url(server), 2_000, 2_000)) {
                assertNotNull(document);
                assertEquals(1, document.getNumberOfPages());
            }
            assertEquals(1, getRequests.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsNonPdfResponseBeforeGet() throws Exception {
        AtomicInteger getRequests = new AtomicInteger();
        HttpServer server = server(exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                getRequests.incrementAndGet();
            }
            byte[] content = "not a PDF".getBytes(StandardCharsets.UTF_8);
            respond(exchange, 200, "text/plain", content, content.length);
        });
        try {
            assertNull(PdfUtil.loadRemotePdf(url(server), 2_000, 2_000));
            assertEquals(0, getRequests.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsMismatchedGetLength() throws Exception {
        byte[] pdf = singlePagePdf();
        HttpServer server = server(exchange -> respond(exchange, 200, "application/pdf", pdf,
                "HEAD".equals(exchange.getRequestMethod()) ? pdf.length + 1 : pdf.length));
        try {
            assertNull(PdfUtil.loadRemotePdf(url(server), 2_000, 2_000));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsMissingPdf() throws Exception {
        HttpServer server = server(exchange -> respond(exchange, 404, "text/plain", new byte[0], 0));
        try {
            assertNull(PdfUtil.loadRemotePdf(url(server), 2_000, 2_000));
        } finally {
            server.stop(0);
        }
    }

    private static byte[] singlePagePdf() throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(out);
            return out.toByteArray();
        }
    }

    private static HttpServer server(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        server.createContext("/document.pdf", handler);
        server.start();
        return server;
    }

    private static String url(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/document.pdf";
    }

    private static void respond(HttpExchange exchange, int status, String contentType,
                                byte[] body, int declaredLength) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(declaredLength));
        try {
            if ("HEAD".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(status, -1);
            } else {
                exchange.sendResponseHeaders(status, body.length);
                exchange.getResponseBody().write(body);
            }
        } finally {
            exchange.close();
        }
    }
}
