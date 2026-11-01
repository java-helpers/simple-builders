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
package org.javahelpers.codegen.processing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertNormalizedEquals;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.model.core.ClassFieldDto;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.AnnotationProcessingContext;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * A complete codegen processor in minimal form — only the domain members that have no generic
 * default are implemented; collection, round-driving, generator and formatter wiring come from
 * {@link AbstractCodegenProcessor}.
 */
public class MinimalCodegenProcessorTest {

  /** Marks a type for delegate-builder generation in this test. */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface GenerateBuilder {}

  /** Analysis result: target type, builder name and the setters to delegate to. */
  record MinimalTargetDefinition(
      TypeName sourceTypeName, TypeName builderTypeName, List<ExecutableElement> setters)
      implements CodegenDefinition {
    @Override
    public TypeName getTypeName() {
      return sourceTypeName;
    }

    @Override
    public TypeName targetTypeName() {
      return builderTypeName;
    }
  }

  /** A processor that generates a builder delegating to the target type's setters. */
  static class MinimalProcessor
      extends AbstractCodegenProcessor<
          AnnotationProcessingContext, CodegenConfiguration, MinimalTargetDefinition> {

    MinimalProcessor() {
      super(
          CodegenProcessorConfiguration.builder()
              .elementCollector(ElementCollector.builder().selector(GenerateBuilder.class).build())
              .loggerPrefix("minimal")
              .build());
    }

    @Override
    protected AnnotationProcessingContext createContext(
        ProcessingEnvironment processingEnv,
        CompilerArgumentsReader reader,
        ProcessingLogger logger) {
      return new AnnotationProcessingContext(logger, processingEnv);
    }

    @Override
    protected List<CodegenTask<CodegenConfiguration>> planTasks(
        ElementGroups collected, AnnotationProcessingContext ctx) {
      return collected.all().stream()
          .map(
              element ->
                  CodegenTask.builder(CodegenConfiguration.class)
                      .element(element)
                      .outputPackage(ctx.getPackageName(element))
                      .build())
          .toList();
    }

    @Override
    protected List<MinimalTargetDefinition> analyse(
        CodegenTask<CodegenConfiguration> task, AnnotationProcessingContext ctx) {
      if (!(task.element() instanceof TypeElement target)) {
        return List.of();
      }
      return List.of(
          new MinimalTargetDefinition(
              JavaLangMapper.mapToTypeName(target, ctx),
              builderName(target, ctx),
              JavaLangAnalyser.findAllPossibleSettersOfClass(target, ctx)));
    }

    @Override
    protected GenerationTargetClassDto mapToTargetSpecification(
        MinimalTargetDefinition definition,
        CodegenConfiguration configuration,
        AnnotationProcessingContext ctx) {
      GenerationTargetClassDto target = new GenerationTargetClassDto();
      target.setTypeName(definition.builderTypeName());
      target.setClassAccessModifier(AccessModifier.PUBLIC);
      target.setFormattingMode(FormattingMode.NONE);

      ClassFieldDto field = new ClassFieldDto();
      field.setFieldName("target");
      field.setFieldType(definition.sourceTypeName());
      field.setVisibility(AccessModifier.PRIVATE);
      field.setLiteralInitializer("new " + definition.sourceTypeName().getClassName() + "()");
      target.addClassField(field);

      for (ExecutableElement setter : definition.setters()) {
        String setterName = setter.getSimpleName().toString();
        String methodName = StringUtils.uncapitalize(Strings.CI.removeStart(setterName, "set"));
        MethodDto method = new MethodDto(methodName, definition.builderTypeName());
        method.setModifier(AccessModifier.PUBLIC);
        String paramName = setter.getParameters().get(0).getSimpleName().toString();
        method.addParameter(JavaLangMapper.map2MethodParameter(setter.getParameters().get(0), ctx));
        method.setCode(
            """
            target.$setterName:N($paramName:N);
            return this;""");
        method.addArgument("setterName", setterName);
        method.addArgument("paramName", paramName);
        target.addMethod(method);
      }
      MethodDto build = new MethodDto("build", definition.sourceTypeName());
      build.setModifier(AccessModifier.PUBLIC);
      build.setCode("return target;");
      target.addMethod(build);
      return target;
    }

    private static TypeName builderName(TypeElement target, AnnotationProcessingContext ctx) {
      return new TypeName(ctx.getPackageName(target), target.getSimpleName() + "Builder");
    }
  }

  @Test
  void minimalProcessorGeneratesDelegatingBuilder() {
    // Given
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              private String name;
              public void setName(String name) { this.name = name; }
            }
            """);

    // When
    Compilation compilation =
        ProcessorTestUtils.createCompiler(new MinimalProcessor()).compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    // The whole expected target as one textblock — pins structure, delegation and the target
    // field; whitespace-normalized compare, nothing generated is left unchecked.
    assertNormalizedEquals(
        """
        package test;
        public class PersonBuilder {

          private Person target = new Person();

          public PersonBuilder name(String name) {
            target.setName(name);
            return this;
          }

          public Person build() {
            return target;
          }
        }""",
        generatedCode,
        "generated PersonBuilder must match the expected source");
  }
}
