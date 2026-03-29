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

/** Represents a single Javadoc tag with its name and value. */
public record JavadocTagDto(String tagName, String tagValue) {

  /**
   * Creates a new Javadoc tag.
   *
   * @param tagName the tag name without @ prefix (e.g., "param", "return", "throws", "see")
   * @param tagValue the tag value/content (may be empty for tags without content like @deprecated)
   */
  public JavadocTagDto {
    if (tagName != null && tagName.startsWith("@")) {
      throw new IllegalArgumentException("Tag name should not include @ prefix: " + tagName);
    }
  }

  /**
   * Creates a tag without a value.
   *
   * @param tagName the tag name without @ prefix
   * @return a new JavadocTagDto with empty value
   */
  public static JavadocTagDto withoutValue(String tagName) {
    return new JavadocTagDto(tagName, "");
  }

  /**
   * Returns whether this tag has a non-empty value.
   *
   * @return true if the tag value is not null and not empty
   */
  public boolean hasValue() {
    return tagValue != null && !tagValue.trim().isEmpty();
  }

  /**
   * Returns the full tag name with @ prefix for display purposes.
   *
   * @return the tag name with @ prefix (e.g., "@param")
   */
  public String getFullTagName() {
    return "@" + tagName;
  }
}
