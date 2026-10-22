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

package org.javahelpers.simple.builders.processor.processing.logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The phase vocabulary a {@link PerformanceTracker} report is rendered as a tree: the top-level
 * phases in display order, plus for each parent phase the ordered sub-phases nested underneath.
 * Phases absent from {@code subPhases} are leaves.
 *
 * <p>The phase names themselves are consumer-defined — this type carries whichever pipeline
 * vocabulary the caller tracks with, keeping the tracker independent of any concrete phase set.
 *
 * @param topLevelPhases top-level phases in display order
 * @param subPhases parent phase name to its ordered sub-phase names
 */
public record PhaseHierarchy(List<String> topLevelPhases, Map<String, List<String>> subPhases) {

  /** Defensive copies: hierarchy structure must not change after tracker creation. */
  public PhaseHierarchy {
    topLevelPhases = List.copyOf(topLevelPhases);
    Map<String, List<String>> copied = new LinkedHashMap<>();
    subPhases.forEach((parent, children) -> copied.put(parent, List.copyOf(children)));
    subPhases = Map.copyOf(copied);
  }
}
