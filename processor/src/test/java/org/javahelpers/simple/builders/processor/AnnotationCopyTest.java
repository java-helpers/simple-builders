package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

/** Test that annotations from target class fields are copied to builder fields. */
class AnnotationCopyTest {

  private Compilation compileSources(JavaFileObject... sources) {
    BuilderProcessor processor = new BuilderProcessor();
    Compiler compiler = Compiler.javac().withProcessors(processor);
    return compiler.compile(sources);
  }

  @Test
  void annotations_copiedToBuilderFields() {
    String packageName = "test";

    JavaFileObject notNullAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".annotations.NotNull",
            """
            package test.annotations;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface NotNull {
            }
            """);

    JavaFileObject customAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".annotations.CustomAnnotation",
            """
            package test.annotations;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface CustomAnnotation {
              String value() default "";
            }
            """);

    JavaFileObject person =
        JavaFileObjects.forSourceString(
            packageName + ".Person",
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import test.annotations.NotNull;
            import test.annotations.CustomAnnotation;

            @SimpleBuilder
            public class Person {
              private String name;
              private String email;
              private int age;

              public String getName() { return name; }
              public void setName(@NotNull String name) { this.name = name; }

              public String getEmail() { return email; }
              public void setEmail(@CustomAnnotation("email-field") String email) { this.email = email; }

              public int getAge() { return age; }
              public void setAge(int age) { this.age = age; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, customAnnotation, person);
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "PersonBuilder", generatedCode);

    // Verify that annotations are copied to builder method parameters
    ProcessorAsserts.assertingResult(
        generatedCode,
        // NotNull annotation should be on the name method parameter
        contains("name(@NotNull String name)"),
        // CustomAnnotation should be on the email method parameter
        contains("email(@CustomAnnotation(\"email-field\") String email)"));
  }

  @Test
  void annotations_constructorParameters_copiedToBuilderFields() {
    String packageName = "test.annotations.constructor";

    JavaFileObject notNullAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".NotNull",
            """
            package test.annotations.constructor;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface NotNull {
            }
            """);

    JavaFileObject positiveAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".Positive",
            """
            package test.annotations.constructor;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface Positive {
            }
            """);

    JavaFileObject product =
        JavaFileObjects.forSourceString(
            packageName + ".Product",
            """
            package test.annotations.constructor;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Product {
              private final String name;
              private final double price;

              public Product(
                @NotNull String name,
                @Positive double price) {
                this.name = name;
                this.price = price;
              }

              public String getName() { return name; }
              public double getPrice() { return price; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, positiveAnnotation, product);
    String generatedCode = loadGeneratedSource(compilation, "ProductBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ProductBuilder", generatedCode);

    // Verify that annotations from constructor parameters are copied to builder method parameters
    ProcessorAsserts.assertingResult(
        generatedCode,
        // NotNull annotation should be on the name method parameter
        contains("name(@NotNull String name)"),
        // Positive annotation should be on the price method parameter
        contains("price(@Positive double price)"));
  }

  @Test
  void annotations_frameworkAnnotations_notCopied() {
    String packageName = "test.annotations.filtered";

    JavaFileObject notNullAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".NotNull",
            """
            package test.annotations.filtered;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface NotNull {
            }
            """);

    JavaFileObject person =
        JavaFileObjects.forSourceString(
            packageName + ".Person",
            """
            package test.annotations.filtered;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder;

            @SimpleBuilder
            public class Person {
              private String name;
              private String ignoredField;

              public String getName() { return name; }
              public void setName(@NotNull String name) { this.name = name; }

              public String getIgnoredField() { return ignoredField; }
              @IgnoreInBuilder
              public void setIgnoredField(String ignoredField) { this.ignoredField = ignoredField; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, person);
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "PersonBuilder", generatedCode);

    // Verify that NotNull annotation is copied to method parameters
    ProcessorAsserts.assertingResult(generatedCode, contains("name(@NotNull String name)"));

    // Verify that ignoredField is not in the builder (due to @IgnoreInBuilder)
    ProcessorAsserts.assertNotContaining(generatedCode, "ignoredField");
  }

  @Test
  void annotations_withComplexValues_copiedCorrectly() {
    String packageName = "test.complex";

    // Create an enum for testing
    JavaFileObject priorityEnum =
        JavaFileObjects.forSourceString(
            packageName + ".Priority",
            """
            package test.complex;
            public enum Priority {
              LOW, MEDIUM, HIGH
            }
            """);

    // Create a nested annotation
    JavaFileObject metadataAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".Metadata",
            """
            package test.complex;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;

            @Retention(RetentionPolicy.RUNTIME)
            public @interface Metadata {
              String author();
              int version();
            }
            """);

    // Create a complex annotation with various value types
    JavaFileObject complexAnnotation =
        JavaFileObjects.forSourceString(
            packageName + ".ComplexAnnotation",
            """
            package test.complex;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface ComplexAnnotation {
              // Primitives
              int intValue() default 42;
              long longValue() default 100L;
              boolean boolValue() default true;
              double doubleValue() default 3.14;

              // String
              String stringValue() default "default";

              // Enum
              Priority priority() default Priority.MEDIUM;

              // Class literal
              Class<?> type() default String.class;

              // Array
              String[] tags() default {};
              int[] numbers() default {};

              // Nested annotation
              Metadata metadata() default @Metadata(author = "unknown", version = 1);
            }
            """);

    JavaFileObject task =
        JavaFileObjects.forSourceString(
            packageName + ".Task",
            """
            package test.complex;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Task {
              private String name;
              private String description;
              private String simpleField;

              public String getName() { return name; }
              public void setName(
                @ComplexAnnotation(
                  intValue = 123,
                  longValue = 999L,
                  boolValue = false,
                  doubleValue = 2.71,
                  stringValue = "test-value",
                  priority = Priority.HIGH,
                  type = Task.class,
                  tags = {"urgent", "important"},
                  numbers = {1, 2, 3},
                  metadata = @Metadata(author = "John", version = 2)
                ) String name) {
                this.name = name;
              }

              public String getDescription() { return description; }
              public void setDescription(@ComplexAnnotation String description) {
                this.description = description;
              }

              public String getSimpleField() { return simpleField; }
              public void setSimpleField(
                @ComplexAnnotation(stringValue = "custom", priority = Priority.LOW, tags = "single")
                String simpleField) {
                this.simpleField = simpleField;
              }
            }
            """);

    Compilation compilation =
        compileSources(priorityEnum, metadataAnnotation, complexAnnotation, task);
    String generatedCode = loadGeneratedSource(compilation, "TaskBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "TaskBuilder", generatedCode);

    // Verify annotation with all value types is copied correctly
    ProcessorAsserts.assertingResult(
        generatedCode,
        // Method with full annotation containing all parameter types
        contains(
            """
            public TaskBuilder name(
                @ComplexAnnotation(intValue = 123, longValue = 999L, boolValue = false, doubleValue = 2.71, stringValue = "test-value", priority = test.complex.Priority.HIGH, type = test.complex.Task.class, tags = {"urgent", "important"}, numbers = {1, 2, 3}, metadata = @test.complex.Metadata(author="John", version=2)) String name)"""),
        // Method with annotation using only default values
        contains("public TaskBuilder description(@ComplexAnnotation String description)"),
        // Method with annotation with partial parameter override
        contains(
            """
            public TaskBuilder simpleField(
                @ComplexAnnotation(stringValue = "custom", priority = test.complex.Priority.LOW, tags = {"single"}) String simpleField)"""),
        // Format method should also have the full annotation
        contains(
            """
            public TaskBuilder name(
                @ComplexAnnotation(intValue = 123, longValue = 999L, boolValue = false, doubleValue = 2.71, stringValue = "test-value", priority = test.complex.Priority.HIGH, type = test.complex.Task.class, tags = {"urgent", "important"}, numbers = {1, 2, 3}, metadata = @test.complex.Metadata(author="John", version=2)) String format,"""),
        // Format method with partial override
        contains(
            """
            public TaskBuilder simpleField(
                @ComplexAnnotation(stringValue = "custom", priority = test.complex.Priority.LOW, tags = {"single"}) String format,"""));
  }
}
