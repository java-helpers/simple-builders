/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.codegen.testing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CodegenTestUtils} — the compile-testing helpers themselves. */
class CodegenTestUtilsTest {

  /** Emits one fixed generated source file so {@code loadGeneratedSource} has something to read. */
  private static class EmittingProcessor extends AbstractProcessor {
    private boolean ran;

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return Set.of("*");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      if (!ran && !roundEnv.processingOver()) {
        ran = true;
        try {
          var file = processingEnv.getFiler().createSourceFile("test.Generated");
          try (var writer = file.openWriter()) {
            writer.write("package test;\npublic class Generated {}\n");
          }
        } catch (java.io.IOException e) {
          throw new IllegalStateException(e);
        }
      }
      return false;
    }
  }

  @Test
  void forSource_derivesQualifiedNameFromSource() {
    JavaFileObject file =
        CodegenTestUtils.forSource(
            """
            package test.sub;
            public record Item(String value) {}
            """);

    // compile-testing names in-memory files after the derived qualified name
    assertTrue(file.getName().endsWith("Item.java"));
    assertTrue(file.getName().contains("test/sub"));
  }

  @Test
  void forSource_rejectsSourceWithoutTypeDeclaration() {
    assertThrows(
        IllegalArgumentException.class,
        () -> CodegenTestUtils.forSource("package test;\n// no type here\n"));
  }

  @Test
  void createMockAnnotation_producesCompilableAnnotation() {
    JavaFileObject annotation =
        CodegenTestUtils.createMockAnnotation(
            "test.anno", "Marker", "ElementType.FIELD", "String value();");

    Compilation compilation =
        CodegenTestUtils.createCompiler()
            .compile(
                annotation,
                CodegenTestUtils.forSource(
                    """
                    package test;
                    public class Dto {
                      @test.anno.Marker("x")
                      private String field;
                    }
                    """));

    assertThat(compilation).succeeded();
  }

  @Test
  void loadGeneratedSource_readsEmittedFile() {
    Compilation compilation =
        CodegenTestUtils.createCompiler(new EmittingProcessor())
            .compile(CodegenTestUtils.forSource("package test;\npublic class Dto {}\n"));

    assertThat(compilation).succeeded();
    assertEquals(
        "package test;\npublic class Generated {}\n",
        CodegenTestUtils.loadGeneratedSource(compilation, "Generated"));
  }

  @Test
  void loadGeneratedSource_failsForMissingFile() {
    Compilation compilation =
        CodegenTestUtils.createCompiler()
            .compile(CodegenTestUtils.forSource("package test;\npublic class Dto {}\n"));

    assertThat(compilation).succeeded();
    assertThrows(
        AssertionError.class, () -> CodegenTestUtils.loadGeneratedSource(compilation, "Missing"));
  }
}
