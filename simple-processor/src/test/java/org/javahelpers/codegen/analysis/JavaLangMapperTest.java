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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.processing.AnnotationProcessingContext;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link JavaLangMapper} against elements captured from a real compilation. */
class JavaLangMapperTest {

  /** Runs {@code probe} once during the first processing round with a live processing context. */
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

  private static VariableElement setterParameter(
      TypeElement dto, String setterName, int paramIndex) {
    return dto.getEnclosedElements().stream()
        .filter(ExecutableElement.class::isInstance)
        .map(ExecutableElement.class::cast)
        .filter(m -> m.getSimpleName().toString().equals(setterName))
        .map(m -> m.getParameters().get(paramIndex))
        .findFirst()
        .orElseThrow();
  }

  @Test
  void map2TypeName_mapsJavaClass() {
    TypeName typeName = JavaLangMapper.map2TypeName(String.class);
    assertEquals("java.lang", typeName.getPackageName());
    assertEquals("String", typeName.getClassName());
  }

  @Test
  void mapToTypeName_mapsTypeElement() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, ctx) -> {
                      TypeElement dto = ctx.getTypeElement("test.Dto");
                      result.set(JavaLangMapper.mapToTypeName(dto, ctx));
                    }))
            .compile(
                CodegenTestUtils.forSource(
                    """
                    package test;
                    public class Dto {
                      private String name;
                      public void setName(String name) { this.name = name; }
                    }
                    """));

    assertThat(compilation).succeeded();
    assertEquals("test", result.get().getPackageName());
    assertEquals("Dto", result.get().getClassName());
  }

  @Test
  void map2MethodParameter_mapsNameAndListType() {
    AtomicReference<MethodParameterDto> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, ctx) -> {
                      TypeElement dto = ctx.getTypeElement("test.Dto");
                      VariableElement param = setterParameter(dto, "setTags", 0);
                      result.set(JavaLangMapper.map2MethodParameter(param, ctx));
                    }))
            .compile(
                CodegenTestUtils.forSource(
                    """
                    package test;
                    import java.util.List;
                    public class Dto {
                      private List<String> tags;
                      public void setTags(List<String> tags) { this.tags = tags; }
                    }
                    """));

    assertThat(compilation).succeeded();
    MethodParameterDto param = result.get();
    assertEquals("tags", param.getParameterName());
    TypeNameList typeName = assertInstanceOf(TypeNameList.class, param.getParameterType());
    assertEquals("java.util.List", typeName.getPackageName() + "." + typeName.getClassName());
    assertEquals("String", typeName.getElementType().getClassName());
  }

  @Test
  void createDerivedTypeName_appendsSuffix() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, ctx) -> {
                      TypeElement dto = ctx.getTypeElement("test.Dto");
                      result.set(JavaLangMapper.createDerivedTypeName(dto, ctx, "Builder"));
                    }))
            .compile(
                CodegenTestUtils.forSource(
                    """
                    package test;
                    public class Dto {}
                    """));

    assertThat(compilation).succeeded();
    assertEquals("test", result.get().getPackageName());
    assertEquals("DtoBuilder", result.get().getClassName());
  }

  @Test
  void extractInterfaceTypeArguments_resolvesInheritedListArgument() {
    // CustomList<X,Y> extends ArrayList<Y>: the List element type is Y, not the declared X/Y pair
    AtomicReference<List<TypeName>> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, ctx) -> {
                      TypeElement customList = ctx.getTypeElement("test.CustomList");
                      TypeElement listInterface = ctx.getTypeElement("java.util.List");
                      result.set(
                          JavaLangMapper.extractInterfaceTypeArguments(
                              customList.asType(), listInterface, ctx));
                    }))
            .compile(
                CodegenTestUtils.forSource(
                    """
                    package test;
                    import java.util.ArrayList;
                    public class CustomList<X, Y> extends ArrayList<Y> {}
                    """));

    assertThat(compilation).succeeded();
    assertEquals(1, result.get().size());
    assertEquals("Y", result.get().get(0).getClassName());
  }
}
