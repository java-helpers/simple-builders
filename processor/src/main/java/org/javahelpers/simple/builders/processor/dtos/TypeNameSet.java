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
 * Represents a type that implements the {@code java.util.Set} interface.
 *
 * <p>This includes the Set interface itself as well as any concrete implementations like HashSet,
 * LinkedHashSet, TreeSet, or custom Set implementations.
 *
 * <p>The concrete class name (e.g., "HashSet") is preserved in the package and class name fields,
 * while this subclass indicates that the type implements the Set interface.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code Set<String>} -&gt; TypeNameSet with 1 inner type argument
 *   <li>{@code HashSet<Person>} -&gt; TypeNameSet with 1 inner type argument
 *   <li>{@code Set} (raw type) -&gt; TypeNameSet with 0 inner type arguments
 * </ul>
 */
public class TypeNameSet extends TypeNameGeneric {

  private final boolean isConcreteImplementation;
  private final TypeName elementType;

  /**
   * Checks if the given package and class name represent the {@code java.util.Set} interface.
   *
   * @param packageName the package name
   * @param className the class name
   * @return {@code true} if this is {@code java.util.Set}
   */
  private static boolean isSetInterface(String packageName, String className) {
    return Strings.CI.equals(packageName, "java.util") && Strings.CI.equals(className, "Set");
  }

  /**
   * Creates a {@code TypeNameSet} based on another {@code TypeName} as outer type and a list of
   * inner type arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete Set
   *     implementation)
   * @param innerTypeArguments the list of generic type arguments (all class type parameters)
   * @param elementType the actual Set element type (extracted from Set interface)
   */
  public TypeNameSet(TypeName outerType, List<TypeName> innerTypeArguments, TypeName elementType) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isSetInterface(getPackageName(), getClassName());
    this.elementType = elementType;
  }

  /**
   * Creates a {@code TypeNameSet} based on another {@code TypeName} as outer type and a list of
   * inner type arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete Set
   *     implementation)
   * @param innerTypeArguments the list of generic type arguments (should be 0 or 1 for Set)
   */
  public TypeNameSet(TypeName outerType, List<TypeName> innerTypeArguments) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isSetInterface(getPackageName(), getClassName());
    this.elementType = innerTypeArguments.isEmpty() ? null : innerTypeArguments.get(0);
  }

  /**
   * Creates a {@code TypeNameSet} for the given package/class and a list of inner type arguments.
   *
   * @param packageName the package name
   * @param className the class name (the concrete Set implementation)
   * @param innerTypeArguments the list of generic type arguments (should be 0 or 1 for Set)
   */
  public TypeNameSet(String packageName, String className, List<TypeName> innerTypeArguments) {
    super(packageName, className, innerTypeArguments);
    this.isConcreteImplementation = !isSetInterface(packageName, className);
    this.elementType = innerTypeArguments.isEmpty() ? null : innerTypeArguments.get(0);
  }

  /**
   * Varargs convenience constructor with an outer {@code TypeName} and any number of inner type
   * arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete Set
   *     implementation)
   * @param innerTypeArguments variable number of generic type arguments (should be 0 or 1 for Set)
   */
  public TypeNameSet(TypeName outerType, TypeName... innerTypeArguments) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isSetInterface(getPackageName(), getClassName());
    this.elementType = innerTypeArguments.length == 0 ? null : innerTypeArguments[0];
  }

  /**
   * Varargs convenience constructor with package/class names and any number of inner type
   * arguments.
   *
   * @param packageName the package name
   * @param className the class name (the concrete Set implementation)
   * @param innerTypeArguments variable number of generic type arguments (should be 0 or 1 for Set)
   */
  public TypeNameSet(String packageName, String className, TypeName... innerTypeArguments) {
    super(packageName, className, innerTypeArguments);
    this.isConcreteImplementation = !isSetInterface(packageName, className);
    this.elementType = innerTypeArguments.length == 0 ? null : innerTypeArguments[0];
  }

  /**
   * Checks if this is a concrete Set implementation (not the interface itself).
   *
   * @return {@code true} for concrete implementations like HashSet, TreeSet, etc., {@code false} if
   *     this is {@code java.util.Set}
   */
  public boolean isConcreteImplementation() {
    return isConcreteImplementation;
  }

  /**
   * Checks if this Set type is properly parameterized (not a raw type).
   *
   * <p>A parameterized Set has exactly 1 type argument (the element type). A raw Set has 0 type
   * arguments.
   *
   * @return {@code true} if this Set has exactly 1 type argument
   */
  public boolean isParameterized() {
    return elementType != null;
  }

  /**
   * Gets the element type of this Set.
   *
   * <p>For a parameterized Set like {@code Set<String>}, this returns the String type.
   *
   * @return the element type (the single type argument)
   * @throws IllegalStateException if this is a raw Set with no type arguments
   */
  public TypeName getElementType() {
    if (elementType == null) {
      throw new IllegalStateException(
          "Cannot get element type from raw Set type: " + getClassName());
    }
    return elementType;
  }
}
