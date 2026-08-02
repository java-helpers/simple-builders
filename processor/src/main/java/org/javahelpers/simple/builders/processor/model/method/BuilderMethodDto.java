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
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocCodeBlockDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.type.GenericParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * BuilderMethodDto containing all information for generating a method in the builder class,
 * including field-origin metadata for pre-conflict-resolution logging and javadoc enrichment.
 *
 * <p>This is the generation-side DTO. It is produced by generators and enhancers, and mapped to
 * {@link MethodDto} (the rendering DTO) by {@link
 * org.javahelpers.simple.builders.processor.model.core.BuilderToGenerationTypeMapper} before being
 * added to {@code GenerationTargetClassDto}.
 */
public class BuilderMethodDto {
  // Priority constants for method conflict resolution (higher values win)
  public static final int PRIORITY_HIGHEST = 100; // Direct setters, with() methods
  public static final int PRIORITY_HIGH = 80; // Supplier, transform methods
  public static final int PRIORITY_MEDIUM = 70; // Consumer, builder consumers
  public static final int PRIORITY_LOW = 60; // Specialized consumers

  /** Access modifier for method. */
  private Optional<AccessModifier> modifier = Optional.empty();

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
  private JavadocDto javadoc;

  /** List of annotations on this method. */
  private final List<AnnotationDto> annotations = new ArrayList<>();

  /** List of parameters of Method. */
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  /** List of generic type parameters for the method (e.g., <T, K, V>). */
  private final List<GenericParameterDto> genericParameters = new ArrayList<>();

  /** Definition of inner implementation for method. */
  private final MethodCodeDto methodCodeDto = new MethodCodeDto();

  /**
   * Fluent-chain fragment describing how this method is invoked in Javadoc examples (e.g., {@code
   * .title("example value")}). When present, downstream enhancers use it to synthesise the
   * method-level example block (as {@code builder<fragment>;}) and to aggregate the class-level
   * kitchen-sink chain. A {@code null} value means the method should not appear in either example.
   */
  private String exampleChainFragment;

  /** Name of the source field this method was generated for. {@code null} for enhancer methods. */
  private String sourceFieldName;

  /**
   * Source method signature for javadoc enrichment, e.g. {@code setTeamname(String teamName)} for
   * setter fields or {@code PersonDto(String name, int age, ...)} for constructor parameters.
   * {@code null} for enhancer methods.
   */
  private String sourceMethodSignature;

  /**
   * Types-only signature for the {@code {@link}} target, e.g. {@code setTeamname(String)} or {@code
   * PersonDto(String, int)}. Uses raw types (no generics) per Javadoc spec. {@code null} for
   * enhancer methods.
   */
  private String sourceMethodLinkSignature;

  /** Whether this method was generated for a constructor field (vs a setter field). */
  private boolean constructorField;

  /** Default constructor. */
  public BuilderMethodDto() {
    // Default constructor
  }

  /**
   * Constructor with method name and return type.
   *
   * @param methodName the name of the method
   * @param returnType the return type of the method
   */
  public BuilderMethodDto(String methodName, TypeName returnType) {
    this.methodName = methodName;
    this.returnType = returnType;
  }

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
   * Returns the fluent-chain fragment for Javadoc examples (e.g. {@code .title("example value")})
   * or {@code null} if this method should not participate in example generation.
   *
   * @return the fragment or {@code null}
   */
  public String getExampleChainFragment() {
    return exampleChainFragment;
  }

  /**
   * Stores the fluent-chain fragment describing how this method is invoked in examples.
   *
   * <p>The fragment contains just the method invocation, e.g. {@code title("example value")}.
   * Downstream enhancers synthesise the method-level example block (as {@code builder.<fragment>;})
   * and the class-level kitchen-sink chain from it.
   *
   * <p>Automatically adds a method-level example to the javadoc if javadoc exists and has no
   * existing examples (to avoid overriding manually set examples).
   *
   * @param exampleChainFragment the fragment or {@code null} to clear
   */
  public void setExampleChainFragment(String exampleChainFragment) {
    this.exampleChainFragment = exampleChainFragment;
    // Automatically add method-level example to javadoc if javadoc exists and has no examples
    if (exampleChainFragment != null && javadoc != null && javadoc.getCodeBlocks().isEmpty()) {
      JavadocCodeBlockDto methodExample = new JavadocCodeBlockDto();
      methodExample.setCodeFormat("builder.%s;".formatted(exampleChainFragment));
      javadoc.addExample(methodExample);
    }
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
   *     org.javahelpers.simple.builders.internal.dtos.MethodParameterDto}
   */
  public void addParameter(MethodParameterDto paramDto) {
    this.parameters.add(paramDto);
  }

  /**
   * Getting a list of parameters of method.
   *
   * @return List of parameters of type {@code
   *     org.javahelpers.simple.builders.internal.dtos.MethodParameterDto}
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
   * Returns the name of the source field this method was generated for.
   *
   * @return the source field name, or {@code null} for enhancer-generated methods
   */
  public String getSourceFieldName() {
    return sourceFieldName;
  }

  /**
   * Sets the name of the source field this method was generated for.
   *
   * @param sourceFieldName the source field name, or {@code null} for enhancer-generated methods
   */
  public void setSourceFieldName(String sourceFieldName) {
    this.sourceFieldName = sourceFieldName;
  }

  /**
   * Returns the source method signature for javadoc enrichment.
   *
   * @return the source method signature, or {@code null} for enhancer-generated methods
   */
  public String getSourceMethodSignature() {
    return sourceMethodSignature;
  }

  /**
   * Sets the source method signature for javadoc enrichment.
   *
   * @param sourceMethodSignature the source method signature, e.g. {@code setTeamname(String
   *     teamName)} for setters or {@code PersonDto(String name, int age, ...)} for constructor
   *     parameters
   */
  public void setSourceMethodSignature(String sourceMethodSignature) {
    this.sourceMethodSignature = sourceMethodSignature;
  }

  /**
   * Returns the types-only link signature for the {@code {@link}} target.
   *
   * @return the link signature, or {@code null} for enhancer-generated methods
   */
  public String getSourceMethodLinkSignature() {
    return sourceMethodLinkSignature;
  }

  /**
   * Sets the types-only link signature for the {@code {@link}} target.
   *
   * @param sourceMethodLinkSignature the link signature, e.g. {@code setTeamname(String)} or {@code
   *     PersonDto(String, int)}
   */
  public void setSourceMethodLinkSignature(String sourceMethodLinkSignature) {
    this.sourceMethodLinkSignature = sourceMethodLinkSignature;
  }

  /**
   * Returns whether this method was generated for a constructor field.
   *
   * @return true if this method was generated for a constructor field, false otherwise
   */
  public boolean isConstructorField() {
    return constructorField;
  }

  /**
   * Sets whether this method was generated for a constructor field.
   *
   * @param constructorField true if this method was generated for a constructor field
   */
  public void setConstructorField(boolean constructorField) {
    this.constructorField = constructorField;
  }

  /**
   * Comparator for sorting BuilderMethodDto instances with sophisticated ordering rules.
   *
   * <p>Sorting order for methods with same priority and name:
   *
   * <ol>
   *   <li>Methods with fewer parameters come first
   *   <li>Non-generic methods come before generic methods
   *   <li>Full method signature (name(paramType1,paramType2,...)) used for final ordering
   * </ol>
   */
  public static class BuilderMethodComparator implements java.util.Comparator<BuilderMethodDto> {

    @Override
    public int compare(BuilderMethodDto m1, BuilderMethodDto m2) {
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
    private String createMethodSignature(BuilderMethodDto method) {
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
    private boolean hasGenericParameters(BuilderMethodDto method) {
      return method.getParameters().stream()
          .anyMatch(param -> getQualifiedName(param.getParameterType()).contains("<"));
    }
  }

  /**
   * Returns a string representation of this method in Java method signature format.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code public PersonBuilder name(String)}
   *   <li>{@code public PersonBuilder age(int)}
   *   <li>{@code public PersonBuilder tags(Consumer<ArrayListBuilder<String>>)}
   * </ul>
   *
   * @return method signature as a string
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // Add modifier if present
    modifier.ifPresent(m -> sb.append(m.toString().toLowerCase()).append(" "));

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
