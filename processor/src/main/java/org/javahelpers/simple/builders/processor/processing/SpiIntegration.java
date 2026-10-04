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

package org.javahelpers.simple.builders.processor.processing;

import java.util.HashMap;
import java.util.Map;

/**
 * State {@code BuilderProcessor} shares with SPI adapters of other frameworks while they share the
 * annotation processor path (and therefore the classloader): the resolved integration switch —
 * which SPI environments cannot see because the hosting framework forwards only its own processor
 * options (e.g. MapStruct's {@code simplebuilder.usingMapStructIntegration}) — and the qualified
 * names of the builders planned for generation, so lookups do not have to guess names or scan
 * packages.
 *
 * <p>All state is compilation-scoped and guarded by {@link State}: javac initializes each processor
 * lazily when its turn in a round comes, so before {@code BuilderProcessor} has run its {@code
 * init()} nothing static can be trusted — values may be leftovers of a previous compilation in a
 * long-lived JVM (Gradle daemon, incremental builds). Only while {@link State#PROCESSING} or {@link
 * State#FINISHED} are the published switch and registry the current compilation's truth.
 */
public final class SpiIntegration {

  /** Lifecycle of the current compilation as the SPI adapters observe it. */
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

  private static final String OPTION_USING_MAPSTRUCT = "simplebuilder.usingMapStructIntegration";

  private static volatile State state = State.INIT;

  /** The processor-resolved switch; {@code null} leaves the system-property fallback active. */
  private static volatile Boolean enabled;

  /** Builders planned for the current compilation: bean qualified name → builder qualified name. */
  private static final Map<String, String> GENERATED_BUILDERS = new HashMap<>();

  /** Reverse of {@link #GENERATED_BUILDERS}: builder qualified name → bean qualified name. */
  private static final Map<String, String> BEAN_BY_BUILDER = new HashMap<>();

  private SpiIntegration() {}

  /**
   * Starts a new compilation: clears the builder registry and publishes the integration switch the
   * processor resolved ({@code null} when the option is unset).
   */
  public static void initCompilation(Boolean integrationEnabled) {
    GENERATED_BUILDERS.clear();
    BEAN_BY_BUILDER.clear();
    enabled = integrationEnabled;
    state = State.PROCESSING;
  }

  /** Marks the compilation as finished: the registry will not grow any further. */
  public static void finishCompilation() {
    state = State.FINISHED;
  }

  /**
   * Called by each SPI adapter on {@code init}. javac initializes every processor lazily in its
   * turn, so an SPI init may run before {@code BuilderProcessor#init} of the same compilation; a
   * {@link State#FINISHED} observed here can only be the previous compilation's leftover — a live
   * {@link State#FINISHED} implies the last round already ran and no new SPI init would follow —
   * and is reset to {@link State#INIT}.
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

  /** Registers a builder planned for {@code beanQualifiedName} in the current compilation. */
  public static void registerBuilder(String beanQualifiedName, String builderQualifiedName) {
    GENERATED_BUILDERS.put(beanQualifiedName, builderQualifiedName);
    BEAN_BY_BUILDER.put(builderQualifiedName, beanQualifiedName);
  }

  /** The qualified name of the builder planned for {@code beanQualifiedName}, or {@code null}. */
  public static String builderFor(String beanQualifiedName) {
    return GENERATED_BUILDERS.get(beanQualifiedName);
  }

  /**
   * The qualified name of the bean {@code builderQualifiedName} was generated for, or {@code null}.
   */
  public static String beanFor(String builderQualifiedName) {
    return BEAN_BY_BUILDER.get(builderQualifiedName);
  }

  /**
   * Whether the integration is switched off: while the processor has published this compilation's
   * resolution ({@link State#PROCESSING} or {@link State#FINISHED}) it wins; without one — incl.
   * the stale leftovers of a previous run in {@link State#INIT} — the {@code simplebuilder.*}
   * convention applies (JVM system property before the annotation processor option MapStruct does
   * not forward anyway).
   */
  public static boolean isDisabled(Map<String, String> processorOptions) {
    if (state != State.INIT) {
      Boolean published = enabled;
      if (published != null) {
        return !published;
      }
    }
    String value =
        System.getProperty(OPTION_USING_MAPSTRUCT, processorOptions.get(OPTION_USING_MAPSTRUCT));
    return "false".equalsIgnoreCase(value) || "disabled".equalsIgnoreCase(value);
  }
}
