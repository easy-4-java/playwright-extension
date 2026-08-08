package com.microsoft.playwright.spring.boot.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class BrowserContextPoolConfigTest {

    @Test
    void shouldCreateWithDefaultValues() {
        BrowserContextPoolConfig config = new BrowserContextPoolConfig();
        assertTrue(config.isBlockWhenExhausted());
        assertNotNull(config.getDurationBetweenEvictionRuns());
        assertNotNull(config.getEvictorShutdownTimeoutDuration());
        assertNotNull(config.getEvictionPolicyClassName());
        assertFalse(config.isFairness());
        assertTrue(config.isLifo());
        assertNotNull(config.getMaxWaitDuration());
        assertEquals(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL, config.getMaxTotal());
        assertEquals(GenericObjectPoolConfig.DEFAULT_MAX_IDLE, config.getMaxIdle());
        assertEquals(GenericObjectPoolConfig.DEFAULT_MIN_IDLE, config.getMinIdle());
        assertNotNull(config.getMinEvictableIdleDuration());
        assertNotNull(config.getSoftMinEvictableIdleDuration());
        assertEquals(GenericObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN, config.getNumTestsPerEvictionRun());
        assertFalse(config.isTestOnCreate());
        assertFalse(config.isTestOnBorrow());
        assertFalse(config.isTestOnReturn());
        assertFalse(config.isTestWhileIdle());
    }

    @Test
    void shouldSupportSetters() {
        BrowserContextPoolConfig config = new BrowserContextPoolConfig();
        config.setBlockWhenExhausted(false);
        config.setDurationBetweenEvictionRuns(Duration.ofSeconds(60));
        config.setEvictorShutdownTimeoutDuration(Duration.ofSeconds(10));
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        config.setFairness(true);
        config.setLifo(false);
        config.setMaxWaitDuration(Duration.ofSeconds(5));
        config.setMaxTotal(20);
        config.setMaxIdle(10);
        config.setMinIdle(5);
        config.setMinEvictableIdleDuration(Duration.ofMinutes(30));
        config.setSoftMinEvictableIdleDuration(Duration.ofMinutes(10));
        config.setNumTestsPerEvictionRun(3);
        config.setTestOnCreate(true);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);

        assertFalse(config.isBlockWhenExhausted());
        assertEquals(Duration.ofSeconds(60), config.getDurationBetweenEvictionRuns());
        assertEquals(20, config.getMaxTotal());
        assertTrue(config.isFairness());
        assertFalse(config.isLifo());
        assertTrue(config.isTestOnBorrow());
        assertTrue(config.isTestWhileIdle());
    }

    @Test
    void shouldConvertToPoolConfigWithDefaults() {
        BrowserContextPoolConfig config = new BrowserContextPoolConfig();
        GenericObjectPoolConfig poolConfig = config.toPoolConfig();
        assertNotNull(poolConfig);
    }

    @Test
    void shouldConvertToPoolConfigWithCustomValues() {
        BrowserContextPoolConfig config = new BrowserContextPoolConfig();
        config.setMaxTotal(50);
        config.setMaxIdle(25);
        config.setMinIdle(10);
        config.setBlockWhenExhausted(false);
        config.setFairness(true);
        config.setLifo(false);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        config.setTestOnCreate(true);
        config.setNumTestsPerEvictionRun(5);
        config.setMaxWaitDuration(Duration.ofSeconds(3));
        config.setDurationBetweenEvictionRuns(Duration.ofSeconds(30));
        config.setEvictorShutdownTimeoutDuration(Duration.ofSeconds(5));
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        config.setMinEvictableIdleDuration(Duration.ofMinutes(15));
        config.setSoftMinEvictableIdleDuration(Duration.ofMinutes(5));

        GenericObjectPoolConfig poolConfig = config.toPoolConfig();
        assertNotNull(poolConfig);
    }

    @Test
    void shouldConvertToPoolConfigWithNullDurations() {
        BrowserContextPoolConfig config = new BrowserContextPoolConfig();
        config.setDurationBetweenEvictionRuns(null);
        config.setEvictorShutdownTimeoutDuration(null);
        config.setMaxWaitDuration(null);
        config.setMinEvictableIdleDuration(null);
        config.setSoftMinEvictableIdleDuration(null);
        GenericObjectPoolConfig poolConfig = config.toPoolConfig();
        assertNotNull(poolConfig);
    }
}
