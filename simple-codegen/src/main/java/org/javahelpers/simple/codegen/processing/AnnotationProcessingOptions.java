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

package org.javahelpers.simple.codegen.processing;

import java.util.List;
import java.util.Map;
import org.javahelpers.simple.codegen.logging.PhaseHierarchy;

/**
 * Static options a concrete annotation-processing context supplies to {@link
 * AnnotationProcessingContext} at construction. How each option is derived (compiler arguments,
 * annotation configuration, ...) is the concrete context's responsibility.
 *
 * @param strictModeEnabled whether strict/fail-fast mode is enabled: strict mode reports problems
 *     as errors, otherwise as warnings
 * @param performanceTrackingEnabled whether pipeline phase timing is collected
 * @param performanceOutputFile file the performance report is written to
 * @param phaseHierarchy the phase tree the performance report renders with
 */
public record AnnotationProcessingOptions(
    boolean strictModeEnabled,
    boolean performanceTrackingEnabled,
    String performanceOutputFile,
    PhaseHierarchy phaseHierarchy) {

  /**
   * Creates a builder for {@link AnnotationProcessingOptions}. Defaults: strict mode enabled,
   * performance tracking disabled, no output file, empty phase hierarchy.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link AnnotationProcessingOptions}. */
  public static final class Builder {

    private boolean strictModeEnabled = true;
    private boolean performanceTrackingEnabled;
    private String performanceOutputFile;
    private PhaseHierarchy phaseHierarchy = new PhaseHierarchy(List.of(), Map.of());

    private Builder() {}

    /**
     * Sets whether strict/fail-fast mode is enabled.
     *
     * @param strictModeEnabled {@code true} to report problems as errors instead of warnings
     * @return this builder
     */
    public Builder strictModeEnabled(boolean strictModeEnabled) {
      this.strictModeEnabled = strictModeEnabled;
      return this;
    }

    /**
     * Sets whether pipeline phase timing is collected.
     *
     * @param performanceTrackingEnabled {@code true} to enable performance tracking
     * @return this builder
     */
    public Builder performanceTrackingEnabled(boolean performanceTrackingEnabled) {
      this.performanceTrackingEnabled = performanceTrackingEnabled;
      return this;
    }

    /**
     * Sets the file the performance report is written to.
     *
     * @param performanceOutputFile path of the report output file
     * @return this builder
     */
    public Builder performanceOutputFile(String performanceOutputFile) {
      this.performanceOutputFile = performanceOutputFile;
      return this;
    }

    /**
     * Sets the phase tree the performance report renders with.
     *
     * @param phaseHierarchy top-level phases in display order and their sub-phases
     * @return this builder
     */
    public Builder phaseHierarchy(PhaseHierarchy phaseHierarchy) {
      this.phaseHierarchy = phaseHierarchy;
      return this;
    }

    /**
     * Builds the options.
     *
     * @return the configured {@link AnnotationProcessingOptions}
     */
    public AnnotationProcessingOptions build() {
      return new AnnotationProcessingOptions(
          strictModeEnabled, performanceTrackingEnabled, performanceOutputFile, phaseHierarchy);
    }
  }
}
