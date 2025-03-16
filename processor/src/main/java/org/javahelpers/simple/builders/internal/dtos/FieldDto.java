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
