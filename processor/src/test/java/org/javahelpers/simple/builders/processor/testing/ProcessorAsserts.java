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

    Assertions.assertTrue(
        generated.contains("public " + targetSimpleName + " build()"),
        "method missing: 'public " + targetSimpleName + " build()'");
    Assertions.assertTrue(
        generated.contains("public static " + builderSimpleName + " create()"),
        "method missing: 'public static " + builderSimpleName + " create()'");
  }

  /** Coupling of a search string with an assertion message for positive contains checks. */
  public static record ContainsAssertRecord(String search, String message) {}

  /** Coupling of a search string with an assertion message for negative contains checks. */
  public static record NotContainsAssertRecord(String search, String message) {}

  /** Factory for positive contains check with custom message. */
  public static ContainsAssertRecord containsWithMessage(String search, String message) {
    return new ContainsAssertRecord(search, message);
  }

  /** Factory for positive contains check with default message. */
  public static ContainsAssertRecord contains(String search) {
    return new ContainsAssertRecord(search, String.format("method missing: '%s'", search));
  }

  /** Factory for negative contains check with custom message. */
  public static NotContainsAssertRecord notContainsWithMessage(String search, String message) {
    return new NotContainsAssertRecord(search, message);
  }

  /** Factory for negative contains check with default message. */
  public static NotContainsAssertRecord notContains(String search) {
    return new NotContainsAssertRecord(search, String.format("should not contain: '%s'", search));
  }

  /** Assert that generated code contains all provided checks. */
  public static void assertingResult(String generatedCode, ContainsAssertRecord... checks) {
    List<Executable> executables = new ArrayList<>();
    for (ContainsAssertRecord check : checks) {
      executables.add(
          () -> Assertions.assertTrue(generatedCode.contains(check.search()), check.message()));
    }
    Assertions.assertAll(executables.toArray(new Executable[0]));
  }

  /** Assert that generated code does not contain any of the provided checks. */
  public static void assertNotContaining(String generatedCode, NotContainsAssertRecord... checks) {
    List<Executable> executables = new ArrayList<>();
    for (NotContainsAssertRecord check : checks) {
      executables.add(
          () -> Assertions.assertFalse(generatedCode.contains(check.search()), check.message()));
    }
    Assertions.assertAll(executables.toArray(new Executable[0]));
  }
}
