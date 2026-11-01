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

package org.javahelpers.codegen.processing.logging;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.javahelpers.codegen.logging.SinkProcessingLogger;

/**
 * {@link org.javahelpers.codegen.logging.ProcessingLogger} for all messages during annotation
 * processing. Providing util-functions for posting messages easily. Using a {@code
 * javax.annotation.processing.Messager} inside to publish the messages.
 */
public class MessagerProcessingLogger extends SinkProcessingLogger {

  /** Util class for exposing messages of type {@code javax.annotation.processing.Messager}. */
  private final Messager messager;

  /**
   * Creates a new MessagerProcessingLogger with the specified ProcessingEnvironment. The Messager
   * is used to report errors, warnings, and other notices during annotation processing. To enable
   * debug messages use {@link #createVerbose(ProcessingEnvironment)} instead.
   *
   * @param processingEnv the processing environment providing the messager
   * @return a processing logger reporting errors, warnings and info messages
   */
  public static MessagerProcessingLogger create(ProcessingEnvironment processingEnv) {
    return new MessagerProcessingLogger(processingEnv, false, null);
  }

  /**
   * Creates a new MessagerProcessingLogger carrying a diagnostic prefix.
   *
   * @param processingEnv the processing environment providing the messager
   * @param loggingPrefix the processor's diagnostic prefix, or {@code null}
   * @return a processing logger reporting errors, warnings and info messages
   */
  public static MessagerProcessingLogger create(
      ProcessingEnvironment processingEnv, String loggingPrefix) {
    return new MessagerProcessingLogger(processingEnv, false, loggingPrefix);
  }

  /**
   * Creates a new MessagerProcessingLogger that additionally reports debug messages.
   *
   * @param processingEnv the processing environment providing the messager
   * @return a debug-enabled processing logger
   */
  public static MessagerProcessingLogger createVerbose(ProcessingEnvironment processingEnv) {
    return new MessagerProcessingLogger(processingEnv, true, null);
  }

  /**
   * Creates a new debug-enabled MessagerProcessingLogger carrying a diagnostic prefix.
   *
   * @param processingEnv the processing environment providing the messager
   * @param loggingPrefix the processor's diagnostic prefix, or {@code null}
   * @return a debug-enabled processing logger
   */
  public static MessagerProcessingLogger createVerbose(
      ProcessingEnvironment processingEnv, String loggingPrefix) {
    return new MessagerProcessingLogger(processingEnv, true, loggingPrefix);
  }

  private MessagerProcessingLogger(
      ProcessingEnvironment processingEnv, boolean verbose, String loggingPrefix) {
    super(
        (severity, message) ->
            processingEnv
                .getMessager()
                .printMessage(Diagnostic.Kind.valueOf(severity.name()), message),
        verbose,
        loggingPrefix);
    this.messager = processingEnv.getMessager();
  }

  /**
   * Reports an error at the location of the given element with a formatted message.
   *
   * @param e the element where the error occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void error(Element e, String format, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(prefixed(format), args), e);
  }

  /**
   * Reports a warning at the location of the given element with a formatted message.
   *
   * @param e the element where the warning occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void warning(Element e, String format, Object... args) {
    String message = String.format(format, args);
    if (verbose) {
      // When debug is enabled, add spaces to align with [DEBUG] prefix (which is 5 characters
      // longer)
      String indentedMessage = formatHierarchicalMessage(message, 5); // Add 5 spaces for alignment
      messager.printMessage(Diagnostic.Kind.WARNING, indentedMessage, e);
    } else {
      // When debug is disabled, print flat
      messager.printMessage(Diagnostic.Kind.WARNING, prefixed(message), e);
    }
  }
}
