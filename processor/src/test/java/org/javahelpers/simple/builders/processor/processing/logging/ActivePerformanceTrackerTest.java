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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.javahelpers.simple.builders.processor.classgen.GenerationPhases;
import org.javahelpers.simple.builders.processor.processing.ProcessingPhases;
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
        new ActivePerformanceTracker(
            outputFile, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
    tracker.startClass("TestClassA");
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_BUILDER_DEFINITION_EXTRACTION);
    tracker.startGenerator();
    tracker.endGenerator("FieldSupplierGenerator");
    tracker.startEnhancer();
    tracker.endEnhancer("CoreMethodsEnhancer");
    tracker.endClass(5, 2);

    tracker.startClass("TestClassB");
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_DTO_MAPPING);
    tracker.endClass(3, 1);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    String jsonContent = Files.readString(Path.of(outputFile));
    return new ObjectMapper().readTree(jsonContent);
  }

  @Test
  void generateReport_withNoData_logsBasicReport() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION);
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_CODE_GENERATION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(capturing.messages().stream().anyMatch(m -> m.contains("Phase breakdown")));
    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION)));
    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains(ProcessingPhases.PHASE_CODE_GENERATION)));
  }

  @Test
  void generateReport_withElementCollectionPhase_logsPhaseInReport() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_ELEMENT_COLLECTION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains(ProcessingPhases.PHASE_ELEMENT_COLLECTION)),
        "Text report should contain " + ProcessingPhases.PHASE_ELEMENT_COLLECTION);
  }

  @Test
  void jsonReport_phaseBreakdown_containsElementCollectionPhase() throws IOException {
    Path jsonFile = tempDir.resolve("report-element-collection.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            jsonFile.toString(),
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_ELEMENT_COLLECTION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode phases = root.get("phaseBreakdown");
    assertTrue(
        phases.has(ProcessingPhases.PHASE_ELEMENT_COLLECTION),
        "JSON phaseBreakdown should contain " + ProcessingPhases.PHASE_ELEMENT_COLLECTION);
    JsonNode elementCollection = phases.get(ProcessingPhases.PHASE_ELEMENT_COLLECTION);
    assertTrue(elementCollection.has("elapsedNanos"));
    assertTrue(elementCollection.has("elapsedSeconds"));
    assertTrue(elementCollection.has("percentage"));
  }

  @Test
  void generateReport_withNullOutputFile_doesNotWriteFile() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
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
    assertEquals("TestClassA", classMetrics.get(0).get("className").asText());
    assertEquals("TestClassB", classMetrics.get(1).get("className").asText());
    assertTrue(
        classMetrics.get(0).get("elapsedNanos").asLong()
            >= classMetrics.get(1).get("elapsedNanos").asLong());
  }

  @Test
  void jsonReport_classMetrics_containsFieldAndCollectionCounts() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode classA = root.get("classMetrics").get(0);
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
    assertTrue(phases.has(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION));
    assertTrue(phases.has(ProcessingPhases.PHASE_BUILDER_DEFINITION_EXTRACTION));
    assertTrue(phases.has(ProcessingPhases.PHASE_DTO_MAPPING));
    assertTrue(phases.has(ProcessingPhases.PHASE_CODE_GENERATION));

    JsonNode codeGen = phases.get(ProcessingPhases.PHASE_CODE_GENERATION);
    assertTrue(codeGen.has("elapsedNanos"));
    assertTrue(codeGen.has("elapsedSeconds"));
    assertTrue(codeGen.has("percentage"));
  }

  @Test
  void jsonReport_phaseBreakdown_codeGenerationHasChildren() throws IOException {
    Path jsonFile = tempDir.resolve("report.json");
    JsonNode root = generateReportAndParseJson(jsonFile.toString());

    JsonNode codeGen = root.get("phaseBreakdown").get(ProcessingPhases.PHASE_CODE_GENERATION);
    assertTrue(codeGen.has("children"));
    JsonNode children = codeGen.get("children");
    assertTrue(children.has(GenerationPhases.PHASE_SOURCE_CONSTRUCTION));
    assertTrue(children.has(GenerationPhases.PHASE_FILE_WRITING));
  }

  @Test
  void jsonReport_withNoData_hasEmptyArrays() throws IOException {
    Path jsonFile = tempDir.resolve("report-empty.json");
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            jsonFile.toString(),
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);

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
        new ActivePerformanceTracker(
            null, ProcessingPhases.TOP_LEVEL_PHASES, ProcessingPhases.PHASE_CHILDREN);
    tracker.endPhase(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION);
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
        new ActivePerformanceTracker(
            jsonFile.toString(),
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            jsonFile.toString(),
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            jsonFile.toString(),
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION);
    tracker.startPhase();
    tracker.endPhase(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION);

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    ProcessingLogger logger = capturing.logger();
    tracker.generateReport(logger);

    JsonNode root = new ObjectMapper().readTree(Files.readString(jsonFile));
    JsonNode phase =
        root.get("phaseBreakdown").get(ProcessingPhases.PHASE_CONFIGURATION_RESOLUTION);
    assertTrue(phase.get("elapsedNanos").asLong() > 0);
  }

  @Test
  void generateReport_logsWarningOnInvalidPath() {
    ActivePerformanceTracker tracker =
        new ActivePerformanceTracker(
            "/nonexistent\0invalid/path.json",
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);
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
        new ActivePerformanceTracker(
            jsonFile.toString(),
            ProcessingPhases.TOP_LEVEL_PHASES,
            ProcessingPhases.PHASE_CHILDREN);
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
    long first = genStats.get(0).get("elapsedNanos").asLong();
    long second = genStats.get(1).get("elapsedNanos").asLong();
    assertTrue(first >= second, "Generator stats should be sorted by elapsed descending");

    JsonNode enhStats = root.get("enhancerStats");
    long firstE = enhStats.get(0).get("elapsedNanos").asLong();
    long secondE = enhStats.get(1).get("elapsedNanos").asLong();
    assertTrue(firstE >= secondE, "Enhancer stats should be sorted by elapsed descending");
  }
}
