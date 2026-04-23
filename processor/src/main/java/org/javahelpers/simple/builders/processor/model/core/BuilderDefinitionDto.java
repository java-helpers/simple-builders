/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.model.core;

import java.util.LinkedList;
import java.util.List;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Builder-specific extension of GenerationTargetClassDto.
 *
 * <p>This class holds all information for generating a builder, including builder-specific fields
 * and methods that are not part of the generic class generation.
 */
public class BuilderDefinitionDto extends GenerationTargetClassDto {
  /** Target type of result of building by builder. Containing package and name of DTO. */
  private TypeName buildingTargetTypeName;

  /**
   * Fields in constructor. Containing all fields which need to be given for calling the
   * constructor. Ordering in LinkedList is matching ordering of parameters in constructor call.
   * Instances of fields are added in LinkedList of all fields too.
   */
  private final List<FieldDto> fieldsInConstructor = new LinkedList<>();

  /**
   * List of all fields of target DTO which are supported by builder. A field could be supported by
   * multiple functions in builder.
   */
  private final List<FieldDto> fields = new LinkedList<>();

  /** Configuration for builder generation. */
  private BuilderConfiguration configuration;

  /**
   * Getting type of builder. Delegates to base class typeName.
   *
   * @return package and name of builder.
   */
  public TypeName getBuilderTypeName() {
    return getTypeName();
  }

  /**
   * Setting type of builder. Delegates to base class typeName.
   *
   * @param builderClassName type of builder
   */
  public void setBuilderTypeName(TypeName builderClassName) {
    setTypeName(builderClassName);
  }

  /**
   * Getting target type of result of building by builder.
   *
   * @return package and name of type of building result.
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
   * Adding a field-definition to builder definition.
   *
   * @param field to be added
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
   * Getting all defined fields in builder definition.
   *
   * @return list of fields
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
}
