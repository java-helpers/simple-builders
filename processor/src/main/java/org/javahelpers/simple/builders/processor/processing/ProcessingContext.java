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

package org.javahelpers.simple.builders.processor.processing;

import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.analysis.BuilderScopeResolver;
import org.javahelpers.simple.builders.processor.analysis.TypeMappingContext;
import org.javahelpers.simple.builders.processor.generators.registry.GeneratorRegistry;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.processing.logging.ActivePerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.NoOpPerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Context object that wraps Elements, Types, and logging utilities from annotation processing,
 * providing domain-specific methods for type and element operations.
 *
 * <p>This eliminates repetitive parameter passing and provides a cleaner API.
 */
public final class ProcessingContext extends AnnotationProcessingContext
    implements TypeMappingContext {
  private final BuilderConfigurationReader configurationReader;
  private final BuilderScopeResolver builderScopeResolver;

  private GeneratorRegistry generatorRegistry;
  private ProcessingTarget processingTarget;

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
        new BuilderConfigurationReader(globalConfiguration, logger, elementUtils);
    this.builderScopeResolver = new BuilderScopeResolver(this);
    // GeneratorRegistry will be lazily initialized on first access
  }

  private static PerformanceTracker createPerformanceTracker(ProcessingEnvironment processingEnv) {
    // Initialize performance tracker based on compiler argument
    CompilerArgumentsReader argReader = new CompilerArgumentsReader(processingEnv);
    boolean perfTrackingEnabled =
        argReader.readBooleanValue(CompilerArgumentsEnum.PERFORMANCE_TRACKING);
    String perfOutputFile = argReader.readValue(CompilerArgumentsEnum.PERFORMANCE_OUTPUT_FILE);
    return perfTrackingEnabled
        ? new ActivePerformanceTracker(perfOutputFile, PipelinePhases.PIPELINE_HIERARCHY)
        : new NoOpPerformanceTracker();
  }

  /**
   * Initializes the per-target state ({@link ProcessingTarget}) for the type whose builder is
   * currently being generated. Called once per target before extraction starts.
   *
   * @param processingTarget the resolved configuration and builder package of the current target
   */
  public void initProcessingTarget(ProcessingTarget processingTarget) {
    this.processingTarget = processingTarget;
  }

  /**
   * Gets the configuration for the current processing target.
   *
   * @return the builder configuration for the target being processed
   */
  public BuilderConfiguration getConfiguration() {
    return this.processingTarget.configuration();
  }

  /**
   * Gets the package the builder of the current processing target is generated in.
   *
   * @return the qualified package name of the generated builder
   */
  public String getBuilderPackageName() {
    return this.processingTarget.builderPackage();
  }

  /**
   * Checks whether a member (constructor, method) is accessible from the package the generated
   * builder is written to.
   *
   * <p>Public members are always accessible. Private members are never accessible - the generated
   * builder is a separate top-level class. Package-private and protected members are only
   * accessible when the member's declaring package equals the builder package (protected access
   * through inheritance does not apply, as the builder does not extend the target type).
   *
   * @param member the member to check
   * @return {@code true} if generated code in the builder package may call the member
   */
  @Override
  public boolean isAccessibleFromGeneratedPackage(Element member) {
    return isMemberAccessibleFromBuilderPackage(member);
  }

  /**
   * Tests whether a member is accessible from the package the generated builder is written to.
   *
   * @param member the member to check
   * @return {@code true} if the member is accessible from the builder package
   */
  public boolean isMemberAccessibleFromBuilderPackage(Element member) {
    if (member.getModifiers().contains(Modifier.PUBLIC)) {
      return true;
    }
    if (member.getModifiers().contains(Modifier.PRIVATE)) {
      return false;
    }
    return getPackageName(member).equals(getBuilderPackageName());
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
      generatorRegistry = new GeneratorRegistry(this, processingEnv);
    }
    return generatorRegistry;
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
   * {@inheritDoc}
   *
   * <p>Backed by the resolved per-target builder configuration.
   */
  @Override
  public boolean shouldCopyTypeAnnotations() {
    return getConfiguration().shouldCopyTypeAnnotations();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegated to the builder scope resolver.
   */
  @Override
  public Optional<ResolvedBuilder> resolveGeneratedType(TypeElement typeElement) {
    return builderScopeResolver.resolveUsableBuilderType(typeElement);
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
      logger.error(element, format, args);
    } else {
      logger.warning(element, format, args);
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
      logger.error(format, args);
    } else {
      logger.warning(format, args);
    }
  }
}
