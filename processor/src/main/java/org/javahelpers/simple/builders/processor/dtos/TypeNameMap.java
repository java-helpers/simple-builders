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
 * Represents a type that implements the {@code java.util.Map} interface.
 *
 * <p>This includes the Map interface itself as well as any concrete implementations like HashMap,
 * LinkedHashMap, TreeMap, Hashtable, or custom Map implementations.
 *
 * <p>The concrete class name (e.g., "HashMap") is preserved in the package and class name fields,
 * while this subclass indicates that the type implements the Map interface.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code Map<String, Integer>} -&gt; TypeNameMap with 2 inner type arguments
 *   <li>{@code HashMap<String, Person>} -&gt; TypeNameMap with 2 inner type arguments
 *   <li>{@code Map} (raw type) -&gt; TypeNameMap with 0 inner type arguments
 * </ul>
 */
public class TypeNameMap extends TypeNameGeneric {

  private final boolean isConcreteImplementation;

  /**
   * Checks if the given package and class name represent the {@code java.util.Map} interface.
   *
   * @param packageName the package name
   * @param className the class name
   * @return {@code true} if this is {@code java.util.Map}
   */
  private static boolean isMapInterface(String packageName, String className) {
    return Strings.CI.equals(packageName, "java.util") && Strings.CI.equals(className, "Map");
  }

  /**
   * Creates a {@code TypeNameMap} based on another {@code TypeName} as outer type and a list of
   * inner type arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete Map
   *     implementation)
   * @param innerTypeArguments the list of generic type arguments (should be 0 or 2 for Map)
   */
  public TypeNameMap(TypeName outerType, List<TypeName> innerTypeArguments) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isMapInterface(getPackageName(), getClassName());
  }

  /**
   * Creates a {@code TypeNameMap} for the given package/class and a list of inner type arguments.
   *
   * @param packageName the package name
   * @param className the class name (the concrete Map implementation)
   * @param innerTypeArguments the list of generic type arguments (should be 0 or 2 for Map)
   */
  public TypeNameMap(String packageName, String className, List<TypeName> innerTypeArguments) {
    super(packageName, className, innerTypeArguments);
    this.isConcreteImplementation = !isMapInterface(packageName, className);
  }

  /**
   * Varargs convenience constructor with an outer {@code TypeName} and any number of inner type
   * arguments.
   *
   * @param outerType the outer type to use for package and class name (the concrete Map
   *     implementation)
   * @param innerTypeArguments variable number of generic type arguments (should be 0 or 2 for Map)
   */
  public TypeNameMap(TypeName outerType, TypeName... innerTypeArguments) {
    super(outerType, innerTypeArguments);
    this.isConcreteImplementation = !isMapInterface(getPackageName(), getClassName());
  }

  /**
   * Varargs convenience constructor with package/class names and any number of inner type
   * arguments.
   *
   * @param packageName the package name
   * @param className the class name (the concrete Map implementation)
   * @param innerTypeArguments variable number of generic type arguments (should be 0 or 2 for Map)
   */
  public TypeNameMap(String packageName, String className, TypeName... innerTypeArguments) {
    super(packageName, className, innerTypeArguments);
    this.isConcreteImplementation = !isMapInterface(packageName, className);
  }

  /**
   * Checks if this is a concrete Map implementation (not the interface itself).
   *
   * @return {@code true} for concrete implementations like HashMap, TreeMap, etc., {@code false} if
   *     this is {@code java.util.Map}
   */
  public boolean isConcreteImplementation() {
    return isConcreteImplementation;
  }

  /**
   * Checks if this Map type is properly parameterized (not a raw type).
   *
   * <p>A parameterized Map has exactly 2 type arguments (key and value types). A raw Map has 0 type
   * arguments.
   *
   * @return {@code true} if this Map has exactly 2 type arguments
   */
  public boolean isParameterized() {
    return getInnerTypeArguments().size() == 2;
  }

  /**
   * Gets the key type of this Map.
   *
   * <p>For a parameterized Map like {@code Map<String, Integer>}, this returns the String type.
   *
   * @return the key type (first type argument)
   * @throws IllegalStateException if this is a raw Map with no type arguments
   */
  public TypeName getKeyType() {
    if (!isParameterized()) {
      throw new IllegalStateException("Cannot get key type from raw Map type: " + getClassName());
    }
    return getInnerTypeArguments().get(0);
  }

  /**
   * Gets the value type of this Map.
   *
   * <p>For a parameterized Map like {@code Map<String, Integer>}, this returns the Integer type.
   *
   * @return the value type (second type argument)
   * @throws IllegalStateException if this is a raw Map with no type arguments
   */
  public TypeName getValueType() {
    if (!isParameterized()) {
      throw new IllegalStateException("Cannot get value type from raw Map type: " + getClassName());
    }
    return getInnerTypeArguments().get(1);
  }
}
