package com.microsoft.playwright.extension.utils;

/**
 * Utility for registering and managing JMX beans for Playwright monitoring.
 * class for generating JMX ObjectName strings used in MBean registration.
 * Produces names in the format {@code "package.name:type=ClassName"}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public class JmxBeanUtils {

    /**
     * Generates a JMX ObjectName string for the given class. The name is formatted as
     * {@code "fully.qualified.package:type=SimpleClassName"}.
     *
     * @param <T>    the type of the class
     * @param tClass the class to generate an ObjectName for
     * @return a JMX ObjectName string in the format {@code "package:type=ClassName"}
     */
    public static <T> String getObjectName(Class<T> tClass) {
        String packageName = tClass.getPackage().getName();
        String objectName = packageName + ":type=" + tClass.getSimpleName();
        return objectName;
    }

}
