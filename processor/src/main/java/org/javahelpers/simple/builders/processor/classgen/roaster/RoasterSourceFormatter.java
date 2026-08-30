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
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.util.FormatterProfileReader;

/**
 * Handles formatting of Java source code produced by Roaster's {@code toUnformattedString()}.
 *
 * <p>This formatter is specifically designed for Roaster's output format and addresses its quirks
 * (tab indentation, concatenated imports/javadoc, missing javadoc asterisk prefixes). It is not a
 * general-purpose source code formatter.
 *
 * <p>Supports three modes controlled by {@link FormattingMode}: full Eclipse JDT formatting,
 * lightweight cosmetic post-processing, or no formatting at all. See the {@link FormattingMode}
 * enum for details on each mode.
 */
public final class RoasterSourceFormatter {

  static final String DEFAULT_FORMATTER_PROFILE_RESOURCE = "eclipse-java-format.xml";
  private static final int SPACES_PER_TAB = 2;

  private final ProcessingLogger logger;
  private final FormattingMode formattingMode;
  private final Properties formatterProperties;
  private final boolean formatterProfileAvailable;
  private final String formatterProfileResource;

  /**
   * Creates a formatter instance.
   *
   * @param logger logger for warnings (e.g. formatter profile load failures)
   * @param formattingMode the formatting mode to use for source code post-processing
   * @throws NullPointerException if logger or formattingMode is null
   */
  public RoasterSourceFormatter(ProcessingLogger logger, FormattingMode formattingMode) {
    this(logger, formattingMode, DEFAULT_FORMATTER_PROFILE_RESOURCE);
  }

  RoasterSourceFormatter(
      ProcessingLogger logger, FormattingMode formattingMode, String formatterProfileResource) {
    this.logger = Objects.requireNonNull(logger, "logger must not be null");
    this.formattingMode = Objects.requireNonNull(formattingMode, "formattingMode must not be null");
    this.formatterProfileResource =
        Objects.requireNonNull(
            formatterProfileResource, "formatterProfileResource must not be null");
    this.formatterProperties = loadFormatterProperties();
    this.formatterProfileAvailable = !formatterProperties.isEmpty();
    if (formattingMode == FormattingMode.JDT && !formatterProfileAvailable) {
      logger.warning(
          "simple-builders: JDT formatting requested but Eclipse formatter profile is unavailable; falling back to lightweight formatting.");
    }
  }

  /**
   * Formats the given raw source code according to the configured {@link FormattingMode}.
   *
   * <p>If {@link FormattingMode#NONE}, the raw source is returned as-is. If {@link
   * FormattingMode#LIGHTWEIGHT} or if the Eclipse formatter profile is unavailable, the lightweight
   * formatter is used. Otherwise the full Eclipse JDT formatter is applied.
   *
   * @param rawSource the unformatted Java source code from Roaster's {@code toUnformattedString()}
   * @return the formatted source code
   */
  public String format(String rawSource) {
    if (formattingMode == FormattingMode.NONE) {
      return rawSource;
    }
    if (formattingMode == FormattingMode.LIGHTWEIGHT) {
      return lightweightFormat(rawSource);
    }
    if (!formatterProfileAvailable) {
      return lightweightFormat(rawSource);
    }
    return Roaster.format(formatterProperties, rawSource);
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
    String[] rawLines = source.split("\n", -1);
    StringBuilder output = new StringBuilder(source.length());
    JavadocState javadocState = new JavadocState();
    boolean prevBlank = false;

    for (String line : rawLines) {
      // 1. Convert leading tabs to spaces
      String converted = convertTabsToSpaces(line);

      // 2. Split import/code concatenated with /**
      if (!javadocState.inJavadoc) {
        converted = splitConcatenatedJavadocOpen(converted, output, javadocState, prevBlank);
      }

      // 3. Fix javadoc asterisk prefixes and indentation
      if (javadocState.inJavadoc) {
        converted = fixJavadocLine(converted, javadocState);
      }

      // 4. Collapse consecutive blank lines and append
      boolean isBlank = converted.isBlank();
      if (isBlank && prevBlank) {
        continue;
      }
      if (output.length() > 0) {
        output.append('\n');
      }
      output.append(converted);
      prevBlank = isBlank;
    }
    return output.toString();
  }

  /**
   * Handles a line that may contain a {@code /**} javadoc opening concatenated with preceding code
   * (e.g. {@code "import ...;/**"}). If found, emits the preceding part as its own line and updates
   * the javadoc state. Returns the remaining line to process (either the original or just the
   * {@code /**} part).
   */
  private String splitConcatenatedJavadocOpen(
      String converted, StringBuilder output, JavadocState javadocState, boolean prevBlank) {
    int jdStart = converted.indexOf("/**");
    if (jdStart < 0) {
      return converted;
    }
    String afterOpen = converted.substring(jdStart + 3);
    if (afterOpen.contains("*/")) {
      return converted;
    }
    String before = converted.substring(0, jdStart).stripTrailing();
    String indent = getLeadingIndent(converted);
    javadocState.inJavadoc = true;
    javadocState.indent = indent.length();
    if (!before.isEmpty()) {
      boolean beforeBlank = before.isBlank();
      if (!(beforeBlank && prevBlank)) {
        if (output.length() > 0) {
          output.append('\n');
        }
        output.append(before);
      }
      return indent + "/**";
    }
    return converted;
  }

  /**
   * Fixes javadoc body lines by adding missing {@code " * "} prefixes and normalizing indentation.
   * Updates the javadoc state when the closing javadoc delimiter is encountered.
   */
  private String fixJavadocLine(String converted, JavadocState javadocState) {
    String stripped = converted.strip();
    if (stripped.startsWith("/**")) {
      return converted;
    }
    if (stripped.endsWith("*/")) {
      if (!stripped.equals("*/") && !stripped.startsWith("*")) {
        String content = converted.substring(0, converted.indexOf("*/")).strip();
        converted = " ".repeat(javadocState.indent) + " * " + content + " */";
      }
      javadocState.inJavadoc = false;
      return converted;
    }
    if (stripped.isBlank()) {
      return " ".repeat(javadocState.indent) + " *";
    }
    if (!stripped.startsWith("*")) {
      return " ".repeat(javadocState.indent) + " * " + stripped;
    }
    return converted;
  }

  /** Mutable state for javadoc processing within {@link #lightweightFormat(String)}. */
  private static final class JavadocState {
    boolean inJavadoc = false;
    int indent = 0;
  }

  /**
   * Convert leading tab characters to spaces (2 spaces per tab).
   *
   * @param line the line to convert
   * @return the line with leading tabs replaced by spaces, or the original line if no tabs
   */
  private String convertTabsToSpaces(String line) {
    int wsEnd = 0;
    while (wsEnd < line.length() && (line.charAt(wsEnd) == ' ' || line.charAt(wsEnd) == '\t')) {
      wsEnd++;
    }
    if (wsEnd == 0) {
      return line;
    }
    boolean hasTab = false;
    for (int j = 0; j < wsEnd; j++) {
      if (line.charAt(j) == '\t') {
        hasTab = true;
        break;
      }
    }
    if (!hasTab) {
      return line;
    }
    StringBuilder sb = new StringBuilder(line.length() + wsEnd);
    for (int j = 0; j < wsEnd; j++) {
      char c = line.charAt(j);
      if (c == '\t') {
        sb.append(" ".repeat(SPACES_PER_TAB));
      } else {
        sb.append(c);
      }
    }
    sb.append(line, wsEnd, line.length());
    return sb.toString();
  }

  /**
   * Extract leading whitespace (spaces and tabs) from a line.
   *
   * @param line the line to extract indent from
   * @return the leading whitespace string
   */
  private static String getLeadingIndent(String line) {
    int end = 0;
    while (end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t')) {
      end++;
    }
    return line.substring(0, end);
  }

  private Properties loadFormatterProperties() {
    try (InputStream inputStream =
        RoasterSourceFormatter.class
            .getClassLoader()
            .getResourceAsStream(formatterProfileResource)) {
      if (inputStream == null) {
        logger.warning(
            "simple-builders: Bundled Eclipse formatter profile '%s' was not found on the processor classpath.",
            formatterProfileResource);
        return new Properties();
      }
      FormatterProfileReader profileReader = FormatterProfileReader.fromEclipseXml(inputStream);
      return profileReader.getDefaultProperties();
    } catch (IOException ex) {
      logger.warning(
          "simple-builders: Failed to load bundled Eclipse formatter profile '%s': %s",
          formatterProfileResource,
          StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
      return new Properties();
    }
  }
}
