package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadmeExampleTest {

  protected BuilderProcessor processor;
  protected Compiler compiler;

  @BeforeEach
  protected void setUp() {
    processor = new BuilderProcessor();
    compiler = Compiler.javac().withProcessors(processor);
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

    Compilation compilation = compiler.compile(person);
    Compilation compilationWithUsage = compiler.compile(person, usage);
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Fixed expected strings based on README usage snippet
    org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public static PersonBuilder create()"),
        contains("public PersonBuilder name(String name)"),
        contains("public PersonBuilder age(int age)"),
        contains("public PersonBuilder emailAddresses(String... emailAddresses)"),
        contains("public Person build()"));
        
     // Validate the full usage too
     assertGenerationSucceeded(compilationWithUsage);
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
            import java.util.Map;
            public class Usage {
              public static void main(){
                Project project = ProjectBuilder.create()
                    .name("Simple Builders with a bit of complexity")
                    .status(ProjectStatus.IN_PROGRESS)
                    .tasks(tasks -> tasks
                        .add(TaskBuilder.create()
                            .title("Implement core functionality")
                            .completed(true)
                            .build())
                        .add(TaskBuilder.create()
                            .title("Add documentation")
                            .description("Update README and add Javadocs")
                            .build())
                    )
                    .metadata(Map.of("version", "1.0.0", "owner", "dev-team"))
                    .build();
              }
            }
            """);

    Compilation compilation = compiler.compile(project, task, statusEnum);
    Compilation compilationWithUsage = compiler.compile(project, task, statusEnum, usage);

    String projectBuilder = loadGeneratedSource(compilation, "ProjectBuilder");
    assertGenerationSucceeded(compilation, "ProjectBuilder", projectBuilder);

    // Assert key capabilities corresponding to README usage
    org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertingResult(
        projectBuilder,
        contains("public static ProjectBuilder create()"),
        contains("public ProjectBuilder name(String name)"),
        contains(
            "public ProjectBuilder tasks(Consumer<ArrayListBuilder<Task>> tasksBuilderConsumer)"),
        contains("public ProjectBuilder metadata(HashMapBuilder<String, String> metadataBuilder)"),
        contains("public ProjectBuilder status(ProjectStatus status)"),
        contains("public Project build()"));

    // Also validate TaskBuilder exists and typical fluent API
    String taskBuilder = loadGeneratedSource(compilation, "TaskBuilder");
    assertGenerationSucceeded(compilation, "TaskBuilder", taskBuilder);
    org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertingResult(
        taskBuilder,
        contains("public static TaskBuilder create()"),
        contains("public TaskBuilder title(String title)"),
        contains("public TaskBuilder description(String description)"),
        contains("public TaskBuilder completed(boolean completed)"),
        contains("public Task build()"));
    
     // Validate the full usage too
     assertGenerationSucceeded(compilationWithUsage);
  }
}
