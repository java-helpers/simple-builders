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

import java.util.List;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Service Provider Interface (SPI) for generating builder methods for fields.
 *
 * <p>Implementations of this interface are responsible for generating specific types of methods
 * (e.g., setters, consumers, suppliers, helpers) for builder fields. Each generator focuses on a
 * single feature or method type.
 *
 * <p>Custom generators can be provided by library users through the Java ServiceLoader mechanism by
 * creating a file {@code
 * META-INF/services/org.javahelpers.simple.builders.processor.generators.MethodGenerator}
 * containing the fully qualified class names of custom generator implementations.
 *
 * <p>Generators are executed in priority order (highest first). Multiple generators can contribute
 * methods to the same field.
 */
public interface MethodGenerator {

  /**
   * Returns the priority of this generator. Generators with higher priority values are executed
   * first.
   *
   * <p>Built-in generator priorities:
   *
   * <ul>
   *   <li>100 - Basic setters (highest priority)
   *   <li>80 - String format helpers
   *   <li>70 - Optional helpers
   *   <li>60 - Supplier methods
   *   <li>50 - Consumer methods
   *   <li>40 - VarArgs helpers
   *   <li>30 - Collection helpers
   *   <li>20 - With interface
   * </ul>
   *
   * <p>Custom generators should use values between 0-200 to integrate with built-in generators.
   *
   * @return the priority value (higher values execute first)
   */
  int getPriority();

  /**
   * Determines whether this generator applies to the given field based on field type,
   * configuration, and other context.
   *
   * <p>This method is called before {@link #generateMethods} to determine if the generator should
   * be invoked for a particular field. Generators should check:
   *
   * <ul>
   *   <li>Configuration flags (e.g., {@code shouldGenerateBuilderConsumer()})
   *   <li>Field type compatibility (e.g., only for collections, strings, etc.)
   *   <li>DTO package or annotations (e.g., only for specific packages or annotated DTOs)
   * </ul>
   *
   * @param field the field being processed
   * @param dtoType the type of the DTO class containing this field
   * @param context the processing context containing configuration and utilities
   * @return true if this generator should generate methods for this field, false otherwise
   */
  boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context);

  /**
   * Generates methods for the given field.
   *
   * <p>This method is only called if {@link #appliesTo} returns true. Implementations should
   * generate all relevant methods for their feature and return them as a list.
   *
   * <p>The returned methods will be added to the field's method list and eventually rendered in the
   * generated builder class.
   *
   * @param field the field being processed (contains field name, type, javadoc, builder types,
   *     etc.)
   * @param builderType the type of the builder being generated (used for return types)
   * @param context the processing context containing configuration and utilities
   * @return list of generated methods (may be empty but should not be null)
   */
  List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context);
}
