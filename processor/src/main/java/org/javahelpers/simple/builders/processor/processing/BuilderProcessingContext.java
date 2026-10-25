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
import javax.lang.model.element.TypeElement;
import org.javahelpers.codegen.processing.ProcessingTarget;
import org.javahelpers.simple.builders.processor.analysis.BuilderScopeResolver;
import org.javahelpers.simple.builders.processor.generators.registry.GeneratorRegistry;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Context object that wraps Elements, Types, and logging utilities from annotation processing,
 * providing domain-specific methods for type and element operations.
 *
 * <p>This eliminates repetitive parameter passing and provides a cleaner API.
 */
public final class BuilderProcessingContext extends AnnotationProcessingContext {
  private final BuilderConfigurationReader configurationReader;
  private final BuilderScopeResolver builderScopeResolver;

  private GeneratorRegistry generatorRegistry;
  private ProcessingTarget<BuilderConfiguration> processingTarget;

  /**
   * Creates a new processing context.
   *
   * @param logger the logging utility for the annotation processor
   * @param globalConfiguration the global builder configuration read from compiler arguments
   * @param processingEnv the processing environment providing access to utilities and facilities
   */
  public BuilderProcessingContext(
      ProcessingLogger logger,
      BuilderConfiguration globalConfiguration,
      ProcessingEnvironment processingEnv) {
    super(logger, processingEnv, createOptions(processingEnv, globalConfiguration));
    this.configurationReader =
        new BuilderConfigurationReader(globalConfiguration, logger, elementUtils);
    this.builderScopeResolver = new BuilderScopeResolver(this);
    // GeneratorRegistry will be lazily initialized on first access
  }

  private static AnnotationProcessingOptions createOptions(
      ProcessingEnvironment processingEnv, BuilderConfiguration globalConfiguration) {
    // Derive generic context options from the builder's compiler arguments
    CompilerArgumentsReader argReader = new CompilerArgumentsReader(processingEnv);
    return AnnotationProcessingOptions.builder()
        .strictModeEnabled(globalConfiguration.isStrictModeEnabled())
        .performanceTrackingEnabled(
            argReader.readBooleanValue(CompilerArgumentsEnum.PERFORMANCE_TRACKING))
        .performanceOutputFile(argReader.readValue(CompilerArgumentsEnum.PERFORMANCE_OUTPUT_FILE))
        .phaseHierarchy(PipelinePhases.PIPELINE_HIERARCHY)
        .build();
  }

  /**
   * Initializes the per-target state ({@link ProcessingTarget}) for the type whose builder is
   * currently being generated. Called once per target before extraction starts.
   *
   * @param processingTarget the resolved configuration and builder package of the current target
   */
  public void initProcessingTarget(ProcessingTarget<BuilderConfiguration> processingTarget) {
    this.processingTarget = processingTarget;
    setGeneratedPackageName(processingTarget.targetPackage());
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
    return this.processingTarget.targetPackage();
  }

  /**
   * Tests whether a member is accessible from the package the generated builder is written to.
   *
   * @param member the member to check
   * @return {@code true} if the member is accessible from the builder package
   */
  public boolean isMemberAccessibleFromBuilderPackage(Element member) {
    return isAccessibleFromGeneratedPackage(member);
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
  public Optional<TypeName> resolveGeneratedType(TypeElement typeElement) {
    return builderScopeResolver
        .resolveUsableBuilderType(typeElement)
        .map(ResolvedBuilder::typeName);
  }

  /**
   * Resolves the builder usable for the given type name, via its type element.
   *
   * @param typeName the referenced type name to resolve
   * @return the usable builder, or empty when none applies or the type cannot be resolved
   */
  public Optional<ResolvedBuilder> resolveBuilderType(TypeName typeName) {
    return Optional.ofNullable(getTypeElement(typeName.getFullQualifiedName()))
        .flatMap(builderScopeResolver::resolveUsableBuilderType);
  }
}
