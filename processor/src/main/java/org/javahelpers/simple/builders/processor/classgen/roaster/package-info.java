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
 * Package org.javahelpers.simple.builders.processor.classgen.roaster
 *
 * <p>Roaster-based code generation for simple-builders processor.
 *
 * <p>This package contains the core code generation components:
 *
 * <ul>
 *   <li>{@link org.javahelpers.simple.builders.processor.classgen.roaster.RoasterCodeGenerator} -
 *       Main code generator that creates builder classes
 *   <li>{@link org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper} - Utility
 *       for mapping DTOs to Java source strings
 * </ul>
 *
 * <p>Exception classes are organized in separate packages:
 *
 * <ul>
 *   <li>{@link org.javahelpers.codegen.CodeGenerationException} -
 *       Exception for code generation errors
 *   <li>{@link
 *       org.javahelpers.simple.builders.processor.classgen.roaster.exceptions.RoasterMapperException}
 *       - Exception for mapping errors
 * </ul>
 */
package org.javahelpers.simple.builders.processor.classgen.roaster;
