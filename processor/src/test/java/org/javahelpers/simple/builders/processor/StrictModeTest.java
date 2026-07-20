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
import org.junit.jupiter.api.Test;

/**
 * Tests for the opt-in strict/fail-fast generation mode ({@code -Asimplebuilder.strict=true}).
 *
 * <p>A builder-generation failure is induced by providing a hand-written builder class with the
 * same name the processor would generate, which makes generation fail with a "Builder class already
 * exists" error. This exercises the failure path in a deterministic way.
 */
class StrictModeTest {

  private static final String PACKAGE = "pkg.strict";

  private JavaFileObject dto() {
    return simpleBuilderClass(
        PACKAGE,
        "StrictDto",
        """
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        """);
  }

  /** A hand-written builder colliding with the generated {@code StrictDtoBuilder}. */
  private JavaFileObject collidingBuilder() {
    return JavaFileObjects.forSourceLines(
        PACKAGE + ".StrictDtoBuilder",
        "package " + PACKAGE + ";",
        "public class StrictDtoBuilder {}");
  }

  @Test
  void defaultMode_generationFailure_isWarning_andCompilationSucceeds() {
    Compilation compilation = createCompiler().compile(dto(), collidingBuilder());

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("Failed to generate builder");
  }

  @Test
  void strictMode_generationFailure_isError_andCompilationFails() {
    Compilation compilation =
        createCompiler()
            .withOptions("-Asimplebuilder.strict=true")
            .compile(dto(), collidingBuilder());

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Failed to generate builder");
  }
}
