package com.microsoft.playwright.extension.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.extension.PlaywrightBrowserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaywrightUtilTest {

    @Mock
    private Playwright playwright;

    @Mock
    private Page page;

    @Mock
    private BrowserContext browserContext;

    @Test
    void shouldGetChromiumBrowserType() {
        when(playwright.chromium()).thenReturn(mock(BrowserType.class));
        BrowserType result = PlaywrightUtil.getBrowserType(playwright, PlaywrightBrowserType.chromium);
        assertNotNull(result);
        verify(playwright).chromium();
    }

    @Test
    void shouldGetFirefoxBrowserType() {
        when(playwright.firefox()).thenReturn(mock(BrowserType.class));
        BrowserType result = PlaywrightUtil.getBrowserType(playwright, PlaywrightBrowserType.firefox);
        assertNotNull(result);
        verify(playwright).firefox();
    }

    @Test
    void shouldGetWebkitBrowserType() {
        when(playwright.webkit()).thenReturn(mock(BrowserType.class));
        BrowserType result = PlaywrightUtil.getBrowserType(playwright, PlaywrightBrowserType.webkit);
        assertNotNull(result);
        verify(playwright).webkit();
    }

    @Test
    void shouldThrowOnUnsupportedBrowserType() {
        assertThrows(Exception.class,
                () -> PlaywrightUtil.getBrowserType(playwright, null));
    }

    @Test
    void shouldConvertCookieListToString() {
        Cookie cookie1 = new Cookie("name1", "value1");
        Cookie cookie2 = new Cookie("name2", "value2");
        List<Cookie> cookies = Arrays.asList(cookie1, cookie2);
        String result = PlaywrightUtil.cookieToString(cookies);
        assertEquals("name1=value1;name2=value2", result);
    }

    @Test
    void shouldConvertEmptyCookieListToString() {
        String result = PlaywrightUtil.cookieToString(Collections.emptyList());
        assertEquals("", result);
    }

    @Test
    void shouldConvertSingleCookieToString() {
        Cookie cookie = new Cookie("session", "abc123");
        String result = PlaywrightUtil.cookieToString(List.of(cookie));
        assertEquals("session=abc123", result);
    }

    @Test
    void shouldGetCookiesFromPage() {
        Cookie cookie = new Cookie("token", "xyz");
        when(page.context()).thenReturn(browserContext);
        when(browserContext.cookies()).thenReturn(List.of(cookie));
        String result = PlaywrightUtil.getCookies(page);
        assertEquals("token=xyz", result);
    }

    @Test
    void shouldClosePage() {
        when(page.isClosed()).thenReturn(false);
        PlaywrightUtil.closePage(page);
        verify(page).close();
    }

    @Test
    void shouldNotCloseAlreadyClosedPage() {
        when(page.isClosed()).thenReturn(true);
        PlaywrightUtil.closePage(page);
        verify(page, never()).close();
    }

    @Test
    void shouldHandleNullPageGracefully() {
        assertDoesNotThrow(() -> PlaywrightUtil.closePage(null));
    }

    @Test
    void shouldHandlePageCloseException() {
        when(page.isClosed()).thenReturn(false);
        doThrow(new RuntimeException("close error")).when(page).close();
        assertDoesNotThrow(() -> PlaywrightUtil.closePage(page));
    }

    @Test
    void shouldSetAndGetUserDataDir() {
        PlaywrightUtil.setUerDataDir("/tmp/test-data");
        assertEquals("/tmp/test-data", PlaywrightUtil.getUerDataDir());
        // Clean up thread local
        PlaywrightUtil.setUerDataDir(null);
    }

    @Test
    void shouldReturnNullWhenUserDataDirNotSet() {
        PlaywrightUtil.setUerDataDir(null);
        assertNull(PlaywrightUtil.getUerDataDir());
    }

    @Test
    void shouldHaveOptionMapper() {
        assertNotNull(PlaywrightUtil.map);
    }
}
