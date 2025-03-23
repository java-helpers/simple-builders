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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * TypeName is the unambiguously definition of a type. Holding name of class and package. Could be
 * extended to be a type with generic parts.
 */
public class TypeName {
  /** Name of package. */
  private final String packageName;

  /** Name of class. */
  private final String className;

  /**
   * Constructor for TypeName.
   *
   * @param packageName name of package
   * @param className name of class, could not be null
   * @throws NullPointerException if classname is null
   */
  public TypeName(String packageName, String className) {
    requireNonNull(className);
    this.packageName = packageName;
    this.className = className;
  }

  /**
   * Returns name of package
   *
   * @return package name of type {@code java.lang.String}
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Returns name of class
   *
   * @return class name of type {@code java.lang.String}
   */
  public String getClassName() {
    return className;
  }

  /**
   * Helper function to hold a specific inner type.Is empty if this is a class without generic parts
   * or a class has multiple generics.
   *
   * @return {@code java.util.Optional<TypeName>} if there is a single inner generic type, otherwise
   *     {@code java.util.Optional.EMPTY}
   */
  public Optional<TypeName> getInnerType() {
    return Optional.empty();
  }
}
