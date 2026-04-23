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

package org.javahelpers.simple.builders.processor.model.imports;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;

/**
 * Represents a regular (non-static) import statement.
 *
 * <p>Example: {@code import com.example.MyClass;}
 */
public class RegularImport implements ImportStatement {

  private final TypeName type;

  /**
   * Creates a regular import from a TypeName.
   *
   * @param type the type to import
   */
  public RegularImport(TypeName type) {
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null");
    } else if (type instanceof TypeNameArray array) {
      this.type = array.getTypeOfArray();
    } else if (type instanceof TypeNameGeneric generic) {
      this.type = generic.getRawType();
    } else {
      this.type = type;
    }
  }

  /**
   * Returns the type being imported.
   *
   * @return the type
   */
  public TypeName getType() {
    return type;
  }

  @Override
  public String getFullyQualifiedName() {
    return type.getFullQualifiedName();
  }

  @Override
  public String getPackageName() {
    return type.getPackageName();
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RegularImport that = (RegularImport) o;
    return new EqualsBuilder().append(type, that.type).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(type).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("fqn", getFullyQualifiedName())
        .toString();
  }
}
