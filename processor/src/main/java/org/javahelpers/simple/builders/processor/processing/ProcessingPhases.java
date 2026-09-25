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

package org.javahelpers.simple.builders.processor.processing;

import static org.javahelpers.simple.builders.processor.classgen.GenerationPhases.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase names of the builder processing pipeline, reported through {@code
 * PerformanceTracker.endPhase}, plus the phase hierarchy an {@code ActivePerformanceTracker} needs
 * to render its report.
 */
public final class ProcessingPhases {

  // Top-level phases
  public static final String PHASE_ELEMENT_COLLECTION = "Element Collection";
  public static final String PHASE_CONFIGURATION_RESOLUTION = "Configuration Resolution";
  public static final String PHASE_BUILDER_DEFINITION_EXTRACTION = "Builder Definition Extraction";
  public static final String PHASE_DTO_MAPPING = "DTO Mapping";
  public static final String PHASE_CODE_GENERATION = "Code Generation";

  /** Top-level phases of the pipeline, in report display order. */
  public static final List<String> TOP_LEVEL_PHASES =
      List.of(
          PHASE_ELEMENT_COLLECTION,
          PHASE_CONFIGURATION_RESOLUTION,
          PHASE_BUILDER_DEFINITION_EXTRACTION,
          PHASE_DTO_MAPPING,
          PHASE_CODE_GENERATION);

  /** Phase hierarchy for report display: parent phase to ordered child phases. */
  public static final Map<String, List<String>> PHASE_CHILDREN = phaseChildren();

  private static Map<String, List<String>> phaseChildren() {
    Map<String, List<String>> children = new LinkedHashMap<>();
    children.put(PHASE_CODE_GENERATION, List.of(PHASE_SOURCE_CONSTRUCTION, PHASE_FILE_WRITING));
    children.put(
        PHASE_SOURCE_CONSTRUCTION,
        List.of(PHASE_ELEMENT_BUILDING, PHASE_STRING_GENERATION, PHASE_FORMATTING));
    children.put(
        PHASE_ELEMENT_BUILDING,
        List.of(
            PHASE_CLASS_CREATION,
            PHASE_CLASS_METADATA,
            PHASE_FIELDS,
            PHASE_CONSTRUCTORS,
            PHASE_METHODS,
            PHASE_NESTED_TYPES,
            PHASE_CLASS_ANNOTATIONS));
    return children;
  }

  private ProcessingPhases() {}
}
