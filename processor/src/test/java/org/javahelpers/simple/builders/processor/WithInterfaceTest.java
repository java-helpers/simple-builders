package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.printDiagnosticsOnVerbose;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

/** Tests for With interface generation in builders. */
class WithInterfaceTest {

  private Compilation compileSources(JavaFileObject... sources) {
    Compilation compilation = createCompiler().compile(sources);
    printDiagnosticsOnVerbose(compilation); // Print diagnostics when verbose mode is enabled
    return compilation;
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

    // Verify complete With interface is generated with default implementations
    String expectedWithInterface =
        """
        /**
         * Interface that can be implemented by the DTO to provide fluent modification methods.
         */
        public interface With {
            /**
             * Applies modifications to a builder initialized from this instance and returns the built object.
             *
             * @param b the consumer to apply modifications
             * @return the modified instance
             */
            default Project with(Consumer<ProjectBuilder> b) {
                ProjectBuilder builder;
                try {
                    builder = new ProjectBuilder(Project.class.cast(this));
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException("The interface 'ProjectBuilder.With' should only be implemented by classes, which could be casted to 'Project'", ex);
                }
                b.accept(builder);
                return builder.build();
            }

            /**
             * Creates a builder initialized from this instance.
             *
             * @return a builder initialized with this instance's values
             */
            default ProjectBuilder with() {
                try {
                    return new ProjectBuilder(Project.class.cast(this));
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException("The interface 'ProjectBuilder.With' should only be implemented by classes, which could be casted to 'Project'", ex);
                }
            }
        }
        """;

    ProcessorAsserts.assertingResult(generatedCode, contains(expectedWithInterface));
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

    // Verify complete With interface with default implementations for constructor fields
    String expectedWithInterface =
        """
        /**
         * Interface that can be implemented by the DTO to provide fluent modification methods.
         */
        public interface With {
            /**
             * Applies modifications to a builder initialized from this instance and returns the built object.
             *
             * @param b the consumer to apply modifications
             * @return the modified instance
             */
            default User with(Consumer<UserBuilder> b) {
                UserBuilder builder;
                try {
                    builder = new UserBuilder(User.class.cast(this));
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException("The interface 'UserBuilder.With' should only be implemented by classes, which could be casted to 'User'", ex);
                }
                b.accept(builder);
                return builder.build();
            }

            /**
             * Creates a builder initialized from this instance.
             *
             * @return a builder initialized with this instance's values
             */
            default UserBuilder with() {
                try {
                    return new UserBuilder(User.class.cast(this));
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException("The interface 'UserBuilder.With' should only be implemented by classes, which could be casted to 'User'", ex);
                }
            }
        }
        """;

    ProcessorAsserts.assertingResult(generatedCode, contains(expectedWithInterface));
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

    // Verify complete With interface with correct type names (primitives)
    String expectedWithInterface =
        """
        /**
         * Interface that can be implemented by the DTO to provide fluent modification methods.
         */
        public interface With {
            /**
             * Applies modifications to a builder initialized from this instance and returns the built object.
             *
             * @param b the consumer to apply modifications
             * @return the modified instance
             */
            default Config with(Consumer<ConfigBuilder> b) {
                ConfigBuilder builder;
                try {
                    builder = new ConfigBuilder(Config.class.cast(this));
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException("The interface 'ConfigBuilder.With' should only be implemented by classes, which could be casted to 'Config'", ex);
                }
                b.accept(builder);
                return builder.build();
            }

            /**
             * Creates a builder initialized from this instance.
             *
             * @return a builder initialized with this instance's values
             */
            default ConfigBuilder with() {
                try {
                    return new ConfigBuilder(Config.class.cast(this));
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException("The interface 'ConfigBuilder.With' should only be implemented by classes, which could be casted to 'Config'", ex);
                }
            }
        }
        """;

    ProcessorAsserts.assertingResult(generatedCode, contains(expectedWithInterface));
  }
}
