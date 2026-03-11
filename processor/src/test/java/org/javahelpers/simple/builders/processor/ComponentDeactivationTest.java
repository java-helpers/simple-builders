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

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for component deactivation functionality.
 *
 * <p>These tests verify that generators and enhancers can be deactivated via compiler arguments.
 */
class ComponentDeactivationTest {

  private static final JavaFileObject TEST_DTO_SOURCE =
      ProcessorTestUtils.forSource(
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
          """);

  @Test
  void testDeactivateConditionalEnhancer() {
    Compilation testCompilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=ConditionalEnhancer")
            .compile(TEST_DTO_SOURCE);

    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);

    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(BooleanSupplier");
    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(");
  }

  @Test
  void testDeactivateAllHelperGenerators() {
    Compilation testCompilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=*HelperGenerator")
            .compile(TEST_DTO_SOURCE);

    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);

    ProcessorAsserts.assertNotContaining(testBuilder, "stringFormat(");
    ProcessorAsserts.assertNotContaining(testBuilder, "varArgs(");
  }

  @Test
  void testDeactivateAllConsumerGenerators() {
    Compilation testCompilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=*ConsumerGenerator")
            .compile(TEST_DTO_SOURCE);

    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);

    ProcessorAsserts.assertNotContaining(testBuilder, "nameConsumer(");
    ProcessorAsserts.assertNotContaining(testBuilder, "ageConsumer(");
    ProcessorAsserts.assertNotContaining(testBuilder, "builderConsumer(");
  }

  @Test
  void testDeactivateMultipleSpecificGenerators() {
    Compilation testCompilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.deactivateGenerationComponents=StringFormatHelperGenerator,VarArgsHelperGenerator,ConditionalEnhancer")
            .compile(TEST_DTO_SOURCE);

    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);

    ProcessorAsserts.assertNotContaining(testBuilder, "stringFormat(");
    ProcessorAsserts.assertNotContaining(testBuilder, "varArgs(");
    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(BooleanSupplier");
    ProcessorAsserts.assertNotContaining(testBuilder, "conditional(");
  }

  @Test
  void testDeactivateStringPatternGenerators() {
    Compilation testCompilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=String*")
            .compile(TEST_DTO_SOURCE);

    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);

    ProcessorAsserts.assertNotContaining(testBuilder, "stringFormat(");
    ProcessorAsserts.assertNotContaining(testBuilder, "stringBuilderConsumer(");
  }

  @Test
  void testDeactivateNonExistentGenerator() {
    Compilation testCompilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.deactivateGenerationComponents=NonExistentGenerator")
            .compile(TEST_DTO_SOURCE);

    String testBuilder = ProcessorTestUtils.loadGeneratedSource(testCompilation, "TestDtoBuilder");
    ProcessorAsserts.assertGenerationSucceeded(testCompilation, "TestDtoBuilder", testBuilder);

    // Should still generate normally since non-existent generator has no effect
  }
}
