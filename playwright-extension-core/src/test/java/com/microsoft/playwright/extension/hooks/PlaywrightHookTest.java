package com.microsoft.playwright.extension.hooks;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.extension.pool.BrowserContextPooledObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaywrightHookTest {

    @Mock
    private BrowserContextPooledObjectFactory factory;

    @Test
    void shouldCreateHookWithFactory() {
        PlaywrightHook hook = new PlaywrightHook(factory, 5000L);
        assertNotNull(hook);
        assertEquals("playwright-shutdown-hook", hook.getName());
    }

    @Test
    void shouldRunWithFactory() throws Exception {
        PlaywrightHook hook = new PlaywrightHook(factory, 5000L);
        hook.run();
        verify(factory).close();
    }

    @Test
    void shouldHandleNullFactoryGracefully() {
        PlaywrightHook hook = new PlaywrightHook(null, 5000L);
        assertDoesNotThrow(hook::run);
    }

    @Test
    void shouldHandleFactoryCloseException() throws Exception {
        doThrow(new RuntimeException("close failed")).when(factory).close();
        PlaywrightHook hook = new PlaywrightHook(factory, 5000L);
        assertDoesNotThrow(hook::run);
    }

    @Test
    void shouldExtendThread() {
        PlaywrightHook hook = new PlaywrightHook(factory, 5000L);
        assertInstanceOf(Thread.class, hook);
    }

    @Test
    void shouldSetNameToPlaywrightShutdownHook() {
        PlaywrightHook hook = new PlaywrightHook(factory, 1000L);
        assertEquals("playwright-shutdown-hook", hook.getName());
    }
}
