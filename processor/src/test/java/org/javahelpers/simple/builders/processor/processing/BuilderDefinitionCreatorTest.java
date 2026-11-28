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
package org.javahelpers.simple.builders.processor.processing;

import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.javahelpers.codegen.testing.ProbeProcessor;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDtoBuilder;
import org.junit.jupiter.api.Test;

class BuilderDefinitionCreatorTest {

  @Test
  void accessorPropertyNamesRequireConventionalPrefixCapitalization() {
    List<String> names =
        List.of(
            "setValue",
            "getValue",
            "settings",
            "getaway",
            "SetValue",
            "SETValue",
            "GetValue",
            "GETValue",
            "isStatic",
            "set",
            "get",
            "setURL",
            "getURL");
    List<String> properties =
        ProbeProcessor.probe(
            (roundEnv, processingEnv) -> {
              var dto = typeElement(processingEnv, "test.AccessorDto");
              return names.stream()
                  .map(name -> BuilderDefinitionCreator.propertyNameFromAccessor(method(dto, name)))
                  .toList();
            },
            CodegenTestUtils.forSource(
                """
                package test;
                public class AccessorDto {
                  public void setValue(String value) {}
                  public String getValue() { return ""; }
                  public String settings() { return ""; }
                  public String getaway() { return ""; }
                  public String SetValue() { return ""; }
                  public String SETValue() { return ""; }
                  public String GetValue() { return ""; }
                  public String GETValue() { return ""; }
                  public boolean isStatic() { return false; }
                  public String set() { return ""; }
                  public String get() { return ""; }
                  public void setURL(String value) {}
                  public String getURL() { return ""; }
                }
                """));

    assertEquals(
        List.of(
            "value",
            "value",
            "settings",
            "getaway",
            "setValue",
            "sETValue",
            "getValue",
            "gETValue",
            "isStatic",
            "set",
            "get",
            "uRL",
            "uRL"),
        properties);
  }

  @Test
  void setterSourceDescriptionRejectsMissingSetterName() {
    BuilderFieldDto field = field();

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> BuilderDefinitionCreator.buildSetterSourceDescription(field, "FlagDto"));

    assertEquals("Missing setter name for field 'isStatic'", exception.getMessage());
  }

  @Test
  void setterSourceDescriptionLinksKnownDto() {
    BuilderFieldDto field = field();
    field.setSetterName("setStatic");

    assertEquals(
        "<p>Generated from setter {@link FlagDto#setStatic(Boolean) setStatic(Boolean isStatic)}",
        BuilderDefinitionCreator.buildSetterSourceDescription(field, "FlagDto"));
  }

  @Test
  void setterSourceDescriptionAllowsUnknownDto() {
    BuilderFieldDto field = field();
    field.setSetterName("setStatic");

    assertEquals(
        "<p>Generated from setter <code>setStatic(Boolean isStatic)</code>",
        BuilderDefinitionCreator.buildSetterSourceDescription(field, null));
  }

  private static BuilderFieldDto field() {
    return BuilderFieldDtoBuilder.create()
        .originalFieldName("isStatic")
        .fieldType(Boolean.class)
        .build();
  }
}
