package org.javahelpers.simple.builders.internal.dtos;

import java.util.LinkedList;

public class BuilderDefinitionDto {
  private TypeName buildingTargetTypeName;
  private TypeName builderTypeName;
  private final LinkedList<MethodDto> methodsForBuilder = new LinkedList<>();
  private final LinkedList<FieldDto> setterFieldsForBuilder = new LinkedList<>();

  public TypeName getBuilderTypeName() {
    return builderTypeName;
  }

  public void setBuilderTypeName(TypeName builderClassName) {
    this.builderTypeName = builderClassName;
  }

  public TypeName getBuildingTargetTypeName() {
    return buildingTargetTypeName;
  }

  public void setBuildingTargetTypeName(TypeName buildingTargetTypeName) {
    this.buildingTargetTypeName = buildingTargetTypeName;
  }

  public void addMethod(MethodDto member) {
    methodsForBuilder.add(member);
  }

  public LinkedList<MethodDto> getMethodsForBuilder() {
    return methodsForBuilder;
  }

  public void addField(FieldDto field) {
    setterFieldsForBuilder.add(field);
  }

  public LinkedList<FieldDto> getSetterFieldsForBuilder() {
    return setterFieldsForBuilder;
  }
}
