/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

/** Tests for null constraint handling in generated builders. */
class NullConstraintTest {

  private Compilation compileSources(JavaFileObject... sources) {
    return Compiler.javac().withProcessors(new BuilderProcessor()).compile(sources);
  }

  @Test
  void mandatoryField_validation_generatedInBuildMethod() {
    String packageName = "test.mandatory";

    JavaFileObject person =
        JavaFileObjects.forSourceString(
            packageName + ".Person",
            """
            package test.mandatory;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class Person {
              private final String name;
              private String email;

              public Person(String name) {
                this.name = name;
              }

              public String getName() { return name; }
              public String getEmail() { return email; }
              public void setEmail(String email) { this.email = email; }
            }
            """);

    Compilation compilation = compileSources(person);
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "PersonBuilder", generatedCode);

    // Verify mandatory field validation is generated
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("if (!this.name.isSet())"),
        contains(
            "throw new IllegalStateException(\"Required field 'name' must be set before calling build()\")"));
  }

  @Test
  void nonNullableConstructorField_validation_generatedInBuildMethod() {
    String packageName = "test.nonnull";

    JavaFileObject notNullAnnotation =
        JavaFileObjects.forSourceString(
            "jakarta.validation.constraints.NotNull",
            """
            package jakarta.validation.constraints;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface NotNull {
              String message() default "";
            }
            """);

    JavaFileObject user =
        JavaFileObjects.forSourceString(
            packageName + ".User",
            """
            package test.nonnull;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import jakarta.validation.constraints.NotNull;

            @SimpleBuilder
            public class User {
              private final String username;

              public User(@NotNull String username) {
                this.username = username;
              }

              public String getUsername() { return username; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, user);
    String generatedCode = loadGeneratedSource(compilation, "UserBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "UserBuilder", generatedCode);

    // Verify non-null validation is generated
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("if (this.username.isSet() && this.username.value() == null)"),
        contains(
            "throw new IllegalStateException(\"Field 'username' is marked as non-null but null value was provided\")"));
  }

  @Test
  void nonNullableSetterField_validation_generatedInBuildMethod() {
    String packageName = "test.setternonnull";

    JavaFileObject notNullAnnotation =
        JavaFileObjects.forSourceString(
            "jakarta.validation.constraints.NotNull",
            """
            package jakarta.validation.constraints;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface NotNull {
              String message() default "";
            }
            """);

    JavaFileObject product =
        JavaFileObjects.forSourceString(
            packageName + ".Product",
            """
            package test.setternonnull;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import jakarta.validation.constraints.NotNull;

            @SimpleBuilder
            public class Product {
              private String name;
              private String description;

              public String getName() { return name; }
              public void setName(@NotNull String name) { this.name = name; }

              public String getDescription() { return description; }
              public void setDescription(String description) { this.description = description; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, product);
    String generatedCode = loadGeneratedSource(compilation, "ProductBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ProductBuilder", generatedCode);

    // Verify non-null validation is generated for setter field
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("if (this.name.isSet() && this.name.value() == null)"),
        contains(
            "throw new IllegalStateException(\"Field 'name' is marked as non-null but null value was provided\")"));

    // Verify nullable field doesn't have validation
    ProcessorAsserts.assertNotContaining(
        generatedCode, "Field 'description' is marked as non-null");
  }

  @Test
  void combinedConstraints_mandatoryAndNonNull_bothValidationsGenerated() {
    String packageName = "test.combined";

    JavaFileObject nonNullAnnotation =
        JavaFileObjects.forSourceString(
            "org.jetbrains.annotations.NotNull",
            """
            package org.jetbrains.annotations;
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.CLASS)
            @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
            public @interface NotNull {
              String value() default "";
            }
            """);

    JavaFileObject account =
        JavaFileObjects.forSourceString(
            packageName + ".Account",
            """
            package test.combined;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.jetbrains.annotations.NotNull;

            @SimpleBuilder
            public class Account {
              private final String accountId;
              private final String ownerId;

              public Account(@NotNull String accountId, @NotNull String ownerId) {
                this.accountId = accountId;
                this.ownerId = ownerId;
              }

              public String getAccountId() { return accountId; }
              public String getOwnerId() { return ownerId; }
            }
            """);

    Compilation compilation = compileSources(nonNullAnnotation, account);
    String generatedCode = loadGeneratedSource(compilation, "AccountBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "AccountBuilder", generatedCode);

    // Verify mandatory validation for both fields
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("if (!this.accountId.isSet())"),
        contains("Required field 'accountId' must be set before calling build()"),
        contains("if (!this.ownerId.isSet())"),
        contains("Required field 'ownerId' must be set before calling build()"));

    // Verify non-null validation for both fields
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("if (this.accountId.isSet() && this.accountId.value() == null)"),
        contains("Field 'accountId' is marked as non-null but null value was provided"),
        contains("if (this.ownerId.isSet() && this.ownerId.value() == null)"),
        contains("Field 'ownerId' is marked as non-null but null value was provided"));
  }

  @Test
  void differentNonNullAnnotations_allRecognized() {
    String packageName = "test.variants";

    // Test multiple annotation variants
    JavaFileObject jakartaNotNull =
        JavaFileObjects.forSourceString(
            "jakarta.validation.constraints.NotNull",
            """
            package jakarta.validation.constraints;
            import java.lang.annotation.*;
            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.PARAMETER})
            public @interface NotNull {}
            """);

    JavaFileObject lombokNonNull =
        JavaFileObjects.forSourceString(
            "lombok.NonNull",
            """
            package lombok;
            import java.lang.annotation.*;
            @Retention(RetentionPolicy.CLASS)
            @Target({ElementType.PARAMETER})
            public @interface NonNull {}
            """);

    JavaFileObject entity =
        JavaFileObjects.forSourceString(
            packageName + ".Entity",
            """
            package test.variants;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import jakarta.validation.constraints.NotNull;
            import lombok.NonNull;

            @SimpleBuilder
            public class Entity {
              private final String jakartaField;
              private String lombokField;

              public Entity(@NotNull String jakartaField) {
                this.jakartaField = jakartaField;
              }

              public String getJakartaField() { return jakartaField; }
              public String getLombokField() { return lombokField; }
              public void setLombokField(@NonNull String lombokField) {
                this.lombokField = lombokField;
              }
            }
            """);

    Compilation compilation = compileSources(jakartaNotNull, lombokNonNull, entity);
    String generatedCode = loadGeneratedSource(compilation, "EntityBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "EntityBuilder", generatedCode);

    // Verify both annotations are recognized
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("if (this.jakartaField.isSet() && this.jakartaField.value() == null)"),
        contains("Field 'jakartaField' is marked as non-null"),
        contains("if (this.lombokField.isSet() && this.lombokField.value() == null)"),
        contains("Field 'lombokField' is marked as non-null"));
  }
}
