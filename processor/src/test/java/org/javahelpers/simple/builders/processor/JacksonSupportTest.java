/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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
import javax.tools.JavaFileObject;
import org.javahelpers.codegen.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonSupportTest {

  @Test
  void usingJacksonDeserializerAnnotation_WhenEnabled_ShouldGenerateAnnotation() {
    // Given
    JavaFileObject source =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "JacksonDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.usingJacksonDeserializerAnnotation=true")
            .compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode = ProcessorTestUtils.loadGeneratedSource(compilation, "JacksonDtoBuilder");

    ProcessorAsserts.assertContaining(
        generatedCode,
        "import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;",
        "@JsonPOJOBuilder(withPrefix = \"\")");
  }

  @Test
  void
      usingJacksonDeserializerAnnotation_WhenEnabledWithCustomPrefix_ShouldGenerateAnnotationWithPrefix() {
    // Given
    JavaFileObject source =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "JacksonCustomDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true",
                "-Asimplebuilder.setterPrefix=with")
            .compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "JacksonCustomDtoBuilder");

    ProcessorAsserts.assertContaining(generatedCode, "@JsonPOJOBuilder(withPrefix = \"with\")");
  }

  @Test
  void usingJacksonDeserializerAnnotation_WhenDisabled_ShouldNotGenerateAnnotation() {
    // Given
    JavaFileObject source =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "NoJacksonDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            // usingJacksonDeserializerAnnotation is disabled by default, but we can explicitly
            // disable
            // it
            .withOptions("-Asimplebuilder.usingJacksonDeserializerAnnotation=false")
            .compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "NoJacksonDtoBuilder");

    ProcessorAsserts.assertNotContaining(generatedCode, "JsonPOJOBuilder");
  }

  @Test
  void usingJacksonDeserializerAnnotation_WhenNotConfigured_ShouldDefaultToDisabled() {
    // Given
    JavaFileObject source =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "DefaultJacksonDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            // No options provided
            .compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "DefaultJacksonDtoBuilder");

    ProcessorAsserts.assertNotContaining(generatedCode, "JsonPOJOBuilder");
  }
}
