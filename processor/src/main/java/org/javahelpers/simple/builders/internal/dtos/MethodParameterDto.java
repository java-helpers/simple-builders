package org.javahelpers.simple.builders.internal.dtos;

import java.util.Optional;

public class MethodParameterDto {
  private TypeName builderType;
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

  public Optional<TypeName> getBuilderType() {
    return Optional.ofNullable(builderType);
  }

  public void setBuilderType(TypeName builderType) {
    this.builderType = builderType;
  }
}
