package org.javahelpers.simple.builders.internal.dtos;

import java.util.LinkedList;
import javax.lang.model.element.Name;

public class BuilderDefinitionDto {
  private Class<?> clazzForBuilder;
  private String builderClassName;
  private Name packageName;
  private final LinkedList<MethodDto> methodsForBuilder = new LinkedList<>();

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

  public void addMethod(MethodDto member) {
    methodsForBuilder.add(member);
  }

  public LinkedList<MethodDto> getMethodsForBuilder() {
    return methodsForBuilder;
  }
}
