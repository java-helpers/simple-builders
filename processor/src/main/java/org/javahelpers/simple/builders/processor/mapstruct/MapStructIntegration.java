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

package org.javahelpers.simple.builders.processor.mapstruct;

import java.util.HashMap;
import java.util.Map;

/**
 * State {@code BuilderProcessor} shares with the MapStruct SPI adapters while they share the
 * annotation processor path (and therefore the classloader): the resolved {@code
 * simplebuilder.usingMapStructIntegration} switch — which SPI environments cannot see because
 * MapStruct forwards only its own processor options — and the qualified names of the builders
 * planned for generation, so lookups do not have to guess names or scan packages.
 *
 * <p>All state is compilation-scoped: {@link #initCompilation} resets it when the processor is
 * initialized, which matters in long-lived JVMs (Gradle daemon, incremental builds) that run
 * several compilations on the same classloader.
 */
public final class MapStructIntegration {

  private static final String OPTION_USING_MAPSTRUCT = "simplebuilder.usingMapStructIntegration";

  /** The processor-resolved switch; {@code null} leaves the system-property fallback active. */
  private static volatile Boolean enabled;

  /** Builders planned for the current compilation: bean qualified name → builder qualified name. */
  private static final Map<String, String> GENERATED_BUILDERS = new HashMap<>();

  /** Reverse of {@link #GENERATED_BUILDERS}: builder qualified name → bean qualified name. */
  private static final Map<String, String> BEAN_BY_BUILDER = new HashMap<>();

  private MapStructIntegration() {}

  /**
   * Starts a new compilation: clears the builder registry and publishes the integration switch the
   * processor resolved ({@code null} when the option is unset).
   */
  public static void initCompilation(Boolean integrationEnabled) {
    GENERATED_BUILDERS.clear();
    BEAN_BY_BUILDER.clear();
    enabled = integrationEnabled;
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
   * Whether the integration is switched off: the value the processor published wins; without one
   * the {@code simplebuilder.*} convention applies (JVM system property before the annotation
   * processor option MapStruct does not forward anyway).
   */
  static boolean isDisabled(Map<String, String> processorOptions) {
    Boolean published = enabled;
    if (published != null) {
      return !published;
    }
    String value = AnnotationSupport.systemOption(processorOptions, OPTION_USING_MAPSTRUCT);
    return "false".equalsIgnoreCase(value) || "disabled".equalsIgnoreCase(value);
  }
}
