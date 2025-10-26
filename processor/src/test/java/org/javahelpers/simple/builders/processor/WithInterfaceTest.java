package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

/** Tests for With interface generation in builders. */
class WithInterfaceTest {

  private Compilation compileSources(JavaFileObject... sources) {
    return Compiler.javac().withProcessors(new BuilderProcessor()).compile(sources);
  }

  @Test
  void withInterface_generatedInBuilder() {
    String packageName = "test.withinterface";

    JavaFileObject project =
        JavaFileObjects.forSourceString(
            packageName + ".Project",
            """
            package test.withinterface;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Project {
              private String name;
              private String description;

              public String getName() { return name; }
              public void setName(String name) { this.name = name; }
              public String getDescription() { return description; }
              public void setDescription(String description) { this.description = description; }
            }
            """);

    Compilation compilation = compileSources(project);
    String generatedCode = loadGeneratedSource(compilation, "ProjectBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ProjectBuilder", generatedCode);

    // Verify With interface is declared
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public interface With {"),
        contains(
            "Interface that can be implemented by the DTO to provide fluent modification methods"));

    // Verify first method with default implementation
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("default Project with(Consumer<ProjectBuilder> b)"),
        contains("Applies modifications to a builder initialized from this instance"),
        contains("@param b the consumer to apply modifications"),
        contains("@return the modified instance"),
        contains("ProjectBuilder builder = new ProjectBuilder((Project) this)"),
        contains("b.accept(builder)"),
        contains("return builder.build()"));

    // Verify second method with default implementation
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("default ProjectBuilder with()"),
        contains("Creates a builder initialized from this instance"),
        contains("@return a builder initialized with this instance's values"),
        contains("return new ProjectBuilder((Project) this)"));
  }

  @Test
  void withInterface_worksWithConstructorFields() {
    String packageName = "test.withinterface.constructor";

    JavaFileObject user =
        JavaFileObjects.forSourceString(
            packageName + ".User",
            """
            package test.withinterface.constructor;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class User {
              private final String username;
              private String email;

              public User(String username) {
                this.username = username;
              }

              public String getUsername() { return username; }
              public String getEmail() { return email; }
              public void setEmail(String email) { this.email = email; }
            }
            """);

    Compilation compilation = compileSources(user);
    String generatedCode = loadGeneratedSource(compilation, "UserBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "UserBuilder", generatedCode);

    // Verify With interface exists with default implementations
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public interface With {"),
        contains("default User with(Consumer<UserBuilder> b)"),
        contains("default UserBuilder with()"),
        contains("return new UserBuilder((User) this)"));
  }

  @Test
  void withInterface_correctTypeNames() {
    String packageName = "test.withinterface.types";

    JavaFileObject config =
        JavaFileObjects.forSourceString(
            packageName + ".Config",
            """
            package test.withinterface.types;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Config {
              private int timeout;
              private boolean enabled;

              public int getTimeout() { return timeout; }
              public void setTimeout(int timeout) { this.timeout = timeout; }
              public boolean isEnabled() { return enabled; }
              public void setEnabled(boolean enabled) { this.enabled = enabled; }
            }
            """);

    Compilation compilation = compileSources(config);
    String generatedCode = loadGeneratedSource(compilation, "ConfigBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ConfigBuilder", generatedCode);

    // Verify return types match the correct classes with default implementations
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("default Config with(Consumer<ConfigBuilder> b)"),
        contains("default ConfigBuilder with()"),
        contains("return new ConfigBuilder((Config) this)"));
  }
}
