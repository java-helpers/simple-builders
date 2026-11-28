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

package org.javahelpers.codegen.model.type;

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
 * Builder for {@code org.javahelpers.codegen.model.type.GenericParameterDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.type.GenericParameterDto with method chaining and validation. Use
 * the static {@code create()} method to obtain a new builder instance, configure the desired
 * properties using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * GenericParameterDto result = GenericParameterDtoBuilder.create()
 *     .name("example name")
 *     .name("Hello %s", "World")
 *     .name(() -> "example name")
 *     .name(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class GenericParameterDtoBuilder implements IBuilderBase<GenericParameterDto> {

  /** Tracked value for <code>name</code>: the name to set (e.g., "T", "K", "V"). */
  private TrackedValue<String> name = unsetValue();

  /** Tracked value for <code>upperBounds</code>: the replacement collection (not null). */
  private TrackedValue<List<TypeName>> upperBounds = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.codegen.model.type.GenericParameterDto}.
   */
  public GenericParameterDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.type.GenericParameterDto} by
   * a instance.
   *
   * @param instance object instance for initialisiation
   */
  public GenericParameterDtoBuilder(GenericParameterDto instance) {
    this.name = initialValue(instance.getName());
    this.upperBounds = initialValue(instance.getUpperBounds());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.type.GenericParameterDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * GenericParameterDtoBuilder builder = GenericParameterDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.type.GenericParameterDto}
   */
  public static GenericParameterDtoBuilder create() {
    return new GenericParameterDtoBuilder();
  }

  /**
   * Sets the value for <code>name</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link GenericParameterDto#setName(String) setName(String name)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.name(sb -> sb.append("text"));
   * }</pre>
   *
   * @param nameStringBuilderConsumer consumer providing an instance of the name to set (e.g., "T",
   *     "K", "V")
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder name(Consumer<StringBuilder> nameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    nameStringBuilderConsumer.accept(builder);
    this.name = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>name</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenericParameterDto#setName(String) setName(String name)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.name(() -> "example name");
   * }</pre>
   *
   * @param nameSupplier supplier for the name to set (e.g., "T", "K", "V")
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder name(Supplier<String> nameSupplier) {
    this.name = changedValue(nameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>name</code> by using String.format(format, args). See {@link
   * String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link GenericParameterDto#setName(String) setName(String name)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.name("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder name(String format, Object... args) {
    this.name = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>name</code>.
   *
   * <p>Generated from setter {@link GenericParameterDto#setName(String) setName(String name)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.name("example name");
   * }</pre>
   *
   * @param name the name to set (e.g., "T", "K", "V")
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder name(String name) {
    this.name = changedValue(name);
    return this;
  }

  /**
   * Adds a single element to <code>upperBounds</code>.
   *
   * <p>Generated from setter {@link GenericParameterDto#setUpperBounds(List)
   * setUpperBounds(List<TypeName> upperBounds)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder add2UpperBounds(TypeName element) {
    List<TypeName> newCollection;
    if (this.upperBounds.isSet()) {
      newCollection = new ArrayList<>(this.upperBounds.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.upperBounds = changedValue(newCollection);
    return this;
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsList(Class<?> elementClass) {
    return add2UpperBounds(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsList(TypeName elementType) {
    return add2UpperBounds(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsSet(Class<?> elementClass) {
    return add2UpperBounds(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsSet(TypeName elementType) {
    return add2UpperBounds(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsArray(Class<?> innerClass) {
    return add2UpperBounds(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsArray(TypeName innerType) {
    return add2UpperBounds(TypeNameArray.of(innerType));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameGeneric built from the given generic outer type
   * and type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsGeneric(
      Class<?> outerType, Class<?>... innerTypeArguments) {
    return add2UpperBounds(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameGeneric built from the given generic outer type
   * and type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsGeneric(
      TypeName outerType, TypeName... innerTypeArguments) {
    return add2UpperBounds(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameMap built from the given map key and value
   * types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsMap(Class<?> keyClass, Class<?> valueClass) {
    return add2UpperBounds(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>add2UpperBounds</code> to a TypeNameMap built from the given map key and value
   * types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBoundsMap(TypeName keyType, TypeName valueType) {
    return add2UpperBounds(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>add2UpperBounds</code> by class.
   *
   * @param element the class to set as <code>add2UpperBounds</code>
   * @return fluent {@code builder} instance
   */
  public GenericParameterDtoBuilder add2UpperBounds(Class<?> element) {
    return add2UpperBounds(TypeName.of(element));
  }

  /**
   * Sets the value for <code>upperBounds</code>.
   *
   * <p>Generated from setter {@link GenericParameterDto#setUpperBounds(List)
   * setUpperBounds(List<TypeName> upperBounds)}
   *
   * @param upperBounds the replacement collection (not null)
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder upperBounds(TypeName... upperBounds) {
    this.upperBounds = changedValue(List.of(upperBounds));
    return this;
  }

  /**
   * Sets the value for <code>upperBounds</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenericParameterDto#setUpperBounds(List)
   * setUpperBounds(List<TypeName> upperBounds)}
   *
   * @param upperBoundsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder upperBounds(
      Consumer<ArrayListBuilder<TypeName>> upperBoundsBuilderConsumer) {
    ArrayListBuilder<TypeName> builder =
        this.upperBounds.isSet()
            ? new ArrayListBuilder<TypeName>(this.upperBounds.value())
            : new ArrayListBuilder<TypeName>();
    upperBoundsBuilderConsumer.accept(builder);
    this.upperBounds = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>upperBounds</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenericParameterDto#setUpperBounds(List)
   * setUpperBounds(List<TypeName> upperBounds)}
   *
   * @param upperBoundsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder upperBounds(Supplier<List<TypeName>> upperBoundsSupplier) {
    this.upperBounds = changedValue(upperBoundsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>upperBounds</code>.
   *
   * <p>Generated from setter {@link GenericParameterDto#setUpperBounds(List)
   * setUpperBounds(List<TypeName> upperBounds)}
   *
   * @param upperBounds the replacement collection (not null)
   * @return current instance of builder
   */
  public GenericParameterDtoBuilder upperBounds(List<TypeName> upperBounds) {
    this.upperBounds = changedValue(upperBounds);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public GenericParameterDtoBuilder conditional(
      BooleanSupplier condition, Consumer<GenericParameterDtoBuilder> yesCondition) {
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
  public GenericParameterDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<GenericParameterDtoBuilder> trueCase,
      Consumer<GenericParameterDtoBuilder> falseCase) {
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
   * GenericParameterDto result = builder.build();
   * }</pre>
   */
  @Override
  public GenericParameterDto build() {
    GenericParameterDto result = new GenericParameterDto();
    this.name.ifSet(result::setName);
    this.upperBounds.ifSet(result::setUpperBounds);
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
        .append("name", this.name)
        .append("upperBounds", this.upperBounds)
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
    default GenericParameterDto with(Consumer<GenericParameterDtoBuilder> b) {
      GenericParameterDtoBuilder builder;
      try {
        builder = new GenericParameterDtoBuilder(GenericParameterDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'GenericParameterDtoBuilder.With' should only be implemented by classes, which could be casted to 'GenericParameterDto'",
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
    default GenericParameterDtoBuilder with() {
      try {
        return new GenericParameterDtoBuilder(GenericParameterDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'GenericParameterDtoBuilder.With' should only be implemented by classes, which could be casted to 'GenericParameterDto'",
            ex);
      }
    }
  }
}
