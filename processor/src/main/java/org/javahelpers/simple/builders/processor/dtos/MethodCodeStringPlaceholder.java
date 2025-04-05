package org.javahelpers.simple.builders.processor.dtos;

/**
 * Placeholder value for text contents. Implements {@code MethodCodePlaceholderInterface} to support
 * a generic mapping of placeholders.
 */
public class MethodCodeStringPlaceholder extends MethodCodePlaceholderInterface<String> {

  /**
   * Constructor for placeholders.
   *
   * @param label name of placeholder
   * @param value dynamic value for placeholder
   */
  public MethodCodeStringPlaceholder(String label, String value) {
    super(label, value);
  }
}
