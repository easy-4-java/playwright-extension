package com.microsoft.playwright.spring.boot.options;


import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.Data;
import lombok.experimental.Accessors;
import com.microsoft.playwright.spring.boot.options.OptionMapper;

/**
 * Configuration options for page navigation.
 * Wraps Playwright's {@link com.microsoft.playwright.Page.NavigateOptions} with a
 * Spring-friendly POJO that supports property binding and fluent chained setters.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.microsoft.playwright.Page.NavigateOptions
 */
@Accessors(chain = true)
@Data
public class PageNavigateOptions {

    /**
     * Referer header value. If provided it will take preference over the referer header value set by {@link
     * Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}.
     */
    public String referer;
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout = 30 * 1000.0;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil = WaitUntilState.NETWORKIDLE;

    /**
     * Converts this configuration object into a Playwright {@link Page.NavigateOptions} instance.
     *
     * @return a new {@link Page.NavigateOptions} populated with non-null values from this configuration
     */
    public Page.NavigateOptions toOptions(){
        OptionMapper map = OptionMapper.get().alwaysApplyingWhenNonNull();
        Page.NavigateOptions options = new Page.NavigateOptions();
        map.from(this.getReferer()).whenHasText().to(options::setReferer);
        map.from(this.getTimeout()).whenNonNull().to(options::setTimeout);
        map.from(this.getWaitUntil()).whenNonNull().to(options::setWaitUntil);
        return options;
    };

}
