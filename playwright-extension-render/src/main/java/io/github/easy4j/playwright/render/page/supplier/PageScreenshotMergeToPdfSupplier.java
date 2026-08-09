package io.github.easy4j.playwright.render.page.supplier;

import io.github.easy4j.playwright.render.strategy.RenderOptions;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.PDPageSize;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.exception.TaskRuntimeException;
import io.github.easy4j.playwright.render.page.checker.PageScreenshotChecker;
import io.github.easy4j.playwright.render.redis.BizRedisKey;
import io.github.easy4j.playwright.render.util.ImageUtil;
import io.github.easy4j.playwright.render.util.PdfUtil;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import io.github.easy4j.playwright.task.store.TaskStateStore;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Slf4j
public class PageScreenshotMergeToPdfSupplier implements Supplier<WkhtmlRenderResultVO> {

    @Getter
    protected RenderOptions renderOptions;
    @Getter
    protected WkhtmlRenderBO renderBO;
    @Getter
    protected List<PageRenderBO> screenshots;
    @Getter
    protected List<PageScreenshotChecker> pageScreenshotCheckers;
    @Getter
    protected BiFunction<PDDocument, List<PageRenderBO>, WkhtmlRenderResultVO> biFunction;
    @Getter
    protected TaskStateStore taskStateStore;

    public PageScreenshotMergeToPdfSupplier(RenderOptions renderOptions,
                                            WkhtmlRenderBO renderBO,
                                            List<PageRenderBO> screenshots,
                                            List<PageScreenshotChecker> pageScreenshotCheckers,
                                            BiFunction<PDDocument, List<PageRenderBO>, WkhtmlRenderResultVO> biFunction,
                                            TaskStateStore taskStateStore) {
        this.renderOptions = renderOptions;
        this.renderBO = renderBO;
        this.screenshots = screenshots;
        this.pageScreenshotCheckers = pageScreenshotCheckers;
        this.biFunction = biFunction;
        this.taskStateStore = taskStateStore;
    }

    @Override
    public WkhtmlRenderResultVO get() {

        //  PDF 重试生成，失败页替换降低整体时间消耗逻辑

        // 1、如果存在源文件URL，则尝试从远程加载源文件
        // 2、如果源文件不存在，则尝试从本地加载源文件（防止远程 PDF 加载失败）

        PDDocument pdDocument = null;
        try {
            // 创建目标 PDF 文档
            pdDocument = new PDDocument();
            // 设置PDF属性
            pdDocument.setDocumentInformation(PdfUtil.information(renderBO));
            // 调用回调函数，返回合并后的结果
            screenshots.sort(Comparator.comparingInt(PageRenderBO::getIndex));
            return biFunction.apply( this.addPages(pdDocument, screenshots), screenshots);

        } catch (Exception e) {
            if(e instanceof TaskRuntimeException){
                throw ExceptionUtils.throwableOfType(e, TaskRuntimeException.class);
            }
            throw new TaskRuntimeException("Marge pdf error", e);
        } finally {
            // 确保资源被正确关闭
            IOUtils.closeQuietly(pdDocument);
        }
    }

    protected void doCustomCheck(PageRenderBO pageRenderBO, BufferedImage pdfImage, PDPageSize pdPageSize) {
        // 如果图片不为空，且存在检查器，则进行图片添加到PDF对象前的检查
        if(renderOptions.isUseCustomCheck() && !(pageScreenshotCheckers == null || pageScreenshotCheckers.isEmpty())){
            for(PageScreenshotChecker checker : pageScreenshotCheckers){
                if(Objects.nonNull(checker) && !checker.beforePdfPageAdd(renderBO, pageRenderBO, pdfImage, pdPageSize)){
                    if(Objects.isNull(pageRenderBO.getRenderState())){
                        pageRenderBO.setCheckState(CheckState.IMG_CHECK_FAIL);
                        pageRenderBO.setCheckFailedReason("[{\"initiatorType\":\"resource\",\"name\":\"截图图片检查未通过, 可能是空白页或数据渲染未成功.\",\"responseStatus\":500}]");
                    }
                    break;
                }
            }
        }
    }

    /**
     * 添加页面（如果某个页面上次已经成功生成，这次则直接引用）
     * @param pdDocument PDF文档对象（即新创建的）
     */
    protected PDDocument addPages(PDDocument pdDocument, List<PageRenderBO> screenshots) {
        // 检查截图列表是否为空
        if ((screenshots == null || screenshots.isEmpty())) {
            log.warn("No screenshots provided, skipping PDF page addition.");
            return pdDocument;
        }

        // 初始化资源
        BufferedImage pdfImage = null;
        BufferedImage emptyImage = null;
        PDImageXObject pdImageObject = null;
        PDPage pdPage;
        PDPageSize pdPageSize = PDPageSize.getByName(renderBO.getPageSize());

        try {
            // 报告单渲染状态缓存
            String rdsKey = BizRedisKey.renderStateKey(renderBO.getTaskId());
            Map<String, String> stateMap = new java.util.HashMap<>(taskStateStore.getAllStates(renderBO.getTaskId()));

            screenshots.sort(Comparator.comparingInt(PageRenderBO::getIndex));
            for (PageRenderBO pageRenderBO : screenshots) {
                // 处理图片加载
                try {

                    // 创建新页面并添加图片
                    pdPage = new PDPage(pdPageSize.getRectangle());

                    // 加载图片
                    pdfImage = this.loadImage(pageRenderBO, pdPageSize);
                    boolean isLoadSuccess = true;
                    // 如果图片为空，则使用空白图片
                    if (pdfImage == null) {
                        isLoadSuccess = false;
                        pageRenderBO.setCheckState(CheckState.WEB_CHECK_FAIL);
                        pageRenderBO.setCheckFailedReason("[{\"initiatorType\":\"resource\",\"name\":\"页面截图失败，空白截图！\",\"responseStatus\":500}]");
                        // 使用空白图片
                        emptyImage = ImageUtil.getWhiteImage(
                                Float.valueOf(pdPageSize.getRectangle().getWidth()).intValue(),
                                Float.valueOf(pdPageSize.getRectangle().getHeight()).intValue()
                        );
                        pdfImage = emptyImage;
                        try {
                            String failedReason ="错误信息：页面截图失败，访问资源不存或接口报错！";
                            // 使用更简单的水印方法
                            pdfImage = addTextWatermark(pdfImage, failedReason, null);
                        } catch (Exception e) {
                            log.error("添加水印失败: ", e);
                        }
                        try {
                            // 整页添加链接 - 检查失败的页面显示可见链接提示
                            PdfUtil.addFullPageLink(pdDocument, pdPage, pageRenderBO.getUrl(), pdPageSize, true);
                        } catch (Exception e) {
                            log.error("添加链接失败: ", e);
                        }
                    }

                    pdDocument.addPage(pdPage);

                    // 创建图片对象并绘制
                    pdImageObject = LosslessFactory.createFromImage(pdDocument, pdfImage);
                    drawImageToPage(pdDocument, pdPage, pdImageObject, pdfImage, pdPageSize);

                    // 当前页面截图正常，添加到 PDF 成功，则更新渲染状态为成功
                    if (isLoadSuccess){
                        pageRenderBO.setRenderState(RenderState.SUCCESS);
                        if(StringUtils.isNotBlank(pageRenderBO.getUniqueId())){
                            stateMap.put(pageRenderBO.getUniqueId(), RenderState.SUCCESS.name());
                        }
                    } else {
                        if(StringUtils.isNotBlank(pageRenderBO.getUniqueId())){
                            stateMap.put(pageRenderBO.getUniqueId(), RenderState.FAIL.name());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing page: {}", e.getMessage());
                    stateMap.put(pageRenderBO.getUniqueId(), RenderState.FAIL.name());
                    pageRenderBO.setCheckState(CheckState.IMG_CHECK_FAIL);
                    pageRenderBO.setCheckFailedReason("[{\"initiatorType\":\"resource\",\"name\":\"页面生成 PDF 失败，访问资源不存或接口报错！\",\"responseStatus\":500}]");
                } finally {
                    // 释放当前页面的资源
                    releaseResources(pdImageObject, pdfImage, emptyImage);
                }
            }

            // 有渲染状态缓存时候
            if (!stateMap.isEmpty()) {
                taskStateStore.setAllStates(renderBO.getTaskId(), stateMap, Duration.ofDays(2));
            }
        } finally {
            // 释放所有资源
            releaseResources(pdImageObject, pdfImage, emptyImage);
        }

        return pdDocument;
    }

    /**
     * 加载图片
     */
    private BufferedImage loadImage(PageRenderBO pageRenderBO, PDPageSize pdPageSize) throws IOException {
        if (Objects.nonNull(pageRenderBO.getBuffer())) {
            log.debug("Loading image from buffer, size: {}", pageRenderBO.getBuffer().length);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(pageRenderBO.getBuffer())) {
                BufferedImage image = ImageIO.read(bis);
                doCustomCheck(pageRenderBO, image, pdPageSize);
                return image;
            }
        } else if (StringUtils.isNotBlank(pageRenderBO.getPath())) {
            log.debug("Loading image from path: {}", pageRenderBO.getPath());
            File imgFile = new File(pageRenderBO.getPath());
            if (imgFile.exists()) {
                BufferedImage image = ImageIO.read(imgFile);
                doCustomCheck(pageRenderBO, image, pdPageSize);
                return image;
            } else {
                log.warn("Image file not found: {}", pageRenderBO.getPath());
                pageRenderBO.setCheckState(CheckState.IMG_CHECK_FAIL);
                pageRenderBO.setCheckFailedReason("[{\"initiatorType\":\"resource\",\"name\":\"图片文件不存在：\"" + pageRenderBO.getPath() + ",\"responseStatus\":500}]");
            }
        }
        return null;
    }

    /**
     * 将图片绘制到PDF页面
     */
    private void drawImageToPage(PDDocument pdDocument, PDPage pdPage, PDImageXObject pdImageObject,
                               BufferedImage pdfImage, PDPageSize pdPageSize) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(
                pdDocument, pdPage, PDPageContentStream.AppendMode.OVERWRITE, renderBO.getCompress())) {

            float scaleFactor = Math.min(
                pdPageSize.getRectangle().getWidth() / pdfImage.getWidth(),
                pdPageSize.getRectangle().getHeight() / pdfImage.getHeight()
            );

            float scaledWidth = pdfImage.getWidth() * scaleFactor;
            float scaledHeight = pdfImage.getHeight() * scaleFactor;

            log.debug("Image scaling - factor: {}, width: {}, height: {}",
                     scaleFactor, scaledWidth, scaledHeight);

            contentStream.drawImage(pdImageObject, 0, 0, scaledWidth, scaledHeight);
        }
    }

    /**
     * 释放资源
     */
    private void releaseResources(PDImageXObject pdImageObject, BufferedImage pdfImage, BufferedImage emptyImage) {
        if (pdImageObject != null) {
            pdImageObject = null;
        }
        if (pdfImage != null) {
            pdfImage.flush();
        }
        if (emptyImage != null) {
            emptyImage.flush();
        }
    }

    /**
     * 直接在图片上添加文本水印
     * @param image 原图片
     * @param failedReason 错误信息
     * @param detailReason 详细错误信息
     * @return 添加水印后的图片
     */
    private BufferedImage addTextWatermark(BufferedImage image, String failedReason, String detailReason) {
        try {
            // 创建与原图相同大小的新图片
            BufferedImage watermarkedImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            // 绘制原图
            Graphics2D g2d = watermarkedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);

            // 设置水印文字样式
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));

            // 设置透明度
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

            // 绘制第一行水印（错误信息）
            if (StringUtils.isNotBlank(failedReason)) {
                g2d.drawString(failedReason, 10, 30);
            }

            // 绘制第二行水印（详细错误信息）
            if (StringUtils.isNotBlank(detailReason)) {
                // 如果详细错误信息太长，进行换行处理
                String[] lines = wrapText(detailReason, 80);
                int y = 60;
                for (String line : lines) {
                    g2d.drawString(line, 10, y);
                    y += 25;
                    // 最多显示5行，避免水印过多
                    if (y > 150) {
                        g2d.drawString("...", 10, y);
                        break;
                    }
                }
            }

            g2d.dispose();
            return watermarkedImage;

        } catch (Exception e) {
            log.error("添加文本水印失败", e);
            return image; // 如果失败，返回原图
        }
    }

    /**
     * 文本换行处理
     * @param text 原文本
     * @param maxLength 每行最大长度
     * @return 换行后的文本数组
     */
    private String[] wrapText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return new String[]{text};
        }

        List<String> lines = new java.util.ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());
            if (end < text.length()) {
                // 尝试在空格处换行
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }
            lines.add(text.substring(start, end));
            start = end;
        }

        return lines.toArray(new String[0]);
    }

}
