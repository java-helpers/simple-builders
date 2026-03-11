/*
 * MIT License
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

package org.javahelpers.simple.builders.processor.generators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.collections4.CollectionUtils;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ComponentFilter;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Unified registry that manages all generators (both method generators and builder enhancers).
 *
 * <p>This class is responsible for:
 *
 * <ul>
 *   <li>Loading all generators via ServiceLoader (built-in and custom)
 *   <li>Sorting generators by priority (highest first)
 *   <li>Coordinating field-level method generation via {@link MethodGenerator}
 *   <li>Coordinating builder-level enhancement via {@link BuilderEnhancer}
 * </ul>
 *
 * <p>The registry loads all {@link Generator} implementations from a single service file and
 * separates them into method generators and builder enhancers based on their type.
 */
public class GeneratorRegistry {

  private final List<MethodGenerator> methodGenerators;
  private final List<BuilderEnhancer> builderEnhancers;
  private final ProcessingContext context;
  private final ComponentFilter componentFilter;

  /**
   * Creates a new registry and initializes it with built-in and custom generators.
   *
   * @param context the processing context for configuration and utilities
   * @param processingEnv the processing environment for reading compiler arguments
   */
  public GeneratorRegistry(ProcessingContext context, ProcessingEnvironment processingEnv) {
    this.context = context;
    this.methodGenerators = new ArrayList<>();
    this.builderEnhancers = new ArrayList<>();
    this.componentFilter = new ComponentFilter(processingEnv);

    loadAllGenerators();
    sortGeneratorsByPriority();

    context.debug(
        "Initialized GeneratorRegistry with %d method generators and %d builder enhancers",
        methodGenerators.size(), builderEnhancers.size());
  }

  /**
   * Generates all methods for a field using all registered method generators.
   *
   * @param field the field to generate methods for, should not be null
   * @param dtoType the TypeName of the DTO containing the field, should not be null
   * @param builderType the type of the builder being generated, should not be null
   * @return list of all generated methods from all applicable generators
   */
  public List<MethodDto> generateAllMethods(
      FieldDto field, TypeName dtoType, TypeName builderType) {
    List<MethodDto> allMethods = new ArrayList<>();

    for (MethodGenerator generator : methodGenerators) {
      if (generator.appliesTo(field, dtoType, context)) {
        try {
          context.debug(
              "  -> Applying method generator: %s (priority: %d)",
              generator.getClass().getSimpleName(), generator.getPriority());

          List<MethodDto> generatedMethods = generator.generateMethods(field, builderType, context);

          if (CollectionUtils.isNotEmpty(generatedMethods)) {
            allMethods.addAll(generatedMethods);
          }
        } catch (Exception e) {
          context.error(
              "Failed to apply method generator %s to field %s: %s",
              generator.getClass().getName(), field.getFieldName(), e.getMessage());
        }
      }
    }

    return allMethods;
  }

  /**
   * Applies all applicable builder enhancers to the given builder.
   *
   * @param builderDto the builder to enhance
   * @param dtoType the DTO type the builder is for
   */
  public void enhanceBuilder(BuilderDefinitionDto builderDto, TypeName dtoType) {
    for (BuilderEnhancer enhancer : builderEnhancers) {
      if (enhancer.appliesTo(builderDto, dtoType, context)) {
        try {
          context.debug(
              "  -> Applying builder enhancer: %s (priority: %d)",
              enhancer.getClass().getSimpleName(), enhancer.getPriority());

          enhancer.enhanceBuilder(builderDto, context);

          context.debug(
              "     Enhanced builder %s with %s",
              builderDto.getBuilderTypeName().getClassName(), enhancer.getClass().getSimpleName());
        } catch (Exception e) {
          context.error(
              "Failed to apply enhancer %s to builder %s: %s",
              enhancer.getClass().getName(),
              builderDto.getBuilderTypeName().getClassName(),
              e.getMessage());
        }
      }
    }
  }

  /**
   * Loads all generators (built-in and custom) via ServiceLoader.
   *
   * <p>Generators are discovered by looking for implementations of {@link Generator} declared in
   * {@code META-INF/services/org.javahelpers.simple.builders.processor.generators.Generator} files.
   *
   * <p>The loaded generators are separated into method generators and builder enhancers based on
   * their type (using the sealed interface hierarchy).
   *
   * <p>If loading fails for any generator, a warning is logged but processing continues with the
   * remaining generators.
   */
  private void loadAllGenerators() {
    int methodGenCount = 0;
    int enhancerCount = 0;

    try {
      ServiceLoader<Generator> serviceLoader =
          ServiceLoader.load(Generator.class, Generator.class.getClassLoader());

      for (Generator generator : serviceLoader) {
        String generatorClassName = generator.getClass().getName();

        // Check if this generator should be deactivated
        if (componentFilter.shouldDeactivateComponent(generatorClassName)) {
          context.debug("Skipping deactivated generator: %s", generatorClassName);
          continue;
        }

        // Separate into method generators and builder enhancers
        // The sealed interface ensures generator is either MethodGenerator or BuilderEnhancer
        if (generator instanceof MethodGenerator methodGen) {
          methodGenerators.add(methodGen);
          methodGenCount++;
          context.debug(
              "Loaded method generator: %s (priority: %d)",
              generatorClassName, methodGen.getPriority());
        } else if (generator instanceof BuilderEnhancer enhancer) {
          builderEnhancers.add(enhancer);
          enhancerCount++;
          context.debug(
              "Loaded builder enhancer: %s (priority: %d)",
              generatorClassName, enhancer.getPriority());
        }
      }
    } catch (Exception e) {
      context.error("Failed to load generators: %s", e.getMessage());
    }

    context.debug(
        "Loaded %d method generators and %d builder enhancers total",
        methodGenCount, enhancerCount);
  }

  /**
   * Sorts all registered generators by priority in descending order (highest priority first).
   *
   * <p>This ensures that generators with higher priority values execute before those with lower
   * priority values.
   */
  private void sortGeneratorsByPriority() {
    methodGenerators.sort(Comparator.comparingInt(MethodGenerator::getPriority).reversed());
    builderEnhancers.sort(Comparator.comparingInt(BuilderEnhancer::getPriority).reversed());
  }
}
