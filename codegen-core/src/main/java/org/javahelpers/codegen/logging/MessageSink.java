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

/**
 * Destination for the messages a {@link SinkProcessingLogger} emits. Binds the host-independent
 * logging contract to a concrete output — e.g. a {@code javax.annotation.processing.Messager} in an
 * annotation processing environment, or a test capture list.
 */
@FunctionalInterface
public interface MessageSink {

  /**
   * Severity of an emitted message. Names mirror {@code javax.tools.Diagnostic.Kind} so a
   * host-bound sink can map them one-to-one.
   */
  enum Severity {
    /** Fatal problem for the compilation. */
    ERROR,
    /** Potential issue that doesn't prevent compilation. */
    WARNING,
    /** Informational status message. */
    NOTE,
    /** Debug output. */
    OTHER
  }

  /**
   * Emits a message.
   *
   * @param severity the message severity
   * @param message the formatted message
   */
  void publish(Severity severity, String message);
}
