package io.github.easy4j.playwright.render.page.checker;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.PDPageSize;
import io.github.easy4j.playwright.render.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Objects;

@Slf4j
/**
 * Default implementation of page screenshot comparison using pixel-level analysis.
 */
public class DefaultPageScreenshotChecker implements PageScreenshotChecker {

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public boolean beforePdfPageAdd(WkhtmlRenderBO renderBO, PageRenderBO pageRenderBO, BufferedImage pdfImage, PDPageSize pdfPageSize) {
        // 1、检查PDF图片是否为空
        if(Objects.isNull(pdfImage)){
            log.warn("PDF Image is null");
            pageRenderBO.setCheckState(CheckState.IMG_CHECK_FAIL);
            pageRenderBO.setCheckFailedReason("[{\"initiatorType\":\"resource\",\"name\":\"页面截图失败，访问资源不存或接口报错！\",\"responseStatus\":500}]");
            return Boolean.FALSE;
        }
        // 2、检查PDF图片是否是白色图片
        if (ImageUtil.isWhiteImage(pdfImage)) {
            pageRenderBO.setCheckState(CheckState.IMG_CHECK_FAIL);
            pageRenderBO.setCheckFailedReason("[{\"initiatorType\":\"resource\",\"name\":\"截图图片检查未通过，截图为白色图片！\",\"responseStatus\":500}]");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
