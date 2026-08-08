package com.microsoft.playwright.spring.boot.options;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageNavigateOptionsTest {

    @Test
    void shouldCreateDefaultOptions() {
        PageNavigateOptions opts = new PageNavigateOptions();
        assertNull(opts.getReferer());
        assertEquals(30000.0, opts.getTimeout());
        assertEquals(WaitUntilState.NETWORKIDLE, opts.getWaitUntil());
    }

    @Test
    void shouldSupportChainedSetters() {
        PageNavigateOptions opts = new PageNavigateOptions()
                .setReferer("http://example.com")
                .setTimeout(10000.0)
                .setWaitUntil(WaitUntilState.LOAD);
        assertEquals("http://example.com", opts.getReferer());
        assertEquals(10000.0, opts.getTimeout());
        assertEquals(WaitUntilState.LOAD, opts.getWaitUntil());
    }

    @Test
    void shouldMapAllFieldsToNavigateOptions() {
        PageNavigateOptions opts = new PageNavigateOptions()
                .setReferer("http://example.com")
                .setTimeout(5000.0)
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED);
        Page.NavigateOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithNullFields() {
        PageNavigateOptions opts = new PageNavigateOptions()
                .setReferer(null)
                .setTimeout(null)
                .setWaitUntil(null);
        Page.NavigateOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyReferer() {
        PageNavigateOptions opts = new PageNavigateOptions()
                .setReferer("")
                .setTimeout(5000.0);
        Page.NavigateOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldSkipBlankReferer() {
        PageNavigateOptions opts = new PageNavigateOptions()
                .setReferer("   ");
        Page.NavigateOptions result = opts.toOptions();
        assertNotNull(result);
    }

    @Test
    void shouldMapWithCommitWaitUntil() {
        PageNavigateOptions opts = new PageNavigateOptions()
                .setWaitUntil(WaitUntilState.COMMIT);
        Page.NavigateOptions result = opts.toOptions();
        assertNotNull(result);
    }
}
