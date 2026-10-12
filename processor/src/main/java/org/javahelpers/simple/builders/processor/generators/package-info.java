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

/**
 * Package org.javahelpers.simple.builders.processor.generators
 *
 * <p>Generator framework for the simple-builders processor.
 *
 * <p>This package contains the core generator interfaces:
 *
 * <ul>
 *   <li>{@link Generator} - Base sealed interface for all generators
 *   <li>{@link MethodGenerator} - Interface for field-level method generators
 *   <li>{@link BuilderEnhancer} - Interface for builder-level enhancers
 * </ul>
 *
 * <p>Generators are organized into subpackages by their scope:
 *
 * <ul>
 *   <li>field - Generators that create methods for individual fields
 *   <li>builder - Enhancers that modify the entire builder
 *   <li>util - Utility generators for common patterns
 *   <li>registry - Generator infrastructure and management
 *   <li>integration - Framework integration generators
 * </ul>
 */
package org.javahelpers.simple.builders.processor.generators;
