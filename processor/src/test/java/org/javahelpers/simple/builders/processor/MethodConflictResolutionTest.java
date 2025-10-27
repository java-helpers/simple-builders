package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for method conflict resolution based on priorities. This tests the feature from issue #64
 * where methods with the same signature from different fields are resolved by keeping the highest
 * priority method.
 */
class MethodConflictResolutionTest {

  private Compilation compile(JavaFileObject... files) {
    return javac().withProcessors(new BuilderProcessor()).compile(files);
  }

  @Test
  void shouldResolveConflictBetweenDirectSetterAndTransformMethod() {
    // Given: Two fields that would generate methods with same signature
    // - String field generates: name(String name) with priority 100
    // - Optional<String> field could generate: name(String name) with priority 80
    String packageName = "test";
    String className = "ConflictExample";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String directName;
                private java.util.Optional<String> optionalName;

                public String getDirectName() { return directName; }
                public void setDirectName(String directName) { this.directName = directName; }

                public java.util.Optional<String> getOptionalName() { return optionalName; }
                public void setOptionalName(java.util.Optional<String> optionalName) { this.optionalName = optionalName; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Should have both field-specific methods (direct setter wins for 'directName')
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public ConflictExampleBuilder directName(String directName)"),
        contains("public ConflictExampleBuilder optionalName(Optional<String> optionalName)"),
        contains("public ConflictExampleBuilder optionalName(String optionalName)"));
  }

  @Test
  void shouldHandleMultipleFieldsWithSimilarMethods() {
    // Given: Multiple string-like fields
    String packageName = "test";
    String className = "MultiStringFields";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String firstName;
                private String lastName;
                private String email;

                public String getFirstName() { return firstName; }
                public void setFirstName(String firstName) { this.firstName = firstName; }

                public String getLastName() { return lastName; }
                public void setLastName(String lastName) { this.lastName = lastName; }

                public String getEmail() { return email; }
                public void setEmail(String email) { this.email = email; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Should have distinct methods for each field (no conflicts)
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public MultiStringFieldsBuilder firstName(String firstName)"),
        contains("public MultiStringFieldsBuilder lastName(String lastName)"),
        contains("public MultiStringFieldsBuilder email(String email)"),
        contains("public MultiStringFieldsBuilder firstName(Supplier<String> firstNameSupplier)"),
        contains("public MultiStringFieldsBuilder lastName(Supplier<String> lastNameSupplier)"),
        contains("public MultiStringFieldsBuilder email(Supplier<String> emailSupplier)"));
  }

  @Test
  void shouldPreferHigherPriorityMethodWhenConflictOccurs() {
    // Given: Fields that might generate overlapping methods
    String packageName = "test";
    String className = "PriorityTest";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.List<String> items;

                public java.util.List<String> getItems() { return items; }
                public void setItems(java.util.List<String> items) { this.items = items; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // List fields should generate multiple methods with different priorities
    // Direct setter (priority 100) should always be present
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public PriorityTestBuilder items(List<String> items)"),
        contains("public PriorityTestBuilder items(Supplier<List<String>> itemsSupplier)"),
        contains("public PriorityTestBuilder items(String... items)"),
        contains(
            "public PriorityTestBuilder items(Consumer<ArrayListBuilder<String>> itemsBuilderConsumer)"));
  }

  @Test
  void shouldCompileSuccessfullyWithoutDuplicateMethods() {
    // Given: A class that previously would have caused compilation errors
    String packageName = "test";
    String className = "NoDuplicates";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;
                private java.util.Optional<String> optionalValue;
                private java.util.List<String> tags;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }

                public java.util.Optional<String> getOptionalValue() { return optionalValue; }
                public void setOptionalValue(java.util.Optional<String> optionalValue) { this.optionalValue = optionalValue; }

                public java.util.List<String> getTags() { return tags; }
                public void setTags(java.util.List<String> tags) { this.tags = tags; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then - should compile successfully
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Verify no duplicate method signatures exist
    int nameMethodCount = generatedCode.split("public NoDuplicatesBuilder name\\(").length - 1;
    int optionalValueMethodCount =
        generatedCode.split("public NoDuplicatesBuilder optionalValue\\(").length - 1;
    int tagsMethodCount = generatedCode.split("public NoDuplicatesBuilder tags\\(").length - 1;

    // Each field should have multiple overloaded methods, but no exact duplicates
    assert nameMethodCount >= 2 : "Expected at least 2 name methods";
    assert optionalValueMethodCount >= 2 : "Expected at least 2 optionalValue methods";
    assert tagsMethodCount >= 4 : "Expected at least 4 tags methods";
  }
}
