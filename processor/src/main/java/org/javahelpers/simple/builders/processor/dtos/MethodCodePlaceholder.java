package org.javahelpers.simple.builders.processor.dtos;

/**
 * Interface for supported placeholder value types.
 *
 * @param <T> Generic type for the dynamic value
 */
public abstract class MethodCodePlaceholder<T> {
  private final String label;
  private final T value;

  /**
   * Constructor for placeholders.
   *
   * @param label name of placeholder
   * @param value dynamic value for placeholder
   */
  public MethodCodePlaceholder(String label, T value) {
    this.label = label;
    this.value = value;
  }

  /**
   * Getter for name of placeholder.
   *
   * @return placeholder name
   */
  public String getLabel() {
    return label;
  }

  /**
   * Getter for dynamic value of placeholder.
   *
   * @return dynamic value
   */
  public T getValue() {
    return value;
  }
}
