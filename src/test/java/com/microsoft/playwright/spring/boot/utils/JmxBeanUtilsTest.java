package com.microsoft.playwright.spring.boot.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JmxBeanUtilsTest {

    @Test
    void shouldGenerateObjectNameForStringClass() {
        String result = JmxBeanUtils.getObjectName(String.class);
        assertEquals("java.lang:type=String", result);
    }

    @Test
    void shouldGenerateObjectNameForIntegerClass() {
        String result = JmxBeanUtils.getObjectName(Integer.class);
        assertEquals("java.lang:type=Integer", result);
    }

    @Test
    void shouldGenerateObjectNameForTestClass() {
        String result = JmxBeanUtils.getObjectName(JmxBeanUtilsTest.class);
        assertEquals("com.microsoft.playwright.spring.boot.utils:type=JmxBeanUtilsTest", result);
    }

    @Test
    void shouldContainTypeNamePrefix() {
        String result = JmxBeanUtils.getObjectName(JmxBeanUtils.class);
        assertTrue(result.contains(":type=JmxBeanUtils"));
    }

    @Test
    void shouldContainPackageName() {
        String result = JmxBeanUtils.getObjectName(JmxBeanUtils.class);
        assertTrue(result.startsWith("com.microsoft.playwright.spring.boot.utils:"));
    }

    @Test
    void shouldHandleDifferentPackages() {
        String result = JmxBeanUtils.getObjectName(java.util.List.class);
        assertEquals("java.util:type=List", result);
    }
}
