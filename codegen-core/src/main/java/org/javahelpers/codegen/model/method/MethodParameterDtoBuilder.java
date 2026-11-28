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

package org.javahelpers.codegen.model.method;

import static org.javahelpers.codegen.builders.TrackedValue.changedValue;
import static org.javahelpers.codegen.builders.TrackedValue.initialValue;
import static org.javahelpers.codegen.builders.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameSet;

/**
 * Builder for {@code org.javahelpers.codegen.model.method.MethodParameterDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.method.MethodParameterDto with method chaining and validation. Use
 * the static {@code create()} method to obtain a new builder instance, configure the desired
 * properties using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * MethodParameterDto result = MethodParameterDtoBuilder.create()
 *     .annotations(List.of(AnnotationDtoBuilder.create().build()))
 *     .annotations(() -> List.of(AnnotationDtoBuilder.create().build()))
 *     .add2Annotations(element -> {})
 *     .add2Annotations(first -> {}, second -> {})
 *     .add2Annotations(AnnotationDtoBuilder.create().build())
 *     .parameterName("example parameterName")
 *     .parameterName("Hello %s", "World")
 *     .parameterName(() -> "example parameterName")
 *     .parameterName(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class MethodParameterDtoBuilder implements IBuilderBase<MethodParameterDto> {

  /** Tracked value for <code>annotations</code>: the replacement collection (not null). */
  private TrackedValue<List<AnnotationDto>> annotations = unsetValue();

  /** Tracked value for <code>parameterName</code>: parameter name. */
  private TrackedValue<String> parameterName = unsetValue();

  /** Tracked value for <code>parameterTypeName</code>: type of parameter. */
  private TrackedValue<TypeName> parameterTypeName = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.codegen.model.method.MethodParameterDto}.
   */
  public MethodParameterDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.method.MethodParameterDto}
   * by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public MethodParameterDtoBuilder(MethodParameterDto instance) {
    this.annotations = initialValue(instance.getAnnotations());
    this.parameterName = initialValue(instance.getParameterName());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.method.MethodParameterDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * MethodParameterDtoBuilder builder = MethodParameterDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.method.MethodParameterDto}
   */
  public static MethodParameterDtoBuilder create() {
    return new MethodParameterDtoBuilder();
  }

  /**
   * Adds a single element to <code>annotations</code>.
   *
   * <p>Generated from setter {@link MethodParameterDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2Annotations(AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder add2Annotations(AnnotationDto element) {
    List<AnnotationDto> newCollection;
    if (this.annotations.isSet()) {
      newCollection = new ArrayList<>(this.annotations.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.annotations = changedValue(newCollection);
    return this;
  }

  /**
   * Adds one annotation of the given type to <code>annotations</code>.
   *
   * @param annotationType the annotation type (not null)
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder add2Annotations(Class<?> annotationType) {
    return add2Annotations(AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>annotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder add2Annotations(String packageName, String className) {
    return add2Annotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>annotations</code>, configured through its builder.
   *
   * @param annotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder add2Annotations(
      Consumer<AnnotationDtoBuilder> annotationBuilderConsumer) {
    AnnotationDtoBuilder elementBuilder = AnnotationDtoBuilder.create();
    annotationBuilderConsumer.accept(elementBuilder);
    return add2Annotations(elementBuilder.build());
  }

  /**
   * Appends elements to <code>annotations</code>, each configured through its builder.
   *
   * @param annotationBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final MethodParameterDtoBuilder add2Annotations(
      Consumer<AnnotationDtoBuilder>... annotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : annotationBuilderConsumers) {
      add2Annotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodParameterDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(() -> List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param annotationsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder annotations(Supplier<List<AnnotationDto>> annotationsSupplier) {
    this.annotations = changedValue(annotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link MethodParameterDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param annotations the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder annotations(List<AnnotationDto> annotations) {
    this.annotations = changedValue(annotations);
    return this;
  }

  /**
   * Sets the value for <code>parameterName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link MethodParameterDto#setParameterName(String)
   * setParameterName(String parameterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param parameterNameStringBuilderConsumer consumer providing an instance of parameter name
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterName(
      Consumer<StringBuilder> parameterNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    parameterNameStringBuilderConsumer.accept(builder);
    this.parameterName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>parameterName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodParameterDto#setParameterName(String)
   * setParameterName(String parameterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterName(() -> "example parameterName");
   * }</pre>
   *
   * @param parameterNameSupplier supplier for parameter name
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterName(Supplier<String> parameterNameSupplier) {
    this.parameterName = changedValue(parameterNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>parameterName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link MethodParameterDto#setParameterName(String)
   * setParameterName(String parameterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterName(String format, Object... args) {
    this.parameterName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>parameterName</code>.
   *
   * <p>Generated from setter {@link MethodParameterDto#setParameterName(String)
   * setParameterName(String parameterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterName("example parameterName");
   * }</pre>
   *
   * @param parameterName parameter name
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterName(String parameterName) {
    this.parameterName = changedValue(parameterName);
    return this;
  }

  /**
   * Sets the value for <code>parameterTypeName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodParameterDto#setParameterTypeName(TypeName)
   * setParameterTypeName(TypeName parameterTypeName)}
   *
   * @param parameterTypeNameSupplier supplier for type of parameter
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterTypeName(Supplier<TypeName> parameterTypeNameSupplier) {
    this.parameterTypeName = changedValue(parameterTypeNameSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>parameterTypeName</code>.
   *
   * <p>Generated from setter {@link MethodParameterDto#setParameterTypeName(TypeName)
   * setParameterTypeName(TypeName parameterTypeName)}
   *
   * @param parameterTypeName type of parameter
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterTypeName(TypeName parameterTypeName) {
    this.parameterTypeName = changedValue(parameterTypeName);
    return this;
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameList(Class<?> elementClass) {
    return parameterTypeName(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameList(TypeName elementType) {
    return parameterTypeName(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameSet(Class<?> elementClass) {
    return parameterTypeName(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameSet(TypeName elementType) {
    return parameterTypeName(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameArray built from the given array component
   * type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameArray(Class<?> innerClass) {
    return parameterTypeName(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameArray built from the given array component
   * type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameArray(TypeName innerType) {
    return parameterTypeName(TypeNameArray.of(innerType));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameGeneric built from the given generic outer
   * type and type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameGeneric(
      Class<?> outerType, Class<?>... innerTypeArguments) {
    return parameterTypeName(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameGeneric built from the given generic outer
   * type and type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameGeneric(
      TypeName outerType, TypeName... innerTypeArguments) {
    return parameterTypeName(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameMap built from the given map key and value
   * types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameMap(Class<?> keyClass, Class<?> valueClass) {
    return parameterTypeName(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>parameterTypeName</code> to a TypeNameMap built from the given map key and value
   * types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeNameMap(TypeName keyType, TypeName valueType) {
    return parameterTypeName(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>parameterTypeName</code> by class.
   *
   * @param parameterTypeName the class to set as <code>parameterTypeName</code>
   * @return fluent {@code builder} instance
   */
  public MethodParameterDtoBuilder parameterTypeName(Class<?> parameterTypeName) {
    return parameterTypeName(TypeName.of(parameterTypeName));
  }

  /**
   * Sets the value for <code>parameterTypeName</code> from package and class name.
   *
   * @param packageName the package of the type
   * @param className the simple name of the type
   * @return current instance of builder
   */
  public MethodParameterDtoBuilder parameterTypeName(String packageName, String className) {
    return parameterTypeName(new TypeName(packageName, className));
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public MethodParameterDtoBuilder conditional(
      BooleanSupplier condition, Consumer<MethodParameterDtoBuilder> yesCondition) {
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
  public MethodParameterDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<MethodParameterDtoBuilder> trueCase,
      Consumer<MethodParameterDtoBuilder> falseCase) {
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
   * MethodParameterDto result = builder.build();
   * }</pre>
   */
  @Override
  public MethodParameterDto build() {
    MethodParameterDto result = new MethodParameterDto();
    this.annotations.ifSet(result::setAnnotations);
    this.parameterName.ifSet(result::setParameterName);
    this.parameterTypeName.ifSet(result::setParameterTypeName);
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
        .append("annotations", this.annotations)
        .append("parameterName", this.parameterName)
        .append("parameterTypeName", this.parameterTypeName)
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
    default MethodParameterDto with(Consumer<MethodParameterDtoBuilder> b) {
      MethodParameterDtoBuilder builder;
      try {
        builder = new MethodParameterDtoBuilder(MethodParameterDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'MethodParameterDtoBuilder.With' should only be implemented by classes, which could be casted to 'MethodParameterDto'",
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
    default MethodParameterDtoBuilder with() {
      try {
        return new MethodParameterDtoBuilder(MethodParameterDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'MethodParameterDtoBuilder.With' should only be implemented by classes, which could be casted to 'MethodParameterDto'",
            ex);
      }
    }
  }
}
