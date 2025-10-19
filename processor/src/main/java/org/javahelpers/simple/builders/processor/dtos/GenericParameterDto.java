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

import java.util.LinkedList;
import java.util.List;

/** Represents a generic type parameter (e.g., {@code T extends Number & Comparable<T>}). */
public class GenericParameterDto {
  /** The name of the generic parameter (e.g., "T"). */
  private String name;

  /** Optional upper bounds in declared order (empty if unbounded). */
  private final List<TypeName> upperBounds = new LinkedList<>();

  /**
   * Gets the name of the generic parameter.
   *
   * @return the name (e.g., "T", "K", "V")
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the generic parameter.
   *
   * @param name the name to set (e.g., "T", "K", "V")
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Add an upper bound.
   *
   * @param bound the upper bound type to add
   */
  public void addUpperBound(TypeName bound) {
    if (bound != null) {
      upperBounds.add(bound);
    }
  }

  /**
   * Returns all upper bounds (possibly empty).
   *
   * @return the list of upper bound types
   */
  public List<TypeName> getUpperBounds() {
    return upperBounds;
  }
}
