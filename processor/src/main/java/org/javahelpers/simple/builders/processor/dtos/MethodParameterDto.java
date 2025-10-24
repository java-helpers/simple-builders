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

import java.util.ArrayList;
import java.util.List;

/** MethodParameterDto contains all information for generating parameters in method headers. */
public class MethodParameterDto {
  /** Name of parameter. */
  private String parameterName;

  /** Type of parameter. */
  private TypeName parameterType;

  /** Annotations to be applied to this parameter. */
  private final List<AnnotationDto> annotations = new ArrayList<>();

  /**
   * Getting name of parameter
   *
   * @return name of parameter
   */
  public String getParameterName() {
    return parameterName;
  }

  /**
   * Setting name of parameter
   *
   * @param parameterName parameter name
   */
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  /**
   * Getting type of parameter
   *
   * @return type of parameter
   */
  public TypeName getParameterType() {
    return parameterType;
  }

  /**
   * Setting type of parameter
   *
   * @param parameterType type of parameter
   */
  public void setParameterTypeName(TypeName parameterType) {
    this.parameterType = parameterType;
  }

  /**
   * Returns the list of annotations to be applied to this parameter.
   *
   * @return the list of annotations
   */
  public List<AnnotationDto> getAnnotations() {
    return annotations;
  }

  /**
   * Adds an annotation to be applied to this parameter.
   *
   * @param annotation the annotation to add
   */
  public void addAnnotation(AnnotationDto annotation) {
    this.annotations.add(annotation);
  }
}
