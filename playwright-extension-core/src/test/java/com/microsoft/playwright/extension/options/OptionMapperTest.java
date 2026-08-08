package com.microsoft.playwright.extension.options;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class OptionMapperTest {

    @Test
    void shouldCreateNewInstanceViaGet() {
        OptionMapper mapper1 = OptionMapper.get();
        OptionMapper mapper2 = OptionMapper.get();
        assertNotNull(mapper1);
        assertNotNull(mapper2);
        assertNotSame(mapper1, mapper2);
    }

    @Test
    void shouldReturnSelfFromAlwaysApplyingWhenNonNull() {
        OptionMapper mapper = OptionMapper.get();
        OptionMapper result = mapper.alwaysApplyingWhenNonNull();
        assertSame(mapper, result);
    }

    @Test
    void shouldApplyWhenNonNullValueIsPresent() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from("hello")
                .whenNonNull()
                .to(captured::set);
        assertEquals("hello", captured.get());
    }

    @Test
    void shouldNotApplyWhenNonNullValueIsNull() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from((String) null)
                .whenNonNull()
                .to(captured::set);
        assertNull(captured.get());
    }

    @Test
    void shouldApplyWhenHasTextWithNonEmptyString() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from("some text")
                .whenHasText()
                .to(captured::set);
        assertEquals("some text", captured.get());
    }

    @Test
    void shouldNotApplyWhenHasTextWithEmptyString() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from("")
                .whenHasText()
                .to(captured::set);
        assertNull(captured.get());
    }

    @Test
    void shouldNotApplyWhenHasTextWithBlankString() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from("   ")
                .whenHasText()
                .to(captured::set);
        assertNull(captured.get());
    }

    @Test
    void shouldNotApplyWhenHasTextWithNull() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from((String) null)
                .whenHasText()
                .to(captured::set);
        assertNull(captured.get());
    }

    @Test
    void shouldNotApplyWhenHasTextWithNonStringType() {
        AtomicReference<Integer> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from(42)
                .whenHasText()
                .to(captured::set);
        assertNull(captured.get());
    }

    @Test
    void shouldApplyWhenPredicateIsTrue() {
        AtomicReference<Integer> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from(42)
                .when(v -> v > 0)
                .to(captured::set);
        assertEquals(42, captured.get());
    }

    @Test
    void shouldNotApplyWhenPredicateIsFalse() {
        AtomicReference<Integer> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from(-1)
                .when(v -> v > 0)
                .to(captured::set);
        assertNull(captured.get());
    }

    @Test
    void shouldChainMultipleFromOperations() {
        AtomicReference<String> str1 = new AtomicReference<>();
        AtomicReference<String> str2 = new AtomicReference<>();
        OptionMapper mapper = OptionMapper.get().alwaysApplyingWhenNonNull();
        mapper.from("first").whenNonNull().to(str1::set);
        mapper.from("second").whenNonNull().to(str2::set);
        assertEquals("first", str1.get());
        assertEquals("second", str2.get());
    }

    @Test
    void shouldHandleWhenAppliedWithoutCondition() {
        AtomicReference<String> captured = new AtomicReference<>();
        OptionMapper.get().alwaysApplyingWhenNonNull()
                .from("value")
                .to(captured::set);
        assertEquals("value", captured.get());
    }
}
