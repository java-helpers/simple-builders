/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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
package org.javahelpers.codegen.model.type;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a declared type that may carry one or more generic type arguments.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code List<T>} -&gt; outer: {@code java.util.List}, inner: {@code T}
 *   <li>{@code Map<K,V>} -&gt; outer: {@code java.util.Map}, inner: {@code K}, {@code V}
 *   <li>{@code Optional<Person>} -&gt; outer: {@code java.util.Optional}, inner: {@code Person}
 * </ul>
 *
 * This class is used throughout the codegen pipeline to model parameterized Java types in a uniform
 * way.
 */
public class TypeNameGeneric extends TypeName {
  /**
   * Checks whether any immediate type argument is a wildcard, rather than a writable element type.
   *
   * @return true if an immediate argument is a wildcard
   */
  @Override
  public boolean hasWildcardArguments() {
    return innerTypeArguments.stream().anyMatch(TypeNameWildcard.class::isInstance);
  }

  private final List<TypeName> innerTypeArguments;

  /** The raw (outer) type before generic parameterization; preserves nested-type structure. */
  private final TypeName outerType;

  /**
   * Creates a {@code TypeNameGeneric} based on another {@code TypeName} as outer type and a list of
   * inner type arguments.
   *
   * @param outerType the outer type to use for package and class name
   * @param innerTypeArguments the list of generic type arguments
   */
  public TypeNameGeneric(TypeName outerType, List<TypeName> innerTypeArguments) {
    super(outerType.getPackageName(), outerType.getClassName());
    this.outerType = outerType;
    this.innerTypeArguments = List.copyOf(innerTypeArguments);
  }

  /**
   * Creates a {@code TypeNameGeneric} for the given package/class and a list of inner type
   * arguments.
   *
   * @param packageName the package name
   * @param className the class name
   * @param innerTypeArguments the list of generic type arguments
   */
  public TypeNameGeneric(String packageName, String className, List<TypeName> innerTypeArguments) {
    super(packageName, className);
    this.outerType = new TypeName(packageName, className);
    this.innerTypeArguments = List.copyOf(innerTypeArguments);
  }

  /**
   * Creates a {@code TypeNameGeneric} with Varargs convenience constructor with an outer {@code
   * TypeName} and any number of inner type arguments.
   *
   * @param outerType the outer type to use for package and class name
   * @param innerTypeArguments variable number of generic type arguments
   */
  public TypeNameGeneric(TypeName outerType, TypeName... innerTypeArguments) {
    super(outerType.getPackageName(), outerType.getClassName());
    this.outerType = outerType;
    this.innerTypeArguments = List.of(innerTypeArguments);
  }

  /**
   * Varargs convenience constructor with package/class names and any number of inner type
   * arguments.
   *
   * @param packageName the package name
   * @param className the class name
   * @param innerTypeArguments variable number of generic type arguments
   */
  public TypeNameGeneric(String packageName, String className, TypeName... innerTypeArguments) {
    super(packageName, className);
    this.outerType = new TypeName(packageName, className);
    this.innerTypeArguments = List.of(innerTypeArguments);
  }

  /**
   * Creates a {@code TypeNameGeneric} for the given outer class and inner type classes.
   *
   * @param outerType the outer class (the raw generic type)
   * @param innerTypeArguments the generic type argument classes
   * @return a {@code TypeNameGeneric} representing {@code outerType<innerTypeArguments...>}
   */
  public static TypeNameGeneric of(Class<?> outerType, Class<?>... innerTypeArguments) {
    return new TypeNameGeneric(outerType, innerTypeArguments);
  }

  /**
   * Creates a {@code TypeNameGeneric} for the given outer type and inner type arguments.
   *
   * @param outerType the outer type (the raw generic type)
   * @param innerTypeArguments the generic type arguments
   * @return a {@code TypeNameGeneric} representing {@code outerType<innerTypeArguments...>}
   */
  public static TypeNameGeneric of(TypeName outerType, TypeName... innerTypeArguments) {
    return new TypeNameGeneric(outerType, innerTypeArguments);
  }

  /**
   * Varargs convenience constructor taking {@link Class} references directly.
   *
   * @param outerType the outer type
   * @param innerTypeArguments variable number of generic type arguments
   */
  public TypeNameGeneric(Class<?> outerType, Class<?>... innerTypeArguments) {
    this(
        TypeName.of(outerType),
        Arrays.stream(innerTypeArguments).map(TypeName::of).toArray(TypeName[]::new));
  }

  /**
   * Returns the raw type (without type parameters).
   *
   * @return the raw type
   */
  public TypeName getRawType() {
    return outerType;
  }

  @Override
  public boolean isNestedType() {
    return outerType.isNestedType();
  }

  /**
   * Returns the list of inner generic type arguments.
   *
   * @return an unmodifiable list of inner type arguments
   */
  public List<TypeName> getInnerTypeArguments() {
    return innerTypeArguments;
  }

  /**
   * Checks if this generic type has multiple inner type arguments.
   *
   * @return true if there is more than one inner type argument, false otherwise
   */
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

  @Override
  public String getFullQualifiedName() {
    String baseName = outerType.getFullQualifiedName();
    if (innerTypeArguments.isEmpty()) {
      return baseName;
    }

    String typeArgs =
        innerTypeArguments.stream()
            .map(TypeName::getFullQualifiedName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

    return baseName + "<" + typeArgs + ">";
  }

  @Override
  public String getSimpleNameWithGenerics() {
    String baseName = outerType.getSimpleNameWithGenerics();
    if (innerTypeArguments.isEmpty()) {
      return baseName;
    }

    String typeArgs =
        innerTypeArguments.stream()
            .map(TypeName::getSimpleNameWithGenerics)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

    return baseName + "<" + typeArgs + ">";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TypeNameGeneric that = (TypeNameGeneric) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(innerTypeArguments, that.innerTypeArguments)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(innerTypeArguments)
        .toHashCode();
  }
}
