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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Definition of a field. Containing all information to generate methods in builder to set or modify
 * that field.
 */
public class FieldDto {
  /** Name of field in Builder. */
  private String fieldName;

  /** Name of field according to estimation by constructors and setters. */
  private String fieldNameEstimated;

  /** Type of field. Containing generic, name of package and class */
  private TypeName fieldType;

  /** List of all methods in builder, which provide helpers to change the field. */
  private final List<MethodDto> fieldSetterMethodsList = new ArrayList<>();

  /** Optional Javadoc of the field extracted from setter or constructor. */
  private String javaDoc;

  /** Method-level generics declared on the setter, to be reused in builder methods. */
  private final List<GenericParameterDto> fieldGenerics = new ArrayList<>();

  /**
   * The name of the getter method to call, because in case of boolean it does not need to start
   * with get (e.g., getName or isActive).
   */
  private String getterName;

  /**
   * Whether this field is marked as non-nullable via annotations like @NotNull, @NonNull, etc. For
   * constructor fields: must be set AND can't be null. For setter fields: if set, can't be null
   * (optional to set).
   */
  private boolean nonNullable = false;

  /**
   * Parameter-level annotations extracted from the field parameter (constructor or setter param).
   * These are separate from type-use annotations and should be applied to method parameters.
   */
  private final List<AnnotationDto> parameterAnnotations = new ArrayList<>();

  /**
   * Getting name of field.
   *
   * @return the name of the field
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Setting name of field.
   *
   * @param fieldName the name to set for this field
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Getting name of field estimated by constructor and setter.
   *
   * @return the estimated name of the field
   */
  public String getFieldNameEstimated() {
    return fieldNameEstimated;
  }

  /**
   * Setting name of field estimated by constructor and setter.
   *
   * @param fieldNameEstimated the estimated name to set for this field
   */
  public void setFieldNameEstimated(String fieldNameEstimated) {
    this.fieldNameEstimated = fieldNameEstimated;
  }

  /**
   * Getting name of setter method to use in build() method. This is always set during field
   * creation and represents the actual setter method name in the DTO.
   *
   * @return name of setter for field (never null)
   */
  public String getSetterName() {
    return "set" + StringUtils.capitalize(fieldNameEstimated);
  }

  /**
   * Getting type of field.
   *
   * @return {@code org.javahelpers.simple.builders.internal.dtos.Typename} of field
   */
  public TypeName getFieldType() {
    return fieldType;
  }

  /**
   * Setting type of field.
   *
   * @param fieldType field of type {@code org.javahelpers.simple.builders.internal.dtos.Typename}
   */
  public void setFieldType(TypeName fieldType) {
    this.fieldType = fieldType;
  }

  /**
   * Getting list of methods to modify that field.
   *
   * @return list of methods with type {@code
   *     org.javahelpers.simple.builders.internal.dtos.MethodDto}
   */
  public List<MethodDto> getMethods() {
    return fieldSetterMethodsList;
  }

  /**
   * Adds a method to field-definition.
   *
   * @param methodDto method definition
   */
  public void addMethod(MethodDto methodDto) {
    this.fieldSetterMethodsList.add(methodDto);
  }

  /**
   * Getting extracted Javadoc.
   *
   * @return Javadoc text or null if none present
   */
  public String getJavaDoc() {
    return javaDoc;
  }

  /**
   * Setting extracted Javadoc.
   *
   * @param javaDoc Javadoc text
   */
  public void setJavaDoc(String javaDoc) {
    this.javaDoc = javaDoc;
  }

  /**
   * Returns generics declared on the setter (field-specific).
   *
   * @return the list of generic parameters for this field
   */
  public List<GenericParameterDto> getFieldGenerics() {
    return fieldGenerics;
  }

  /**
   * Replaces generics list with the provided entries.
   *
   * @param generics the list of generic parameters to set
   */
  public void setFieldGenerics(List<GenericParameterDto> generics) {
    this.fieldGenerics.clear();
    if (generics != null) {
      this.fieldGenerics.addAll(generics);
    }
  }

  /**
   * Returns the getter method name to use (without parentheses).
   *
   * @return Optional containing the getter name, or empty if not set
   */
  public Optional<String> getGetterName() {
    return Optional.ofNullable(getterName);
  }

  /**
   * Sets the getter method name to use (without parentheses).
   *
   * @param getterName the getter method name to set
   */
  public void setGetterName(String getterName) {
    this.getterName = getterName;
  }

  /**
   * Checks if this field is marked as non-nullable.
   *
   * @return true if the field has @NotNull/@NonNull annotation
   */
  public boolean isNonNullable() {
    return nonNullable;
  }

  /**
   * Marks this field as non-nullable.
   *
   * @param nonNullable true if the field cannot be null
   */
  public void setNonNullable(boolean nonNullable) {
    this.nonNullable = nonNullable;
  }

  /**
   * Returns parameter-level annotations that should be applied to method parameters.
   *
   * @return list of parameter annotations
   */
  public List<AnnotationDto> getParameterAnnotations() {
    return parameterAnnotations;
  }

  /**
   * Sets parameter-level annotations from the field parameter.
   *
   * @param annotations list of annotations to set
   */
  public void setParameterAnnotations(List<AnnotationDto> annotations) {
    this.parameterAnnotations.clear();
    if (annotations != null) {
      this.parameterAnnotations.addAll(annotations);
    }
  }
}
