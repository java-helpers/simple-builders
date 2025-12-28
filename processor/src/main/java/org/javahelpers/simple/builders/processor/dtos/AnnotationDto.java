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

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * DTO representing an annotation to be copied from the target class field to the builder class
 * field. Contains the annotation type and its members in a plain format suitable for code
 * generation.
 */
public class AnnotationDto {
  /** The annotation type (fully qualified name). */
  private TypeName annotationType;

  /**
   * Annotation members (parameters) as key-value pairs. The values are stored as code strings ready
   * for generation (e.g., "\"value\"", "123", "{1, 2, 3}"). LinkedHashMap preserves declaration
   * order.
   */
  private final Map<String, String> members = new LinkedHashMap<>();

  /**
   * Gets the annotation type.
   *
   * @return the type of the annotation
   */
  public TypeName getAnnotationType() {
    return annotationType;
  }

  /**
   * Sets the annotation type.
   *
   * @param annotationType the type of the annotation
   */
  public void setAnnotationType(TypeName annotationType) {
    this.annotationType = annotationType;
  }

  /**
   * Gets the annotation members (parameters) as key-value pairs.
   *
   * @return map of member names to their code string values
   */
  public Map<String, String> getMembers() {
    return members;
  }

  /**
   * Adds an annotation member (parameter).
   *
   * @param name the member name
   * @param value the member value as a code string
   */
  public void addMember(String name, String value) {
    this.members.put(name, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AnnotationDto that = (AnnotationDto) o;

    return new EqualsBuilder()
        .append(annotationType, that.annotationType)
        .append(members, that.members)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(annotationType).append(members).toHashCode();
  }
}
