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
import org.javahelpers.codegen.logging.MessageSink.Severity;

/**
 * {@link ProcessingLogger} emitting formatted messages to a {@link MessageSink}. Every emitted
 * message is prefixed with the logger's diagnostic prefix ("{@code prefix: }") when one was
 * supplied at creation.
 */
public class SinkProcessingLogger implements ProcessingLogger {

  /** Destination of the emitted messages. */
  private final MessageSink sink;

  /** Whether debug messages are emitted. */
  protected final boolean verbose;

  /** Diagnostic prefix identifying the processor — every emitted message is prefixed with it. */
  private final String loggingPrefix;

  /** Thread-local indentation level for hierarchical logging. */
  private static final ThreadLocal<Integer> indentationLevel = ThreadLocal.withInitial(() -> 0);

  /**
   * Creates a logger emitting to the given sink.
   *
   * @param sink the destination for emitted messages
   * @param verbose whether debug messages are emitted
   * @param loggingPrefix the processor's diagnostic prefix, or {@code null}
   */
  public SinkProcessingLogger(MessageSink sink, boolean verbose, String loggingPrefix) {
    // Reset ThreadLocal state to ensure clean state between test runs
    resetThreadLocalState();

    this.sink = sink;
    this.verbose = verbose;
    this.loggingPrefix = loggingPrefix;
  }

  /**
   * Applies this logger's "{@code prefix: }" prefix to a message — the one place the prefix pattern
   * is defined. Loggers without a prefix return the message unchanged.
   *
   * @param message the message or message format
   * @return the message prefixed with this logger's prefix
   */
  protected String prefixed(String message) {
    return loggingPrefix != null ? loggingPrefix + ": " + message : message;
  }

  /** Resets ThreadLocal state to ensure clean state between test runs. */
  private void resetThreadLocalState() {
    indentationLevel.set(0);
  }

  @Override
  public void error(String format, Object... args) {
    sink.publish(Severity.ERROR, String.format(prefixed(format), args));
  }

  /**
   * {@inheritDoc}
   *
   * <p>The sink does not carry element location — the element is ignored.
   */
  @Override
  public void error(Element e, String format, Object... args) {
    error(format, args);
  }

  @Override
  public void info(String message) {
    sink.publish(Severity.NOTE, prefixed(message));
  }

  /**
   * Formats a message with hierarchical indentation and proper spacing for alignment. Used by info
   * and warning methods when debug mode is enabled.
   *
   * @param message the message to format
   * @param spaces the number of spaces to add before the hierarchy for alignment
   * @return the formatted message with proper indentation and spacing
   */
  protected String formatHierarchicalMessage(String message, int spaces) {
    String prefix = getCurrentIndentationLevel() > 0 ? "└─ " : "";
    String spacing = " ".repeat(spaces);
    return spacing + prefixed(formatWithIndentationNoDebug(message, prefix));
  }

  @Override
  public void info(String format, Object... args) {
    String message = String.format(format, args);
    if (verbose) {
      // When debug is enabled, add spaces to align with [DEBUG] prefix (which is 6 characters
      // longer)
      String indentedMessage = formatHierarchicalMessage(message, 8); // Add 8 spaces for alignment
      sink.publish(Severity.NOTE, indentedMessage);
    } else {
      // When debug is disabled, use flat formatting (current behavior)
      sink.publish(Severity.NOTE, prefixed(message));
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>The sink does not carry element location — the element is ignored.
   */
  @Override
  public void warning(Element e, String format, Object... args) {
    warning(format, args);
  }

  @Override
  public void debug(String message) {
    if (verbose) {
      String indentedMessage = formatWithIndentation(message);
      sink.publish(Severity.OTHER, indentedMessage);
    }
  }

  @Override
  public void debug(String format, Object... args) {
    if (verbose) {
      String message = String.format(format, args);
      String indentedMessage = formatWithIndentation(message);
      sink.publish(Severity.OTHER, indentedMessage);
    }
  }

  @Override
  public void warning(String format, Object... args) {
    String message = String.format(format, args);
    if (verbose) {
      // When debug is enabled, add spaces to align with [DEBUG] prefix (which is 5 characters
      // longer)
      String indentedMessage = formatHierarchicalMessage(message, 5); // Add 5 spaces for alignment
      sink.publish(Severity.WARNING, indentedMessage);
    } else {
      // When debug is disabled, print flat
      sink.publish(Severity.WARNING, prefixed(message));
    }
  }

  /**
   * Formats a message with appropriate indentation based on current context.
   *
   * @param message the message to format
   * @param prefix the prefix to add (e.g., "├─ ", "└─ ") or empty string for no prefix
   * @return the formatted message with indentation
   */
  protected String formatWithIndentation(String message, String prefix) {
    int level = indentationLevel.get();
    if (level == 0) {
      // At level 0, no indentation or prefix
      return "[DEBUG] " + prefixed(message);
    }

    // Use │ characters with proper spacing for better visual connection between hierarchical levels
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < level - 1; i++) {
      indent.append("│  ");
    }
    // Add the specified prefix
    indent.append(prefix);
    return "[DEBUG] " + prefixed(indent.toString() + message);
  }

  /**
   * Formats a message with indentation but without [DEBUG] prefix for INFO/WARNING messages.
   *
   * @param message the message to format
   * @param prefix the prefix to add (e.g., "├─ ", "└─ ") or empty string for no prefix
   * @return the formatted message with indentation but without [DEBUG] prefix
   */
  private String formatWithIndentationNoDebug(String message, String prefix) {
    int level = indentationLevel.get();
    if (level == 0) {
      // At level 0, no indentation or prefix
      return message;
    }

    // Use │ characters with proper spacing for better visual connection between hierarchical levels
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < level - 1; i++) {
      indent.append("│  ");
    }
    // Add the specified prefix
    indent.append(prefix);
    return indent + message;
  }

  /**
   * Formats a message with ├─ prefix for debug messages.
   *
   * @param message the message to format
   * @return the formatted message with indentation and ├─ prefix
   */
  protected String formatWithIndentation(String message) {
    return formatWithIndentation(message, "├─ ");
  }

  @Override
  public void debugStartOperation(String formatClosingMessage, Object... args) {
    int currentLevel = indentationLevel.get();

    // Log the operation start message with proper prefix handling
    if (verbose) {
      String operationMessage = formatWithIndentation(String.format(formatClosingMessage, args));
      sink.publish(Severity.NOTE, operationMessage);
    }

    // Increase indentation for subsequent messages
    indentationLevel.set(currentLevel + 1);
  }

  @Override
  public void debugEndOperation() {
    int currentLevel = indentationLevel.get();
    if (currentLevel > 0) {
      indentationLevel.set(currentLevel - 1);
      // No need to log "Operation completed" - it's redundant and adds noise
    }
  }

  @Override
  public void debugEndOperation(String formatClosingMessage, Object... args) {
    int currentLevel = indentationLevel.get();

    // Log the closing message with └─ to indicate it's the last operation at this level
    if (verbose) {
      String closingMessage = String.format(formatClosingMessage, args);
      String operationMessage = formatWithIndentation(closingMessage, "└─ ");
      sink.publish(Severity.NOTE, operationMessage);
    }

    // Decrease indentation level
    if (currentLevel > 0) {
      indentationLevel.set(currentLevel - 1);
    }
  }

  @Override
  public void resetIndentation() {
    indentationLevel.set(0);
    // Clean up ThreadLocal to prevent memory leaks
    indentationLevel.remove();
  }

  /** Gets the current indentation level for debugging purposes. */
  @Override
  public int getCurrentIndentationLevel() {
    return indentationLevel.get();
  }
}
