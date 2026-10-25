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

package org.javahelpers.codegen.processing;

/**
 * The common configuration contract a code-generation processor's task configuration supports:
 * strict/fail-fast reporting and verbose diagnostics.
 *
 * <p>Framework defaults for new processors: strict enabled, verbose disabled (the javac {@code
 * -verbose} flag enables verbose output too). Domains may deviate — simple-builders keeps strict
 * opt-in.
 */
public interface CodegenConfiguration {

  /**
   * Whether generation problems are reported as errors (fail-fast) instead of warnings.
   *
   * @return {@code true} for strict reporting
   */
  boolean isStrictModeEnabled();

  /**
   * Whether verbose diagnostic output is enabled.
   *
   * @return {@code true} for verbose output
   */
  boolean isVerboseEnabled();
}
