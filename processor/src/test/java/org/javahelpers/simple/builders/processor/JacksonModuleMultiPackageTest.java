package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertContaining;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonModuleMultiPackageTest {

  @Test
  void generateJacksonModule_WithMultiplePackages_ShouldGenerateMultipleModules() {
    // Given: Two DTOs in different packages, default configuration
    JavaFileObject dto1 =
        ProcessorTestUtils.simpleBuilderClass(
            "pkg.one",
            "DtoOne",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    JavaFileObject dto2 =
        ProcessorTestUtils.simpleBuilderClass(
            "pkg.two",
            "DtoTwo",
            """
            private int value;
            public int getValue() { return value; }
            public void setValue(int value) { this.value = value; }
            """);

    // When: Compile with Jackson module generation enabled, no global package set
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true")
            .compile(dto1, dto2);

    // Then: Should generate TWO modules, one in each package
    assertThat(compilation).succeeded();

    // Verify module in pkg.one
    JavaFileObject module1 =
        compilation.generatedSourceFile("pkg.one.SimpleBuildersJacksonModule").orElseThrow();
    String content1;
    try {
      content1 = module1.getCharContent(false).toString();
    } catch (Exception e) {
      content1 = module1.toString();
    }
    assertContaining(
        content1,
        "package pkg.one;",
        "import com.fasterxml.jackson.databind.annotation.JsonDeserialize;",
        "import com.fasterxml.jackson.databind.module.SimpleModule;",
        "public class SimpleBuildersJacksonModule extends SimpleModule",
        "setMixInAnnotation(DtoOne.class, DtoOneMixin.class)",
        "@JsonDeserialize",
        "builder = DtoOneBuilder.class",
        "private interface DtoOneMixin");

    // Verify module in pkg.two
    JavaFileObject module2 =
        compilation.generatedSourceFile("pkg.two.SimpleBuildersJacksonModule").orElseThrow();
    String content2;
    try {
      content2 = module2.getCharContent(false).toString();
    } catch (Exception e) {
      content2 = module2.toString();
    }
    assertContaining(
        content2,
        "package pkg.two;",
        "import com.fasterxml.jackson.databind.annotation.JsonDeserialize;",
        "import com.fasterxml.jackson.databind.module.SimpleModule;",
        "public class SimpleBuildersJacksonModule extends SimpleModule",
        "setMixInAnnotation(DtoTwo.class, DtoTwoMixin.class)",
        "@JsonDeserialize",
        "builder = DtoTwoBuilder.class",
        "private interface DtoTwoMixin");
  }

  @Test
  void generateJacksonModule_WithAnnotationOverride_ShouldRespectOverride() {
    // Given: DTO with specific package override via annotation
    // Note: We simulate this by having one DTO use default (its package) and another potentially
    // using a different one if we could mocking the annotation value,
    // but here we test the "default package" vs "configured package" behavior.
    // Actually, testing annotation override specifically requires a DTO with
    // @SimpleBuilder.Options(jacksonModulePackage="...")
    // But SimpleBuilder.Options doesn't expose jacksonModulePackage yet in the annotation
    // definition?
    // Wait, we added it to BuilderConfiguration but did we add it to the @SimpleBuilder.Options
    // annotation?
    // Checking SimpleBuilder.java...

    // We need to construct the source file manually to include the annotation with options
    // because simpleBuilderClass doesn't support adding options easily
    JavaFileObject sourceWithAnnotation =
        ProcessorTestUtils.forSource(
            """
            package pkg.source;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;

            @SimpleBuilder(options = @SimpleBuilder.Options(
                generateJacksonModule = OptionState.ENABLED,
                usingJacksonDeserializerAnnotation = OptionState.ENABLED,
                jacksonModulePackage = "pkg.target"
            ))
            public class DtoWithOverride {
                private String name;
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """);

    // When
    Compilation compilation = ProcessorTestUtils.createCompiler().compile(sourceWithAnnotation);

    // Then
    assertThat(compilation).succeeded();

    // Should generate in pkg.target, NOT pkg.source
    JavaFileObject module3 =
        compilation.generatedSourceFile("pkg.target.SimpleBuildersJacksonModule").orElseThrow();
    String content3;
    try {
      content3 = module3.getCharContent(false).toString();
    } catch (Exception e) {
      content3 = module3.toString();
    }
    assertContaining(
        content3,
        "package pkg.target;",
        "import com.fasterxml.jackson.databind.annotation.JsonDeserialize;",
        "import com.fasterxml.jackson.databind.module.SimpleModule;",
        "import pkg.source.DtoWithOverride;",
        "import pkg.source.DtoWithOverrideBuilder;",
        "public class SimpleBuildersJacksonModule extends SimpleModule",
        "setMixInAnnotation(DtoWithOverride.class, DtoWithOverrideMixin.class)",
        "@JsonDeserialize",
        "builder = DtoWithOverrideBuilder.class",
        "private interface DtoWithOverrideMixin");
  }
}
