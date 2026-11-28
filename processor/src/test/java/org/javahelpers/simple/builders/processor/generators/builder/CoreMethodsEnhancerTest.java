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
package org.javahelpers.simple.builders.processor.generators.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDtoBuilder;
import org.junit.jupiter.api.Test;

class CoreMethodsEnhancerTest {

  @Test
  void buildMethodUsesResolvedSetterName() {
    BuilderDefinitionDto builder = builder();
    BuilderFieldDto field = field();
    field.setSetterName("setStatic");
    builder.addAllFields(List.of(field));

    String code =
        new CoreMethodsEnhancer().createBuildMethod(builder).getMethodCodeDto().getCodeFormat();

    assertEquals(
        """
        $buildResultType:T result = new $dtoBaseType:T();
        this.isStatic.ifSet(result::setStatic);
        return result;""",
        code);
  }

  @Test
  void buildMethodRejectsSetterFieldWithoutSetterName() {
    BuilderDefinitionDto builder = builder();
    builder.addAllFields(List.of(field()));
    CoreMethodsEnhancer enhancer = new CoreMethodsEnhancer();

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> enhancer.createBuildMethod(builder));

    assertEquals("Missing setter name for field 'isStatic'", exception.getMessage());
  }

  @Test
  void buildMethodAllowsConstructorFieldWithoutSetterName() {
    BuilderDefinitionDto builder = builder();
    builder.addAllFieldsInConstructor(List.of(field()));

    String code =
        new CoreMethodsEnhancer().createBuildMethod(builder).getMethodCodeDto().getCodeFormat();

    assertEquals(
        """
        $buildResultType:T result = new $dtoBaseType:T(this.isStatic.value());
        return result;""",
        code);
  }

  @Test
  void buildMethodAllowsNoFields() {
    String code =
        new CoreMethodsEnhancer().createBuildMethod(builder()).getMethodCodeDto().getCodeFormat();

    assertEquals(
        """
        $buildResultType:T result = new $dtoBaseType:T();
        return result;""",
        code);
  }

  private static BuilderDefinitionDto builder() {
    BuilderDefinitionDto builder = new BuilderDefinitionDto();
    builder.setBuildingTargetTypeName(new TypeName("test", "FlagDto"));
    builder.setConfiguration(BuilderConfiguration.builder().build());
    return builder;
  }

  private static BuilderFieldDto field() {
    return BuilderFieldDtoBuilder.create()
        .originalFieldName("isStatic")
        .fieldNameInBuilder("isStatic")
        .fieldType(Boolean.class)
        .build();
  }
}
