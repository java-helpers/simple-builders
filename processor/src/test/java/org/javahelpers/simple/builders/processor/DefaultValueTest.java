/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for default value support via {@code @Default} and third-party {@code @DefaultValue}
 * annotations.
 */
class DefaultValueTest {

  protected Compilation compile(JavaFileObject... sourceFiles) {
    return ProcessorTestUtils.createCompiler().compile(sourceFiles);
  }

  private static Stream<Arguments> constructorDefaultCases() {
    return Stream.of(
        Arguments.of(
            "ProductRecord",
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.Default;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record ProductRecord(
                String name,
                double price,
                @Default("GENERAL") String category) {}
            """,
            """
        public ProductRecord build() {
          if (!this.price.isSet()) {
            throw new IllegalStateException("Required field 'price' must be set before calling build()");
          }
          if (this.price.value() == null) {
            throw new IllegalStateException("Field 'price' is marked as non-null but null value was provided");
          }
          ProductRecord result = new ProductRecord(this.name.value(), this.price.value(), this.category.valueOr("GENERAL"));
          return result;
        }
        """),
        Arguments.of(
            "MetricRecord",
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.Default;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record MetricRecord(
                String name,
                @Default("0.0") double price,
                @Default("0") int quantity) {}
            """,
            """
         public MetricRecord build() {
           MetricRecord result = new MetricRecord(this.name.value(), this.price.valueOr(0.0), this.quantity.valueOr(0));
           return result;
         }
        """),
        Arguments.of(
            "PlainRecord",
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record PlainRecord(String name, Integer age) {}
            """,
            """
        public PlainRecord build() {
          PlainRecord result = new PlainRecord(this.name.value(), this.age.value());
          return result;
        }
        """));
  }

  @ParameterizedTest
  @MethodSource("constructorDefaultCases")
  void constructorDefaultCases_generateExpectedBuildMethod(
      String recordName, String source, String expectedBuildMethod) {
    String builderClassName = recordName + "Builder";
    Compilation compilation = compile(ProcessorTestUtils.forSource(source));
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(generatedCode, expectedBuildMethod);
  }

  /**
   * Verifies that a {@code @Default} annotation on a field in a setter-based class causes the
   * generated {@code build()} method to use {@code ifSet(result::setStatus).orElse("PENDING")}
   * instead of plain {@code ifSet(result::setStatus)}.
   *
   * <p>Also verifies the builder setter method for the defaulted field is still generated, so users
   * can override the default with an explicit value.
   */
  @Test
  void defaultAppliedWhenUnset_setterField_class() {
    String className = "OrderDto";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.Default;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class OrderDto {
              private String id;
              @Default("PENDING")
              private String status;

              public String getId() { return id; }
              public void setId(String id) { this.id = id; }
              public String getStatus() { return status; }
              public void setStatus(String status) { this.status = status; }
            }
            """);

    Compilation compilation = compile(sourceFile);
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // build() must use ifSet().orElse() with the quoted String default for the status field
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public OrderDto build() {
          OrderDto result = new OrderDto();
          this.id.ifSet(result::setId);
          this.status.ifSet(result::setStatus).orElse("PENDING");
          return result;
        }
        """);
    // Setter method for the defaulted field must still be generated
    ProcessorAsserts.assertContaining(
        generatedCode, "public OrderDtoBuilder status(String status)");
  }

  /**
   * Verifies that a setter-based class field <em>without</em> {@code @Default} generates plain
   * {@code ifSet(result::setStatus);} with no {@code .orElse()} call. This is a regression guard to
   * ensure defaults are not accidentally applied when not declared.
   */
  @Test
  void setterFieldWithoutDefault_usesIfSetOnly() {
    String className = "OrderDto";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class OrderDto {
              private String id;
              private String status;

              public String getId() { return id; }
              public void setId(String id) { this.id = id; }
              public String getStatus() { return status; }
              public void setStatus(String status) { this.status = status; }
            }
            """);

    Compilation compilation = compile(sourceFile);
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Without default, should use plain ifSet (no .orElse)
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public OrderDto build() {
          OrderDto result = new OrderDto();
          this.id.ifSet(result::setId);
          this.status.ifSet(result::setStatus);
          return result;
        }
        """);
  }

  // === Default + non-null interaction ===

  /**
   * Verifies the interaction between {@code @NotNull} and {@code @Default}:
   *
   * <ul>
   *   <li>A field with both {@code @NotNull} and {@code @Default} is <em>not</em> required (the
   *       default makes it optional), so no required-field validation is generated.
   *   <li>A field with only {@code @NotNull} (no {@code @Default}) remains required, so
   *       required-field validation is still generated.
   *   <li>The defaulted field uses {@code valueOr()} in the constructor call.
   * </ul>
   */
  @Test
  void defaultWithNonNull_skipsValidation() {
    String recordName = "RequiredRecord";
    String builderClassName = recordName + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.forSource(
            """
            package test;

            import jakarta.validation.constraints.NotNull;
            import org.javahelpers.simple.builders.core.annotations.Default;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record RequiredRecord(
                @NotNull @Default("UNKNOWN") String name,
                @NotNull String required) {}
            """);

    // Create mock for NotNull annotation
    JavaFileObject notNullMock =
        ProcessorTestUtils.createMockAnnotation(
            "jakarta.validation.constraints",
            "NotNull",
            "ElementType.FIELD, ElementType.PARAMETER");

    Compilation compilation = compile(notNullMock, sourceFile);
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // name has @Default → not required → no required-field validation
    // name uses valueOr with the default
    // required has no @Default → still required → validation present
    // required uses plain value() (no default)
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public RequiredRecord build() {
          if (!this.required.isSet()) {
            throw new IllegalStateException("Required field 'required' must be set before calling build()");
          }
          if (this.required.value() == null) {
            throw new IllegalStateException("Field 'required' is marked as non-null but null value was provided");
          }
          RequiredRecord result = new RequiredRecord(this.name.valueOr("UNKNOWN"), this.required.value());
          return result;
        }
        """);
  }

  // === Framework-agnostic detection ===

  /**
   * Verifies that the processor detects third-party annotations named {@code @DefaultValue} (e.g.,
   * Jakarta REST {@code jakarta.ws.rs.DefaultValue}) by simple name matching, not just our own
   * {@code @Default}. The generated code should use {@code valueOr()} with the annotation's value.
   */
  @Test
  void detectsThirdPartyDefaultValueAnnotation() {
    String recordName = "JakartaRecord";
    String builderClassName = recordName + "Builder";

    // Create a mock @DefaultValue annotation (simulating Jakarta REST)
    JavaFileObject defaultValueMock =
        ProcessorTestUtils.createMockAnnotation(
            "jakarta.ws.rs",
            "DefaultValue",
            "ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD",
            "String value();");

    JavaFileObject sourceFile =
        ProcessorTestUtils.forSource(
            """
            package test;

            import jakarta.ws.rs.DefaultValue;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record JakartaRecord(
                String name,
                @DefaultValue("FALLBACK") String category) {}
            """);

    Compilation compilation = compile(defaultValueMock, sourceFile);
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // "category" field detects @DefaultValue → generates valueOr with quoted default
    // "name" field is without @DefaultValue → must still use plain value()
    ProcessorAsserts.assertContaining(
        generatedCode,
        """
        public JakartaRecord build() {
          JakartaRecord result = new JakartaRecord(this.name.value(), this.category.valueOr("FALLBACK"));
          return result;
        }
        """);
  }
}
