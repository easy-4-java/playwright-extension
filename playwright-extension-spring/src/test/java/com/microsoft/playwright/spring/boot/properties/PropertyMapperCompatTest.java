package com.microsoft.playwright.spring.boot.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyMapperCompatTest {

    @Test
    void alwaysApplyingWhenNonNullIgnoresNullAndMapsValues() {
        AtomicReference<String> result = new AtomicReference<>("original");
        PropertyMapper mapper = PropertyMapperCompat.alwaysApplyingWhenNonNull();

        mapper.from((String) null).to(result::set);
        assertEquals("original", result.get());

        mapper.from("updated").to(result::set);
        assertEquals("updated", result.get());
    }

    @Test
    void whenNonNullFiltersOnlyNullSources() {
        AtomicReference<String> result = new AtomicReference<>("original");

        PropertyMapperCompat.whenNonNull(PropertyMapper.get().from((String) null)).to(result::set);
        assertEquals("original", result.get());

        PropertyMapperCompat.whenNonNull(PropertyMapper.get().from("updated")).to(result::set);
        assertEquals("updated", result.get());
    }

    @Test
    void supplierFailuresAreNotSwallowed() {
        Supplier<String> failing = () -> { throw new NullPointerException("supplier failure"); };
        AtomicReference<String> result = new AtomicReference<>("original");

        assertThrows(NullPointerException.class,
                () -> PropertyMapperCompat.whenNonNull(PropertyMapper.get().from(failing)).to(result::set));
        assertThrows(NullPointerException.class,
                () -> PropertyMapperCompat.alwaysApplyingWhenNonNull().from(failing).to(result::set));
        assertEquals("original", result.get());
    }
}
