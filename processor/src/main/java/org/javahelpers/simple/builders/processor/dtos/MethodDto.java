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

  /** Whether the method is static. */
  private boolean isStatic = false;

  /** Priority for method conflict resolution. Higher wins. */
  private int priority = 0;

  /** Ordering for method generation. Lower values appear first in generated class. */
  private int ordering = 1000; // Default high value for field-generated methods

  /** Name of method. */
  private String methodName;

  /** Return type of method. */
  private TypeName returnType;

  /** Javadoc comment for the method. */
  private String javadoc;

  /** List of annotations on this method. */
  private final List<AnnotationDto> annotations = new ArrayList<>();

  /** List of parameters of Method. */
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  /** List of generic type parameters for the method (e.g., <T, K, V>). */
  private final List<GenericParameterDto> genericParameters = new ArrayList<>();

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
   * Sets the ordering for this method.
   *
   * <p>Lower values appear first in the generated class. Methods with the same ordering and name
   * are sorted using the following enhanced rules:
   *
   * <ol>
   *   <li>Methods with fewer parameters come first
   *   <li>Non-generic methods come before generic methods
   *   <li>Full method signature (name(paramType1,paramType2,...)) used for final ordering
   * </ol>
   *
   * @param ordering the ordering value (lower values appear first)
   */
  public void setOrdering(int ordering) {
    this.ordering = ordering;
  }

  /**
   * Returns the ordering of this method.
   *
   * @return the ordering value
   */
  public int getOrdering() {
    return ordering;
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
   * @return modifier {@code java.util.Optional} access modifier of type {@code
   *     javax.lang.model.element.Modifier}
   */
  public Optional<Modifier> getModifier() {
    return modifier;
  }

  /**
   * Sets the access modifier for method.
   *
   * @param modifier access modifier of type {@code javax.lang.model.element.Modifier}
   */
  public void setModifier(Modifier modifier) {
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
  public void addAnnotation(
      org.javahelpers.simple.builders.processor.dtos.AnnotationDto annotation) {
    this.annotations.add(annotation);
  }

  /**
   * Comparator for sorting MethodDto instances with sophisticated ordering rules.
   *
   * <p>Sorting order for methods with same priority and name:
   *
   * <ol>
   *   <li>Methods with fewer parameters come first
   *   <li>Non-generic methods come before generic methods
   *   <li>Full method signature (name(paramType1,paramType2,...)) used for final ordering
   * </ol>
   */
  public static class MethodComparator implements java.util.Comparator<MethodDto> {

    @Override
    public int compare(MethodDto m1, MethodDto m2) {
      // Primary sort: ordering value
      int orderingCompare = Integer.compare(m1.getOrdering(), m2.getOrdering());
      if (orderingCompare != 0) {
        return orderingCompare;
      }

      // Secondary sort: method name
      int nameCompare = m1.getMethodName().compareTo(m2.getMethodName());
      if (nameCompare != 0) {
        return nameCompare;
      }

      // Tertiary sort: parameter count (fewer parameters first)
      int paramCountCompare = Integer.compare(m1.getParameters().size(), m2.getParameters().size());
      if (paramCountCompare != 0) {
        return paramCountCompare;
      }

      // Quaternary sort: generic vs non-generic (non-generic first)
      boolean m1Generic = hasGenericParameters(m1);
      boolean m2Generic = hasGenericParameters(m2);
      if (m1Generic != m2Generic) {
        return m1Generic ? 1 : -1; // non-generic comes first
      }

      // Final sort: full method signature
      String signature1 = createMethodSignature(m1);
      String signature2 = createMethodSignature(m2);
      return signature1.compareTo(signature2);
    }

    /**
     * Creates a qualified name string for a TypeName.
     *
     * @param typeName the type name
     * @return qualified name using the type's own formatting logic
     */
    private String getQualifiedName(TypeName typeName) {
      return typeName.getFullQualifiedName();
    }

    /**
     * Creates a method signature string for sorting purposes.
     *
     * <p>The signature includes method name and parameter types in the format:
     * methodName(paramType1,paramType2,...)
     *
     * @param method the method to create signature for
     * @return signature string for comparison
     */
    private String createMethodSignature(MethodDto method) {
      StringBuilder signature = new StringBuilder(method.getMethodName());
      signature.append("(");

      java.util.List<String> paramTypes =
          method.getParameters().stream()
              .map(param -> getQualifiedName(param.getParameterType()))
              .toList();

      signature.append(String.join(",", paramTypes));
      signature.append(")");

      return signature.toString();
    }

    /**
     * Checks if a method has generic parameters.
     *
     * @param method the method to check
     * @return true if any parameter is generic (contains type parameters)
     */
    private boolean hasGenericParameters(MethodDto method) {
      return method.getParameters().stream()
          .anyMatch(param -> getQualifiedName(param.getParameterType()).contains("<"));
    }
  }
}
