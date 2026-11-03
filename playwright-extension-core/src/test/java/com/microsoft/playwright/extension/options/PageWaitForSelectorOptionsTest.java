package com.microsoft.playwright.extension.options;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PageWaitForSelectorOptionsTest {

    @Test
    void mapsDefaultsAndExplicitFalseAndZero() {
        PageWaitForSelectorOptions settings = new PageWaitForSelectorOptions();
        Page.WaitForSelectorOptions defaults = settings.toOptions();
        assertEquals(WaitForSelectorState.ATTACHED, defaults.state);
        assertEquals(30_000.0, defaults.timeout);
        assertNull(defaults.strict);

        settings.setStrict(false);
        settings.setTimeout(0.0);
        settings.setState(WaitForSelectorState.HIDDEN);
        Page.WaitForSelectorOptions explicit = settings.toOptions();
        assertFalse(explicit.strict);
        assertEquals(0.0, explicit.timeout);
        assertEquals(WaitForSelectorState.HIDDEN, explicit.state);
    }
}
