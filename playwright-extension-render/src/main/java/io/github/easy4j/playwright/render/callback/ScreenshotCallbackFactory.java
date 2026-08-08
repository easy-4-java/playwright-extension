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
package io.github.easy4j.playwright.render.callback;

import com.microsoft.playwright.Page;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.enums.RenderState;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.function.BiFunction;

/**
 * Builds the {@code (Page, PageRenderBO) -> PageRenderBO} callback that
 * captures a screenshot via {@link Page#ScreenshotOptions}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public class ScreenshotCallbackFactory {

    /**
     * @param rendeId   render task id (for log + output dir)
     * @param config    render config (writeToFile / tmpDir)
     */
    public BiFunction<Page, PageRenderBO, PageRenderBO> create(String rendeId, RenderConfig config) {
        return (page, pageBO) -> {
            String fileName = String.format("%d.png", pageBO.getIndex());
            pageBO.setName(fileName);
            pageBO.setFileSize(0L);
            try {
                Page.ScreenshotOptions opts = new Page.ScreenshotOptions();
                if (config.isWriteToFile()) {
                    File out = new File(config.getTmpDir(), rendeId + File.separator + fileName);
                    log.debug("Capturing screenshot file {} ({})", out.getAbsolutePath(), rendeId);
                    opts.setPath(out.toPath());
                    byte[] buffer = page.screenshot(opts);
                    pageBO.setPath(out.getAbsolutePath());
                    pageBO.setFileSize((long) buffer.length);
                } else {
                    byte[] buffer = page.screenshot(opts);
                    pageBO.setBuffer(buffer);
                    pageBO.setFileSize((long) buffer.length);
                }
            } catch (Exception e) {
                pageBO.setRenderState(RenderState.FAIL);
                pageBO.setRenderFailedReason("页面截图失败: " + e.getMessage());
                log.error("Screenshot callback error", e);
            }
            return pageBO;
        };
    }
}