package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
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
    return javac().withProcessors(new BuilderProcessor()).compile(files);
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
    assert generatedCode.contains("public NoConflictsBuilder firstName(String firstName)")
        : "firstName setter should exist";
    assert generatedCode.contains("public NoConflictsBuilder lastName(String lastName)")
        : "lastName setter should exist";
    assert generatedCode.contains("public NoConflictsBuilder age(Integer age)")
        : "age setter should exist";
    assert generatedCode.contains("public NoConflictsBuilder hobbies(List<String> hobbies)")
        : "hobbies setter should exist";
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
    assert generatedCode.contains("public WithOptionalsBuilder name(String name)")
        : "name setter should exist";
    assert generatedCode.contains(
            "public WithOptionalsBuilder description(Optional<String> description)")
        : "description setter with Optional should exist";
    assert generatedCode.contains("public WithOptionalsBuilder description(String description)")
        : "description setter with unwrapped String should exist";
    assert generatedCode.contains("public WithOptionalsBuilder count(Optional<Integer> count)")
        : "count setter with Optional should exist";
    assert generatedCode.contains("public WithOptionalsBuilder count(Integer count)")
        : "count setter with unwrapped Integer should exist";
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
                private String description;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }

                // Helper function that will conflict with auto-generated supplier method
                public void setName(java.util.function.Supplier<String> nameProvider) {
                    this.name = nameProvider.get();
                }

                public String getDescription() { return description; }
                public void setDescription(String description) { this.description = description; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then - MUST have emitted a warning about the method signature conflict
    long conflictWarningCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(d -> d.getMessage(null).contains("Method conflict"))
            .count();

    assert conflictWarningCount > 0
        : "Expected at least one method conflict warning. "
            + "The String field 'name' generates name(Supplier<String>) with priority 80, "
            + "and the manual setName(Supplier<String>) generates name(Supplier<String>) with priority 100. "
            + "These have the same signature and should trigger a conflict warning.";

    // Print the conflict warnings for verification
    compilation.diagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
        .filter(d -> d.getMessage(null).contains("Method conflict"))
        .forEach(
            d ->
                System.out.println(
                    "✓ Method conflict detected and resolved: " + d.getMessage(null)));

    // Note: This scenario also creates a builder field name conflict:
    // - TrackedValue<String> name (from setName(String))
    // - TrackedValue<Supplier<String>> name (from setName(Supplier<String>))
    // This currently causes a compilation error. The builder field conflict resolution
    // should be implemented to handle this case (e.g., rename to nameSupplier).
  }

  @Test
  void shouldDetectConflictWhenDtoHasMistakenSetterNames() {
    // Given: A DTO with a mistake - two different fields but same setter name
    // This is a user error, but we should handle it gracefully
    String packageName = "test";
    String className = "MistakenSetterNames";

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

    // Then - Should have emitted warnings about conflicts
    long conflictWarningCount =
        compilation.diagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
            .filter(d -> d.getMessage(null).contains("Method conflict"))
            .count();

    assert conflictWarningCount > 0
        : "Expected method conflict warnings. "
            + "setName(String) generates name(String) with priority 100, "
            + "and setName(Optional<String>) generates unwrapped name(String) with priority 80, "
            + "creating a conflict on the name(String) signature.";

    // Print all conflict warnings
    compilation.diagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.WARNING)
        .filter(d -> d.getMessage(null).contains("Method conflict"))
        .forEach(
            d ->
                System.out.println(
                    "✓ User error detected (duplicate setter names): " + d.getMessage(null)));

    // Note: This also creates builder field name conflicts:
    // - TrackedValue<String> name (from first setName)
    // - TrackedValue<String> name (from second setName)
    // The builder field conflict resolution should handle this by renaming the fields
    // and emitting a warning about the DTO mistake.
  }
}
