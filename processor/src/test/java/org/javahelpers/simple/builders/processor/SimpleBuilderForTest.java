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
  void ignore4BuilderGenerationOnTarget_ExplicitDeclarationStillGenerates() {
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

    // The explicit @SimpleBuilderFor declaration wins - the target type's own annotations
    // (including @Ignore4BuilderGeneration) are not consulted
    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "OptedOutBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "OptedOutBuilder", generated);
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

  @Test
  void packageName_GeneratesBuilderIntoConfiguredPackage() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(
                value = ext.ExternalUser.class,
                options = @SimpleBuilder.Options(packageName = "com.example.generated"))
            public class Builders {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(externalDto(), holder);

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(
        generated,
        "package com.example.generated;",
        "import ext.ExternalUser;",
        "public ExternalUserBuilder name(String name)");
  }

  @Test
  void packageName_Invalid_ProducesDiagnosticAndNoBuilder() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(
                value = ext.ExternalUser.class,
                options = @SimpleBuilder.Options(packageName = "com.example..broken"))
            public class Builders {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(externalDto(), holder);

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("is not a valid Java package name");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "ExternalUserBuilder", "An invalid packageName must not produce a builder");
  }

  @Test
  void packageName_PackagePrivateConstructorIsNotReachable() {
    // With the builder placed in another package, package-private members of the target are no
    // longer reachable - they are treated as if absent, so no accessible constructor remains.
    JavaFileObject packagePrivateCtor =
        ProcessorTestUtils.forSource(
            """
            package ext;
            public class ExternalWidget {
              ExternalWidget() {}
            }
            """);
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(
                value = ext.ExternalWidget.class,
                options = @SimpleBuilder.Options(packageName = "com.example.generated"))
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(packagePrivateCtor, holder);

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("No accessible constructor");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "ExternalWidgetBuilder", "No accessible constructor - no builder");
  }

  @Test
  void packageName_GlobalCompilerArgumentIsIgnored() {
    // packageName is annotation-only: a global -A/-D value must not collapse all builders into
    // a single package.
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.packageName=com.example.generated")
            .compile(externalDto(), holder("test", "Builders", "ext.ExternalUser"));

    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(generated, "package test;");
  }

  @Test
  void repeatable_GeneratesBuildersIntoDifferentPackages() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(
                value = ext.ExternalUser.class,
                options = @SimpleBuilder.Options(packageName = "com.example.users"))
            @SimpleBuilderFor(
                value = ext.ExternalOrder.class,
                options = @SimpleBuilder.Options(packageName = "com.example.orders"))
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(externalDto(), externalOrderDto(), holder);

    assertThat(compilation).succeededWithoutWarnings();
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "users/ExternalUserBuilder"),
        "package com.example.users;");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "orders/ExternalOrderBuilder"),
        "package com.example.orders;");
  }

  @Test
  void repeatable_OnPackageInfo_GeneratesBuildersIntoAnnotatedPackage() {
    JavaFileObject packageInfo =
        JavaFileObjects.forSourceLines(
            "test.package-info",
            "@SimpleBuilderFor(ext.ExternalUser.class)",
            "@SimpleBuilderFor(ext.ExternalOrder.class)",
            "package test;",
            "",
            "import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;");

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(externalDto(), externalOrderDto(), packageInfo);

    assertThat(compilation).succeededWithoutWarnings();
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder"),
        "package test;");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalOrderBuilder"),
        "package test;");
  }

  @Test
  void repeatable_DuplicateType_SecondDeclarationSkippedWithWarning() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(ext.ExternalUser.class)
            @SimpleBuilderFor(ext.ExternalUser.class)
            public class Builders {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(externalDto(), holder);

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("already generated elsewhere");
    // Exactly one ExternalUserBuilder was generated in package test
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(generated, "package test;");
  }

  @Test
  void sourcePackages_GeneratesBuildersForTopLevelConstructibleTypes() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(sourcePackages = "pkg")
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(pkgUser(), pkgOrder(), pkgInterface(), pkgEnum(), pkgAbstract(), holder);

    assertThat(compilation).succeededWithoutWarnings();
    // Only the two concrete top-level classes get builders; interface, enum and abstract class
    // are skipped
    String userBuilder = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    String orderBuilder =
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalOrderBuilder");
    ProcessorAsserts.assertContaining(userBuilder, "package test;", "public ExternalUser build()");
    ProcessorAsserts.assertContaining(
        orderBuilder, "package test;", "public ExternalOrder build()");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "Contract", "an interface cannot be built");
    ProcessorAsserts.assertNoBuilderGenerated(compilation, "State", "an enum cannot be built");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "BaseDto", "an abstract class cannot be built");
  }

  @Test
  void sourcePackages_NestedTypeKeepsRequiringExplicitEntry() {
    JavaFileObject withNested =
        ProcessorTestUtils.forSource(
            """
            package pkg;
            public class Wrapper {
              public Wrapper() {}
              public static class Inner {
                public Inner() {}
              }
            }
            """);
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(sourcePackages = "pkg")
            public class Builders {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(withNested, holder);

    assertThat(compilation).succeededWithoutWarnings();
    // Only the top-level type is picked up; its nested class is not
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "WrapperBuilder");
    ProcessorAsserts.assertContaining(generated, "public Wrapper build()");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "Inner", "nested types keep requiring an explicit value entry");
  }

  @Test
  void sourcePackages_WithOptions_GeneratesIntoOptionsPackage() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(
                sourcePackages = "pkg",
                options = @SimpleBuilder.Options(packageName = "com.example.generated"))
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(pkgUser(), pkgOrder(), holder);

    assertThat(compilation).succeededWithoutWarnings();
    // Every scanned type routes into the options package, as with value-listed types
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "generated/ExternalUserBuilder"),
        "package com.example.generated;");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "generated/ExternalOrderBuilder"),
        "package com.example.generated;");
  }

  @Test
  void sourcePackages_TypeAlsoInValue_GeneratesSingleBuilder() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(value = pkg.ExternalUser.class, sourcePackages = "pkg")
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(pkgUser(), pkgOrder(), holder);

    // The explicit listing and the scan overlap on ExternalUser: exactly one builder, no
    // duplicate warning
    assertThat(compilation).succeededWithoutWarnings();
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(generated, "package test;", "public ExternalUser build()");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalOrderBuilder"),
        "public ExternalOrder build()");
  }

  @Test
  void sourcePackages_Multiple_GeneratesFromAllListedPackages() {
    JavaFileObject otherType =
        ProcessorTestUtils.forSource(
            """
            package other;
            public class ExternalCustomer {
              private String id;
              public ExternalCustomer() {}
              public String getId() { return id; }
              public void setId(String id) { this.id = id; }
            }
            """);
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(sourcePackages = {"pkg", "other"})
            public class Builders {}
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(pkgUser(), pkgOrder(), otherType, holder);

    assertThat(compilation).succeededWithoutWarnings();
    // Every listed package contributes its top-level classes into the holder's package
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder"),
        "import pkg.ExternalUser;");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalOrderBuilder"),
        "import pkg.ExternalOrder;");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalCustomerBuilder"),
        "import other.ExternalCustomer;");
  }

  @Test
  void sourcePackages_Unresolvable_WarnsAndGeneratesFromValue() {
    JavaFileObject holder =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
            @SimpleBuilderFor(value = ext.ExternalUser.class, sourcePackages = "does.not.exist")
            public class Builders {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(externalDto(), holder);

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("could not be resolved");
    // The unresolvable package is skipped; the value-listed type still generates
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, "ExternalUserBuilder");
    ProcessorAsserts.assertContaining(generated, "public ExternalUser build()");
  }

  private static JavaFileObject externalOrderDto() {
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

  private static JavaFileObject pkgUser() {
    return ProcessorTestUtils.forSource(
        """
        package pkg;
        public class ExternalUser {
          private String name;
          public ExternalUser() {}
          public String getName() { return name; }
          public void setName(String name) { this.name = name; }
        }
        """);
  }

  private static JavaFileObject pkgOrder() {
    return ProcessorTestUtils.forSource(
        """
        package pkg;
        public class ExternalOrder {
          private String id;
          public ExternalOrder() {}
          public String getId() { return id; }
          public void setId(String id) { this.id = id; }
        }
        """);
  }

  private static JavaFileObject pkgInterface() {
    return ProcessorTestUtils.forSource(
        """
        package pkg;
        public interface Contract {
          String getName();
        }
        """);
  }

  private static JavaFileObject pkgEnum() {
    return ProcessorTestUtils.forSource(
        """
        package pkg;
        public enum State {
          OPEN, CLOSED
        }
        """);
  }

  private static JavaFileObject pkgAbstract() {
    return ProcessorTestUtils.forSource(
        """
        package pkg;
        public abstract class BaseDto {
          public BaseDto() {}
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
