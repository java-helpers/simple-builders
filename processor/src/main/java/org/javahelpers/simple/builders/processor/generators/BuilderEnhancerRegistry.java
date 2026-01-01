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
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Registry for managing and applying builder enhancers.
 *
 * <p>This registry discovers all {@link BuilderEnhancer} implementations via ServiceLoader and
 * applies them to builders in priority order. Built-in enhancers are automatically included via the
 * service file in this module, while custom enhancers can be provided by library users.
 *
 * <p>The registry follows the same pattern as {@link MethodGeneratorRegistry} for consistency.
 */
public class BuilderEnhancerRegistry {

  private final List<BuilderEnhancer> enhancers;
  private final ProcessingContext context;

  /**
   * Creates a new registry and initializes it with built-in and custom enhancers.
   *
   * @param context the processing context for configuration and utilities
   */
  public BuilderEnhancerRegistry(ProcessingContext context) {
    this.context = context;
    this.enhancers = new ArrayList<>();

    loadAllEnhancers();
    sortEnhancersByPriority();

    context.debug("Initialized BuilderEnhancerRegistry with %d enhancers", enhancers.size());
  }

  /**
   * Applies all applicable enhancers to the given builder.
   *
   * @param builderDto the builder to enhance
   * @param dtoType the DTO type the builder is for
   */
  public void enhanceBuilder(BuilderDefinitionDto builderDto, TypeName dtoType) {
    for (BuilderEnhancer enhancer : enhancers) {
      if (enhancer.appliesTo(builderDto, dtoType, context)) {
        try {
          enhancer.enhanceBuilder(builderDto, context);
          context.debug(
              "Applied enhancer: %s to builder %s",
              enhancer.getClass().getName(), builderDto.getBuilderTypeName().getClassName());
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
   * Loads all builder enhancers (built-in and custom) via ServiceLoader.
   *
   * <p>Enhancers are discovered by looking for implementations of {@link BuilderEnhancer} declared
   * in {@code
   * META-INF/services/org.javahelpers.simple.builders.processor.generators.BuilderEnhancer} files.
   *
   * <p>Built-in enhancers are automatically included via the service file in this module, while
   * custom enhancers can be provided by library users in their own modules.
   *
   * <p>If loading fails for any enhancer, a warning is logged but processing continues with the
   * remaining enhancers.
   */
  private void loadAllEnhancers() {
    int loadedCount = 0;
    try {
      ServiceLoader<BuilderEnhancer> serviceLoader =
          ServiceLoader.load(BuilderEnhancer.class, BuilderEnhancer.class.getClassLoader());

      for (BuilderEnhancer enhancer : serviceLoader) {
        enhancers.add(enhancer);
        loadedCount++;

        context.debug(
            "Loaded enhancer: %s (priority: %d)",
            enhancer.getClass().getName(), enhancer.getPriority());
      }
    } catch (Exception e) {
      context.error("Failed to load enhancers: %s", e.getMessage());
    }

    context.debug("Loaded %d enhancers total", loadedCount);
  }

  /**
   * Sorts all registered enhancers by priority in descending order (highest priority first).
   *
   * <p>This ensures that enhancers with higher priority values execute before those with lower
   * priority values.
   */
  private void sortEnhancersByPriority() {
    enhancers.sort(Comparator.comparingInt(BuilderEnhancer::getPriority).reversed());
  }
}
