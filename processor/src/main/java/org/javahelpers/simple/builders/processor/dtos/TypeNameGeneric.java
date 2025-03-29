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

/** TypeNameGeneric extends TypeName with one specific generic type. */
public class TypeNameGeneric extends TypeName {
  private final TypeName innerType;

  /**
   * Constructor for generic types, having just one generic inner type.
   *
   * @param outerType outer type of generic
   * @param innerType inner type of generic
   */
  public TypeNameGeneric(TypeName outerType, TypeName innerType) {
    super(outerType.getPackageName(), outerType.getClassName());
    requireNonNull(innerType);
    this.innerType = innerType;
  }

  /**
   * Constructor for generic types, having just one generic inner type.
   *
   * @param packageName name of package
   * @param className name of class
   * @param innerType inner type of generic
   */
  public TypeNameGeneric(String packageName, String className, TypeName innerType) {
    super(packageName, className);
    requireNonNull(innerType);
    this.innerType = innerType;
  }

  @Override
  public Optional<TypeName> getInnerType() {
    return Optional.of(innerType);
  }
}
