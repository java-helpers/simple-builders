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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.analysis.BuilderScopeResolver;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;
import org.javahelpers.simple.builders.processor.processing.ProcessingTarget;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Probe-based coverage of {@link BuilderScopeResolver} internals that generated-source assertions
 * cannot observe: result caching, cache invalidation on configuration change, and per-round
 * registration of generated types. End-to-end scope behavior visible in generated builders is
 * covered by {@link BuilderScopeProcessingTest}.
 */
class BuilderScopeResolverTest {

  @Test
  void resolverReturnsEmptyAfterConfigurationChangesToExcludePackage() {
    ResolverProbeProcessor.reset();
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
    assertEquals("lib.LibHelperBuilder", ResolverProbeProcessor.first.get().getFullQualifiedName());
    // After clearing registration and changing config to exclude "lib", the resolver returns
    // empty (not registered, not in scope)
    assertEquals(Optional.empty(), ResolverProbeProcessor.afterConfigurationChange);
    // Cache was cleared by the config change, so a new Optional instance is returned
    assertNotSame(ResolverProbeProcessor.first, ResolverProbeProcessor.afterConfigurationChange);
  }

  @Test
  void resolverCachesResolvedOptionalInstancePerReferencedType() {
    ResolverProbeProcessor.reset();
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
    ResolverProbeProcessor.reset();
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
        ResolverProbeProcessor.usageAfterRegistration.get().getFullQualifiedName());
  }

  @Test
  void resolverUsageScope_ResolvesBuilderWithoutSimpleBuilderAnnotation() {
    ResolverProbeProcessor.reset();
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
                    public class LibHelperBuilder {
                      public LibHelperBuilder() {}
                      public LibHelperBuilder(LibHelper value) {}
                      public LibHelper build() { return new LibHelper(); }
                    }
                    """));

    assertThat(compilation).succeeded();
    // Usage scope without @SimpleBuilder annotation — builder resolved by contract check
    assertEquals(
        "lib.LibHelperBuilder",
        ResolverProbeProcessor.usageWithoutAnnotation.get().getFullQualifiedName());
  }

  @Test
  void resolverUsageScope_UsesBuilderUsageSuffixWhenConfigured() {
    ResolverProbeProcessor.reset();
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
        ResolverProbeProcessor.usageWithSuffix.get().getFullQualifiedName());
  }

  @Test
  void resolverUsageScope_FallsBackToBuilderSuffixWhenUsageSuffixNotSet() {
    ResolverProbeProcessor.reset();
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
        ResolverProbeProcessor.usageDefaultSuffix.get().getFullQualifiedName());
  }

  private static final class ResolverProbeProcessor extends AbstractProcessor {
    private static Optional<TypeName> first;
    private static Optional<TypeName> second;
    private static Optional<TypeName> afterConfigurationChange;
    private static Optional<TypeName> beforeRegistration;
    private static Optional<TypeName> afterRegistration;
    private static Optional<TypeName> usageBeforeRegistration;
    private static Optional<TypeName> usageAfterRegistration;
    private static Optional<TypeName> usageWithoutAnnotation;
    private static Optional<TypeName> usageWithSuffix;
    private static Optional<TypeName> usageDefaultSuffix;

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
      // registerGeneratedBuilders before any resolution happens.
      resolver.registerGeneratedBuilders(
          Map.of(new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder")));
      first = resolver.resolveUsableBuilderType(helper);
      second = resolver.resolveUsableBuilderType(helper);
      // Clear registration before testing scope-only behavior
      resolver.registerGeneratedBuilders(Map.of());
      context.initProcessingTarget(
          new ProcessingTarget(configuration("other", "OtherBuilder"), ""));
      afterConfigurationChange = resolver.resolveUsableBuilderType(helper);
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("other"), ""));
      beforeRegistration = resolver.resolveUsableBuilderType(helper);
      // Registration alone is not enough — the type must be in scope
      resolver.registerGeneratedBuilders(
          Map.of(new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder")));
      afterRegistration = resolver.resolveUsableBuilderType(helper);
      // Clear registration for usage-scope classpath lookup tests
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), ""));
      resolver.registerGeneratedBuilders(Map.of());
      usageBeforeRegistration = resolver.resolveUsableBuilderType(helper);
      resolver.registerGeneratedBuilders(
          Map.of(new TypeName("lib", "LibHelper"), new TypeName("lib", "LibHelperBuilder")));
      usageAfterRegistration = resolver.resolveUsableBuilderType(helper);
      // Usage scope without @SimpleBuilder annotation — type existence check only
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), ""));
      resolver.registerGeneratedBuilders(Map.of());
      usageWithoutAnnotation = resolver.resolveUsableBuilderType(helper);
      // Usage scope with builderUsageSuffix="Factory"
      context.initProcessingTarget(
          new ProcessingTarget(usageWithSuffixConfiguration("lib", "Factory"), ""));
      usageWithSuffix = resolver.resolveUsableBuilderType(helper);
      // Usage scope with default suffix (no builderUsageSuffix configured)
      context.initProcessingTarget(new ProcessingTarget(usageOnlyConfiguration("lib"), ""));
      usageDefaultSuffix = resolver.resolveUsableBuilderType(helper);
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
