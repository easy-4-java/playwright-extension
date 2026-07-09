package com.microsoft.playwright.spring.boot.options;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class OptionMapper {

    private OptionMapper() {
    }

    public static OptionMapper get() {
        return new OptionMapper();
    }

    public OptionMapper alwaysApplyingWhenNonNull() {
        return this;
    }

    public <T> Source<T> from(T value) {
        return new Source<>(value);
    }

    public static final class Source<T> {

        private final T value;
        private boolean accepted = true;

        private Source(T value) {
            this.value = value;
        }

        public Source<T> whenNonNull() {
            this.accepted = Objects.nonNull(this.value);
            return this;
        }

        public Source<T> whenHasText() {
            this.accepted = this.value instanceof String && ((String) this.value).trim().length() > 0;
            return this;
        }

        public Source<T> when(Predicate<T> predicate) {
            this.accepted = predicate.test(this.value);
            return this;
        }

        public void to(Consumer<T> consumer) {
            if (this.accepted) {
                consumer.accept(this.value);
            }
        }
    }
}
