package org.javahelpers.simple.builders.internal.dtos;

import javax.lang.model.element.Name;

public class MethodParameterDto {
  private String parameterName;
  private TypeName parameterType;

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public TypeName getParameterType() {
    return parameterType;
  }

  public void setParameterTypeName(TypeName parameterType) {
    this.parameterType = parameterType;
  }
}
