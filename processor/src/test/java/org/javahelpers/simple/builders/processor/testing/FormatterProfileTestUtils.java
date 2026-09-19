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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Test helper for deriving Eclipse formatter profiles from the bundled {@code
 * eclipse-java-format.xml} classpath resource with selected settings overridden.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * String profile = FormatterProfileTestUtils.formatterProfileWithSettings(
 *     Map.of("org.eclipse.jdt.core.formatter.tabulation.char", "tab"));
 * }</pre>
 */
public final class FormatterProfileTestUtils {

  private static final String BUNDLED_PROFILE_RESOURCE = "eclipse-java-format.xml";

  private FormatterProfileTestUtils() {}

  /**
   * Reads the bundled formatter profile and replaces the values of the given settings.
   *
   * @param settings map of Eclipse formatter setting ids to their new values
   * @return the profile XML with the requested settings applied
   * @throws IOException if the bundled profile cannot be read
   * @throws IllegalArgumentException if a requested setting id is absent or malformed
   */
  public static String formatterProfileWithSettings(Map<String, String> settings)
      throws IOException {
    String profile;
    try (InputStream inputStream =
        FormatterProfileTestUtils.class
            .getClassLoader()
            .getResourceAsStream(BUNDLED_PROFILE_RESOURCE)) {
      if (inputStream == null) {
        throw new IllegalStateException(
            "Bundled formatter profile '" + BUNDLED_PROFILE_RESOURCE + "' not found on classpath");
      }
      profile = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
    for (Map.Entry<String, String> setting : settings.entrySet()) {
      String prefix = "<setting id=\"" + setting.getKey() + "\" value=\"";
      int prefixStart = profile.indexOf(prefix);
      if (prefixStart < 0) {
        throw new IllegalArgumentException(
            "No setting with id '" + setting.getKey() + "' found in bundled formatter profile");
      }
      int valueStart = prefixStart + prefix.length();
      int valueEnd = profile.indexOf('"', valueStart);
      if (valueEnd < 0) {
        throw new IllegalArgumentException(
            "Malformed setting with id '" + setting.getKey() + "' in bundled formatter profile");
      }
      profile = profile.substring(0, valueStart) + setting.getValue() + profile.substring(valueEnd);
    }
    return profile;
  }
}
