package com.microsoft.playwright.extension.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PlaywrightException}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class PlaywrightExceptionTest {

    @Test
    void constructorShouldSetMessage() {
        PlaywrightException ex = new PlaywrightException("browser error");
        assertThat(ex.getMessage()).isEqualTo("browser error");
    }

    @Test
    void constructorShouldSetMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        PlaywrightException ex = new PlaywrightException("browser error", cause);
        assertThat(ex.getMessage()).isEqualTo("browser error");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
