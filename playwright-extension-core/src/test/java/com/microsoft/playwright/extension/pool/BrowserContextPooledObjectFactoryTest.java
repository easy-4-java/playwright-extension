package com.microsoft.playwright.extension.pool;

import com.microsoft.playwright.*;
import com.microsoft.playwright.extension.PlaywrightBrowserType;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BrowserContextPooledObjectFactoryTest {

    @Mock
    private BrowserContext browserContext;

    @Mock
    private Browser browser;

    @Mock
    private Page page;

    @Test
    void shouldCreateFactoryWithLaunchOptions() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, contextOptions);
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithNullBrowserType() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                null, launchOptions, contextOptions);
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithNullLaunchOptions() {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, null, contextOptions);
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithNullContextOptions() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, null);
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithPersistentOptions() {
        BrowserType.LaunchPersistentContextOptions persistentOptions = new BrowserType.LaunchPersistentContextOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, persistentOptions, "/tmp/userData");
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithNullPersistentOptions() {
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, (BrowserType.LaunchPersistentContextOptions) null, "/tmp/userData");
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithEmptyUserDataRootDir() {
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, (BrowserType.LaunchPersistentContextOptions) null, "");
        assertNotNull(factory);
    }

    @Test
    void shouldCreateFactoryWithNullUserDataRootDir() {
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, (BrowserType.LaunchPersistentContextOptions) null, null);
        assertNotNull(factory);
    }

    @Test
    void shouldActivateObject() throws Exception {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(browserContext);
        factory.activateObject(pooledObject);
        verify(browserContext).clearCookies();
    }

    @Test
    void shouldActivateObjectWithNullContext() throws Exception {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(null);
        assertDoesNotThrow(() -> factory.activateObject(pooledObject));
    }

    @Test
    void shouldDestroyObjectWithNullContext() throws Exception {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(null);
        assertDoesNotThrow(() -> factory.destroyObject(pooledObject));
    }

    @Test
    void shouldPassivateObject() throws Exception {
        when(browserContext.pages()).thenReturn(Collections.emptyList());
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(browserContext);
        factory.passivateObject(pooledObject);
        verify(browserContext).clearCookies();
        verify(browserContext).pages();
    }

    @Test
    void shouldPassivateObjectWithNullContext() throws Exception {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(null);
        assertDoesNotThrow(() -> factory.passivateObject(pooledObject));
    }

    @Test
    void shouldPassivateObjectWithPages() throws Exception {
        when(page.isClosed()).thenReturn(false);
        when(browserContext.pages()).thenReturn(java.util.Collections.singletonList(page));
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(browserContext);
        factory.passivateObject(pooledObject);
        verify(browserContext).clearCookies();
    }

    @Test
    void shouldValidateObjectWithLaunchOptionsAndConnected() {
        when(browserContext.browser()).thenReturn(browser);
        when(browser.isConnected()).thenReturn(true);
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(browserContext);
        assertTrue(factory.validateObject(pooledObject));
    }

    @Test
    void shouldValidateObjectWithLaunchOptionsAndDisconnected() {
        when(browserContext.browser()).thenReturn(browser);
        when(browser.isConnected()).thenReturn(false);
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(browserContext);
        assertFalse(factory.validateObject(pooledObject));
    }

    @Test
    void shouldValidateObjectWithNullLaunchOptions() {
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, (BrowserType.LaunchPersistentContextOptions) null, "/tmp");
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(browserContext);
        assertTrue(factory.validateObject(pooledObject));
    }

    @Test
    void shouldValidateObjectWithNullLaunchOptionsAndNullContext() {
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, (BrowserType.LaunchPersistentContextOptions) null, "/tmp");
        DefaultPooledObject<BrowserContext> pooledObject = new DefaultPooledObject<>(null);
        assertFalse(factory.validateObject(pooledObject));
    }

    @Test
    void shouldCleanupBrowserContextWithNullContext() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        assertDoesNotThrow(() -> factory.cleanupBrowserContext(null));
    }

    @Test
    void shouldCleanupBrowserContextWithPages() {
        when(page.isClosed()).thenReturn(true);
        when(browserContext.pages()).thenReturn(java.util.Collections.singletonList(page));
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        assertDoesNotThrow(() -> factory.cleanupBrowserContext(browserContext));
        verify(browserContext).clearCookies();
        verify(browserContext).clearPermissions();
    }

    @Test
    void shouldCleanupBrowserContextWithNoPages() {
        when(browserContext.pages()).thenReturn(Collections.emptyList());
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        assertDoesNotThrow(() -> factory.cleanupBrowserContext(browserContext));
        verify(browserContext).clearCookies();
        verify(browserContext).clearPermissions();
    }

    @Test
    void shouldImplementAutoCloseable() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        assertInstanceOf(AutoCloseable.class, factory);
    }

    @Test
    void shouldCloseEmptyFactory() throws Exception {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        assertDoesNotThrow(factory::close);
    }
}
