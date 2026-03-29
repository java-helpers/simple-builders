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

package org.javahelpers.simple.builders.processor.model.javadoc;

import org.apache.commons.lang3.StringUtils;

/** Utility class for parsing Javadoc strings into structured JavadocDto objects. */
public final class JavadocParser {

  private JavadocParser() {
    // Utility class - prevent instantiation
  }

  /**
   * Parses a Javadoc string into a structured JavadocDto.
   *
   * @param javadocString the raw Javadoc string (may be null or empty)
   * @return the parsed JavadocDto, or null if the input is null or empty
   */
  public static JavadocDto parse(String javadocString) {
    if (StringUtils.isBlank(javadocString)) {
      return null;
    }

    // Normalize line endings without regex to avoid ReDoS vulnerability
    String normalized = javadocString.replace("\r\n", "\n").replace("\r", "\n");
    // Remove trailing newlines using StringUtils
    normalized = StringUtils.stripEnd(normalized, "\n");
    String[] lines = normalized.split("\n", -1);

    JavadocDto javadoc = new JavadocDto();
    StringBuilder description = new StringBuilder();
    boolean inTags = false;

    for (String line : lines) {
      if (!inTags && line.startsWith("@")) {
        inTags = true;
      }

      if (inTags && line.startsWith("@")) {
        // Process tag line
        JavadocTagDto tag = parseTagLine(line);
        if (tag != null) {
          javadoc.addTag(tag.tagName(), tag.tagValue());
        }
      } else {
        // Process description line
        if (!description.isEmpty()) {
          description.append('\n');
        }
        description.append(line);
      }
    }

    // Set description (may be empty)
    String descriptionText = description.toString().trim();
    if (!descriptionText.isEmpty()) {
      javadoc.setDescription(descriptionText);
    }

    return javadoc;
  }

  /**
   * Parses a single tag line into a JavadocTagDto.
   *
   * @param line the tag line (starts with @)
   * @return the parsed tag, or null if invalid
   */
  private static JavadocTagDto parseTagLine(String line) {
    if (line == null || !line.startsWith("@") || line.length() <= 1) {
      return null;
    }

    int firstSpace = line.indexOf(' ');
    if (firstSpace > 1) {
      // Tag with value: @param name description
      String tagName = line.substring(1, firstSpace); // Remove @ prefix
      String tagValue = line.substring(firstSpace + 1);
      return new JavadocTagDto(tagName, tagValue);
    } else if (line.length() > 1) {
      // Tag without value: @deprecated
      String tagName = line.substring(1); // Remove @ prefix
      return JavadocTagDto.withoutValue(tagName);
    }

    return null;
  }

  /**
   * Converts a JavadocDto back to a string representation.
   *
   * @param javadoc the JavadocDto to convert (may be null)
   * @return the string representation, or null if input is null
   */
  public static String toString(JavadocDto javadoc) {
    if (javadoc == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    // Add description
    if (StringUtils.isNotBlank(javadoc.getDescription())) {
      sb.append(javadoc.getDescription());
    }

    // Add tags
    for (JavadocTagDto tag : javadoc.getTags()) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append('@').append(tag.tagName());
      if (tag.hasValue()) {
        sb.append(' ').append(tag.tagValue());
      }
    }

    return sb.toString();
  }
}
