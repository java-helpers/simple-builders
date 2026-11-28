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

package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.processing.CodegenTask;
import org.javahelpers.codegen.processing.logging.MessagerProcessingLogger;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.processing.BuilderProcessingContext;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

class MethodGeneratorUtilTest {

  @Test
  void createsSetterWhenParameterAnnotationsAreNull() {
    SetterProbe processor = new SetterProbe();
    assertThat(
            ProcessorTestUtils.createCompiler(processor)
                .compile(
                    ProcessorTestUtils.forSource(
                        """
                        package test;
                        public class Sample {}
                        """)))
        .succeeded();
    assertNotNull(processor.method);
    assertEquals("value", processor.method.getMethodName());
    assertEquals(1, processor.method.getParameters().size());
    assertEquals(List.of(), processor.method.getParameters().get(0).getAnnotations());
    assertEquals("value", processor.method.getParameters().get(0).getParameterName());
    assertEquals(
        TypeName.of(String.class), processor.method.getParameters().get(0).getParameterType());
  }

  private static class SetterProbe extends AbstractProcessor {
    private BuilderMethodDto method;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return Set.of("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      if (method != null || roundEnv.processingOver()) {
        return false;
      }
      TypeElement type = processingEnv.getElementUtils().getTypeElement("test.Sample");
      BuilderProcessingContext context =
          new BuilderProcessingContext(
              MessagerProcessingLogger.create(processingEnv),
              BuilderConfiguration.DEFAULT,
              processingEnv);
      context.setCurrentTask(new CodegenTask<>(type, BuilderConfiguration.DEFAULT, type, ""));
      BuilderFieldDto field =
          new BuilderFieldDto() {
            @Override
            public List<AnnotationDto> getParameterAnnotations() {
              return null;
            }
          };
      field.setOriginalFieldName("value");
      field.setFieldNameInBuilder("value");
      method =
          MethodGeneratorUtil.createBuilderMethodForFieldWithTransform(
              field,
              null,
              TypeName.of(String.class),
              new TypeName("test", "SampleBuilder"),
              context);
      return false;
    }
  }
}
