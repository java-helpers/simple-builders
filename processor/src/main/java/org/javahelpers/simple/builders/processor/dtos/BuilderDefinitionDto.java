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

package org.javahelpers.simple.builders.processor.dtos;

import java.util.LinkedList;
import java.util.List;

/** BuilderDefinitionDto holds all information for generating a builder. */
public class BuilderDefinitionDto {
  /** Target type of result of building by builder. Containing package and name of DTO. */
  private TypeName buildingTargetTypeName;

  /** Type of builder. Containing package and name of DTO. */
  private TypeName builderTypeName;

  /** Generic parameters declared on the target DTO (e.g., <T extends Number, U>). */
  private final List<GenericParameterDto> generics = new LinkedList<>();

  /**
   * List of all methods of target DTO which are supported by builder and not targeting a specific
   * DTO field.
   */
  private final LinkedList<MethodDto> methodsForBuilder = new LinkedList<>();

  /**
   * List of all fields of target DTO which are supported by builder. A field could be supported by
   * several functions in builder.
   */
  private final LinkedList<FieldDto> setterFieldsForBuilder = new LinkedList<>();

  /**
   * Getting type of builder.
   *
   * @return {@code org.javahelpers.simple.builders.internal.dtos.Typename} package and name of
   *     builder.
   */
  public TypeName getBuilderTypeName() {
    return builderTypeName;
  }

  /**
   * Setting type of builder.
   *
   * @param builderClassName type of builder
   */
  public void setBuilderTypeName(TypeName builderClassName) {
    this.builderTypeName = builderClassName;
  }

  /**
   * Getting target type of result of building by builder.
   *
   * @return {@code org.javahelpers.simple.builders.internal.dtos.Typename} package and name of type
   *     of building result.
   */
  public TypeName getBuildingTargetTypeName() {
    return buildingTargetTypeName;
  }

  /**
   * Setting target type of result of building by builder.
   *
   * @param buildingTargetTypeName package and name of type of building result
   */
  public void setBuildingTargetTypeName(TypeName buildingTargetTypeName) {
    this.buildingTargetTypeName = buildingTargetTypeName;
  }

  /**
   * Adding a method-definition to builder defintion.
   *
   * @param member {@code org.javahelpers.simple.builders.internal.dtos.MethodDto} to be added
   */
  public void addMethod(MethodDto member) {
    methodsForBuilder.add(member);
  }

  /**
   * Getting all definied methods in builder definition.
   *
   * @return list of methods with type {@code
   *     org.javahelpers.simple.builders.internal.dtos.MethodDto}
   */
  public LinkedList<MethodDto> getMethodsForBuilder() {
    return methodsForBuilder;
  }

  /**
   * Adding a field-definition to builder defintion.
   *
   * @param field {@code org.javahelpers.simple.builders.internal.dtos.FieldDto} to be added
   */
  public void addField(FieldDto field) {
    setterFieldsForBuilder.add(field);
  }

  /**
   * Getting all definied fields in builder definition.
   *
   * @return list of fields with type {@code org.javahelpers.simple.builders.internal.dtos.FieldDto}
   */
  public LinkedList<FieldDto> getSetterFieldsForBuilder() {
    return setterFieldsForBuilder;
  }

  /** Adds a generic parameter definition. */
  public void addGeneric(GenericParameterDto generic) {
    generics.add(generic);
  }

  /** Returns the list of generic parameters of the target DTO, in declared order. */
  public List<GenericParameterDto> getGenerics() {
    return generics;
  }
}
