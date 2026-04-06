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
package org.javahelpers.simple.builders.processor.generators.builder;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.GenericParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds core builder methods (build, create, toString).
 *
 * <p>This enhancer generates the essential methods that every builder needs: {@code build()} to
 * construct the final DTO instance, {@code create()} as a static factory method, and {@code
 * toString()} for debugging and logging.
 *
 * <p><b>Important behavior:</b> The {@code build()} method performs null-safety validation for
 * non-null fields, constructs the DTO instance, and assigns all set field values. The {@code
 * create()} method provides a convenient static entry point. The {@code toString()} method shows
 * which fields are set and their values.
 *
 * <p><b>Requirements:</b> Always applies to all builders. These methods are fundamental to builder
 * functionality and cannot be disabled.
 *
 * <p>This enhancer cannot be deactivated as it provides the core builder functionality.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 *
 * @SimpleBuilder
 * public record BookDto(String title, String author, int pages) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.create()  // Static factory method
 *     .title("My Book")
 *     .author("John Doe")
 *     .pages(250)
 *     .build();  // Constructs the final BookDto
 *
 * // toString() for debugging (only shows set fields with unwrapped values):
 * System.out.println(BookDtoBuilder.create().title("Test"));
 * // Output: BookDtoBuilder[title=Test]
 * }</pre>
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
    MethodDto buildMethod = createBuildMethod(builderDto);
    builderDto.addCoreMethod(buildMethod);

    // Add static create() method
    MethodDto createMethod = createStaticCreateMethod(builderDto);
    builderDto.addCoreMethod(createMethod);

    // Add toString() method
    MethodDto toStringMethod = createToStringMethod(builderDto);
    builderDto.addCoreMethod(toStringMethod);
  }

  /** Creates the build() method. */
  protected MethodDto createBuildMethod(BuilderDefinitionDto builderDto) {
    TypeName returnType =
        MethodGeneratorUtil.createGenericTypeName(
            builderDto.getBuildingTargetTypeName(), builderDto.getGenerics());
    MethodDto method = new MethodDto("build", returnType);
    method.setOrdering(ORDERING_BUILD);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);

    // Add @Override annotation only if implementing IBuilderBase interface
    if (builderDto.getConfiguration().shouldImplementBuilderBase()) {
      AnnotationDto overrideAnnotation = new AnnotationDto();
      overrideAnnotation.setAnnotationType(JavaLangMapper.map2TypeName(Override.class));
      method.addAnnotation(overrideAnnotation);
    }

    // Create method implementation with validation and setter application
    StringBuilder code = new StringBuilder();

    // Add validation for non-nullable constructor fields
    for (var field : builderDto.getConstructorFieldsForBuilder()) {
      if (field.isNonNullable()) {
        code.append("if (!this.")
            .append(field.getFieldNameInBuilder())
            .append(".isSet()) {\n")
            .append("  throw new IllegalStateException(\"Required field '")
            .append(field.getFieldNameInBuilder())
            .append("' must be set before calling build()\");\n")
            .append("}\n");
        code.append("if (this.")
            .append(field.getFieldNameInBuilder())
            .append(".value() == null) {\n")
            .append("  throw new IllegalStateException(\"Field '")
            .append(field.getFieldNameInBuilder())
            .append("' is marked as non-null but null value was provided\");\n")
            .append("}\n");
      }
    }

    // Add validation for non-nullable setter fields
    for (var field : builderDto.getSetterFieldsForBuilder()) {
      if (field.isNonNullable() && !(field.getFieldType() instanceof TypeNamePrimitive)) {
        // Non-nullable non-primitive field - validate not null
        // Skip primitives as they can't be null (but they could be null if boxed)
        code.append("if (this.")
            .append(field.getFieldNameInBuilder())
            .append(".isSet() && this.")
            .append(field.getFieldNameInBuilder())
            .append(".value() == null) {\n")
            .append("  throw new IllegalStateException(\"Field '")
            .append(field.getFieldNameInBuilder())
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
          .append(field.getFieldNameInBuilder())
          .append(".ifSet(result::")
          .append(field.getSetterName())
          .append(");\n");
    }

    code.append("return result;");

    method.setCode(code.toString());
    method.addArgument("dtoBaseType", builderDto.getBuildingTargetTypeName());
    method.addArgument("buildResultType", returnType);

    // Add imports for types used in code block
    method.addCodeBlockImport(new TypeName("java.lang", "IllegalStateException"));

    method.setJavadoc(new JavadocDto("Builds the configured DTO instance."));

    return method;
  }

  /** Creates the static create() method. */
  protected MethodDto createStaticCreateMethod(BuilderDefinitionDto builderDto) {
    TypeName returnType =
        MethodGeneratorUtil.createGenericTypeName(
            builderDto.getBuilderTypeName(), builderDto.getGenerics());
    MethodDto method = new MethodDto("create", returnType);
    method.setOrdering(ORDERING_CREATE);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);
    method.setStatic(true);

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
        new JavadocDto("Creating a new builder for {@code %s}.", targetFullName)
            .addReturn("builder for {@code %s}", targetFullName));

    return method;
  }

  /** Creates the toString() method. */
  protected MethodDto createToStringMethod(BuilderDefinitionDto builderDto) {
    MethodDto method = new MethodDto("toString", new TypeName("java.lang", "String"));
    method.setOrdering(ORDERING_TO_STRING);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);
    AnnotationDto overrideAnnotation = new AnnotationDto();
    overrideAnnotation.setAnnotationType(JavaLangMapper.map2TypeName(Override.class));
    method.addAnnotation(overrideAnnotation);

    // Create method implementation
    method.setCode(
        "return new $toStringBuilder:T(this, $toStringStyle:T.INSTANCE)"
            + createToStringAppendCalls(builderDto)
            + "\n        .toString();");

    // Add template arguments for code generation
    TypeName toStringBuilderType =
        new TypeName("org.apache.commons.lang3.builder", "ToStringBuilder");
    TypeName toStringStyleType =
        new TypeName("org.javahelpers.simple.builders.core.util", "BuilderToStringStyle");

    method.addArgument("toStringBuilder", toStringBuilderType);
    method.addArgument("toStringStyle", toStringStyleType);

    // Add imports for types used in code block
    method.addCodeBlockImport(toStringBuilderType);
    method.addCodeBlockImport(toStringStyleType);

    method.setJavadoc(
        new JavadocDto(
                "Returns a string representation of this builder, including only fields that have been set.")
            .addReturn("string representation of the builder"));

    return method;
  }

  /** Creates the constructor arguments string for the build() method. */
  private String createConstructorArgsString(BuilderDefinitionDto builderDto) {
    return builderDto.getConstructorFieldsForBuilder().stream()
        .map(field -> "this." + field.getFieldNameInBuilder() + ".value()")
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /** Creates the append calls for toString() method. */
  private String createToStringAppendCalls(BuilderDefinitionDto builderDto) {
    StringBuilder sb = new StringBuilder();

    // Combine all fields and process them
    List<FieldDto> allFields = new ArrayList<>();
    allFields.addAll(builderDto.getConstructorFieldsForBuilder());
    allFields.addAll(builderDto.getSetterFieldsForBuilder());

    for (int i = 0; i < allFields.size(); i++) {
      FieldDto field = allFields.get(i);
      sb.append("\n        .append(\"")
          .append(field.getFieldNameInBuilder())
          .append("\", this.")
          .append(field.getFieldNameInBuilder())
          .append(")");
    }

    return sb.toString();
  }
}
