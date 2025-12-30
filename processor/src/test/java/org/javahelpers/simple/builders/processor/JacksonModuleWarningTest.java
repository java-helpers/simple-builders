package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonModuleWarningTest {

  @Test
  void generateJacksonModule_WhenEnabledButDeserializerDisabled_ShouldWarnAndNotGenerate() {
    // Given
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "WarningDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=false")
            .compile(dto);

    // Then
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .hadWarningContaining(
            "simple-builders: generateJacksonModule is enabled but usingJacksonDeserializerAnnotation is disabled");

    // Verify file is NOT generated
    try {
      ProcessorTestUtils.loadGeneratedSource(compilation, "SimpleBuildersJacksonModule");
      throw new AssertionError("SimpleBuildersJacksonModule should not have been generated");
    } catch (AssertionError e) {
      // Expected
    }
  }

  @Test
  void generateJacksonModule_WhenEnabledAndDeserializerEnabled_ShouldGenerate() {
    // Given
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "SuccessDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true")
            .compile(dto);

    // Then
    assertThat(compilation).succeeded();

    // Verify file IS generated
    ProcessorTestUtils.loadGeneratedSource(compilation, "SimpleBuildersJacksonModule");
  }
}
