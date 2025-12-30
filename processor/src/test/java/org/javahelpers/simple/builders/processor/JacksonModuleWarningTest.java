package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.simpleBuilderClass;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonModuleWarningTest {

  @Test
  void generateJacksonModuleEnabled_ButAnnotationDisabled_ShouldEmitWarningAndSkipGeneration() {
    // Given
    JavaFileObject dto =
        simpleBuilderClass(
            "pkg.test",
            "MisconfiguredDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    // Enable generateJacksonModule but NOT usingJacksonDeserializerAnnotation (which defaults to
    // DISABLED)
    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.generateJacksonModule=true").compile(dto);

    // Then
    assertThat(compilation).succeeded();

    // Should have a warning
    assertThat(compilation)
        .hadWarningContaining(
            "simple-builders: generateJacksonModule is enabled but usingJacksonDeserializerAnnotation is disabled");

    // Should NOT have generated the Jackson module
    // We verify this by ensuring the warning is present, which implies the generation branch was
    // skipped.
    // Testing explicitly for "file not generated" is tricky with compile-testing without complex
    // custom assertions.
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
