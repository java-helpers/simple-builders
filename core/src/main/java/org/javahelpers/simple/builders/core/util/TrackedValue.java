package org.javahelpers.simple.builders.core.util;

import java.util.function.Consumer;

public record TrackedValue<T>(T value, boolean isChanged) {

  public void ifChanged(Consumer<T> consumer) {
    if (isChanged) {
      consumer.accept(value);
    }
  }

  public static <T> TrackedValue<T> unsetValue() {
    return new TrackedValue<>(null, false);
  }

  public static <T> TrackedValue<T> initialValue(T value) {
    return new TrackedValue<>(value, false);
  }

  public static <T> TrackedValue<T> changedValue(T value) {
    return new TrackedValue<>(value, true);
  }
}
