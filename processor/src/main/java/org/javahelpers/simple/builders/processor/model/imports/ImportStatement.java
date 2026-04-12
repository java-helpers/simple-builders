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

package org.javahelpers.simple.builders.processor.model.imports;

/**
 * Represents an import statement in generated code.
 *
 * <p>This is the base interface for all import types (regular and static imports). This is a pure
 * data holder - business logic for filtering imports belongs in ImportCollector.
 */
public interface ImportStatement {

  /**
   * Returns the fully qualified name for this import.
   *
   * <p>For regular imports: {@code com.example.MyClass}
   *
   * <p>For static imports: {@code com.example.MyClass.staticMethod}
   *
   * @return the fully qualified name
   */
  String getFullyQualifiedName();

  /**
   * Returns the package name for this import.
   *
   * <p>Extracted from the fully qualified name by taking everything before the last dot.
   *
   * @return the package name, or empty string if no package
   */
  String getPackageName();

  /**
   * Checks if this is a static import.
   *
   * @return true if this is a static import, false for regular imports
   */
  boolean isStatic();
}
