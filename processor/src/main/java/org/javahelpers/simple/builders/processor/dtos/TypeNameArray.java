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

import java.util.Optional;

/** TypeName is a specific array type. Holding name of class and package of inner type. */
public class TypeNameArray extends TypeName {
  private final TypeName typeOfArray;
  private final boolean fillsSet;

  /**
   * Constructor for simple classes.
   *
   * @param packageName name of package
   * @param className name of class, could not be null
   * @param isSet {@code true} if this array represents a set
   */
  public TypeNameArray(String packageName, String className, boolean isSet) {
    super(packageName, className);
    this.typeOfArray = new TypeName(packageName, className);
    this.fillsSet = isSet;
  }

  /**
   * Constructor with innerType.
   *
   * @param innerType name of package
   * @param isSet {@code true} if this array represents a set
   */
  public TypeNameArray(TypeName innerType, boolean isSet) {
    super(innerType.getPackageName(), innerType.getClassName());
    this.typeOfArray = innerType;
    this.fillsSet = isSet;
  }

  /**
   * Helper function to hold a specific inner type.Is empty if this is a class without generic parts
   * or a class has multiple generics.
   *
   * @return {@code java.util.Optional<TypeName>} if there is a single inner generic type, otherwise
   *     {@code java.util.Optional.EMPTY}
   */
  @Override
  public Optional<TypeName> getInnerType() {
    return Optional.of(typeOfArray);
  }

  /**
   * Helper function to return the type of objects in array.
   *
   * @return
   */
  public TypeName getTypeOfArray() {
    return typeOfArray;
  }

  /**
   * Getter for fillsSet.
   *
   * @return {@code true}, if this is array that should fill a set
   */
  public boolean isFillingSet() {
    return fillsSet;
  }
}
