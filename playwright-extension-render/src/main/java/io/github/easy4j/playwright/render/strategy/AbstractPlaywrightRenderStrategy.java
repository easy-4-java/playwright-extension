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
package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.config.TaskIdGenerator;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Slimmed-down base class (~120 lines, down from the original 1012-line
 * {@code AbstractPlaywrightRenderStrategy}).
 *
 * <p>Responsibilities reduced to:
 * <ol>
 *   <li>Task id materialisation</li>
 *   <li>Three-phase template method: {@link #doGenerate}/{@link #doCompress}/{@link #doPacking}</li>
 *   <li>Failure handling + temporary cleanup</li>
 * </ol>
 *
 * <p>Spring / Redis / dynamic-tp dependencies are gone: collaborators are
 * passed in via the constructor by the Spring Boot starter (or a plain-Java
 * assembler).</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractPlaywrightRenderStrategy<B extends WkhtmlRenderBO>
        implements PlaywrightRenderStrategy<B> {

    protected final RenderConfig config;
    protected final TaskIdGenerator taskIdGenerator;

    protected AbstractPlaywrightRenderStrategy(RenderConfig config, TaskIdGenerator taskIdGenerator) {
        this.config = config;
        this.taskIdGenerator = taskIdGenerator;
    }

    @Override
    public final WkhtmlRenderResultVO render(B renderBO) throws Exception {
        if (renderBO.getTaskId() == null || renderBO.getTaskId().isEmpty()) {
            renderBO.setTaskId(taskIdGenerator.next());
        }
        log.info("Render start taskId={} type={}", renderBO.getTaskId(), getRenderType());
        try {
            // Phase 1: capture
            List<PageRenderBO> pages = doGenerate(renderBO);
            // Phase 2: compress (optional)
            pages = doCompress(renderBO, pages);
            // Phase 3: pack into final output
            return doPacking(renderBO, pages);
        } catch (Exception e) {
            log.error("Render failed taskId={}", renderBO.getTaskId(), e);
            return new WkhtmlRenderResultVO()
                    .setRenderState(RenderState.FAIL)
                    .setRenderFailedReason(e.getMessage())
                    .setFileSize(0L);
        }
    }

    /** Phase 1: capture each page (screenshot or per-page PDF). */
    protected abstract List<PageRenderBO> doGenerate(B renderBO) throws Exception;

    /** Phase 2: optional compression / transformation. Default: no-op. */
    protected List<PageRenderBO> doCompress(B renderBO, List<PageRenderBO> pages) {
        return pages;
    }

    /** Phase 3: pack the captured pages into the final output (PDF / ZIP / single image). */
    protected abstract WkhtmlRenderResultVO doPacking(B renderBO, List<PageRenderBO> pages) throws Exception;
}