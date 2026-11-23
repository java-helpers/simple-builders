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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.javahelpers.codegen.CodeGenerationException;
import org.javahelpers.codegen.SourceFormatter;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.logging.ProcessingLogger;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.method.ConstructorDto;
import org.javahelpers.codegen.model.method.ConstructorDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodeDto;
import org.javahelpers.codegen.model.method.MethodCodeDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodePlaceholder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.method.MethodParameterDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.roaster.exceptions.RoasterMapperException;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.javahelpers.codegen.testing.TestingGenerationEnvironment;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link RoasterCodeGenerator} error-handling robustness.
 *
 * <p>Rendering failures surface as {@link RoasterMapperException}, which extends {@link
 * RuntimeException}. These must be converted into the checked {@link CodeGenerationException} so
 * that the per-builder resilience loops in {@code BuilderProcessor} can isolate the failure to the
 * offending class and continue generating the remaining builders instead of aborting the whole
 * round.
 */
class RoasterCodeGeneratorResilienceTest {

  /** Placeholder subtype the mapper does not know how to render, forcing a mapping failure. */
  private static final class UnsupportedPlaceholder extends MethodCodePlaceholder<String> {
    private UnsupportedPlaceholder(String label, String value) {
      super(label, value);
    }
  }

  @Test
  void shouldWrapRenderingRuntimeExceptionInCodeGenerationException() {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("com.example", "FailingBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .build();

    MethodCodeDto code = MethodCodeDtoBuilder.create().codeFormat("return $value:X;").build();
    code.addCodeArgument(new UnsupportedPlaceholder("value", "irrelevant"));

    ConstructorDto constructor =
        ConstructorDtoBuilder.create()
            .visibility(AccessModifier.PUBLIC)
            .methodCodeDto(code)
            .build();
    classDef.addConstructor(constructor);

    ProcessingLogger logger = CapturingProcessingLogger.create().logger();
    RoasterCodeGenerator generator =
        new RoasterCodeGenerator(TestingGenerationEnvironment.createUnwritable(logger));

    SourceFormatter formatter = new RoasterSourceFormatter(logger, FormattingMode.NONE);
    CodeGenerationException thrown =
        assertThrows(
            CodeGenerationException.class, () -> generator.generateClass(classDef, formatter));
    assertInstanceOf(RoasterMapperException.class, thrown.getCause());
  }

  /**
   * Two methods with identical signature keys must not produce duplicate members in the emitted
   * source: the safety-net resolution keeps the first occurrence and reports the dropped one as a
   * warning (an error in strict mode). Conflicts are normally resolved earlier by {@code
   * BuilderDefinitionCreator}, so this path is only reachable when methods are fed directly to the
   * generator.
   */
  @Test
  void shouldKeepFirstMethodAndWarnOnDuplicateSignature() {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("com.example", "DuplicateSignatureBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .build();
    classDef.addMethod(duplicateSignatureMethod("first"));
    classDef.addMethod(duplicateSignatureMethod("second"));

    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    RoasterCodeGenerator generator =
        new RoasterCodeGenerator(TestingGenerationEnvironment.createUnwritable(capturing.logger()));

    SourceFormatter formatter = new RoasterSourceFormatter(capturing.logger(), FormattingMode.NONE);
    // generation itself succeeds; only the file-write step fails on the stub's null source file
    assertThrows(NullPointerException.class, () -> generator.generateClass(classDef, formatter));
    capturing.assertMessage(
        "WARNING: Unexpected duplicate method signature: 'name(java.lang.String)'"
            + " — keeping first occurrence (safety net)");
  }

  private static MethodDto duplicateSignatureMethod(String marker) {
    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("name")
            .returnType(new TypeName("com.example", "DuplicateSignatureBuilder"))
            .modifier(AccessModifier.PUBLIC)
            .code("return this; // " + marker)
            .build();
    MethodParameterDto parameter =
        MethodParameterDtoBuilder.create()
            .parameterName("value")
            .parameterTypeName(TypeName.of(String.class))
            .build();
    method.addParameter(parameter);
    return method;
  }
}
