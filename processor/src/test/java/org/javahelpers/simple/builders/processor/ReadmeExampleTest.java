package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

class ReadmeExampleTest {

  private Compilation compileSources(JavaFileObject... sources) {
    BuilderProcessor processor = new BuilderProcessor();
    Compiler compiler = Compiler.javac().withProcessors(processor);
    return compiler.compile(sources);
  }

  @Test
  void basicUsage_personBuilder_example() {
    String packageName = "readme";
    String className = "Person";
    String builderClassName = className + "Builder";

    JavaFileObject person =
        JavaFileObjects.forSourceString(
            packageName + "." + className,
            """
            package readme;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import java.util.List;
            @SimpleBuilder
            public class Person {
              private String name;
              private int age;
              private List<String> emailAddresses;
              public String getName() { return name; }
              public void setName(String name) { this.name = name; }
              public int getAge() { return age; }
              public void setAge(int age) { this.age = age; }
              public List<String> getEmailAddresses() { return emailAddresses; }
              public void setEmailAddresses(List<String> emailAddresses) { this.emailAddresses = emailAddresses; }
            }
            """);

    JavaFileObject usage =
        JavaFileObjects.forSourceString(
            packageName + ".Usage",
            """
            package readme;
            public class Usage {
              public static void main(){
                Person person = PersonBuilder.create()
                           .name("John Doe")
                           .age(30)
                           .emailAddresses("john@example.com", "j.doe@example.com")
                           .build();
              }
            }
            """);

    // Test 1: Builder generation (without usage)
    Compilation compilation = compileSources(person);
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    ProcessorAsserts.assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Fixed expected strings based on README usage snippet
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public static PersonBuilder create()"),
        contains("public PersonBuilder name(String name)"),
        contains("public PersonBuilder age(int age)"),
        contains("public PersonBuilder emailAddresses(String... emailAddresses)"),
        contains("public Person build()"));

    // Test 2: Usage compilation (with usage)
    Compilation compilationWithUsage = compileSources(person, usage);

    // Validate the full usage too
    assertGenerationSucceeded(compilationWithUsage, "PersonBuilder");
  }

  @Test
  void collectionsAndNestedObjects_projectTask_example() {
    String packageName = "readme";

    JavaFileObject project =
        JavaFileObjects.forSourceString(
            packageName + ".Project",
            """
            package readme;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import java.util.List;
            import java.util.Map;
            @SimpleBuilder
            public class Project {
              private String name;
              private List<Task> tasks;
              private Map<String, String> metadata;
              private ProjectStatus status;
              public String getName() { return name; }
              public void setName(String name) { this.name = name; }
              public List<Task> getTasks() { return tasks; }
              public void setTasks(List<Task> tasks) { this.tasks = tasks; }
              public Map<String, String> getMetadata() { return metadata; }
              public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
              public ProjectStatus getStatus() { return status; }
              public void setStatus(ProjectStatus status) { this.status = status; }
            }
            """);

    JavaFileObject task =
        JavaFileObjects.forSourceString(
            packageName + ".Task",
            """
            package readme;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            @SimpleBuilder
            public class Task {
              private String title;
              private String description;
              private boolean completed;
              public String getTitle() { return title; }
              public void setTitle(String title) { this.title = title; }
              public String getDescription() { return description; }
              public void setDescription(String description) { this.description = description; }
              public boolean isCompleted() { return completed; }
              public void setCompleted(boolean completed) { this.completed = completed; }
            }
            """);

    JavaFileObject statusEnum =
        JavaFileObjects.forSourceString(
            packageName + ".ProjectStatus",
            """
            package readme;
            public enum ProjectStatus { PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD }
            """);

    JavaFileObject usage =
        JavaFileObjects.forSourceString(
            packageName + ".Usage",
            """
            package readme;
            public class Usage {
              public static void main(){
                String version = "1.0.0";
                Project project = ProjectBuilder.create()
                    .name("Simple Builders in version %s with a bit of complexity", version)
                    .status(ProjectStatus.IN_PROGRESS)
                    .tasks(tasks -> tasks
                        .add(taskBuilder -> taskBuilder
                            .title("Implement core functionality")
                            .completed(true)
                        )
                        .add(taskBuilder -> taskBuilder
                            .title("Add documentation")
                            .description("Update README and add Javadocs")
                        )
                    )
                    .metadata(metadata -> metadata
                        .put("version", "1.0.0")
                        .put("owner", "dev-team"))
                    .build();
              }
            }
            """);

    Compilation compilation = compileSources(project, task, statusEnum);

    String projectBuilder = loadGeneratedSource(compilation, "ProjectBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ProjectBuilder", projectBuilder);

    // Assert key capabilities corresponding to README usage
    ProcessorAsserts.assertingResult(
        projectBuilder,
        contains("public static ProjectBuilder create()"),
        contains("public ProjectBuilder name(String name)"),
        contains(
            "public ProjectBuilder tasks(Consumer<ArrayListBuilderWithElementBuilders<Task, TaskBuilder>> tasksBuilderConsumer)"),
        contains(
            "public ProjectBuilder metadata(Consumer<HashMapBuilder<String, String>> metadataBuilderConsumer"),
        contains("public ProjectBuilder status(ProjectStatus status)"),
        contains("public Project build()"));

    // Also validate TaskBuilder exists and typical fluent API
    String taskBuilder = loadGeneratedSource(compilation, "TaskBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "TaskBuilder", taskBuilder);
    ProcessorAsserts.assertingResult(
        taskBuilder,
        contains("public static TaskBuilder create()"),
        contains("public TaskBuilder title(String title)"),
        contains("public TaskBuilder description(String description)"),
        contains("public TaskBuilder completed(boolean completed)"),
        contains("public Task build()"));

    // Validate the full usage too
    Compilation compilationWithUsage = compileSources(project, task, statusEnum, usage);
    assertGenerationSucceeded(compilationWithUsage, "TaskBuilder");
  }

  /**
   * Asserts compilation succeeded and that the basic builder API exists in the provided generated
   * source (build() and static create()).
   */
  public static void assertGenerationSucceeded(Compilation compilation, String builderSimpleName) {
    ProcessorAsserts.assertGenerationSucceeded(
        compilation, builderSimpleName, loadGeneratedSource(compilation, builderSimpleName));
  }
}
