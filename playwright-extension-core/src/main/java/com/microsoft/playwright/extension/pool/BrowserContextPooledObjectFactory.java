package com.microsoft.playwright.extension.pool;

import com.microsoft.playwright.*;
import com.microsoft.playwright.extension.PlaywrightBrowserType;
import com.microsoft.playwright.extension.utils.PlaywrightUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PooledObjectFactory} that creates, validates, activates, passivates, and destroys
 * {@link BrowserContext} instances for use in an object pool. Each pooled browser context is
 * backed by its own {@link Playwright} instance, which is tracked in a static map and closed
 * when the browser context is destroyed or the factory is closed.
 *
 * <p>Supports two modes of operation:
 * <ul>
 *   <li><b>Incognito mode</b>: launches a headless browser and creates isolated contexts via
 *       {@link BrowserType.LaunchOptions} and {@link Browser.NewContextOptions}.</li>
 *   <li><b>Persistent mode</b>: launches a browser with a user data directory via
 *       {@link BrowserType.LaunchPersistentContextOptions}, retaining cookies and storage.</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see BrowserContextPool
 * @see PlaywrightBrowserType
 */
@Slf4j
public class BrowserContextPooledObjectFactory implements PooledObjectFactory<BrowserContext>, AutoCloseable {

    /**
     * Maps each pooled BrowserContext to its owning Playwright instance.
     */
    private static final Map<BrowserContext, Playwright> PLAYWRIGHT_MAP = new ConcurrentHashMap<>();

    /**
     * Maps each persistent BrowserContext to its user data directory for cleanup.
     */
    private static final Map<BrowserContext, File> USER_DATA_DIR_MAP = new ConcurrentHashMap<>();
    /**
     * The browser type to launch (chromium, firefox, or webkit).
     */
    private PlaywrightBrowserType browserType = PlaywrightBrowserType.chromium;
    /**
     * Launch options for incognito (non-persistent) browser mode.
     */
    private BrowserType.LaunchOptions launchOptions;
    /**
     * Options for creating new incognito browser contexts.
     */
    private Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setScreenSize(1920, 1080);
    /**
     * Launch options for persistent browser context mode (with user data directory).
     */
    private BrowserType.LaunchPersistentContextOptions launchPersistentOptions;
    private String userDataRootDir;
    /**
     * Constructs a factory for incognito (non-persistent) browser context creation.
     *
     * @param browserType      the browser engine to use, defaults to {@link PlaywrightBrowserType#chromium} if {@code null}
     * @param launchOptions    the browser launch options, defaults to headless mode if {@code null}
     * @param newContextOptions the context creation options, uses screen size 1920x1080 if {@code null}
     */
    public BrowserContextPooledObjectFactory(PlaywrightBrowserType browserType,
                                             BrowserType.LaunchOptions launchOptions,
                                             Browser.NewContextOptions newContextOptions) {
        if (Objects.nonNull(browserType)) {
            this.browserType = browserType;
        }
        if (Objects.nonNull(launchOptions)) {
            this.launchOptions = launchOptions;
        } else {
            this.launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        }
        if (Objects.nonNull(newContextOptions)) {
            this.newContextOptions = newContextOptions;
        }
    }

    /**
     * Constructs a factory for persistent browser context creation with a user data directory.
     *
     * @param browserType           the browser engine to use, defaults to {@link PlaywrightBrowserType#chromium} if {@code null}
     * @param launchPersistentOptions the persistent context launch options, defaults to headless mode if {@code null}
     * @param userDataRootDir       the root directory for user data, defaults to {@code java.io.tmpdir} if blank
     */
    public BrowserContextPooledObjectFactory(PlaywrightBrowserType browserType,
                                             BrowserType.LaunchPersistentContextOptions launchPersistentOptions,
                                             String userDataRootDir) {
        if (Objects.nonNull(browserType)) {
            this.browserType = browserType;
        }
        if (Objects.nonNull(launchPersistentOptions)) {
            this.launchPersistentOptions = launchPersistentOptions;
        } else {
            this.launchPersistentOptions = new BrowserType.LaunchPersistentContextOptions().setHeadless(true);
        }
        if (userDataRootDir != null && !userDataRootDir.trim().isEmpty()) {
            this.userDataRootDir = userDataRootDir;
        } else {
            this.userDataRootDir = System.getProperty("java.io.tmpdir");
        }
    }

    /**
     * Called when a {@link BrowserContext} is borrowed from the pool. Clears cookies
     * to ensure a clean state for the next consumer.
     *
     * @param p a {@code PooledObject} wrapping the instance to be activated
     * @throws Exception if there is a problem activating the object
     */
    @Override
    public void activateObject(PooledObject<BrowserContext> p) throws Exception {
        BrowserContext browserContext = p.getObject();
        log.info("Activate BrowserContext Instance '{}'.", browserContext);
        if(Objects.nonNull(browserContext)){
            browserContext.clearCookies();
        }
    }

    /**
     * Called when a {@link BrowserContext} is being permanently removed from the pool.
     * Cleans up browser context resources, removes the Playwright mapping, and closes
     * the underlying Playwright instance.
     *
     * @param p a {@code PooledObject} wrapping the instance to be destroyed
     * @throws Exception if there is a problem destroying the object
     */
    @Override
    public void destroyObject(PooledObject<BrowserContext> p) throws Exception {
        BrowserContext browserContext = p.getObject();
        if (Objects.isNull(browserContext)) {
            return;
        }
        // Cleanup browser context
        cleanupBrowserContext(browserContext);
        log.info("Destroy BrowserContext Instance '{}'.", browserContext);
        Playwright playwright = PLAYWRIGHT_MAP.remove(browserContext);
        if (playwright != null) {
            playwright.close();
            log.info("Destroy browserContext of Playwright Instance '{}' Success.", playwright);
        }
    }

    /**
     * Cleans up all resources associated with a browser context, including cookies,
     * permissions, user data directories, and open pages.
     *
     * @param browserContext the browser context to clean up, may be {@code null}
     */
    public void cleanupBrowserContext(BrowserContext browserContext) {
        if (Objects.isNull(browserContext)) {
            return;
        }
        log.info("Cleanup BrowserContext Cookies '{}'.", browserContext);
        browserContext.clearCookies();
        log.info("Cleanup BrowserContext Permissions '{}'.", browserContext);
        browserContext.clearPermissions();
        File userDataDir = USER_DATA_DIR_MAP.remove(browserContext);
        if (Objects.nonNull(userDataDir) && userDataDir.exists()) {
            log.info("Cleanup BrowserContext user data directory '{}'.", userDataDir);
            try {
                FileUtils.deleteDirectory(userDataDir);
                log.info("Deleted user data directory: {}", userDataDir);
            } catch (IOException e) {
                log.error("Failed to delete user data directory: {}", userDataDir, e);
            }
        }
        List<Page> pages = browserContext.pages();
        if (!pages.isEmpty()) {
            for (Page page : pages) {
                if (page.isClosed()) {
                    continue;
                }
                log.info("Destroy page of BrowserContext Instance '{}'.", browserContext);
            }
        }
    }

    /**
     * Creates a new {@link BrowserContext} by launching a Playwright browser. In incognito mode,
     * a browser is launched and a new context is created. In persistent mode, a persistent context
     * is created with a dedicated user data directory.
     *
     * @return a new {@link PooledObject} wrapping the created {@link BrowserContext}
     * @throws Exception if there is a problem creating a new instance
     */
    @Override
    public PooledObject<BrowserContext> makeObject() throws Exception {
        Playwright playwright = Playwright.create();
        log.info("Create Playwright Instance '{}' Success.", playwright);
        BrowserContext browserContext = null;
        if (Objects.nonNull(launchPersistentOptions)) {
            File userDataDir = new File(userDataRootDir, String.valueOf(System.currentTimeMillis()));
            if(!userDataDir.exists()){
                userDataDir.mkdirs();
            }
            browserContext = PlaywrightUtil.getBrowserType(playwright, browserType)
                    .launchPersistentContext(userDataDir.toPath() , launchPersistentOptions);
            USER_DATA_DIR_MAP.put(browserContext, userDataDir);
            log.info("Create Persistent BrowserContext Instance '{}', browserType : {} , Success.", browserContext, browserType);
        } else {
            browserContext = PlaywrightUtil.getBrowserType(playwright, browserType)
                    .launch(launchOptions)
                    .newContext(newContextOptions);
            log.info("Create BrowserContext Instance '{}', browserType : {} , Success.", browserContext, browserType);
        }
        PLAYWRIGHT_MAP.put(browserContext, playwright);
        return new DefaultPooledObject<>(browserContext);
    }

    /**
     * Called when a {@link BrowserContext} is returned to the pool. Clears cookies and
     * closes all open pages to prepare the context for reuse.
     *
     * @param p a {@code PooledObject} wrapping the instance to be passivated
     * @throws Exception if there is a problem passivating the object
     */
    @Override
    public void passivateObject(PooledObject<BrowserContext> p) throws Exception {
        BrowserContext browserContext = p.getObject();
        log.info("Return BrowserContext Instance '{}'.", browserContext);
        if(Objects.nonNull(browserContext)){
            browserContext.clearCookies();
            browserContext.pages().forEach(page -> {
                PlaywrightUtil.closePage(page);
            });
            log.info("Return BrowserContext Instance : clear cookies success");
        }
    }

    /**
     * Validates whether a {@link BrowserContext} is still usable. In incognito mode,
     * checks that the context is non-null and the underlying browser is still connected.
     * In persistent mode, only checks for non-null.
     *
     * @param p a {@code PooledObject} wrapping the instance to be validated
     * @return {@code true} if the object is valid and can be served, {@code false} otherwise
     */
    @Override
    public boolean validateObject(PooledObject<BrowserContext> p) {
        BrowserContext browserContext = p.getObject();
        boolean isValidated;
        if (Objects.nonNull(launchOptions)) {
            isValidated = Objects.nonNull(browserContext) && browserContext.browser().isConnected();
        } else {
            isValidated = Objects.nonNull(browserContext);
        }
        log.info("Validate BrowserContext : {}, isValidated : {}", browserContext, isValidated);
        return isValidated;
    }

    /**
     * Closes all Playwright instances and cleans up all browser contexts managed by this factory.
     * This method is idempotent and safe to call multiple times.
     *
     * @throws Exception if an error occurs during cleanup
     */
    @Override
    public void close() throws Exception {
        PLAYWRIGHT_MAP.forEach((browserContext, playwright) -> {
            // Cleanup browser context
            cleanupBrowserContext(browserContext);
            if (playwright != null) {
                playwright.close();
                log.info("Destroy browserContext of Playwright Instance '{}' Success.", playwright);
            }
        });

    }

}
