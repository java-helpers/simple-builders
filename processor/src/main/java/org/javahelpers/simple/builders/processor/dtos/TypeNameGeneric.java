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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a declared type that may carry one or more generic type arguments.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>List<T> -> outer: {@code java.util.List}, inner: {@code T}
 *   <li>Map<K,V> -> outer: {@code java.util.Map}, inner: {@code K}, {@code V}
 *   <li>Optional<Person> -> outer: {@code java.util.Optional}, inner: {@code Person}
 * </ul>
 *
 * This class is used throughout the processor to model parameterized Java types in a uniform way.
 */
public class TypeNameGeneric extends TypeName {
  private final List<TypeName> innerTypeArguments;

  /**
   * Creates a {@code TypeNameGeneric} based on another {@code TypeName} as outer type and a list of
   * inner type arguments.
   */
  public TypeNameGeneric(TypeName outerType, List<TypeName> innerTypeArguments) {
    super(outerType.getPackageName(), outerType.getClassName());
    this.innerTypeArguments = List.copyOf(innerTypeArguments);
  }

  /**
   * Creates a {@code TypeNameGeneric} for the given package/class and a list of inner type
   * arguments.
   */
  public TypeNameGeneric(String packageName, String className, List<TypeName> innerTypeArguments) {
    super(packageName, className);
    this.innerTypeArguments = List.copyOf(innerTypeArguments);
  }

  /**
   * Creates a {@code TypeNameGeneric} with Varargs convenience constructor with an outer {@code
   * TypeName} and any number of inner type arguments.
   */
  public TypeNameGeneric(TypeName outerType, TypeName... innerTypeArguments) {
    super(outerType.getPackageName(), outerType.getClassName());
    this.innerTypeArguments = List.of(innerTypeArguments);
  }

  /**
   * Varargs convenience constructor with package/class names and any number of inner type
   * arguments.
   */
  public TypeNameGeneric(String packageName, String className, TypeName... innerTypeArguments) {
    super(packageName, className);
    this.innerTypeArguments = List.of(innerTypeArguments);
  }

  public List<TypeName> getInnerTypeArguments() {
    return Collections.unmodifiableList(innerTypeArguments);
  }

  public boolean hasMultipleInnerTypes() {
    return innerTypeArguments.size() > 1;
  }

  @Override
  public Optional<TypeName> getInnerType() {
    // Backward compatibility: expose the first type arg when exactly one is present
    return innerTypeArguments.size() == 1
        ? Optional.of(innerTypeArguments.get(0))
        : Optional.empty();
  }
}
