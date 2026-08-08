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
package io.github.easy4j.playwright.render.capture;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.extension.pool.BrowserContextPool;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.lifecycle.PageNavigator;
import io.github.easy4j.playwright.task.executor.TaskExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Default {@link CaptureService}. Uses {@link TaskExecutor} for async dispatch
 * and {@link BrowserContextPool} (from playwright-extension-core) for browser
 * context lifecycle.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public class DefaultCaptureService implements CaptureService {

    private final BrowserContextPool pool;
    private final PageNavigator navigator;
    private final BiFunction<Page, PageRenderBO, PageRenderBO> callbackTemplate;

    /**
     * @param pool             browser context pool
     * @param navigator        page lifecycle driver
     * @param callbackTemplate single-page capture callback (screenshot or pdf)
     */
    public DefaultCaptureService(BrowserContextPool pool,
                                   PageNavigator navigator,
                                   BiFunction<Page, PageRenderBO, PageRenderBO> callbackTemplate) {
        this.pool = pool;
        this.navigator = navigator;
        this.callbackTemplate = callbackTemplate;
    }

    @Override
    public List<PageRenderBO> captureAsync(WkhtmlRenderBO renderBO, RenderConfig config, List<PageRenderBO> pages) {
        if (pages == null || pages.isEmpty()) {
            return Collections.emptyList();
        }
        List<CompletableFuture<PageRenderBO>> futures = new ArrayList<>(pages.size());
        List<PageRenderBO> collected = Collections.synchronizedList(new ArrayList<>(pages.size()));
        for (PageRenderBO page : pages) {
            if (page.getUrl() == null || page.getUrl().trim().isEmpty()) {
                page.setRenderState(RenderState.FAIL);
                page.setRenderFailedReason("Url 为空");
                continue;
            }
            page.setRenderState(RenderState.GENERATING);
            // NOTE: we capture callback per page so the pageBO mutation inside the
            // callback targets the right instance.
            BiFunction<Page, PageRenderBO, PageRenderBO> cb = (p, bo) -> callbackTemplate.apply(p, bo);
            CompletableFuture<PageRenderBO> f = taskExecutorFor(config).submit("capture-" + page.getIndex(),
                    () -> captureOne(renderBO, config, page, cb));
            f.whenComplete((result, ex) -> {
                if (ex != null) {
                    page.setRenderState(RenderState.FAIL);
                    page.setRenderFailedReason(ex.getMessage());
                    log.error("Async capture failed for {}", page.getUrl(), ex);
                } else if (result != null && RenderState.SUCCESS.equals(result.getRenderState())) {
                    collected.add(result);
                }
            });
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.warn("Some capture tasks failed: {}", e.getMessage());
        }
        return collected;
    }

    @Override
    public List<PageRenderBO> captureSync(WkhtmlRenderBO renderBO, RenderConfig config, List<PageRenderBO> pages) {
        if (pages == null || pages.isEmpty()) {
            return Collections.emptyList();
        }
        List<PageRenderBO> results = new ArrayList<>(pages.size());
        BrowserContext ctx = null;
        try {
            ctx = pool.borrowObject();
            for (PageRenderBO page : pages) {
                if (page.getUrl() == null || page.getUrl().trim().isEmpty()) {
                    page.setRenderState(RenderState.FAIL);
                    page.setRenderFailedReason("Url 为空");
                    continue;
                }
                page.setRenderState(RenderState.GENERATING);
                PageRenderBO r = captureOneInContext(ctx, renderBO, config, page, callbackTemplate);
                if (r != null && RenderState.SUCCESS.equals(r.getRenderState())) {
                    results.add(r);
                }
            }
        } catch (Exception e) {
            log.error("Sync capture failed", e);
        } finally {
            if (ctx != null) {
                try {
                    pool.returnObject(ctx);
                } catch (Exception ignored) {
                }
            }
        }
        return results;
    }

    /**
     * Hook for sub-classes to supply the async executor. The default uses
     * a "capture" named executor supplied externally.
     */
    protected TaskExecutor taskExecutorFor(RenderConfig config) {
        throw new IllegalStateException(
                "DefaultCaptureService.taskExecutorFor must be overridden when async capture is used; " +
                        "inject a TaskExecutor via constructor or override this method.");
    }

    private PageRenderBO captureOne(WkhtmlRenderBO renderBO, RenderConfig config, PageRenderBO page,
                                      BiFunction<Page, PageRenderBO, PageRenderBO> cb) {
        BrowserContext ctx = null;
        try {
            ctx = pool.borrowObject();
            return captureOneInContext(ctx, renderBO, config, page, cb);
        } catch (Exception e) {
            page.setRenderState(RenderState.FAIL);
            page.setRenderFailedReason(e.getMessage());
            log.error("Capture failed for {}", page.getUrl(), e);
            return page;
        } finally {
            if (ctx != null) {
                try {
                    pool.returnObject(ctx);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private PageRenderBO captureOneInContext(BrowserContext ctx, WkhtmlRenderBO renderBO, RenderConfig config,
                                               PageRenderBO page, BiFunction<Page, PageRenderBO, PageRenderBO> cb) {
        try (Page p = ctx.newPage()) {
            return navigator.navigate(ctx, p, config, page, renderBO.getTaskId(), cb);
        }
    }

    /**
     * Build a {@link DefaultCaptureService} for screenshots.
     */
    public static DefaultCaptureService forScreenshots(BrowserContextPool pool,
                                                          PageNavigator navigator,
                                                          io.github.easy4j.playwright.render.callback.ScreenshotCallbackFactory cbFactory,
                                                          String rendeId,
                                                          RenderConfig config,
                                                          TaskExecutor asyncExecutor) {
        return new DefaultCaptureService(pool, navigator, cbFactory.create(rendeId, config)) {
            @Override
            protected TaskExecutor taskExecutorFor(RenderConfig cfg) {
                return Objects.requireNonNull(asyncExecutor, "asyncExecutor");
            }
        };
    }

    /**
     * Build a {@link DefaultCaptureService} for single-page PDFs.
     */
    public static DefaultCaptureService forPdf(BrowserContextPool pool,
                                                  PageNavigator navigator,
                                                  io.github.easy4j.playwright.render.callback.PdfCallbackFactory cbFactory,
                                                  String rendeId,
                                                  RenderConfig config,
                                                  TaskExecutor asyncExecutor) {
        return new DefaultCaptureService(pool, navigator, cbFactory.create(rendeId, config)) {
            @Override
            protected TaskExecutor taskExecutorFor(RenderConfig cfg) {
                return Objects.requireNonNull(asyncExecutor, "asyncExecutor");
            }
        };
    }
}