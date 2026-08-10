package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.Media;
import com.microsoft.playwright.options.ReducedMotion;
import lombok.Data;
import lombok.experimental.Accessors;
import com.microsoft.playwright.extension.options.OptionMapper;

/**
 * Configuration options for emulating CSS media features on a page.
 * Wraps Playwright's {@link com.microsoft.playwright.Page.EmulateMediaOptions} with a
 * Spring-friendly POJO that supports property binding and fluent chained setters.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see com.microsoft.playwright.Page.EmulateMediaOptions
 */
@Accessors(chain = true)
@Data
public class PageEmulateMediaOptions {

    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. Passing {@code null} disables color scheme emulation.
     */
    public ColorScheme colorScheme;
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"} and {@code "none"}. Passing {@code
     * null} disables forced colors emulation.
     */
    public ForcedColors forcedColors;
    /**
     * Changes the CSS media type of the page. The only allowed values are {@code "screen"}, {@code "print"} and {@code null}.
     * Passing {@code null} disables CSS media emulation.
     */
    public Media media;
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * Passing {@code null} disables reduced motion emulation.
     */
    public ReducedMotion reducedMotion;

    /**
     * Converts this configuration object into a Playwright {@link Page.EmulateMediaOptions} instance.
     *
     * @return a new {@link Page.EmulateMediaOptions} populated with non-null values from this configuration
     */
    public Page.EmulateMediaOptions toOptions() {
        OptionMapper map = OptionMapper.get().alwaysApplyingWhenNonNull();
        Page.EmulateMediaOptions options = new Page.EmulateMediaOptions();
        map.from(this.getColorScheme()).whenNonNull().to(options::setColorScheme);
        map.from(this.getForcedColors()).whenNonNull().to(options::setForcedColors);
        map.from(this.getMedia()).whenNonNull().to(options::setMedia);
        map.from(this.getReducedMotion()).whenNonNull().to(options::setReducedMotion);
        return options;
    }

}
