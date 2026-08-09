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
package io.github.easy4j.playwright.pdf.bo;

import lombok.Builder;
import lombok.Data;

/**
 * PDF metadata used to populate {@code PDDocumentInformation}.
 *
 * <p>Replaces the original {@code WkhtmlRenderBO}-based signature in
 * ddd4j-cloud-cmpt-playwright's {@code PdfUtil#information(WkhtmlRenderBO)}
 * so the PDF module can be used without the render BO class.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Data
@Builder
/**
 * Data class holding PDF metadata including title, author, and creation date.
 */
public class PdfMeta {

    private String author;
    private String keywords;
    private String subject;
    private String title;
    private String creator;

    /** Producer tag; defaults to a stable string when not set. */
    public String getProducer() {
        return "Playwright PDF Generator";
    }
}