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
package org.javahelpers.simple.builders.processor.classgen.roaster;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.util.FormatterProfileReader;

/**
 * Handles formatting of generated Java source code.
 *
 * <p>Supports two modes:
 *
 * <ul>
 *   <li><b>Eclipse JDT formatter</b> — full formatting using a bundled Eclipse formatter profile.
 *       This is the default and produces the highest quality output.
 *   <li><b>Lightweight formatter</b> — minimal post-processing of Roaster's {@code
 *       toUnformattedString()} output. Used when {@code skipFormatting} is enabled or when the
 *       Eclipse formatter profile cannot be loaded. Applies cosmetic fixes at a fraction of the
 *       cost:
 *       <ul>
 *         <li>Convert tab indentation to 2-space indentation
 *         <li>Remove duplicate blank lines (collapse 2+ consecutive blanks to 1)
 *         <li>Insert newline between a trailing import and an adjacent {@code /**} javadoc opening
 *         <li>Add missing {@code " * "} prefixes to javadoc body lines
 *         <li>Normalize javadoc body indentation to match the enclosing member
 *       </ul>
 * </ul>
 */
public class SourceFormatter {

  private static final String FORMATTER_PROFILE_RESOURCE = "eclipse-java-format.xml";

  private final ProcessingLogger logger;
  private final boolean skipFormatting;
  private final Properties formatterProperties;

  /**
   * Creates a formatter instance.
   *
   * @param logger logger for warnings (e.g. formatter profile load failures)
   * @param skipFormatting if {@code true}, bypass the Eclipse formatter and use lightweight
   *     post-processing instead
   */
  public SourceFormatter(ProcessingLogger logger, boolean skipFormatting) {
    this.logger = logger;
    this.skipFormatting = skipFormatting;
    this.formatterProperties = loadFormatterProperties();
  }

  /**
   * Formats the given raw source code.
   *
   * <p>If {@code skipFormatting} is enabled or the Eclipse formatter profile is unavailable, the
   * lightweight formatter is used instead. Otherwise the full Eclipse JDT formatter is applied.
   *
   * @param rawSource the unformatted Java source code from Roaster's {@code toUnformattedString()}
   * @return the formatted source code
   */
  public String format(String rawSource) {
    if (skipFormatting || formatterProperties.isEmpty()) {
      return lightweightFormat(rawSource);
    }
    try {
      return Roaster.format(formatterProperties, rawSource);
    } catch (Exception ex) {
      logger.warning(
          "simple-builders: Failed to format generated source with bundled Eclipse formatter profile: %s",
          StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
      return rawSource;
    }
  }

  /**
   * Lightweight post-processing of Roaster's unformatted output.
   *
   * <p>Applies minimal cosmetic fixes that are much cheaper than the full Eclipse JDT formatter:
   *
   * <ul>
   *   <li>Convert tab indentation to 2-space indentation
   *   <li>Remove duplicate blank lines (collapse 2+ consecutive blanks to 1)
   *   <li>Insert newline between a trailing import and an adjacent {@code /**} javadoc opening
   *   <li>Add missing {@code " * "} prefixes to javadoc body lines
   *   <li>Normalize javadoc body indentation to match the enclosing member
   * </ul>
   *
   * @param source the raw source from Roaster's {@code toUnformattedString()}
   * @return the lightly post-processed source
   */
  String lightweightFormat(String source) {
    // Use a list so we can insert new lines when splitting concatenated code
    List<String> lines = new ArrayList<>(Arrays.asList(source.split("\n", -1)));
    boolean inJavadoc = false;
    int javadocIndent = 0;

    for (int i = 0; i < lines.size(); i++) {
      // 1. Convert leading tabs to 2-space indentation
      lines.set(i, convertTabsToSpaces(lines.get(i)));
      String converted = lines.get(i);
      String convertedStripped = converted.strip();

      // 2. Handle import/code concatenated with /** (e.g. "import ...;/**")
      if (!inJavadoc) {
        int jdStart = converted.indexOf("/**");
        if (jdStart >= 0) {
          String afterOpen = converted.substring(jdStart + 3);
          if (!afterOpen.contains("*/")) {
            String before = converted.substring(0, jdStart).stripTrailing();
            String indent = getLeadingIndent(converted);
            if (!before.isEmpty()) {
              // Split: code stays on this line, /** goes on next line
              lines.set(i, before);
              lines.add(i + 1, indent + "/**");
              converted = indent + "/**";
              convertedStripped = "/**";
            }
            inJavadoc = true;
            javadocIndent = getLeadingIndent(converted).length();
            continue;
          }
        }
      }

      // 3. Javadoc asterisk and indentation fixup
      if (inJavadoc && !convertedStripped.startsWith("/**")) {
        if (convertedStripped.endsWith("*/")) {
          // Closing line
          if (!convertedStripped.equals("*/") && !convertedStripped.startsWith("*")) {
            String content = converted.substring(0, converted.indexOf("*/")).strip();
            lines.set(i, " ".repeat(javadocIndent) + " * " + content + " */");
          }
          inJavadoc = false;
        } else if (!convertedStripped.startsWith("*") && !convertedStripped.isBlank()) {
          // Body line missing asterisk — add " * " prefix with proper indentation
          lines.set(i, " ".repeat(javadocIndent) + " * " + convertedStripped);
        }
      }
    }

    // Second pass: collapse consecutive blank lines to one
    List<String> result = new ArrayList<>();
    boolean prevBlank = false;
    for (String line : lines) {
      boolean isBlank = line.isBlank();
      if (isBlank && prevBlank) {
        continue;
      }
      result.add(line);
      prevBlank = isBlank;
    }

    return String.join("\n", result);
  }

  /** Convert leading tab characters to 2 spaces per tab. */
  private String convertTabsToSpaces(String line) {
    if (!line.contains("\t")) {
      return line;
    }
    StringBuilder sb = new StringBuilder(line.length());
    for (int j = 0; j < line.length(); j++) {
      char c = line.charAt(j);
      if (c == '\t') {
        sb.append("  ");
      } else if (c == ' ') {
        sb.append(' ');
      } else {
        sb.append(line, j, line.length());
        break;
      }
    }
    return sb.toString();
  }

  /** Extract leading whitespace (spaces and tabs) from a line. */
  private String getLeadingIndent(String line) {
    int end = 0;
    while (end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t')) {
      end++;
    }
    return line.substring(0, end);
  }

  private Properties loadFormatterProperties() {
    try (InputStream inputStream =
        SourceFormatter.class.getClassLoader().getResourceAsStream(FORMATTER_PROFILE_RESOURCE)) {
      if (inputStream == null) {
        logger.warning(
            "simple-builders: Bundled Eclipse formatter profile '%s' was not found on the processor classpath.",
            FORMATTER_PROFILE_RESOURCE);
        return new Properties();
      }
      FormatterProfileReader profileReader = FormatterProfileReader.fromEclipseXml(inputStream);
      return profileReader.getDefaultProperties();
    } catch (IOException ex) {
      logger.warning(
          "simple-builders: Failed to load bundled Eclipse formatter profile '%s': %s",
          FORMATTER_PROFILE_RESOURCE,
          StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
      return new Properties();
    }
  }
}
