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

/** MethodParameterDto contains all information for generating parameters in method headers. */
public class MethodParameterDto {
  /** Name of parameter. */
  private String parameterName;

  /** Type of parameter. */
  private TypeName parameterType;

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
}
