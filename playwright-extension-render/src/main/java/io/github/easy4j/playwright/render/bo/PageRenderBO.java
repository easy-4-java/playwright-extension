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
package io.github.easy4j.playwright.render.bo;

import io.github.easy4j.playwright.render.enums.CheckState;
import io.github.easy4j.playwright.render.enums.RenderState;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-page render state. Domain-specific fields from the original
 * {@code PageRenderBO} (schoolCode, gradeCode, classCode, stuId) are removed;
 * callers needing them should subclass.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class PageRenderBO {

    /** Original position in the render request. */
    private int index;
    /** Caller-supplied unique id; used as the state-store sub-key. */
    private String uniqueId;
    /** Optional background image URL for similarity check. */
    private String bgUrl;
    /** Target URL of this page. */
    private String url;
    /** Output file name (e.g. {@code "0.png"}). */
    private String name;
    /** Whether this page should be written to disk. */
    private Boolean toFile;
    /** On-disk output path (when writeToFile = true). */
    private String path;
    /** Captured image / PDF size in bytes. */
    private Long fileSize;
    /** In-memory captured bytes (when writeToFile = false). */
    private byte[] buffer;
    /** Internal: set by event listeners when a tracked resource fails. */
    private boolean needReload;
    /** Internal: whether the current iteration is a reload. */
    private boolean reload;
    /** Current reload timeout (ms); grows on each retry. */
    private Double reloadTimeout;
    /** Per-page quality check state. */
    private CheckState checkState;
    /** Per-page quality check failure reason. */
    private String checkFailedReason;
    /** Per-page render state. */
    private RenderState renderState;
    /** Per-page render failure reason. */
    private String renderFailedReason;
    /** Per-page failed-resource status codes (url → HTTP status). */
    private Map<String, Integer> resourceLoadState = new HashMap<>();
}