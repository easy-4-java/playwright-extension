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
package io.github.easy4j.playwright.render.exception;

/**
 * Thrown when a render task fails irrecoverably (e.g. screenshot capture
 * error, PDF merge failure).
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class TaskRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TaskRuntimeException(String message) {
        super(message);
    }

    public TaskRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}