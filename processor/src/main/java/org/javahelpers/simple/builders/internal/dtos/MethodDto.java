package org.javahelpers.simple.builders.internal.dtos;

import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.LinkedList;
import javax.lang.model.element.Modifier;

public class MethodDto {
  private Modifier modifier = PUBLIC;
  private String methodName;
  private String fullQualifiedType;
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getFullQualifiedType() {
    return fullQualifiedType;
  }

  public void setFullQualifiedType(String fullQualifiedType) {
    this.fullQualifiedType = fullQualifiedType;
  }

  public void addParameter(MethodParameterDto paramDto) {
    this.parameters.add(paramDto);
  }

  public LinkedList<MethodParameterDto> getParameters() {
    return parameters;
  }

  public Modifier getModifier() {
    return modifier;
  }

  public void setModifier(Modifier modifier) {
    this.modifier = modifier;
  }
}
