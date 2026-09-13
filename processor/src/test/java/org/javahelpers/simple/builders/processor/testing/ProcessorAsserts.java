package org.javahelpers.simple.builders.processor.testing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;

/**
 * Assertion helpers for processor tests. Centralizes common positive and negative checks and
 * compilation assertions for generated sources.
 */
public final class ProcessorAsserts {

  private ProcessorAsserts() {}

  /** Collapse all whitespace (spaces, tabs, newlines) to single spaces and trim. */
  private static String normalizeWhitespace(String input) {
    if (input == null) return null;
    return input.replaceAll("[\\t\\n\\r\\s]+", "").trim();
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

  /** Coupling of a search string with an assertion message for negative contains checks. */
  public static record NotContainsAssertRecord(String search, String message)
      implements AssertRecord {}

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
    String normalizedGenerated = normalizeWhitespace(generatedCode);
    List<String> failures = new ArrayList<>();

    for (AssertRecord check : checks) {
      if (isAssertNotFullfilled(check, normalizedGenerated)) {
        failures.add(check.message());
      }
    }

    if (!failures.isEmpty()) {
      String combinedMessage = "\n" + String.join("\n", failures);
      Assertions.fail(combinedMessage);
    }
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

  /**
   * Asserts that the compilation contains all the specified note messages. Useful for verifying
   * debug or info logging output.
   *
   * @param compilation the compilation result
   * @param noteMessages the note messages to check for
   */
  public static void assertHadNoteContaining(Compilation compilation, String... noteMessages) {
    for (String noteMessage : noteMessages) {
      assertThat(compilation).hadNoteContaining(noteMessage);
    }
  }

  /**
   * Asserts that the compilation's notes match the expected substrings in order and count.
   *
   * <p>Each expected substring is matched against the corresponding note message (by position). The
   * note count must match exactly, so adding or removing any log message fails the test — making
   * logging changes visible in code review and prompting documentation updates.
   *
   * @param compilation the compilation result
   * @param expectedSubstrings the substring each note (in order) must contain; length must equal
   *     the number of notes produced
   */
  public static void assertNotesInOrder(Compilation compilation, String... expectedSubstrings) {
    List<String> notes = compilation.notes().stream().map(n -> n.getMessage(null)).toList();
    assertEquals(
        expectedSubstrings.length,
        notes.size(),
        "Log note count changed — update expectedSubstrings and docs. "
            + "Expected %d, got %d. Actual notes:%n%s"
                .formatted(expectedSubstrings.length, notes.size(), String.join("%n", notes)));
    for (int i = 0; i < notes.size(); i++) {
      int index = i;
      Assertions.assertTrue(
          notes.get(i).contains(expectedSubstrings[i]),
          "Note %d mismatch.%n  Expected to contain: %s%n  Actual: %s"
              .formatted(index, expectedSubstrings[index], notes.get(index)));
    }
  }

  /**
   * Asserts that no generated source file for the given class' builder exists in the compilation.
   *
   * <p>The check is based on the simple class name: a file whose name ends with {@code
   * <className>Builder.java} is considered the builder for {@code className}.
   *
   * @param compilation the compilation result
   * @param className the simple class name for which no builder should have been generated
   * @param message the failure message
   */
  public static void assertNoBuilderGenerated(
      Compilation compilation, String className, String message) {
    Assertions.assertTrue(
        compilation.generatedSourceFiles().stream()
            .noneMatch(f -> f.getName().endsWith(className + "Builder.java")),
        message);
  }

  /**
   * Asserts that the expected and actual strings are equal after normalizing whitespace.
   *
   * @param expected the expected string
   * @param actual the actual string
   */
  public static void assertNormalizedEquals(String expected, String actual, String message) {
    assertEquals(normalizeWhitespace(expected), normalizeWhitespace(actual), message);
  }

  private static boolean isAssertNotFullfilled(AssertRecord check, String normalizedGenerated) {
    String normalizedSearch = normalizeWhitespace(check.search());
    return (check instanceof ContainsAssertRecord
            && !normalizedGenerated.contains(normalizedSearch))
        || (check instanceof NotContainsAssertRecord
            && normalizedGenerated.contains(normalizedSearch));
  }
}
