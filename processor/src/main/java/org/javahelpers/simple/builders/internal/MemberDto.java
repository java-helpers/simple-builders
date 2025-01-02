package org.javahelpers.simple.builders.internal;

import javax.lang.model.element.Name;

public class MemberDto {
  private Name memberName;
  private Name fullQualifiedType;
  private boolean builderAnnotation = false;

  public Name getMemberName() {
    return memberName;
  }

  public void setMemberName(Name memberName) {
    this.memberName = memberName;
  }

  public boolean hasBuilderAnnotation() {
    return builderAnnotation;
  }

  public void setHasBuilderAnnotation(boolean builderAnnotation) {
    this.builderAnnotation = builderAnnotation;
  }

  public Name getFullQualifiedType() {
    return fullQualifiedType;
  }

  public void setFullQualifiedType(Name fullQualifiedType) {
    this.fullQualifiedType = fullQualifiedType;
  }
}
