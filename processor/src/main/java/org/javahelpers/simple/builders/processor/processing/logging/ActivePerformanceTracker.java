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

package org.javahelpers.simple.builders.processor.processing.logging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Active implementation of {@link PerformanceTracker} that measures execution times using {@link
 * System#nanoTime()} and aggregates results for a summary report.
 *
 * <p>This tracker maintains:
 *
 * <ul>
 *   <li>Per-phase total time with a hardcoded hierarchy for display:
 *       <pre>
 *       Configuration Resolution
 *       Builder Definition Extraction
 *       DTO Mapping
 *       Code Generation
 *       ├─ Source Construction
 *       │  ├─ Element Building
 *       │  │  ├─ Class Creation
 *       │  │  ├─ Class Metadata
 *       │  │  ├─ Fields
 *       │  │  ├─ Constructors
 *       │  │  ├─ Methods
 *       │  │  ├─ Nested Types
 *       │  │  └─ Class Annotations
 *       │  ├─ String Generation
 *       │  └─ Formatting
 *       └─ File Writing
 *       </pre>
 *       Percentages are calculated relative to the parent phase.
 *   <li>Per-generator total time and call count (for MethodGenerators)
 *   <li>Per-enhancer total time and call count (for BuilderEnhancers)
 *   <li>Per-class total time with field count and collection count
 * </ul>
 */
public final class ActivePerformanceTracker implements PerformanceTracker {

  /** Hardcoded phase hierarchy for report display. Order defines display order. */
  private static final List<String> TOP_LEVEL_PHASES =
      List.of(
          PHASE_CONFIGURATION_RESOLUTION,
          PHASE_BUILDER_DEFINITION_EXTRACTION,
          PHASE_DTO_MAPPING,
          PHASE_CODE_GENERATION);

  private static final Map<String, List<String>> PHASE_CHILDREN = new LinkedHashMap<>();

  static {
    PHASE_CHILDREN.put(
        PHASE_CODE_GENERATION, List.of(PHASE_SOURCE_CONSTRUCTION, PHASE_FILE_WRITING));
    PHASE_CHILDREN.put(
        PHASE_SOURCE_CONSTRUCTION,
        List.of(PHASE_ELEMENT_BUILDING, PHASE_STRING_GENERATION, PHASE_FORMATTING));
    PHASE_CHILDREN.put(
        PHASE_ELEMENT_BUILDING,
        List.of(
            PHASE_CLASS_CREATION,
            PHASE_CLASS_METADATA,
            PHASE_FIELDS,
            PHASE_CONSTRUCTORS,
            PHASE_METHODS,
            PHASE_NESTED_TYPES,
            PHASE_CLASS_ANNOTATIONS));
  }

  private final Map<String, Long> phaseTimes = new LinkedHashMap<>();
  private final Map<String, Long> generatorTimes = new LinkedHashMap<>();
  private final Map<String, Integer> generatorCalls = new LinkedHashMap<>();
  private final Map<String, Long> enhancerTimes = new LinkedHashMap<>();
  private final Map<String, Integer> enhancerCalls = new LinkedHashMap<>();
  private final List<ClassMetric> classMetrics = new ArrayList<>();

  private final ThreadLocal<List<Long>> phaseStartStack = ThreadLocal.withInitial(ArrayList::new);
  private final ThreadLocal<List<Long>> generatorStartStack =
      ThreadLocal.withInitial(ArrayList::new);
  private final ThreadLocal<List<Long>> enhancerStartStack =
      ThreadLocal.withInitial(ArrayList::new);

  private long totalStartTime;
  private int totalClasses = 0;

  private String currentClassName;
  private long classStartTime;

  /** Creates a new ActivePerformanceTracker and records the overall start time. */
  public ActivePerformanceTracker() {
    this.totalStartTime = System.nanoTime();
  }

  @Override
  public void startPhase(String phase, String className) {
    phaseStartStack.get().add(System.nanoTime());
  }

  @Override
  public void endPhase(String phase) {
    List<Long> stack = phaseStartStack.get();
    if (stack.isEmpty()) {
      return;
    }
    long start = stack.remove(stack.size() - 1);
    long elapsed = System.nanoTime() - start;
    phaseTimes.merge(phase, elapsed, Long::sum);
  }

  @Override
  public void startGenerator(String generatorName) {
    generatorStartStack.get().add(System.nanoTime());
  }

  @Override
  public void endGenerator(String generatorName) {
    List<Long> stack = generatorStartStack.get();
    if (stack.isEmpty()) {
      return;
    }
    long start = stack.remove(stack.size() - 1);
    long elapsed = System.nanoTime() - start;
    generatorTimes.merge(generatorName, elapsed, Long::sum);
    generatorCalls.merge(generatorName, 1, Integer::sum);
  }

  @Override
  public void startEnhancer(String enhancerName) {
    enhancerStartStack.get().add(System.nanoTime());
  }

  @Override
  public void endEnhancer(String enhancerName) {
    List<Long> stack = enhancerStartStack.get();
    if (stack.isEmpty()) {
      return;
    }
    long start = stack.remove(stack.size() - 1);
    long elapsed = System.nanoTime() - start;
    enhancerTimes.merge(enhancerName, elapsed, Long::sum);
    enhancerCalls.merge(enhancerName, 1, Integer::sum);
  }

  @Override
  public void startClass(String className) {
    this.currentClassName = className;
    this.classStartTime = System.nanoTime();
  }

  @Override
  public void endClass(int fieldCount, int collectionCount) {
    if (currentClassName == null) {
      return;
    }
    long elapsed = System.nanoTime() - classStartTime;
    classMetrics.add(new ClassMetric(currentClassName, elapsed, fieldCount, collectionCount));
    totalClasses++;
    currentClassName = null;
  }

  @Override
  public void generateReport(ProcessingLogger logger) {
    long totalTime = System.nanoTime() - totalStartTime;
    double totalSeconds = totalTime / 1_000_000_000.0;

    logger.info("simple-builders: PERFORMANCE REPORT");
    logger.info("================================");
    logger.info("Total classes processed: %d", totalClasses);
    logger.info(String.format(Locale.US, "Total processing time: %.1fs", totalSeconds));
    logger.info("");

    // Phase breakdown (hierarchical, using hardcoded hierarchy)
    logger.info("Phase breakdown:");
    for (int i = 0; i < TOP_LEVEL_PHASES.size(); i++) {
      reportPhase(
          logger, TOP_LEVEL_PHASES.get(i), totalSeconds, "", i == TOP_LEVEL_PHASES.size() - 1);
    }
    logger.info("");

    // Average per class
    if (totalClasses > 0) {
      double avgPerClass = (totalTime / 1_000_000.0) / totalClasses;
      logger.info(String.format(Locale.US, "Average per class: %.1fms", avgPerClass));
      logger.info("");
    }

    // Top 20 slowest classes
    List<ClassMetric> topClasses = new ArrayList<>(classMetrics);
    topClasses.sort(Comparator.comparingLong(ClassMetric::elapsedNanos).reversed());
    int classLimit = Math.min(20, topClasses.size());
    if (classLimit > 0) {
      logger.info("Top %d slowest classes:", classLimit);
      for (int i = 0; i < classLimit; i++) {
        ClassMetric cm = topClasses.get(i);
        double ms = cm.elapsedNanos() / 1_000_000.0;
        logger.info(
            String.format(
                Locale.US,
                "  %d. %s - %.1fms (%d fields, %d collections)",
                i + 1,
                cm.className(),
                ms,
                cm.fieldCount(),
                cm.collectionCount()));
      }
      logger.info("");
    }

    // Top 5 slowest MethodGenerators
    List<Map.Entry<String, Long>> topGenerators = new ArrayList<>(generatorTimes.entrySet());
    topGenerators.sort(Map.Entry.<String, Long>comparingByValue().reversed());
    int genLimit = Math.min(5, topGenerators.size());
    if (genLimit > 0) {
      logger.info("Top %d slowest MethodGenerators:", genLimit);
      for (int i = 0; i < genLimit; i++) {
        Map.Entry<String, Long> entry = topGenerators.get(i);
        double seconds = entry.getValue() / 1_000_000_000.0;
        int calls = generatorCalls.getOrDefault(entry.getKey(), 0);
        double avgMs = calls > 0 ? (entry.getValue() / 1_000_000.0) / calls : 0;
        logger.info(
            String.format(
                Locale.US,
                "  %d. %s - %.1fs (%d calls, %.2fms/call)",
                i + 1,
                entry.getKey(),
                seconds,
                calls,
                avgMs));
      }
      logger.info("");
    }

    // Top 5 slowest BuilderEnhancers
    List<Map.Entry<String, Long>> topEnhancers = new ArrayList<>(enhancerTimes.entrySet());
    topEnhancers.sort(Map.Entry.<String, Long>comparingByValue().reversed());
    int enhLimit = Math.min(5, topEnhancers.size());
    if (enhLimit > 0) {
      logger.info("Top %d slowest BuilderEnhancers:", enhLimit);
      for (int i = 0; i < enhLimit; i++) {
        Map.Entry<String, Long> entry = topEnhancers.get(i);
        double seconds = entry.getValue() / 1_000_000_000.0;
        int calls = enhancerCalls.getOrDefault(entry.getKey(), 0);
        double avgMs = calls > 0 ? (entry.getValue() / 1_000_000.0) / calls : 0;
        logger.info(
            String.format(
                Locale.US,
                "  %d. %s - %.1fs (%d calls, %.2fms/call)",
                i + 1,
                entry.getKey(),
                seconds,
                calls,
                avgMs));
      }
    }
  }

  /** Record for per-class performance metrics. */
  private record ClassMetric(
      String className, long elapsedNanos, int fieldCount, int collectionCount) {}

  private void reportPhase(
      ProcessingLogger logger, String phase, double parentSeconds, String prefix, boolean isLast) {
    long nanos = phaseTimes.getOrDefault(phase, 0L);
    double seconds = nanos / 1_000_000_000.0;
    double percentage = parentSeconds > 0 ? (seconds / parentSeconds) * 100 : 0;
    String connector = isLast ? "└─ " : "├─ ";
    logger.info(
        String.format(
            Locale.US, "%s%s%s: %.1fs (%.1f%%)", prefix, connector, phase, seconds, percentage));
    List<String> children = PHASE_CHILDREN.get(phase);
    if (children != null) {
      String childPrefix = prefix + (isLast ? "   " : "│  ");
      for (int i = 0; i < children.size(); i++) {
        reportPhase(logger, children.get(i), seconds, childPrefix, i == children.size() - 1);
      }
    }
  }
}
