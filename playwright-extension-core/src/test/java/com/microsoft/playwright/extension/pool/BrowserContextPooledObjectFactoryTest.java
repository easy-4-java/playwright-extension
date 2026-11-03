package com.microsoft.playwright.extension.pool;

import com.microsoft.playwright.*;
import com.microsoft.playwright.extension.PlaywrightBrowserType;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    void shouldDestroyObjectAndClearContextResources() throws Exception {
        when(browserContext.pages()).thenReturn(Collections.emptyList());
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, new BrowserType.LaunchOptions(), new Browser.NewContextOptions());

        factory.destroyObject(new DefaultPooledObject<>(browserContext));

        verify(browserContext).clearCookies();
        verify(browserContext).clearPermissions();
        verify(browserContext).pages();
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
        when(browserContext.pages()).thenReturn(List.of(page));
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
        when(browserContext.pages()).thenReturn(List.of(page));
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, launchOptions, new Browser.NewContextOptions());
        assertDoesNotThrow(() -> factory.cleanupBrowserContext(browserContext));
        verify(browserContext).clearCookies();
        verify(browserContext).clearPermissions();
    }

    @Test
    void shouldCloseOpenPagesDuringCleanup() {
        when(page.isClosed()).thenReturn(false);
        when(browserContext.pages()).thenReturn(List.of(page));
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, new BrowserType.LaunchOptions(), new Browser.NewContextOptions());

        factory.cleanupBrowserContext(browserContext);

        verify(page).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeletePersistentUserDataDirectoryDuringCleanup(@TempDir Path temporaryDirectory) throws Exception {
        Path userDataDirectory = Files.createDirectory(temporaryDirectory.resolve("browser-profile"));
        Files.writeString(userDataDirectory.resolve("state.txt"), "session data");
        Field field = BrowserContextPooledObjectFactory.class.getDeclaredField("USER_DATA_DIR_MAP");
        field.setAccessible(true);
        Map<BrowserContext, File> directories = (Map<BrowserContext, File>) field.get(null);
        directories.put(browserContext, userDataDirectory.toFile());
        when(browserContext.pages()).thenReturn(Collections.emptyList());
        BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
                PlaywrightBrowserType.chromium, new BrowserType.LaunchOptions(), new Browser.NewContextOptions());

        factory.cleanupBrowserContext(browserContext);

        assertFalse(Files.exists(userDataDirectory));
        assertFalse(directories.containsKey(browserContext));
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
