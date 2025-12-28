package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonSupportTest {

  @Test
  void jacksonSupport_WhenEnabled_ShouldGenerateAnnotation() {
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
  void jacksonSupport_WhenEnabledWithCustomPrefix_ShouldGenerateAnnotationWithPrefix() {
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
                "-Asimplebuilder.setterSuffix=with")
            .compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode =
        ProcessorTestUtils.loadGeneratedSource(compilation, "JacksonCustomDtoBuilder");

    ProcessorAsserts.assertContaining(generatedCode, "@JsonPOJOBuilder(withPrefix = \"with\")");
  }

  @Test
  void jacksonSupport_WhenDisabled_ShouldNotGenerateAnnotation() {
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
  void jacksonSupport_WhenNotConfigured_ShouldDefaultToDisabled() {
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
