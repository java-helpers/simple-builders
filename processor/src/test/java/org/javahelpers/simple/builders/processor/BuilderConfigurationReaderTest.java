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

package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link
 * org.javahelpers.simple.builders.processor.util.BuilderConfigurationReader}.
 *
 * <p>Verifies configuration reading from various sources through end-to-end compilation:
 *
 * <ul>
 *   <li>Direct {@code @SimpleBuilder.Options} annotations
 *   <li>Template annotations ({@code @SimpleBuilder.Template})
 *   <li>Configuration resolution with proper priority chain
 *   <li>Compiler arguments
 * </ul>
 *
 * <p>These are integration tests that verify BuilderConfigurationReader by checking generated
 * builder code reflects the correct configuration values.
 */
class BuilderConfigurationReaderTest {

  /**
   * Test: Builder respects configuration from @SimpleBuilder.Options annotation.
   *
   * <p>Verifies BuilderConfigurationReader.readFromOptions() correctly reads and applies all
   * options.
   */
  @Test
  void readFromOptions_WithOptionsAnnotation_AppliesAllOptions() {
    // Given: A DTO with comprehensive @SimpleBuilder.Options
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;
            import org.javahelpers.simple.builders.core.enums.AccessModifier;

            @SimpleBuilder
            @SimpleBuilder.Options(
                generateFieldSupplier = OptionState.DISABLED,
                generateFieldConsumer = OptionState.DISABLED,
                generateBuilderConsumer = OptionState.DISABLED,
                generateVarArgsHelpers = OptionState.DISABLED,
                builderAccess = AccessModifier.PACKAGE_PRIVATE,
                methodAccess = AccessModifier.PACKAGE_PRIVATE,
                builderSuffix = "Factory",
                setterSuffix = "with"
            )
            public class PersonDto {
                private String name;
                private java.util.List<String> tags;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public java.util.List<String> getTags() { return tags; }
                public void setTags(java.util.List<String> tags) { this.tags = tags; }
            }
            """);

    // When: Compile
    Compilation compilation = ProcessorTestUtils.createCompiler().compile(source);

    // Then: Generated code reflects options
    assertThat(compilation).succeeded();

    String generatedCode = ProcessorTestUtils.loadGeneratedSource(compilation, "PersonDtoFactory");

    // Verify builderSuffix="Factory"
    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoFactory");

    // Verify class access is PACKAGE_PRIVATE
    ProcessorAsserts.assertNotContaining(generatedCode, "public class PersonDtoFactory");

    // Verify setterSuffix="with"
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoFactory withName(String name)");
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoFactory withTags(");

    // Verify builderAccess=PACKAGE_PRIVATE (no "public" before class)
    ProcessorAsserts.assertNotContaining(generatedCode, "public class PersonDtoFactory");
    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoFactory");

    // Verify methodAccess=PACKAGE_PRIVATE (no "public" before setter methods)
    ProcessorAsserts.assertNotContaining(generatedCode, "public PersonDtoFactory withName");
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoFactory withName(String name)");

    // Verify builder methods have public access even with builderAccess=PACKAGE_PRIVATE being
    // package private
    ProcessorAsserts.assertContaining(generatedCode, "public PersonDto build()");
    ProcessorAsserts.assertContaining(generatedCode, "public static PersonDtoFactory create()");

    // Verify generateFieldSupplier=DISABLED (no Supplier methods)
    ProcessorAsserts.assertNotContaining(generatedCode, "Supplier<String>");

    // Verify generateFieldConsumer=DISABLED (no Consumer methods for List)
    ProcessorAsserts.assertNotContaining(generatedCode, "Consumer<List<String>>");

    // Verify generateBuilderConsumer=DISABLED (no builder consumers)
    ProcessorAsserts.assertNotContaining(generatedCode, "Consumer<ArrayListBuilder");

    // Verify generateVarArgsHelpers=DISABLED
    ProcessorAsserts.assertNotContaining(
        generatedCode, "PersonDtoFactory withTags(String... tags)");
  }

  /**
   * Test: Builder respects configuration from template annotation.
   *
   * <p>Verifies BuilderConfigurationReader.readFromTemplate() correctly detects and applies
   * template configuration.
   */
  @Test
  void readFromTemplate_WithTemplateAnnotation_AppliesTemplateConfiguration() {
    // Given: A custom template annotation
    JavaFileObject templateAnnotation =
        JavaFileObjects.forSourceString(
            "test.MinimalBuilder",
            """
            package test;
            import java.lang.annotation.*;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;

            @SimpleBuilder.Template(options = @SimpleBuilder.Options(
                generateFieldSupplier = OptionState.DISABLED,
                generateFieldConsumer = OptionState.DISABLED,
                generateBuilderConsumer = OptionState.DISABLED,
                generateVarArgsHelpers = OptionState.DISABLED,
                builderSuffix = "MiniBuilder",
                setterSuffix = "set"
            ))
            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.TYPE)
            public @interface MinimalBuilder {
            }
            """);

    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            @MinimalBuilder
            public class PersonDto {
                private String name;
                private int age;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public int getAge() { return age; }
                public void setAge(int age) { this.age = age; }
            }
            """);

    // When: Compile
    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(templateAnnotation, dtoSource);

    // Then: Generated code reflects template configuration
    assertThat(compilation).succeeded();

    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "PersonDtoMiniBuilder");

    // Verify template values are applied
    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoMiniBuilder");
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoMiniBuilder setName(String name)");
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoMiniBuilder setAge(int age)");

    // Verify disabled features
    ProcessorAsserts.assertNotContaining(generatedCode, "Supplier<");
    ProcessorAsserts.assertNotContaining(generatedCode, "Consumer<");
  }

  /**
   * Test: Options annotation overrides template annotation (proper priority).
   *
   * <p>Verifies BuilderConfigurationReader.resolveConfiguration() applies correct priority: Options
   * > Template > Compiler args > Defaults
   */
  @Test
  void resolveConfiguration_OptionsOverridesTemplate_AppliesPriorityCorrectly() {
    // Given: Both template and options specified
    JavaFileObject templateAnnotation =
        ProcessorTestUtils.forSource(
            """
            package test;
            import java.lang.annotation.*;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder.Template(options = @SimpleBuilder.Options(
                builderSuffix = "TemplateBuilder",
                setterSuffix = "with"
            ))
            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.TYPE)
            public @interface CustomBuilder {
            }
            """);

    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @CustomBuilder
            @SimpleBuilder.Options(
                builderSuffix = "OptionsBuilder",
                setterSuffix = "set"
            )
            public class PersonDto {
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    // When: Compile
    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(templateAnnotation, dtoSource);

    // Then: Options wins over template
    assertThat(compilation).succeeded();

    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "PersonDtoOptionsBuilder");

    // Verify options values (not template values)
    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoOptionsBuilder");
    ProcessorAsserts.assertContaining(
        generatedCode, "PersonDtoOptionsBuilder setName(String name)");

    // Template values should NOT be present
    ProcessorAsserts.assertNotContaining(generatedCode, "TemplateBuilder");
    ProcessorAsserts.assertNotContaining(generatedCode, "withName");
  }

  /**
   * Test: Compiler arguments apply when no annotation configuration present.
   *
   * <p>Verifies BuilderConfigurationReader correctly reads and applies compiler arguments via
   * CompilerArgumentsReader integration.
   */
  @Test
  void resolveConfiguration_WithCompilerArgsOnly_AppliesCompilerArgs() {
    // Given: Simple @SimpleBuilder with no options or template
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class PersonDto {
                private String name;
                private java.util.List<String> tags;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public java.util.List<String> getTags() { return tags; }
                public void setTags(java.util.List<String> tags) { this.tags = tags; }
            }
            """);

    // When: Compile with compiler arguments
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.builderSuffix=CustomBuilder",
                "-Asimplebuilder.setterSuffix=set",
                "-Asimplebuilder.generateVarArgsHelpers=false")
            .compile(source);

    // Then: Compiler args are applied
    assertThat(compilation).succeeded();

    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "PersonDtoCustomBuilder");

    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoCustomBuilder");
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoCustomBuilder setName(String name)");
    ProcessorAsserts.assertContaining(generatedCode, "PersonDtoCustomBuilder setTags(");

    // VarArgs disabled by compiler arg
    ProcessorAsserts.assertNotContaining(generatedCode, "setTags(String... tags)");
  }

  /** Test: Options override compiler arguments (proper priority). */
  @Test
  void resolveConfiguration_OptionsOverridesCompilerArgs_AppliesPriorityCorrectly() {
    // Given: Both compiler args and options specified
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            @SimpleBuilder.Options(
                builderSuffix = "OptionsBuilder"
            )
            public class PersonDto {
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    // When: Compile with conflicting compiler argument
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderSuffix=CompilerArgBuilder")
            .compile(source);

    // Then: Options wins over compiler arg
    assertThat(compilation).succeeded();

    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "PersonDtoOptionsBuilder");

    // Verify options value (not compiler arg value)
    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoOptionsBuilder");
    ProcessorAsserts.assertNotContaining(generatedCode, "CompilerArgBuilder");
  }

  /**
   * Test: All layers work together in complete priority chain.
   *
   * <p>Verifies the complete configuration resolution: Options > Template > CompilerArgs > Defaults
   */
  @Test
  void resolveConfiguration_AllLayersTogether_CompleteChain() {
    // Given: All configuration sources present
    JavaFileObject templateAnnotation =
        ProcessorTestUtils.forSource(
            """
            package test;
            import java.lang.annotation.*;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;

            @SimpleBuilder.Template(options = @SimpleBuilder.Options(
                generateFieldSupplier = OptionState.DISABLED,
                setterSuffix = "with"
            ))
            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.TYPE)
            public @interface TemplatedBuilder {
            }
            """);

    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;

            @TemplatedBuilder
            @SimpleBuilder
            @SimpleBuilder.Options(
                generateVarArgsHelpers = OptionState.DISABLED
            )
            public class PersonDto {
                private String name;
                private java.util.List<String> tags;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public java.util.List<String> getTags() { return tags; }
                public void setTags(java.util.List<String> tags) { this.tags = tags; }
            }
            """);

    // When: Compile with compiler args
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderSuffix=CompilerBuilder")
            .compile(templateAnnotation, dtoSource);

    // Then: Layered configuration is applied correctly
    assertThat(compilation).succeeded();

    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "PersonDtoCompilerBuilder");

    // Options wins for generateVarArgsHelpers
    ProcessorAsserts.assertNotContaining(generatedCode, "withTags(String... tags)");

    // Template wins for setterSuffix (not overridden by options)
    ProcessorAsserts.assertContaining(generatedCode, "withName(String name)");
    ProcessorAsserts.assertContaining(generatedCode, "withTags(");

    // Template wins for generateFieldSupplier
    ProcessorAsserts.assertNotContaining(generatedCode, "Supplier<String>");

    // Compiler arg wins for builderSuffix (not overridden by template or options)
    ProcessorAsserts.assertContaining(generatedCode, "class PersonDtoCompilerBuilder");
  }
}
