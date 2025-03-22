/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
