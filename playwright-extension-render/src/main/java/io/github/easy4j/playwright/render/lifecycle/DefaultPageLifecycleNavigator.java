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
package io.github.easy4j.playwright.render.lifecycle;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.config.RenderConfig;
import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.ResourceType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Default {@link PageNavigator} that mirrors the original
 * {@code loadPageWithCallback} logic from ddd4j-cloud-cmpt-playwright:
 * attach event listeners, navigate, wait for selector, capture, then on
 * failure enter a reload-retry loop with growing timeout.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Slf4j
public class DefaultPageLifecycleNavigator implements PageNavigator {

    public static final String DATA_RENDER_ATTR = "data-render-result";
    public static final String DATA_BACKGROUND_ATTR = "data-background-url";
    public static final String DATA_RENDER_SUCCESS = "success";
    public static final String DATA_RENDER_ERROR = "error";

    @Override
    public PageRenderBO navigate(BrowserContext context,
                                   Page page,
                                   RenderConfig config,
                                   PageRenderBO pageBO,
                                   String rendeId,
                                   BiFunction<Page, PageRenderBO, PageRenderBO> callback) {
        pageBO.setFileSize(0L);
        pageBO.setNeedReload(false);
        pageBO.setReload(false);
        pageBO.setReloadTimeout((double) config.getNavigateTimeoutMs());

        attachListeners(page, pageBO);

        // First navigation
        page.navigate(pageBO.getUrl(),
                new Page.NavigateOptions().setTimeout(config.getNavigateTimeoutMs()));

        waitForSelector(page, config, pageBO, rendeId);
        optionalSleep(config.getLoadWaitDuration(), "load", page);
        captureBackgroundUrl(page, config, pageBO);

        PageRenderBO applied = callback.apply(page, pageBO);
        if (isPresentable(applied, config)) {
            applied.setRenderState(RenderState.SUCCESS);
            return applied;
        }
        pageBO.setNeedReload(true);

        // Reload-retry loop
        AtomicInteger loadRetry = new AtomicInteger(0);
        while (config.isReloadAble()
                && pageBO.isNeedReload()
                && loadRetry.incrementAndGet() < config.getReloadLimit()) {
            try {
                if (pageBO.getReloadTimeout() != null) {
                    pageBO.setReloadTimeout(pageBO.getReloadTimeout() * 1.25);
                }
                pageBO.setNeedReload(false);
                log.debug("Reloading {} attempt={} timeout={}",
                        page.url(), loadRetry.get(), pageBO.getReloadTimeout());
                page.reload(new Page.ReloadOptions()
                        .setTimeout(pageBO.getReloadTimeout().longValue()));
                pageBO.setReload(false);

                waitForSelector(page, config, pageBO, rendeId);
                optionalSleep(config.getReloadWaitDuration(), "reload-wait", page);

                applied = callback.apply(page, pageBO);
                if (isPresentable(applied, config)) {
                    pageBO.setNeedReload(false);
                    applied.setRenderState(RenderState.SUCCESS);
                    return applied;
                }
                pageBO.setNeedReload(true);
            } catch (Exception e) {
                pageBO.setRenderState(RenderState.FAIL);
                pageBO.setRenderFailedReason("页面重新加载失败: " + e.getMessage());
                log.error("Reload failed for {}", page.url(), e);
            }
        }
        return pageBO;
    }

    private void attachListeners(Page page, PageRenderBO pageBO) {
        page.onLoad(p -> log.debug("onLoad url={} reload={}", p.url(), pageBO.isReload()));
        page.onRequest(req -> log.debug("onRequest url={} type={}", req.url(), req.resourceType()));
        page.onRequestFailed(req -> {
            log.warn("onRequestFailed url={} type={} failure={}",
                    req.url(), req.resourceType(), req.failure());
            ResourceType t = ResourceType.getByName(req.resourceType());
            if (t != null && t.isNeedRetry()) {
                pageBO.setNeedReload(true);
            }
        });
        page.onResponse(resp -> {
            Request req = resp.request();
            if (resp.status() != 200 && req != null) {
                ResourceType t = ResourceType.getByName(req.resourceType());
                if (t != null && t.isNeedRecord404()) {
                    Map<String, Integer> state = pageBO.getResourceLoadState();
                    if (state == null) {
                        state = new HashMap<>();
                        pageBO.setResourceLoadState(state);
                    }
                    state.put(resp.url(), resp.status());
                }
            }
        });
        page.onPageError(err -> {
            log.error("onPageError url={} : {}", page.url(), err);
            pageBO.setNeedReload(true);
        });
        page.onCrash(p -> {
            log.error("onCrash url={}", p.url());
            pageBO.setNeedReload(true);
        });
    }

    private void waitForSelector(Page page, RenderConfig config, PageRenderBO pageBO, String rendeId) {
        String sel = config.getWaitForSelector();
        if (sel == null || sel.trim().isEmpty()) {
            return;
        }
        ElementHandle el = null;
        try {
            el = page.waitForSelector(sel,
                    new Page.WaitForSelectorOptions().setTimeout(config.getNavigateTimeoutMs()));
            if (el == null) {
                pageBO.setRenderState(RenderState.FAIL);
                pageBO.setRenderFailedReason("就绪检测元素未找到: " + sel);
                return;
            }
            String attr = el.getAttribute(DATA_RENDER_ATTR);
            if (DATA_RENDER_ERROR.equalsIgnoreCase(attr)) {
                pageBO.setCheckState(CheckState.WEB_CHECK_FAIL);
                pageBO.setCheckFailedReason(el.textContent());
            } else if (DATA_RENDER_SUCCESS.equalsIgnoreCase(attr)) {
                pageBO.setCheckState(CheckState.SUCCESS);
            }
        } finally {
            if (el != null) {
                try {
                    el.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void captureBackgroundUrl(Page page, RenderConfig config, PageRenderBO pageBO) {
        String selector = null;
        // Selector intentionally read from the BO if the caller set one (matches original code)
        // — but RenderConfig is the source of truth here, kept null for now.
        if (selector == null || selector.trim().isEmpty()) {
            return;
        }
        ElementHandle el = null;
        try {
            el = page.querySelector(selector);
            if (el != null) {
                String bg = el.getAttribute(DATA_BACKGROUND_ATTR);
                if (bg != null && !bg.trim().isEmpty()) {
                    pageBO.setBgUrl(bg);
                }
            }
        } catch (Exception e) {
            pageBO.setRenderState(RenderState.FAIL);
            pageBO.setRenderFailedReason("背景图元素获取失败");
        } finally {
            if (el != null) {
                try {
                    el.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void optionalSleep(java.time.Duration d, String label, Page page) {
        if (d == null || d.toMillis() <= 0) {
            return;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(d.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Heuristic presentability check: path or buffer must exist, file size must
     * exceed a non-trivial threshold, and (optionally) user checkers must pass.
     */
    protected boolean isPresentable(PageRenderBO pageBO, RenderConfig config) {
        if ((pageBO.getPath() == null || pageBO.getPath().trim().isEmpty())
                && pageBO.getBuffer() == null) {
            return false;
        }
        if (pageBO.getPath() != null && !pageBO.getPath().trim().isEmpty()) {
            java.io.File f = new java.io.File(pageBO.getPath());
            if (!f.exists()) {
                return false;
            }
        }
        // 1KB threshold: catches accidental zero-byte output.
        if (pageBO.getFileSize() == null || pageBO.getFileSize() < 1024L) {
            return false;
        }
        return true;
    }
}