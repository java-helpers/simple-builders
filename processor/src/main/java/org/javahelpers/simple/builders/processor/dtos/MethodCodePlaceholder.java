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
