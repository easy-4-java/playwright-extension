package com.microsoft.playwright.extension.pool;

import com.microsoft.playwright.BrowserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * An object pool for managing {@link BrowserContext} instances using Apache Commons Pool2.
 * Extends {@link GenericObjectPool} to provide pooled browser context lifecycle management,
 * enabling efficient reuse of browser contexts across concurrent requests.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see GenericObjectPool
 * @see BrowserContextPooledObjectFactory
 */
@Slf4j
public class BrowserContextPool extends GenericObjectPool<BrowserContext> {

    /**
     * Creates a new pool with the given factory and default pool configuration.
     *
     * @param factory the pooled object factory for creating and managing {@link BrowserContext} instances
     */
    public BrowserContextPool(PooledObjectFactory<BrowserContext> factory) {
        super(factory);
    }

    /**
     * Creates a new pool with the given factory and custom pool configuration.
     *
     * @param factory the pooled object factory for creating and managing {@link BrowserContext} instances
     * @param config  the pool configuration controlling max size, eviction policy, etc.
     */
    public BrowserContextPool(PooledObjectFactory<BrowserContext> factory, GenericObjectPoolConfig<BrowserContext> config) {
        super(factory, config);
    }

    /**
     * Creates a new pool with the given factory, custom pool configuration, and abandoned object detection settings.
     *
     * @param factory         the pooled object factory for creating and managing {@link BrowserContext} instances
     * @param config          the pool configuration controlling max size, eviction policy, etc.
     * @param abandonedConfig the configuration for detecting and reclaiming abandoned objects
     */
    public BrowserContextPool(PooledObjectFactory<BrowserContext> factory, GenericObjectPoolConfig<BrowserContext> config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }
}
