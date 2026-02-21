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

/**
 * Marker interface for all generator components (method generators and builder enhancers).
 *
 * <p>This interface serves as a common parent for both {@link MethodGenerator} and {@link
 * BuilderEnhancer}, allowing them to be registered in a single ServiceLoader service file while
 * maintaining their distinct interfaces and method signatures.
 *
 * <p>This is a sealed interface that permits only two implementations:
 *
 * <ul>
 *   <li>{@link MethodGenerator} - for generating methods on individual fields
 *   <li>{@link BuilderEnhancer} - for enhancing the entire builder class
 * </ul>
 *
 * <p>Custom generators must implement one of the permitted sub-interfaces, not this interface
 * directly. Custom generators should be registered in {@code
 * META-INF/services/org.javahelpers.simple.builders.processor.generators.Generator}
 *
 * @see MethodGenerator
 * @see BuilderEnhancer
 */
public sealed interface Generator permits MethodGenerator, BuilderEnhancer {

  /**
   * Returns the priority of this generator. Generators with higher priority values are executed
   * first.
   *
   * <p>Recommended priority ranges:
   *
   * <ul>
   *   <li>100+ - Core infrastructure (basic setters, core methods, critical annotations)
   *   <li>70-99 - Standard features (optional helpers, consumer methods)
   *   <li>40-69 - Convenience features (varargs, suppliers)
   *   <li>10-39 - Optional enhancements (collection helpers, documentation)
   * </ul>
   *
   * @return the priority value (higher values execute first)
   */
  int getPriority();
}
