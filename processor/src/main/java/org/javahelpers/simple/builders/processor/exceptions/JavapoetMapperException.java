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

package org.javahelpers.simple.builders.processor.exceptions;

/** Special exception for errors in mapping to Javapoet classes. */
public class JavapoetMapperException extends RuntimeException {

  /**
   * Creating an exception with message and parameters.
   *
   * @param cause root cause of current exception, containing stacktrace
   */
  public JavapoetMapperException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  /**
   * Creating an exception with message and parameters.
   *
   * @param message A specific message, supports String.format arguments
   * @param args Arguments for Stringlformat on message
   */
  public JavapoetMapperException(String message, Object... args) {
    super(String.format(message, args));
  }

  /**
   * Creating an exception with message and parameters.
   *
   * @param cause root cause of current exception, containing stacktrace
   * @param message A specific message, supports String.format arguments
   * @param args Arguments for Stringlformat on message
   */
  public JavapoetMapperException(Throwable cause, String message, Object... args) {
    super(String.format(message, args), cause);
  }
}
