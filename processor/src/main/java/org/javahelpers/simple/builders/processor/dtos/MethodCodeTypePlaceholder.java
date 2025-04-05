package org.javahelpers.simple.builders.processor.dtos;

/**
 * Placeholder value for type contents. Implements {@code MethodCodePlaceholderInterface} to support
 * a generic mapping of placeholders.
 */
public class MethodCodeTypePlaceholder extends MethodCodePlaceholderInterface<TypeName> {

  /**
   * Constructor for placeholders.
   *
   * @param label name of placeholder
   * @param value dynamic value for placeholder
   */
  public MethodCodeTypePlaceholder(String label, TypeName value) {
    super(label, value);
  }
}
