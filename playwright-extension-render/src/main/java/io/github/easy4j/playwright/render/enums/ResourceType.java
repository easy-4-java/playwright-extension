package io.github.easy4j.playwright.render.enums;

/**
 * 渲染引擎所感知的请求的资源类型。ResourceType将是以下之一：
 * document, stylesheet, image, media, font, script, texttrack, xhr, fetch, eventsource, websocket, manifest, other.
 */
public enum ResourceType {

    document, stylesheet, image, media, font, script, texttrack, xhr, fetch, eventsource, websocket, manifest, other;

    public static ResourceType getByName(String name) {
        for (ResourceType value : ResourceType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return other;
    }

    public boolean isNeedRetry() {
        return this == ResourceType.stylesheet || this == ResourceType.script || this == ResourceType.fetch || this == ResourceType.xhr;
    }

    public boolean isNeedRecord404() {
        return this == ResourceType.stylesheet || this == ResourceType.image || this == ResourceType.media || this == ResourceType.font ||
                this == ResourceType.script|| this == ResourceType.xhr|| this == ResourceType.fetch;
    }

}
