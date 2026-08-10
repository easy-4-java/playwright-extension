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
package io.ddd4j.boot.core.sequence;

/**
 * Sequence-id generator shim.
 *
 * <p>Lifted into the render module so the migrated code keeps compiling
 * without the original {@code io.ddd4j.boot:ddd4j-boot-core} dependency.
 * The starter (or any user code) supplies a concrete bean — typically a
 * snowflake / UUID / DB-sequence implementation — and the original
 * {@code @Resource Sequence sequence} injection site in
 * {@code AbstractPlaywrightRenderStrategy} picks it up unchanged.</p>
 *
 * <p>The package name matches the original so existing user beans that
 * implement {@code io.ddd4j.boot.core.sequence.Sequence} keep working.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface Sequence {
    long nextId();
}