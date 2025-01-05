package org.javahelpers.simple.builders.internal.dtos;


import java.util.LinkedList;
import java.util.Optional;
import javax.lang.model.element.Modifier;

public class MethodDto {
  private Optional<Modifier> modifier = Optional.empty();
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

  public Optional<Modifier> getModifier() {
    return modifier;
  }

  public void setModifier(Modifier modifier) {
    this.modifier = Optional.ofNullable(modifier);
  }
}
