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
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation.StaticFactoryCall;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * The builders {@link BuilderProcessor} generates, published to SPI adapters of other frameworks
 * sharing the annotation processor path (and therefore the classloader). Each entry carries the
 * fully resolved builder — type name, creation and build method — plus the bean's configured {@code
 * setterSuffix}, so adapters do not scan elements or read annotations themselves.
 *
 * <p>The lifecycle reports where {@link BuilderProcessor}'s builder generation stands and is
 * transitioned by that processor alone — other processors or SPI adapters never write it. All state
 * is compilation-scoped: javac initializes each processor lazily when its turn in a round comes, so
 * before {@link BuilderProcessor} has run its {@code init()} nothing static can be trusted — values
 * may be leftovers of a previous compilation in a long-lived JVM (Gradle daemon, incremental
 * builds). Only while {@link State#PROCESSING} or {@link State#FINISHED} are the published switch
 * and registry the current compilation's truth.
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
    /** {@code BuilderProcessor} initialized: the registry may still grow this round. */
    PROCESSING,
    /** The last round ran: the registry is final for this compilation. */
    FINISHED
  }

  /**
   * One bean-to-builder pair resolved at registration time: the {@link ResolvedBuilder} this
   * processor emits for {@code beanType} plus the bean's {@code setterSuffix} naming option.
   */
  public record PublishedBuilder(TypeName beanType, ResolvedBuilder builder, String setterSuffix) {}

  private static final String OPTION_USING_MAPSTRUCT = "simplebuilder.usingMapStructIntegration";

  private static volatile State state = State.INIT;

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
  static void initCompilation(Boolean integrationEnabled) {
    BY_BEAN.clear();
    BY_BUILDER.clear();
    SimpleBuildersSpiIntegration.integrationEnabled = integrationEnabled;
    state = State.PROCESSING;
  }

  /** Marks the compilation as finished: the registry will not grow any further. */
  static void finishCompilation() {
    state = State.FINISHED;
  }

  /**
   * Called by each SPI adapter on {@code init} to age out a stale {@link State#FINISHED} left by a
   * previous compilation: javac initializes every processor lazily in its turn, so an SPI init may
   * run before {@link BuilderProcessor#init} of the same compilation, and a {@link State#FINISHED}
   * observed here can only be leftover — a live {@link State#FINISHED} implies the last round
   * already ran and no new SPI init would follow — and is reset to {@link State#INIT}. This
   * corrects the observation; it does not declare generation state, which {@link BuilderProcessor}
   * alone transitions.
   */
  public static void spiInitialized() {
    if (state == State.FINISHED) {
      state = State.INIT;
    }
  }

  /** The lifecycle state the SPI adapters observe for the current compilation. */
  public static State state() {
    return state;
  }

  /**
   * Publishes the builder planned for {@code beanType} in this compilation under the standard
   * contract every generated builder satisfies: {@code create()} obtains an empty builder instance
   * and {@code build()} returns the finished bean.
   */
  static void registerBuilder(TypeName beanType, TypeName builderType, String setterSuffix) {
    PublishedBuilder publishedBuilder =
        new PublishedBuilder(
            beanType,
            new ResolvedBuilder(builderType, new StaticFactoryCall("create"), null),
            setterSuffix);
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
   * Whether the integration is switched off: while the processor has published this compilation's
   * resolution ({@link State#PROCESSING} or {@link State#FINISHED}) it wins; without one — incl.
   * the stale leftovers of a previous run in {@link State#INIT} — the {@code simplebuilder.*}
   * convention applies (JVM system property before the annotation processor option the hosting
   * framework does not forward anyway).
   */
  public static boolean isIntegrationDisabled(Map<String, String> processorOptions) {
    if (state != State.INIT) {
      Boolean published = integrationEnabled;
      if (published != null) {
        return !published;
      }
    }
    String value =
        System.getProperty(OPTION_USING_MAPSTRUCT, processorOptions.get(OPTION_USING_MAPSTRUCT));
    return "false".equalsIgnoreCase(value) || "disabled".equalsIgnoreCase(value);
  }
}
