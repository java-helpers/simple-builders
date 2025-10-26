package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

/** Tests for conditional execution feature in generated builders. */
class ConditionalExecutionTest {

  private Compilation compileSources(JavaFileObject... sources) {
    return Compiler.javac().withProcessors(new BuilderProcessor()).compile(sources);
  }

  @Test
  void conditionalMethod_generatedInBuilder() {
    String packageName = "test.conditional";

    JavaFileObject person =
        JavaFileObjects.forSourceString(
            packageName + ".Person",
            """
            package test.conditional;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Person {
              private String name;
              private int age;

              public String getName() { return name; }
              public void setName(String name) { this.name = name; }
              public int getAge() { return age; }
              public void setAge(int age) { this.age = age; }
            }
            """);

    Compilation compilation = compileSources(person);
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "PersonBuilder", generatedCode);

    // Verify complete method signature with exact types
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public PersonBuilder conditional(BooleanSupplier condition, Consumer<PersonBuilder> trueCase, Consumer<PersonBuilder> falseCase) {
        """);

    // Verify Javadoc is present and complete
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("Conditionally applies builder modifications based on a condition"),
        contains("@param condition the condition to evaluate"),
        contains("@param trueCase the consumer to apply if condition is true"),
        contains("@param falseCase the consumer to apply if condition is false (can be null)"),
        contains("@return this builder instance"));

    // Verify null-safe implementation for falseCase
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        if (condition.getAsBoolean()) {
            trueCase.accept(this);
        } else if (falseCase != null) {
            falseCase.accept(this);
        }
        """);

    // Verify imports are present
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("import java.util.function.BooleanSupplier"),
        contains("import java.util.function.Consumer"));
  }

  @Test
  void conditionalMethod_worksWithConstructorFields() {
    String packageName = "test.conditional.constructor";

    JavaFileObject user =
        JavaFileObjects.forSourceString(
            packageName + ".User",
            """
            package test.conditional.constructor;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class User {
              private final String username;
              private String displayName;

              public User(String username) {
                this.username = username;
              }

              public String getUsername() { return username; }
              public String getDisplayName() { return displayName; }
              public void setDisplayName(String displayName) { this.displayName = displayName; }
            }
            """);

    Compilation compilation = compileSources(user);
    String generatedCode = loadGeneratedSource(compilation, "UserBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "UserBuilder", generatedCode);

    // Verify conditional method has correct signature for UserBuilder
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public UserBuilder conditional(BooleanSupplier condition, Consumer<UserBuilder> trueCase, Consumer<UserBuilder> falseCase) {
        """);

    // Verify the method body is correctly generated with null-safe falseCase
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        if (condition.getAsBoolean()) {
            trueCase.accept(this);
        } else if (falseCase != null) {
            falseCase.accept(this);
        }
        return this;
        """);

    // Verify that conditional works alongside constructor field methods
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public UserBuilder username(String username)"),
        contains("public UserBuilder displayName(String displayName)"),
        contains("public UserBuilder conditional("));
  }

  @Test
  void conditionalMethod_returnsCorrectBuilderType() {
    String packageName = "test.conditional.returntype";

    JavaFileObject config =
        JavaFileObjects.forSourceString(
            packageName + ".Config",
            """
            package test.conditional.returntype;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Config {
              private boolean enabled;
              private int timeout;

              public boolean isEnabled() { return enabled; }
              public void setEnabled(boolean enabled) { this.enabled = enabled; }
              public int getTimeout() { return timeout; }
              public void setTimeout(int timeout) { this.timeout = timeout; }
            }
            """);

    Compilation compilation = compileSources(config);
    String generatedCode = loadGeneratedSource(compilation, "ConfigBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ConfigBuilder", generatedCode);

    // Verify complete signature with ConfigBuilder return type
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public ConfigBuilder conditional(BooleanSupplier condition, Consumer<ConfigBuilder> trueCase, Consumer<ConfigBuilder> falseCase) {
        """);

    // Verify return type is ConfigBuilder for method chaining
    ProcessorAsserts.assertingResult(
        generatedCode, contains("public ConfigBuilder conditional("), contains("return this;"));

    // Verify all consumer parameters are typed with ConfigBuilder
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("Consumer<ConfigBuilder> trueCase"),
        contains("Consumer<ConfigBuilder> falseCase"));

    // Verify method can be chained with other builder methods
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public ConfigBuilder enabled(boolean enabled)"),
        contains("public ConfigBuilder timeout(int timeout)"),
        contains("public ConfigBuilder conditional("));
  }

  @Test
  void conditionalMethod_positiveOnlyOverload_generated() {
    String packageName = "test.conditional.positiveonly";

    JavaFileObject settings =
        JavaFileObjects.forSourceString(
            packageName + ".Settings",
            """
            package test.conditional.positiveonly;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Settings {
              private String theme;
              private boolean darkMode;

              public String getTheme() { return theme; }
              public void setTheme(String theme) { this.theme = theme; }
              public boolean isDarkMode() { return darkMode; }
              public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
            }
            """);

    Compilation compilation = compileSources(settings);
    String generatedCode = loadGeneratedSource(compilation, "SettingsBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "SettingsBuilder", generatedCode);

    // Verify positive-only overload signature
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public SettingsBuilder conditional(BooleanSupplier condition, Consumer<SettingsBuilder> yesCondition) {
        """);

    // Verify positive-only overload delegates to full method
    ProcessorAsserts.assertContaining(
        generatedCode, "return conditional(condition, yesCondition, null);");

    // Verify positive-only Javadoc
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("Conditionally applies builder modifications if the condition is true"),
        contains("@param condition the condition to evaluate"),
        contains("@param yesCondition the consumer to apply if condition is true"));

    // Verify both overloads exist (2-parameter and 3-parameter)
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains(
            "public SettingsBuilder conditional(BooleanSupplier condition, Consumer<SettingsBuilder> yesCondition)"),
        contains(
            "public SettingsBuilder conditional(BooleanSupplier condition, Consumer<SettingsBuilder> trueCase, Consumer<SettingsBuilder> falseCase)"));
  }
}
