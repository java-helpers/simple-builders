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

package org.javahelpers.simple.builders.processor.processing;

/**
 * A compiler option that can be read from the annotation processing environment.
 *
 * <p>An option has a bare option name (e.g. {@code "verbose"}) and a prefixed compiler argument
 * name (e.g. {@code "simplebuilder.verbose"}) which is looked up as {@code -A} compiler argument
 * and as {@code -D} JVM system property.
 */
public interface CompilerOption {

  /**
   * Returns the bare option name (e.g. {@code "verbose"}).
   *
   * @return the option name without prefix
   */
  String getOptionName();

  /**
   * Returns the prefixed compiler argument name (e.g. {@code "simplebuilder.verbose"}).
   *
   * @return the compiler argument name
   */
  String getCompilerArgument();
}
