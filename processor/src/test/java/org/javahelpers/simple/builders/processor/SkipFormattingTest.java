/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation (the "Software"), to deal
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

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@code skipFormatting} compiler option.
 *
 * <p>Verifies that when {@code -Asimplebuilder.skipFormatting=true} is set:
 *
 * <ul>
 *   <li>The generated code compiles successfully
 *   <li>Tab indentation is converted to 2-space indentation
 *   <li>Javadoc body lines have proper {@code " * "} prefixes
 *   <li>Import statements are not concatenated with javadoc {@code /**}
 *   <li>Consecutive blank lines are collapsed to one
 *   <li>The generated code structure matches the expected lightweight-formatted output
 * </ul>
 */
class SkipFormattingTest {

  @Test
  void skipFormatting_producesValidCodeWithLightweightFormatting() {
    JavaFileObject sourceFile =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class FormatTestDto {
              private String name;
              private int count;

              public FormatTestDto(String name, int count) {
                this.name = name;
                this.count = count;
              }

              public String getName() {
                return name;
              }

              public int getCount() {
                return count;
              }
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.skipFormatting=true")
            .compile(sourceFile);

    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "FormatTestDtoBuilder");

    // Normalize trailing whitespace per line for text block comparison (lightweight formatter
    // may leave trailing spaces on blank javadoc lines, but text blocks strip them)
    generatedCode =
        java.util.Arrays.stream(generatedCode.split("\n", -1))
            .map(String::stripTrailing)
            .collect(java.util.stream.Collectors.joining("\n"));

    // Verify 2-space indentation (no tabs)
    org.junit.jupiter.api.Assertions.assertFalse(
        generatedCode.contains("\t"),
        "Generated code should not contain tab characters when skipFormatting is enabled");

    // Verify import is not concatenated with /** (should be on separate lines)
    org.junit.jupiter.api.Assertions.assertFalse(
        generatedCode.contains(";/**"),
        "Import statements should not be concatenated with javadoc '/**' — expected newline between them");

    // Verify no consecutive blank lines
    org.junit.jupiter.api.Assertions.assertFalse(
        generatedCode.contains("\n\n\n"),
        "Generated code should not have consecutive blank lines (collapsed to one by lightweight formatter)");

    // Verify javadoc body lines have " * " prefix
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(" * Builder for {@code test.FormatTestDto}."),
        "Class-level javadoc should have ' * ' prefix on body lines");

    // Verify field-level javadoc has proper indentation and asterisk
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains("  /**\n   * Tracked value for <code>name</code>"),
        "Field-level javadoc should be indented with 2 spaces and have ' * ' prefix");

    // Verify specific formatting properties using text block snippets

    // 1. Import and class-level javadoc: import should NOT be concatenated with /**
    //    (lightweight formatter splits "import ...;/**" into separate lines)
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            """
            import org.javahelpers.simple.builders.core.util.TrackedValue;
            /**
             * Builder for {@code test.FormatTestDto}.
            """),
        "Last import should be followed by '/**' on its own line (not concatenated)");

    // 2. Class-level javadoc body lines should have " * " prefix with correct indentation
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            """
             * This builder provides a fluent API for creating instances of test.FormatTestDto with
             * method chaining and validation. Use the static {@code create()} method
            """),
        "Class-level javadoc body lines should have ' * ' prefix");

    // 3. Field-level javadoc should be indented with 2 spaces and have " * " prefix
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            """
              /**
               * Tracked value for <code>name</code>: name.
               */
              private TrackedValue<String> name = unsetValue();
            """),
        "Field-level javadoc should be indented with 2 spaces and have ' * ' prefix");

    // 4. Method-level javadoc should have proper indentation and " * " prefixes
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            """
              /**
               * Sets the value for <code>name</code>.
               * <p>Generated from parameter in constructor {@link FormatTestDto#FormatTestDto(String, int) FormatTestDto(String name, int count)}

               * <h4>Example:</h4><pre>{@code
               * builder.name("example value");
               * }</pre>
               * @param name name
               * @return current instance of builder
               */
              public FormatTestDtoBuilder name(String name) {
            """),
        "Method-level javadoc should have proper 2-space indentation and ' * ' prefixes");

    // 5. The create() method javadoc should have proper formatting
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            """
              /**
               * Creating a new builder for {@code test.FormatTestDto}.

               * <h4>Example:</h4><pre>{@code
               * FormatTestDtoBuilder builder = FormatTestDtoBuilder.create();
               * }</pre>
               * @return builder for {@code test.FormatTestDto}
               */
              public static FormatTestDtoBuilder create() {
            """),
        "create() method javadoc should have proper formatting with ' * ' prefixes");

    // 6. Build method should be present with javadoc
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            """
              @Override
              public FormatTestDto build() {
            """),
        "build() method should be present with @Override annotation");

    // 7. With interface should be present at the end
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains("public interface With {"), "With interface should be generated");

    // 8. Class annotations should be present
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains("@Generated("), "@Generated annotation should be present");
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains("@BuilderImplementation("),
        "@BuilderImplementation annotation should be present");
  }

  @Test
  void skipFormatting_disabledByDefault_usesEclipseFormatter() {
    JavaFileObject sourceFile =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class DefaultFormatDto {
              private String value;

              public DefaultFormatDto(String value) {
                this.value = value;
              }

              public String getValue() {
                return value;
              }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(sourceFile);

    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "DefaultFormatDtoBuilder");

    // When formatting is NOT skipped, the Eclipse formatter adds a blank line between
    // the last import and the class-level javadoc (Roaster's unformatted output concatenates them)
    org.junit.jupiter.api.Assertions.assertTrue(
        generatedCode.contains(
            "import org.javahelpers.simple.builders.core.util.TrackedValue;\n\n/**"),
        "Formatted output should have a blank line between last import and class javadoc");

    // Verify no tabs in formatted output either (Eclipse formatter uses spaces)
    org.junit.jupiter.api.Assertions.assertFalse(
        generatedCode.contains("\t"), "Formatted output should not contain tab characters");
  }
}
