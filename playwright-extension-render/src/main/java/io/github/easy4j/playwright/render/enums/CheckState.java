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

import lombok.Getter;

/**
 * Per-page quality-check outcome.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Getter
public enum CheckState {
    SUCCESS(1, "check passed"),
    WEB_CHECK_FAIL(2, "front-end check failed"),
    IMG_CHECK_FAIL(3, "image check failed");

    private final int state;
    private final String desc;

    CheckState(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static CheckState getByName(int state) {
        for (CheckState cs : values()) {
            if (cs.state == state) {
                return cs;
            }
        }
        return null;
    }
}