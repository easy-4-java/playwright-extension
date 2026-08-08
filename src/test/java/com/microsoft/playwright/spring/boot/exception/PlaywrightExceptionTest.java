package com.microsoft.playwright.spring.boot.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaywrightExceptionTest {

    @Test
    void shouldCreateExceptionWithErrorCode() {
        PlaywrightException ex = new PlaywrightException("ERR_001");
        assertEquals("ERR_001", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        PlaywrightException ex = new PlaywrightException("ERR_001", cause);
        assertEquals("ERR_001", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndDescription() {
        PlaywrightException ex = new PlaywrightException("ERR_001", "something went wrong");
        assertEquals("ERR_001:something went wrong", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldCreateExceptionWithErrorCodeDescriptionAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        PlaywrightException ex = new PlaywrightException("ERR_001", "something went wrong", cause);
        assertEquals("ERR_001:something went wrong", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldCreateExceptionWithCauseOnly() {
        RuntimeException cause = new RuntimeException("root cause");
        PlaywrightException ex = new PlaywrightException(cause);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("root cause"));
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        PlaywrightException ex = new PlaywrightException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void shouldPreserveSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        var field = PlaywrightException.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        assertEquals(-7545341502620139031L, field.getLong(null));
    }
}
