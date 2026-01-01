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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** BuilderDefinitionDto holds all information for generating a builder. */
public class BuilderDefinitionDto {
  /** Target type of result of building by builder. Containing package and name of DTO. */
  private TypeName buildingTargetTypeName;

  /** Type of builder. Containing package and name of DTO. */
  private TypeName builderTypeName;

  /**
   * Fields in constructor. Containing all fields which need to be given for calling the
   * constructor. Ordering in LinkedList is matching orderung of parameters in constructor call.
   * Instances of fields are added in LinkedList of all fields too.
   */
  private final List<FieldDto> fieldsInConstructor = new LinkedList<>();

  /** Generic parameters declared on the target DTO (e.g., {@code <T extends Number, U>}). */
  private final List<GenericParameterDto> generics = new LinkedList<>();

  /**
   * List of all fields of target DTO which are supported by builder. A field could be supported by
   * multiple functions in builder.
   */
  private final List<FieldDto> fields = new LinkedList<>();

  /**
   * Nested types (interfaces or classes) to be generated inside the builder, such as the "With"
   * interface.
   */
  private final List<NestedTypeDto> nestedTypes = new LinkedList<>();

  /**
   * Core builder methods (build, create, conditional, toString, etc.) to be generated. These are
   * added by BuilderEnhancers and have ordering for generation sequence.
   */
  private final List<MethodDto> coreMethods = new LinkedList<>();

  /**
   * Class-level annotations to be added to the generated builder class. These are added by
   * BuilderEnhancers and include annotations like @Generated, @BuilderImplementation, etc.
   *
   * <p>Uses a Set to ensure annotation uniqueness and prevent duplicates.
   */
  private final Set<AnnotationDto> classAnnotations = new LinkedHashSet<>();

  /**
   * Interfaces to be implemented by the generated builder class. These are added by
   * BuilderEnhancers and include interfaces like IBuilderBase.
   *
   * <p>Uses a Set to ensure interface uniqueness and prevent duplicates.
   */
  private final Set<InterfaceName> interfaces = new LinkedHashSet<>();

  /** Class-level JavaDoc for the generated builder class. */
  private String classJavadoc;

  /** Configuration for builder generation. */
  private BuilderConfiguration configuration;

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
   * Adding a field-definition to builder defintion.
   *
   * @param field {@code org.javahelpers.simple.builders.internal.dtos.FieldDto} to be added
   */
  public void addField(FieldDto field) {
    fields.add(field);
  }

  /**
   * Adds a field definition that will be provided via the DTO constructor.
   *
   * @param field the field to add to the constructor parameters
   */
  public void addFieldInConstructor(FieldDto field) {
    fieldsInConstructor.add(field);
  }

  /**
   * Adds multiple field definitions to be provided via the DTO constructor.
   *
   * @param fields the list of fields to add to the constructor parameters
   */
  public void addAllFieldsInConstructor(List<FieldDto> fields) {
    fieldsInConstructor.addAll(fields);
  }

  /**
   * Adds multiple field definitions to the builder.
   *
   * @param fields the list of fields to add
   */
  public void addAllFields(List<FieldDto> fields) {
    this.fields.addAll(fields);
  }

  /**
   * Getting all definied fields in builder definition.
   *
   * @return list of fields with type {@code org.javahelpers.simple.builders.internal.dtos.FieldDto}
   */
  public List<FieldDto> getSetterFieldsForBuilder() {
    return fields;
  }

  /**
   * Returns the ordered list of fields to be passed to the DTO constructor.
   *
   * @return the ordered list of constructor parameter fields
   */
  public List<FieldDto> getConstructorFieldsForBuilder() {
    return fieldsInConstructor;
  }

  /**
   * Returns all fields known to the builder in a single list. Constructor parameters appear first
   * (in constructor order), followed by setter-derived fields.
   *
   * @return all fields in the builder (constructor fields first, then setter fields)
   */
  public List<FieldDto> getAllFieldsForBuilder() {
    LinkedList<FieldDto> all = new LinkedList<>();
    all.addAll(fieldsInConstructor);
    all.addAll(fields);
    return all;
  }

  /**
   * Adds a generic parameter definition.
   *
   * @param generic the generic parameter to add
   */
  public void addGeneric(GenericParameterDto generic) {
    generics.add(generic);
  }

  /**
   * Returns the list of generic parameters of the target DTO, in declared order.
   *
   * @return the list of generic parameters
   */
  public List<GenericParameterDto> getGenerics() {
    return generics;
  }

  /**
   * Returns the list of nested types (interfaces or classes) to be generated inside the builder.
   *
   * @return the list of nested types
   */
  public List<NestedTypeDto> getNestedTypes() {
    return nestedTypes;
  }

  /**
   * Adds a nested type definition to the builder.
   *
   * @param nestedType the nested type to add
   */
  public void addNestedType(NestedTypeDto nestedType) {
    this.nestedTypes.add(nestedType);
  }

  /**
   * Returns the list of core builder methods.
   *
   * @return the list of core methods
   */
  public List<MethodDto> getCoreMethods() {
    return coreMethods;
  }

  /**
   * Adds a core method to be generated in the builder.
   *
   * @param method the core method to add
   */
  public void addCoreMethod(MethodDto method) {
    this.coreMethods.add(method);
  }

  /**
   * Returns the set of class-level annotations.
   *
   * @return the set of class annotations (unique, no duplicates)
   */
  public Set<AnnotationDto> getClassAnnotations() {
    return classAnnotations;
  }

  /**
   * Adds a class-level annotation to be generated in the builder.
   *
   * @param annotation the class annotation to add
   */
  public void addClassAnnotation(AnnotationDto annotation) {
    this.classAnnotations.add(annotation);
  }

  /**
   * Returns the set of interfaces to be implemented by the builder.
   *
   * @return the set of interfaces (unique, no duplicates)
   */
  public Set<InterfaceName> getInterfaces() {
    return interfaces;
  }

  /**
   * Adds an interface to be implemented by the builder.
   *
   * @param interfaceType the interface to add
   */
  public void addInterface(InterfaceName interfaceType) {
    this.interfaces.add(interfaceType);
  }

  /**
   * Returns the configuration for builder generation.
   *
   * @return the builder configuration
   */
  public BuilderConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Sets the builder configuration.
   *
   * @param configuration the builder configuration
   */
  public void setConfiguration(BuilderConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Returns the class-level JavaDoc for the generated builder.
   *
   * @return the class JavaDoc, or null if not set
   */
  public String getClassJavadoc() {
    return classJavadoc;
  }

  /**
   * Sets the class-level JavaDoc for the generated builder.
   *
   * @param classJavadoc the class JavaDoc to set
   */
  public void setClassJavadoc(String classJavadoc) {
    this.classJavadoc = classJavadoc;
  }
}
