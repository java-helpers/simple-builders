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

package org.javahelpers.simple.builders.processor.model.method;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.type.GenericParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Rendering-side DTO containing all information to generate a method in a generated class.
 *
 * <p>This DTO is consumed by the code generator. It is generation-specific in the sense that it
 * holds only rendering-relevant data — no field-origin metadata or other generation-phase-only
 * information.
 */
public class MethodDto {
  /** Access modifier for method. */
  private Optional<AccessModifier> modifier = Optional.empty();

  /** Whether the method is static. */
  private boolean isStatic = false;

  /** Name of method. */
  private String methodName;

  /** Return type of method. */
  private TypeName returnType;

  /** Javadoc comment for the method. */
  private JavadocDto javadoc;

  /** List of annotations on this method. */
  private final List<AnnotationDto> annotations = new ArrayList<>();

  /** List of parameters of Method. */
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  /** List of generic type parameters for the method (e.g., <T, K, V>). */
  private final List<GenericParameterDto> genericParameters = new ArrayList<>();

  /** Definition of inner implementation for method. */
  private final MethodCodeDto methodCodeDto = new MethodCodeDto();

  /** Default constructor. */
  public MethodDto() {
    // Default constructor
  }

  /**
   * Constructor with method name and return type.
   *
   * @param methodName the name of the method
   * @param returnType the return type of the method
   */
  public MethodDto(String methodName, TypeName returnType) {
    this.methodName = methodName;
    this.returnType = returnType;
  }

  /**
   * Setting the inner implementation of a method. Supports placeholders which has to be set by
   * addArgument.
   *
   * @param codeFormat Codeformat with placeholders
   */
  public void setCode(String codeFormat) {
    methodCodeDto.setCodeFormat(codeFormat);
  }

  /**
   * Adding the value for a text - placeholder.
   *
   * @param name name of placeholder
   * @param value dynamic value of placeholder
   */
  public void addArgument(String name, String value) {
    methodCodeDto.addArgument(name, value);
  }

  /**
   * Adding the value for a type - placeholder.
   *
   * @param name name of placeholder
   * @param value dynamic value of placeholder
   */
  public void addArgument(String name, TypeName value) {
    methodCodeDto.addArgument(name, value);
  }

  /**
   * Getter for inner implementation of method.
   *
   * @return {@code MethodCodeDto} containing definition of implementation
   */
  public MethodCodeDto getMethodCodeDto() {
    return methodCodeDto;
  }

  /**
   * Checks if the method has a code block.
   *
   * @return true if the method has a code block, false otherwise
   */
  public boolean hasCode() {
    return methodCodeDto.hasCode();
  }

  /**
   * Getting name of method.
   *
   * @return name with type {@code java.lang.String}
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Setting name of method.
   *
   * @param methodName name with type {@code java.lang.String}
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Adding a further parameter of method.
   *
   * @param paramDto parameter to be added of type {@code
   *     rg.javahelpers.simple.builders.internal.dtos.MethodParameterDto}
   */
  public void addParameter(MethodParameterDto paramDto) {
    this.parameters.add(paramDto);
  }

  /**
   * Getting a list of parameters of method.
   *
   * @return List of parameters of type {@code
   *     rg.javahelpers.simple.builders.internal.dtos.MethodParameterDto}
   */
  public List<MethodParameterDto> getParameters() {
    return parameters;
  }

  /**
   * Adds a generic type parameter to this method.
   *
   * @param genericParameter the generic parameter to add
   */
  public void addGenericParameter(GenericParameterDto genericParameter) {
    this.genericParameters.add(genericParameter);
  }

  /**
   * Getting a list of generic type parameters of method.
   *
   * @return List of generic parameters of type {@code GenericParameterDto}
   */
  public List<GenericParameterDto> getGenericParameters() {
    return genericParameters;
  }

  /**
   * Getting the access modifier for method. Optional for usage in stream-notation.
   *
   * @return modifier {@code java.util.Optional} access modifier of type {@code AccessModifier}
   */
  public Optional<AccessModifier> getModifier() {
    return modifier;
  }

  /**
   * Sets the access modifier for method.
   *
   * @param modifier access modifier of type {@code AccessModifier}
   */
  public void setModifier(AccessModifier modifier) {
    this.modifier = Optional.ofNullable(modifier);
  }

  /**
   * Returns whether this method is static.
   *
   * @return true if the method is static, false otherwise
   */
  public boolean isStatic() {
    return isStatic;
  }

  /**
   * Sets whether this method is static.
   *
   * @param isStatic true if the method should be static, false otherwise
   */
  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  /**
   * Gets the return type of the method.
   *
   * @return the return type as TypeName
   */
  public TypeName getReturnType() {
    return returnType;
  }

  /**
   * Sets the return type of the method.
   *
   * @param returnType the return type as TypeName
   */
  public void setReturnType(TypeName returnType) {
    this.returnType = returnType;
  }

  /**
   * Returns a unique signature key for the method based on name and parameter types. Used for
   * conflict resolution. The signature matches Java's method signature rules (name + parameter
   * types, ignoring generics due to type erasure).
   *
   * @return the signature key (e.g., "fieldName(java.lang.String,java.util.List)")
   */
  public String getSignatureKey() {
    StringBuilder sb = new StringBuilder();
    sb.append(methodName).append('(');
    for (int i = 0; i < parameters.size(); i++) {
      if (i > 0) sb.append(',');
      TypeName tn = parameters.get(i).getParameterType();
      // Handle null package names
      if (StringUtils.isNoneBlank(tn.getPackageName())) {
        sb.append(tn.getPackageName()).append('.');
      }
      sb.append(tn.getClassName());
    }
    sb.append(')');
    return sb.toString();
  }

  public JavadocDto getJavadoc() {
    return javadoc;
  }

  /**
   * Sets the Javadoc comment for the method.
   *
   * @param javadoc the Javadoc comment
   */
  public void setJavadoc(JavadocDto javadoc) {
    this.javadoc = javadoc;
  }

  /**
   * Returns the list of annotations on this method.
   *
   * @return list of annotations
   */
  public List<AnnotationDto> getAnnotations() {
    return annotations;
  }

  /**
   * Adds an annotation to this method.
   *
   * @param annotation the annotation to add
   */
  public void addAnnotation(AnnotationDto annotation) {
    this.annotations.add(annotation);
  }

  /**
   * Returns a string representation of this method in Java method signature format.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code public GeneratedClass name(String)}
   *   <li>{@code public GeneratedClass age(int)}
   *   <li>{@code public GeneratedClass tags(Consumer<ListBuilder<String>>)}
   * </ul>
   *
   * @return method signature as a string
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // Add modifier if present
    modifier.ifPresent(m -> sb.append(m.toString().toLowerCase(Locale.ROOT)).append(" "));

    // Add static if applicable
    if (isStatic) {
      sb.append("static ");
    }

    // Add return type (void if not specified)
    String returnTypeName = returnType != null ? returnType.getClassName() : "void";
    sb.append(returnTypeName).append(" ");

    // Add method name and parameters
    String parameterList =
        parameters.stream()
            .map(param -> param.getParameterType().getClassName())
            .collect(java.util.stream.Collectors.joining(", "));

    sb.append(methodName).append("(").append(parameterList).append(")");

    return sb.toString();
  }
}
