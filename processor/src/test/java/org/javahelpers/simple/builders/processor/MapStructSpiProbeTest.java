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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.mapstruct.MapStructAccessorNamingStrategy;
import org.javahelpers.simple.builders.processor.mapstruct.MapStructBuilderProvider;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.ap.spi.BuilderInfo;
import org.mapstruct.ap.spi.MapStructProcessingEnvironment;
import org.mapstruct.ap.spi.MethodType;
import org.mapstruct.ap.spi.TypeHierarchyErroneousException;

/**
 * Exercises both MapStruct SPIs from inside a real {@code javac} run: a probe processor drives them
 * against the elements the same compilation emits — covering paths MapStruct's own call sites never
 * reach in the integration tests (method classification on the builder type, deferral while the
 * published builder is not emitted yet, marker scans of foreign, ignored and template beans, cache
 * hits, and lookups after the registry finalized).
 */
class MapStructSpiProbeTest {

  private static JavaFileObject personDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public class PersonDto {
          private String name;

          public String getName() {
            return name;
          }

          public void setName(String name) {
            this.name = name;
          }
        }
        """);
  }

  private static JavaFileObject foreignDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        public class ForeignDto {
          private String name;
        }
        """);
  }

  private static JavaFileObject ignoredDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        @Ignore4BuilderGeneration
        public class IgnoredDto {
          private String name;
        }
        """);
  }

  private static JavaFileObject scopedDto() {
    return ProcessorTestUtils.forSource(
        """
        package scoped;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public class ScopedDto {
          private String name;
        }
        """);
  }

  private static JavaFileObject templateDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleMinimalBuilder;

        @SimpleMinimalBuilder
        public class MinimalDto {
          private String name;
        }
        """);
  }

  @Test
  void probe_shouldDriveSpiAgainstEmittedElements() {
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new SpiProbeProcessor(), new BuilderProcessor())
            .compile(personDto(), foreignDto(), ignoredDto(), templateDto());
    assertThat(compilation).succeeded();

    ProbeResults results = ProbeResults.instance;

    // Marked beans defer while unpublished — marked or template-annotated alike.
    assertInstanceOf(TypeHierarchyErroneousException.class, results.unregisteredDeferral);
    assertInstanceOf(TypeHierarchyErroneousException.class, results.templateDeferral);

    // Foreign and opted-out beans are never claimed and never wait, whichever state applies.
    assertNull(results.foreignUnpublished);
    assertNull(results.ignoredUnpublished);
    assertNull(results.foreignRegistered);
    assertNull(results.ignoredRegistered);
    assertNull(results.foreignWhenFinished);
    assertNull(results.noType);

    // Resolution once the builder exists resolves the published contract methods and caches.
    assertNotNull(results.builderInfo);
    assertEquals(
        "create", results.builderInfo.getBuilderCreationMethod().getSimpleName().toString());
    assertEquals(
        "build",
        results.builderInfo.getBuildMethods().iterator().next().getSimpleName().toString());
    assertEquals(results.builderInfo, results.cachedBuilderInfo);

    // The naming strategy keeps only direct property setters visible as write accessors.
    assertEquals(MethodType.SETTER, results.methodTypes.get("name"));
    assertEquals(MethodType.OTHER, results.methodTypes.get("nameUpdate"));
    assertEquals(MethodType.OTHER, results.methodTypes.get("build"));
    assertEquals(MethodType.OTHER, results.methodTypes.get("create"));

    // A marked bean outside the generation scope is skipped by planning: it defers in every
    // non-final state while unpublished and resolves to no builder — reported as a warning —
    // only once finished. The in-scope bean forces a second round so the post-registration
    // lookup runs.
    Compilation skippedCompile =
        Compiler.javac()
            .withProcessors(new BuilderProcessor(), new SpiProbeProcessor(true))
            .withOptions("-Asimplebuilder.builderGenerationPackages=scoped")
            .compile(personDto(), scopedDto());
    assertThat(skippedCompile).succeeded();
    ProbeResults skipped = ProbeResults.instance;
    assertInstanceOf(TypeHierarchyErroneousException.class, skipped.skippedBeanDeferred);
    assertInstanceOf(TypeHierarchyErroneousException.class, skipped.skippedBeanRegisteredDeferral);
    assertNull(skipped.skippedBeanFinished);
  }

  /** Records the SPI outcomes of one probe compilation for assertions after it. */
  private static final class ProbeResults {
    static final ProbeResults instance = new ProbeResults();

    final Map<String, MethodType> methodTypes = new LinkedHashMap<>();
    Throwable unregisteredDeferral;
    Throwable templateDeferral;
    Throwable skippedBeanDeferred;
    Throwable skippedBeanRegisteredDeferral;
    BuilderInfo foreignUnpublished;
    BuilderInfo ignoredUnpublished;
    BuilderInfo skippedBeanFinished;
    BuilderInfo foreignRegistered;
    BuilderInfo ignoredRegistered;
    BuilderInfo foreignWhenFinished;
    BuilderInfo noType;
    BuilderInfo builderInfo;
    BuilderInfo cachedBuilderInfo;

    void reset() {
      methodTypes.clear();
      unregisteredDeferral = null;
      templateDeferral = null;
      skippedBeanDeferred = null;
      skippedBeanRegisteredDeferral = null;
      foreignUnpublished = null;
      ignoredUnpublished = null;
      skippedBeanFinished = null;
      foreignRegistered = null;
      ignoredRegistered = null;
      foreignWhenFinished = null;
      noType = null;
      builderInfo = null;
      cachedBuilderInfo = null;
    }
  }

  /**
   * Drives {@link MapStructBuilderProvider} and {@link MapStructAccessorNamingStrategy} like
   * MapStruct would — one SPI instance per compilation, lookups on the mapped bean while its
   * builder is pending, then method classification once the builder type exists. Runs ahead of
   * {@link BuilderProcessor} in the first round so a marked bean is probed before registration. In
   * {@code skippedMode} it runs behind {@link BuilderProcessor} and probes the marked bean that the
   * scoped processor never plans — deferral through the registered states plus the finished
   * outcome.
   */
  @SupportedAnnotationTypes("*")
  public static final class SpiProbeProcessor extends AbstractProcessor {

    private final MapStructBuilderProvider provider = new MapStructBuilderProvider();
    private final MapStructAccessorNamingStrategy naming = new MapStructAccessorNamingStrategy();
    private final boolean skippedMode;
    private boolean namingProbed;

    SpiProbeProcessor() {
      this(false);
    }

    SpiProbeProcessor(boolean skippedMode) {
      this.skippedMode = skippedMode;
    }

    @Override
    public synchronized void init(javax.annotation.processing.ProcessingEnvironment env) {
      super.init(env);
      ProbeResults.instance.reset();
      provider.init(spiEnvironment());
      naming.init(spiEnvironment());
    }

    private MapStructProcessingEnvironment spiEnvironment() {
      return new MapStructProcessingEnvironment() {
        @Override
        public Elements getElementUtils() {
          return processingEnv.getElementUtils();
        }

        @Override
        public Types getTypeUtils() {
          return processingEnv.getTypeUtils();
        }

        @Override
        public Map<String, String> getOptions() {
          return Map.of();
        }
      };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      ProbeResults results = ProbeResults.instance;
      if (roundEnv.processingOver()) {
        if (skippedMode) {
          results.skippedBeanFinished = lookup(provider, "test.PersonDto");
        } else {
          results.foreignWhenFinished = lookup(provider, "test.ForeignDto");
        }
        return false;
      }

      TypeElement bean = processingEnv.getElementUtils().getTypeElement("test.PersonDto");
      if (bean == null) {
        return false;
      }

      if (skippedMode) {
        // The bean is marked but out of the generation scope: it defers in every non-final
        // state while unpublished — the probe runs behind the processor, so these lookups
        // already observe the post-registration state.
        if (results.skippedBeanDeferred == null) {
          results.skippedBeanDeferred = lookupExpectingDeferral(bean);
        } else {
          results.skippedBeanRegisteredDeferral = lookupExpectingDeferral(bean);
        }
        return false;
      }

      if (results.unregisteredDeferral == null) {
        // Probed before BuilderProcessor planned the bean — a marked bean without a registry
        // entry must defer like a mapper running ahead of the generating round.
        results.unregisteredDeferral = lookupExpectingDeferral(bean);
        results.foreignUnpublished = lookup(provider, "test.ForeignDto");
        results.ignoredUnpublished = lookup(provider, "test.IgnoredDto");
        results.templateDeferral = lookupExpectingDeferral(element("test.MinimalDto"));
        return false;
      }

      TypeElement builder = processingEnv.getElementUtils().getTypeElement("test.PersonDtoBuilder");
      if (builder == null) {
        // Published but not emitted yet — the lookup must defer until the type exists.
        lookupExpectingDeferral(bean);
        return false;
      }

      if (results.builderInfo != null) {
        return false;
      }
      results.builderInfo = provider.findBuilderInfo(bean.asType());
      results.cachedBuilderInfo = provider.findBuilderInfo(bean.asType());
      results.foreignRegistered = lookup(provider, "test.ForeignDto");
      results.ignoredRegistered = lookup(provider, "test.IgnoredDto");
      results.noType =
          provider.findBuilderInfo(
              processingEnv.getTypeUtils().getNoType(javax.lang.model.type.TypeKind.NONE));

      if (!namingProbed) {
        namingProbed = true;
        for (ExecutableElement method : ElementFilter.methodsIn(builder.getEnclosedElements())) {
          results.methodTypes.put(method.getSimpleName().toString(), naming.getMethodType(method));
        }
      }
      return false;
    }

    private TypeElement element(String qualifiedName) {
      return processingEnv.getElementUtils().getTypeElement(qualifiedName);
    }

    private Throwable lookupExpectingDeferral(TypeElement bean) {
      try {
        provider.findBuilderInfo(bean.asType());
        return null;
      } catch (RuntimeException deferred) {
        return deferred;
      }
    }

    private BuilderInfo lookup(MapStructBuilderProvider provider, String qualifiedName) {
      Element element = processingEnv.getElementUtils().getTypeElement(qualifiedName);
      TypeMirror type = element == null ? null : element.asType();
      return type == null ? null : provider.findBuilderInfo(type);
    }
  }
}
