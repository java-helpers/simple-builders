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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.javahelpers.codegen.GenerationPhases;
import org.javahelpers.simple.builders.processor.processing.PipelinePhases;
import org.javahelpers.simple.builders.processor.testing.CapturingProcessingLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link ActivePerformanceTracker}.
 *
 * <p>Tests cover phase tracking, generator/enhancer tracking, class metrics, JSON report output
 * structure and correctness, edge cases, and ThreadLocal cleanup.
 */
class ActivePerformanceTrackerTest {

  @TempDir Path tempDir;

  /** Helper to create a tracker, track some data, and generate report with JSON output. */
  private JsonNode generateReportAndParseJson(String outputFile) throws IOException {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(outputFile, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("TestClassA");
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_ANALYSIS);
    tracker.startGenerator();
    tracker.endGenerator("FieldSupplierGenerator");
    tracker.startEnhancer();
    tracker.endEnhancer("CoreMethodsEnhancer");
    tracker.endClass(5, 2);

    tracker.startClass("TestClassB");
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_TARGET_MAPPING);
    tracker.endClass(3, 1);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    String jsonContent = Files.readString(Path.of(outputFile));
    return new ObjectMapper().readTree(jsonContent);
  }

  /** Asserts the metric entries are ordered by {@code elapsedNanos} descending. */
  private static void assertSortedByElapsedDesc(JsonNode metrics) {
    for (int i = 1; i < metrics.size(); i++) {
      assertTrue(
          metrics.get(i - 1).get("elapsedNanos").asLong()
              >= metrics.get(i).get("elapsedNanos").asLong(),
          "metrics not sorted by elapsedNanos descending at index " + i);
    }
  }

  /** Asserts the class-metric entries carry exactly the given class names. */
  private static void assertClassNames(JsonNode classMetrics, Set<String> expected) {
    Set<String> names = new HashSet<>();
    for (JsonNode cm : classMetrics) {
      names.add(cm.get("className").asText());
    }
    assertEquals(expected, names);
  }

  /** The class-metric entry for the given class name — fails the test when absent. */
  private static JsonNode classMetricByName(JsonNode root, String className) {
    for (JsonNode cm : root.get("classMetrics")) {
      if (className.equals(cm.get("className").asText())) {
        return cm;
      }
    }
    return fail("No classMetric entry for " + className);
  }

  /**
   * Pins the complete tagged report layout: every line carries the logger's diagnostic prefix and
   * the tree connectors sit after it. Timing values are normalized to {@code <n>} — the structure,
   * labels and indentation are what is asserted.
   */
  @Test
  void generateReport_prefixedLogger_producesExpectedTaggedLayout() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_ANALYSIS);
    tracker.startGenerator();
    tracker.endGenerator("MyGenerator");
    tracker.startEnhancer();
    tracker.endEnhancer("MyEnhancer");
    tracker.endClass(5, 2);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create("simple-builders");
    tracker.generateReport(capturing.logger());

    // Remove the diagnostic kind marker and the exact measurements (durations, percentages,
    // counts) before comparing — the assertion pins the report's structure, labels and
    // indentation, not the timings
    String actual =
        String.join(
            "\n",
            capturing.messages().stream()
                .map(m -> m.replaceFirst("^[A-Z]+: ", ""))
                .map(m -> m.replaceAll("\\d+(\\.\\d+)?", "<n>"))
                .map(String::stripTrailing)
                .toList());

    assertEquals(
        """
        simple-builders: PERFORMANCE REPORT
        simple-builders: ================================
        simple-builders: Total classes processed: <n>
        simple-builders: Total processing time: <n>s
        simple-builders:
        simple-builders: Phase breakdown:
        simple-builders: ├─ Element Collection: <n>s (<n>%)
        simple-builders: ├─ Configuration Resolution: <n>s (<n>%)
        simple-builders: ├─ Analysis: <n>s (<n>%)
        simple-builders: ├─ Target Mapping: <n>s (<n>%)
        simple-builders: └─ Code Generation: <n>s (<n>%)
        simple-builders:    ├─ Source Construction: <n>s (<n>%)
        simple-builders:    │  ├─ Element Building: <n>s (<n>%)
        simple-builders:    │  │  ├─ Class Creation: <n>s (<n>%)
        simple-builders:    │  │  ├─ Class Metadata: <n>s (<n>%)
        simple-builders:    │  │  ├─ Fields: <n>s (<n>%)
        simple-builders:    │  │  ├─ Constructors: <n>s (<n>%)
        simple-builders:    │  │  ├─ Methods: <n>s (<n>%)
        simple-builders:    │  │  ├─ Nested Types: <n>s (<n>%)
        simple-builders:    │  │  └─ Class Annotations: <n>s (<n>%)
        simple-builders:    │  ├─ String Generation: <n>s (<n>%)
        simple-builders:    │  └─ Formatting: <n>s (<n>%)
        simple-builders:    └─ File Writing: <n>s (<n>%)
        simple-builders:
        simple-builders: Average per class: <n>ms
        simple-builders:
        simple-builders: Top <n> slowest classes:
        simple-builders:   <n>. MyClass - <n>ms (<n> fields, <n> collections)
        simple-builders:
        simple-builders: Top <n> slowest MethodGenerators:
        simple-builders:   <n>. MyGenerator - <n>s (<n> calls, <n>ms/call)
        simple-builders:
        simple-builders: Top <n> slowest BuilderEnhancers:
        simple-builders:   <n>. MyEnhancer - <n>s (<n> calls, <n>ms/call)\
        """,
        actual);
  }

  @Test
  void generateReport_withNoData_logsBasicReport() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();

    tracker.generateReport(logger);

    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("PERFORMANCE REPORT")));
    assertTrue(
        capturing.messages().stream().anyMatch(m -> m.contains("Total classes processed: 0")));
  }

  @Test
  void generateReport_withClassData_logsClassCount() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.endClass(4, 1);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(
        capturing.messages().stream().anyMatch(m -> m.contains("Total classes processed: 1")));
    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("MyClass")));
  }

  @Test
  void generateReport_withGeneratorData_logsGeneratorStats() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.startGenerator();
    tracker.endGenerator("MyGenerator");
    tracker.endClass(2, 0);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("MethodGenerators")));
    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("MyGenerator")));
  }

  @Test
  void generateReport_withEnhancerData_logsEnhancerStats() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.startEnhancer();
    tracker.endEnhancer("MyEnhancer");
    tracker.endClass(2, 0);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("BuilderEnhancers")));
    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("MyEnhancer")));
  }

  @Test
  void generateReport_withPhaseData_logsPhaseBreakdown() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION);
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_CODE_GENERATION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("Phase breakdown")));
    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION)));
    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains(PipelinePhases.PHASE_CODE_GENERATION)));
  }

  @Test
  void generateReport_withElementCollectionPhase_logsPhaseInReport() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_ELEMENT_COLLECTION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains(PipelinePhases.PHASE_ELEMENT_COLLECTION)),
        "Text report should contain " + PipelinePhases.PHASE_ELEMENT_COLLECTION);
  }

  @Test
  void jsonReport_phaseBreakdown_containsElementCollectionPhase() throws IOException {
    Path jsonFile = tempDir.resolve("report-element-collection.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(jsonFile.toString(), PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_ELEMENT_COLLECTION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode phases = root.get("phaseBreakdown");
    assertTrue(
        phases.has(PipelinePhases.PHASE_ELEMENT_COLLECTION),
        "JSON phaseBreakdown should contain " + PipelinePhases.PHASE_ELEMENT_COLLECTION);
    JsonNode elementCollection = phases.get(PipelinePhases.PHASE_ELEMENT_COLLECTION);
    assertTrue(elementCollection.has("elapsedNanos"));
    assertTrue(elementCollection.has("elapsedSeconds"));
    assertTrue(elementCollection.has("percentage"));
  }

  @Test
  void generateReport_withNullOutputFile_doesNotWriteFile() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.endClass(1, 0);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertFalse(capturing.messages().stream().anyMatch(m -> m.contains("JSON report written")));
  }

  @Test
  void generateReport_withOutputFile_writesValidJson() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    assertTrue(root.has("timestamp"));
    assertEquals(2, root.get("totalClasses").asInt());
    assertTrue(root.has("totalProcessingTimeNanos"));
    assertTrue(root.has("totalProcessingTimeSeconds"));
    assertTrue(root.has("averagePerClassMs"));
    assertTrue(root.has("phaseBreakdown"));
    assertTrue(root.has("classMetrics"));
    assertTrue(root.has("generatorStats"));
    assertTrue(root.has("enhancerStats"));
  }

  @Test
  void jsonReport_classMetrics_containsAllClassesSortedByElapsedDesc() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode classMetrics = root.get("classMetrics");
    assertEquals(2, classMetrics.size());
    // Ordering is the contract (desc by elapsed); which class is first is timing-dependent.
    assertClassNames(classMetrics, Set.of("TestClassA", "TestClassB"));
    assertSortedByElapsedDesc(classMetrics);
  }

  @Test
  void jsonReport_classMetrics_containsFieldAndCollectionCounts() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode classA = classMetricByName(root, "TestClassA");
    assertEquals(5, classA.get("fieldCount").asInt());
    assertEquals(2, classA.get("collectionCount").asInt());
    assertTrue(classA.has("elapsedMs"));
  }

  @Test
  void jsonReport_generatorStats_containsGeneratorData() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode genStats = root.get("generatorStats");
    assertEquals(1, genStats.size());
    assertEquals("FieldSupplierGenerator", genStats.get(0).get("name").asText());
    assertEquals(1, genStats.get(0).get("calls").asInt());
    assertTrue(genStats.get(0).has("elapsedNanos"));
    assertTrue(genStats.get(0).has("avgMsPerCall"));
  }

  @Test
  void jsonReport_enhancerStats_containsEnhancerData() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode enhStats = root.get("enhancerStats");
    assertEquals(1, enhStats.size());
    assertEquals("CoreMethodsEnhancer", enhStats.get(0).get("name").asText());
    assertEquals(1, enhStats.get(0).get("calls").asInt());
  }

  @Test
  void jsonReport_phaseBreakdown_containsPhaseHierarchy() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode phases = root.get("phaseBreakdown");
    assertTrue(phases.has(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION));
    assertTrue(phases.has(PipelinePhases.PHASE_ANALYSIS));
    assertTrue(phases.has(PipelinePhases.PHASE_TARGET_MAPPING));
    assertTrue(phases.has(PipelinePhases.PHASE_CODE_GENERATION));

    JsonNode codeGen = phases.get(PipelinePhases.PHASE_CODE_GENERATION);
    assertTrue(codeGen.has("elapsedNanos"));
    assertTrue(codeGen.has("elapsedSeconds"));
    assertTrue(codeGen.has("percentage"));
  }

  @Test
  void jsonReport_phaseBreakdown_codeGenerationHasChildren() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode codeGen = root.get("phaseBreakdown").get(PipelinePhases.PHASE_CODE_GENERATION);
    assertTrue(codeGen.has("children"));
    JsonNode children = codeGen.get("children");
    assertTrue(children.has(GenerationPhases.PHASE_SOURCE_CONSTRUCTION));
    assertTrue(children.has(GenerationPhases.PHASE_FILE_WRITING));
  }

  @Test
  void jsonReport_withNoData_hasEmptyArrays() throws IOException {
    Path jsonFile = tempDir.resolve("report-empty.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(jsonFile.toString(), PipelinePhases.PIPELINE_HIERARCHY);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    assertEquals(0, root.get("totalClasses").asInt());
    assertTrue(root.get("classMetrics").isArray());
    assertEquals(0, root.get("classMetrics").size());
    assertTrue(root.get("generatorStats").isArray());
    assertEquals(0, root.get("generatorStats").size());
    assertTrue(root.get("enhancerStats").isArray());
    assertEquals(0, root.get("enhancerStats").size());
  }

  @Test
  void jsonReport_createsParentDirectoriesIfMissing() throws IOException {
    Path jsonFile = tempDir.resolve("subdir").resolve("nested").resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    assertTrue(Files.exists(jsonFile));
    assertEquals(2, root.get("totalClasses").asInt());
  }

  @Test
  void endMethodsWithoutStart_doesNothing() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(null, PipelinePhases.PIPELINE_HIERARCHY);
    tracker.endPhase(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION);
    tracker.endGenerator("NonexistentGenerator");
    tracker.endEnhancer("NonexistentEnhancer");
    tracker.endClass(5, 2);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("PERFORMANCE REPORT")));
    assertTrue(
        capturing.messages().stream().anyMatch(m -> m.contains("Total classes processed: 0")));
    assertFalse(capturing.messages().stream().anyMatch(m -> m.contains("MethodGenerators")));
    assertFalse(capturing.messages().stream().anyMatch(m -> m.contains("BuilderEnhancers")));
  }

  @Test
  void multipleGenerators_accumulateTimeAndCalls() throws IOException {
    Path jsonFile = tempDir.resolve("report-multi-gen.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(jsonFile.toString(), PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.startGenerator();
    tracker.endGenerator("GenA");
    tracker.startGenerator();
    tracker.endGenerator("GenA");
    tracker.startGenerator();
    tracker.endGenerator("GenB");
    tracker.endClass(2, 0);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode genStats = root.get("generatorStats");
    assertEquals(2, genStats.size());

    JsonNode genA = genStats.get(genStats.get(0).get("name").asText().equals("GenA") ? 0 : 1);
    assertEquals(2, genA.get("calls").asInt());
  }

  @Test
  void jsonReport_escapesSpecialCharactersInNames() throws IOException {
    Path jsonFile = tempDir.resolve("report-escaping.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(jsonFile.toString(), PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.startGenerator();
    tracker.endGenerator("Gen\"\\\n\t\r\b\fA\u0001");
    tracker.startClass("MyClass2");
    tracker.startEnhancer();
    tracker.endEnhancer("Enh\u0001x");
    tracker.endClass(2, 0);

    tracker.generateReport(CapturingProcessingLogger.create().logger());

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode genStats = root.get("generatorStats");
    assertEquals("Gen\"\\\n\t\r\b\fA\u0001", genStats.get(0).get("name").asText());
    JsonNode enhStats = root.get("enhancerStats");
    assertEquals("Enh\u0001x", enhStats.get(0).get("name").asText());
  }

  @Test
  void multiplePhases_accumulateTime() throws IOException {
    Path jsonFile = tempDir.resolve("report-multi-phase.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(jsonFile.toString(), PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION);
    tracker.startPhase();
    tracker.endPhase(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode phase = root.get("phaseBreakdown").get(PipelinePhases.PHASE_CONFIGURATION_RESOLUTION);
    assertTrue(phase.get("elapsedNanos").asLong() > 0);
  }

  @Test
  void generateReport_logsWarningOnInvalidPath() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            "/nonexistent\0invalid/path.json", PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.endClass(1, 0);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains("WARNING") && m.contains("Failed to write")));
  }

  @Test
  void noOpPerformanceTracker_allMethodsAreNoOps() {
    NoOpPerformanceTracker tracker = new NoOpPerformanceTracker();
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();

    tracker.startPhase();
    tracker.endPhase("Phase");
    tracker.startGenerator();
    tracker.endGenerator("Gen");
    tracker.startEnhancer();
    tracker.endEnhancer("Enh");
    tracker.startClass("Class");
    tracker.endClass(1, 0);
    tracker.generateReport(logger);

    assertTrue(capturing.messages().isEmpty());
  }

  @Test
  void jsonReport_generatorAndEnhancerStats_sortedByElapsedDesc() throws IOException {
    Path jsonFile = tempDir.resolve("report-sorted.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(jsonFile.toString(), PipelinePhases.PIPELINE_HIERARCHY);
    tracker.startClass("MyClass");
    tracker.startGenerator();
    tracker.endGenerator("SlowGen");
    tracker.startGenerator();
    tracker.endGenerator("FastGen");
    tracker.startEnhancer();
    tracker.endEnhancer("SlowEnh");
    tracker.startEnhancer();
    tracker.endEnhancer("FastEnh");
    tracker.endClass(2, 0);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode genStats = root.get("generatorStats");
    assertSortedByElapsedDesc(genStats);

    JsonNode enhStats = root.get("enhancerStats");
    assertSortedByElapsedDesc(enhStats);
  }
}
