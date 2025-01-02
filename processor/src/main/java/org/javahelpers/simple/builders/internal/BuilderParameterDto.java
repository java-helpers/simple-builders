package org.javahelpers.simple.builders.internal;

import javax.lang.model.element.Name;

public class BuilderParameterDto {
  private Name parameterName;
  private Name parameterTypeName;

  public Name getParameterName() {
    return parameterName;
  }

  public void setParameterName(Name parameterName) {
    this.parameterName = parameterName;
  }

  public Name getParameterTypeName() {
    return parameterTypeName;
  }

  public void setParameterTypeName(Name parameterTypeName) {
    this.parameterTypeName = parameterTypeName;
  }
}
