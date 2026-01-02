/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ComponentFilter;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Registry that manages all method generators and orchestrates method generation for builder
 * fields.
 *
 * <p>This class is responsible for:
 *
 * <ul>
 *   <li>Registering built-in method generators
 *   <li>Loading custom generators via ServiceLoader
 *   <li>Sorting generators by priority
 *   <li>Coordinating method generation across all applicable generators
 * </ul>
 *
 * <p>The registry follows a chain-of-responsibility pattern where each generator is given the
 * opportunity to contribute methods to a field if it applies.
 */
public class MethodGeneratorRegistry {

  private final List<MethodGenerator> generators;
  private final ProcessingContext context;
  private final ComponentFilter componentFilter;

  /**
   * Creates a new registry and initializes it with built-in and custom generators.
   *
   * @param context the processing context for configuration and utilities
   * @param processingEnv the processing environment for reading compiler arguments
   */
  public MethodGeneratorRegistry(ProcessingContext context, ProcessingEnvironment processingEnv) {
    this.context = context;
    this.generators = new ArrayList<>();
    this.componentFilter = new ComponentFilter(processingEnv);

    loadAllGenerators();
    sortGeneratorsByPriority();

    context.debug("Initialized MethodGeneratorRegistry with %d generators", generators.size());
  }

  /**
   * Generates all methods for a field using all registered generators.
   *
   * @param field the field to generate methods for, should not be null
   * @param dtoType the TypeName of the DTO containing the field, should not be null
   * @param builderType the type of the builder being generated, should not be null
   * @return list of all generated methods from all applicable generators
   */
  public List<MethodDto> generateAllMethods(
      FieldDto field, TypeName dtoType, TypeName builderType) {
    List<MethodDto> allMethods = new ArrayList<>();

    for (MethodGenerator generator : generators) {
      if (generator.appliesTo(field, dtoType, context)) {
        context.debug(
            "  -> Applying generator: %s (priority: %d)",
            generator.getClass().getSimpleName(), generator.getPriority());

        List<MethodDto> generatedMethods = generator.generateMethods(field, builderType, context);

        if (generatedMethods != null && !generatedMethods.isEmpty()) {
          allMethods.addAll(generatedMethods);
          context.debug(
              "     Generated %d method(s) from %s",
              generatedMethods.size(), generator.getClass().getSimpleName());
        }
      }
    }

    return allMethods;
  }

  /**
   * Loads all method generators (built-in and custom) via ServiceLoader.
   *
   * <p>Generators are discovered by looking for implementations of {@link MethodGenerator} declared
   * in {@code
   * META-INF/services/org.javahelpers.simple.builders.processor.generators.MethodGenerator} files.
   *
   * <p>Built-in generators are automatically included via the service file in this module, while
   * custom generators can be provided by library users in their own modules.
   *
   * <p>If loading fails for any generator, a warning is logged but processing continues with the
   * remaining generators.
   */
  private void loadAllGenerators() {
    int loadedCount = 0;
    try {
      ServiceLoader<MethodGenerator> serviceLoader =
          ServiceLoader.load(MethodGenerator.class, MethodGenerator.class.getClassLoader());

      for (MethodGenerator generator : serviceLoader) {
        String generatorClassName = generator.getClass().getName();

        // Check if this generator should be deactivated
        if (componentFilter.shouldDeactivateComponent(generatorClassName)) {
          context.debug("Skipping deactivated generator: %s", generatorClassName);
          continue;
        }

        generators.add(generator);
        loadedCount++;

        context.debug(
            "Loaded generator: %s (priority: %d)", generatorClassName, generator.getPriority());
      }
    } catch (Exception e) {
      context.error("Failed to load generators: %s", e.getMessage());
    }

    context.debug("Loaded %d generators total", loadedCount);
  }

  /**
   * Sorts all registered generators by priority in descending order (highest priority first).
   *
   * <p>This ensures that generators with higher priority values execute before those with lower
   * priority values.
   */
  private void sortGeneratorsByPriority() {
    generators.sort(Comparator.comparingInt(MethodGenerator::getPriority).reversed());
  }

  /**
   * Returns the number of registered generators (for testing/debugging).
   *
   * @return the total number of registered generators
   */
  public int getGeneratorCount() {
    return generators.size();
  }
}
