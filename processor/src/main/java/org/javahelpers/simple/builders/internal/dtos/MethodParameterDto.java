package org.javahelpers.simple.builders.internal.dtos;

import javax.lang.model.element.Name;

public class MethodParameterDto {
  private Name parameterName;
  private TypeName parameterType;

  public Name getParameterName() {
    return parameterName;
  }

  public void setParameterName(Name parameterName) {
    this.parameterName = parameterName;
  }

  public TypeName getParameterType() {
    return parameterType;
  }

  public void setParameterTypeName(TypeName parameterType) {
    this.parameterType = parameterType;
  }
}
