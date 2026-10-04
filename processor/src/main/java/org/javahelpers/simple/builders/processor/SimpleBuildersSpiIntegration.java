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
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * The builders {@link BuilderProcessor} generates, published to SPI adapters of other frameworks
 * sharing the annotation processor path (and therefore the classloader). Each entry carries the
 * fully resolved builder — type name, creation and build method — plus the bean's configured {@code
 * setterSuffix}, so adapters do not scan elements or read annotations themselves.
 *
 * <p>The lifecycle reports where {@link BuilderProcessor}'s builder generation stands and is
 * transitioned by that processor alone — SPI adapters only ever read this holder, they never
 * initialize or mutate it. All state is compilation-scoped: javac initializes each processor lazily
 * when its turn in a round comes, so before {@link BuilderProcessor} has run its {@code init()}
 * nothing static can be trusted — values may be leftovers of a previous compilation in a long-lived
 * JVM (Gradle daemon, incremental builds). Adapters therefore pass their own {@link Elements} to
 * {@link #isCurrentCompilation}: javac hands every processor of one compilation the same {@code
 * Elements} instance, so a mismatch means this holder still describes an older run.
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
   * One bean-to-builder pair resolved at registration time: the {@link ResolvedBuilder} this
   * processor emits for {@code beanType} plus the bean's {@code setterSuffix} naming option.
   */
  public record PublishedBuilder(TypeName beanType, ResolvedBuilder builder, String setterSuffix) {}

  private static final String OPTION_USING_MAPSTRUCT = "simplebuilder.usingMapStructIntegration";

  private static volatile State state = State.INIT;

  /** The {@link Elements} of the compilation this holder's content describes. */
  private static final AtomicReference<Elements> compilationElements = new AtomicReference<>();

  /** The processor-resolved integration switch; {@code null} leaves the fallbacks active. */
  private static volatile Boolean integrationEnabled;

  /** Builders published for the current compilation, keyed by bean qualified name. */
  private static final Map<String, PublishedBuilder> BY_BEAN = new HashMap<>();

  /** The same entries keyed by builder qualified name. */
  private static final Map<String, PublishedBuilder> BY_BUILDER = new HashMap<>();

  private SimpleBuildersSpiIntegration() {}

  /**
   * Starts a new compilation: clears the registry and publishes the integration switch the
   * processor resolved ({@code null} when the option is unset).
   */
  static void initCompilation(Elements elements, Boolean integrationEnabled) {
    BY_BEAN.clear();
    BY_BUILDER.clear();
    compilationElements.set(elements);
    SimpleBuildersSpiIntegration.integrationEnabled = integrationEnabled;
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
  public static boolean isCurrentCompilation(Elements observed) {
    return observed != null && observed == compilationElements.get();
  }

  /** The lifecycle state the SPI adapters observe for the current compilation. */
  public static State state() {
    return state;
  }

  /** Whether {@link BuilderProcessor} finished generating builders for this compilation. */
  public static boolean isSimpleBuildersFinishedForIntegration() {
    return state == State.FINISHED;
  }

  /**
   * Publishes the builder {@link BuilderProcessor} resolved for {@code beanType} in this
   * compilation.
   */
  static void registerBuilder(TypeName beanType, ResolvedBuilder builder, String setterSuffix) {
    PublishedBuilder publishedBuilder = new PublishedBuilder(beanType, builder, setterSuffix);
    BY_BEAN.put(publishedBuilder.beanType().getFullQualifiedName(), publishedBuilder);
    BY_BUILDER.put(publishedBuilder.builder().typeName().getFullQualifiedName(), publishedBuilder);
  }

  /** The builder published for {@code beanType}'s qualified name, if this compilation plans one. */
  public static Optional<PublishedBuilder> builderFor(String beanQualifiedName) {
    return Optional.ofNullable(BY_BEAN.get(beanQualifiedName));
  }

  /**
   * The published builder with the given qualified type name, if {@link BuilderProcessor} plans it
   * in this compilation.
   */
  public static Optional<PublishedBuilder> builderByName(String builderQualifiedName) {
    return Optional.ofNullable(BY_BUILDER.get(builderQualifiedName));
  }

  /**
   * Whether the MapStruct integration is switched on: while the processor has published this
   * compilation's resolution (and {@code observed} proves it current) that value wins; without one
   * — incl. the stale leftovers of a previous run — the {@code simplebuilder.*} convention applies
   * (JVM system property before the annotation processor option the hosting framework does not
   * forward anyway). Anything but {@code false}/{@code disabled} keeps it on.
   */
  public static boolean isMapstructGenerationEnabled(
      Elements observed, Map<String, String> processorOptions) {
    if (isCurrentCompilation(observed) && state != State.INIT) {
      Boolean published = integrationEnabled;
      if (published != null) {
        return published;
      }
    }
    String value =
        System.getProperty(OPTION_USING_MAPSTRUCT, processorOptions.get(OPTION_USING_MAPSTRUCT));
    return !("false".equalsIgnoreCase(value) || "disabled".equalsIgnoreCase(value));
  }
}
