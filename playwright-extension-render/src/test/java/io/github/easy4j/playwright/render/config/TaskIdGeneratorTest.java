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
package io.github.easy4j.playwright.render.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link TaskIdGenerators}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
class TaskIdGeneratorTest {

    @Test
    @DisplayName("sequential() produces monotonically increasing string ids")
    void sequential_increases() {
        TaskIdGenerator gen = TaskIdGenerators.sequential();
        String a = gen.next();
        String b = gen.next();
        String c = gen.next();
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);
        assertNotEquals(a, b);
        assertNotEquals(b, c);
        // All parseable as long
        Long.parseLong(a);
        Long.parseLong(b);
        Long.parseLong(c);
    }

    @Test
    @DisplayName("uuid() produces 32-char hex strings")
    void uuid_isHex32() {
        TaskIdGenerator gen = TaskIdGenerators.uuid();
        for (int i = 0; i < 5; i++) {
            String id = gen.next();
            assertThat(id).hasSize(32).matches("[0-9a-f]+");
        }
    }
}