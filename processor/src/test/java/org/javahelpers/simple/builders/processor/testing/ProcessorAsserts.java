package org.javahelpers.simple.builders.processor.testing;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

/**
 * Assertion helpers for processor tests. Centralizes common positive and negative checks and
 * compilation assertions for generated sources.
 */
public final class ProcessorAsserts {

  private ProcessorAsserts() {}

  /** Collapse all whitespace (spaces, tabs, newlines) to single spaces and trim. */
  private static String normalizeWhitespace(String input) {
    if (input == null) return null;
    return input.replaceAll("\\s+", " ").trim();
  }

  /**
   * Asserts compilation succeeded and that the basic builder API exists in the provided generated
   * source (build() and static create()).
   */
  public static void assertGenerationSucceeded(
      Compilation compilation, String builderSimpleName, String generated) {
    assertThat(compilation).succeededWithoutWarnings();

    String targetSimpleName =
        builderSimpleName.endsWith("Builder")
            ? builderSimpleName.substring(0, builderSimpleName.length() - "Builder".length())
            : builderSimpleName;

    String normalized = normalizeWhitespace(generated);
    Assertions.assertTrue(
        normalized.contains(normalizeWhitespace("public " + targetSimpleName + " build()")),
        "method missing: 'public " + targetSimpleName + " build()'");
    Assertions.assertTrue(
        normalized.contains(
            normalizeWhitespace("public static " + builderSimpleName + " create()")),
        "method missing: 'public static " + builderSimpleName + " create()'");
  }

  /** Common interface for assertion records. */
  public interface AssertRecord {
    String search();

    String message();
  }

  /** Coupling of a search string with an assertion message for positive contains checks. */
  public static record ContainsAssertRecord(String search, String message)
      implements AssertRecord {}
  ;

  /** Coupling of a search string with an assertion message for negative contains checks. */
  public static record NotContainsAssertRecord(String search, String message)
      implements AssertRecord {}
  ;

  /** Factory for positive contains check with default message. */
  public static ContainsAssertRecord contains(String search) {
    return new ContainsAssertRecord(search, String.format("content missing: '%s'", search));
  }

  /** Factory for negative contains check with default message. */
  public static NotContainsAssertRecord notContains(String search) {
    return new NotContainsAssertRecord(
        search, String.format("content should not be found: '%s'", search));
  }

  /** Assert that generated code matches all provided checks (positive or negative). */
  public static void assertingResult(String generatedCode, AssertRecord... checks) {
    List<Executable> executables = new ArrayList<>();
    String normalizedGenerated = normalizeWhitespace(generatedCode);
    for (AssertRecord check : checks) {
      String normalizedSearch = normalizeWhitespace(check.search());
      if (check instanceof ContainsAssertRecord) {
        executables.add(
            () ->
                Assertions.assertTrue(
                    normalizedGenerated.contains(normalizedSearch),
                    check.message()));
      } else if (check instanceof NotContainsAssertRecord) {
        executables.add(
            () ->
                Assertions.assertFalse(
                    normalizedGenerated.contains(normalizedSearch),
                    check.message()));
      }
    }
    Assertions.assertAll(executables.toArray(new Executable[0]));
  }

  /** Convenience overload: accept plain strings and convert to NotContainsAssertRecord. */
  public static void assertNotContaining(String generatedCode, String... searches) {
    AssertRecord[] checks =
        java.util.Arrays.stream(searches)
            .map(ProcessorAsserts::notContains)
            .toArray(AssertRecord[]::new);
    assertingResult(generatedCode, checks);
  }

  /** Convenience overload: accept plain strings and convert to ContainsAssertRecord. */
  public static void assertContaining(String generatedCode, String... searches) {
    AssertRecord[] checks =
        java.util.Arrays.stream(searches)
            .map(ProcessorAsserts::contains)
            .toArray(AssertRecord[]::new);
    assertingResult(generatedCode, checks);
  }
}
