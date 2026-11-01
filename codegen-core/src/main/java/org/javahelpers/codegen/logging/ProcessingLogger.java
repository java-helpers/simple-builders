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

package org.javahelpers.codegen.logging;

import javax.lang.model.element.Element;

/**
 * Logger for all messages during code generation. Every emitted message is prefixed with the
 * logger's diagnostic prefix ("{@code prefix: }") when one was supplied at creation. This interface
 * carries only the host-independent API — implementations bound to an annotation processing
 * environment may additionally offer element-attached reporting.
 */
public interface ProcessingLogger {

  /**
   * Reports an error with a formatted message.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  void error(String format, Object... args);

  /**
   * Reports an error at the location of the given element with a formatted message.
   *
   * @param e the element where the error occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  void error(Element e, String format, Object... args);

  /**
   * Posts an info-level message (NOTE level in Maven). Used for important status information about
   * the generation.
   *
   * @param message the info message to be posted
   */
  void info(String message);

  /**
   * Posts an info-level message with a formatted string.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  void info(String format, Object... args);

  /**
   * Posts a debug message. Only visible when debug output is enabled.
   *
   * @param message the debug message to be posted
   */
  void debug(String message);

  /**
   * Posts a debug message with a formatted string. Only visible when debug output is enabled.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  void debug(String format, Object... args);

  /**
   * Posts a warning message with a formatted string. Warnings are displayed by default and indicate
   * potential issues that don't prevent compilation.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  void warning(String format, Object... args);

  /**
   * Reports a warning at the location of the given element with a formatted message.
   *
   * @param e the element where the warning occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  void warning(Element e, String format, Object... args);

  /**
   * Starts a new hierarchical operation context, increasing indentation for subsequent debug
   * messages. This should be called before starting a major operation that has sub-operations.
   *
   * @param formatClosingMessage the format string for the operation message
   * @param args arguments referenced by the format specifiers
   */
  void debugStartOperation(String formatClosingMessage, Object... args);

  /**
   * Ends the current hierarchical operation context, decreasing indentation. This should be called
   * after completing an operation started with startOperation.
   */
  void debugEndOperation();

  /**
   * Ends the current hierarchical operation context with a closing message, decreasing indentation.
   * This should be called after completing an operation started with startOperation when you want
   * to log a closing message with the proper tree structure (using └─ for the last operation).
   *
   * @param formatClosingMessage the format string for the closing message
   * @param args arguments referenced by the format specifiers
   */
  void debugEndOperation(String formatClosingMessage, Object... args);

  /**
   * Resets the indentation level to zero to prevent cascading errors between processing runs. This
   * should be called at the end of each processing round.
   */
  void resetIndentation();

  /**
   * Gets the current indentation level for debugging purposes.
   *
   * @return the current indentation level
   */
  int getCurrentIndentationLevel();
}
