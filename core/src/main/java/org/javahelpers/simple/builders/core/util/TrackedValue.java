/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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
   * Executes the provided {@link Consumer} only if this value has been set (initial value or
   * changed).
   *
   * @param consumer action to perform with the current {@link #value()}
   */
  public void ifSet(Consumer<T> consumer) {
    if (isSet()) {
      consumer.accept(value);
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
