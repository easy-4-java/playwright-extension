package com.microsoft.playwright.extension.options;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A fluent utility for conditionally mapping values from source objects to target consumers.
 * Provides a builder-style API with predicate-based filtering ({@link #whenNonNull()},
 * {@link #whenHasText()}, {@link #when(java.util.function.Predicate)}) that skips the
 * consumer call when the predicate evaluates to {@code false}.
 *
 * <p>Usage example:
 * <pre>{@code
 * OptionMapper map = OptionMapper.get().alwaysApplyingWhenNonNull();
 * map.from(config.getValue()).whenNonNull().to(options::setValue);
 * }</pre>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public final class OptionMapper {

    private OptionMapper() {
    }

    /**
     * Creates a new {@code OptionMapper} instance.
     *
     * @return a new {@code OptionMapper}
     */
    public static OptionMapper get() {
        return new OptionMapper();
    }

    /**
     * Configures this mapper to always apply mappings when the value is non-null.
     * Currently a no-op placeholder for future configuration.
     *
     * @return this mapper instance for chaining
     */
    public OptionMapper alwaysApplyingWhenNonNull() {
        return this;
    }

    /**
     * Begins a mapping chain from the given source value.
     *
     * @param  <T>   the type of the source value
     * @param value  the source value to map from
     * @return a {@link Source} that can be conditionally mapped to a consumer
     */
    public <T> Source<T> from(T value) {
        return new Source<>(value);
    }

    /**
     * Represents a source value in a conditional mapping chain. The value is only
     * passed to the consumer if the configured predicate evaluates to {@code true}.
     *
     * @param <T> the type of the source value
     */
    public static final class Source<T> {

        private final T value;
        private boolean accepted = true;

        private Source(T value) {
            this.value = value;
        }

        /**
         * Configures this source to only apply when the value is not {@code null}.
         *
         * @return this source instance for chaining
         */
        public Source<T> whenNonNull() {
            this.accepted = Objects.nonNull(this.value);
            return this;
        }

        /**
         * Configures this source to only apply when the value is a non-blank {@link String}.
         *
         * @return this source instance for chaining
         */
        public Source<T> whenHasText() {
            this.accepted = this.value instanceof String && ((String) this.value).trim().length() > 0;
            return this;
        }

        /**
         * Configures this source to only apply when the given predicate is satisfied.
         *
         * @param predicate the condition to evaluate against the source value
         * @return this source instance for chaining
         */
        public Source<T> when(Predicate<T> predicate) {
            this.accepted = predicate.test(this.value);
            return this;
        }

        /**
         * Passes the source value to the consumer if the predicate evaluated to {@code true}.
         *
         * @param consumer the consumer to receive the value
         */
        public void to(Consumer<T> consumer) {
            if (this.accepted) {
                consumer.accept(this.value);
            }
        }
    }
}
