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

package org.javahelpers.codegen.model.annotation;

import static org.javahelpers.codegen.builders.TrackedValue.changedValue;
import static org.javahelpers.codegen.builders.TrackedValue.initialValue;
import static org.javahelpers.codegen.builders.TrackedValue.unsetValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.HashMapBuilder;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;
import org.javahelpers.codegen.model.type.TypeName;

/**
 * Builder for {@code org.javahelpers.codegen.model.annotation.AnnotationDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.annotation.AnnotationDto with method chaining and validation. Use
 * the static {@code create()} method to obtain a new builder instance, configure the desired
 * properties using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * AnnotationDto result = AnnotationDtoBuilder.create()
 *     .members(Map.of("example members", "example members"))
 *     .members(() -> Map.of("example members", "example members"))
 *     .members(Map.entry("key", "example members"))
 *     .build();
 * }</pre>
 */
public class AnnotationDtoBuilder implements IBuilderBase<AnnotationDto> {

  /** Tracked value for <code>annotationType</code>: the type of the annotation. */
  private TrackedValue<TypeName> annotationType = unsetValue();

  /** Tracked value for <code>members</code>: the replacement collection (not null). */
  private TrackedValue<Map<String, String>> members = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.codegen.model.annotation.AnnotationDto}.
   */
  public AnnotationDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.annotation.AnnotationDto} by
   * a instance.
   *
   * @param instance object instance for initialisiation
   */
  public AnnotationDtoBuilder(AnnotationDto instance) {
    this.annotationType = initialValue(instance.getAnnotationType());
    this.members = initialValue(instance.getMembers());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.annotation.AnnotationDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * AnnotationDtoBuilder builder = AnnotationDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.annotation.AnnotationDto}
   */
  public static AnnotationDtoBuilder create() {
    return new AnnotationDtoBuilder();
  }

  /**
   * Sets the value for <code>annotationType</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link AnnotationDto#setAnnotationType(TypeName)
   * setAnnotationType(TypeName annotationType)}
   *
   * @param annotationTypeSupplier supplier for the type of the annotation
   * @return current instance of builder
   */
  public AnnotationDtoBuilder annotationType(Supplier<TypeName> annotationTypeSupplier) {
    this.annotationType = changedValue(annotationTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotationType</code>.
   *
   * <p>Generated from setter {@link AnnotationDto#setAnnotationType(TypeName)
   * setAnnotationType(TypeName annotationType)}
   *
   * @param annotationType the type of the annotation
   * @return current instance of builder
   */
  public AnnotationDtoBuilder annotationType(TypeName annotationType) {
    this.annotationType = changedValue(annotationType);
    return this;
  }

  /**
   * Sets <code>annotationType</code> by class.
   *
   * @param annotationType the class to set as <code>annotationType</code>
   * @return fluent {@code builder} instance
   */
  public AnnotationDtoBuilder annotationType(Class<?> annotationType) {
    return annotationType(TypeName.of(annotationType));
  }

  /**
   * Sets the value for <code>annotationType</code> from package and class name.
   *
   * @param packageName the package of the type
   * @param className the simple name of the type
   * @return current instance of builder
   */
  public AnnotationDtoBuilder annotationType(String packageName, String className) {
    return annotationType(new TypeName(packageName, className));
  }

  /**
   * Sets the value for <code>members</code>.
   *
   * <p>Generated from setter {@link AnnotationDto#setMembers(Map) setMembers(Map<String, String>
   * members)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.members(Map.entry("key", "example members"));
   * }</pre>
   *
   * @param members the replacement collection (not null)
   * @return current instance of builder
   */
  public AnnotationDtoBuilder members(Entry<String, String>... members) {
    this.members = changedValue(Map.ofEntries(members));
    return this;
  }

  /**
   * Adds a single member to the annotation.
   *
   * @param key member name
   * @param value member value
   * @return this builder
   */
  public AnnotationDtoBuilder add2Members(String key, String value) {
    Map<String, String> newMap =
        this.members.isSet() ? new LinkedHashMap<>(this.members.value()) : new LinkedHashMap<>();
    newMap.put(key, value);
    this.members = changedValue(newMap);
    return this;
  }

  /**
   * Sets the value for <code>members</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link AnnotationDto#setMembers(Map) setMembers(Map<String, String>
   * members)}
   *
   * @param membersBuilderConsumer consumer providing an instance of a builder for the replacement
   *     collection (not null)
   * @return current instance of builder
   */
  public AnnotationDtoBuilder members(
      Consumer<HashMapBuilder<String, String>> membersBuilderConsumer) {
    HashMapBuilder<String, String> builder =
        this.members.isSet()
            ? new HashMapBuilder<String, String>(this.members.value())
            : new HashMapBuilder<String, String>();
    membersBuilderConsumer.accept(builder);
    this.members = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>members</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link AnnotationDto#setMembers(Map) setMembers(Map<String, String>
   * members)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.members(() -> Map.of("example members", "example members"));
   * }</pre>
   *
   * @param membersSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public AnnotationDtoBuilder members(Supplier<Map<String, String>> membersSupplier) {
    this.members = changedValue(membersSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>members</code>.
   *
   * <p>Generated from setter {@link AnnotationDto#setMembers(Map) setMembers(Map<String, String>
   * members)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.members(Map.of("example members", "example members"));
   * }</pre>
   *
   * @param members the replacement collection (not null)
   * @return current instance of builder
   */
  public AnnotationDtoBuilder members(Map<String, String> members) {
    this.members = changedValue(members);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public AnnotationDtoBuilder conditional(
      BooleanSupplier condition, Consumer<AnnotationDtoBuilder> yesCondition) {
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
  public AnnotationDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<AnnotationDtoBuilder> trueCase,
      Consumer<AnnotationDtoBuilder> falseCase) {
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
   * AnnotationDto result = builder.build();
   * }</pre>
   */
  @Override
  public AnnotationDto build() {
    AnnotationDto result = new AnnotationDto();
    this.annotationType.ifSet(result::setAnnotationType);
    this.members.ifSet(result::setMembers);
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
        .append("annotationType", this.annotationType)
        .append("members", this.members)
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
    default AnnotationDto with(Consumer<AnnotationDtoBuilder> b) {
      AnnotationDtoBuilder builder;
      try {
        builder = new AnnotationDtoBuilder(AnnotationDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'AnnotationDtoBuilder.With' should only be implemented by classes, which could be casted to 'AnnotationDto'",
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
    default AnnotationDtoBuilder with() {
      try {
        return new AnnotationDtoBuilder(AnnotationDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'AnnotationDtoBuilder.With' should only be implemented by classes, which could be casted to 'AnnotationDto'",
            ex);
      }
    }
  }
}
