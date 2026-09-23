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

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * End-to-end coverage of {@code @SimpleBuilderFor}: generating builders for external types that
 * cannot be annotated directly, declared on a holder class whose package receives the generated
 * builders.
 */
class SimpleBuilderForTest {

  @Test
  void singleType_GeneratesBuilderInHolderPackage() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(externalDto(), holder("test", "Builders", "ext.ExternalUser"));

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ExternalUserBuilder", generated);
    ProcessorAsserts.assertContaining(
        generated,
        "package test;",
        "import ext.ExternalUser;",
        "public ExternalUserBuilder name(String name)");
    // The holder itself must not get a builder - it carries no template annotation
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "Builders", "The @SimpleBuilderFor holder must not get a builder");
  }

  @Test
  void multipleTypes_GeneratesBuilderForEach() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor({ext.ExternalUser.class, ext.ExternalOrder.class})
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(externalDto(), externalOrder(), holder);

    assertThat(compilation).succeededWithoutWarnings();
    String userBuilder = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    String orderBuilder =
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalOrderBuilder");
    ProcessorAsserts.assertContaining(userBuilder, "package test;");
    ProcessorAsserts.assertContaining(
        orderBuilder, "package test;", "public ExternalOrder build()");
  }

  @Test
  void options_HonourBuilderSuffix() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(
                value = ext.ExternalUser.class,
                options = @SimpleBuilder.Options(builderSuffix = "Factory"))
            public class Builders {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(externalDto(), holder);

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserFactory");
    ProcessorAsserts.assertContaining(
        generated, "public class ExternalUserFactory", "public ExternalUser build()");
  }

  @Test
  void noAccessibleConstructor_WarnsAndGeneratesNoBuilder() {
    JavaFileObject unconstructable =
        ProcessorTestUtils.forSource(
            """
            package ext;
            public class Singleton {
              private Singleton() {}
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(unconstructable, holder("test", "Builders", "ext.Singleton"));

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("Failed to generate builder");
    assertThat(compilation).hadWarningContaining("No accessible constructor");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "Singleton", "A type without accessible constructor must not get a builder");
  }

  @Test
  void unresolvableType_ProducesClearWarning() {
    // javac itself rejects naming a package-private type of another package; the processor
    // must still degrade gracefully with a clear diagnostic instead of crashing.
    JavaFileObject invisible =
        ProcessorTestUtils.forSource(
            """
            package ext;
            class Hidden {
              public Hidden() {}
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(invisible, holder("test", "Builders", "ext.Hidden"));

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("is not public in ext");
    assertThat(compilation).hadWarningContaining("could not be resolved to a type");
  }

  @Test
  void strictMode_GenerationFailureFailsCompilation() {
    JavaFileObject unconstructable =
        ProcessorTestUtils.forSource(
            """
            package ext;
            public class Singleton {
              private Singleton() {}
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.strict=true")
            .compile(unconstructable, holder("test", "Builders", "ext.Singleton"));

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Failed to generate builder");
    assertThat(compilation).hadErrorContaining("No accessible constructor");
  }

  @Test
  void inaccessibleMembers_AreNotExposedInBuilder() {
    JavaFileObject external =
        ProcessorTestUtils.forSource(
            """
            package ext;
            public class Mixed {
              private String name;
              private String secret;
              public Mixed() {}
              public void setName(String name) { this.name = name; }
              void setSecret(String secret) { this.secret = secret; }
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(external, holder("test", "Builders", "ext.Mixed"));

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "MixedBuilder");
    ProcessorAsserts.assertContaining(generated, "public MixedBuilder name(String name)");
    // The package-private setter of the foreign package cannot be called from the builder
    ProcessorAsserts.assertNotContaining(generated, "secret(");
  }

  @Test
  void generatedBuilder_IsUsedByOtherGeneratedBuilders() {
    JavaFileObject dto =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import ext.ExternalUser;
            @SimpleBuilder
            public class OrderDto {
              private ExternalUser user;
              public ExternalUser getUser() { return user; }
              public void setUser(ExternalUser user) { this.user = user; }
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(externalDto(), dto, holder("test", "Builders", "ext.ExternalUser"));

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "OrderDtoBuilder");
    ProcessorAsserts.assertContaining(
        generated, "userBuilderConsumer", "ExternalUserBuilder builder");
  }

  @Test
  void ignore4BuilderGenerationOnTarget_SkipsWithWarning() {
    JavaFileObject optedOut =
        ProcessorTestUtils.forSource(
            """
            package ext;
            import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
            @Ignore4BuilderGeneration
            public class OptedOut {
              public OptedOut() {}
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(optedOut, holder("test", "Builders", "ext.OptedOut"));

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("@Ignore4BuilderGeneration");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "OptedOut", "An opted-out type must not get a builder");
  }

  @Test
  void builderNameCollision_OnlyOneBuilderGeneratedWithWarning() {
    JavaFileObject secondHolder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(ext.ExternalUser.class)
            public class MoreBuilders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(externalDto(), holder("test", "Builders", "ext.ExternalUser"), secondHolder);

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("already generated elsewhere");
    // Exactly one ExternalUserBuilder was generated in package test
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(generated, "package test;");
  }

  @Test
  void generationScope_DoesNotBlockExplicitDeclarationButWarns() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderGenerationPackages=other.pkg")
            .compile(externalDto(), holder("test", "Builders", "ext.ExternalUser"));

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("outside builderGenerationPackages");
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(generated, "package test;");
  }

  @Test
  void recordTarget_GeneratesBuilder() {
    JavaFileObject externalRecord =
        ProcessorTestUtils.forSource(
            """
            package ext;
            public record ExternalPoint(int x, int y) {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(externalRecord, holder("test", "Builders", "ext.ExternalPoint"));

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalPointBuilder");
    ProcessorAsserts.assertContaining(generated, "package test;", "public ExternalPoint build()");
  }

  @Test
  void packageInfo_GeneratesBuildersIntoAnnotatedPackage() {
    JavaFileObject packageInfo =
        JavaFileObjects.forSourceLines(
            "test.package-info",
            "@SimpleBuilderFor(ext.ExternalUser.class)",
            "package test;",
            "",
            "import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;");

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(externalDto(), packageInfo);

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(
        generated, "package test;", "public ExternalUserBuilder name(String name)");
  }

  private static JavaFileObject externalDto() {
    return ProcessorTestUtils.forSource(
        """
        package ext;
        public class ExternalUser {
          private String name;
          private int age;
          public ExternalUser() {}
          public String getName() { return name; }
          public void setName(String name) { this.name = name; }
          public int getAge() { return age; }
          public void setAge(int age) { this.age = age; }
        }
        """);
  }

  private static JavaFileObject externalOrder() {
    return ProcessorTestUtils.forSource(
        """
        package ext;
        public class ExternalOrder {
          private String id;
          public ExternalOrder() {}
          public String getId() { return id; }
          public void setId(String id) { this.id = id; }
        }
        """);
  }

  private static JavaFileObject holder(String pkg, String className, String targetType) {
    return ProcessorTestUtils.forSource(
        """
        package %s;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
        @SimpleBuilderFor(%s.class)
        public class %s {}
        """
            .formatted(pkg, targetType, className));
  }
}
