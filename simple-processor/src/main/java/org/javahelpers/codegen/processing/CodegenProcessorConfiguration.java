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

package org.javahelpers.codegen.processing;

import java.util.List;

/**
 * Static declarations of an {@link AbstractCodegenProcessor}: everything the framework needs to
 * know up front, collected in one record the processor hands to the base constructor.
 *
 * <p>Only {@link #elementCollector} is required; all other components carry defaults a minimal
 * processor can leave unset. Use {@link #builder()} to build an instance.
 *
 * @param elementCollector which elements the domain generates code for (required)
 * @param optionRegistry the processor's declared compiler options; default: an empty registry
 * @param requiredSourceVersion the minimum source level by name (e.g. {@code "RELEASE_17"});
 *     default: the running JDK's latest supported release
 * @param loggerPrefix the diagnostic prefix carried by every emitted log line ({@code "prefix: "});
 *     default: the processor's simple class name
 */
public record CodegenProcessorConfiguration(
    ElementCollector elementCollector,
    ProcessorOptionRegistry optionRegistry,
    String requiredSourceVersion,
    String loggerPrefix) {

  /**
   * Creates a builder with all defaults applied — only {@link
   * Builder#elementCollector(ElementCollector)} must be set.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder for {@link CodegenProcessorConfiguration}. */
  public static final class Builder {
    private ElementCollector elementCollector;
    private ProcessorOptionRegistry optionRegistry = new ProcessorOptionRegistry(List.of());
    private String requiredSourceVersion;
    private String loggerPrefix;

    private Builder() {}

    /**
     * Sets which elements the domain generates code for (required).
     *
     * @param elementCollector the collector declaring which elements to process
     * @return this builder
     */
    public Builder elementCollector(ElementCollector elementCollector) {
      this.elementCollector = elementCollector;
      return this;
    }

    /**
     * Sets the processor's declared compiler options.
     *
     * @param optionRegistry the option registry
     * @return this builder
     */
    public Builder optionRegistry(ProcessorOptionRegistry optionRegistry) {
      this.optionRegistry = optionRegistry;
      return this;
    }

    /**
     * Sets the minimum source level the processor supports, by {@code SourceVersion} constant name
     * (e.g. {@code "RELEASE_17"}). A name — not the enum constant — keeps the check resolvable at
     * processing time: a JDK whose {@code SourceVersion} lacks the constant still reports a
     * graceful error instead of a linkage failure.
     *
     * @param requiredSourceVersion the required source version name
     * @return this builder
     */
    public Builder requiredSourceVersion(String requiredSourceVersion) {
      this.requiredSourceVersion = requiredSourceVersion;
      return this;
    }

    /**
     * Sets the diagnostic prefix every emitted log line carries ({@code "prefix: "}). Default: the
     * processor's simple class name.
     *
     * @param loggerPrefix the logger prefix
     * @return this builder
     */
    public Builder loggerPrefix(String loggerPrefix) {
      this.loggerPrefix = loggerPrefix;
      return this;
    }

    /**
     * Builds the configuration.
     *
     * @return the configuration
     * @throws IllegalStateException when no {@link #elementCollector} was set
     */
    public CodegenProcessorConfiguration build() {
      if (elementCollector == null) {
        throw new IllegalStateException("elementCollector is required");
      }
      return new CodegenProcessorConfiguration(
          elementCollector, optionRegistry, requiredSourceVersion, loggerPrefix);
    }
  }
}
