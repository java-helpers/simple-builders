package org.javahelpers.simple.builders.internal;

import java.util.LinkedList;
import javax.lang.model.element.Name;

public class MemberDto {
  private Name memberName;
  private Name fullQualifiedType;
  private final LinkedList<BuilderParameterDto> parameters = new LinkedList<>();

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

  public void addParameter(BuilderParameterDto paramDto) {
    this.parameters.add(paramDto);
  }
}
