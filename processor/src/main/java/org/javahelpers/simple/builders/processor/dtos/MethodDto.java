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
import java.util.Optional;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

/** MethodDto containing all information to generate a method in builder. */
public class MethodDto {
  // Priority constants for method conflict resolution (higher values win)
  public static final int PRIORITY_HIGHEST = 100; // Direct setters, with() methods
  public static final int PRIORITY_HIGH = 80; // Supplier, transform methods
  public static final int PRIORITY_MEDIUM = 70; // Consumer, builder consumers
  public static final int PRIORITY_LOW = 60; // Specialized consumers

  /** Access modifier for method. */
  private Optional<Modifier> modifier = Optional.empty();

  /** Priority for method conflict resolution. Higher wins. */
  private int priority = 0;

  /** Name of method. */
  private String methodName;

  /** Return type of method. */
  private TypeName returnType;

  /** Javadoc comment for the method. */
  private String javadoc;

  /** List of parameters of Method. */
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  /** Definition of inner implementation for method. */
  private final MethodCodeDto methodCodeDto = new MethodCodeDto();

  /**
   * Sets the priority for this method. Higher values win when signatures clash. Priority levels:
   *
   * <ul>
   *   <li>{@link #PRIORITY_HIGHEST} (100): Direct setters, with() methods
   *   <li>{@link #PRIORITY_HIGH} (80): Supplier methods, transform methods (e.g., format, toArray)
   *   <li>{@link #PRIORITY_MEDIUM} (70): Consumer methods, builder consumers
   *   <li>{@link #PRIORITY_LOW} (60): Specialized consumers (e.g., StringBuilder)
   *   <li>0: Default (no priority set)
   * </ul>
   *
   * @param priority the priority value (higher values take precedence in conflicts)
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Returns the priority of this method for conflict resolution.
   *
   * @return the priority value
   */
  public int getPriority() {
    return priority;
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
   * Getting name of method.
   *
   * @return name with type {@code java.lang.String}
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Helper function to generate a name for setter method. Does not work on non-field methods.
   *
   * @return returning name of field-setter method
   */
  public String createFieldSetterMethodName() {
    return "set" + StringUtils.capitalize(this.getMethodName());
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
   * Getting the access modifier for method. Optional for usage in stream-notation.
   *
   * @return modifier {@code java.util.Optional} access modifier of type {@code
   *     javax.lang.model.element.Modifier}
   */
  public Optional<Modifier> getModifier() {
    return modifier;
  }

  /**
   * Setting the access modifier for method.
   *
   * @param modifier access modifier of type {@code javax.lang.model.element.Modifier}
   */
  public void setModifier(Modifier modifier) {
    this.modifier = Optional.ofNullable(modifier);
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

  public String getJavadoc() {
    return javadoc;
  }

  /**
   * Sets the Javadoc comment for the method.
   *
   * @param javadoc the Javadoc comment
   */
  public void setJavadoc(String javadoc) {
    this.javadoc = javadoc;
  }
}
