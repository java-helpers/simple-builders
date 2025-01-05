package org.javahelpers.simple.builders.internal.dtos;

import org.apache.commons.lang3.StringUtils;

public class SetterMethodDto extends MethodDto {
  public MethodParameterDto get(){
    return super.getParameters().getFirst();
  }
  
  public String getMethodName(){
    return StringUtils.removeStart(super.getMethodName(), "set");
  }
}
