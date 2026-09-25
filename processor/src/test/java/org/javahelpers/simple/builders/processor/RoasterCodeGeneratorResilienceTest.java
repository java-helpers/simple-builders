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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.annotation.processing.ProcessingEnvironment;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.classgen.GenerationEnvironment;
import org.javahelpers.simple.builders.processor.classgen.roaster.RoasterCodeGenerator;
import org.javahelpers.simple.builders.processor.classgen.roaster.exceptions.RoasterMapperException;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.method.ConstructorDto;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeDto;
import org.javahelpers.simple.builders.processor.model.method.MethodCodePlaceholder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.javahelpers.simple.builders.processor.testing.ProcessingEnvironmentStub;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link RoasterCodeGenerator} error-handling robustness.
 *
 * <p>Rendering failures surface as {@link RoasterMapperException}, which extends {@link
 * RuntimeException}. These must be converted into the checked {@link BuilderException} so that the
 * per-builder resilience loops in {@code BuilderProcessor} can isolate the failure to the offending
 * class and continue generating the remaining builders instead of aborting the whole round.
 */
class RoasterCodeGeneratorResilienceTest {

  /** Placeholder subtype the mapper does not know how to render, forcing a mapping failure. */
  private static final class UnsupportedPlaceholder extends MethodCodePlaceholder<String> {
    private UnsupportedPlaceholder(String label, String value) {
      super(label, value);
    }
  }

  @Test
  void shouldWrapRenderingRuntimeExceptionInBuilderException() {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("com.example", "FailingBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);

    MethodCodeDto code = new MethodCodeDto();
    code.setCodeFormat("return $value:X;");
    code.addCodeArgument(new UnsupportedPlaceholder("value", "irrelevant"));

    ConstructorDto constructor = new ConstructorDto();
    constructor.setVisibility(AccessModifier.PUBLIC);
    constructor.setMethodCodeDto(code);
    classDef.addConstructor(constructor);

    ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
    ProcessingLogger logger = new ProcessingLogger(env, false);
    ProcessingContext context = new ProcessingContext(logger, BuilderConfiguration.DEFAULT, env);
    RoasterCodeGenerator generator =
        new RoasterCodeGenerator(
            new GenerationEnvironment(env, logger, context.getPerformanceTracker()),
            context::createSourceFormatter);

    BuilderException thrown =
        assertThrows(BuilderException.class, () -> generator.generateClass(classDef));
    assertInstanceOf(RoasterMapperException.class, thrown.getCause());
  }
}
