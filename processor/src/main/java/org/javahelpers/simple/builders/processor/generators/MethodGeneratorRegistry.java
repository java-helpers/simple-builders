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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
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

  /**
   * Creates a new registry and initializes it with built-in and custom generators.
   *
   * @param context the processing context for configuration and utilities
   */
  public MethodGeneratorRegistry(ProcessingContext context) {
    this.context = context;
    this.generators = new ArrayList<>();

    registerBuiltInGenerators();
    loadCustomGenerators();
    sortGeneratorsByPriority();

    context.debug("Initialized MethodGeneratorRegistry with %d generators", generators.size());
  }

  /**
   * Generates all applicable methods for a field by invoking all registered generators.
   *
   * <p>Generators are invoked in priority order (highest first). Each generator that applies to the
   * field contributes its methods to the result list.
   *
   * @param field the field being processed
   * @param fieldParameter the variable element representing the field parameter
   * @param fieldTypeElement the type element of the field's type, or null if not available
   * @param builderType the type of the builder being generated
   * @return list of all generated methods from all applicable generators
   */
  public List<MethodDto> generateAllMethods(
      FieldDto field,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      TypeName builderType) {
    List<MethodDto> allMethods = new ArrayList<>();

    for (MethodGenerator generator : generators) {
      if (generator.appliesTo(field, fieldParameter, fieldTypeElement, context)) {
        context.debug(
            "  -> Applying generator: %s (priority: %d)",
            generator.getClass().getSimpleName(), generator.getPriority());

        List<MethodDto> generatedMethods =
            generator.generateMethods(
                field, fieldParameter, fieldTypeElement, builderType, context);

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
   * Registers all built-in method generators.
   *
   * <p>Built-in generators are added in declaration order, but will be sorted by priority after all
   * generators are registered.
   */
  private void registerBuiltInGenerators() {
    generators.add(new BasicSetterGenerator());
    generators.add(new StringFormatHelperGenerator());
    generators.add(new OptionalHelperGenerator());
    generators.add(new ConsumerMethodGenerator());
    generators.add(new SupplierMethodGenerator());
    generators.add(new VarArgsHelperGenerator());
    generators.add(new CollectionHelperGenerator());

    context.debug("Registered %d built-in generators", generators.size());
  }

  /**
   * Loads custom generators provided by library users via ServiceLoader.
   *
   * <p>Custom generators are discovered by looking for implementations of {@link MethodGenerator}
   * declared in {@code
   * META-INF/services/org.javahelpers.simple.builders.processor.generators.MethodGenerator} files.
   *
   * <p>If loading fails for any generator, a warning is logged but processing continues with the
   * remaining generators.
   */
  private void loadCustomGenerators() {
    int customCount = 0;
    try {
      ServiceLoader<MethodGenerator> serviceLoader =
          ServiceLoader.load(MethodGenerator.class, MethodGenerator.class.getClassLoader());

      for (MethodGenerator generator : serviceLoader) {
        generators.add(generator);
        customCount++;
        context.debug(
            "Loaded custom generator: %s (priority: %d)",
            generator.getClass().getName(), generator.getPriority());
      }
    } catch (Exception e) {
      context.warning(
          null, "Failed to load custom method generators via ServiceLoader: %s", e.getMessage());
    }

    if (customCount > 0) {
      context.debug("Loaded %d custom generator(s) via ServiceLoader", customCount);
    }
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
