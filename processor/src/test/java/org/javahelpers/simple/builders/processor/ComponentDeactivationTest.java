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
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forSourceString;

import com.google.testing.compile.Compilation;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for component deactivation functionality.
 *
 * <p>These tests verify that generators and enhancers can be deactivated via compiler arguments.
 */
class ComponentDeactivationTest {

  private static final String TEST_DTO_SOURCE =
      "package test;\n"
          + "import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;\n"
          + "@SimpleBuilder\n"
          + "public class TestDto {\n"
          + "  private String name;\n"
          + "  private int age;\n"
          + "  public String getName() { return name; }\n"
          + "  public void setName(String name) { this.name = name; }\n"
          + "  public int getAge() { return age; }\n"
          + "  public void setAge(int age) { this.age = age; }\n"
          + "}";

  @ParameterizedTest
  @ValueSource(
      strings = {
        "ConditionalEnhancer",
        "*HelperGenerator",
        "*ConsumerGenerator",
        "StringFormatHelperGenerator,VarArgsHelperGenerator,ConditionalEnhancer",
        "String*",
        "NonExistentGenerator"
      })
  void testDeactivateGenerationComponents(String deactivationPatterns) {
    Compilation compilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=" + deactivationPatterns)
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(compilation).succeeded();

    String generatedBuilder = ProcessorTestUtils.loadGeneratedSource(compilation, "TestDtoBuilder");

    // Basic setters should always be present (unless BasicSetterGenerator is deactivated)
    ProcessorAsserts.assertContaining(generatedBuilder, "name(");
    ProcessorAsserts.assertContaining(generatedBuilder, "age(");

    // Verify specific deactivations based on patterns
    if (deactivationPatterns.contains("ConditionalEnhancer")) {
      ProcessorAsserts.assertNotContaining(generatedBuilder, "conditional(BooleanSupplier");
      ProcessorAsserts.assertNotContaining(generatedBuilder, "conditional(");
    }

    if (deactivationPatterns.contains("*HelperGenerator")) {
      ProcessorAsserts.assertNotContaining(generatedBuilder, "stringFormat(");
      ProcessorAsserts.assertNotContaining(generatedBuilder, "varArgs(");
    }

    if (deactivationPatterns.contains("*ConsumerGenerator")) {
      ProcessorAsserts.assertNotContaining(generatedBuilder, "nameConsumer(");
      ProcessorAsserts.assertNotContaining(generatedBuilder, "ageConsumer(");
      ProcessorAsserts.assertNotContaining(generatedBuilder, "builderConsumer(");
    }

    if (deactivationPatterns.contains("String*")) {
      ProcessorAsserts.assertNotContaining(generatedBuilder, "stringFormat(");
      ProcessorAsserts.assertNotContaining(generatedBuilder, "stringBuilderConsumer(");
    }

    // Always verify generation succeeded
    ProcessorAsserts.assertGenerationSucceeded(compilation, "TestDtoBuilder", generatedBuilder);
  }
}
