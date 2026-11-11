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

package org.javahelpers.codegen.analysis;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.processing.AnnotationProcessingContext;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link FieldAnnotationExtractor} against elements captured from a compilation. */
class FieldAnnotationExtractorTest {

  private static class ProbeProcessor extends AbstractProcessor {
    private final BiConsumer<RoundEnvironment, TypeMappingContext> probe;
    private boolean ran;

    ProbeProcessor(BiConsumer<RoundEnvironment, TypeMappingContext> probe) {
      this.probe = probe;
    }

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
      if (ran || roundEnv.processingOver()) {
        return false;
      }
      ran = true;
      probe.accept(
          roundEnv,
          new AnnotationProcessingContext(
              CapturingProcessingLogger.create().logger(), processingEnv));
      return false;
    }
  }

  private static VariableElement field(TypeElement dto, String fieldName) {
    return dto.getEnclosedElements().stream()
        .filter(VariableElement.class::isInstance)
        .map(VariableElement.class::cast)
        .filter(e -> e.getSimpleName().toString().equals(fieldName))
        .findFirst()
        .orElseThrow();
  }

  private static final String DTO_SOURCE =
      """
      package test;
      public class Dto {
        @Deprecated
        private String legacy;
        @test.Default("GENERAL")
        private String mode;
        @test.NonNull
        private String required;
        private String plain;
      }
      """;

  private static final String ANNOTATION_SOURCES =
      """
      package test;
      public @interface Default { String value(); }
      """;

  private static final String NONNULL_SOURCE =
      """
      package test;
      public @interface NonNull {}
      """;

  private Compilation compile(BiConsumer<RoundEnvironment, TypeMappingContext> probe) {
    return CodegenTestUtils.createCompiler(new ProbeProcessor(probe))
        .compile(
            CodegenTestUtils.forSource(DTO_SOURCE),
            CodegenTestUtils.forSource(ANNOTATION_SOURCES),
            CodegenTestUtils.forSource(NONNULL_SOURCE));
  }

  @Test
  void extractDeprecatedAnnotation_findsDeprecatedField() {
    AtomicReference<Optional<AnnotationDto>> result = new AtomicReference<>();
    Compilation compilation =
        compile(
            (roundEnv, ctx) ->
                result.set(
                    FieldAnnotationExtractor.extractDeprecatedAnnotation(
                        field(ctx.getTypeElement("test.Dto"), "legacy"), ctx)));

    assertThat(compilation).succeeded();
    assertTrue(result.get().isPresent());
    assertEquals(
        "java.lang.Deprecated",
        result.get().get().getAnnotationType().getPackageName()
            + "."
            + result.get().get().getAnnotationType().getClassName());
  }

  @Test
  void extractDeprecatedAnnotation_emptyForPlainField() {
    AtomicReference<Optional<AnnotationDto>> result = new AtomicReference<>();
    Compilation compilation =
        compile(
            (roundEnv, ctx) ->
                result.set(
                    FieldAnnotationExtractor.extractDeprecatedAnnotation(
                        field(ctx.getTypeElement("test.Dto"), "plain"), ctx)));

    assertThat(compilation).succeeded();
    assertTrue(result.get().isEmpty());
  }

  @Test
  void extractAnnotations_returnsMemberValues() {
    AtomicReference<List<AnnotationDto>> result = new AtomicReference<>();
    Compilation compilation =
        compile(
            (roundEnv, ctx) ->
                result.set(
                    FieldAnnotationExtractor.extractAnnotations(
                        field(ctx.getTypeElement("test.Dto"), "mode"), ctx)));

    assertThat(compilation).succeeded();
    assertEquals(1, result.get().size());
    AnnotationDto annotation = result.get().get(0);
    assertEquals("Default", annotation.getAnnotationType().getClassName());
    assertEquals("\"GENERAL\"", annotation.getMembers().get("value"));
  }

  @Test
  void hasNonNullConstraint_detectsNonNullAnnotation() {
    AtomicReference<Boolean> nonNull = new AtomicReference<>();
    AtomicReference<Boolean> plain = new AtomicReference<>();
    Compilation compilation =
        compile(
            (roundEnv, ctx) -> {
              TypeElement dto = ctx.getTypeElement("test.Dto");
              nonNull.set(FieldAnnotationExtractor.hasNonNullConstraint(field(dto, "required")));
              plain.set(FieldAnnotationExtractor.hasNonNullConstraint(field(dto, "plain")));
            });

    assertThat(compilation).succeeded();
    assertTrue(nonNull.get());
    assertFalse(plain.get());
  }

  @Test
  void extractAnnotationValue_matchesAnnotationBySimpleName() {
    AtomicReference<Optional<String>> result = new AtomicReference<>();
    Compilation compilation =
        compile(
            (roundEnv, ctx) ->
                result.set(
                    FieldAnnotationExtractor.extractAnnotationValue(
                        field(ctx.getTypeElement("test.Dto"), "mode"), Set.of("Default"))));

    assertThat(compilation).succeeded();
    assertEquals(Optional.of("GENERAL"), result.get());
  }

  @Test
  void formatDefaultExpression_quotesStringValues() {
    assertEquals(
        "\"GENERAL\"",
        FieldAnnotationExtractor.formatDefaultExpression("GENERAL", TypeName.of(String.class)));
  }

  @Test
  void formatDefaultExpression_qualifiesSimpleEnumConstant() {
    TypeName enumType = new TypeName("test", "ItemCondition");
    enumType.setEnumType(true);
    assertEquals(
        "ItemCondition.GOOD", FieldAnnotationExtractor.formatDefaultExpression("GOOD", enumType));
  }

  @Test
  void formatDefaultExpression_keepsComplexExpressions() {
    TypeName enumType = new TypeName("test", "ItemCondition");
    enumType.setEnumType(true);
    assertEquals(
        "ItemCondition.GOOD",
        FieldAnnotationExtractor.formatDefaultExpression("ItemCondition.GOOD", enumType));
    assertEquals(
        "List.of()",
        FieldAnnotationExtractor.formatDefaultExpression("List.of()", TypeName.of(List.class)));
  }
}
