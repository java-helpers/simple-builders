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

import static java.util.Objects.requireNonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * TypeName of a nested (inner) class. Holds the enclosing type explicitly so arbitrarily deep
 * nesting (e.g. {@code Outer.Middle.Inner}) is represented structurally instead of by a dotted
 * class name alone.
 *
 * <p>{@link #getClassName()} returns the dotted name inside the package (e.g. {@code
 * "Outer.Inner"}) and {@link #getFullQualifiedName()} the full qualified name (e.g. {@code
 * "com.example.Outer.Inner"}). Rendering retains enclosing type arguments, while {@code
 * getClassName()} stays raw.
 */
public class TypeNameNested extends TypeName {

  /** The enclosing type; may itself be a {@link TypeNameNested} for deeper nesting. */
  private final TypeName enclosingType;

  private final String simpleName;

  /**
   * Constructor for TypeNameNested.
   *
   * @param enclosingType the type declaring this nested class, could not be null
   * @param simpleName the nested class's own name, could not be null
   */
  public TypeNameNested(TypeName enclosingType, String simpleName) {
    super(
        requireNonNull(enclosingType).getPackageName(),
        enclosingType.getClassName() + "." + requireNonNull(simpleName));
    this.enclosingType = enclosingType;
    this.simpleName = simpleName;
  }

  /**
   * Returns the enclosing type of this nested class.
   *
   * @return enclosing type, e.g. {@code Outer} for {@code Outer.Inner}
   */
  public TypeName getEnclosingType() {
    return enclosingType;
  }

  @Override
  public String getFullQualifiedName() {
    return enclosingType.getFullQualifiedName() + "." + simpleName;
  }

  @Override
  public String getSimpleNameWithGenerics() {
    return enclosingType.getSimpleNameWithGenerics() + "." + simpleName;
  }

  @Override
  public boolean isNestedType() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TypeNameNested that = (TypeNameNested) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(enclosingType, that.enclosingType)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(enclosingType)
        .toHashCode();
  }
}
