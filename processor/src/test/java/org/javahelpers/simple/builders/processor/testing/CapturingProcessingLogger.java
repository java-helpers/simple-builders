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

package org.javahelpers.simple.builders.processor.testing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Test helper for creating a {@link ProcessingLogger} that captures all emitted messages into a
 * list for assertion.
 *
 * <p>This avoids duplicating {@code CapturingMessager} and {@code createLogger} boilerplate across
 * test classes. Usage:
 *
 * <pre>{@code
 * CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
 * ProcessingLogger logger = capturing.logger();
 * // ... call code that logs ...
 * assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("expected")));
 * }</pre>
 */
public final class CapturingProcessingLogger {

  private final List<String> messages;
  private final ProcessingLogger logger;

  private CapturingProcessingLogger(List<String> messages, ProcessingLogger logger) {
    this.messages = messages;
    this.logger = logger;
  }

  /**
   * Creates a new capturing logger with an empty message list.
   *
   * @return a new capturing logger instance
   */
  public static CapturingProcessingLogger create() {
    List<String> messages = new ArrayList<>();
    ProcessingLogger logger =
        new ProcessingLogger(createCapturingEnvironment(messages, Collections.emptyMap()));
    return new CapturingProcessingLogger(messages, logger);
  }

  /**
   * Creates a new capturing logger with debug logging enabled via {@code
   * simplebuilder.verbose=true}.
   *
   * @return a new debug-enabled capturing logger instance
   */
  public static CapturingProcessingLogger createDebugEnabled() {
    List<String> messages = new ArrayList<>();
    ProcessingLogger logger =
        new ProcessingLogger(
            createCapturingEnvironment(messages, Map.of("simplebuilder.verbose", "true")));
    return new CapturingProcessingLogger(messages, logger);
  }

  /**
   * Returns the captured messages.
   *
   * @return unmodifiable view of the captured messages
   */
  public List<String> messages() {
    return Collections.unmodifiableList(messages);
  }

  /**
   * Returns the {@link ProcessingLogger} to pass to production code.
   *
   * @return the processing logger
   */
  public ProcessingLogger logger() {
    return logger;
  }

  /**
   * Asserts that the given exact message was captured. Extra messages and ordering are ignored.
   *
   * @param expectedMessage the full expected message including the diagnostic kind prefix
   */
  public void assertMessage(String expectedMessage) {
    assertTrue(
        messages.contains(expectedMessage),
        () ->
            "Expected captured message not found: "
                + expectedMessage
                + "\nActual messages: "
                + messages);
  }

  /** Creates a ProcessingEnvironment whose Messager captures all messages into the list. */
  private static ProcessingEnvironment createCapturingEnvironment(
      List<String> messages, Map<String, String> options) {
    return new ProcessingEnvironment() {
      @Override
      public Map<String, String> getOptions() {
        return options;
      }

      @Override
      public Messager getMessager() {
        return new CapturingMessager(messages);
      }

      @Override
      public Filer getFiler() {
        return null;
      }

      @Override
      public Elements getElementUtils() {
        return null;
      }

      @Override
      public Types getTypeUtils() {
        return null;
      }

      @Override
      public SourceVersion getSourceVersion() {
        return SourceVersion.latest();
      }

      @Override
      public Locale getLocale() {
        return Locale.getDefault();
      }
    };
  }

  /** Messager that captures all messages into a list. */
  private static final class CapturingMessager implements Messager {
    private final List<String> messages;

    CapturingMessager(List<String> messages) {
      this.messages = messages;
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
      messages.add(kind + ": " + msg);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
      messages.add(kind + ": " + msg);
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
      messages.add(kind + ": " + msg);
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
      messages.add(kind + ": " + msg);
    }
  }
}
