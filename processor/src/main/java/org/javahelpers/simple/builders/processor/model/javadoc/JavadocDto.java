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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents structured Javadoc with description text and tags.
 *
 * <p>Used for method and class-level javadoc. Note that @param tags are for documenting method
 * parameters, not for individual parameter javadoc.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * JavadocDto javadoc = new JavadocDto("Builds a new PersonDto.")
 *     .addParam("name", "the person's name")
 *     .addParam("age", "the person's age")
 *     .addThrows("IllegalArgumentException", "if name is null")
 *     .addReturn("a new PersonDto instance");
 * }</pre>
 */
public class JavadocDto {

  /** The main description text (everything before the first tag). */
  private String description;

  /** List of Javadoc tags in the order they appear. */
  private final List<JavadocTagDto> tags = new ArrayList<>();

  /** Default constructor. */
  public JavadocDto() {
    // Default constructor
  }

  /**
   * Constructor with description text.
   *
   * @param description the main description text
   */
  public JavadocDto(String description) {
    this.description = description;
  }

  /**
   * Constructor with formatted description text.
   *
   * <p>Allows using String.format-style formatting directly in the constructor.
   *
   * <p>Example:
   *
   * <pre>{@code
   * new JavadocDto("Sets the value for <code>%s</code>.", fieldName)
   * }</pre>
   *
   * @param format the format string (using String.format syntax)
   * @param args the arguments referenced by the format specifiers
   */
  public JavadocDto(String format, Object... args) {
    this.description = String.format(format, args);
  }

  /**
   * Constructor with description and tags.
   *
   * @param description the main description text
   * @param tags the list of tags
   */
  public JavadocDto(String description, List<JavadocTagDto> tags) {
    this.description = description;
    if (tags != null) {
      this.tags.addAll(tags);
    }
  }

  /**
   * Gets the main description text.
   *
   * @return the description text
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the list of Javadoc tags.
   *
   * @return the list of tags
   */
  public List<JavadocTagDto> getTags() {
    return tags;
  }

  /**
   * Adds a tag with the given name and value.
   *
   * @param tagName the tag name without @ prefix
   * @param tagValue the tag value
   */
  public void addTag(String tagName, String tagValue) {
    if (tagName != null) {
      tags.add(new JavadocTagDto(tagName, tagValue));
    }
  }

  /**
   * Adds a tag without a value.
   *
   * @param tagName the tag name without @ prefix
   */
  public void addTag(String tagName) {
    if (tagName != null) {
      tags.add(JavadocTagDto.withoutValue(tagName));
    }
  }

  /**
   * Adds a @param tag with optional formatted description.
   *
   * <p>Supports String.format-style formatting when args are provided.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * addParam("value", "the value to set")              // No formatting
   * addParam("value", "the %s to set", fieldName)      // With formatting
   * }</pre>
   *
   * @param paramName the parameter name
   * @param descriptionFormat the format string for the parameter description
   * @param args optional arguments referenced by format specifiers in the description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addParam(String paramName, String descriptionFormat, Object... args) {
    String paramDescription = String.format(descriptionFormat, args);
    addTag("param", paramName + " " + paramDescription);
    return this;
  }

  /**
   * Adds a @return tag with optional formatted description.
   *
   * <p>Supports String.format-style formatting when args are provided.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * addReturn("current instance of builder")           // No formatting
   * addReturn("builder for {@code %s}", className)     // With formatting
   * }</pre>
   *
   * @param descriptionFormat the format string for the return description
   * @param args optional arguments referenced by format specifiers in the description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addReturn(String descriptionFormat, Object... args) {
    String returnDescription = String.format(descriptionFormat, args);
    addTag("return", returnDescription);
    return this;
  }

  /**
   * Adds a @throws tag with optional formatted description.
   *
   * <p>Supports String.format-style formatting when args are provided.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * addThrows("IllegalArgumentException", "if name is null")
   * addThrows("IllegalStateException", "if %s is invalid", fieldName)
   * }</pre>
   *
   * @param exceptionName the exception name
   * @param descriptionFormat the format string for the exception description
   * @param args optional arguments referenced by format specifiers in the description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addThrows(String exceptionName, String descriptionFormat, Object... args) {
    String throwsDescription = String.format(descriptionFormat, args);
    addTag("throws", exceptionName + " " + throwsDescription);
    return this;
  }

  /**
   * Returns whether this Javadoc has any content (description or tags).
   *
   * @return true if there is description text or at least one tag
   */
  public boolean hasContent() {
    return StringUtils.isNotBlank(description) || !tags.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JavadocDto that = (JavadocDto) o;
    return Objects.equals(description, that.description) && Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, tags);
  }

  @Override
  public String toString() {
    return "JavadocDto{" + "description='" + description + '\'' + ", tags=" + tags + '}';
  }
}
