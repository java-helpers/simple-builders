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

package org.javahelpers.simple.builders.processor.model.core;

import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;

/**
 * Encapsulates deprecation metadata detected from source DTO property elements.
 *
 * <p>This DTO is used during builder definition generation to carry deprecation information from
 * source elements (constructor parameter, record component, backing field) to the point where
 * generated builder methods are annotated. It is an analysis-side DTO and is not directly used for
 * class generation — the actual rendering uses the existing {@link
 * org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto#getAnnotations()
 * annotations list} and {@link org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto
 * javadoc} on the rendering DTOs.
 *
 * <p>The {@link #deprecatedAnnotation} preserves {@code @Deprecated(since = ..., forRemoval = ...)}
 * attributes. The {@link #deprecatedJavaDoc} carries the {@code @deprecated} Javadoc tag text.
 *
 * <p>Note: deprecation of setter and getter methods is not stored here. A deprecated setter or
 * getter does not mean the property itself is deprecated, so it is not a propagation source. The
 * suppression of compiler warnings from generated code that calls deprecated setters/getters is
 * handled separately in {@code applyDeprecationSuppressions} by checking the source elements
 * directly.
 *
 * @param deprecatedAnnotation the {@code @Deprecated} annotation DTO preserving {@code since} and
 *     {@code forRemoval} attributes, or {@code null} if not deprecated
 * @param deprecatedJavaDoc the {@code @deprecated} Javadoc text, or {@code null} if no explicit tag
 *     is present
 */
public record DeprecationInfoDto(AnnotationDto deprecatedAnnotation, String deprecatedJavaDoc) {

  /**
   * Returns whether this field is deprecated.
   *
   * @return {@code true} if a {@code @Deprecated} annotation is present
   */
  public boolean isDeprecated() {
    return deprecatedAnnotation != null;
  }

  /**
   * Returns the {@code @Deprecated} annotation DTO as an {@link Optional}.
   *
   * @return the annotation DTO, or empty if not deprecated
   */
  public Optional<AnnotationDto> getDeprecatedAnnotation() {
    return Optional.ofNullable(deprecatedAnnotation);
  }

  /**
   * Returns the {@code @deprecated} Javadoc text as an {@link Optional}.
   *
   * @return the deprecated Javadoc text, or empty if none present
   */
  public Optional<String> getDeprecatedJavaDoc() {
    return Optional.ofNullable(deprecatedJavaDoc);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("deprecatedAnnotation", deprecatedAnnotation)
        .append("deprecatedJavaDoc", deprecatedJavaDoc)
        .toString();
  }
}
