package io.github.easy4j.playwright.render.page.checker;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.PDPageSize;
import io.github.easy4j.playwright.render.strategy.Ordered;

import java.awt.image.BufferedImage;

/**
 * 页面截图检查器
 */
public interface PageScreenshotChecker extends Ordered, Comparable<PageScreenshotChecker> {

    /**
     * 页面截图后检查，用于检查页面截图是否符合要求
     * @param urlTemp 页面截图信息
     * @return 是否检查通过
     */
    default boolean afterPageScreenShot(PageRenderBO urlTemp) {
        return true;
    }

    /**
     * 添加PDF页面前检查，用于检查PDF图片是否符合要求
     * @param pageRenderBO 页面截图信息
     * @param pdfImage PDF图片
     * @param pdfPageSize 页面大小
     * @return 是否检查通过
     */
    default boolean beforePdfPageAdd(WkhtmlRenderBO renderBO, PageRenderBO pageRenderBO, BufferedImage pdfImage, PDPageSize pdfPageSize) {
        return true;
    }

    @Override
    default int compareTo(PageScreenshotChecker o){
        return Integer.compare(this.getOrder(), o.getOrder());
    }

}
