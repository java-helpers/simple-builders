package org.javahelpers.simple.builders.internal.dtos;

import javax.lang.model.element.Name;

public class MethodParameterDto {
  private Name parameterName;
  private String parameterTypeName;

  public Name getParameterName() {
    return parameterName;
  }

  public void setParameterName(Name parameterName) {
    this.parameterName = parameterName;
  }

  public String getParameterTypeName() {
    return parameterTypeName;
  }

  public void setParameterTypeName(String parameterTypeName) {
    this.parameterTypeName = parameterTypeName;
  }
}
