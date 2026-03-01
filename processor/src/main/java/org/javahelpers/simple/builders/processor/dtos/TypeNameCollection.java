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

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base class for types that implement Java collection interfaces (List, Set, etc.).
 *
 * <p>This class provides common functionality for single-element collections like List and Set,
 * including:
 *
 * <ul>
 *   <li>Tracking whether the type is a concrete implementation or the interface itself
 *   <li>Managing the element type of the collection
 *   <li>Common equals/hashCode implementation
 * </ul>
 *
 * <p>Subclasses should implement the interface-specific logic for determining whether a type
 * represents the core collection interface.
 */
public abstract class TypeNameCollection extends TypeNameGeneric {

  private final boolean isConcreteImplementation;
  private final TypeName elementType;

  /**
   * Creates a {@code TypeNameCollection} based on another {@code TypeName} as outer type, a list of
   * inner type arguments, and the actual element type.
   *
   * @param outerType the outer type to use for package and class name (the concrete collection
   *     implementation)
   * @param innerTypeArguments the list of generic type arguments (all class type parameters)
   * @param elementType the actual collection element type (extracted from collection interface)
   * @param interfaceClassName the simple class name of the collection interface (e.g., "List",
   *     "Set")
   */
  protected TypeNameCollection(
      TypeName outerType,
      List<TypeName> innerTypeArguments,
      TypeName elementType,
      String interfaceClassName) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation =
        !isCollectionInterface(getPackageName(), getClassName(), interfaceClassName);
    this.elementType = elementType;
  }

  /**
   * Checks if the given package and class name represent the specified collection interface.
   *
   * @param packageName the package name
   * @param className the class name
   * @param interfaceClassName the simple class name of the collection interface
   * @return {@code true} if this is the specified collection interface
   */
  private static boolean isCollectionInterface(
      String packageName, String className, String interfaceClassName) {
    return "java.util".equals(packageName) && interfaceClassName.equals(className);
  }

  /**
   * Checks if this is a concrete collection implementation (not the interface itself).
   *
   * @return {@code true} for concrete implementations like ArrayList, HashSet, etc., {@code false}
   *     if this is the collection interface
   */
  public boolean isConcreteImplementation() {
    return isConcreteImplementation;
  }

  /**
   * Checks if this collection type is properly parameterized (not a raw type).
   *
   * <p>A parameterized collection has an element type extracted from the collection interface. A
   * raw collection has no element type.
   *
   * @return {@code true} if this collection has an element type
   */
  public boolean isParameterized() {
    return elementType != null;
  }

  /**
   * Gets the element type of this collection.
   *
   * <p>For a parameterized collection like {@code List<String>}, this returns the String type.
   *
   * @return the element type (extracted from the collection interface)
   * @throws IllegalStateException if this is a raw collection with no type arguments
   */
  public TypeName getElementType() {
    if (elementType == null) {
      throw new IllegalStateException(
          "Cannot get element type from raw collection type: " + getClassName());
    }
    return elementType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TypeNameCollection that = (TypeNameCollection) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(isConcreteImplementation, that.isConcreteImplementation)
        .append(elementType, that.elementType)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(isConcreteImplementation)
        .append(elementType)
        .toHashCode();
  }
}
