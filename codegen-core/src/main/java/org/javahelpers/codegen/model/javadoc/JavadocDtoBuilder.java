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

package org.javahelpers.codegen.model.javadoc;

import static org.javahelpers.codegen.builders.TrackedValue.changedValue;
import static org.javahelpers.codegen.builders.TrackedValue.initialValue;
import static org.javahelpers.codegen.builders.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.builders.ArrayListBuilder;
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;

/**
 * Builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.javadoc.JavadocDto with method chaining and validation. Use the
 * static {@code create()} method to obtain a new builder instance, configure the desired properties
 * using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * JavadocDto result = JavadocDtoBuilder.create()
 *     .description("Rate 100%")
 *     .description("Hello %s", "World")
 *     .description(() -> "example description")
 *     .description(sb -> sb.append("text"))
 *     .exampleUsageCodeBlock(new JavadocCodeBlockDto())
 *     .exampleUsageCodeBlock(JavadocCodeBlockDto::new)
 *     .exampleUsageCodeBlock(javadocCodeBlockDtoBuilder -> javadocCodeBlockDtoBuilder)
 *     .build();
 * }</pre>
 */
public class JavadocDtoBuilder implements IBuilderBase<JavadocDto> {

  /** Tracked value for <code>description</code>: the main description text. */
  private TrackedValue<String> description = unsetValue();

  /**
   * Tracked value for <code>exampleUsageCodeBlock</code>: the usage-example code block, or {@code
   * null} to clear it.
   */
  private TrackedValue<JavadocCodeBlockDto> exampleUsageCodeBlock = unsetValue();

  /** Tracked value for <code>tags</code>: the replacement collection (not null). */
  private TrackedValue<List<JavadocTagDto>> tags = unsetValue();

  /** Empty constructor of builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}. */
  public JavadocDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto} by a
   * instance.
   *
   * @param instance object instance for initialisiation
   */
  public JavadocDtoBuilder(JavadocDto instance) {
    this.description = initialValue(instance.getDescription());
    this.exampleUsageCodeBlock = initialValue(instance.getExampleUsageCodeBlock());
    this.tags = initialValue(instance.getTags());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * JavadocDtoBuilder builder = JavadocDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}
   */
  public static JavadocDtoBuilder create() {
    return new JavadocDtoBuilder();
  }

  /**
   * Sets the value for <code>exampleUsageCodeBlock</code> using a builder consumer that produces
   * the value.
   *
   * <p>Generated from setter {@link JavadocDto#setExampleUsageCodeBlock(JavadocCodeBlockDto)
   * setExampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleUsageCodeBlock(javadocCodeBlockDtoBuilder -> javadocCodeBlockDtoBuilder);
   * }</pre>
   *
   * @param exampleUsageCodeBlockBuilderConsumer consumer providing an instance of a builder for the
   *     usage-example code block, or {@code null} to clear it
   * @return current instance of builder
   */
  public JavadocDtoBuilder exampleUsageCodeBlock(
      Consumer<JavadocCodeBlockDtoBuilder> exampleUsageCodeBlockBuilderConsumer) {
    JavadocCodeBlockDtoBuilder builder =
        this.exampleUsageCodeBlock.isSet()
            ? new JavadocCodeBlockDtoBuilder(this.exampleUsageCodeBlock.value())
            : JavadocCodeBlockDtoBuilder.create();
    exampleUsageCodeBlockBuilderConsumer.accept(builder);
    this.exampleUsageCodeBlock = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>exampleUsageCodeBlock</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocDto#setExampleUsageCodeBlock(JavadocCodeBlockDto)
   * setExampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleUsageCodeBlock(JavadocCodeBlockDto::new);
   * }</pre>
   *
   * @param exampleUsageCodeBlockSupplier supplier for the usage-example code block, or {@code null}
   *     to clear it
   * @return current instance of builder
   */
  public JavadocDtoBuilder exampleUsageCodeBlock(
      Supplier<JavadocCodeBlockDto> exampleUsageCodeBlockSupplier) {
    this.exampleUsageCodeBlock = changedValue(exampleUsageCodeBlockSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>exampleUsageCodeBlock</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setExampleUsageCodeBlock(JavadocCodeBlockDto)
   * setExampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleUsageCodeBlock(new JavadocCodeBlockDto());
   * }</pre>
   *
   * @param exampleUsageCodeBlock the usage-example code block, or {@code null} to clear it
   * @return current instance of builder
   */
  public JavadocDtoBuilder exampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock) {
    this.exampleUsageCodeBlock = changedValue(exampleUsageCodeBlock);
    return this;
  }

  /**
   * Sets the literal <code>description</code> by executing the provided consumer.
   *
   * <p>Uses {@link JavadocDto#setDescription(String)} without formatting the result.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.description(sb -> sb.append("text"));
   * }</pre>
   *
   * @param descriptionStringBuilderConsumer consumer composing the description
   * @return current instance of builder
   */
  public JavadocDtoBuilder description(Consumer<StringBuilder> descriptionStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    descriptionStringBuilderConsumer.accept(builder);
    this.description = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the literal <code>description</code> by invoking the provided supplier.
   *
   * <p>Uses {@link JavadocDto#setDescription(String)} without formatting the result.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.description(() -> "Rate 100%");
   * }</pre>
   *
   * @param descriptionSupplier supplier for the description
   * @return current instance of builder
   */
  public JavadocDtoBuilder description(Supplier<String> descriptionSupplier) {
    this.description = changedValue(descriptionSupplier.get());
    return this;
  }

  /**
   * Sets the <code>description</code> by applying {@link String#format(String, Object...)} once.
   * The result is not formatted again at {@link #build()}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.description("%s done", "100%");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public JavadocDtoBuilder description(String format, Object... args) {
    this.description = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the literal <code>description</code>, without interpreting format specifiers.
   *
   * <p>Uses {@link JavadocDto#setDescription(String)}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.description("Rate 100%");
   * }</pre>
   *
   * @param description the main description text (may be null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder description(String description) {
    this.description = changedValue(description);
    return this;
  }

  /**
   * Adds a single element to <code>tags</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public JavadocDtoBuilder add2Tags(JavadocTagDto element) {
    List<JavadocTagDto> newCollection;
    if (this.tags.isSet()) {
      newCollection = new ArrayList<>(this.tags.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.tags = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tags the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(JavadocTagDto... tags) {
    this.tags = changedValue(List.of(tags));
    return this;
  }

  /**
   * Sets the value for <code>tags</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tagsBuilderConsumer consumer providing an instance of a builder for the replacement
   *     collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(Consumer<ArrayListBuilder<JavadocTagDto>> tagsBuilderConsumer) {
    ArrayListBuilder<JavadocTagDto> builder =
        this.tags.isSet()
            ? new ArrayListBuilder<JavadocTagDto>(this.tags.value())
            : new ArrayListBuilder<JavadocTagDto>();
    tagsBuilderConsumer.accept(builder);
    this.tags = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>tags</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tagsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(Supplier<List<JavadocTagDto>> tagsSupplier) {
    this.tags = changedValue(tagsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tags the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(List<JavadocTagDto> tags) {
    this.tags = changedValue(tags);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public JavadocDtoBuilder conditional(
      BooleanSupplier condition, Consumer<JavadocDtoBuilder> yesCondition) {
    return conditional(condition, yesCondition, null);
  }

  /**
   * Conditionally applies builder modifications based on a condition evaluation.
   *
   * @param condition the condition to evaluate
   * @param trueCase the consumer to apply if condition is true
   * @param falseCase the consumer to apply if condition is false (can be null)
   * @return this builder instance
   */
  public JavadocDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<JavadocDtoBuilder> trueCase,
      Consumer<JavadocDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a {@code @param} tag with formatted description (shortcut mirroring {@link
   * JavadocDto#addParam}).
   *
   * @param paramName the parameter name
   * @param descriptionFormat the format string for the parameter description
   * @param args optional arguments referenced by format specifiers
   * @return current instance of builder
   */
  public JavadocDtoBuilder param(String paramName, String descriptionFormat, Object... args) {
    return add2Tags(
        new JavadocTagDto(
            JavadocTag.PARAM, paramName + " " + String.format(descriptionFormat, args)));
  }

  /**
   * Adds a {@code @return} tag with formatted description (shortcut mirroring {@link
   * JavadocDto#addReturn}).
   *
   * @param descriptionFormat the format string for the return description
   * @param args optional arguments referenced by format specifiers
   * @return current instance of builder
   */
  public JavadocDtoBuilder returns(String descriptionFormat, Object... args) {
    return add2Tags(new JavadocTagDto(JavadocTag.RETURN, String.format(descriptionFormat, args)));
  }

  /**
   * Adds a {@code @throws} tag with formatted description (shortcut mirroring {@link
   * JavadocDto#addThrows}).
   *
   * @param exceptionName the exception name
   * @param descriptionFormat the format string for the exception description
   * @param args optional arguments referenced by format specifiers
   * @return current instance of builder
   */
  public JavadocDtoBuilder throwsTag(
      String exceptionName, String descriptionFormat, Object... args) {
    return add2Tags(
        new JavadocTagDto(
            JavadocTag.THROWS, exceptionName + " " + String.format(descriptionFormat, args)));
  }

  /**
   * Adds a {@code @deprecated} tag with formatted description (shortcut mirroring {@link
   * JavadocDto#addDeprecated}).
   *
   * @param descriptionFormat the format string for the deprecation description
   * @param args optional arguments referenced by format specifiers
   * @return current instance of builder
   */
  public JavadocDtoBuilder deprecated(String descriptionFormat, Object... args) {
    return add2Tags(
        new JavadocTagDto(JavadocTag.DEPRECATED, String.format(descriptionFormat, args)));
  }

  /**
   * Adds a tag with the given name and value (shortcut mirroring {@link JavadocDto#addTag}).
   *
   * @param tagName the tag name without @ prefix
   * @param tagValue the tag value
   * @return current instance of builder
   */
  public JavadocDtoBuilder add2Tags(String tagName, String tagValue) {
    return add2Tags(new JavadocTagDto(tagName, tagValue));
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * JavadocDto result = builder.build();
   * }</pre>
   */
  @Override
  public JavadocDto build() {
    JavadocDto result = new JavadocDto();
    this.description.ifSet(result::setDescription);
    this.exampleUsageCodeBlock.ifSet(result::setExampleUsageCodeBlock);
    this.tags.ifSet(result::setTags);
    return result;
  }

  /**
   * Returns a string representation of this builder, including only fields that have been set.
   *
   * @return string representation of the builder
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)
        .append("description", this.description)
        .append("exampleUsageCodeBlock", this.exampleUsageCodeBlock)
        .append("tags", this.tags)
        .toString();
  }

  /** Interface that can be implemented by the DTO to provide fluent modification methods. */
  public interface With {
    /**
     * Initializes a builder from an instance of this class, using methods of this builder to change
     * values and returns the new built object.
     *
     * @param b the consumer to apply modifications
     * @return the modified instance
     */
    default JavadocDto with(Consumer<JavadocDtoBuilder> b) {
      JavadocDtoBuilder builder;
      try {
        builder = new JavadocDtoBuilder(JavadocDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'JavadocDtoBuilder.With' should only be implemented by classes, which could be casted to 'JavadocDto'",
            ex);
      }
      b.accept(builder);
      return builder.build();
    }

    /**
     * Creates a builder initialized from this instance.
     *
     * @return a builder initialized with this instance's values
     */
    default JavadocDtoBuilder with() {
      try {
        return new JavadocDtoBuilder(JavadocDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'JavadocDtoBuilder.With' should only be implemented by classes, which could be casted to 'JavadocDto'",
            ex);
      }
    }
  }
}
