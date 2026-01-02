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

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

  /**
   * Returns the fully qualified name of this interface.
   *
   * @return fully qualified name
   */
  public String getQualifiedName() {
    return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
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
        .append(annotations, that.annotations)
        .append(typeParameters, that.typeParameters)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(packageName)
        .append(simpleName)
        .append(annotations)
        .append(typeParameters)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("packageName", packageName)
        .append("simpleName", simpleName)
        .append("annotations", annotations)
        .append("typeParameters", typeParameters)
        .toString();
  }
}
