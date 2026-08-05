/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.simple.builders.core.util;

import java.util.function.Consumer;

/**
 * Represents a value together with a flag indicating whether it has been explicitly changed.
 *
 * <p>This utility is used by generated builders to differentiate between:
 *
 * <ul>
 *   <li>an unset value (no change made),
 *   <li>an initial value (derived from an existing instance), and
 *   <li>a changed value (explicitly set via a builder method).
 * </ul>
 *
 * The {@code isChanged} flag controls whether setter invocations should be performed during {@code
 * build()}; only changed values are applied.
 *
 * @param <T> the value type
 * @param value the underlying value (may be {@code null})
 * @param isChanged whether the value was explicitly changed by the builder API
 * @param isInitial whether the value is an initial value from an existing instance
 */
public record TrackedValue<T>(T value, boolean isChanged, boolean isInitial) {

  /**
   * Checks if this value has been set (either as initial value or changed).
   *
   * @return true if the value is set (initial or changed), false if unset
   */
  public boolean isSet() {
    return isChanged || isInitial;
  }

  /**
   * Returns the value if set, otherwise the given default.
   *
   * @param defaultValue the default to return if unset
   * @return the value if set, otherwise the default
   */
  public T valueOr(T defaultValue) {
    return isSet() ? value : defaultValue;
  }

  /**
   * Executes the provided {@link Consumer} only if this value has been set (initial value or
   * changed). Returns a {@link DefaultValueApplier} that can fluently provide a default via {@code
   * .orElse(default)} if the value was unset.
   *
   * <p>Existing code that ignores the return value (e.g. {@code tracked.ifSet(consumer);})
   * continues to work unchanged.
   *
   * @param consumer action to perform with the current {@link #value()}
   * @return a {@link DefaultValueApplier} for fluent default handling
   */
  public DefaultValueApplier<T> ifSet(Consumer<T> consumer) {
    if (isSet()) {
      consumer.accept(value);
    }
    return new DefaultValueApplier<>(isSet(), consumer);
  }

  /**
   * Intermediate result returned by {@link #ifSet(Consumer)} to support fluent default-value
   * application via {@code .orElse(default)}.
   *
   * <p>When {@link #ifSet(Consumer)} is called on a set {@link TrackedValue}, the consumer is
   * invoked immediately and {@code alreadyApplied} is {@code true}, making the subsequent {@link
   * #orElse(Object)} call a no-op. When called on an unset value, the consumer is not invoked and
   * {@code alreadyApplied} is {@code false}, so {@link #orElse(Object)} applies the default value
   * to the same consumer.
   *
   * @param <T> the value type
   * @param alreadyApplied whether the consumer has already been invoked
   * @param consumer the consumer to apply the default value to if not already applied
   */
  public record DefaultValueApplier<T>(boolean alreadyApplied, Consumer<T> consumer) {

    /**
     * Applies the given default value to the consumer if the original value was unset.
     *
     * @param defaultValue the default value to apply
     */
    public void orElse(T defaultValue) {
      if (!alreadyApplied) {
        consumer.accept(defaultValue);
      }
    }
  }

  /**
   * Creates a tracked value representing an unset state (no change made).
   *
   * @param <T> the value type
   * @return an instance with {@code value == null}, {@code isChanged == false}, and {@code
   *     isInitial == false}
   */
  public static <T> TrackedValue<T> unsetValue() {
    return new TrackedValue<>(null, false, false);
  }

  /**
   * Creates a tracked value representing an initial value copied from an existing instance. The
   * value is present but is not considered a change initiated by the builder.
   *
   * @param <T> the value type
   * @param value the initial value (may be {@code null})
   * @return an instance with the given value, {@code isChanged == false}, and {@code isInitial ==
   *     true}
   */
  public static <T> TrackedValue<T> initialValue(T value) {
    return new TrackedValue<>(value, false, true);
  }

  /**
   * Creates a tracked value representing an explicit change performed via the builder API.
   *
   * @param <T> the value type
   * @param value the changed value (may be {@code null})
   * @return an instance with the given value and {@code isChanged == true}
   */
  public static <T> TrackedValue<T> changedValue(T value) {
    return new TrackedValue<>(value, true, false);
  }
}
