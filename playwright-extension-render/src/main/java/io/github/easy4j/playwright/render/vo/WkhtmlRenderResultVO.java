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
package io.github.easy4j.playwright.render.vo;

import io.github.easy4j.playwright.render.bo.PageRenderBO;
import io.github.easy4j.playwright.render.enums.RenderState;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Render result: file path / buffer + per-page detail.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class WkhtmlRenderResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private RenderState renderState;
    private String renderFailedReason;
    private List<PageRenderBO> pages;
    private String fileId;
    private String fileUrl;
    private String filePath;
    private String fileName;
    private byte[] fileBuffer;
    /** Output size in bytes. */
    private Long fileSize;
}