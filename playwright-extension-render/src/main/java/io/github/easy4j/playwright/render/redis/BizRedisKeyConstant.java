package io.github.easy4j.playwright.render.redis;

/**
 * Constants for Redis key prefixes used in business caching.
 */
public abstract class BizRedisKeyConstant {
    /**
     * redis 报告单渲染状态 缓存Key前缀
     */
    public final static String RENDER_STATE_KEY = "pdf-render:state";

}
