package com.microsoft.playwright.extension.pool;

import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.microsoft.playwright.extension.options.OptionMapper;

import java.time.Duration;

/**
 * Configuration POJO for the {@link BrowserContextPool}. Exposes all Apache Commons Pool2
 * {@link GenericObjectPoolConfig} settings with a Spring-friendly interface that supports
 * property binding via {@code @ConfigurationProperties}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see BrowserContextPool
 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig
 */
@Data
public class BrowserContextPoolConfig {

    private boolean blockWhenExhausted = GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;

    private Duration durationBetweenEvictionRuns = GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS;

    private Duration evictorShutdownTimeoutDuration = GenericObjectPoolConfig.DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT;

    private String evictionPolicyClassName = GenericObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME;

    private boolean fairness = GenericObjectPoolConfig.DEFAULT_FAIRNESS;

    private boolean lifo = GenericObjectPoolConfig.DEFAULT_LIFO;

    private Duration maxWaitDuration = GenericObjectPoolConfig.DEFAULT_MAX_WAIT;

    private int maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;

    private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;

    private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

    private Duration minEvictableIdleDuration = GenericObjectPoolConfig. DEFAULT_MIN_EVICTABLE_IDLE_DURATION;

    private Duration softMinEvictableIdleDuration = GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION;

    private int numTestsPerEvictionRun = GenericObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

    private boolean testOnCreate = GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE;

    private boolean testOnBorrow = GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW;

    private boolean testOnReturn = GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN;

    private boolean testWhileIdle = GenericObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;

    /**
     * Converts this configuration POJO into an Apache Commons Pool2 {@link GenericObjectPoolConfig} instance.
     *
     * @return a new {@link GenericObjectPoolConfig} populated with the values from this configuration
     */
    public GenericObjectPoolConfig toPoolConfig(){
        OptionMapper map = OptionMapper.get().alwaysApplyingWhenNonNull();
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        map.from(this.isBlockWhenExhausted()).to(poolConfig::setBlockWhenExhausted);
        map.from(this.getDurationBetweenEvictionRuns()).whenNonNull().to(poolConfig::setTimeBetweenEvictionRuns);
        map.from(this.getEvictionPolicyClassName()).whenHasText().to(poolConfig::setEvictionPolicyClassName);
        map.from(this.getEvictorShutdownTimeoutDuration()).whenNonNull().to(poolConfig::setEvictorShutdownTimeout);
        map.from(this.isFairness()).to(poolConfig::setFairness);
        map.from(this.isLifo()).to(poolConfig::setLifo);
        map.from(this.getMaxWaitDuration()).whenNonNull().to(poolConfig::setMaxWait);
        map.from(this.getMaxIdle()).to(poolConfig::setMaxIdle);
        map.from(this.getMaxTotal()).to(poolConfig::setMaxTotal);
        map.from(this.getMinEvictableIdleDuration()).whenNonNull().to(poolConfig::setMinEvictableIdleTime);
        map.from(this.getMinIdle()).to(poolConfig::setMinIdle);
        map.from(this.getNumTestsPerEvictionRun()).to(poolConfig::setNumTestsPerEvictionRun);
        map.from(this.getSoftMinEvictableIdleDuration()).whenNonNull().to(poolConfig::setSoftMinEvictableIdleTime);
        map.from(this.isTestOnBorrow()).to(poolConfig::setTestOnBorrow);
        map.from(this.isTestOnCreate()).to(poolConfig::setTestOnCreate);
        map.from(this.isTestOnReturn()).to(poolConfig::setTestOnReturn);
        map.from(this.isTestWhileIdle()).to(poolConfig::setTestWhileIdle);
        return poolConfig;
    }


}
