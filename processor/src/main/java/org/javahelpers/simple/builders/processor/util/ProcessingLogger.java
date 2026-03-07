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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.javahelpers.simple.builders.processor.enums.CompilerArgumentsEnum;

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

  /** Thread-local indentation level for hierarchical logging. */
  private static final ThreadLocal<Integer> indentationLevel = ThreadLocal.withInitial(() -> 0);

  /** Thread-local operation context for hierarchical logging. */
  private static final ThreadLocal<StringBuilder> operationContext =
      ThreadLocal.withInitial(StringBuilder::new);

  /** Thread-local tracking of first operation at each indentation level. */
  private static final ThreadLocal<Map<Integer, Boolean>> firstOperationAtLevel =
      ThreadLocal.withInitial(HashMap::new);

  /**
   * Constructs a new ProcessingLogger with the specified ProcessingEnvironment. The Messager is
   * used to report errors, warnings, and other notices during annotation processing. Debug logging
   * is enabled by setting the compiler argument: -Averbose=true or -Asimplebuilder.verbose=true
   *
   * @param processingEnv the processing environment providing messager and options
   */
  public ProcessingLogger(ProcessingEnvironment processingEnv) {
    // Reset ThreadLocal state to ensure clean state between test runs
    resetThreadLocalState();

    this.messager = processingEnv.getMessager();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(processingEnv);
    this.debugEnabled = reader.readBooleanValue(CompilerArgumentsEnum.VERBOSE);
  }

  /** Resets ThreadLocal state to ensure clean state between test runs. */
  private void resetThreadLocalState() {
    indentationLevel.set(0);
    operationContext.set(new StringBuilder());
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
   * process. Only visible when enabled via -Averbose=true or -Asimplebuilder.verbose=true compiler
   * argument.
   *
   * @param message the debug message to be posted
   */
  public void debug(String message) {
    if (debugEnabled) {
      String indentedMessage = formatWithIndentation(message);
      messager.printMessage(Diagnostic.Kind.OTHER, indentedMessage);
    }
  }

  /**
   * Posts a debug message with a formatted string. Only visible when enabled via -Averbose=true or
   * -Asimplebuilder.verbose=true.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void debug(String format, Object... args) {
    if (debugEnabled) {
      String message = String.format(format, args);
      String indentedMessage = formatWithIndentation(message);
      messager.printMessage(Diagnostic.Kind.OTHER, indentedMessage);
    }
  }

  /**
   * Posts a warning message with a formatted string. Warnings are displayed by default and indicate
   * potential issues that don't prevent compilation.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void warning(String format, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(format, args));
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
    String indentedMessage = formatWithIndentation(message);
    messager.printMessage(Diagnostic.Kind.WARNING, indentedMessage, e);
  }

  /**
   * Formats a message with appropriate indentation based on current context.
   *
   * @param message the message to format
   * @return the formatted message with indentation
   */
  private String formatWithIndentation(String message) {
    int level = indentationLevel.get();
    if (level == 0) {
      return "[DEBUG] " + message;
    }

    // Use │ characters with proper spacing for better visual connection between hierarchical levels
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < level; i++) {
      indent.append("│ ");
    }
    // Add ├─ for debug messages inside operations
    indent.append("├─ ");
    return "[DEBUG] " + indent + message;
  }

  private String formatOperationMessage(String message) {
    int level = indentationLevel.get();
    if (level == 0) {
      return "[DEBUG] " + message;
    }

    // Use │ characters with proper spacing for operation messages (no ├─ prefix)
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < level; i++) {
      indent.append("│ ");
    }
    return "[DEBUG] " + indent + message;
  }

  /**
   * Starts a new hierarchical operation context, increasing indentation for subsequent debug
   * messages. This should be called before starting a major operation that has sub-operations.
   *
   * @param operationName the name of the operation being started
   */
  public void startOperation(String formatClosingMessage, Object... args) {
    int currentLevel = indentationLevel.get();
    Map<Integer, Boolean> firstOps = firstOperationAtLevel.get();

    // Mark that we've had an operation at this level
    firstOps.put(currentLevel, false);

    StringBuilder context = operationContext.get();
    context.append("├─ ").append(String.format(formatClosingMessage, args));

    // Log the operation directly without going through debug() to avoid extra indentation
    String operationMessage = formatOperationMessage(context.toString());
    messager.printMessage(Diagnostic.Kind.NOTE, operationMessage);

    indentationLevel.set(currentLevel + 1);
    context.setLength(0); // Reset for next operation
  }

  /**
   * Ends the current hierarchical operation context, decreasing indentation. This should be called
   * after completing an operation started with startOperation.
   */
  public void endOperation() {
    int currentLevel = indentationLevel.get();
    if (currentLevel > 0) {
      indentationLevel.set(currentLevel - 1);
      // No need to log "Operation completed" - it's redundant and adds noise
    }
  }

  /**
   * Ends the current hierarchical operation context with a closing message, decreasing indentation.
   * This should be called after completing an operation started with startOperation when you want
   * to log a closing message with the proper tree structure (using └─ for the last operation).
   *
   * @param closingMessage the message to log for the operation completion
   */
  public void endOperation(String formatClosingMessage, Object... args) {
    int currentLevel = indentationLevel.get();

    // Log the closing message with └─ to indicate it's the last operation at this level
    StringBuilder context = operationContext.get();
    context.append("└─ ").append(String.format(formatClosingMessage, args));

    // Log the operation directly without going through debug() to avoid extra indentation
    String operationMessage = formatOperationMessage(context.toString());
    messager.printMessage(Diagnostic.Kind.NOTE, operationMessage);

    context.setLength(0); // Reset for next operation
    if (currentLevel > 0) {
      indentationLevel.set(currentLevel - 1);
    }
  }

  /**
   * Logs a closing operation message with └─ character without changing indentation level. This is
   * useful for logging the completion of an operation while staying at the same indentation level.
   *
   * @param closingMessage the message to log for the operation completion
   */
  public void logClosingOperation(String closingMessage) {
    // Log the closing message with └─ to indicate it's the last operation at this level
    StringBuilder context = operationContext.get();
    context.append("└─ ").append(closingMessage);

    // Log the operation directly without going through debug() to avoid extra indentation
    // Use formatOperationMessage instead of formatWithIndentation to avoid double ├─
    String operationMessage = formatOperationMessage(context.toString());
    messager.printMessage(Diagnostic.Kind.NOTE, operationMessage);

    context.setLength(0); // Reset for next operation
  }

  /**
   * Resets the indentation level to zero to prevent cascading errors between processing runs. This
   * should be called at the end of each processing round.
   */
  public void resetIndentation() {
    indentationLevel.set(0);
    operationContext.get().setLength(0);
    firstOperationAtLevel.get().clear(); // Reset first operation tracking
  }

  /** Gets the current indentation level for debugging purposes. */
  public int getCurrentIndentationLevel() {
    return indentationLevel.get();
  }

  /**
   * Gets the current operation context for debugging purposes.
   *
   * @return the current operation context string
   */
  public String getCurrentOperationContext() {
    return operationContext.get().toString();
  }
}
