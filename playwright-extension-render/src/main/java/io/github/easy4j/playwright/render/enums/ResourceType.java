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
package io.github.easy4j.playwright.render.enums;

/**
 * Resource types reported by Playwright's request/response events.
 * Used by {@code PageLifecycleNavigator} to decide whether a failed resource
 * should trigger a page reload.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public enum ResourceType {
    document, stylesheet, image, media, font, script, texttrack, xhr, fetch, eventsource, websocket, manifest, other;

    public static ResourceType getByName(String name) {
        if (name == null) {
            return other;
        }
        for (ResourceType value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return other;
    }

    public boolean isNeedRetry() {
        return this == stylesheet || this == script || this == fetch || this == xhr;
    }

    public boolean isNeedRecord404() {
        return this == stylesheet || this == image || this == media || this == font
                || this == script || this == xhr || this == fetch;
    }
}