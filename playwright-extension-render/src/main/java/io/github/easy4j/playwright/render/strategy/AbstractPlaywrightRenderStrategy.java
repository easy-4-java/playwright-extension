package io.github.easy4j.playwright.render.strategy;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Media;
import com.microsoft.playwright.spring.boot.PlaywrightProperties;
import com.microsoft.playwright.extension.pool.BrowserContextPool;
import io.github.easy4j.playwright.render.PlaywrightRenderProperties;
import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.bo.WkhtmlRenderBO;
import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.RenderState;
import io.github.easy4j.playwright.render.enums.ResourceType;
import io.github.easy4j.playwright.render.page.checker.PageScreenshotChecker;
import io.github.easy4j.playwright.render.page.supplier.PagePdfMergeToPdfSupplier;
import io.github.easy4j.playwright.render.page.supplier.PageScreenshotMergeToPdfSupplier;
import io.github.easy4j.playwright.render.redis.BizRedisKey;
import io.github.easy4j.playwright.render.util.ImageUtil;
import io.github.easy4j.playwright.render.util.TimeUtil;
import io.github.easy4j.playwright.render.vo.WkhtmlRenderResultVO;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisOperationTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * 抽象的 Playwright 处理策略
 */
@Slf4j
public abstract class AbstractPlaywrightRenderStrategy<B extends WkhtmlRenderBO> implements PlaywrightRenderStrategy<B>, InitializingBean, ApplicationEventPublisherAware {

    protected static final int MIN_QUALITY = 0;
    protected static final int MAX_QUALITY = 100;

    protected static final String DATE_PATTERN = "yyyyMMddHHmmssS";
    protected static final String REPORT_URLS_PARAM_NAME = "report_urls";
    protected static final String REPORT_PARAM_UNIQUEID_NAME = "uniqueId";
    protected static final String REPORT_PARAM_OUTPAGE_NAME = "outpage";
    protected static final String REPORT_PARAM_PROJECT_ORIGIN_NAME = "projectOrigin";
    protected static final String DATA_RENDER_ATTR  = "data-render-result";
    protected static final String DATA_BACKGROUND_ATTR  = "data-background-url";
    protected static final String DATA_RENDER_SUCCESS  = "success";
    protected static final String DATA_RENDER_ERROR  = "error";
    protected static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    protected static final DateTimeFormatter DIRECTORY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    protected PlaywrightProperties playwrightProperties;
    @Autowired
    protected PlaywrightRenderProperties playwrightRenderProperties;
    @Autowired
    protected BrowserContextPool browserContextPool;
    @Resource
    protected ThreadPoolExecutor dtpToImageExecutor;
    @Resource
    protected ThreadPoolExecutor dtpToImageCompressExecutor;
    @Resource
    protected ThreadPoolExecutor dtpToImageZipExecutor;
    @Resource
    protected ThreadPoolExecutor dtpToPdfExecutor;
    @Resource
    protected ThreadPoolExecutor dtpToPdfMergeExecutor;
    @Getter
    @Autowired
    protected RedisOperationTemplate redisOperation;
    @Getter
    protected List<PageScreenshotChecker> pageScreenshotCheckers;

    protected ApplicationEventPublisher eventPublisher;

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPageScreenshotCheckers(List<PageScreenshotChecker> pageScreenshotCheckers) {
        this.pageScreenshotCheckers = pageScreenshotCheckers;
    }

    @Override
    public WkhtmlRenderResultVO render(B renderBO) throws Exception {
        log.info("=================Playwright 渲染 HTML:开始=================");
        String taskId = StringUtils.isNotBlank(renderBO.getTaskId()) ? renderBO.getTaskId() : IdUtil.getSnowflakeNextIdStr();
        renderBO.setTaskId(taskId);
        StopWatch stopWatch = new StopWatch(taskId);
        WkhtmlRenderResultVO resultBO = null;
        try {
            // 1、执行参数校验
            if(!StringUtils.isNotBlank(renderBO.getParam())){
                throw new PlaywrightException("param is empty");
            }
            String param = Base64.decodeStr(renderBO.getParam(), Charset.defaultCharset());
            JSONObject jsonObject = JSON.parseObject(param);
            List<String> urls = jsonObject.getList(REPORT_URLS_PARAM_NAME, String.class);
            if(CollectionUtils.isEmpty(urls)){
                throw new PlaywrightException("report_urls is empty");
            }

            // 去除空白URL
            urls.removeIf(StringUtils::isBlank);

            // 报告单渲染状态缓存
            String rdsKey = BizRedisKey.RENDER_STATE.getKey(taskId);
            Map<Object, Object> stateMap = redisOperation.hmGet(rdsKey);
            // 网页URL数组
            List<PageRenderBO> tempList = Lists.newArrayList();
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                String urlPrefix = StringUtils.EMPTY;
                UrlBuilder urlBuilder = UrlBuilder.of(url);
                UrlQuery query = urlBuilder.getQuery();
                // 如果配置了域名前缀，则需要将传递过来的URL进行替换出来
                if (!StringUtils.isNotBlank(query.get(REPORT_PARAM_PROJECT_ORIGIN_NAME)) && !StringUtils.isNotBlank(query.get(REPORT_PARAM_OUTPAGE_NAME))){
                    // 如果配置了域名前缀，则使用对应的URL前缀
                    urlPrefix = Objects.toString(playwrightRenderProperties.getUrlPrefix(), StringUtils.EMPTY);
                }
                if(StringUtils.isNotBlank(urlPrefix) && playwrightRenderProperties.isUseUrlPrefix()){
                    urlBuilder = UrlBuilder.of(urlPrefix)
                            .setPath(urlBuilder.getPath())
                            .setQuery(urlBuilder.getQuery())
                            .setFragment(urlBuilder.getFragment());
                }
                PageRenderBO pageRenderBO = new PageRenderBO()
                        .setIndex(i)
                        .setUrl(urlBuilder.build())
                        .setRenderState(RenderState.WAITING);
                UrlQuery urlQuery = urlBuilder.getQuery();
                if(Objects.nonNull(urlQuery)){
                    String uniqueId = Objects.toString(urlQuery.get(REPORT_PARAM_UNIQUEID_NAME), StringUtils.EMPTY);
                    if(StringUtils.isNotBlank(uniqueId)){
                        // 设置唯一ID
                        pageRenderBO.setUniqueId(uniqueId);
                        // 没有缓存则表示是初次渲染或缓存过期了
                        if (Objects.isNull(stateMap)) {
                            stateMap = Maps.newHashMap();
                        }
                        // 如果是重试，则根据渲染状态进行处理实际的逻辑处理
                        if(renderBO.isRetry()){
                            stateMap.put(uniqueId, RenderState.WAITING.name());
                            log.info("重试渲染，需要重新生成，TaskId: {}, url : {}", taskId, pageRenderBO.getUrl());
                            // 如果渲染状态存在，则根据渲染状态进行处理
                            if (stateMap.containsKey(uniqueId)) {
                                String renderState = MapUtils.getString(stateMap, uniqueId, StringUtils.EMPTY);
                                if(StringUtils.isNotBlank(renderState)){
                                    switch (RenderState.valueOf(renderState)) {
                                        case SUCCESS:
                                            log.info("在上次任务中，渲染成功，本次再次生成，TaskId: {}, url : {}", taskId, pageRenderBO.getUrl());
                                            break;
                                        case FAIL:
                                            log.info("在上次任务中，渲染失败，需要重新生成，TaskId: {}, url : {}", taskId, pageRenderBO.getUrl());
                                            break;
                                        case GENERATING:
                                            log.info("在上次任务中，生成超时，需要重新生成，TaskId: {}, url : {}", taskId, pageRenderBO.getUrl());
                                            break;
                                        default:
                                            log.info("在上次任务中，等待超时，需要重新生成，TaskId: {}, url : {}", taskId, pageRenderBO.getUrl());
                                    }
                                }
                            }
                        } else {
                            stateMap.put(uniqueId, RenderState.WAITING.name());
                            log.info("非重试渲染，首次生成，TaskId: {}, url : {}", taskId, pageRenderBO.getUrl());
                        }
                    }
                }
                tempList.add(pageRenderBO);
            }
            renderBO.setUrls(tempList);
            // 有渲染状态缓存时候
            if (MapUtils.isNotEmpty(stateMap)) {
                redisOperation.hmSet(rdsKey, stateMap, Duration.ofDays(2));
            }
            renderBO.setCompress(Objects.nonNull(renderBO.getCompress()) ? renderBO.getCompress() : Boolean.FALSE);
            renderBO.setToFile(Objects.nonNull(renderBO.getToFile()) ? renderBO.getToFile() : Boolean.FALSE);
            // 2、执行内容生成逻辑
            List<PageRenderBO> pageRenders;
            try {
                log.info("=================Playwright 渲染 PDF/Image:开始=================");
                stopWatch.start("Playwright 渲染 PDF/Image");
                pageRenders = this.doGenerate(renderBO);
            } finally {
                stopWatch.stop();
                log.info("=================Playwright 渲染 PDF/Image:结束=================");
            }
            // 2、执行打包内容生成逻辑
            try {
                log.info("=================压缩 Image:开始=================");
                stopWatch.start("Playwright 渲染 PDF/Image");
                pageRenders = this.doCompress(renderBO, pageRenders);
            } finally {
                stopWatch.stop();
                log.info("=================Playwright 渲染 PDF/Image:结束=================");
            }
            // 3、执行打包逻辑
            try {
                log.info("=================PDF/Image文件zip压缩:开始=================");
                stopWatch.start("PDF/Image文件zip压缩");
                resultBO = this.doPacking(renderBO, pageRenders);
                resultBO.setPages(pageRenders);
            } finally {
                stopWatch.stop();
                log.info("=================PDF/Image文件zip压缩:结束=================");
            }
            return resultBO;
        } catch (Exception e) {
            this.afterException(renderBO, resultBO);
            throw e;
        } finally {
            if(stopWatch.isRunning()){
                stopWatch.stop();
            }
            log.info(stopWatch.prettyPrint());
            log.info("=================Playwright 渲染 HTML:结束=================");
        }
    }

    protected abstract List<PageRenderBO> doGenerate(B renderBO) throws IOException;

    protected abstract List<PageRenderBO> doCompress(B renderBO, List<PageRenderBO> urlTemps) throws IOException;

    protected abstract WkhtmlRenderResultVO doPacking(B renderBO, List<PageRenderBO> urlTemps) throws IOException;

    protected void afterException(B renderBO, WkhtmlRenderResultVO resultBO) throws IOException {
        this.cleanTemporary(renderBO, resultBO);
    }

    /**
     * 异步截图
     * @param renderBO 渲染参数 BO
     * @return 截图结果
     */
    protected List<PageRenderBO> captureScreenshotAsync(B renderBO){
        if (CollectionUtils.isEmpty(renderBO.getUrls())) {
            return Lists.newArrayList();
        }
        try {
            // 2、使用CompletableFuture异步处理
            List<CompletableFuture<PageRenderBO>> futureList = new ArrayList<>();
            List<PageRenderBO> tempRtList = new ArrayList<>();
            for (PageRenderBO pageRenderBO : renderBO.getUrls()) {
                // 如果url为空，则跳过
                if (StringUtils.isBlank(pageRenderBO.getUrl())) {
                    pageRenderBO.setRenderState(RenderState.FAIL);
                    pageRenderBO.setRenderFailedReason("页面截图失败，失败原因：Url 为空");
                    continue;
                }
                // 3、异步截图
                CompletableFuture<PageRenderBO> completableFuture = this.captureScreenshotAsync(renderBO.getTaskId(), renderBO.getSelector(), pageRenderBO);
                // 4、异步截图任务执行完成
                completableFuture.whenComplete((pageScreenshot, e) -> {
                    if (Objects.nonNull(e)) {
                        pageRenderBO.setRenderState(RenderState.FAIL);
                        pageRenderBO.setRenderFailedReason(String.format("页面截图失败，失败原因：%s", e.getMessage()));
                        log.error("异步截图任务执行异常，异常信息：", e);
                    } else {
                        log.debug("异步截图任务执行完成，TaskId: {}, url : {}, pageName: {}, fileSize: {}", renderBO.getTaskId(), pageScreenshot.getUrl(), pageScreenshot.getName(),
                                ((pageScreenshot.getFileSize() / 1024)));
                        tempRtList.add(pageScreenshot);
                    }
                });
                futureList.add(completableFuture);
            }
            if (CollectionUtils.isEmpty(futureList)) {
                return Lists.newArrayList();
            }
            // 2、使用CompletableFuture.allOf()方法，等待所有异步线程执行完毕
            log.debug("等待截图异步任务完成，TaskId: {}", renderBO.getTaskId());
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.debug("异步截图任务执行完毕，TaskId: {}", renderBO.getTaskId());
            return tempRtList;
        } catch (Exception e) {
            log.error("异步截图任务执行异常", e);
            if(e instanceof PlaywrightException){
                throw ExceptionUtils.throwableOfType(e, PlaywrightException.class);
            }
            throw new PlaywrightException("异步截图任务执行异常", e);
        }
    }

    /**
     * 异步截图
     * @param rendeId 渲染ID
     * @param selector 选择器
     * @param pageRenderBO 截图临时对象
     * @return 截图结果
     */
    protected final CompletableFuture<PageRenderBO> captureScreenshotAsync(String rendeId, String selector, PageRenderBO pageRenderBO){
        return CompletableFuture.supplyAsync(() -> {
            BrowserContext browserContext = null;
            try {
                // 1、获取浏览器上下文
                browserContext = browserContextPool.borrowObject();
                log.info("异步截图任务开始执行...");
                pageRenderBO.setRenderState(RenderState.GENERATING);
                try(Page page = browserContext.newPage()) {
                    // 跳转到url
                    log.debug("Async Capturing Screenshot start for rendeId: {}, selector: {}, url : {}", rendeId, selector, pageRenderBO.getUrl());
                    PageRenderBO pageRenderRt = this.loadPageWithCallback(page, rendeId, selector, pageRenderBO, this.doPageScreenShot(rendeId, selector));
                    log.debug("Async Capturing Screenshot completed for rendeId: {}, selector: {}, url : {}, pageName: {}, fileSize: {}", rendeId, selector, pageRenderRt.getUrl(),
                            pageRenderRt.getName(), ((pageRenderRt.getFileSize() / 1024)));
                    return pageRenderRt;
                }
            } catch (Exception e) {
                log.error("异步页面截图异常：", e);
                pageRenderBO.setRenderState(RenderState.FAIL);
                pageRenderBO.setRenderFailedReason(String.format("页面截图失败，失败原因：%s", e.getMessage()));
                return pageRenderBO;
            } finally {
                if(Objects.nonNull(browserContext)){
                    browserContextPool.returnObject(browserContext);
                }
            }
        }, dtpToImageExecutor);
    }

    /**
     * 同步截图
     * @param renderBO 渲染参数 BO
     * @return 截图结果
     */
    protected List<PageRenderBO> captureScreenshotSync(B renderBO){
        BrowserContext browserContext = null;
        try {
            // 1、获取浏览器上下文
            browserContext = browserContextPool.borrowObject();
            // 2、同步截图
            List<PageRenderBO> tempRtList = new ArrayList<>();
            for (PageRenderBO pageRenderBO : renderBO.getUrls()) {
                // 如果url为空，则跳过
                if (StringUtils.isBlank(pageRenderBO.getUrl())) {
                    pageRenderBO.setRenderState(RenderState.FAIL);
                    pageRenderBO.setRenderFailedReason("页面截图失败，失败原因：Url 为空");
                    continue;
                }
                tempRtList.add(this.captureScreenshotSync(browserContext, renderBO.getTaskId(), renderBO.getSelector(), pageRenderBO));
            }
            return tempRtList;
        } catch (Exception e) {
            log.error("Sync Capture screenshot error: ", e);
            if(e instanceof PlaywrightException){
                throw ExceptionUtils.throwableOfType(e, PlaywrightException.class);
            }
            throw new PlaywrightException("Sync Capture screenshot error", e);
        } finally {
            if(Objects.nonNull(browserContext)){
                browserContextPool.returnObject(browserContext);
            }
        }
    }

    /**
     * 同步截图
     * @param browserContext 浏览器上下文
     * @param rendeId 渲染ID
     * @param selector 选择器
     * @param pageRenderBO 截图临时对象
     * @return 截图结果
     */
    protected PageRenderBO captureScreenshotSync(BrowserContext browserContext, String rendeId, String selector, PageRenderBO pageRenderBO){
        // 获取浏览器Page对象
        try (Page page = browserContext.newPage()) {
            pageRenderBO.setRenderState(RenderState.GENERATING);
            // 跳转到url
            log.debug("Sync Capturing screenshot start for rendeId: {}, selector: {}, url : {}", rendeId, selector, pageRenderBO.getUrl());
            PageRenderBO pageScreenshot = this.loadPageWithCallback(page, rendeId, selector, pageRenderBO, this.doPageScreenShot(rendeId, selector));
            log.debug("Sync Capturing screenshot completed for rendeId: {}, selector: {}, url : {}, pageName: {}, fileSize: {}", rendeId, selector, pageRenderBO.getUrl(),
                    pageScreenshot.getName(), ((pageScreenshot.getFileSize() / 1024)));
            return pageScreenshot;
        } catch (Exception e) {
            pageRenderBO.setRenderState(RenderState.FAIL);
            pageRenderBO.setRenderFailedReason(String.format("页面截图失败，失败原因：%s", e.getMessage()));
            log.error("Sync Capturing screenshot failed for rendeId: {}, selector: {}, url : {}", rendeId, selector, pageRenderBO.getUrl(), e);
            return pageRenderBO;
        }
    }

    /**
     * 判断截图/生成PDF是否可用
     * @param pageRenderBO url信息
     * @return 是否可用
     */
    protected boolean isPresentable(PageRenderBO pageRenderBO){
        // 如果path为空，且buffer为空，则不符合要求
        if(StringUtils.isBlank(pageRenderBO.getPath()) && Objects.isNull(pageRenderBO.getBuffer())){
            return false;
        }
        // 如果path不为空，且文件不存在，则不符合要求
        if(StringUtils.isNotBlank(pageRenderBO.getPath())){
            File screenshotFile = new File(pageRenderBO.getPath());
            if(!screenshotFile.exists()){
                return false;
            }
        }
        // 如果截图文件大小小于指定空白处图片大小，则不符合要求
        if(pageRenderBO.getFileSize() < ImageUtil.WHITE_A4_SIZE){
            log.debug("截图文件大小小于空白 A4 图片的大小，需要重新截图");
            return false;
        }
        // 如果自定义判断方法，则执行自定义判断方法
        if(playwrightRenderProperties.isUseCustomCheck() && !CollectionUtils.isEmpty(pageScreenshotCheckers)){
            return pageScreenshotCheckers.stream().filter(Objects::nonNull)
                    .allMatch(checker -> checker.afterPageScreenShot(pageRenderBO));
        }
        return true;
    }

    /**
     * 加载页面并执行回调函数
     * @param page 浏览器Page对象
     * @param rendeId 渲染ID
     * @param selector 选择器
     * @param pageRenderBO 截图临时对象
     * @param callback 回调函数
     * @return 截图结果
     * @throws Exception 异常信息
     */
    protected PageRenderBO loadPageWithCallback(Page page, String rendeId, String selector, PageRenderBO pageRenderBO, BiFunction<Page, PageRenderBO, PageRenderBO> callback) throws Exception {

        // 默认设置截图文件大小为0
        pageRenderBO.setFileSize(0L);
        // 默认设置不需要重新加载
        pageRenderBO.setNeedReload(false);
        // 默认设置不需要重新加载
        pageRenderBO.setReload(false);
        // 默认设置加载超时时间为页面导航超时时间
        pageRenderBO.setReloadTimeout(playwrightProperties.getPageNavigateOptions().getTimeout());
        // 监听页面加载完成事件
        page.onLoad(page1 -> {
            if(pageRenderBO.isReload()){
                log.debug("Reload page for : {}", page1.url());
            } else {
                log.debug("Load page for : {}", page1.url());
            }
        });
        // 监听页面请求事件
        page.onRequest(request -> {
            if(pageRenderBO.isReload()){
                log.debug("Reload Request url: {}, resource type：{}, method：{}, postData：{}", request.url(), request.resourceType(),
                        request.method(), request.postData());
            } else {
                log.debug("Request url: {}, resource type：{}, method：{}, postData：{}", request.url(), request.resourceType(),
                        request.method(), request.postData());
            }
        });
        // 监听页面请求失败事件
        page.onRequestFailed(request -> {
            if(pageRenderBO.isReload()){
                log.error("Reload Request failed: {}, resource type：{}, reason：{}", request.url(), request.resourceType(), request.failure());
            } else {
                log.error("Request failed: {}, resource type：{}, reason：{}", request.url(), request.resourceType(), request.failure());
            }
            // 渲染引擎所感知的请求的资源类型。ResourceType将是以下之一： document, stylesheet, image, media, font, script, texttrack, xhr, fetch, eventsource, websocket, manifest, other.
            ResourceType resourceType = ResourceType.getByName(request.resourceType());
            if (Objects.nonNull(resourceType) && resourceType.isNeedRetry()) {
                pageRenderBO.setNeedReload(true);
                log.debug("Page need reload for url : {}", page.url());
            }
        });
        // 监听页面响应事件
        page.onResponse(response -> {
            Request request = response.request();
            // 记录图片资源加载状态
            if (response.status() != 200 && Objects.nonNull(request) && Objects.requireNonNull(ResourceType.getByName(request.resourceType())).isNeedRecord404()) {
                if(Objects.isNull(pageRenderBO.getResourceLoadState())){
                    pageRenderBO.setResourceLoadState(Maps.newHashMap());
                }
                pageRenderBO.getResourceLoadState().put(response.url(), response.status());
            }
            if(pageRenderBO.isReload()){
                log.debug("Reload Response url: {}, status：{}, headers：{}", response.url(), response.status(), response.headers());
            } else {
                log.debug("Response url: {}, status：{}, headers：{}", response.url(), response.status(), response.headers());
            }
        });
        // 监听页面错误事件
        page.onPageError(exception -> {
            log.error("page error: {}", exception);
            pageRenderBO.setNeedReload(true);
            log.debug("page need retry for url : {}", page.url());
        });
        // 监听页面崩溃事件
        page.onCrash(page1 -> {
            log.error("page crash for url : {}", page1.url());
            pageRenderBO.setNeedReload(true);
            log.debug("page crash and need reload for url : {}", page1.url());
        });
        // 设置页面加载参数, 并跳转到url
        Page.NavigateOptions navigateOptions = playwrightProperties.getPageNavigateOptions().toOptions();
        page.navigate(pageRenderBO.getUrl(), navigateOptions);
        // 设置了元素等待选择器，则等待元素加载完成
        String waitForSelector = playwrightRenderProperties.getWaitForSelector();
        if (StringUtils.isNotBlank(waitForSelector)) {
            log.debug("The page waitForSelector for : {}", waitForSelector);
            ElementHandle elementHandle = null;
            try {
                // 等待元素加载完成
                elementHandle = page.waitForSelector(waitForSelector, playwrightProperties.getPageWaitForSelectorOptions().toOptions());
                if(Objects.isNull(elementHandle)){
                    pageRenderBO.setRenderState(RenderState.FAIL);
                    pageRenderBO.setRenderFailedReason(String.format("页面截图失败，前端就绪状态检测元素（%s）未找到", waitForSelector));
                    log.error("The page waitForSelector element is not found for : {}" , waitForSelector);
                    return pageRenderBO;
                }
                // 如果元素加载完成，则获取元素的 data-render-result 属性
                if(DATA_RENDER_ERROR.equalsIgnoreCase(elementHandle.getAttribute(DATA_RENDER_ATTR))){
                    pageRenderBO.setCheckState(CheckState.WEB_CHECK_FAIL);
                    pageRenderBO.setCheckFailedReason(elementHandle.textContent());
                    log.error("Load failed for rendeId: {}, selector: {}, url : {}， Error : {}", rendeId, selector, pageRenderBO.getUrl(), elementHandle.textContent());
                } else if(DATA_RENDER_SUCCESS.equalsIgnoreCase(elementHandle.getAttribute(DATA_RENDER_ATTR))){
                    pageRenderBO.setCheckState(CheckState.SUCCESS);
                }
            } finally {
                try {
                    if(Objects.nonNull(elementHandle)){
                        // ElementHandle 会阻止 DOM 元素进行垃圾回收，除非使用 JSHandle.dispose() 处理该句柄。当其原始框架被导航时，ElementHandles 会被自动处理。
                        elementHandle.dispose();
                    }
                } catch (Exception e) {
                    // ignore
                    // 捕获 ElementHandle.dispose() 异常
                    log.error("ElementHandle dispose error: ", e);
                }
            }
        }
        // 如果设置了加载等待时间，则等待一段时间
        if(playwrightRenderProperties.isLoadWait() && Objects.nonNull(playwrightRenderProperties.getLoadWaitDuration()) && playwrightRenderProperties.getLoadWaitDuration().toMillis() > 0){
            try {
                log.debug("The page load wait {} milliseconds for : {}", playwrightRenderProperties.getLoadWaitDuration().toMillis(), page.url());
                TimeUnit.MILLISECONDS.sleep(playwrightRenderProperties.getLoadWaitDuration().toMillis());
            } catch (InterruptedException e) {
                // ignore
                log.error("Thread was interrupted", e);
            }
            log.debug("The page load wait completed for : {}", page.url());
        }
        log.debug("The page load completed for : {}", page.url());
        // 定位到要截图的元素，找到背景图元素
        if(StringUtils.isNotBlank(selector)){
            ElementHandle elementHandle = null;
            try {
                elementHandle = page.querySelector(selector);
                if(Objects.nonNull(elementHandle)){
                    String backgroundUrl = elementHandle.getAttribute(DATA_BACKGROUND_ATTR);
                    if(StringUtils.isNotBlank(backgroundUrl)){
                        // 获取背景图片
                        log.debug("The page background image is : {}", backgroundUrl);
                        pageRenderBO.setBgUrl(backgroundUrl);
                    }
                }
            } catch (Exception e){
                pageRenderBO.setRenderState(RenderState.FAIL);
                pageRenderBO.setRenderFailedReason("页面截图失败，背景图元素获取失败。");
            } finally {
                try {
                    if(Objects.nonNull(elementHandle)){
                        // ElementHandle 会阻止 DOM 元素进行垃圾回收，除非使用 JSHandle.dispose() 处理该句柄。当其原始框架被导航时，ElementHandles 会被自动处理。
                        elementHandle.dispose();
                    }
                } catch (Exception e) {
                    // 捕获 ElementHandle.dispose() 异常
                    log.error("ElementHandle dispose error: ", e);
                }
            }
        }
        // 执行回调函数（截图、单页生成pdf）
        PageRenderBO applyTemp = callback.apply(page, pageRenderBO);
        // 判断回调处理后的结果是否可用，如果可用则返回，无需在进行重试
        if(this.isPresentable(applyTemp)){
            // 设置渲染状态为成功
            applyTemp.setRenderState(RenderState.SUCCESS);
            return applyTemp;
        } else {
            pageRenderBO.setNeedReload(true);
        }
        // 结果不符合要求，补充重试机制，多次打开页面
        AtomicInteger loadRetry = new AtomicInteger(0);
        while ( playwrightRenderProperties.isReloadAble() && pageRenderBO.isNeedReload() && loadRetry.incrementAndGet() < playwrightRenderProperties.getReloadLimit()) {
            try {
                // 动态调整超时时间
                if(Objects.nonNull(pageRenderBO.getReloadTimeout())){
                    pageRenderBO.setReloadTimeout(TimeUtil.getRetryTimeout(pageRenderBO.getReloadTimeout()));
                }
                // 重置重新加载标识
                pageRenderBO.setNeedReload(false);
                log.debug("The page reloading for : {} , reloadTimes: {}, reloadTimeout: {}", page.url(), loadRetry.get(), pageRenderBO.getReloadTimeout());
                Page.ReloadOptions reloadOptions = new Page.ReloadOptions()
                        .setTimeout(pageRenderBO.getReloadTimeout())
                        .setWaitUntil(playwrightProperties.getPageNavigateOptions().getWaitUntil());
                page.reload(reloadOptions);
                // 设置重新加载状态为false
                pageRenderBO.setReload(false);
                // 设置了元素等待选择器，则等待元素加载完成
                if (StringUtils.isNotBlank(waitForSelector)) {
                    log.debug("The page waitForSelector for : {}", waitForSelector);
                    ElementHandle elementHandle = null;
                    try {
                        elementHandle = page.waitForSelector(waitForSelector, playwrightProperties.getPageWaitForSelectorOptions().toOptions());
                        // 如果元素加载完成，则获取元素的 data-render-result 属性
                        if(DATA_RENDER_ERROR.equalsIgnoreCase(elementHandle.getAttribute(DATA_RENDER_ATTR))){
                            pageRenderBO.setCheckState(CheckState.WEB_CHECK_FAIL);
                            pageRenderBO.setCheckFailedReason(elementHandle.textContent());
                            log.error("Reload failed for renderId: {}, selector: {}, url : {}， Error : {}", rendeId, selector, pageRenderBO.getUrl(), elementHandle.textContent());
                            continue;
                        }else if(DATA_RENDER_SUCCESS.equalsIgnoreCase(elementHandle.getAttribute(DATA_RENDER_ATTR))){
                            pageRenderBO.setCheckState(CheckState.SUCCESS);
                        }
                    } finally {
                        try {
                            if(Objects.nonNull(elementHandle)){
                                // ElementHandle 会阻止 DOM 元素进行垃圾回收，除非使用 JSHandle.dispose() 处理该句柄。当其原始框架被导航时，ElementHandles 会被自动处理。
                                elementHandle.dispose();
                            }
                        } catch (Exception e) {
                            // 捕获 ElementHandle.dispose() 异常
                            log.error("ElementHandle dispose error: ", e);
                        }
                    }
                }
                // 如果是重试，则等待一段时间
                if(playwrightRenderProperties.isReloadWait() && Objects.nonNull(playwrightRenderProperties.getReloadWaitDuration()) && playwrightRenderProperties.getReloadWaitDuration().toMillis() > 0){
                    try {
                        log.debug("The page reload wait {} milliseconds for : {}", playwrightRenderProperties.getReloadWaitDuration().toMillis(), page.url());
                        TimeUnit.MILLISECONDS.sleep(playwrightRenderProperties.getReloadWaitDuration().toMillis());
                    } catch (InterruptedException e) {
                        log.error("Thread was interrupted", e);
                    }
                    log.debug("The page reload wait completed for : {}", page.url());
                }
                log.debug("The page reload completed for : {} , reloadTimes: {}, reloadTimeout: {}", page.url(), loadRetry.get(), pageRenderBO.getReloadTimeout());
                applyTemp = callback.apply(page, pageRenderBO);
                // 判断重新加载，再次处理后的结果是否可用，如果可用则返回，无需在进行重试
                if(this.isPresentable(applyTemp)){
                    pageRenderBO.setNeedReload(false);
                    // 设置渲染状态为成功
                    applyTemp.setRenderState(RenderState.SUCCESS);
                    return applyTemp;
                } else {
                    pageRenderBO.setNeedReload(true);
                }
            } catch (Exception e){
                pageRenderBO.setRenderState(RenderState.FAIL);
                pageRenderBO.setRenderFailedReason(String.format("页面重新加载失败，失败原因：%s", e.getMessage()));
                log.error("The page screamsot error: ", e);
            }
        }
        return pageRenderBO;
    }

    /**
     * 执行页面截图动作
     * @param rendeId 渲染ID
     * @param selector 元素选择权
     * @return 截图结果
     */
    protected BiFunction<Page, PageRenderBO, PageRenderBO> doPageScreenShot(String rendeId, String selector){
        return (page, pageRenderBO) -> {
            // 定义截图输出路径
            String fileName = String.format("%s.png", pageRenderBO.getIndex());
            pageRenderBO.setName(fileName);
            pageRenderBO.setFileSize(0L);
            try {
                // 截图
                if(StringUtils.isEmpty(selector)){
                    Page.ScreenshotOptions screenshotOptions = playwrightProperties.getPageScreenshotOptions().toOptions();
                    if(playwrightRenderProperties.isWriteToFile()){
                        File screenshotFile = new File(playwrightRenderProperties.getTmpDir(), rendeId + File.separator + fileName);
                        log.info("准备第{}页截图，renderId:{}, renderType : {}, to path: {}", pageRenderBO.getIndex(), rendeId, getRenderType(), screenshotFile.getAbsolutePath());
                        screenshotOptions.setPath(screenshotFile.toPath());
                        page.screenshot(screenshotOptions);
                        pageRenderBO.setPath(screenshotFile.getAbsolutePath());
                        pageRenderBO.setFileSize((screenshotFile.length() / 1024L));
                        log.debug("第{}页截图成功，并保存到本地目录，renderId:{},, renderType : {}, to path: {}, fileSize: {}", pageRenderBO.getIndex(), rendeId, getRenderType(), screenshotFile.getAbsolutePath(),
                                ((pageRenderBO.getFileSize() / 1024)));
                    } else {
                        log.debug("准备第{}页截图，rendeId : {}, renderType : {}, to buffer", pageRenderBO.getIndex(), rendeId, getRenderType());
                        byte[] screenshotBuffer = page.screenshot(screenshotOptions);
                        pageRenderBO.setBuffer(screenshotBuffer);
                        pageRenderBO.setFileSize(((long)(screenshotBuffer.length / 1024L)));
                        log.debug("第{}页截图成功，并保存到内存中，rendeId : {}, renderType : {}, to buffer, fileSize: {}", pageRenderBO.getIndex(), rendeId, getRenderType(),
                                ((pageRenderBO.getFileSize() / 1024)));
                    }
                } else {
                    ElementHandle elementHandle = null;
                    try {
                        // 定位到要截图的元素
                        elementHandle = page.querySelector(selector);
                        if (Objects.nonNull(elementHandle)) {
                           /* if (elementHandle.isVisible()) {
                                // 滚动到元素位置
                                elementHandle.scrollIntoViewIfNeeded();
                            }*/
                            ElementHandle.ScreenshotOptions screenshotOptions = playwrightProperties.getElementScreenshotOptions().toOptions();
                            if (playwrightRenderProperties.isWriteToFile()) {
                                File screenshotFile = new File(playwrightRenderProperties.getTmpDir(), rendeId + File.separator + fileName);
                                log.debug("准备第{}页截图，rendeId : {}, renderType : {}, with selector : {}, to path: {}", pageRenderBO.getIndex(), rendeId, getRenderType(), selector, screenshotFile.getAbsolutePath());
                                screenshotOptions.setPath(screenshotFile.toPath());
                                elementHandle.screenshot(screenshotOptions);
                                pageRenderBO.setPath(screenshotFile.getAbsolutePath());
                                pageRenderBO.setFileSize((screenshotFile.length() / 1024L));
                                log.debug("第{}页截图成功，并保存到本地目录，renderId : {}, renderType : {}, with selector: {}, to path: {}, fileSize: {}", pageRenderBO.getIndex(), rendeId, getRenderType(), selector,
                                        screenshotFile.getAbsolutePath(), ((pageRenderBO.getFileSize() / 1024)));
                            } else {
                                log.debug("准备第{}页截图，rendeId : {}, renderType : {}, with selector : {}, to buffer", pageRenderBO.getIndex(), rendeId, getRenderType(), selector);
                                byte[] screenshotBuffer = elementHandle.screenshot(screenshotOptions);
                                pageRenderBO.setBuffer(screenshotBuffer);
                                pageRenderBO.setFileSize(((long)(screenshotBuffer.length / 1024L)));
                                log.debug("第{}页截图成功，并保存到内存中，rendeId : {}, renderType : {}, with selector: {}, to buffer, fileSize: {}", pageRenderBO.getIndex(), rendeId, getRenderType(), selector,
                                        ((pageRenderBO.getFileSize() / 1024)));
                            }
                        } else {
                            log.error("element not found for selector: {}, url : {}", selector, pageRenderBO.getUrl());
                            pageRenderBO.setRenderState(RenderState.FAIL);
                            pageRenderBO.setRenderFailedReason(String.format("页面截图失败，失败原因：未找到要截图的元素（%s）", selector));
                        }
                    } catch (Exception e) {
                        pageRenderBO.setRenderState(RenderState.FAIL);
                        pageRenderBO.setRenderFailedReason(String.format("页面截图失败，失败原因：%s", e.getMessage()));
                    } finally {
                        try {
                            if(Objects.nonNull(elementHandle)){
                                // ElementHandle 会阻止 DOM 元素进行垃圾回收，除非使用 JSHandle.dispose() 处理该句柄。当其原始框架被导航时，ElementHandles 会被自动处理。
                                elementHandle.dispose();
                            }
                        } catch (Exception e) {
                            // 捕获 ElementHandle.dispose() 异常
                            log.error("ElementHandle dispose error: ", e);
                        }
                    }
                }
                return pageRenderBO;
            } catch (Exception e){
                if(e instanceof PlaywrightException){
                    throw ExceptionUtils.throwableOfType(e, PlaywrightException.class);
                }
                throw new PlaywrightException("Generate PDF error:", e);
            }
        };
    }

    /**
     * 页面异步保存为 PDF
     * @param renderBO 渲染参数 BO
     * @return PDF
     */
    protected List<PageRenderBO> pageToPdfFutureAsync(B renderBO){
        if (CollectionUtils.isEmpty(renderBO.getUrls())) {
            return Lists.newArrayList();
        }
        BrowserContext browserContext = null;
        try {
            // 1、获取浏览器上下文
            browserContext = browserContextPool.borrowObject();
            // 2、使用CompletableFuture异步处理
            List<CompletableFuture<PageRenderBO>> futureList = new ArrayList<>();
            for (PageRenderBO pageRenderBO : renderBO.getUrls()) {
                // 如果url为空，则跳过
                if (StringUtils.isBlank(pageRenderBO.getUrl())) {
                    pageRenderBO.setRenderState(RenderState.FAIL);
                    pageRenderBO.setRenderFailedReason("页面异步保存为 PDF失败，失败原因：Url 为空");
                    continue;
                }
                // 3、异步截图
                CompletableFuture<PageRenderBO> completableFuture = this.pageToPdfFutureAsync(browserContext, renderBO.getTaskId(), renderBO.getSelector(), pageRenderBO);
                // 4、异步截图任务执行完成
                completableFuture.whenComplete((pageScreenshot, e) -> {
                    if (Objects.nonNull(e)) {
                        pageScreenshot.setRenderState(RenderState.FAIL);
                        pageScreenshot.setRenderFailedReason(String.format("页面异步保存为 PDF失败，失败原因：%s", e.getMessage()));
                        log.error("页面异步保存为 PDF 任务执行异常，异常信息：", e);
                    } else {
                        log.debug("页面异步保存为 PDF 任务执行完成，TaskId: {}, url : {}, pageName: {}, fileSize: {}", renderBO.getTaskId(), pageScreenshot.getUrl(), pageScreenshot.getName(),
                                ((pageScreenshot.getFileSize() / 1024)));
                    }
                });
                futureList.add(completableFuture);
            }
            if (CollectionUtils.isEmpty(futureList)) {
                return Lists.newArrayList();
            }
            // 2、使用CompletableFuture.allOf()方法，等待所有异步线程执行完毕
            log.debug("等待页面保存为 PDF 异步任务完成，TaskId: {}", renderBO.getTaskId());
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.debug("页面异步保存为 PDF 任务执行完毕，TaskId: {}", renderBO.getTaskId());
            return renderBO.getUrls();
        } catch (Exception e) {
            log.error("页面异步保存为 PDF 异常: ", e);
            if(e instanceof PlaywrightException){
                throw ExceptionUtils.throwableOfType(e, PlaywrightException.class);
            }
            throw new PlaywrightException("页面异步保存为 PDF 异常", e);
        } finally {
            if(Objects.nonNull(browserContext)){
                browserContextPool.returnObject(browserContext);
            }
        }
    }

    protected final CompletableFuture<PageRenderBO> pageToPdfFutureAsync(BrowserContext browserContext, String rendeId, String selector, PageRenderBO pageRenderBO) {
        // 如果url为空，则直接返回
        if(StringUtils.isBlank(pageRenderBO.getUrl())){
            return CompletableFuture.completedFuture(pageRenderBO);
        }
        //HttpServletRequest request = WebUtils.getHttpServletRequest();
        // 1、使用CompletableFuture.supplyAsync()方法，异步执行截图
        return CompletableFuture.supplyAsync(() -> {
            pageRenderBO.setRenderState(RenderState.GENERATING);
            try(Page page = browserContext.newPage()) {
                log.debug("Async Generate pdf start for rendeId: {}, url : {}", rendeId, pageRenderBO.getUrl());
                PageRenderBO pageToPdf = this.loadPageWithCallback(page, rendeId, selector, pageRenderBO, this.doPageToPdf(rendeId));
                log.debug("Async Generate pdf completed for rendeId: {}, url : {}, pageName: {}, fileSize: {}", rendeId, pageRenderBO.getUrl(), pageToPdf.getName(),
                        ((pageToPdf.getFileSize() / 1024)));
                return pageToPdf;
            } catch (Exception e) {
                pageRenderBO.setRenderState(RenderState.FAIL);
                pageRenderBO.setRenderFailedReason(String.format("页面截图失败，失败原因：%s", e.getMessage()));
                log.error("Async Generate pdf error: ", e);
                return pageRenderBO;
            }
        }, dtpToPdfExecutor);
    }

    /**
     * 同步页面保存为PDF
     * @param renderBO 渲染参数 BO
     * @return 保存的结果
     */
    protected List<PageRenderBO> pageToPdfFutureSync(B renderBO){
        BrowserContext browserContext = null;
        try {
            // 1、获取浏览器上下文
            browserContext = browserContextPool.borrowObject();
            // 2、同步截图
            List<PageRenderBO> tempRtList = new ArrayList<>();
            for (PageRenderBO pageRenderBO : renderBO.getUrls()) {
                // 如果url为空，则跳过
                if (StringUtils.isBlank(pageRenderBO.getUrl())) {
                    pageRenderBO.setRenderState(RenderState.FAIL);
                    pageRenderBO.setRenderFailedReason("页面保存为PDF失败，失败原因：Url 为空");
                    continue;
                }
                tempRtList.add(this.pageToPdfFutureSync(browserContext, renderBO.getTaskId(), renderBO.getSelector(), pageRenderBO));
            }
            return tempRtList;
        } catch (Exception e) {
            if(e instanceof PlaywrightException){
                throw ExceptionUtils.throwableOfType(e, PlaywrightException.class);
            }
            throw new PlaywrightException("Sync Generate pdf error", e);
        } finally {
            if(Objects.nonNull(browserContext)){
                browserContextPool.returnObject(browserContext);
            }
        }
    }

    protected PageRenderBO pageToPdfFutureSync(BrowserContext browserContext, String rendeId, String selector, PageRenderBO pageRenderBO) {
        try (Page page = browserContext.newPage()) {
            pageRenderBO.setRenderState(RenderState.GENERATING);
            log.debug("Sync Generate pdf start for rendeId: {}, url : {}", rendeId, pageRenderBO.getUrl());
            PageRenderBO pageToPdf = this.loadPageWithCallback(page, rendeId, selector, pageRenderBO, this.doPageToPdf(rendeId));
            log.debug("Sync Generate pdf completed for rendeId: {}, url : {}, pageName: {}, fileSize: {}", rendeId, pageRenderBO.getUrl(), pageToPdf.getName(),
                    ((pageToPdf.getFileSize() / 1024)));
            return pageToPdf;
        } catch (Exception e) {
            pageRenderBO.setRenderState(RenderState.FAIL);
            pageRenderBO.setRenderFailedReason(String.format("页面保存为Pdf失败，失败原因：%s", e.getMessage()));
            log.error("Sync Generate pdf error: ", e);
            return pageRenderBO;
        }
    }

    protected BiFunction<Page, PageRenderBO, PageRenderBO> doPageToPdf(String rendeId) {
        return (page, pageRenderBO) -> {
            try {
                // 定义截图输出路径
                String fileName = String.format("%s.pdf", pageRenderBO.getIndex());
                pageRenderBO.setName(fileName);
                page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
                if(playwrightRenderProperties.isWriteToFile()){
                    File pdfFile = new File(playwrightRenderProperties.getTmpDir(), rendeId + File.separator + fileName);
                    log.debug("Generate pdf file start for rendeId : {}, renderType : {}, to path: {}", rendeId, getRenderType(), pdfFile.getAbsolutePath());
                    Page.PdfOptions pdfOptions = playwrightProperties.getPagePdfOptions().toOptions();
                    pdfOptions.setPath(pdfFile.toPath());
                    page.pdf(pdfOptions);
                    pageRenderBO.setPath(pdfFile.getAbsolutePath());
                    pageRenderBO.setFileSize((pdfFile.length() / 1024L));
                    log.debug("Generate pdf file success for rendeId : {}, renderType : {}, to path: {}, fileSize: {}", rendeId, getRenderType(), pdfFile.getAbsolutePath(),
                            ((pageRenderBO.getFileSize() / 1024)));
                } else {
                    log.debug("Generate pdf buffer start for rendeId : {}, renderType : {}", rendeId, getRenderType());
                    // 生成PDF
                    Page.PdfOptions pdfOptions = playwrightProperties.getPagePdfOptions().toOptions();
                    byte[] pdfBuffer = page.pdf(pdfOptions);
                    pageRenderBO.setBuffer(pdfBuffer);
                    pageRenderBO.setFileSize(((long)(pdfBuffer.length / 1024L)));
                    log.debug("Generate pdf buffer success for rendeId : {}, renderType : {}, fileSize: {}", rendeId, getRenderType(),
                            ((pageRenderBO.getFileSize() / 1024)));
                }
                return pageRenderBO;
            } catch (Exception e) {
                pageRenderBO.setRenderState(RenderState.FAIL);
                pageRenderBO.setRenderFailedReason(String.format("页面保存为Pdf失败，失败原因：%s", e.getMessage()));
                log.error("Generate pdf error: ", e);
                return pageRenderBO;
            }
        };
    }

    /**
     * 定义一个图片合并为PDF方法
     * @param renderBO 渲染参数 BO
     * @param screenshots 截图列表
     * @param biFunction 回调函数
     * @return PDF
     */
    protected CompletableFuture<WkhtmlRenderResultVO> mergeScreenshotsToPDF(WkhtmlRenderBO renderBO,
                                                                    List<PageRenderBO> screenshots,
                                                                    BiFunction<PDDocument, List<PageRenderBO>, WkhtmlRenderResultVO> biFunction) {
        return CompletableFuture.supplyAsync(new PageScreenshotMergeToPdfSupplier(playwrightRenderProperties, renderBO, screenshots, pageScreenshotCheckers, biFunction, redisOperation), dtpToPdfMergeExecutor);
    }


    /**
     * 定义一个图片合并为PDF方法
     * @param renderBO 渲染参数
     * @param pdfs Pdf 列表
     * @param biFunction 回调函数
     */
    protected CompletableFuture<WkhtmlRenderResultVO> mergePdfsToPDF(WkhtmlRenderBO renderBO,
                                                             List<PageRenderBO> pdfs,
                                                             BiFunction<PDFMergerUtility, List<PageRenderBO>, WkhtmlRenderResultVO> biFunction) {
        return CompletableFuture.supplyAsync(new PagePdfMergeToPdfSupplier(playwrightRenderProperties, renderBO, pdfs, pageScreenshotCheckers, biFunction), dtpToPdfMergeExecutor);
    }

    @Override
    public void cleanTemporary(B renderBO, WkhtmlRenderResultVO resultBO) {
        log.debug("clean Temporary");
        try {
            if(StringUtils.isNotBlank(renderBO.getTaskId())){
                File fileDirectory = new File(playwrightRenderProperties.getTmpDir(), renderBO.getTaskId());
                if(fileDirectory.exists()){
                    log.debug("delete Temporary Directory  : {}" , fileDirectory.getAbsolutePath());
                    FileUtils.deleteDirectory(fileDirectory);
                }
            }
            if(Objects.nonNull(resultBO) && StringUtils.isNotBlank(resultBO.getFilePath())){
                File rtFile = new File(resultBO.getFilePath());
                log.debug("delete Temporary File  : {}" , rtFile.getAbsolutePath());
                if(rtFile.exists()){
                    Files.delete(rtFile.toPath());
                }
            }
        } catch (Exception e){
            log.error("Failed to delete file", e);
        }
    }

    protected ApplicationEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected Sequence getSequence() {
        return sequence;
    }


}
