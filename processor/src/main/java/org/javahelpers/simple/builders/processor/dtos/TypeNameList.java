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
import org.apache.commons.lang3.Strings;

/**
 * Represents a type that implements the {@code java.util.List} interface.
 *
 * <p>This includes the List interface itself as well as any concrete implementations like
 * ArrayList, LinkedList, Vector, Stack, or custom List implementations.
 *
 * <p>The concrete class name (e.g., "ArrayList") is preserved in the package and class name fields,
 * while this subclass indicates that the type implements the List interface.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code List<String>} -&gt; TypeNameList with 1 inner type argument
 *   <li>{@code ArrayList<Person>} -&gt; TypeNameList with 1 inner type argument
 *   <li>{@code List} (raw type) -&gt; TypeNameList with 0 inner type arguments
 * </ul>
 */
public class TypeNameList extends TypeNameGeneric {

  private final boolean isConcreteImplementation;
  private final TypeName elementType;

  /**
   * Checks if the given package and class name represent the {@code java.util.List} interface.
   *
   * @param packageName the package name
   * @param className the class name
   * @return {@code true} if this is {@code java.util.List}
   */
  private static boolean isListInterface(String packageName, String className) {
    return Strings.CI.equals(packageName, "java.util") && Strings.CI.equals(className, "List");
  }

  /**
   * Creates a {@code TypeNameList} based on another {@code TypeName} as outer type and a list of
   * inner type arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete List
   *     implementation)
   * @param innerTypeArguments the list of generic type arguments (all class type parameters)
   * @param elementType the actual List element type (extracted from List interface)
   */
  public TypeNameList(TypeName outerType, List<TypeName> innerTypeArguments, TypeName elementType) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isListInterface(getPackageName(), getClassName());
    this.elementType = elementType;
  }

  /**
   * Checks if this is a concrete List implementation (not the interface itself).
   *
   * @return {@code true} for concrete implementations like ArrayList, LinkedList, etc., {@code
   *     false} if this is {@code java.util.List}
   */
  public boolean isConcreteImplementation() {
    return isConcreteImplementation;
  }

  /**
   * Checks if this List type is properly parameterized (not a raw type).
   *
   * <p>A parameterized List has an element type extracted from the List interface. A raw List has
   * no element type.
   *
   * <p>This works correctly for both standard Lists like {@code List<String>} and custom
   * implementations like {@code CustomList<X,Y> extends ArrayList<Y>}.
   *
   * @return {@code true} if this List has an element type
   */
  public boolean isParameterized() {
    return elementType != null;
  }

  /**
   * Gets the element type of this List.
   *
   * <p>For a parameterized List like {@code List<String>}, this returns the String type.
   *
   * <p>For custom implementations like {@code CustomList<X,Y> extends ArrayList<Y>}, this returns Y
   * (the actual List element type), not X or both X and Y.
   *
   * @return the element type (extracted from the List interface)
   * @throws IllegalStateException if this is a raw List with no type arguments
   */
  public TypeName getElementType() {
    if (elementType == null) {
      throw new IllegalStateException(
          "Cannot get element type from raw List type: " + getClassName());
    }
    return elementType;
  }
}
