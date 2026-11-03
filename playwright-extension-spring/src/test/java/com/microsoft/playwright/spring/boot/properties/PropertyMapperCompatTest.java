package com.microsoft.playwright.spring.boot.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
