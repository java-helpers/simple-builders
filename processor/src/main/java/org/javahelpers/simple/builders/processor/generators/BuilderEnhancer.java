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

import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Interface for enhancing or modifying generated builders beyond the basic field-based methods.
 *
 * <p>BuilderEnhancers provide a plugin mechanism for adding custom functionality to generated
 * builders, such as:
 *
 * <ul>
 *   <li>Additional utility methods (e.g., validation, transformation)
 *   <li>Interface implementations (e.g., With, Serializable)
 *   <li>Custom annotations or documentation
 *   <li>Builder composition or delegation patterns
 * </ul>
 *
 * <p>Enhancers are discovered via ServiceLoader and applied in priority order (highest first). Each
 * enhancer can decide whether it applies to a specific builder type and then modify the builder
 * accordingly.
 *
 * <p>Unlike {@link MethodGenerator} which operates on individual fields, BuilderEnhancers operate
 * on the entire builder after all field methods have been generated.
 */
public interface BuilderEnhancer {

  /**
   * Returns the priority of this enhancer.
   *
   * <p>Higher priority values are executed first. Use this to control the order of enhancements
   * when multiple enhancers might interact with each other.
   *
   * <p>Recommended priority ranges:
   *
   * <ul>
   *   <li>90-100: Critical infrastructure (interfaces, annotations)
   *   <li>70-89: Core functionality (validation, transformation)
   *   <li>50-69: Utility methods (convenience helpers)
   *   <li>30-49: Optional features (debugging, logging)
   *   <li>10-29: Experimental or user-specific enhancements
   * </ul>
   *
   * @return the priority value (higher = executed first)
   */
  int getPriority();

  /**
   * Determines whether this enhancer should be applied to the given builder.
   *
   * <p>This method allows enhancers to selectively apply based on:
   *
   * <ul>
   *   <li>Builder type characteristics (package, annotations, interfaces)
   *   <li>Configuration settings (enabled features, options)
   *   <li>Field composition (presence of certain field types)
   * </ul>
   *
   * @param builderDto the builder being generated
   * @param dtoType the DTO type the builder is for
   * @param context the processing context for configuration and utilities
   * @return true if this enhancer should be applied to this builder
   */
  boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context);

  /**
   * Enhances the builder by adding or modifying its structure.
   *
   * <p>This method is called after all field-based methods have been generated, allowing enhancers
   * to add:
   *
   * <ul>
   *   <li>Additional methods (utility, validation, transformation)
   *   <li>Interface implementations
   *   <li>Annotations on the builder class or methods
   *   <li>JavaDoc documentation
   *   <li>Inner classes or enums
   * </ul>
   *
   * @param builderDto the builder to enhance (modifiable)
   * @param context the processing context for configuration and utilities
   */
  void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context);
}
