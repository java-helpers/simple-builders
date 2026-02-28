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
import org.junit.jupiter.api.Test;

/**
 * Tests for component deactivation functionality.
 *
 * <p>These tests verify that generators and enhancers can be deactivated via compiler arguments.
 */
class ComponentDeactivationTest {

  private static final String TEST_DTO_SOURCE =
      """
      package test;
      import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
      @SimpleBuilder
      public class TestDto {
        private String name;
        private int age;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
      }
      """;

  @Test
  void testDeactivateConditionalEnhancer() {
    Compilation testCompilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=ConditionalEnhancer")
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(testCompilation).succeeded();
    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");

    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(BooleanSupplier");
    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);
  }

  @Test
  void testDeactivateAllHelperGenerators() {
    Compilation testCompilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=*HelperGenerator")
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(testCompilation).succeeded();
    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");

    ProcessorAsserts.assertNotContaining(testBuilder, "stringFormat(");
    ProcessorAsserts.assertNotContaining(testBuilder, "varArgs(");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);
  }

  @Test
  void testDeactivateAllConsumerGenerators() {
    Compilation testCompilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=*ConsumerGenerator")
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(testCompilation).succeeded();
    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");

    ProcessorAsserts.assertNotContaining(testBuilder, "nameConsumer(");
    ProcessorAsserts.assertNotContaining(testBuilder, "ageConsumer(");
    ProcessorAsserts.assertNotContaining(testBuilder, "builderConsumer(");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);
  }

  @Test
  void testDeactivateMultipleSpecificGenerators() {
    Compilation testCompilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions(
                "-Asimplebuilder.deactivateGenerationComponents=StringFormatHelperGenerator,VarArgsHelperGenerator,ConditionalEnhancer")
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(testCompilation).succeeded();
    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");

    ProcessorAsserts.assertNotContaining(testBuilder, "stringFormat(");
    ProcessorAsserts.assertNotContaining(testBuilder, "varArgs(");
    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(BooleanSupplier");
    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);
  }

  @Test
  void testDeactivateStringPatternGenerators() {
    Compilation testCompilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=String*")
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(testCompilation).succeeded();
    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");

    ProcessorAsserts.assertNotContaining(testBuilder, "stringFormat(");
    ProcessorAsserts.assertNotContaining(testBuilder, "stringBuilderConsumer(");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);
  }

  @Test
  void testDeactivateNonExistentGenerator() {
    Compilation testCompilation =
        javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=NonExistentGenerator")
            .compile(forSourceString("test.TestDto", TEST_DTO_SOURCE));

    assertThat(testCompilation).succeeded();
    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");

    // Should still generate normally since non-existent generator has no effect
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);
  }
}
