package org.javahelpers.simple.builders.processor.dtos;

public class MethodCodePlaceholderInterface<T> {
  private final String label;
  private final T value;

  public MethodCodePlaceholderInterface(String label, T value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public T getValue() {
    return value;
  }
}
