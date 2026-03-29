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

/** Represents structured Javadoc with description text and tags.
 * 
 * <p>Used for method and class-level javadoc. Note that @param tags are for documenting
 * method parameters, not for individual parameter javadoc.
 * 
 * <p>Example usage for a method:
 * <pre>{@code
 * JavadocDto javadoc = new JavadocDto("Builds a new PersonDto.")
 *     .addParam("name", "the person's name")
 *     .addParam("age", "the person's age")
 *     .addReturn("a new PersonDto instance")
 *     .addThrows("IllegalArgumentException", "if name is null");
 * }</pre>
 * 
 * <p>Example usage for a class:
 * <pre>{@code
 * JavadocDto classJavadoc = new JavadocDto("Builder for PersonDto objects.")
 *     .addSee("PersonDto")
 *     .addDeprecated();
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
   * Sets the main description text.
   *
   * @param description the description text
   */
  public void setDescription(String description) {
    this.description = description;
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
    addTag(JavadocTagDto.withoutValue(tagName));
  }

  /**
   * Adds a @param tag.
   *
   * @param paramName the parameter name
   * @param description the parameter description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addParam(String paramName, String description) {
    addTag("param", paramName + " " + description);
    return this;
  }

  /**
   * Adds a @return tag.
   *
   * @param description the return description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addReturn(String description) {
    addTag("return", description);
    return this;
  }

  /**
   * Adds a @throws tag.
   *
   * @param exceptionName the exception name
   * @param description the exception description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addThrows(String exceptionName, String description) {
    addTag("throws", exceptionName + " " + description);
    return this;
  }

  /**
   * Adds a @see tag.
   *
   * @param reference the reference description
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addSee(String reference) {
    addTag("see", reference);
    return this;
  }

  /**
   * Adds a @deprecated tag.
   *
   * @return this JavadocDto for fluent chaining
   */
  public JavadocDto addDeprecated() {
    addTag("deprecated");
    return this;
  }

  /**
   * Gets the first tag with the specified name.
   *
   * @param tagName the tag name to search for (without @ prefix)
   * @return the first matching tag, or null if not found
   */
  public JavadocTagDto getTag(String tagName) {
    return tags.stream()
        .filter(tag -> Objects.equals(tag.tagName(), tagName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets all tags with the specified name.
   *
   * @param tagName the tag name to search for (without @ prefix)
   * @return list of matching tags
   */
  public List<JavadocTagDto> getTags(String tagName) {
    return tags.stream()
        .filter(tag -> Objects.equals(tag.tagName(), tagName))
        .toList();
  }

  /**
   * Returns whether this Javadoc has any content (description or tags).
   *
   * @return true if there is description text or at least one tag
   */
  public boolean hasContent() {
    return StringUtils.isNotBlank(description) || !tags.isEmpty();
  }

  /**
   * Returns whether this Javadoc has any tags.
   *
   * @return true if there is at least one tag
   */
  public boolean hasTags() {
    return !tags.isEmpty();
  }

  /**
   * Removes all tags with the specified name.
   *
   * @param tagName the tag name to remove (without @ prefix)
   * @return true if any tags were removed
   */
  public boolean removeTags(String tagName) {
    return tags.removeIf(tag -> Objects.equals(tag.tagName(), tagName));
  }

  /**
   * Clears all tags.
   */
  public void clearTags() {
    tags.clear();
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
    return "JavadocDto{" +
        "description='" + description + '\'' +
        ", tags=" + tags +
        '}';
  }
}
