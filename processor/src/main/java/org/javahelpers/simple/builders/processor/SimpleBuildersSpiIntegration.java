/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.util.Elements;
import org.javahelpers.codegen.processing.ProcessorSpiIntegration;
import org.javahelpers.codegen.processing.ProcessorSpiIntegration.State;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeInstantiation.StaticFactoryCall;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * The builders {@link BuilderProcessor} generates, published to SPI adapters of other frameworks
 * sharing the annotation processor path (and therefore the classloader). Each entry carries the
 * resolved builder's type, creation and build method names plus the bean's configured {@code
 * setterPrefix}, so adapters do not scan candidates or read annotation configuration themselves.
 *
 * <p>The lifecycle — where {@link BuilderProcessor}'s builder generation stands — is delegated to a
 * {@link ProcessorSpiIntegration} instance and transitioned by the processor alone: SPI adapters
 * only ever read this holder, they never initialize or mutate it.
 */
public final class SimpleBuildersSpiIntegration {

  /** The processor-lifecycle holder this domain's SPI integration delegates to. */
  private static final ProcessorSpiIntegration LIFECYCLE = new ProcessorSpiIntegration();

  /**
   * One bean-to-builder pair resolved at registration time: the type this processor emits for
   * {@code beanType} plus its creation/build method names and the bean's {@code setterPrefix}
   * naming option — flattened to names so SPI adapters never touch the resolution model.
   */
  public record PublishedBuilder(
      TypeName beanType,
      TypeName builderType,
      String creationMethodName,
      String buildMethodName,
      String setterPrefix) {}

  /** Builders published for the current compilation, keyed by bean qualified name. */
  private static final Map<String, PublishedBuilder> BY_BEAN = new HashMap<>();

  /** The same entries keyed by builder qualified name. */
  private static final Map<String, PublishedBuilder> BY_BUILDER = new HashMap<>();

  private SimpleBuildersSpiIntegration() {}

  /** Starts a new compilation: clears the registry and marks generation as running. */
  static void initCompilation(Elements elements) {
    BY_BEAN.clear();
    BY_BUILDER.clear();
    LIFECYCLE.initCompilation(elements);
  }

  /**
   * Marks the registry as final: {@link BuilderProcessor}'s generating round ran (or the
   * compilation ended without one) — no more builders will be published.
   */
  static void finishCompilation() {
    LIFECYCLE.finishCompilation();
  }

  /**
   * Whether {@code observed} belongs to the compilation this holder currently describes — see
   * {@link ProcessorSpiIntegration#isCurrentCompilation}.
   */
  static boolean isCurrentCompilation(Elements observed) {
    return LIFECYCLE.isCurrentCompilation(observed);
  }

  /** The lifecycle state of the current compilation. */
  static State state() {
    return LIFECYCLE.state();
  }

  /**
   * Whether {@link BuilderProcessor} finished generating builders for the compilation {@code
   * observed} belongs to — {@code false} while this holder still describes another run. See {@link
   * ProcessorSpiIntegration#isFinishedForIntegration} for the staleness semantics.
   */
  public static boolean isSimpleBuildersFinishedForIntegration(Elements observed) {
    return LIFECYCLE.isFinishedForIntegration(observed);
  }

  /**
   * Publishes the builder {@link BuilderProcessor} resolved for {@code beanType} in this
   * compilation — generated builders always create via a static factory.
   */
  static void registerBuilder(TypeName beanType, ResolvedBuilder builder, String setterPrefix) {
    if (!(builder.funcForEmptyBuilder() instanceof StaticFactoryCall factory)) {
      return;
    }
    PublishedBuilder publishedBuilder =
        new PublishedBuilder(
            beanType,
            builder.typeName(),
            factory.methodName(),
            builder.buildMethodName(),
            setterPrefix);
    BY_BEAN.put(publishedBuilder.beanType().getFullQualifiedName(), publishedBuilder);
    BY_BUILDER.put(publishedBuilder.builderType().getFullQualifiedName(), publishedBuilder);
  }

  /**
   * The builder published for {@code beanType}'s qualified name in the compilation {@code observed}
   * belongs to — empty while this holder still describes another run, so stale entries can never
   * claim a bean.
   */
  public static Optional<PublishedBuilder> builderFor(String beanQualifiedName, Elements observed) {
    return isCurrentCompilation(observed)
        ? Optional.ofNullable(BY_BEAN.get(beanQualifiedName))
        : Optional.empty();
  }

  /**
   * The published builder with the given qualified type name in the compilation {@code observed}
   * belongs to — empty while this holder still describes another run.
   */
  public static Optional<PublishedBuilder> builderByName(
      String builderQualifiedName, Elements observed) {
    return isCurrentCompilation(observed)
        ? Optional.ofNullable(BY_BUILDER.get(builderQualifiedName))
        : Optional.empty();
  }
}
