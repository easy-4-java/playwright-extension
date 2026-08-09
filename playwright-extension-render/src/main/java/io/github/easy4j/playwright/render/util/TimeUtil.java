package io.github.easy4j.playwright.render.util;

/**
 * Utility class for time measurement and formatting operations.
 */
public class TimeUtil {

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public static double getRetryTimeout(double timeout) {
        return timeout + timeout * DEFAULT_LOAD_FACTOR;
    }

}
