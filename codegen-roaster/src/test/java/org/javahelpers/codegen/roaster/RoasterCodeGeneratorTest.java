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

package org.javahelpers.codegen.roaster;

import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNormalizedEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import org.javahelpers.codegen.GeneratedSourceFile;
import org.javahelpers.codegen.GenerationEnvironment;
import org.javahelpers.codegen.GenerationIssueReporter;
import org.javahelpers.codegen.SourceFormatter;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.logging.NoOpPerformanceTracker;
import org.javahelpers.codegen.logging.PerformanceTracker;
import org.javahelpers.codegen.logging.ProcessingLogger;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RoasterCodeGenerator} driving {@link GenerationTargetClassDto}s directly — no
 * simple-builder types involved, the {@link GenerationEnvironment} is built from codegen-core types
 * only and the emitted source is captured in memory and asserted as a whole text block.
 */
class RoasterCodeGeneratorTest {

  private static final class CapturingFile implements GeneratedSourceFile {
    private final String name;
    private final StringWriter buffer = new StringWriter();

    private CapturingFile(CharSequence qualifiedName) {
      this.name = qualifiedName.toString();
    }

    @Override
    public URI toUri() {
      return URI.create("mem:///" + name.replace('.', '/') + ".java");
    }

    @Override
    public Writer openWriter() {
      return buffer;
    }

    private String source() {
      return buffer.toString();
    }
  }

  private static GenerationEnvironment environment(
      ProcessingLogger logger, CapturingFile[] written) {
    return new GenerationEnvironment() {
      @Override
      public ProcessingLogger getLogger() {
        return logger;
      }

      @Override
      public PerformanceTracker getPerformanceTracker() {
        return new NoOpPerformanceTracker();
      }

      @Override
      public GenerationIssueReporter getIssueReporter() {
        return logger::warning;
      }

      @Override
      public GeneratedSourceFile createSourceFile(CharSequence qualifiedName) {
        CapturingFile file = new CapturingFile(qualifiedName);
        written[0] = file;
        return file;
      }
    };
  }

  /** A builder-style DTO: private target field, one delegating setter, one build method. */
  private static GenerationTargetClassDto personBuilderDefinition() {
    TypeName builderType = new TypeName("test", "PersonBuilder");
    TypeName personType = new TypeName("test", "Person");

    GenerationTargetClassDto target = new GenerationTargetClassDto();
    target.setTypeName(builderType);
    target.setClassAccessModifier(AccessModifier.PUBLIC);
    target.setFormattingMode(FormattingMode.NONE);

    ClassFieldDto field = new ClassFieldDto();
    field.setFieldName("target");
    field.setFieldType(personType);
    field.setVisibility(AccessModifier.PRIVATE);
    field.setLiteralInitializer("new Person()");
    target.addClassField(field);

    MethodDto name = new MethodDto("name", builderType);
    name.setModifier(AccessModifier.PUBLIC);
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("name");
    param.setParameterTypeName(new TypeName("java.lang", "String"));
    name.addParameter(param);
    name.setCode(
        """
        target.setName(name);
        return this;""");
    target.addMethod(name);

    MethodDto build = new MethodDto("build", personType);
    build.setModifier(AccessModifier.PUBLIC);
    build.setCode("return target;");
    target.addMethod(build);
    return target;
  }

  @Test
  void generatesDelegatingBuilderFromDto() throws Exception {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    CapturingFile[] written = new CapturingFile[1];
    RoasterCodeGenerator generator =
        new RoasterCodeGenerator(environment(capturing.logger(), written));
    SourceFormatter formatter = new RoasterSourceFormatter(capturing.logger(), FormattingMode.NONE);

    generator.generateClass(personBuilderDefinition(), formatter);

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
        written[0].source(),
        "generated PersonBuilder must match the expected source");
  }

  @Test
  void writesFileUnderQualifiedName() throws Exception {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    CapturingFile[] written = new CapturingFile[1];
    RoasterCodeGenerator generator =
        new RoasterCodeGenerator(environment(capturing.logger(), written));

    GenerationTargetClassDto empty = new GenerationTargetClassDto();
    empty.setTypeName(new TypeName("com.example", "EmptyBuilder"));
    empty.setClassAccessModifier(AccessModifier.PUBLIC);
    empty.setFormattingMode(FormattingMode.NONE);

    generator.generateClass(
        empty, new RoasterSourceFormatter(capturing.logger(), FormattingMode.NONE));

    org.junit.jupiter.api.Assertions.assertEquals(
        "mem:///com/example/EmptyBuilder.java", written[0].toUri().toString());
  }
}
