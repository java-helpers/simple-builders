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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.analysis.BuilderScopeResolver;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;
import org.javahelpers.simple.builders.processor.processing.ProcessingTarget;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Probe-based coverage of {@link BuilderScopeResolver} internals that generated-source assertions
 * cannot observe: result caching, cache invalidation on configuration change, and per-round
 * registration of generated types. End-to-end scope behavior visible in generated builders is
 * covered by {@link BuilderScopeProcessingTest}.
 */
class BuilderScopeResolverTest {

  @BeforeEach
  void resetProbe() {
    ResolverProbeProcessor.reset();
  }

  @Test
  void resolverReturnsEmptyAfterConfigurationChangesToExcludePackage() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                    @SimpleBuilder
                    public class LibHelper { public LibHelper() {} }
                    """));

    assertThat(compilation).succeeded();
    // First resolution with generation scope "lib" and registered → builder found
    assertEquals(
        "lib.LibHelperBuilder",
        ResolverProbeProcessor.first.get().typeName().getFullQualifiedName());
    // After clearing registration and changing config to exclude "lib", the resolver returns
    // empty (not registered, not in scope)
    assertEquals(Optional.empty(), ResolverProbeProcessor.afterConfigurationChange);
    // Cache was cleared by the config change, so a new Optional instance is returned
    assertNotSame(ResolverProbeProcessor.first, ResolverProbeProcessor.afterConfigurationChange);
  }

  @Test
  void resolverCachesResolvedOptionalInstancePerReferencedType() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                    @SimpleBuilder
                    public class LibHelper { public LibHelper() {} }
                    """));

    assertThat(compilation).succeeded();
    // Two consecutive calls with the same config return the same cached Optional instance
    assertSame(ResolverProbeProcessor.first, ResolverProbeProcessor.second);
  }

  @Test
  void resolverClearsCacheOnRegistrationAndResolvesUsageScope() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                    @SimpleBuilder
                    public class LibHelper { public LibHelper() {} }
                    """));

    assertThat(compilation).succeeded();
    // With usage scope "other" (not "lib") and no registration → empty
    assertEquals(Optional.empty(), ResolverProbeProcessor.beforeRegistration);
    // Registration alone is not enough — the type must also be in the usage scope
    assertEquals(Optional.empty(), ResolverProbeProcessor.afterRegistration);
    // With usage scope "lib" but no registration → empty (builder not on classpath)
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageBeforeRegistration);
    // With usage scope "lib" AND registration → builder resolved
    assertEquals(
        "lib.LibHelperBuilder",
        ResolverProbeProcessor.usageAfterRegistration.get().typeName().getFullQualifiedName());
  }

  private static final String LIB_HELPER =
      """
      package lib;
      public class LibHelper { public LibHelper() {} }
      """;

  @ParameterizedTest(name = "{0}")
  @MethodSource("resolvedContractVariants")
  void resolverUsageScope_ResolvesContractVariants(
      String name,
      String builderSource,
      Supplier<Optional<ResolvedBuilder>> probeResult,
      String expectedBuilder,
      Class<?> expectedEmptyPath,
      String expectedEmptyMethod,
      Class<?> expectedPrefilledPath,
      String expectedPrefilledMethod) {
    Compilation compilation = compileWithBuilder(builderSource);

    assertThat(compilation).succeeded();
    ResolvedBuilder resolved = probeResult.get().get();
    assertEquals(expectedBuilder, resolved.typeName().getFullQualifiedName());
    BuilderInstantiation empty = resolved.funcForEmptyBuilder();
    BuilderInstantiation prefilled = resolved.funcForPrefilledBuilder();
    assertInstanceOf(expectedEmptyPath, empty);
    assertInstanceOf(expectedPrefilledPath, prefilled);
    assertFactoryMethod(expectedEmptyMethod, empty);
    assertFactoryMethod(expectedPrefilledMethod, prefilled);
  }

  static Stream<Arguments> resolvedContractVariants() {
    return Stream.of(
        // Usage scope without @SimpleBuilder annotation — builder resolved by contract check
        Arguments.of(
            "BuilderWithoutSimpleBuilderAnnotation",
            """
            package lib;
            public class LibHelperBuilder {
              public LibHelperBuilder() {}
              public LibHelperBuilder(LibHelper value) {}
              public LibHelper build() { return new LibHelper(); }
            }
            """,
            probe(() -> ResolverProbeProcessor.usageWithoutAnnotation),
            "lib.LibHelperBuilder",
            BuilderInstantiation.ConstructorCall.class,
            null,
            BuilderInstantiation.ConstructorCall.class,
            null),
        // No accessible constructors: both instantiation paths come from the static factories
        Arguments.of(
            "StaticFactories",
            """
            package lib;
            public class LibHelperBuilder {
              private LibHelperBuilder() {}
              public static LibHelperBuilder create() { return new LibHelperBuilder(); }
              public static LibHelperBuilder of(LibHelper value) { return create(); }
              public LibHelper build() { return new LibHelper(); }
            }
            """,
            probe(() -> ResolverProbeProcessor.usageWithoutAnnotation),
            "lib.LibHelperBuilder",
            BuilderInstantiation.StaticFactoryCall.class,
            "create",
            BuilderInstantiation.StaticFactoryCall.class,
            "of"),
        // Package-private contract members are accessible when the generated builder is in
        // the same package (builderPackage "lib")
        Arguments.of(
            "PackagePrivateMembers_SamePackage",
            """
            package lib;
            public class LibHelperBuilder {
              LibHelperBuilder() {}
              LibHelperBuilder(LibHelper value) {}
              LibHelper build() { return new LibHelper(); }
            }
            """,
            probe(() -> ResolverProbeProcessor.usagePackagePrivate),
            "lib.LibHelperBuilder",
            BuilderInstantiation.ConstructorCall.class,
            null,
            BuilderInstantiation.ConstructorCall.class,
            null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rejectedContractVariants")
  void resolverUsageScope_RejectsContractVariants(
      String name, String builderSource, Supplier<Optional<ResolvedBuilder>> probeResult) {
    assertThat(compileWithBuilder(builderSource)).succeeded();
    assertEquals(Optional.empty(), probeResult.get());
  }

  static Stream<Arguments> rejectedContractVariants() {
    return Stream.of(
        // ctor(T) + build() but no no-arg ctor: generated consumer code calls `new
        // LibHelperBuilder()`, so the builder must not qualify
        Arguments.of(
            "BuilderWithoutNoArgConstructor",
            """
            package lib;
            public class LibHelperBuilder {
              public LibHelperBuilder(LibHelper value) {}
              public LibHelper build() { return new LibHelper(); }
            }
            """,
            probe(() -> ResolverProbeProcessor.usageWithoutAnnotation)),
        // The no-arg ctor exists but is private: generated code could not call it
        Arguments.of(
            "BuilderWithPrivateConstructor",
            """
            package lib;
            public class LibHelperBuilder {
              private LibHelperBuilder() {}
              public LibHelperBuilder(LibHelper value) {}
              public LibHelper build() { return new LibHelper(); }
            }
            """,
            probe(() -> ResolverProbeProcessor.usageWithoutAnnotation)),
        // Package-private contract members are not accessible from a different package
        // (builderPackage unset)
        Arguments.of(
            "PackagePrivateMembers_OtherPackage",
            """
            package lib;
            public class LibHelperBuilder {
              LibHelperBuilder() {}
              LibHelperBuilder(LibHelper value) {}
              LibHelper build() { return new LibHelper(); }
            }
            """,
            probe(() -> ResolverProbeProcessor.usageWithoutAnnotation)));
  }

  private static Compilation compileWithBuilder(String builderSource) {
    return Compiler.javac()
        .withProcessors(new ResolverProbeProcessor())
        .compile(
            ProcessorTestUtils.forSource(LIB_HELPER), ProcessorTestUtils.forSource(builderSource));
  }

  private static Supplier<Optional<ResolvedBuilder>> probe(
      Supplier<Optional<ResolvedBuilder>> field) {
    return field;
  }

  private static void assertFactoryMethod(
      String expectedMethod, BuilderInstantiation instantiation) {
    if (expectedMethod != null) {
      assertEquals(
          expectedMethod,
          assertInstanceOf(BuilderInstantiation.StaticFactoryCall.class, instantiation)
              .methodName());
    }
  }

  @Test
  void resolverUsageScope_UsesBuilderUsageSuffixWhenConfigured() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper { public LibHelper() {} }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelperFactory {
                      public LibHelperFactory() {}
                      public LibHelperFactory(LibHelper value) {}
                      public LibHelper build() { return new LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // With builderUsageSuffix="Factory", the candidate name uses "Factory"
    assertEquals(
        "lib.LibHelperFactory",
        ResolverProbeProcessor.usageWithSuffix.get().typeName().getFullQualifiedName());
  }

  @Test
  void resolverUsageScope_FallsBackToBuilderSuffixWhenUsageSuffixNotSet() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                    @SimpleBuilder
                    public class LibHelper { public LibHelper() {} }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelperBuilder {
                      public LibHelperBuilder() {}
                      public LibHelperBuilder(LibHelper value) {}
                      public LibHelper build() { return new LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // Without builderUsageSuffix, the candidate name uses builderSuffix ("Builder")
    assertEquals(
        "lib.LibHelperBuilder",
        ResolverProbeProcessor.usageDefaultSuffix.get().typeName().getFullQualifiedName());
  }

  @Test
  void resolverUsageScope_ResolvesBuilderAnchoredInsideType() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static builders.LibHelperBuilder builder() {
                        return new builders.LibHelperBuilder();
                      }
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package builders;
                    public class LibHelperBuilder {
                      public LibHelperBuilder() {}
                      public LibHelperBuilder(lib.LibHelper value) {}
                      public lib.LibHelper build() { return new lib.LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // The type's own builder() declaration anchors the builder, even in another package:
    // the empty path calls LibHelper.builder(), the seeded path the builder's ctor
    ResolvedBuilder resolved = ResolverProbeProcessor.usageWithoutAnnotation.get();
    assertEquals("builders.LibHelperBuilder", resolved.typeName().getFullQualifiedName());
    BuilderInstantiation.AnchorFactoryCall empty =
        assertInstanceOf(
            BuilderInstantiation.AnchorFactoryCall.class, resolved.funcForEmptyBuilder());
    assertEquals("builder", empty.methodName());
    assertEquals("lib.LibHelper", empty.anchor().getFullQualifiedName());
    assertInstanceOf(
        BuilderInstantiation.ConstructorCall.class, resolved.funcForPrefilledBuilder());
  }

  @Test
  void resolverUsageScope_IgnoresInTypeAnchorWithoutContract() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static builders.NoContract builder() {
                        return new builders.NoContract();
                      }
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package builders;
                    public class NoContract {
                      public NoContract() {}
                    }
                    """));

    assertThat(compilation).succeeded();
    // The anchored type does not satisfy the contract, so resolution falls through
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageWithoutAnnotation);
  }

  @Test
  void resolverUsageScope_PrefersBuilderNamedInTypeFactory() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static builders.OtherBuilder other() {
                        return new builders.OtherBuilder();
                      }
                      public static builders.LibHelperBuilder builder() {
                        return new builders.LibHelperBuilder();
                      }
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package builders;
                    public class OtherBuilder {
                      public OtherBuilder() {}
                      public OtherBuilder(lib.LibHelper value) {}
                      public lib.LibHelper build() { return new lib.LibHelper(); }
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package builders;
                    public class LibHelperBuilder {
                      public LibHelperBuilder() {}
                      public LibHelperBuilder(lib.LibHelper value) {}
                      public lib.LibHelper build() { return new lib.LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // Both candidates satisfy the contract; the conventional builder() name wins
    assertEquals(
        "builders.LibHelperBuilder",
        ResolverProbeProcessor.usageWithoutAnnotation.get().typeName().getFullQualifiedName());
  }

  @Test
  void resolverUsageScope_ResolvesNestedTypeBuilder() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static class LibHelperBuilder {
                        public LibHelperBuilder() {}
                        public LibHelperBuilder(LibHelper value) {}
                        public LibHelper build() { return new LibHelper(); }
                      }
                    }
                    """));

    assertThat(compilation).succeeded();
    // The nested type satisfies the full contract and wins over any anchored method
    ResolvedBuilder resolved = ResolverProbeProcessor.usageWithoutAnnotation.get();
    assertEquals("lib.LibHelper.LibHelperBuilder", resolved.typeName().getFullQualifiedName());
    assertInstanceOf(BuilderInstantiation.ConstructorCall.class, resolved.funcForEmptyBuilder());
    assertInstanceOf(
        BuilderInstantiation.ConstructorCall.class, resolved.funcForPrefilledBuilder());
  }

  @Test
  void resolverUsageScope_ResolvesLombokBuilderShape() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static LibHelperBuilder builder() { return new LibHelperBuilder(); }
                      public LibHelperBuilder toBuilder() { return new LibHelperBuilder(); }
                      public static class LibHelperBuilder {
                        LibHelperBuilder() {}
                        public LibHelper build() { return new LibHelper(); }
                      }
                    }
                    """));

    assertThat(compilation).succeeded();
    // Delombok output of @Builder(toBuilder = true): builder() anchors the empty path,
    // toBuilder() the seeded one; the nested builder's package-private ctor is never called
    ResolvedBuilder resolved = ResolverProbeProcessor.usageWithoutAnnotation.get();
    assertEquals("lib.LibHelper.LibHelperBuilder", resolved.typeName().getFullQualifiedName());
    assertInstanceOf(BuilderInstantiation.AnchorFactoryCall.class, resolved.funcForEmptyBuilder());
    BuilderInstantiation.ValueFactoryCall prefilled =
        assertInstanceOf(
            BuilderInstantiation.ValueFactoryCall.class, resolved.funcForPrefilledBuilder());
    assertEquals("toBuilder", prefilled.methodName());
  }

  @Test
  void resolverUsageScope_LombokBuilderShapeWithoutToBuilderFallsBack() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static LibHelperBuilder builder() { return new LibHelperBuilder(); }
                      public static class LibHelperBuilder {
                        LibHelperBuilder() {}
                        public LibHelper build() { return new LibHelper(); }
                      }
                    }
                    """));

    assertThat(compilation).succeeded();
    // Delombok output of @Builder without toBuilder: builder() anchors the empty path, but
    // no seeded path exists, so the anchored candidate is rejected
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageWithoutAnnotation);
  }

  @Test
  void resolverUsageScope_FreeBuilderShapeFallsBack() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static class Builder {
                        public Builder() {}
                        public Builder mergeFrom(LibHelper value) { return this; }
                        public LibHelper build() { return new LibHelper(); }
                      }
                    }
                    """));

    assertThat(compilation).succeeded();
    // org.inferred.freebuilder generated shape: nested Builder with a public ctor and
    // mergeFrom(T) — mergeFrom is an instance method, so the contract's seeded path fails
    // and resolution falls back to the classpath candidate (absent here)
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageWithoutAnnotation);
  }

  @Test
  void resolverUsageScope_AutoValueShapeFallsBack() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public static AutoValue_LibHelper.Builder create() {
                        return new AutoValue_LibHelper.Builder();
                      }
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class AutoValue_LibHelper {
                      public static class Builder {
                        Builder() {}
                        public LibHelper build() { return new LibHelper(); }
                      }
                    }
                    """));

    assertThat(compilation).succeeded();
    // com.google.auto.value generated shape: create() anchors a builder on the generated
    // sibling, but AutoValue offers no seeded path (no ctor(T), no toBuilder)
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageWithoutAnnotation);
  }

  @Test
  void resolverUsageScope_ImmutablesShapeFallsBack() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public LibHelper() {}
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class ImmutableLibHelper {
                      public static Builder builder() { return new Builder(); }
                      public Builder toBuilder() { return new Builder(); }
                      public static class Builder {
                        public Builder() {}
                        public ImmutableLibHelper build() { return new ImmutableLibHelper(); }
                      }
                    }
                    """));

    assertThat(compilation).succeeded();
    // org.immutables generated shape: builder() and toBuilder() live on the generated
    // sibling ImmutableLibHelper, unreachable from the referenced type itself
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageWithoutAnnotation);
  }

  @Test
  void resolverUsageScope_ResolvesRecordBuilderShape() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public LibHelper() {}
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelperBuilder {
                      public LibHelperBuilder() {}
                      public LibHelperBuilder(LibHelper value) {}
                      public LibHelper build() { return new LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // io.soabase.record-builder generated shape: a same-package <Type>Builder with public
    // ctors resolves through the constructed-name candidate path
    ResolvedBuilder resolved = ResolverProbeProcessor.usageWithoutAnnotation.get();
    assertEquals("lib.LibHelperBuilder", resolved.typeName().getFullQualifiedName());
    assertInstanceOf(BuilderInstantiation.ConstructorCall.class, resolved.funcForEmptyBuilder());
    assertInstanceOf(
        BuilderInstantiation.ConstructorCall.class, resolved.funcForPrefilledBuilder());
  }

  @Test
  void resolverUsageScope_UsingExistingBuildersDisabled() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new ResolverProbeProcessor())
            .compile(
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelper {
                      public LibHelper() {}
                    }
                    """),
                ProcessorTestUtils.forSource(
                    """
                    package lib;
                    public class LibHelperBuilder {
                      public LibHelperBuilder() {}
                      public LibHelperBuilder(LibHelper value) {}
                      public LibHelper build() { return new LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // usingExistingBuilders=DISABLED: the contract-satisfying classpath builder is not reused
    assertEquals(Optional.empty(), ResolverProbeProcessor.usageWithExistingDisabled);
    // ... but a builder generated in the current round is own generation and still resolves
    ResolvedBuilder generated = ResolverProbeProcessor.usageGeneratedWithExistingDisabled.get();
    assertEquals("lib.LibHelperBuilder", generated.typeName().getFullQualifiedName());
    assertInstanceOf(BuilderInstantiation.StaticFactoryCall.class, generated.funcForEmptyBuilder());
    assertInstanceOf(
        BuilderInstantiation.ConstructorCall.class, generated.funcForPrefilledBuilder());
  }

  private static final class ResolverProbeProcessor extends AbstractProcessor {
    private static Optional<ResolvedBuilder> first;
    private static Optional<ResolvedBuilder> second;
    private static Optional<ResolvedBuilder> afterConfigurationChange;
    private static Optional<ResolvedBuilder> beforeRegistration;
    private static Optional<ResolvedBuilder> afterRegistration;
    private static Optional<ResolvedBuilder> usageBeforeRegistration;
    private static Optional<ResolvedBuilder> usageAfterRegistration;
    private static Optional<ResolvedBuilder> usageWithoutAnnotation;
    private static Optional<ResolvedBuilder> usageWithSuffix;
    private static Optional<ResolvedBuilder> usageDefaultSuffix;
    private static Optional<ResolvedBuilder> usagePackagePrivate;
    private static Optional<ResolvedBuilder> usageWithExistingDisabled;
    private static Optional<ResolvedBuilder> usageGeneratedWithExistingDisabled;

    private boolean captured;

    static void reset() {
      first = null;
      second = null;
      afterConfigurationChange = null;
      beforeRegistration = null;
      afterRegistration = null;
      usageBeforeRegistration = null;
      usageAfterRegistration = null;
      usageWithoutAnnotation = null;
      usageWithSuffix = null;
      usageDefaultSuffix = null;
      usagePackagePrivate = null;
      usageWithExistingDisabled = null;
      usageGeneratedWithExistingDisabled = null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return Set.of("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      if (captured || roundEnv.processingOver()) {
        return false;
      }
      TypeElement helper = processingEnv.getElementUtils().getTypeElement("lib.LibHelper");
      ProcessingContext context =
          new ProcessingContext(
              new ProcessingLogger(processingEnv), BuilderConfiguration.DEFAULT, processingEnv);
      context.initProcessingTarget(new ProcessingTarget(configuration("lib", "Builder"), ""));
      BuilderScopeResolver resolver = context.getBuilderScopeResolver();
      // Register the type as generated, mirroring the real processor which calls
      // registerGeneratedBuilder before any resolution happens.
      resolver.registerGeneratedBuilder(
          new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder"));
      first = resolver.resolveUsableBuilderType(helper);
      second = resolver.resolveUsableBuilderType(helper);
      // Clear registration before testing scope-only behavior
      resolver.resetGeneratedBuilders();
      context.initProcessingTarget(
          new ProcessingTarget(configuration("other", "OtherBuilder"), ""));
      afterConfigurationChange = resolver.resolveUsableBuilderType(helper);
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("other"), ""));
      beforeRegistration = resolver.resolveUsableBuilderType(helper);
      // Registration alone is not enough — the type must be in scope
      resolver.registerGeneratedBuilder(
          new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder"));
      afterRegistration = resolver.resolveUsableBuilderType(helper);
      // Clear registration for usage-scope classpath lookup tests
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), ""));
      resolver.resetGeneratedBuilders();
      usageBeforeRegistration = resolver.resolveUsableBuilderType(helper);
      resolver.registerGeneratedBuilder(
          new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder"));
      usageAfterRegistration = resolver.resolveUsableBuilderType(helper);
      // Usage scope without @SimpleBuilder annotation — type existence check only
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), ""));
      resolver.resetGeneratedBuilders();
      usageWithoutAnnotation = resolver.resolveUsableBuilderType(helper);
      // Usage scope with builderUsageSuffix="Factory"
      context.initProcessingTarget(
          new ProcessingTarget(usageWithSuffixConfiguration("lib", "Factory"), ""));
      usageWithSuffix = resolver.resolveUsableBuilderType(helper);
      // Usage scope with default suffix (no builderUsageSuffix configured)
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), ""));
      usageDefaultSuffix = resolver.resolveUsableBuilderType(helper);
      // Generated builder in the same package as the referenced builder: package-private
      // contract members are accessible
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), "lib"));
      usagePackagePrivate = resolver.resolveUsableBuilderType(helper);
      // usingExistingBuilders=DISABLED: existing builders are not reused, but a builder
      // generated in the current round is own generation and still resolves
      context.initProcessingTarget(
          new ProcessingTarget(existingBuildersDisabledConfiguration("lib"), ""));
      usageWithExistingDisabled = resolver.resolveUsableBuilderType(helper);
      resolver.registerGeneratedBuilder(
          new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder"));
      usageGeneratedWithExistingDisabled = resolver.resolveUsableBuilderType(helper);
      captured = true;
      return false;
    }

    private static BuilderConfiguration configuration(String packageName, String suffix) {
      return BuilderConfiguration.DEFAULT.merge(
          BuilderConfiguration.builder()
              .builderGenerationPackages(packageName)
              .builderSuffix(suffix)
              .build());
    }

    private static BuilderConfiguration usageOnlyConfiguration(String packageName) {
      return BuilderConfiguration.DEFAULT.merge(
          BuilderConfiguration.builder().builderUsagePackages(packageName).build());
    }

    private static BuilderConfiguration existingBuildersDisabledConfiguration(String packageName) {
      return BuilderConfiguration.DEFAULT.merge(
          BuilderConfiguration.builder()
              .builderUsagePackages(packageName)
              .usingExistingBuilders(OptionState.DISABLED)
              .build());
    }

    private static BuilderConfiguration usageWithSuffixConfiguration(
        String packageName, String usageSuffix) {
      return BuilderConfiguration.DEFAULT.merge(
          BuilderConfiguration.builder()
              .builderUsagePackages(packageName)
              .builderUsageSuffix(usageSuffix)
              .build());
    }
  }
}
