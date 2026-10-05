/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons with the following conditions:
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
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.util.Elements;
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation.StaticFactoryCall;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * The builders {@link BuilderProcessor} generates, published to SPI adapters of other frameworks
 * sharing the annotation processor path (and therefore the classloader). Each entry carries the
 * resolved builder's type, creation and build method names plus the bean's configured {@code
 * setterSuffix}, so adapters do not scan candidates or read annotation configuration themselves.
 *
 * <p>The lifecycle reports where {@link BuilderProcessor}'s builder generation stands and is
 * transitioned by that processor alone — SPI adapters only ever read this holder, they never
 * initialize or mutate it. All state is compilation-scoped: javac initializes each processor lazily
 * when its turn in a round comes, so before {@link BuilderProcessor} has run its {@code init()}
 * nothing static can be trusted — values may be leftovers of a previous compilation in a long-lived
 * JVM (Gradle daemon, incremental builds). Adapters therefore pass their own {@link Elements}
 * instance to every read: javac hands every processor of one compilation the same {@code Elements}
 * instance, so a mismatch means this holder still describes an older run.
 */
public final class SimpleBuildersSpiIntegration {

  /**
   * Where {@link BuilderProcessor}'s builder generation stands in the current compilation, as the
   * SPI adapters observe it.
   */
  public enum State {
    /**
     * No processor init happened in this compilation yet — static content may be a previous run's,
     * and the registry must be treated as possibly still filling.
     */
    INIT,
    /**
     * {@code BuilderProcessor} initialized: its single generating round may not have run yet — the
     * registry must be treated as possibly still filling.
     */
    PROCESSING,
    /**
     * The generating round ran: the registry is final for this compilation. {@code
     * BuilderProcessor} generates in exactly one round — the first round carrying its annotations —
     * so elements first appearing in later rounds get no builder.
     */
    FINISHED
  }

  /**
   * One bean-to-builder pair resolved at registration time: the type this processor emits for
   * {@code beanType} plus its creation/build method names and the bean's {@code setterSuffix}
   * naming option — flattened to names so SPI adapters never touch the resolution model.
   */
  public record PublishedBuilder(
      TypeName beanType,
      TypeName builderType,
      String creationMethodName,
      String buildMethodName,
      String setterSuffix) {}

  private static volatile State state = State.INIT;

  /** The {@link Elements} of the compilation this holder's content describes. */
  private static final AtomicReference<Elements> compilationElements = new AtomicReference<>();

  /** Builders published for the current compilation, keyed by bean qualified name. */
  private static final Map<String, PublishedBuilder> BY_BEAN = new HashMap<>();

  /** The same entries keyed by builder qualified name. */
  private static final Map<String, PublishedBuilder> BY_BUILDER = new HashMap<>();

  private SimpleBuildersSpiIntegration() {}

  /** Starts a new compilation: clears the registry and marks generation as running. */
  static void initCompilation(Elements elements) {
    BY_BEAN.clear();
    BY_BUILDER.clear();
    compilationElements.set(elements);
    state = State.PROCESSING;
  }

  /**
   * Marks the registry as final: {@link BuilderProcessor}'s generating round ran (or the
   * compilation ended without one) — no more builders will be published.
   */
  static void finishCompilation() {
    state = State.FINISHED;
  }

  /**
   * Whether {@code observed} belongs to the compilation this holder currently describes — {@code
   * false} while it still carries a previous run's leftovers. javac creates one {@link Elements}
   * per compilation and shares it between all processors, so reference identity is a reliable
   * staleness check that never requires the SPI adapters to write anything.
   */
  static boolean isCurrentCompilation(Elements observed) {
    return observed != null && observed == compilationElements.get();
  }

  /** The lifecycle state of the current compilation. */
  static State state() {
    return state;
  }

  /**
   * Whether {@link BuilderProcessor} finished generating builders for the compilation {@code
   * observed} belongs to — {@code false} while this holder still describes another run.
   *
   * <p>The guard is not optional: this holder's statics live in the processor-path classloader,
   * which build tools reuse across compilations (Gradle daemon, in-process javac, IDE builds,
   * repeated compiles in one JVM). In a new compilation's first round — before {@link
   * BuilderProcessor}'s {@code init()} had its turn — {@code state} can still be the previous run's
   * {@link State#FINISHED} and the registry still holds its entries. Trusting them would claim
   * beans by qualified name they were never planned with here and answer misses as final instead of
   * deferring. javac hands every processor of one compilation the same {@link Elements} instance
   * and a different one per compilation, so identity is the read-only, order-independent boundary
   * marker — an SPI-side reset cannot work, because SPI init order against {@link
   * BuilderProcessor}'s init is path-order dependent.
   */
  public static boolean isSimpleBuildersFinishedForIntegration(Elements observed) {
    return isCurrentCompilation(observed) && state == State.FINISHED;
  }

  /**
   * Publishes the builder {@link BuilderProcessor} resolved for {@code beanType} in this
   * compilation — generated builders always create via a static factory.
   */
  static void registerBuilder(TypeName beanType, ResolvedBuilder builder, String setterSuffix) {
    if (!(builder.funcForEmptyBuilder() instanceof StaticFactoryCall factory)) {
      return;
    }
    PublishedBuilder publishedBuilder =
        new PublishedBuilder(
            beanType,
            builder.typeName(),
            factory.methodName(),
            builder.buildMethodName(),
            setterSuffix);
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
