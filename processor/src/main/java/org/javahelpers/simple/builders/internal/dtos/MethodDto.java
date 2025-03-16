package org.javahelpers.simple.builders.internal.dtos;

import static org.javahelpers.simple.builders.internal.dtos.MethodTypes.PROXY;

import java.util.LinkedList;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public class MethodDto {
  private Optional<Modifier> modifier = Optional.empty();
  private MethodTypes methodType = PROXY;
  private String methodName;
  private String fullQualifiedType;
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  public MethodTypes getMethodType() {
    return methodType;
  }

  public void setMethodType(MethodTypes methodType) {
    this.methodType = methodType;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getFieldSetterMethodName() {
    return "set" + StringUtils.capitalize(this.getMethodName());
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
