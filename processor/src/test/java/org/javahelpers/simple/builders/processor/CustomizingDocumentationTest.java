package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertingResult;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.printDiagnosticsOnVerbose;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

/**
 * Test class that validates code examples from CUSTOMIZING.md documentation.
 *
 * <p>This test ensures that key code patterns and structures from the documentation are
 * syntactically correct and can be compiled successfully. When updating the documentation, this
 * test should be updated accordingly to maintain consistency.
 *
 * <p><strong>Documentation Reference:</strong> <a
 * href="../../../../../../../docs/CUSTOMIZING.md">docs/CUSTOMIZING.md</a>
 *
 * <p><strong>Update Instructions:</strong>
 *
 * <ol>
 *   <li>When updating CUSTOMIZING.md code examples, update the corresponding test methods
 *   <li>Keep the documentation link reference in this class header
 *   <li>Focus on testing compilable patterns rather than complete working examples
 * </ol>
 *
 * <p><strong>Note:</strong> Some examples in CUSTOMIZING.md use custom annotations and external
 * dependencies that are not available in tests. This test validates the core patterns and
 * structures that can be compiled.
 */
class CustomizingDocumentationTest {

  /**
   * Test basic DTO generation with @SimpleBuilder annotation.
   *
   * <p>This validates the fundamental pattern shown throughout CUSTOMIZING.md.
   */
  @Test
  void testBasicDtoGeneration() {
    String emailDto =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class EmailDto {
                private String email;

                public EmailDto(String email) {
                    this.email = email;
                }

                public String getEmail() {
                    return email;
                }

                public void setEmail(String email) {
                    this.email = email;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(JavaFileObjects.forSourceString("com.example.test.EmailDto", emailDto));
    printDiagnosticsOnVerbose(compilation);

    String builderClassName = "EmailDtoBuilder";
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Verify basic structure of generated builder
    assertingResult(
        generatedCode,
        contains("public class EmailDtoBuilder"),
        contains("public EmailDto build()"),
        contains("public EmailDtoBuilder email(String email)"),
        contains("public static EmailDtoBuilder create()"));
  }

  /**
   * Test Generator interface hierarchy structure from CUSTOMIZING.md.
   *
   * <p>Validates the sealed interface pattern: Generator permits MethodGenerator, BuilderEnhancer
   */
  @Test
  void testGeneratorInterfaceStructure() {
    String customGenerator =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class TestMethodGenerator implements MethodGenerator {
                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    // Check if field type is String by comparing class name
                    return "java.lang.String".equals(field.getFieldType().getFullQualifiedName());
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    return List.of();
                }

                @Override
                public int getPriority() {
                    return 100;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.TestMethodGenerator", customGenerator));
    printDiagnosticsOnVerbose(compilation);

    // Verify the custom generator class compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test BuilderEnhancer interface implementation from CUSTOMIZING.md.
   *
   * <p>Validates the builder-level enhancement pattern.
   */
  @Test
  void testBuilderEnhancerStructure() {
    String customEnhancer =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
            import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;

            public class TestBuilderEnhancer implements BuilderEnhancer {
                @Override
                public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
                    return true;
                }

                @Override
                public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
                    // Enhancement logic
                }

                @Override
                public int getPriority() {
                    return 500;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.TestBuilderEnhancer", customEnhancer));
    printDiagnosticsOnVerbose(compilation);

    // Verify the custom enhancer class compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test custom generator with method generation pattern from CUSTOMIZING.md.
   *
   * <p>Validates the pattern for creating custom methods with parameters and code.
   */
  @Test
  void testCustomGeneratorMethodCreation() {
    String customGenerator =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class CustomHelperGenerator implements MethodGenerator {
                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    return "java.lang.String".equals(field.getFieldType().getFullQualifiedName());
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    String fieldName = field.getFieldName();
                    String methodName = "custom" + capitalize(fieldName);

                    MethodDto method = new MethodDto(methodName, builderType);

                    MethodParameterDto parameter = new MethodParameterDto();
                    parameter.setParameterName("value");
                    parameter.setParameterTypeName(new TypeName("java.lang", "String"));
                    method.addParameter(parameter);

                    method.setCode("return this." + fieldName + "(value.toUpperCase());");

                    return List.of(method);
                }

                @Override
                public int getPriority() {
                    return 200;
                }

                private String capitalize(String str) {
                    if (str == null || str.isEmpty()) return str;
                    return str.substring(0, 1).toUpperCase() + str.substring(1);
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.CustomHelperGenerator", customGenerator));
    printDiagnosticsOnVerbose(compilation);

    // Verify the custom generator class compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test priority-based generator ordering pattern from CUSTOMIZING.md.
   *
   * <p>Validates that generators can specify different priority levels.
   */
  @Test
  void testGeneratorPriorityPattern() {
    String highPriorityGenerator =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class HighPriorityGenerator implements MethodGenerator {
                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    return true;
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    return List.of();
                }

                @Override
                public int getPriority() {
                    return 1000; // Higher than default generators
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.HighPriorityGenerator", highPriorityGenerator));
    printDiagnosticsOnVerbose(compilation);

    // Verify the custom generator class compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test error handling pattern from CUSTOMIZING.md.
   *
   * <p>Validates the recommended error handling approach using ProcessingContext.
   */
  @Test
  void testErrorHandlingPattern() {
    String generatorWithErrorHandling =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class SafeGenerator implements MethodGenerator {
                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    return true;
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    try {
                        // Generation logic
                        return List.of();
                    } catch (Exception e) {
                        context.error("Failed to generate method for field %s: %s", field.getFieldName(), e.getMessage());
                        return List.of(); // Return empty list on error
                    }
                }

                @Override
                public int getPriority() {
                    return 100;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.SafeGenerator", generatorWithErrorHandling));
    printDiagnosticsOnVerbose(compilation);

    // Verify the custom generator class compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test conditional application pattern from CUSTOMIZING.md.
   *
   * <p>Validates the pattern for conditionally applying generators based on field properties.
   */
  @Test
  void testConditionalApplicationPattern() {
    String conditionalGenerator =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class ConditionalGenerator implements MethodGenerator {
                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    // Check if component should apply
                    if (!shouldApply(field, context)) {
                        return false;
                    }

                    // Check configuration
                    return context.getConfiguration() != null;
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    return List.of();
                }

                @Override
                public int getPriority() {
                    return 100;
                }

                private boolean shouldApply(FieldDto field, ProcessingContext context) {
                    return "java.lang.String".equals(field.getFieldType().getFullQualifiedName());
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.ConditionalGenerator", conditionalGenerator));
    printDiagnosticsOnVerbose(compilation);

    // Verify the custom generator class compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test complete CustomValidationGenerator example structure from CUSTOMIZING.md.
   *
   * <p>Validates the full example showing method creation with parameters, code, and arguments.
   * Note: Uses simplified logic since some API methods from docs are conceptual.
   */
  @Test
  void testCompleteCustomValidationGenerator() {
    String customValidationGenerator =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class CustomValidationGenerator implements MethodGenerator {

                private static final TypeName TRACKED_VALUE_TYPE =
                    new TypeName("org.javahelpers.simple.builders.core.util", "TrackedValue");

                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    // Only apply to String fields with @Email annotation
                    return "java.lang.String".equals(field.getFieldType().getFullQualifiedName())
                        && field.hasAnnotation("javax.validation.constraints.Email");
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    String fieldName = field.getFieldName();
                    String methodName = "validated" + capitalize(fieldName);

                    MethodDto method = new MethodDto(methodName, builderType);

                    String parameterName = fieldName;
                    MethodParameterDto parameter = new MethodParameterDto();
                    parameter.setParameterName(parameterName);
                    parameter.setParameterTypeName(new TypeName("java.lang", "String"));
                    method.addParameter(parameter);

                    method.setCode(String.format(
                        "if (%s != null && %s.contains(\\"@\\")) { " +
                        "this.%s = $builderFieldWrapper:T.changedValue(%s); " +
                        "return this; } " +
                        "throw new IllegalArgumentException(\\"Invalid email: \\" + %s);",
                        parameterName, parameterName, fieldName, parameterName, parameterName));
                    method.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);

                    return List.of(method);
                }

                @Override
                public int getPriority() {
                    return 1000;
                }

                private String capitalize(String str) {
                    if (str == null || str.isEmpty()) return str;
                    return str.substring(0, 1).toUpperCase() + str.substring(1);
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.CustomValidationGenerator", customValidationGenerator));
    printDiagnosticsOnVerbose(compilation);

    // Verify the complete custom generator compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test complete CustomValidationEnhancer example structure from CUSTOMIZING.md.
   *
   * <p>Validates the full enhancer example showing annotation adding and method creation. Note:
   * Uses simplified logic since some API methods from docs are conceptual.
   */
  @Test
  void testCompleteCustomValidationEnhancer() {
    String customValidationEnhancer =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
            import org.javahelpers.simple.builders.processor.dtos.AnnotationDto;
            import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;

            public class CustomValidationEnhancer implements BuilderEnhancer {

                @Override
                public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
                    // Only apply to DTOs with validation annotations
                    return builderDto.getAllFieldsForBuilder().stream()
                        .flatMap(field -> field.getParameterAnnotations().stream())
                        .anyMatch(annotation ->
                            annotation.getAnnotationType() != null &&
                            org.apache.commons.lang3.StringUtils.startsWith(
                                annotation.getAnnotationType().getFullQualifiedName(),
                                "javax.validation.constraints."));
                }

                @Override
                public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
                    // Add validation method to builder
                    MethodDto validateMethod = createValidateMethod();
                    builderDto.addCoreMethod(validateMethod);

                    // Add @Valid annotation if available
                    if (isValidationAvailable(context)) {
                        AnnotationDto validAnnotation = new AnnotationDto();
                        validAnnotation.setAnnotationType(new TypeName("javax.validation", "Valid"));
                        builderDto.addClassAnnotation(validAnnotation);
                    }

                    context.debug("Added validation enhancements to builder %s",
                        builderDto.getBuilderTypeName().getClassName());
                }

                @Override
                public int getPriority() {
                    return 500;
                }

                private MethodDto createValidateMethod() {
                    TypeName returnType = new TypeName("java.lang", "Void");
                    MethodDto method = new MethodDto("validate", returnType);
                    method.setCode("// Validation logic here");
                    return method;
                }

                private boolean isValidationAvailable(ProcessingContext context) {
                    return context.getTypeElement("javax.validation.Valid") != null;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.CustomValidationEnhancer", customValidationEnhancer));
    printDiagnosticsOnVerbose(compilation);

    // Verify the complete custom enhancer compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test google-compile-testing pattern from CUSTOMIZING.md.
   *
   * <p>Validates the recommended testing approach using compile-testing framework.
   */
  @Test
  void testGoogleCompileTestingPattern() {
    // This test validates that the pattern shown in CUSTOMIZING.md works
    String testDto =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record TestDto(String name, int value) {}
            """;

    Compilation compilation =
        createCompiler()
            .compile(JavaFileObjects.forSourceString("com.example.test.TestDto", testDto));

    // Verify compilation succeeded (as shown in docs)
    assertThat(compilation).succeeded();

    // Load and verify generated code (as shown in docs)
    String generatedBuilder = loadGeneratedSource(compilation, "TestDtoBuilder");
    assertGenerationSucceeded(compilation, "TestDtoBuilder", generatedBuilder);

    // Verify custom methods are present (pattern from docs)
    assertingResult(
        generatedBuilder,
        contains("public class TestDtoBuilder"),
        contains("public TestDto build()"),
        contains("public static TestDtoBuilder create()"));
  }

  /**
   * Test DateParserGenerator example structure from CUSTOMIZING.md.
   *
   * <p>Validates the date parsing generator pattern. Note: Uses simplified logic since annotation
   * checking API is conceptual.
   */
  @Test
  void testDateParserGeneratorExample() {
    String dateParserGenerator =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
            import org.javahelpers.simple.builders.processor.dtos.FieldDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;
            import java.util.List;

            public class DateParserGenerator implements MethodGenerator {

                @Override
                public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
                    return "java.time.LocalDate".equals(field.getFieldType().getFullQualifiedName())
                        && field.hasAnnotation("org.javahelpers.simple.builders.processor.testannotations.ParseFromString");
                }

                @Override
                public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
                    String fieldName = field.getFieldName();
                    String methodName = fieldName + "FromString";

                    MethodDto method = new MethodDto(methodName, builderType);

                    String parameterName = fieldName + "String";
                    MethodParameterDto parameter = new MethodParameterDto();
                    parameter.setParameterName(parameterName);
                    parameter.setParameterTypeName(new TypeName("java.lang", "String"));
                    method.addParameter(parameter);

                    method.setCode(String.format(
                        "try { return this.%s(java.time.LocalDate.parse(%s)); } " +
                        "catch (java.time.format.DateTimeParseException e) { " +
                        "throw new IllegalArgumentException(\\"Invalid date format: \\" + %s, e); }",
                        fieldName, parameterName, parameterName));

                    return List.of(method);
                }

                @Override
                public int getPriority() {
                    return 200;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.DateParserGenerator", dateParserGenerator));
    printDiagnosticsOnVerbose(compilation);

    // Verify the date parser generator compiles successfully
    assertThat(compilation).succeeded();
  }

  /**
   * Test BuilderFactoryEnhancer example structure from CUSTOMIZING.md.
   *
   * <p>Validates the factory method enhancer pattern. Note: Uses simplified logic since some API
   * methods from docs are conceptual.
   */
  @Test
  void testBuilderFactoryEnhancerExample() {
    String builderFactoryEnhancer =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
            import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodDto;
            import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
            import org.javahelpers.simple.builders.processor.dtos.TypeName;
            import org.javahelpers.simple.builders.processor.util.ProcessingContext;

            public class BuilderFactoryEnhancer implements BuilderEnhancer {

                @Override
                public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
                    return dtoType.hasAnnotation("org.javahelpers.simple.builders.processor.testannotations.BuilderFactory");
                }

                @Override
                public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
                    TypeName builderType = builderDto.getBuilderTypeName();

                    // Add static factory method (conceptual - actual API may differ)
                    MethodDto factoryMethod = new MethodDto("from", builderType);
                    factoryMethod.setStatic(true);

                    MethodParameterDto parameter = new MethodParameterDto();
                    parameter.setParameterName("template");
                    parameter.setParameterTypeName(builderType);
                    factoryMethod.addParameter(parameter);

                    factoryMethod.setCode(String.format("return new %s();", builderType.getClassName()));

                    // Note: addStaticMethod is conceptual - shows the pattern
                    builderDto.addCoreMethod(factoryMethod);

                    context.debug("Added factory method to builder %s", builderType.getClassName());
                }

                @Override
                public int getPriority() {
                    return 50;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(
                JavaFileObjects.forSourceString(
                    "com.example.test.BuilderFactoryEnhancer", builderFactoryEnhancer));
    printDiagnosticsOnVerbose(compilation);

    // Verify the factory enhancer compiles successfully
    assertThat(compilation).succeeded();
  }
}
