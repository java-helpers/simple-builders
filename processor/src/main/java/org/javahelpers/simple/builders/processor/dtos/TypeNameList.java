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
   * @param innerTypeArguments the list of generic type arguments (should be 0 or 1 for List)
   */
  public TypeNameList(TypeName outerType, List<TypeName> innerTypeArguments) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isListInterface(getPackageName(), getClassName());
  }

  /**
   * Creates a {@code TypeNameList} for the given package/class and a list of inner type arguments.
   *
   * @param packageName the package name
   * @param className the class name (the concrete List implementation)
   * @param innerTypeArguments the list of generic type arguments (should be 0 or 1 for List)
   */
  public TypeNameList(String packageName, String className, List<TypeName> innerTypeArguments) {
    super(packageName, className, innerTypeArguments);
    this.isConcreteImplementation = !isListInterface(packageName, className);
  }

  /**
   * Varargs convenience constructor with an outer {@code TypeName} and any number of inner type
   * arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete List
   *     implementation)
   * @param innerTypeArguments variable number of generic type arguments (should be 0 or 1 for List)
   */
  public TypeNameList(TypeName outerType, TypeName... innerTypeArguments) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isListInterface(getPackageName(), getClassName());
  }

  /**
   * Varargs convenience constructor with package/class names and any number of inner type
   * arguments.
   *
   * @param packageName the package name
   * @param className the class name (the concrete List implementation)
   * @param innerTypeArguments variable number of generic type arguments (should be 0 or 1 for List)
   */
  public TypeNameList(String packageName, String className, TypeName... innerTypeArguments) {
    super(packageName, className, innerTypeArguments);
    this.isConcreteImplementation = !isListInterface(packageName, className);
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
   * <p>A parameterized List has exactly 1 type argument (the element type). A raw List has 0 type
   * arguments.
   *
   * @return {@code true} if this List has exactly 1 type argument
   */
  public boolean isParameterized() {
    return getInnerTypeArguments().size() == 1;
  }

  /**
   * Gets the element type of this List.
   *
   * <p>For a parameterized List like {@code List<String>}, this returns the String type.
   *
   * @return the element type (the single type argument)
   * @throws IllegalStateException if this is a raw List with no type arguments
   */
  public TypeName getElementType() {
    if (!isParameterized()) {
      throw new IllegalStateException(
          "Cannot get element type from raw List type: " + getClassName());
    }
    return getInnerTypeArguments().get(0);
  }
}
