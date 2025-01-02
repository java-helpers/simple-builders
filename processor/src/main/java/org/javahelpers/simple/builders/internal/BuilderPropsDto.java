package org.javahelpers.simple.builders.internal;

import java.util.LinkedList;
import javax.lang.model.element.Name;

public class BuilderPropsDto {
  private Class<?> clazzForBuilder;
  private String builderClassName;
  private Name packageName;
  private final LinkedList<MemberDto> membersForBuilder = new LinkedList<>();

  public Class<?> getClazzForBuilder() {
    return clazzForBuilder;
  }

  public void setClazzForBuilder(Class<?> clazzForBuilder) {
    this.clazzForBuilder = clazzForBuilder;
  }

  public String getBuilderClassName() {
    return builderClassName;
  }

  public void setBuilderClassName(String builderClassName) {
    this.builderClassName = builderClassName;
  }

  public Name getPackageName() {
    return packageName;
  }

  public void setPackageName(Name packageName) {
    this.packageName = packageName;
  }

  public void addMember(MemberDto member) {
    membersForBuilder.add(member);
  }

  public LinkedList<MemberDto> getMembersForBuilder() {
    return membersForBuilder;
  }
}
