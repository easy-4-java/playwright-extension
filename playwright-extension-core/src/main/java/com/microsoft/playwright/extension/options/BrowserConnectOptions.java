package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.BrowserType;
import lombok.Data;
import lombok.experimental.Accessors;
import com.microsoft.playwright.extension.options.OptionMapper;

import java.util.Map;

/**
 * Configuration options for connecting to an existing browser instance via WebSocket.
 * Wraps Playwright's {@link com.microsoft.playwright.BrowserType.ConnectOptions} with
 * a Spring-friendly POJO that supports property binding and fluent chained setters.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see com.microsoft.playwright.BrowserType.ConnectOptions
 */
@Accessors(chain = true)
@Data
public class BrowserConnectOptions {

    /**
     * Additional HTTP headers to be sent with web socket connect request. Optional.
     */
    public Map<String, String> headers;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public Double slowMo = 0.0;
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 0} (no timeout).
     */
    public Double timeout = 0.0;

    /**
     * Converts this configuration object into a Playwright {@link BrowserType.ConnectOptions} instance.
     *
     * @return a new {@link BrowserType.ConnectOptions} populated with non-null values from this configuration
     */
    public BrowserType.ConnectOptions toOptions() {
        OptionMapper map = OptionMapper.get().alwaysApplyingWhenNonNull();
        BrowserType.ConnectOptions options = new BrowserType.ConnectOptions();
        map.from(this.getHeaders()).whenNonNull().to(options::setHeaders);
        map.from(this.getSlowMo()).whenNonNull().to(options::setSlowMo);
        map.from(this.getTimeout()).whenNonNull().to(options::setTimeout);
        return options;
    }

}
