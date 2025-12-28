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

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for add2FieldName helper methods that add single elements to List and Set fields. This
 * implements feature #86: Supporting addToField for Sets/Lists.
 */
class AddToCollectionTest {

  private static Compilation compile(JavaFileObject... sources) {
    return ProcessorTestUtils.createCompiler().compile(sources);
  }

  @Test
  void add2Method_generatedForListField() {
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "PersonDto",
            """
                private final String name;
                private final java.util.List<String> nicknames;

                public PersonDto(String name, java.util.List<String> nicknames) {
                  this.name = name;
                  this.nicknames = nicknames;
                }

                public String getName() {
                  return name;
                }

                public java.util.List<String> getNicknames() {
                  return nicknames;
                }
            """);

    Compilation compilation = compile(dto);
    String generatedCode = loadGeneratedSource(compilation, "PersonDtoBuilder");
    assertGenerationSucceeded(compilation, "PersonDtoBuilder", generatedCode);

    // Verify add2Nicknames method is generated
    ProcessorAsserts.assertContaining(
        generatedCode, "public PersonDtoBuilder add2Nicknames(String element)");

    // Verify javadoc is present
    ProcessorAsserts.assertContaining(
        generatedCode, "Adds a single element to <code>nicknames</code>.");
  }

  @Test
  void add2Method_generatedForSetField() {
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "PersonDto",
            """
                private final String name;
                private final java.util.Set<String> tags;

                public PersonDto(String name, java.util.Set<String> tags) {
                  this.name = name;
                  this.tags = tags;
                }

                public String getName() {
                  return name;
                }

                public java.util.Set<String> getTags() {
                  return tags;
                }
            """);

    Compilation compilation = compile(dto);
    String generatedCode = loadGeneratedSource(compilation, "PersonDtoBuilder");
    assertGenerationSucceeded(compilation, "PersonDtoBuilder", generatedCode);

    // Verify add2Tags method is generated
    ProcessorAsserts.assertContaining(
        generatedCode, "public PersonDtoBuilder add2Tags(String element)");
  }

  @Test
  void add2Method_worksWithComplexElementTypes() {
    JavaFileObject taskDto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "TaskDto",
            """
                private final String title;

                public TaskDto(String title) {
                  this.title = title;
                }

                public String getTitle() {
                  return title;
                }
            """);

    JavaFileObject projectDto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "ProjectDto",
            """
                private final String name;
                private final java.util.List<TaskDto> tasks;

                public ProjectDto(String name, java.util.List<TaskDto> tasks) {
                  this.name = name;
                  this.tasks = tasks;
                }

                public String getName() {
                  return name;
                }

                public java.util.List<TaskDto> getTasks() {
                  return tasks;
                }
            """);

    Compilation compilation = compile(taskDto, projectDto);
    String generatedCode = loadGeneratedSource(compilation, "ProjectDtoBuilder");
    assertGenerationSucceeded(compilation, "ProjectDtoBuilder", generatedCode);

    // Verify add2Tasks method is generated with correct element type
    ProcessorAsserts.assertContaining(
        generatedCode, "public ProjectDtoBuilder add2Tasks(TaskDto element)");
  }

  @Test
  void add2Method_ignoresSetterSuffix() {
    JavaFileObject dto =
        com.google.testing.compile.JavaFileObjects.forSourceLines(
            "test.PersonDto",
            "package test;",
            "import java.util.List;",
            "@org.javahelpers.simple.builders.core.annotations.SimpleBuilder(",
            "  options = @org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Options(",
            "    setterSuffix = \"with\"",
            "  )",
            ")",
            "public class PersonDto {",
            "  private final String name;",
            "  private final List<String> nicknames;",
            "",
            "  public PersonDto(String name, List<String> nicknames) {",
            "    this.name = name;",
            "    this.nicknames = nicknames;",
            "  }",
            "",
            "  public String getName() {",
            "    return name;",
            "  }",
            "",
            "  public List<String> getNicknames() {",
            "    return nicknames;",
            "  }",
            "}");

    Compilation compilation = compile(dto);
    String generatedCode = loadGeneratedSource(compilation, "PersonDtoBuilder");
    assertGenerationSucceeded(compilation, "PersonDtoBuilder", generatedCode);

    // Verify regular setter respects setterSuffix
    ProcessorAsserts.assertContaining(
        generatedCode, "public PersonDtoBuilder withName(String name)");
    ProcessorAsserts.assertContaining(
        generatedCode, "public PersonDtoBuilder withNicknames(List<String> nicknames)");

    // Verify add2 method does NOT respect setterSuffix - always uses add2FieldName pattern
    ProcessorAsserts.assertContaining(
        generatedCode, "public PersonDtoBuilder add2Nicknames(String element)");

    // Verify it's NOT named add2withNicknames
    ProcessorAsserts.assertNotContaining(generatedCode, "add2withNicknames");
  }
}
