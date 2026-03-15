/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.model.annotation;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * InterfaceName represents a Java interface type with package and class name information.
 *
 * <p>This type is specifically designed for interfaces and contains only interface-relevant
 * information. Unlike {@link TypeName}, it doesn't include class-specific concepts like builders,
 * constructors, or inner types.
 *
 * <p>Typical usage includes:
 *
 * <ul>
 *   <li>IBuilderBase interface for builder contracts
 *   <li>Custom interfaces for builder extensions
 *   <li>Mixin interfaces for Jackson serialization
 * </ul>
 */
public class InterfaceName {

  /** Name of package. */
  private final String packageName;

  /** Name of interface. */
  private final String simpleName;

  /** Annotations on this interface (TYPE_USE). */
  private final List<AnnotationDto> annotations = new java.util.ArrayList<>();

  /** Generic type parameters for this interface. */
  private final List<TypeName> typeParameters = new java.util.ArrayList<>();

  /**
   * Constructor for InterfaceName.
   *
   * @param packageName name of package
   * @param simpleName name of interface, could not be null
   */
  public InterfaceName(String packageName, String simpleName) {
    requireNonNull(simpleName);
    this.packageName = packageName;
    this.simpleName = simpleName;
  }

  /**
   * Returns name of package.
   *
   * @return package name of type {@code java.lang.String}
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Returns name of interface.
   *
   * @return interface name of type {@code java.lang.String}
   */
  public String getSimpleName() {
    return simpleName;
  }

  /**
   * Returns the list of annotations on this interface.
   *
   * @return list of annotations
   */
  public List<AnnotationDto> getAnnotations() {
    return annotations;
  }

  /**
   * Adds an annotation to this interface.
   *
   * @param annotation the annotation to add
   */
  public void addAnnotation(AnnotationDto annotation) {
    this.annotations.add(annotation);
  }

  /**
   * Returns the list of generic type parameters for this interface.
   *
   * @return list of type parameters
   */
  public List<TypeName> getTypeParameters() {
    return typeParameters;
  }

  /**
   * Adds a generic type parameter to this interface.
   *
   * @param typeParameter the type parameter to add
   */
  public void addTypeParameter(TypeName typeParameter) {
    this.typeParameters.add(typeParameter);
  }

  /**
   * Returns whether this interface has generic type parameters.
   *
   * @return true if the interface has type parameters
   */
  public boolean hasTypeParameters() {
    return !typeParameters.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InterfaceName that = (InterfaceName) o;
    return new EqualsBuilder()
        .append(packageName, that.packageName)
        .append(simpleName, that.simpleName)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(packageName).append(simpleName).toHashCode();
  }

  /**
   * Returns a string representation of this interface in Java syntax format. Includes annotations,
   * package, simple name, and type parameters.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code com.example.Builder} - simple interface
   *   <li>{@code com.example.Builder<T>} - with type parameter
   *   <li>{@code @Deprecated com.example.Builder<T, R>} - with annotation and type parameters
   * </ul>
   *
   * @return fully qualified name with annotations and type parameters
   */
  @Override
  public String toString() {
    String annotationsPart =
        CollectionUtils.isNotEmpty(annotations)
            ? annotations.stream()
                    .map(AnnotationDto::toString)
                    .collect(java.util.stream.Collectors.joining(" "))
                + " "
            : "";

    String qualifiedName = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;

    String typeParamsPart =
        CollectionUtils.isNotEmpty(typeParameters)
            ? "<"
                + typeParameters.stream()
                    .map(TypeName::getClassName)
                    .collect(java.util.stream.Collectors.joining(", "))
                + ">"
            : "";

    return annotationsPart + qualifiedName + typeParamsPart;
  }
}
