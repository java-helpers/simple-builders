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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for toString method generation in builders.
 *
 * <p>Verifies that:
 *
 * <ul>
 *   <li>toString method is generated with fluent ToStringBuilder API
 *   <li>Custom BuilderToStringStyle is used
 *   <li>All fields are included in the toString output
 *   <li>The generated code compiles successfully
 * </ul>
 */
class ToStringGenerationTest {

  @Test
  void toString_generatedWithFluentAPI() {
    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class PersonDto {
                private String name;
                private int age;
                private String email;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public int getAge() { return age; }
                public void setAge(int age) { this.age = age; }
                public String getEmail() { return email; }
                public void setEmail(String email) { this.email = email; }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(dtoSource);

    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "PersonDtoBuilder");

    // Verify toString method exists
    assertTrue(
        generatedCode.contains("public String toString()"), "toString method should be generated");

    // Verify it uses BuilderToStringStyle
    assertTrue(
        generatedCode.contains("BuilderToStringStyle.INSTANCE"),
        "Should use BuilderToStringStyle.INSTANCE");

    // Verify it uses ToStringBuilder
    assertTrue(
        generatedCode.contains("new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)"),
        "Should create ToStringBuilder with custom style");

    // Verify fluent API with all fields
    assertTrue(generatedCode.contains(".append(\"name\", this.name)"), "Should append name field");
    assertTrue(generatedCode.contains(".append(\"age\", this.age)"), "Should append age field");
    assertTrue(
        generatedCode.contains(".append(\"email\", this.email)"), "Should append email field");

    // Verify method ends with .toString()
    assertTrue(generatedCode.contains(".toString();"), "Should end with .toString() call");

    // Verify imports
    assertTrue(
        generatedCode.contains("import org.apache.commons.lang3.builder.ToStringBuilder;"),
        "Should import ToStringBuilder");
    assertTrue(
        generatedCode.contains(
            "import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;"),
        "Should import BuilderToStringStyle");
  }

  @Test
  void toString_includesConstructorAndSetterFields() {
    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class BookDto {
                private String title;
                private String author;
                private int pages;

                public BookDto(String title, String author) {
                    this.title = title;
                    this.author = author;
                }

                public String getTitle() { return title; }
                public String getAuthor() { return author; }
                public int getPages() { return pages; }
                public void setPages(int pages) { this.pages = pages; }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(dtoSource);

    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "BookDtoBuilder");

    // Verify constructor fields are included
    assertTrue(
        generatedCode.contains(".append(\"title\", this.title)"),
        "Should append constructor field: title");
    assertTrue(
        generatedCode.contains(".append(\"author\", this.author)"),
        "Should append constructor field: author");

    // Verify setter fields are included
    assertTrue(
        generatedCode.contains(".append(\"pages\", this.pages)"),
        "Should append setter field: pages");
  }

  @Test
  void toString_generatedCodeStructure() {
    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class ProductDto {
                private String name;
                private double price;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public double getPrice() { return price; }
                public void setPrice(double price) { this.price = price; }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(dtoSource);

    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "ProductDtoBuilder");

    // Verify the complete structure matches expected pattern
    String expectedPattern =
        """
        @Override
        public String toString() {
          return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)
              .append("name", this.name)
              .append("price", this.price)
              .toString();
        }
        """;

    // Normalize whitespace for comparison
    String normalizedGenerated = generatedCode.replaceAll("\\s+", " ").trim();
    String normalizedExpected = expectedPattern.replaceAll("\\s+", " ").trim();

    assertTrue(
        normalizedGenerated.contains(normalizedExpected),
        "Generated toString method should match expected structure.\nExpected pattern: "
            + normalizedExpected
            + "\nGenerated code: "
            + normalizedGenerated);
  }

  @Test
  void toString_hasCorrectJavadoc() {
    JavaFileObject dtoSource =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class SimpleDto {
                private String value;
                public String getValue() { return value; }
                public void setValue(String value) { this.value = value; }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(dtoSource);

    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "SimpleDtoBuilder");

    // Verify javadoc is present
    assertTrue(
        generatedCode.contains(
            "Returns a string representation of this builder, including only fields that have been"
                + " set."),
        "Should have descriptive javadoc");
    assertTrue(
        generatedCode.contains("@return string representation of the builder"),
        "Should have @return tag in javadoc");
  }
}
