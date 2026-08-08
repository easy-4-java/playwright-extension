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

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Render request. Generalised from ddd4j-cloud-cmpt-playwright's
 * {@code WkhtmlRenderBO}: domain-specific fields (schoolCode, gradeCode, ...)
 * are removed — callers can attach extra metadata via {@link #param} or a
 * subclass.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class WkhtmlRenderBO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Render task id (filled by the strategy if blank). */
    private String taskId;
    /** Whether this render is a retry of a previous run. */
    private boolean retry;
    /** Optional source file URL (used as a base PDF for retry fast-path). */
    private String fileUrl;
    /** CSS selector of the element to capture; null = full-page. */
    private String selector;
    /** Image compression quality 1-100 (out-of-range skips compression). */
    private Integer quality;
    /** Whether to compress the resulting PDF. */
    private Boolean compress;
    /** Base64-encoded JSON with extra parameters (e.g. {@code report_urls}). */
    private String param;
    /** Whether to write to disk (true) or return buffer (false). */
    private Boolean toFile;
    /** Per-page render specs. */
    private List<PageRenderBO> urls;
    /** PDF metadata: author / keywords / subject / title / creator. */
    private String author;
    private String keywords;
    private String subject;
    private String title;
    private String creator;
    /** PDF page size (LETTER / LEGAL / A0-A6). */
    private String pageSize;
    /** Whether to render each page concurrently (default true). */
    private boolean async = true;
    /** Whether to continue processing remaining pages when one fails. */
    private boolean skipFail;
    /** Max ratio of any single colour allowed in a screenshot (0-1). */
    private float maxSingleColorPercent = 0.95f;
    /** Max similarity between screenshot and background image (0-1). */
    private float maxSimilarity = 0.75f;
}