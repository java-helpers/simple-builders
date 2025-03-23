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

package org.javahelpers.simple.builders.internal;

import javax.annotation.processing.Messager;
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

  /**
   * Constructor for ProcessingLogger with instance of messager.
   *
   * @param messager
   */
  public ProcessingLogger(Messager messager) {
    this.messager = messager;
  }

  /**
   * Posting an error with elment and message.
   *
   * @param e element on which the error happened
   * @param message message to be posted to messager
   */
  public void error(Element e, String message) {
    messager.printMessage(Diagnostic.Kind.ERROR, message, e);
  }

  /**
   * Posting a simple information message.
   *
   * @param message message to be posted to messager
   */
  public void log(String message) {
    messager.printMessage(Diagnostic.Kind.NOTE, message);
  }
}
