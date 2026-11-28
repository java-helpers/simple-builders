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

package org.javahelpers.simple.builders.processor.model.core;

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.simple.builders.processor.model.core.GetterInfoDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.simple.builders.processor.model.core.GetterInfoDto with method chaining and
 * validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the
 * final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * GetterInfoDto result = GetterInfoDtoBuilder.create()
 *     .getterName("example getterName")
 *     .getterName("Hello %s", "World")
 *     .getterName(() -> "example getterName")
 *     .getterName(sb -> sb.append("text"))
 *     .deprecated(true)
 *     .deprecated(() -> true)
 *     .build();
 * }</pre>
 */
public class GetterInfoDtoBuilder implements IBuilderBase<GetterInfoDto> {

  /** Tracked value for <code>getterName</code>: getterName. */
  private TrackedValue<String> getterName = unsetValue();

  /** Tracked value for <code>deprecated</code>: deprecated. */
  private TrackedValue<Boolean> deprecated = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.GetterInfoDto}.
   */
  public GetterInfoDtoBuilder() {}

  /**
   * Initialisation of builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.GetterInfoDto} by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public GetterInfoDtoBuilder(GetterInfoDto instance) {
    this.getterName = initialValue(instance.getGetterName());
    this.deprecated = initialValue(instance.deprecated());
  }

  /**
   * Creating a new builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.GetterInfoDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * GetterInfoDtoBuilder builder = GetterInfoDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.simple.builders.processor.model.core.GetterInfoDto}
   */
  public static GetterInfoDtoBuilder create() {
    return new GetterInfoDtoBuilder();
  }

  /**
   * Sets the value for <code>deprecated</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link GetterInfoDto#GetterInfoDto(String, boolean)
   * GetterInfoDto(String getterName, boolean deprecated)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.deprecated(() -> true);
   * }</pre>
   *
   * @param deprecatedSupplier boolean supplier for deprecated
   * @return current instance of builder
   */
  public GetterInfoDtoBuilder deprecated(BooleanSupplier deprecatedSupplier) {
    this.deprecated = changedValue(deprecatedSupplier.getAsBoolean());
    return this;
  }

  /**
   * Sets the value for <code>deprecated</code>.
   *
   * <p>Generated from parameter in constructor {@link GetterInfoDto#GetterInfoDto(String, boolean)
   * GetterInfoDto(String getterName, boolean deprecated)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.deprecated(true);
   * }</pre>
   *
   * @param deprecated deprecated
   * @return current instance of builder
   */
  public GetterInfoDtoBuilder deprecated(boolean deprecated) {
    this.deprecated = changedValue(deprecated);
    return this;
  }

  /**
   * Sets the value for <code>getterName</code> by executing the provided consumer.
   *
   * <p>Generated from parameter in constructor {@link GetterInfoDto#GetterInfoDto(String, boolean)
   * GetterInfoDto(String getterName, boolean deprecated)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param getterNameStringBuilderConsumer consumer providing an instance of getterName
   * @return current instance of builder
   */
  public GetterInfoDtoBuilder getterName(Consumer<StringBuilder> getterNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    getterNameStringBuilderConsumer.accept(builder);
    this.getterName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>getterName</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link GetterInfoDto#GetterInfoDto(String, boolean)
   * GetterInfoDto(String getterName, boolean deprecated)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterName(() -> "example getterName");
   * }</pre>
   *
   * @param getterNameSupplier supplier for getterName
   * @return current instance of builder
   */
  public GetterInfoDtoBuilder getterName(Supplier<String> getterNameSupplier) {
    this.getterName = changedValue(getterNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>getterName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from parameter in constructor {@link GetterInfoDto#GetterInfoDto(String, boolean)
   * GetterInfoDto(String getterName, boolean deprecated)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public GetterInfoDtoBuilder getterName(String format, Object... args) {
    this.getterName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>getterName</code>.
   *
   * <p>Generated from parameter in constructor {@link GetterInfoDto#GetterInfoDto(String, boolean)
   * GetterInfoDto(String getterName, boolean deprecated)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterName("example getterName");
   * }</pre>
   *
   * @param getterName getterName
   * @return current instance of builder
   */
  public GetterInfoDtoBuilder getterName(String getterName) {
    this.getterName = changedValue(getterName);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public GetterInfoDtoBuilder conditional(
      BooleanSupplier condition, Consumer<GetterInfoDtoBuilder> yesCondition) {
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
  public GetterInfoDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<GetterInfoDtoBuilder> trueCase,
      Consumer<GetterInfoDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * GetterInfoDto result = builder.build();
   * }</pre>
   */
  @Override
  public GetterInfoDto build() {
    if (!this.deprecated.isSet()) {
      throw new IllegalStateException(
          "Required field 'deprecated' must be set before calling build()");
    }
    if (this.deprecated.value() == null) {
      throw new IllegalStateException(
          "Field 'deprecated' is marked as non-null but null value was provided");
    }
    return new GetterInfoDto(this.getterName.value(), this.deprecated.value());
  }

  /**
   * Returns a string representation of this builder, including only fields that have been set.
   *
   * @return string representation of the builder
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)
        .append("getterName", this.getterName)
        .append("deprecated", this.deprecated)
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
    default GetterInfoDto with(Consumer<GetterInfoDtoBuilder> b) {
      GetterInfoDtoBuilder builder;
      try {
        builder = new GetterInfoDtoBuilder(GetterInfoDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'GetterInfoDtoBuilder.With' should only be implemented by classes, which could be casted to 'GetterInfoDto'",
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
    default GetterInfoDtoBuilder with() {
      try {
        return new GetterInfoDtoBuilder(GetterInfoDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'GetterInfoDtoBuilder.With' should only be implemented by classes, which could be casted to 'GetterInfoDto'",
            ex);
      }
    }
  }
}
