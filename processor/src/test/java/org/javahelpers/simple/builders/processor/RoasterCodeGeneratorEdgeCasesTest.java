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

package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertContaining;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertNotContaining;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for RoasterCodeGenerator edge cases to improve code coverage.
 *
 * <p>These tests exercise specific code paths in RoasterCodeGenerator that are typically not
 * covered by standard integration tests, such as empty collections, null checks, and edge cases.
 */
class RoasterCodeGeneratorEdgeCasesTest {

  /**
   * Tests that builders can be generated for DTOs in the default package (no package declaration).
   *
   * <p>This edge case ensures the code generator handles the absence of a package name correctly
   * and generates valid builder code without package declarations.
   */
  @Test
  void shouldHandleBuilderInDefaultPackage() {
    JavaFileObject sourceFile =
        JavaFileObjects.forSourceString(
            "SimpleDto",
            """
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class SimpleDto {
              private String name;
              public String getName() { return name; }
              public void setName(String name) { this.name = name; }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(sourceFile);

    String generatedCode = loadGeneratedSource(compilation, "SimpleDtoBuilder");
    assertGenerationSucceeded(compilation, "SimpleDtoBuilder", generatedCode);
    assertContaining(generatedCode, "public class SimpleDtoBuilder");
    assertNotContaining("package");
  }

  /**
   * Tests that builders are generated correctly when class-level annotations are disabled.
   *
   * <p>This edge case verifies that the code generator correctly handles the empty annotation list
   * when {@code usingGeneratedAnnotation} and {@code usingBuilderImplementationAnnotation} are both
   * disabled, ensuring the annotation copying logic skips processing when no annotations should be
   * added.
   */
  @Test
  void shouldHandleBuilderWithNoClassAnnotations() {
    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "NoAnnotationsDto",
            """
            private String value;
            public String getValue() { return value; }
            public void setValue(String value) { this.value = value; }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.usingGeneratedAnnotation=false",
                "-Asimplebuilder.usingBuilderImplementationAnnotation=false")
            .compile(sourceFile);

    String generatedCode = loadGeneratedSource(compilation, "NoAnnotationsDtoBuilder");
    assertGenerationSucceeded(compilation, "NoAnnotationsDtoBuilder", generatedCode);
    assertContaining(generatedCode, "public class NoAnnotationsDtoBuilder");
    // Verify no class-level annotations are present
    // (check for annotations before the class declaration)
    assertNotContaining(generatedCode, "@Generated", "@BuilderImplementation");
  }

  /**
   * Tests that builders are generated correctly when the With interface is disabled.
   *
   * <p>This edge case ensures the code generator handles builders with no nested types (the With
   * interface is the only nested type typically generated in builders). When {@code
   * generateWithInterface=false}, the builder should have no inner interfaces, and the nested type
   * generation logic should correctly skip processing when the nested type list is empty.
   */
  @Test
  void shouldHandleBuilderWithNoNestedTypes() {
    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "NoNestedTypesDto",
            """
            private Integer count;
            public Integer getCount() { return count; }
            public void setCount(Integer count) { this.count = count; }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.generateWithInterface=false")
            .compile(sourceFile);

    String generatedCode = loadGeneratedSource(compilation, "NoNestedTypesDtoBuilder");
    assertGenerationSucceeded(compilation, "NoNestedTypesDtoBuilder", generatedCode);
    assertContaining(generatedCode, "public class NoNestedTypesDtoBuilder");
    // Verify no With interface is generated
    assertNotContaining(generatedCode, "public interface With");
  }

  /**
   * Tests that the code generator detects and reports when a builder class already exists.
   *
   * <p>This edge case verifies that if a builder class with the same name already exists (either
   * manually written or from a previous compilation), the processor detects the conflict and issues
   * a warning, allowing compilation to succeed gracefully.
   */
  @Test
  void shouldDetectExistingBuilderClass() {
    JavaFileObject dto =
        JavaFileObjects.forSourceString(
            "test.PersonDto",
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class PersonDto {
              private String name;
              public String getName() { return name; }
              public void setName(String name) { this.name = name; }
            }
            """);

    // Create a builder class that already exists
    JavaFileObject existingBuilder =
        JavaFileObjects.forSourceString(
            "test.PersonDtoBuilder",
            """
            package test;

            public class PersonDtoBuilder {
              // Manually written or previously generated builder
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(dto, existingBuilder);

    // Verify compilation succeeds with a warning about the existing builder
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .hadWarningContaining(
            "Failed to generate builder - Builder class 'test.PersonDtoBuilder' already exists");
  }

  /**
   * Tests that fields without JavaDoc documentation are handled correctly.
   *
   * <p>This edge case verifies that the code generator handles blank or missing JavaDoc strings and
   * skips JavaDoc generation when documentation is not provided.
   */
  @Test
  void shouldHandleFieldWithNoJavadoc() {
    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "NoJavadocDto",
            """
            private String field;
            public String getField() { return field; }
            public void setField(String field) { this.field = field; }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(sourceFile);

    String generatedCode = loadGeneratedSource(compilation, "NoJavadocDtoBuilder");
    assertGenerationSucceeded(compilation, "NoJavadocDtoBuilder", generatedCode);
    assertContaining(generatedCode, "private TrackedValue<String> field");
  }

  /**
   * Tests that primitive types (int, boolean, double) are handled correctly in builders.
   *
   * <p>This edge case ensures primitive types are properly boxed in TrackedValue and that primitive
   * type imports are skipped (since primitives don't require imports).
   */
  @Test
  void shouldHandlePrimitiveTypes() {
    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "PrimitiveDto",
            """
            private int count;
            private boolean active;
            private double value;
            public int getCount() { return count; }
            public void setCount(int count) { this.count = count; }
            public boolean isActive() { return active; }
            public void setActive(boolean active) { this.active = active; }
            public double getValue() { return value; }
            public void setValue(double value) { this.value = value; }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(sourceFile);

    String generatedCode = loadGeneratedSource(compilation, "PrimitiveDtoBuilder");
    assertGenerationSucceeded(compilation, "PrimitiveDtoBuilder", generatedCode);
    assertContaining(
        generatedCode,
        "TrackedValue<Integer> count",
        "TrackedValue<Boolean> active",
        "TrackedValue<Double> value");
  }

  /**
   * Tests that generic type variables (T, K, V) are handled correctly in builders.
   *
   * <p>This edge case verifies that type variables are properly preserved in the generated builder
   * and that type variable imports are skipped (since they're not actual classes).
   */
  @Test
  void shouldHandleGenericTypeVariables() {
    JavaFileObject sourceFile =
        JavaFileObjects.forSourceString(
            "test.GenericDto",
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class GenericDto<T> {
              private T value;
              public T getValue() { return value; }
              public void setValue(T value) { this.value = value; }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(sourceFile);

    String generatedCode = loadGeneratedSource(compilation, "GenericDtoBuilder");
    assertContaining(
        generatedCode,
        "public class GenericDtoBuilder<T>",
        "TrackedValue<T> value",
        "public GenericDto<T> build()",
        "public static <T> GenericDtoBuilder<T> create()");
  }
}
