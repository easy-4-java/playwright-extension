package io.github.easy4j.playwright.render.strategy;


import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.RenderType;
import io.github.easy4j.playwright.render.page.checker.PageScreenshotChecker;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;

import java.util.List;

/**
 * Playwright 处理策略
 */
public interface PlaywrightRenderStrategy<B extends WkhtmlRenderBO> {

    /**
     * 使用 Playwright 渲染引擎将 HTML 渲染为 PDF 和各种图像格式
     * @param renderBO 渲染参数来源 BO
     */
    WkhtmlRenderResultVO render(B renderBO) throws Exception;

    /**
     * 自定义检查接口
     * @param pageScreenshotCheckers 检查接口
     */
    void setPageScreenshotCheckers(List<PageScreenshotChecker> pageScreenshotCheckers);

    /**
     * 清理临时文件
     * @param renderBO
     * @param resultBO
     */
    void cleanTemporary(B renderBO, WkhtmlRenderResultVO resultBO);

    /**
     * 处理类型
     * @return
     */
    RenderType getRenderType();

}
