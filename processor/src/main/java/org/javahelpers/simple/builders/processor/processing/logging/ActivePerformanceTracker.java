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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
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
  private final String outputFilePath;

  private String currentClassName;
  private long classStartTime;

  /**
   * Creates a new ActivePerformanceTracker and records the overall start time.
   *
   * @param outputFilePath optional path for JSON report output; null or empty disables file output
   */
  public ActivePerformanceTracker(String outputFilePath) {
    this.totalStartTime = System.nanoTime();
    this.outputFilePath = outputFilePath;
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

    // Write JSON report if output file is configured
    if (outputFilePath != null && !outputFilePath.isBlank()) {
      try {
        writeJsonReport(totalTime);
        logger.info("Performance JSON report written to: %s", outputFilePath);
      } catch (IOException e) {
        logger.warning("Failed to write performance JSON report: %s", e.getMessage());
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

  /**
   * Writes the performance report as structured JSON to the configured output file.
   *
   * @param totalNanos total processing time in nanoseconds
   * @throws IOException if the file cannot be written
   */
  private void writeJsonReport(long totalNanos) throws IOException {
    double totalSeconds = totalNanos / 1_000_000_000.0;
    double avgPerClassMs = totalClasses > 0 ? (totalNanos / 1_000_000.0) / totalClasses : 0;

    StringBuilder sb = new StringBuilder(4096);
    String indent = "  ";
    String indent2 = indent + indent;
    String indent3 = indent + indent + indent;

    sb.append("{\n");
    sb.append(indent)
        .append(jsonString("timestamp"))
        .append(": ")
        .append(jsonString(Instant.now().toString()))
        .append(",\n");
    sb.append(indent)
        .append(jsonString("totalClasses"))
        .append(": ")
        .append(totalClasses)
        .append(",\n");
    sb.append(indent)
        .append(jsonString("totalProcessingTimeNanos"))
        .append(": ")
        .append(totalNanos)
        .append(",\n");
    sb.append(indent)
        .append(jsonString("totalProcessingTimeSeconds"))
        .append(": ")
        .append(String.format(Locale.US, "%.3f", totalSeconds))
        .append(",\n");
    sb.append(indent)
        .append(jsonString("averagePerClassMs"))
        .append(": ")
        .append(String.format(Locale.US, "%.3f", avgPerClassMs))
        .append(",\n");

    // Phase breakdown
    sb.append(indent).append(jsonString("phaseBreakdown")).append(": {\n");
    for (int i = 0; i < TOP_LEVEL_PHASES.size(); i++) {
      appendPhaseJson(
          sb, TOP_LEVEL_PHASES.get(i), totalNanos, indent2, i == TOP_LEVEL_PHASES.size() - 1);
    }
    sb.append(indent).append("},\n");

    // Class metrics (all, sorted by elapsed descending)
    List<ClassMetric> sortedClasses = new ArrayList<>(classMetrics);
    sortedClasses.sort(Comparator.comparingLong(ClassMetric::elapsedNanos).reversed());
    sb.append(indent).append(jsonString("classMetrics")).append(": [\n");
    for (int i = 0; i < sortedClasses.size(); i++) {
      ClassMetric cm = sortedClasses.get(i);
      double ms = cm.elapsedNanos() / 1_000_000.0;
      sb.append(indent2).append("{\n");
      sb.append(indent3)
          .append(jsonString("className"))
          .append(": ")
          .append(jsonString(cm.className()))
          .append(",\n");
      sb.append(indent3)
          .append(jsonString("elapsedNanos"))
          .append(": ")
          .append(cm.elapsedNanos())
          .append(",\n");
      sb.append(indent3)
          .append(jsonString("elapsedMs"))
          .append(": ")
          .append(String.format(Locale.US, "%.3f", ms))
          .append(",\n");
      sb.append(indent3)
          .append(jsonString("fieldCount"))
          .append(": ")
          .append(cm.fieldCount())
          .append(",\n");
      sb.append(indent3)
          .append(jsonString("collectionCount"))
          .append(": ")
          .append(cm.collectionCount())
          .append("\n");
      sb.append(indent2).append(i < sortedClasses.size() - 1 ? "},\n" : "}\n");
    }
    sb.append(indent).append("],\n");

    // Generator stats
    List<Map.Entry<String, Long>> sortedGenerators = new ArrayList<>(generatorTimes.entrySet());
    sortedGenerators.sort(Map.Entry.<String, Long>comparingByValue().reversed());
    sb.append(indent).append(jsonString("generatorStats")).append(": [\n");
    for (int i = 0; i < sortedGenerators.size(); i++) {
      Map.Entry<String, Long> entry = sortedGenerators.get(i);
      int calls = generatorCalls.getOrDefault(entry.getKey(), 0);
      double avgMs = calls > 0 ? (entry.getValue() / 1_000_000.0) / calls : 0;
      sb.append(indent2).append("{\n");
      sb.append(indent3)
          .append(jsonString("name"))
          .append(": ")
          .append(jsonString(entry.getKey()))
          .append(",\n");
      sb.append(indent3)
          .append(jsonString("elapsedNanos"))
          .append(": ")
          .append(entry.getValue())
          .append(",\n");
      sb.append(indent3).append(jsonString("calls")).append(": ").append(calls).append(",\n");
      sb.append(indent3)
          .append(jsonString("avgMsPerCall"))
          .append(": ")
          .append(String.format(Locale.US, "%.3f", avgMs))
          .append("\n");
      sb.append(indent2).append(i < sortedGenerators.size() - 1 ? "},\n" : "}\n");
    }
    sb.append(indent).append("],\n");

    // Enhancer stats
    List<Map.Entry<String, Long>> sortedEnhancers = new ArrayList<>(enhancerTimes.entrySet());
    sortedEnhancers.sort(Map.Entry.<String, Long>comparingByValue().reversed());
    sb.append(indent).append(jsonString("enhancerStats")).append(": [\n");
    for (int i = 0; i < sortedEnhancers.size(); i++) {
      Map.Entry<String, Long> entry = sortedEnhancers.get(i);
      int calls = enhancerCalls.getOrDefault(entry.getKey(), 0);
      double avgMs = calls > 0 ? (entry.getValue() / 1_000_000.0) / calls : 0;
      sb.append(indent2).append("{\n");
      sb.append(indent3)
          .append(jsonString("name"))
          .append(": ")
          .append(jsonString(entry.getKey()))
          .append(",\n");
      sb.append(indent3)
          .append(jsonString("elapsedNanos"))
          .append(": ")
          .append(entry.getValue())
          .append(",\n");
      sb.append(indent3).append(jsonString("calls")).append(": ").append(calls).append(",\n");
      sb.append(indent3)
          .append(jsonString("avgMsPerCall"))
          .append(": ")
          .append(String.format(Locale.US, "%.3f", avgMs))
          .append("\n");
      sb.append(indent2).append(i < sortedEnhancers.size() - 1 ? "},\n" : "}\n");
    }
    sb.append(indent).append("]\n");

    sb.append("}\n");

    Path outPath = Paths.get(outputFilePath);
    if (outPath.getParent() != null) {
      Files.createDirectories(outPath.getParent());
    }
    Files.writeString(outPath, sb.toString(), StandardCharsets.UTF_8);
  }

  /**
   * Appends a phase entry (with children) as JSON.
   *
   * @param sb the string builder to append to
   * @param phase the phase name
   * @param parentNanos the parent phase total in nanoseconds (for percentage calculation)
   * @param indent the indentation string for this level
   * @param isLast whether this is the last sibling at this level
   */
  private void appendPhaseJson(
      StringBuilder sb, String phase, long parentNanos, String indent, boolean isLast) {
    long nanos = phaseTimes.getOrDefault(phase, 0L);
    double seconds = nanos / 1_000_000_000.0;
    double percentage = parentNanos > 0 ? (nanos * 100.0 / parentNanos) : 0;
    String childIndent = indent + "  ";

    sb.append(indent).append(jsonString(phase)).append(": {\n");
    sb.append(childIndent)
        .append(jsonString("elapsedNanos"))
        .append(": ")
        .append(nanos)
        .append(",\n");
    sb.append(childIndent)
        .append(jsonString("elapsedSeconds"))
        .append(": ")
        .append(String.format(Locale.US, "%.3f", seconds))
        .append(",\n");
    sb.append(childIndent)
        .append(jsonString("percentage"))
        .append(": ")
        .append(String.format(Locale.US, "%.1f", percentage));

    List<String> children = PHASE_CHILDREN.get(phase);
    if (children != null && !children.isEmpty()) {
      sb.append(",\n");
      sb.append(childIndent).append(jsonString("children")).append(": {\n");
      for (int i = 0; i < children.size(); i++) {
        appendPhaseJson(sb, children.get(i), nanos, childIndent + "  ", i == children.size() - 1);
      }
      sb.append(childIndent).append("}\n");
    } else {
      sb.append("\n");
    }
    sb.append(indent).append(isLast ? "}\n" : "},\n");
  }

  /**
   * Escapes a string value for JSON output.
   *
   * @param value the raw string
   * @return the JSON-escaped string wrapped in double quotes
   */
  private static String jsonString(String value) {
    if (value == null) {
      return "null";
    }
    StringBuilder escaped = new StringBuilder(value.length() + 2);
    escaped.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> escaped.append(c);
      }
    }
    escaped.append('"');
    return escaped.toString();
  }
}
