package com.microsoft.playwright.spring.boot.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ThreadUtilsTest {

    @Test
    void shouldCreateThreadPoolExecutor() {
        ExecutorService executor = ThreadUtils.newThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10), "test-pool", true);
        assertNotNull(executor);
        executor.shutdownNow();
    }

    @Test
    void shouldCreateSingleThreadExecutor() {
        ExecutorService executor = ThreadUtils.newSingleThreadExecutor("test-single", true);
        assertNotNull(executor);
        executor.shutdownNow();
    }

    @Test
    void shouldCreateSingleThreadScheduledExecutor() {
        ScheduledExecutorService executor = ThreadUtils.newSingleThreadScheduledExecutor("test-scheduled", true);
        assertNotNull(executor);
        executor.shutdownNow();
    }

    @Test
    void shouldCreateFixedThreadScheduledPool() {
        ScheduledExecutorService executor = ThreadUtils.newFixedThreadScheduledPool(3, "test-fixed-scheduled", true);
        assertNotNull(executor);
        executor.shutdownNow();
    }

    @Test
    void shouldCreateThreadFactory() {
        ThreadFactory factory = ThreadUtils.newThreadFactory("test-factory", true);
        assertNotNull(factory);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.getName().startsWith("Remoting-test-factory_"));
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldCreateNonDaemonThread() {
        ThreadFactory factory = ThreadUtils.newThreadFactory("test-nondaemon", false);
        Thread thread = factory.newThread(() -> {});
        assertFalse(thread.isDaemon());
    }

    @Test
    void shouldCreateGenericThreadFactory() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("generic-test");
        assertNotNull(factory);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.getName().startsWith("generic-test_"));
        assertFalse(thread.isDaemon());
    }

    @Test
    void shouldCreateGenericThreadFactoryWithDaemon() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("generic-daemon-test", true);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldCreateGenericThreadFactoryWithThreadCount() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("generic-count-test", 5);
        assertNotNull(factory);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.getName().contains("generic-count-test"));
        assertFalse(thread.isDaemon());
    }

    @Test
    void shouldCreateGenericThreadFactoryWithThreadCountAndDaemon() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("generic-count-daemon-test", 3, true);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.getName().contains("generic-count-daemon-test_3_"));
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldIncrementThreadIndex() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("indexed");
        Thread t1 = factory.newThread(() -> {});
        Thread t2 = factory.newThread(() -> {});
        assertTrue(t1.getName().endsWith("_1"));
        assertTrue(t2.getName().endsWith("_2"));
    }

    @Test
    void shouldCreateNewThread() {
        AtomicBoolean executed = new AtomicBoolean(false);
        Thread thread = ThreadUtils.newThread("test-thread", () -> executed.set(true), true);
        assertNotNull(thread);
        assertEquals("test-thread", thread.getName());
        assertTrue(thread.isDaemon());
        thread.start();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertTrue(executed.get());
    }

    @Test
    void shouldCreateNonDaemonNewThread() {
        Thread thread = ThreadUtils.newThread("non-daemon-thread", () -> {}, false);
        assertFalse(thread.isDaemon());
    }

    @Test
    void shouldShutdownNullThreadGracefully() {
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(null));
    }

    @Test
    void shouldShutdownNullThreadWithTimeout() {
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(null, 1000L));
    }

    @Test
    void shouldShutdownThreadGracefully() {
        AtomicBoolean running = new AtomicBoolean(true);
        Thread thread = new Thread(() -> {
            while (running.get()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "graceful-test");
        thread.start();
        running.set(false);
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(thread, 1000L));
    }

    @Test
    void shouldShutdownExecutorServiceGracefully() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {});
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(executor, 1, TimeUnit.SECONDS));
        assertTrue(executor.isShutdown());
    }

    @Test
    void shouldShutdownAlreadyTerminatedExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.shutdown();
        executor.shutdownNow();
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(executor, 1, TimeUnit.SECONDS));
    }

    @Test
    void shouldNotBeInstantiable() throws NoSuchMethodException {
        var constructor = ThreadUtils.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null));
    }
}
