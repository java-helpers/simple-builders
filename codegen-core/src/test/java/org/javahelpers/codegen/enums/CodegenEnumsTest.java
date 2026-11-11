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

package org.javahelpers.codegen.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for the codegen enums' value mappings and parsing. */
class CodegenEnumsTest {

  @Test
  void accessModifier_mapsToJavaKeyword() {
    assertEquals("public", AccessModifier.DEFAULT.getJavaKeyword());
    assertEquals("public", AccessModifier.PUBLIC.getJavaKeyword());
    assertEquals("protected", AccessModifier.PROTECTED.getJavaKeyword());
    assertEquals("", AccessModifier.PACKAGE_PRIVATE.getJavaKeyword());
    assertEquals("private", AccessModifier.PRIVATE.getJavaKeyword());
  }

  @Test
  void formattingMode_optionValuesAndParsing() {
    assertEquals("jdt", FormattingMode.JDT.getOptionValue());
    assertEquals("lightweight", FormattingMode.LIGHTWEIGHT.getOptionValue());
    assertEquals("none", FormattingMode.NONE.getOptionValue());

    assertEquals(FormattingMode.NONE, FormattingMode.fromString("none"));
    assertEquals(FormattingMode.LIGHTWEIGHT, FormattingMode.fromString("LightWeight"));
    assertEquals(FormattingMode.JDT, FormattingMode.fromString(null));
    assertEquals(FormattingMode.JDT, FormattingMode.fromString("unknown"));
  }

  @Test
  void optionState_hasTriStateValues() {
    assertEquals(
        java.util.Set.of(OptionState.UNSET, OptionState.ENABLED, OptionState.DISABLED),
        java.util.Set.of(OptionState.values()));
  }
}
