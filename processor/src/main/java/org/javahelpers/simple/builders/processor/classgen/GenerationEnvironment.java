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

package org.javahelpers.simple.builders.processor.classgen;

import javax.annotation.processing.ProcessingEnvironment;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Environment handed to a {@link ClassCodeGenerator}: the annotation processing environment, the
 * logger, the performance tracker and the reporter for generation problems.
 */
public class GenerationEnvironment {

  private final ProcessingEnvironment processingEnvironment;
  private final ProcessingLogger logger;
  private final PerformanceTracker performanceTracker;
  private final GenerationIssueReporter issueReporter;

  /**
   * Creates a new generation environment.
   *
   * @param processingEnvironment the annotation processing environment
   * @param logger the logger for debug output during code generation
   * @param performanceTracker the tracker for sub-phase timing
   * @param issueReporter the reporter for problems found during generation; the reporter decides
   *     the severity
   */
  public GenerationEnvironment(
      ProcessingEnvironment processingEnvironment,
      ProcessingLogger logger,
      PerformanceTracker performanceTracker,
      GenerationIssueReporter issueReporter) {
    this.processingEnvironment = processingEnvironment;
    this.logger = logger;
    this.performanceTracker = performanceTracker;
    this.issueReporter = issueReporter;
  }

  /**
   * Returns the annotation processing environment.
   *
   * @return the processing environment
   */
  public ProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironment;
  }

  /**
   * Returns the logger for debug output during code generation.
   *
   * @return the logger
   */
  public ProcessingLogger getLogger() {
    return logger;
  }

  /**
   * Returns the performance tracker for sub-phase timing.
   *
   * @return the tracker
   */
  public PerformanceTracker getPerformanceTracker() {
    return performanceTracker;
  }

  /**
   * Returns the reporter for problems found during generation.
   *
   * @return the issue reporter
   */
  public GenerationIssueReporter getIssueReporter() {
    return issueReporter;
  }
}
