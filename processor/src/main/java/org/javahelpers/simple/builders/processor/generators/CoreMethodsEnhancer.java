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
package org.javahelpers.simple.builders.processor.generators;

import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.GenericParameterDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds core builder methods (build, create, conditional, toString).
 *
 * <p>This enhancer generates the essential methods that every builder needs:
 *
 * <ul>
 *   <li>{@code build()} - constructs the final DTO instance
 *   <li>{@code create()} - static factory method
 *   <li>{@code conditional()} - conditional method application
 *   <li>{@code toString()} - string representation
 * </ul>
 *
 * <p>These methods are added with specific ordering to ensure they appear in the correct location
 * in the generated builder class.
 *
 * <p>Priority: 100 (highest - core infrastructure should be applied first)
 */
public class CoreMethodsEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 100;

  // Ordering constants for method generation order
  private static final int ORDERING_CREATE = 200; // After constructor, before field methods
  private static final int ORDERING_BUILD =
      1200; // After builder methods and conditional, before toString
  private static final int ORDERING_TO_STRING = 2000; // Last, after conditional methods

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return true; // Core methods are always needed
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    // Add build() method
    MethodDto buildMethod = createBuildMethod(builderDto, context);
    builderDto.addCoreMethod(buildMethod);

    // Add static create() method
    MethodDto createMethod = createStaticCreateMethod(builderDto, context);
    builderDto.addCoreMethod(createMethod);

    // Add toString() method
    MethodDto toStringMethod = createToStringMethod(builderDto, context);
    builderDto.addCoreMethod(toStringMethod);

    context.debug(
        "Added core methods to builder %s", builderDto.getBuilderTypeName().getClassName());
  }

  /** Creates the build() method. */
  private MethodDto createBuildMethod(BuilderDefinitionDto builderDto, ProcessingContext context) {
    MethodDto method = new MethodDto();
    method.setMethodName("build");
    TypeName returnType =
        MethodGeneratorUtil.createGenericTypeName(
            builderDto.getBuildingTargetTypeName(), builderDto.getGenerics());
    method.setReturnType(returnType);
    method.setOrdering(ORDERING_BUILD);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);

    // Add @Override annotation only if implementing IBuilderBase interface
    if (builderDto.getConfiguration().shouldImplementBuilderBase()) {
      method.addAnnotation("java.lang", "Override");
    }

    // Create method implementation with validation and setter application
    StringBuilder code = new StringBuilder();

    // Add validation for non-nullable constructor fields
    for (var field : builderDto.getConstructorFieldsForBuilder()) {
      if (field.isNonNullable()) {
        code.append("if (!this.")
            .append(field.getFieldName())
            .append(".isSet()) {\n")
            .append("  throw new IllegalStateException(\"Required field '")
            .append(field.getFieldName())
            .append("' must be set before calling build()\");\n")
            .append("}\n");
        code.append("if (this.")
            .append(field.getFieldName())
            .append(".value() == null) {\n")
            .append("  throw new IllegalStateException(\"Field '")
            .append(field.getFieldName())
            .append("' is marked as non-null but null value was provided\");\n")
            .append("}\n");
      }
    }

    // Add validation for non-nullable setter fields
    for (var field : builderDto.getSetterFieldsForBuilder()) {
      if (field.isNonNullable()) {
        code.append("if (this.")
            .append(field.getFieldName())
            .append(".isSet() && this.")
            .append(field.getFieldName())
            .append(".value() == null) {\n")
            .append("  throw new IllegalStateException(\"Field '")
            .append(field.getFieldName())
            .append("' is marked as non-null but null value was provided\");\n")
            .append("}\n");
      }
    }

    // Create DTO instance
    String ctorArgs = createConstructorArgsString(builderDto);
    if (builderDto.getGenerics().isEmpty()) {
      code.append("$buildResultType:T result = new $dtoBaseType:T(")
          .append(ctorArgs)
          .append(");\n");
    } else {
      code.append("$buildResultType:T result = new $dtoBaseType:T<>(")
          .append(ctorArgs)
          .append(");\n");
    }

    // Apply setter-based fields
    for (var field : builderDto.getSetterFieldsForBuilder()) {
      code.append("this.")
          .append(field.getFieldName())
          .append(".ifSet(result::")
          .append(field.getSetterName())
          .append(");\n");
    }

    code.append("return result;");

    method.setCode(code.toString());
    method.addArgument("dtoBaseType", builderDto.getBuildingTargetTypeName());
    method.addArgument("buildResultType", returnType);

    method.setJavadoc("Builds the configured DTO instance.");

    return method;
  }

  /** Creates the static create() method. */
  private MethodDto createStaticCreateMethod(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    MethodDto method = new MethodDto();
    method.setMethodName("create");
    method.setOrdering(ORDERING_CREATE);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);
    method.setStatic(true);
    TypeName returnType =
        MethodGeneratorUtil.createGenericTypeName(
            builderDto.getBuilderTypeName(), builderDto.getGenerics());
    method.setReturnType(returnType);

    // Use appropriate code template based on whether we have generics
    if (builderDto.getGenerics().isEmpty()) {
      method.setCode("return new $builderType:T();");
    } else {
      method.setCode("return new $builderType:T<>();");
    }

    // Add generic type parameters to method if builder has generics
    // (because this is a static function, the generic names from class are not available)
    for (GenericParameterDto genericParam : builderDto.getGenerics()) {
      method.addGenericParameter(genericParam);
    }
    method.addArgument("builderType", builderDto.getBuilderTypeName());

    String targetFullName = builderDto.getBuildingTargetTypeName().getFullQualifiedName();

    method.setJavadoc(
        """
        Creating a new builder for {@code %s}.

        @return builder for {@code %s}
        """
            .formatted(targetFullName, targetFullName));

    return method;
  }

  /** Creates the toString() method. */
  private MethodDto createToStringMethod(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    MethodDto method = new MethodDto();
    method.setMethodName("toString");
    method.setReturnType(
        new org.javahelpers.simple.builders.processor.dtos.TypeName("java.lang", "String"));
    method.setOrdering(ORDERING_TO_STRING);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);
    method.addAnnotation("java.lang", "Override");

    // Create method implementation
    method.setCode(
        "return new $toStringBuilder:T(this, $toStringStyle:T.INSTANCE)"
            + createToStringAppendCalls(builderDto)
            + "\n        .toString();");

    // Add template arguments for code generation
    method.addArgument(
        "toStringBuilder",
        new org.javahelpers.simple.builders.processor.dtos.TypeName(
            "org.apache.commons.lang3.builder", "ToStringBuilder"));
    method.addArgument(
        "toStringStyle",
        new org.javahelpers.simple.builders.processor.dtos.TypeName(
            "org.javahelpers.simple.builders.core.util", "BuilderToStringStyle"));

    method.setJavadoc(
        """
        Returns a string representation of this builder, including only fields that have been set.

        @return string representation of the builder
        """);

    return method;
  }

  /** Creates the constructor arguments string for the build() method. */
  private String createConstructorArgsString(BuilderDefinitionDto builderDto) {
    return builderDto.getConstructorFieldsForBuilder().stream()
        .map(field -> "this." + field.getFieldName() + ".value()")
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /** Creates the append calls for toString() method. */
  private String createToStringAppendCalls(BuilderDefinitionDto builderDto) {
    StringBuilder sb = new StringBuilder();
    boolean firstField = true;

    // Process constructor fields
    for (FieldDto field : builderDto.getConstructorFieldsForBuilder()) {
      if (firstField) {
        sb.append("\n        .append(\"")
            .append(field.getFieldName())
            .append("\", this.")
            .append(field.getFieldName())
            .append(")");
        firstField = false;
      } else {
        sb.append("\n        .append(\"")
            .append(field.getFieldName())
            .append("\", this.")
            .append(field.getFieldName())
            .append(")");
      }
    }

    // Process setter fields
    for (FieldDto field : builderDto.getSetterFieldsForBuilder()) {
      if (firstField) {
        sb.append("\n        .append(\"")
            .append(field.getFieldName())
            .append("\", this.")
            .append(field.getFieldName())
            .append(")");
        firstField = false;
      } else {
        sb.append("\n        .append(\"")
            .append(field.getFieldName())
            .append("\", this.")
            .append(field.getFieldName())
            .append(")");
      }
    }

    return sb.toString();
  }
}
