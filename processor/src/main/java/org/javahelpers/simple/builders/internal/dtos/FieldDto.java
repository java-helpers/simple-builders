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

package org.javahelpers.simple.builders.internal.dtos;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;

public class FieldDto {
  private String fieldName;
  private TypeName fieldType;
  private final List<MethodDto> fieldSetterMethodsList = new ArrayList<>();

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public TypeName getFieldType() {
    return fieldType;
  }

  public void setFieldType(TypeName fieldType) {
    this.fieldType = fieldType;
  }

  public List<MethodDto> getFieldSetterMethodsList() {
    return fieldSetterMethodsList;
  }

  public void addFieldSetter(String methodName, TypeName parameterType, String parameterName) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(parameterName);
    parameter.setParameterTypeName(parameterType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(methodName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.PROXY);
    addFieldSetterMethodDto(methodDto);
  }

  public void addFieldConsumer(String methodName, TypeName parameterType, String parameterName) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(parameterName);
    parameter.setParameterTypeName(parameterType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(methodName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER);
    addFieldSetterMethodDto(methodDto);
  }

  public void addFieldConsumerByBuilder(
      String methodName, TypeName parameterType, String parameterName) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(parameterName);
    parameter.setParameterTypeName(parameterType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(methodName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER_BY_BUILDER);
    addFieldSetterMethodDto(methodDto);
  }

  public void addFieldSupplier(String methodName, TypeName parameterType, String parameterName) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(parameterName);
    parameter.setParameterTypeName(parameterType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(methodName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.SUPPLIER);
    addFieldSetterMethodDto(methodDto);
  }

  public void addFieldSetterMethodDto(MethodDto methodDto) {
    this.fieldSetterMethodsList.add(methodDto);
  }
}
