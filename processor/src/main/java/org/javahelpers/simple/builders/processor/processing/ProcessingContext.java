/* MIT License
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

package org.javahelpers.simple.builders.processor.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.analysis.BuilderScopeResolver;
import org.javahelpers.simple.builders.processor.classgen.roaster.RoasterSourceFormatter;
import org.javahelpers.simple.builders.processor.generators.registry.GeneratorRegistry;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.processing.logging.ActivePerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.NoOpPerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Builder-specific processing context. Adds builder configuration, the generator registry and the
 * builder scope resolver on top of the generic element/type/log utilities of {@link
 * AnnotationProcessingContext}.
 */
public final class ProcessingContext extends AnnotationProcessingContext {
  private final BuilderConfigurationReader configurationReader;
  private final String formatterProfile;
  private final BuilderScopeResolver builderScopeResolver;
  private GeneratorRegistry generatorRegistry;
  private BuilderConfiguration configurationForProcessingTarget;

  /**
   * Creates a new processing context.
   *
   * @param logger the logging utility for the annotation processor
   * @param globalConfiguration the global builder configuration read from compiler arguments
   * @param processingEnv the processing environment providing access to utilities and facilities
   */
  public ProcessingContext(
      ProcessingLogger logger,
      BuilderConfiguration globalConfiguration,
      ProcessingEnvironment processingEnv) {
    super(logger, processingEnv, createPerformanceTracker(processingEnv));
    this.configurationReader =
        new BuilderConfigurationReader(globalConfiguration, logger, getElementUtils());
    this.formatterProfile =
        StringUtils.trimToNull(
            new CompilerArgumentsReader(processingEnv)
                .readValue(CompilerArgumentsEnum.FORMATTER_PROFILE));
    this.builderScopeResolver = new BuilderScopeResolver(this);
    // GeneratorRegistry will be lazily initialized on first access
  }

  /**
   * Creates the performance tracker based on the builder compiler arguments.
   *
   * @param processingEnv the processing environment providing compiler options
   * @return an {@link ActivePerformanceTracker} when enabled, a {@link NoOpPerformanceTracker}
   *     otherwise
   */
  private static PerformanceTracker createPerformanceTracker(ProcessingEnvironment processingEnv) {
    CompilerArgumentsReader argReader = new CompilerArgumentsReader(processingEnv);
    boolean perfTrackingEnabled =
        argReader.readBooleanValue(CompilerArgumentsEnum.PERFORMANCE_TRACKING);
    String perfOutputFile = argReader.readValue(CompilerArgumentsEnum.PERFORMANCE_OUTPUT_FILE);
    return perfTrackingEnabled
        ? new ActivePerformanceTracker(
            perfOutputFile, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN)
        : new NoOpPerformanceTracker();
  }

  /**
   * Initializes the configuration for the current processing target.
   *
   * @param config the builder configuration for the target being processed
   */
  public void initConfigurationForProcessingTarget(BuilderConfiguration config) {
    this.configurationForProcessingTarget = config;
  }

  /**
   * Gets the configuration for the current processing target.
   *
   * @return the builder configuration for the target being processed
   */
  public BuilderConfiguration getConfiguration() {
    return this.configurationForProcessingTarget;
  }

  /**
   * Gets the configuration reader for reading builder configurations.
   *
   * @return the builder configuration reader
   */
  public BuilderConfigurationReader getConfigurationReader() {
    return configurationReader;
  }

  /**
   * Get the unified generator registry for both field-level method generation and builder-level
   * enhancement.
   *
   * <p>The registry is lazily initialized on first access to avoid circular dependency issues.
   *
   * @return the generator registry
   */
  public GeneratorRegistry getGeneratorRegistry() {
    if (generatorRegistry == null) {
      generatorRegistry = new GeneratorRegistry(this, getProcessingEnvironment());
    }
    return generatorRegistry;
  }

  /**
   * Creates a {@link RoasterSourceFormatter} for the given formatting mode, applying the configured
   * Eclipse formatter profile when one is set.
   *
   * @param mode the formatting mode
   * @return a new formatter instance
   */
  public RoasterSourceFormatter createSourceFormatter(FormattingMode mode) {
    return new RoasterSourceFormatter(getLogger(), mode, formatterProfile);
  }

  /**
   * Get the builder scope resolver for deciding whether a builder may be referenced.
   *
   * <p>The resolver is keyed off the current target configuration and is recomputed when the target
   * configuration changes.
   *
   * @return the builder scope resolver
   */
  public BuilderScopeResolver getBuilderScopeResolver() {
    return builderScopeResolver;
  }

  /**
   * Returns whether strict/fail-fast generation mode is enabled via the global compiler
   * configuration.
   *
   * @return true if {@code -Asimplebuilder.strict=true} was supplied, false otherwise
   */
  public boolean isStrictModeEnabled() {
    return configurationReader.getGlobalConfiguration().isStrictModeEnabled();
  }

  /**
   * Reports an error or warning based on strict mode. In strict mode a build failure is emitted;
   * otherwise a warning is logged so generation of remaining builders can continue.
   *
   * @param element the element associated with the problem, for compiler location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers
   */
  public void reportBasedOnStrictMode(Element element, String format, Object... args) {
    if (isStrictModeEnabled()) {
      error(element, format, args);
    } else {
      warning(element, format, args);
    }
  }

  /**
   * Reports an error or warning based on strict mode, without an element location.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers
   */
  public void reportBasedOnStrictMode(String format, Object... args) {
    if (isStrictModeEnabled()) {
      error(format, args);
    } else {
      warning(format, args);
    }
  }
}
