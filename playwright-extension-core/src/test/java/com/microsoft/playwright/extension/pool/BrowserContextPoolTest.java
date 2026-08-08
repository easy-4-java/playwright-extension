package com.microsoft.playwright.extension.pool;

import com.microsoft.playwright.BrowserContext;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BrowserContextPoolTest {

    @Mock
    private PooledObjectFactory<BrowserContext> factory;

    @Test
    void shouldCreatePoolWithFactoryOnly() {
        BrowserContextPool pool = new BrowserContextPool(factory);
        assertNotNull(pool);
        pool.close();
    }

    @Test
    void shouldCreatePoolWithFactoryAndConfig() {
        GenericObjectPoolConfig<BrowserContext> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(5);
        BrowserContextPool pool = new BrowserContextPool(factory, config);
        assertNotNull(pool);
        pool.close();
    }

    @Test
    void shouldExtendGenericObjectPool() {
        BrowserContextPool pool = new BrowserContextPool(factory);
        assertInstanceOf(org.apache.commons.pool2.impl.GenericObjectPool.class, pool);
        pool.close();
    }
}
