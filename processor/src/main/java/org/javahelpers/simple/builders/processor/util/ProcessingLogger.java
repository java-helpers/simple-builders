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

package org.javahelpers.simple.builders.processor.util;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Logger for all messages during annotation processing. Providing util-functions for posting
 * messages easily. Using a {@code javax.annotation.processing.Messager} inside to publish the
 * messages.
 */
public class ProcessingLogger {
  /** Util class for exposing messages of type {@code javax.annotation.processing.Messager}. */
  private final Messager messager;

  /** Flag indicating if debug logging is enabled. */
  private final boolean debugEnabled;

  /**
   * Constructs a new ProcessingLogger with the specified ProcessingEnvironment. The Messager is
   * used to report errors, warnings, and other notices during annotation processing. Debug logging
   * is enabled by setting the compiler argument: -Averbose=true
   *
   * @param processingEnv the processing environment providing messager and options
   */
  public ProcessingLogger(ProcessingEnvironment processingEnv) {
    this.messager = processingEnv.getMessager();
    this.debugEnabled = "true".equalsIgnoreCase(processingEnv.getOptions().get("verbose"));
  }

  /**
   * Reports an error with a formatted message.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void error(String format, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
  }

  /**
   * Posts an info-level message (NOTE level in Maven). Used for important status information about
   * builder generation.
   *
   * @param message the info message to be posted
   */
  public void info(String message) {
    messager.printMessage(Diagnostic.Kind.NOTE, message);
  }

  /**
   * Posts an info-level message with a formatted string.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void info(String format, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
  }

  /**
   * Posts a debug message with OTHER level. Used for detailed tracing of the builder generation
   * process. Only visible when enabled via -Averbose=true compiler argument.
   *
   * @param message the debug message to be posted
   */
  public void debug(String message) {
    if (debugEnabled) {
      String formatWithDebug = "[DEBUG] " + message;
      messager.printMessage(Diagnostic.Kind.OTHER, formatWithDebug);
    }
  }

  /**
   * Posts a debug message with a formatted string. Only visible when enabled via -Averbose=true.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void debug(String format, Object... args) {
    if (debugEnabled) {
      String formatWithDebug = "[DEBUG] " + format;
      messager.printMessage(Diagnostic.Kind.OTHER, String.format(formatWithDebug, args));
    }
  }

  /**
   * Reports a warning at the location of the given element with a formatted message.
   *
   * @param e the element where the warning occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void warning(Element e, String format, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(format, args), e);
  }
}
