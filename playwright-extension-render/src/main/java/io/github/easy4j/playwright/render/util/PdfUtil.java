package io.github.easy4j.playwright.render.util;

import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.PDPageSize;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.StringJoiner;

@Slf4j
public class PdfUtil {

    public static PDDocumentInformation information(WkhtmlRenderBO renderBO){
        PDDocumentInformation information = new PDDocumentInformation();
        information.setAuthor(StringUtils.defaultString(renderBO.getAuthor()));
        information.setKeywords(StringUtils.defaultString(renderBO.getKeywords()));
        information.setSubject(StringUtils.defaultString(renderBO.getSubject())) ;
        information.setTitle(StringUtils.defaultString(renderBO.getAuthor()));
        information.setProducer("Playwright PDF Generater");
        information.setCreator(StringUtils.defaultString(renderBO.getCreator()));
        information.setCreationDate(Calendar.getInstance());
        information.setModificationDate(Calendar.getInstance());
        return information;
    }


    /**
     * 为PDF页面添加整页链接
     * @param pdDocument PDF文档对象
     * @param pdPage PDF页面对象
     * @param url 链接URL
     * @param pdPageSize 页面尺寸
     * @param showVisibleLink 是否显示可见的链接提示
     * @throws IOException 如果添加链接失败
     */
    public static void addFullPageLink(PDDocument pdDocument, PDPage pdPage, String url, PDPageSize pdPageSize, boolean showVisibleLink) throws IOException {
        // 参数验证
        if (pdDocument == null) {
            throw new IllegalArgumentException("PDF文档不能为空");
        }
        if (pdPage == null) {
            throw new IllegalArgumentException("PDF页面不能为空");
        }
        if (StringUtils.isBlank(url)) {
            log.warn("URL为空，跳过整页链接添加");
            return;
        }
        if (pdPageSize == null || pdPageSize.getRectangle() == null) {
            throw new IllegalArgumentException("页面尺寸不能为空");
        }

        try {

            // 创建URI动作
            PDAnnotationLink annotationLink = getPdAnnotationLink(url, pdPageSize, showVisibleLink);

            // 将链接添加到页面
            pdPage.getAnnotations().add(annotationLink);

            log.debug("成功为页面添加整页链接: {}, 可见性: {}", url, showVisibleLink);

        } catch (Exception e) {
            log.error("添加整页链接失败: url={}", url, e);
        }
    }

    private static PDAnnotationLink getPdAnnotationLink(String url, PDPageSize pdPageSize, boolean showVisibleLink) {
        PDActionURI actionURI = new PDActionURI();
        actionURI.setURI(url);

        // 创建链接注释
        PDAnnotationLink annotationLink = new PDAnnotationLink();
        annotationLink.setAction(actionURI);

        // 设置链接区域为整个页面
        PDRectangle pageRect = pdPageSize.getRectangle();
        annotationLink.setRectangle(new PDRectangle(0, 0, pageRect.getWidth(), pageRect.getHeight()));

        // 设置链接内容
        annotationLink.setContents("点击访问源页面: " + url);

        // 设置链接样式
        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        if (showVisibleLink) {
            // 显示可见的链接边框
            borderStyle.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            borderStyle.setWidth(1);
        } else {
            // 隐藏链接边框
            borderStyle.setStyle(PDBorderStyleDictionary.STYLE_SOLID);
            borderStyle.setWidth(0);
        }
        annotationLink.setBorderStyle(borderStyle);
        return annotationLink;
    }

    /**
     * 从远程URL加载PDF文件
     * @param fileUrl PDF文件URL
     * @return 加载的PDF文档，如果加载失败或文件不符合要求则返回null
     */
    public static PDDocument loadRemotePdf(String fileUrl, long connectTimeout, long readTimeout) {
        HttpURLConnection headConnection = null;
        HttpURLConnection getConnection = null;
        try {
            URL url = new URL(fileUrl);

            // 第一次连接：HEAD 请求检查文件
            headConnection = (HttpURLConnection) url.openConnection();
            headConnection.setConnectTimeout(Long.valueOf(connectTimeout).intValue());
            headConnection.setReadTimeout(Long.valueOf(readTimeout).intValue());
            headConnection.setRequestMethod("HEAD");

            // 检查响应状态
            int responseCode = headConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("Source PDF file not found or not accessible. Response code: {}", responseCode);
                return null;
            }

            // 检查 Content-Type
            String contentType = headConnection.getContentType();
            if (contentType == null || !contentType.toLowerCase().contains("application/pdf")) {
                log.warn("Invalid file type. Expected PDF but got: {}", contentType);
                return null;
            }

            // 获取文件大小
            int fileSize = headConnection.getContentLength();
            if (fileSize <= 0) {
                log.warn("Invalid file size: {}", fileSize);
                return null;
            }
            log.debug("Source PDF file size: {} bytes", fileSize);

            // 关闭 HEAD 请求连接
            headConnection.disconnect();
            headConnection = null;

            // 第二次连接：GET 请求获取文件内容
            getConnection = (HttpURLConnection) url.openConnection();
            getConnection.setConnectTimeout(Long.valueOf(connectTimeout).intValue());
            getConnection.setReadTimeout(Long.valueOf(readTimeout).intValue());

            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(getConnection.getInputStream(), 8192)) {
                // 使用 IOUtils 高效读取字节数组
                byte[] pdfBytes = IOUtils.toByteArray(bufferedInputStream);

                // 验证文件大小
                if (pdfBytes.length != fileSize) {
                    log.warn("File size mismatch. Expected: {}, Actual: {}", fileSize, pdfBytes.length);
                    return null;
                }

                // 加载并返回PDF文档
                return Loader.loadPDF(pdfBytes);
            }
        } catch (Exception e) {
            log.warn("Failed to load remote PDF file: {}", fileUrl, e);
            return null;
        } finally {
            // 确保所有连接都被正确关闭
            if (headConnection != null) {
                headConnection.disconnect();
            }
            if (getConnection != null) {
                getConnection.disconnect();
            }
        }
    }

    /**
     * 在PDF页面添加链接（简化版本，避免字体依赖）
     * @param document PDF文档
     * @param pdPage PDF页面
     * @param text 链接文本
     * @param url 链接URL
     * @param x 链接左上角X坐标
     * @param y 链接左上角Y坐标
     * @param fontSize 字体大小
     * @throws IOException 如果添加链接失败
     */
    public static void addLinkSimple(PDDocument document, PDPage pdPage, String text, String url, float x, float y, float fontSize) throws IOException {
        // 参数验证
        if (document == null || pdPage == null) {
            throw new IllegalArgumentException("PDF文档和页面不能为空");
        }
        if (StringUtils.isBlank(text) || StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("链接文本和URL不能为空");
        }
        if (fontSize <= 0) {
            fontSize = 12;
        }

        try {
            // 使用英文字体（避免中文字体依赖）
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            
            float pageHeight = pdPage.getMediaBox().getHeight();
            
            // 计算文本宽度（简化计算）
            float textWidth = text.length() * fontSize * 0.6f;
            float textHeight = fontSize;
            
            // 计算坐标
            float y1 = pageHeight - y - textHeight;

            // 添加文字
            try (PDPageContentStream content = new PDPageContentStream(document, pdPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                content.beginText();
                content.setFont(font, fontSize);
                content.setNonStrokingColor(Color.BLUE);
                content.newLineAtOffset(x, y1);
                content.showText(text);
                content.endText();
                
                // 绘制下划线
                content.setStrokingColor(Color.BLUE);
                content.setLineWidth(0.5f);
                content.moveTo(x, y1 - 2);
                content.lineTo(x + textWidth, y1 - 2);
                content.stroke();
            }

            // 添加超链接
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

    /**
     * 在PDF页面添加链接（简化版本，使用默认位置）
     * @param document PDF文档
     * @param pdPage PDF页面
     * @param text 链接文本
     * @param url 链接URL
     * @throws IOException 如果添加链接失败
     */
    public static void addLinkSimple(PDDocument document, PDPage pdPage, String text, String url) throws IOException {
        addLinkSimple(document, pdPage, text, url, 10, 20, 12);
    }

    public static void main(String[] args) {
        String userDir = "/home/admin";
        StringJoiner joiner = new StringJoiner(File.separator);
        joiner.add(File.separator).add(userDir).add("static").add("v3");
        log.info("staticFile:{}", joiner);
        System.out.println("staticFile: " + joiner);

    }
}
