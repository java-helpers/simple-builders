package org.javahelpers.simple.builders.internal.dtos;

import java.util.LinkedList;
import javax.lang.model.element.Name;

public class MethodDto {
  private Name memberName;
  private Name fullQualifiedType;
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  public Name getMemberName() {
    return memberName;
  }

  public void setMemberName(Name memberName) {
    this.memberName = memberName;
  }

  public Name getFullQualifiedType() {
    return fullQualifiedType;
  }

  public void setFullQualifiedType(Name fullQualifiedType) {
    this.fullQualifiedType = fullQualifiedType;
  }

  public void addParameter(MethodParameterDto paramDto) {
    this.parameters.add(paramDto);
  }
}
