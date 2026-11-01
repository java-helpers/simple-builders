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

package org.javahelpers.codegen;

import java.io.IOException;
import org.javahelpers.codegen.logging.PerformanceTracker;
import org.javahelpers.codegen.logging.ProcessingLogger;

/**
 * Environment handed to a {@link ClassCodeGenerator}: the logger, the performance tracker, the
 * reporter for generation problems and the sink for generated source files. Implementations are
 * bound to the host that runs the generation — e.g. an annotation processing environment.
 */
public interface GenerationEnvironment {

  /**
   * Returns the logger for debug output during code generation.
   *
   * @return the logger
   */
  ProcessingLogger getLogger();

  /**
   * Returns the performance tracker for sub-phase timing.
   *
   * @return the tracker
   */
  PerformanceTracker getPerformanceTracker();

  /**
   * Returns the reporter for problems found during generation.
   *
   * @return the issue reporter
   */
  GenerationIssueReporter getIssueReporter();

  /**
   * Creates the target file for a generated type.
   *
   * @param qualifiedName the qualified name of the type being generated
   * @return the created source file
   * @throws IOException if the file cannot be created
   */
  GeneratedSourceFile createSourceFile(CharSequence qualifiedName) throws IOException;
}
