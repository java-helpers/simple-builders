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
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for inheritance of builder-triggering annotations.
 *
 * <p>Both {@code @SimpleBuilder} and the {@code @SimpleBuilder.Template} meta-annotation are
 * meta-annotated with {@code @Inherited}, so unannotated subclasses of an annotated parent also get
 * a builder. Custom template annotations that are themselves {@code @Inherited} propagate to
 * subclasses in the same way. See <a
 * href="https://github.com/java-helpers/simple-builders/issues/244">issue #244</a>.
 */
class BuilderAnnotationInheritanceTest {

  private Compilation compile(JavaFileObject... sourceFiles) {
    return ProcessorTestUtils.createCompiler().compile(sourceFiles);
  }

  /**
   * Asserts that no generated source file for the given class builder exists.
   *
   * @param compilation the compilation result
   * @param className the simple class name for which no builder should have been generated
   * @param message the failure message
   */
  private void assertNoBuilderGenerated(Compilation compilation, String className, String message) {
    Assertions.assertTrue(
        compilation.generatedSourceFiles().stream()
            .noneMatch(f -> f.getName().endsWith(className + "Builder.java")),
        message);
  }

  /**
   * An unannotated subclass of a class annotated with {@code @SimpleBuilder} must itself get a
   * builder, because {@code @SimpleBuilder} is {@code @Inherited}. The parent's builder is still
   * generated as well.
   */
  @Test
  void unannotatedSubclassGetsBuilderFromInheritedSimpleBuilder() {
    JavaFileObject parentSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class ParentDto {
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    JavaFileObject childSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class ChildDto extends ParentDto {
                private int age;

                public int getAge() { return age; }
                public void setAge(int age) { this.age = age; }
            }
            """);

    Compilation compilation = compile(parentSource, childSource);

    assertThat(compilation).succeededWithoutWarnings();

    String parentBuilder = loadGeneratedSource(compilation, "ParentDtoBuilder");
    assertGenerationSucceeded(compilation, "ParentDtoBuilder", parentBuilder);
    assertContaining(parentBuilder, "public ParentDtoBuilder name(String name)");

    String childBuilder = loadGeneratedSource(compilation, "ChildDtoBuilder");
    assertGenerationSucceeded(compilation, "ChildDtoBuilder", childBuilder);
    // The child builder must expose setters for both the inherited field and its own field.
    assertContaining(childBuilder, "public ChildDtoBuilder name(String name)");
    assertContaining(childBuilder, "public ChildDtoBuilder age(int age)");
  }

  /**
   * An unannotated subclass of a class carrying a custom {@code @Inherited} template annotation
   * (meta-annotated with {@code @SimpleBuilder.Template}) must itself get a builder. The parent's
   * builder is still generated as well, and the template's options apply to both.
   */
  @Test
  void unannotatedSubclassGetsBuilderFromInheritedTemplate() {
    JavaFileObject templateAnnotation =
        ProcessorTestUtils.forSource(
            """
            package test;

            import java.lang.annotation.ElementType;
            import java.lang.annotation.Inherited;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder.Template(options = @SimpleBuilder.Options())
            @Inherited
            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.TYPE)
            public @interface InheritedTemplate {}
            """);

    JavaFileObject parentSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            @InheritedTemplate
            public class ParentDto {
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    JavaFileObject childSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class ChildDto extends ParentDto {
                private int age;

                public int getAge() { return age; }
                public void setAge(int age) { this.age = age; }
            }
            """);

    Compilation compilation = compile(templateAnnotation, parentSource, childSource);

    assertThat(compilation).succeededWithoutWarnings();

    String parentBuilder = loadGeneratedSource(compilation, "ParentDtoBuilder");
    assertGenerationSucceeded(compilation, "ParentDtoBuilder", parentBuilder);
    assertContaining(parentBuilder, "public ParentDtoBuilder name(String name)");

    String childBuilder = loadGeneratedSource(compilation, "ChildDtoBuilder");
    assertGenerationSucceeded(compilation, "ChildDtoBuilder", childBuilder);
    // The child builder must expose setters for both the inherited field and its own field.
    assertContaining(childBuilder, "public ChildDtoBuilder name(String name)");
    assertContaining(childBuilder, "public ChildDtoBuilder age(int age)");
  }

  /**
   * A subclass annotated with {@code @Ignore4BuilderGeneration} must NOT get a builder even though
   * it would otherwise inherit {@code @SimpleBuilder} from its parent. The parent's builder is
   * still generated.
   */
  @Test
  void subclassOptedOutViaIgnoreDoesNotGetInheritedBuilder() {
    JavaFileObject parentSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class ParentDto {
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    JavaFileObject childSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;

            @Ignore4BuilderGeneration
            public class ChildDto extends ParentDto { }
            """);

    Compilation compilation = compile(parentSource, childSource);

    assertThat(compilation).succeededWithoutWarnings();

    String parentBuilder = loadGeneratedSource(compilation, "ParentDtoBuilder");
    assertGenerationSucceeded(compilation, "ParentDtoBuilder", parentBuilder);

    assertNoBuilderGenerated(
        compilation,
        "ChildDto",
        "ChildDto builder should not have been generated due to @Ignore4BuilderGeneration");
  }

  /**
   * A grandchild of an annotated type must also inherit {@code @SimpleBuilder} across multiple
   * levels of the type hierarchy.
   */
  @Test
  void grandchildInheritsSimpleBuilderAcrossMultipleLevels() {
    JavaFileObject parentSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class GrandParentDto {
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    JavaFileObject middleSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class MiddleDto extends GrandParentDto {
                private int age;

                public int getAge() { return age; }
                public void setAge(int age) { this.age = age; }
            }
            """);

    JavaFileObject childSource =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class ChildDto extends MiddleDto {
                private boolean active;

                public boolean isActive() { return active; }
                public void setActive(boolean active) { this.active = active; }
            }
            """);

    Compilation compilation = compile(parentSource, middleSource, childSource);

    assertThat(compilation).succeededWithoutWarnings();

    assertGenerationSucceeded(
        compilation,
        "GrandParentDtoBuilder",
        loadGeneratedSource(compilation, "GrandParentDtoBuilder"));
    assertGenerationSucceeded(
        compilation, "MiddleDtoBuilder", loadGeneratedSource(compilation, "MiddleDtoBuilder"));
    assertGenerationSucceeded(
        compilation, "ChildDtoBuilder", loadGeneratedSource(compilation, "ChildDtoBuilder"));
  }
}
