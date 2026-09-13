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

package org.javahelpers.simple.builders.processor.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.testing.CapturingProcessingLogger;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OptionValueParsers}, covering parsing correctness and warning logging for
 * unrecognized non-blank values.
 */
class OptionValueParsersTest {

  @Test
  void parseOptionState_recognizesTrueAndEnabled() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    assertEquals(
        OptionState.ENABLED, OptionValueParsers.parseOptionState("true", capturing.logger()));
    assertEquals(
        OptionState.ENABLED, OptionValueParsers.parseOptionState("enabled", capturing.logger()));
    assertEquals(
        OptionState.ENABLED, OptionValueParsers.parseOptionState("TRUE", capturing.logger()));
    assertEquals(
        OptionState.ENABLED, OptionValueParsers.parseOptionState("Enabled", capturing.logger()));
    assertTrue(capturing.messages().isEmpty(), "No warnings for recognized values");
  }

  @Test
  void parseOptionState_recognizesFalseAndDisabled() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    assertEquals(
        OptionState.DISABLED, OptionValueParsers.parseOptionState("false", capturing.logger()));
    assertEquals(
        OptionState.DISABLED, OptionValueParsers.parseOptionState("disabled", capturing.logger()));
    assertEquals(
        OptionState.DISABLED, OptionValueParsers.parseOptionState("FALSE", capturing.logger()));
    assertTrue(capturing.messages().isEmpty(), "No warnings for recognized values");
  }

  @Test
  void parseOptionState_returnsUnsetForNullOrBlank() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    assertEquals(OptionState.UNSET, OptionValueParsers.parseOptionState(null, capturing.logger()));
    assertEquals(OptionState.UNSET, OptionValueParsers.parseOptionState("", capturing.logger()));
    assertEquals(OptionState.UNSET, OptionValueParsers.parseOptionState("   ", capturing.logger()));
    assertTrue(capturing.messages().isEmpty(), "No warnings for null or blank values");
  }

  @Test
  void parseOptionState_logsWarningForUnrecognizedNonBlankValue() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();

    OptionState result = OptionValueParsers.parseOptionState("invalid", capturing.logger());

    assertEquals(OptionState.UNSET, result);
    assertTrue(
        capturing.messages().stream().anyMatch(m -> m.contains("invalid") && m.contains("UNSET")),
        "Warning should mention the unrecognized value and the fallback");
  }

  @Test
  void parseAccessModifier_recognizesPublicPrivatePackagePrivate() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    assertEquals(
        AccessModifier.PUBLIC,
        OptionValueParsers.parseAccessModifier("public", capturing.logger()));
    assertEquals(
        AccessModifier.PRIVATE,
        OptionValueParsers.parseAccessModifier("private", capturing.logger()));
    assertEquals(
        AccessModifier.PACKAGE_PRIVATE,
        OptionValueParsers.parseAccessModifier("package-private", capturing.logger()));
    assertEquals(
        AccessModifier.PACKAGE_PRIVATE,
        OptionValueParsers.parseAccessModifier("package_private", capturing.logger()));
    assertTrue(capturing.messages().isEmpty(), "No warnings for recognized values");
  }

  @Test
  void parseAccessModifier_returnsDefaultForNullOrBlank() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    assertEquals(
        AccessModifier.DEFAULT, OptionValueParsers.parseAccessModifier(null, capturing.logger()));
    assertEquals(
        AccessModifier.DEFAULT, OptionValueParsers.parseAccessModifier("", capturing.logger()));
    assertEquals(
        AccessModifier.DEFAULT, OptionValueParsers.parseAccessModifier("   ", capturing.logger()));
    assertTrue(capturing.messages().isEmpty(), "No warnings for null or blank values");
  }

  @Test
  void parseAccessModifier_logsWarningForUnrecognizedNonBlankValue() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();

    AccessModifier result = OptionValueParsers.parseAccessModifier("protected", capturing.logger());

    assertEquals(AccessModifier.DEFAULT, result);
    assertTrue(
        capturing.messages().stream()
            .anyMatch(m -> m.contains("protected") && m.contains("DEFAULT")),
        "Warning should mention the unrecognized value and the fallback");
  }
}
