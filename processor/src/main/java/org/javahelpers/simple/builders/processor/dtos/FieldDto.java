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

/**
 * Definition of a field. Containing all information to generate methods in builder to set or modify
 * that field.
 */
public class FieldDto {
  /** Name of field. */
  private String fieldName;

  /** Type of field. Containing generic, name of package and class */
  private TypeName fieldType;

  /** List of all methods in builder, which change the field. */
  private final List<MethodDto> fieldSetterMethodsList = new ArrayList<>();

  /**
   * Getting name of field.
   *
   * @return
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Setting name of field.
   *
   * @param fieldName
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Getting type of field.
   *
   * @return {@code org.javahelpers.simple.builders.internal.dtos.Typename} of field
   */
  public TypeName getFieldType() {
    return fieldType;
  }

  /**
   * Setting type of field.
   *
   * @param fieldType field of type {@code org.javahelpers.simple.builders.internal.dtos.Typename}
   */
  public void setFieldType(TypeName fieldType) {
    this.fieldType = fieldType;
  }

  /**
   * Getting list of methods to modify that field.
   *
   * @return list of methods with type {@code
   *     org.javahelpers.simple.builders.internal.dtos.MethodDto}
   */
  public List<MethodDto> getMethods() {
    return fieldSetterMethodsList;
  }

  /**
   * Adds a method to field-definition.
   *
   * @param methodDto method definition
   */
  public void addMethod(MethodDto methodDto) {
    this.fieldSetterMethodsList.add(methodDto);
  }
}
