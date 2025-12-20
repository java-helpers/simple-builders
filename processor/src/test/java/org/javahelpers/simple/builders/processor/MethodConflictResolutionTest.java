package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for method conflict resolution mechanism. The conflict resolution system ensures that when
 * multiple fields would generate methods with identical signatures, only the highest priority
 * method is kept and a warning is emitted.
 *
 * <p><b>Note on Conflict Detection:</b> In practice, method conflicts are rare in normal usage
 * because:
 *
 * <ul>
 *   <li>Different field names generate different method names (no conflict)
 *   <li>You cannot have two fields with the same name in Java (prevents most conflicts)
 *   <li>Functional interface fields (Supplier, Consumer) skip supplier/consumer generation
 * </ul>
 *
 * <p><b>Important:</b> Java uses type erasure for generics. Methods with identical names and
 * parameter types (after erasure) conflict, even if generic parameters differ. For example: {@code
 * items(Consumer<ArrayListBuilder<String>>)} and {@code items(Consumer<ArrayListBuilder<Integer>>)}
 * both erase to {@code items(Consumer)} and would conflict. However, since Java doesn't allow
 * duplicate field names, this scenario shouldn't occur through normal field definitions.
 *
 * <p>A true conflict would require two setters with the same name setting different fields OR extra
 * helper methods in the DTO for setting the same field on different ways. The conflict resolution
 * mechanism exists as a safety net for edge cases and future feature additions.
 *
 * <p>These tests verify that:
 *
 * <ul>
 *   <li>Common field combinations don't accidentally create conflicts
 *   <li>The system compiles successfully (no duplicate method errors)
 *   <li>Specific methods exist/don't exist as expected (concrete assertions)
 *   <li>No spurious conflict warnings are generated for valid code
 * </ul>
 */
class MethodConflictResolutionTest {

  private Compilation compile(JavaFileObject... files) {
    return ProcessorTestUtils.createCompiler().compile(files);
  }

  @Test
  void shouldHandleMultipleFieldsWithoutConflicts() {
    // Given: Multiple fields with distinct names - no conflicts expected
    String packageName = "test";
    String className = "NoConflicts";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String firstName;
                private String lastName;
                private Integer age;
                private java.util.List<String> hobbies;

                public String getFirstName() { return firstName; }
                public void setFirstName(String firstName) { this.firstName = firstName; }

                public String getLastName() { return lastName; }
                public void setLastName(String lastName) { this.lastName = lastName; }

                public Integer getAge() { return age; }
                public void setAge(Integer age) { this.age = age; }

                public java.util.List<String> getHobbies() { return hobbies; }
                public void setHobbies(java.util.List<String> hobbies) { this.hobbies = hobbies; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Verify no conflict warnings
    long conflictWarningCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(d -> d.getMessage(null).contains("Method conflict"))
            .count();

    assert conflictWarningCount == 0
        : "Expected no method conflicts, but got " + conflictWarningCount + " conflict warnings";

    // Assert specific methods exist
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public NoConflictsBuilder firstName(String firstName)",
        "public NoConflictsBuilder lastName(String lastName)",
        "public NoConflictsBuilder age(Integer age)",
        "public NoConflictsBuilder hobbies(List<String> hobbies)");
  }

  @Test
  void shouldNotProduceConflictsForOptionalFields() {
    // Given: Optional fields alongside regular fields
    String packageName = "test";
    String className = "WithOptionals";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;
                private java.util.Optional<String> description;
                private java.util.Optional<Integer> count;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }

                public java.util.Optional<String> getDescription() { return description; }
                public void setDescription(java.util.Optional<String> description) { this.description = description; }

                public java.util.Optional<Integer> getCount() { return count; }
                public void setCount(java.util.Optional<Integer> count) { this.count = count; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Verify no warnings
    long conflictWarningCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(d -> d.getMessage(null).contains("Method conflict"))
            .count();

    assert conflictWarningCount == 0
        : "Expected no method conflicts for Optional fields, but got "
            + conflictWarningCount
            + " warnings";

    // Assert specific methods exist
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public WithOptionalsBuilder name(String name)",
        "public WithOptionalsBuilder description(Optional<String> description)",
        "public WithOptionalsBuilder description(String description)",
        "public WithOptionalsBuilder count(Optional<Integer> count)",
        "public WithOptionalsBuilder count(Integer count)");
  }

  @Test
  void shouldDetectConflictsBecauseOfHelperFunctionsAndGenerateWithoutError() {
    // Given: A DTO with a helper function that conflicts with auto-generated methods
    // - Field 'name' (String) will auto-generate: name(Supplier<String>) with priority 90
    // - Manual setter setName(Supplier<String>) will generate: name(Supplier<String>) with priority
    // 100
    // These have identical signatures, creating a REAL conflict that must be resolved
    String packageName = "test";
    String className = "WithHelperFunction";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }

                // Helper function that will conflict with auto-generated supplier method
                public void setName(java.util.function.Supplier<String> nameProvider) {
                    this.name = nameProvider.get();
                }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then - Should compile successfully
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, builderClassName);

    // Verify builder field conflict was also resolved
    long builderFieldConflictCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(d -> d.getMessage(null).contains("Builder field conflict"))
            .count();

    assert builderFieldConflictCount > 0
        : "Expected builder field conflict warning for duplicate 'name' fields with different types";

    // Assert specific methods exist in generated code
    // Note: Both fields try to create supplier methods with same signature:
    // - String field generates: name(Supplier<String>) with priority 80 (supplier method)
    // - Supplier field generates: name(Supplier<String>) with priority 80 (also supplier -
    // functional interface skips direct setter logic somehow)
    // After conflict resolution with equal priority, first occurrence wins
    // The surviving name(Supplier<String>) method comes from whichever field is processed first
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public WithHelperFunctionBuilder name(String name) { this.name = changedValue(name); return this; }",
        "public WithHelperFunctionBuilder name(Supplier<String> name) { this.nameSupplier = changedValue(name); return this; }");

    // Verify the build() method uses the original setter names
    String expectedBuildMethod =
        """
        @Override
        public WithHelperFunction build() {
          WithHelperFunction result = new WithHelperFunction();
          this.name.ifSet(result::setName);
          this.nameSupplier.ifSet(result::setName);
          return result;
        }
        """;
    ProcessorAsserts.assertContaining(generatedCode, expectedBuildMethod);

    // Print the conflict warnings for verification
    compilation.diagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
        .filter(
            d ->
                d.getMessage(null).contains("Method conflict")
                    || d.getMessage(null).contains("Builder field conflict"))
        .forEach(
            d -> System.out.println("✓ Conflict detected and resolved: " + d.getMessage(null)));
  }

  @Test
  void shouldDetectConflictWhenDtoHasMistakenSetterNames() {
    // Given: A DTO with a mistake - two different fields but same setter name
    // This is a user error, but we should handle it gracefully
    String packageName = "test";
    String className = "MistakenSetterNames";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String firstName;
                private java.util.Optional<String> lastName;

                public String getFirstName() { return firstName; }
                // Mistake: setter name doesn't match field name
                public void setName(String firstName) { this.firstName = firstName; }

                public java.util.Optional<String> getLastName() { return lastName; }
                // Mistake: setter name doesn't match field name (same name as above!)
                public void setName(java.util.Optional<String> lastName) { this.lastName = lastName; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then - Should compile successfully
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, builderClassName);

    // Should have emitted warnings about builder field conflicts
    long builderFieldConflictCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(d -> d.getMessage(null).contains("Builder field conflict"))
            .count();

    assert builderFieldConflictCount > 0
        : "Expected builder field conflict warning for duplicate 'name' fields";

    // Assert specific methods exist in generated code
    // Note: firstName, lastName fields have no generated methods because they have no getters
    // The two setName() methods create builder fields 'name' (String) and 'nameOptional'
    // (Optional<String>), but both methods are named 'name()' because they derive from the same
    // setter name
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public MistakenSetterNamesBuilder name(String name)",
        "public MistakenSetterNamesBuilder name(Optional<String> name)");

    // Comprehensive assertion showing the complete build() method structure
    // This demonstrates that both builder fields target different DTO fields:
    // - this.name (String) -> calls setName(String) -> sets firstName in DTO
    // - this.nameOptional (Optional<String>) -> calls setName(Optional<String>) -> sets lastName
    // in DTO
    String expectedBuildMethod =
        """
        @Override
        public MistakenSetterNames build() {
          MistakenSetterNames result = new MistakenSetterNames();
          this.name.ifSet(result::setName);
          this.nameOptional.ifSet(result::setName);
          return result;
        }
        """;
    ProcessorAsserts.assertContaining(generatedCode, expectedBuildMethod);

    // Print all conflict warnings
    compilation.diagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
        .filter(
            d ->
                d.getMessage(null).contains("Method conflict")
                    || d.getMessage(null).contains("Builder field conflict"))
        .forEach(
            d ->
                System.out.println(
                    "✓ User error detected (duplicate setter names): " + d.getMessage(null)));
  }

  @Test
  void shouldDropLowerPriorityMethodInFavorOfHigherPriority() {
    // Given: A DTO where auto-generated supplier method (priority 80) conflicts with
    // direct setter from Supplier field (priority 100)
    // This tests the path: method.getPriority() < existing.getPriority()
    String packageName = "test";
    String className = "PriorityConflict";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String value;
                private java.util.function.Supplier<String> valueSupplier;

                public String getValue() { return value; }
                public void setValue(String value) { this.value = value; }

                public java.util.function.Supplier<String> getValueSupplier() { return valueSupplier; }
                // Direct setter creates method with priority 100
                public void setValue(java.util.function.Supplier<String> valueSupplier) {
                    this.valueSupplier = valueSupplier;
                }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then - Should compile successfully
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, builderClassName);

    // Verify the conflict warning for lower priority method being dropped
    long lowerPriorityDroppedCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(
                d ->
                    d.getMessage(null).contains("Method conflict")
                        && d.getMessage(null).contains("dropped in favor of field"))
            .filter(d -> d.getMessage(null).contains("priority 80"))
            .filter(d -> d.getMessage(null).contains("priority 100"))
            .count();

    assert lowerPriorityDroppedCount > 0
        : "Expected warning about lower priority method (80) being dropped in favor of higher priority (100)";

    // Verify that only the higher priority method exists (the direct setter from Supplier field)
    // The auto-generated supplier method from String field should have been dropped
    ProcessorAsserts.assertContaining(
        generatedCode, "public PriorityConflictBuilder value(Supplier<String> value)");

    // Print the conflict warnings for verification
    compilation.diagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
        .filter(d -> d.getMessage(null).contains("Method conflict"))
        .forEach(
            d -> System.out.println("✓ Lower priority dropped correctly: " + d.getMessage(null)));
  }
}
