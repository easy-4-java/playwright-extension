package com.microsoft.playwright.extension.hooks;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.extension.pool.BrowserContextPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaywrightInstallTest {

    @Mock
    private BrowserContextPool browserContextPool;

    @Test
    void shouldCreateInstallWithPoolAndHost() {
        PlaywrightInstall install = new PlaywrightInstall(browserContextPool, "https://registry.npmjs.org");
        assertNotNull(install);
        assertFalse(install.isInstalled());
    }

    @Test
    void shouldReturnFalseBeforeRun() {
        PlaywrightInstall install = new PlaywrightInstall(browserContextPool, "https://registry.npmjs.org");
        assertFalse(install.isInstalled());
    }

    @Test
    void shouldHandleNullPoolGracefully() {
        PlaywrightInstall install = new PlaywrightInstall(null, "https://registry.npmjs.org");
        assertDoesNotThrow(install::run);
        assertFalse(install.isInstalled());
    }

    @Test
    void shouldHandleNullDownloadHostGracefully() {
        PlaywrightInstall install = new PlaywrightInstall(browserContextPool, null);
        assertDoesNotThrow(install::run);
        assertFalse(install.isInstalled());
    }

    @Test
    void shouldHandleBothNullGracefully() {
        PlaywrightInstall install = new PlaywrightInstall(null, null);
        assertDoesNotThrow(install::run);
        assertFalse(install.isInstalled());
    }

    @Test
    void shouldImplementRunnable() {
        PlaywrightInstall install = new PlaywrightInstall(browserContextPool, "https://registry.npmjs.org");
        assertInstanceOf(Runnable.class, install);
    }

    @Test
    void shouldHandleBorrowObjectException() throws Exception {
        when(browserContextPool.borrowObject()).thenThrow(new RuntimeException("borrow failed"));
        PlaywrightInstall install = new PlaywrightInstall(browserContextPool, "https://registry.npmjs.org");
        assertDoesNotThrow(install::run);
        assertFalse(install.isInstalled());
    }
}
