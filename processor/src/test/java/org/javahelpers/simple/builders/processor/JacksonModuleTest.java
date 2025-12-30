package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonModuleTest {

  @Test
  void generateJacksonModule_WhenEnabled_ShouldGenerateModuleClass() {
    // Given
    JavaFileObject dto1 =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "FirstDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    JavaFileObject dto2 =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "SecondDto",
            """
            private int age;
            public int getAge() { return age; }
            public void setAge(int age) { this.age = age; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true")
            .compile(dto1, dto2);

    // Then
    assertThat(compilation).succeeded();

    // Check for generated module class
    // It should be in the same package as the first DTO ("test")
    String generatedModule =
        ProcessorTestUtils.loadGeneratedSource(compilation, "SimpleBuildersJacksonModule");

    ProcessorAsserts.assertContaining(
        generatedModule,
        "package test;",
        "public class SimpleBuildersJacksonModule extends SimpleModule",
        "setMixInAnnotation(FirstDto.class, FirstDtoMixin.class)",
        "setMixInAnnotation(SecondDto.class, SecondDtoMixin.class)",
        "@JsonDeserialize(builder = FirstDtoBuilder.class)",
        "@JsonDeserialize(builder = SecondDtoBuilder.class)");
  }

  @Test
  void generateJacksonModule_WhenDisabled_ShouldNotGenerateModuleClass() {
    // Given
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "ModuleDisabledDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.generateJacksonModule=false")
            .compile(dto);

    // Then
    assertThat(compilation).succeeded();

    // Verify file is NOT generated
    try {
      ProcessorTestUtils.loadGeneratedSource(compilation, "SimpleBuildersJacksonModule");
      throw new AssertionError("SimpleBuildersJacksonModule should not have been generated");
    } catch (AssertionError e) {
      // Expected
    }
  }
}
