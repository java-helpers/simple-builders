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

package org.javahelpers.simple.builders.processor.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link FormatterProfileTestUtils}. */
class FormatterProfileTestUtilsTest {

  @Test
  void createFormatterProfile_unknownSetting_throwsIllegalArgumentException(@TempDir Path tempDir) {
    Map<String, String> settings = Map.of("no.such.formatter.setting", "x");
    assertThrows(
        IllegalArgumentException.class,
        () -> FormatterProfileTestUtils.createFormatterProfile(tempDir, settings));
  }

  @Test
  void createFormatterProfile_writesProfileWithSetting(@TempDir Path tempDir) throws IOException {
    Path profilePath =
        FormatterProfileTestUtils.createFormatterProfile(
            tempDir, Map.of("org.eclipse.jdt.core.formatter.tabulation.char", "tab"));
    assertEquals(
        tempDir.resolve("custom-eclipse-profile.xml"),
        profilePath,
        "The profile should be created with the fixed filename in the given directory");
    assertTrue(
        Files.readString(profilePath)
            .contains(
                "<setting id=\"org.eclipse.jdt.core.formatter.tabulation.char\" value=\"tab\"/>"),
        "The written profile should contain the requested setting value");
  }
}
