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

package org.javahelpers.codegen.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.junit.jupiter.api.Test;

/** Tests for the generated {@link GenerationTargetClassDtoBuilder}. */
class GenerationTargetClassDtoBuilderTest {

  @Test
  void buildsEquivalentDtoAsSetterSequence() {
    // Same DTO as assembled field-by-field in RoasterCodeGeneratorTest
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "AnnotatedBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classJavadoc(new JavadocDto("Builder for {@link Person}."))
            .superType(new TypeName("test", "AbstractBuilder"))
            .build();

    assertEquals(new TypeName("test", "AnnotatedBuilder"), dto.getTypeName());
    assertEquals(AccessModifier.PUBLIC, dto.getClassAccessModifier());
    assertEquals(FormattingMode.NONE, dto.getFormattingMode());
    assertEquals("Builder for {@link Person}.", dto.getClassJavadoc().getDescription());
    assertEquals(new TypeName("test", "AbstractBuilder"), dto.getSuperType());
  }

  @Test
  void buildsNestedDtoViaBuilderConsumer() {
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "EmptyBuilder"))
            .classJavadoc(javadoc -> javadoc.format("Builder for {@link %s}.", "Person"))
            .build();

    assertEquals("Builder for {@link Person}.", dto.getClassJavadoc().getDescription());
  }

  @Test
  void unsetPropertiesRemainNull() {
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "EmptyBuilder"))
            .build();

    assertNull(dto.getClassAccessModifier());
    assertNull(dto.getClassJavadoc());
    assertNull(dto.getSuperType());
    assertNull(dto.getFormattingMode());
  }

  @Test
  void builderInitializedFromInstanceAppliesChanges() {
    GenerationTargetClassDto original =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "Builder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .build();

    GenerationTargetClassDto modified =
        new GenerationTargetClassDtoBuilder(original)
            .classAccessModifier(AccessModifier.PRIVATE)
            .build();

    assertEquals(original.getTypeName(), modified.getTypeName());
    assertEquals(AccessModifier.PRIVATE, modified.getClassAccessModifier());
    assertEquals(AccessModifier.PUBLIC, original.getClassAccessModifier());
  }

  @Test
  void methodDtoBuilderCoversIsStaticFlag() {
    // The isStatic getter supplies a legal property name for the setStatic setter.
    MethodDto method = MethodDtoBuilder.create().methodName("m").isStatic(true).build();

    assertEquals("m", method.getMethodName());
    assertTrue(method.isStatic());
  }
}
