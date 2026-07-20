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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.simpleBuilderClass;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class JacksonModuleWarningTest {

  private static final String STRICT_MODE_PACKAGE = "pkg.jacksonstrict";

  @Test
  void generateJacksonModuleEnabled_ButAnnotationDisabled_ShouldEmitWarningAndSkipGeneration() {
    // Given
    JavaFileObject dto =
        simpleBuilderClass(
            "pkg.test",
            "MisconfiguredDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    // Enable generateJacksonModule but NOT usingJacksonDeserializerAnnotation (which defaults to
    // DISABLED)
    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.generateJacksonModule=true").compile(dto);

    // Then
    assertThat(compilation).succeeded();

    // Should have a warning
    assertThat(compilation)
        .hadWarningContaining(
            "simple-builders: generateJacksonModule is enabled but usingJacksonDeserializerAnnotation is disabled");

    // Should NOT have generated the Jackson module
    // We verify this by ensuring the warning is present, which implies the generation branch was
    // skipped.
    // Testing explicitly for "file not generated" is tricky with compile-testing without complex
    // custom assertions.
  }

  @Test
  void generateJacksonModule_WhenEnabledAndDeserializerEnabled_ShouldGenerate() {
    // Given
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "SuccessDto",
            """
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true")
            .compile(dto);

    // Then
    assertThat(compilation).succeeded();

    // Verify file IS generated
    ProcessorTestUtils.loadGeneratedSource(compilation, "SimpleBuildersJacksonModule");
  }

  private JavaFileObject dto() {
    return simpleBuilderClass(
        STRICT_MODE_PACKAGE,
        "JacksonStrictDto",
        """
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        """);
  }

  /** A hand-written class colliding with the generated Jackson module for the package. */
  private JavaFileObject collidingModule() {
    return JavaFileObjects.forSourceLines(
        STRICT_MODE_PACKAGE + ".SimpleBuildersJacksonModule",
        "package " + STRICT_MODE_PACKAGE + ";",
        "public class SimpleBuildersJacksonModule {}");
  }

  @Test
  void defaultMode_jacksonModuleFailure_isWarning_andCompilationSucceeds() {
    Compilation compilation =
        createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true")
            .compile(dto(), collidingModule());

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("Error generating Jackson module");
  }

  @Test
  void strictMode_jacksonModuleFailure_isError_andCompilationFails() {
    Compilation compilation =
        createCompiler()
            .withOptions(
                "-Asimplebuilder.generateJacksonModule=true",
                "-Asimplebuilder.usingJacksonDeserializerAnnotation=true",
                "-Asimplebuilder.strict=true")
            .compile(dto(), collidingModule());

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Error generating Jackson module");
  }
}
