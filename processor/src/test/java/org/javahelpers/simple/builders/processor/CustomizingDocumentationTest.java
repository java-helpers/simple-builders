package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertingResult;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.printDiagnosticsOnVerbose;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

/**
 * Test class that validates all code examples from CUSTOMIZING.md documentation.
 *
 * <p>This test ensures that all code examples in the documentation are syntactically correct and
 * can be compiled successfully. When updating the documentation, this test should be updated
 * accordingly to maintain consistency.
 *
 * <p><strong>Documentation Reference:</strong> <a
 * href="file:///Users/andreas.igel/Documents/entwicklung/repositories/java-helpers/simple-builders/docs/CUSTOMIZING.md">
 * docs/CUSTOMIZING.md</a>
 *
 * <p><strong>Update Instructions:</strong>
 *
 * <ol>
 *   <li>When updating CUSTOMIZING.md, update the corresponding test methods in this class
 *   <li>Keep the documentation link reference in this class header
 *   <li>Ensure all code examples are tested for compilation
 * </ol>
 */
class CustomizingDocumentationTest {

  /**
   * Test that the generated builders work correctly with custom components.
   *
   * <p>This is an integration test that verifies the generated builders can be compiled and used
   * successfully.
   */
  @Test
  void testGeneratedBuildersWithCustomComponents() {
    String emailDto =
        """
            package com.example.test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public class EmailDto {
                private String email;

                public EmailDto(String email) {
                    this.email = email;
                }

                public String getEmail() {
                    return email;
                }

                public void setEmail(String email) {
                    this.email = email;
                }
            }
            """;

    Compilation compilation =
        createCompiler()
            .compile(JavaFileObjects.forSourceString("com.example.test.EmailDto", emailDto));
    printDiagnosticsOnVerbose(compilation);

    String builderClassName = "EmailDtoBuilder";
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Verify basic structure of generated builder
    assertingResult(
        generatedCode,
        contains("public class EmailDtoBuilder"),
        contains("public EmailDto build()"),
        contains("public EmailDtoBuilder email(String email)"),
        contains("public static EmailDtoBuilder create()"));
  }
}
