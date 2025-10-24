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
}
