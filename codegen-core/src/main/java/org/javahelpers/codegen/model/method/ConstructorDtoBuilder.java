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
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;

/**
 * Builder for {@code org.javahelpers.codegen.model.method.ConstructorDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.method.ConstructorDto with method chaining and validation. Use the
 * static {@code create()} method to obtain a new builder instance, configure the desired properties
 * using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * ConstructorDto result = ConstructorDtoBuilder.create()
 *     .annotations(List.of(AnnotationDtoBuilder.create().build()))
 *     .annotations(() -> List.of(AnnotationDtoBuilder.create().build()))
 *     .add2Annotations(element -> {})
 *     .add2Annotations(first -> {}, second -> {})
 *     .add2Annotations(AnnotationDtoBuilder.create().build())
 *     .javadoc(new JavadocDto())
 *     .javadoc(JavadocDto::new)
 *     .javadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .methodCodeDto(new MethodCodeDto())
 *     .methodCodeDto(MethodCodeDto::new)
 *     .methodCodeDto(methodCodeDtoBuilder -> methodCodeDtoBuilder)
 *     .parameters(List.of(MethodParameterDtoBuilder.create().build()))
 *     .parameters(() -> List.of(MethodParameterDtoBuilder.create().build()))
 *     .add2Parameters(element -> {})
 *     .add2Parameters(first -> {}, second -> {})
 *     .add2Parameters(MethodParameterDtoBuilder.create().build())
 *     .build();
 * }</pre>
 */
public class ConstructorDtoBuilder implements IBuilderBase<ConstructorDto> {

  /** Tracked value for <code>annotations</code>: the replacement collection (not null). */
  private TrackedValue<List<AnnotationDto>> annotations = unsetValue();

  /** Tracked value for <code>javadoc</code>: javadoc. */
  private TrackedValue<JavadocDto> javadoc = unsetValue();

  /** Tracked value for <code>methodCodeDto</code>: the method code DTO. */
  private TrackedValue<MethodCodeDto> methodCodeDto = unsetValue();

  /** Tracked value for <code>parameters</code>: the replacement collection (not null). */
  private TrackedValue<List<MethodParameterDto>> parameters = unsetValue();

  /** Tracked value for <code>visibility</code>: visibility. */
  private TrackedValue<AccessModifier> visibility = unsetValue();

  /**
   * Empty constructor of builder for {@code org.javahelpers.codegen.model.method.ConstructorDto}.
   */
  public ConstructorDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.method.ConstructorDto} by a
   * instance.
   *
   * @param instance object instance for initialisiation
   */
  public ConstructorDtoBuilder(ConstructorDto instance) {
    this.annotations = initialValue(instance.getAnnotations());
    this.javadoc = initialValue(instance.getJavadoc());
    this.methodCodeDto = initialValue(instance.getMethodCodeDto());
    this.parameters = initialValue(instance.getParameters());
    this.visibility = initialValue(instance.getVisibility());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.method.ConstructorDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * ConstructorDtoBuilder builder = ConstructorDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.method.ConstructorDto}
   */
  public static ConstructorDtoBuilder create() {
    return new ConstructorDtoBuilder();
  }

  /**
   * Adds a single element to <code>annotations</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setAnnotations(List)
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
  public ConstructorDtoBuilder add2Annotations(AnnotationDto element) {
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
  public ConstructorDtoBuilder add2Annotations(Class<?> annotationType) {
    return add2Annotations(AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>annotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public ConstructorDtoBuilder add2Annotations(String packageName, String className) {
    return add2Annotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>annotations</code>, configured through its builder.
   *
   * @param annotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public ConstructorDtoBuilder add2Annotations(
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
  public final ConstructorDtoBuilder add2Annotations(
      Consumer<AnnotationDtoBuilder>... annotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : annotationBuilderConsumers) {
      add2Annotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ConstructorDto#setAnnotations(List)
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
  public ConstructorDtoBuilder annotations(Supplier<List<AnnotationDto>> annotationsSupplier) {
    this.annotations = changedValue(annotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setAnnotations(List)
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
  public ConstructorDtoBuilder annotations(List<AnnotationDto> annotations) {
    this.annotations = changedValue(annotations);
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link ConstructorDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javadoc(javadocDtoBuilder -> javadocDtoBuilder);
   * }</pre>
   *
   * @param javadocBuilderConsumer consumer providing an instance of a builder for javadoc
   * @return current instance of builder
   */
  public ConstructorDtoBuilder javadoc(Consumer<JavadocDtoBuilder> javadocBuilderConsumer) {
    JavadocDtoBuilder builder =
        this.javadoc.isSet()
            ? new JavadocDtoBuilder(this.javadoc.value())
            : JavadocDtoBuilder.create();
    javadocBuilderConsumer.accept(builder);
    this.javadoc = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ConstructorDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javadoc(JavadocDto::new);
   * }</pre>
   *
   * @param javadocSupplier supplier for javadoc
   * @return current instance of builder
   */
  public ConstructorDtoBuilder javadoc(Supplier<JavadocDto> javadocSupplier) {
    this.javadoc = changedValue(javadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javadoc(new JavadocDto());
   * }</pre>
   *
   * @param javadoc javadoc
   * @return current instance of builder
   */
  public ConstructorDtoBuilder javadoc(JavadocDto javadoc) {
    this.javadoc = changedValue(javadoc);
    return this;
  }

  /**
   * Sets the value for <code>methodCodeDto</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link ConstructorDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodCodeDto(methodCodeDtoBuilder -> methodCodeDtoBuilder);
   * }</pre>
   *
   * @param methodCodeDtoBuilderConsumer consumer providing an instance of a builder for the method
   *     code DTO
   * @return current instance of builder
   */
  public ConstructorDtoBuilder methodCodeDto(
      Consumer<MethodCodeDtoBuilder> methodCodeDtoBuilderConsumer) {
    MethodCodeDtoBuilder builder =
        this.methodCodeDto.isSet()
            ? new MethodCodeDtoBuilder(this.methodCodeDto.value())
            : MethodCodeDtoBuilder.create();
    methodCodeDtoBuilderConsumer.accept(builder);
    this.methodCodeDto = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>methodCodeDto</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ConstructorDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodCodeDto(MethodCodeDto::new);
   * }</pre>
   *
   * @param methodCodeDtoSupplier supplier for the method code DTO
   * @return current instance of builder
   */
  public ConstructorDtoBuilder methodCodeDto(Supplier<MethodCodeDto> methodCodeDtoSupplier) {
    this.methodCodeDto = changedValue(methodCodeDtoSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>methodCodeDto</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodCodeDto(new MethodCodeDto());
   * }</pre>
   *
   * @param methodCodeDto the method code DTO
   * @return current instance of builder
   */
  public ConstructorDtoBuilder methodCodeDto(MethodCodeDto methodCodeDto) {
    this.methodCodeDto = changedValue(methodCodeDto);
    return this;
  }

  /**
   * Adds a single element to <code>parameters</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2Parameters(MethodParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public ConstructorDtoBuilder add2Parameters(MethodParameterDto element) {
    List<MethodParameterDto> newCollection;
    if (this.parameters.isSet()) {
      newCollection = new ArrayList<>(this.parameters.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.parameters = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>parameters</code>, configured through its builder.
   *
   * @param parameterBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public ConstructorDtoBuilder add2Parameters(
      Consumer<MethodParameterDtoBuilder> parameterBuilderConsumer) {
    MethodParameterDtoBuilder elementBuilder = MethodParameterDtoBuilder.create();
    parameterBuilderConsumer.accept(elementBuilder);
    return add2Parameters(elementBuilder.build());
  }

  /**
   * Appends elements to <code>parameters</code>, each configured through its builder.
   *
   * @param parameterBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final ConstructorDtoBuilder add2Parameters(
      Consumer<MethodParameterDtoBuilder>... parameterBuilderConsumers) {
    for (Consumer<MethodParameterDtoBuilder> consumer : parameterBuilderConsumers) {
      add2Parameters(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>parameters</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ConstructorDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(() -> List.of(MethodParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parametersSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public ConstructorDtoBuilder parameters(Supplier<List<MethodParameterDto>> parametersSupplier) {
    this.parameters = changedValue(parametersSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>parameters</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(List.of(MethodParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parameters the replacement collection (not null)
   * @return current instance of builder
   */
  public ConstructorDtoBuilder parameters(List<MethodParameterDto> parameters) {
    this.parameters = changedValue(parameters);
    return this;
  }

  /**
   * Sets the value for <code>visibility</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ConstructorDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibilitySupplier supplier for visibility
   * @return current instance of builder
   */
  public ConstructorDtoBuilder visibility(Supplier<AccessModifier> visibilitySupplier) {
    this.visibility = changedValue(visibilitySupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>visibility</code>.
   *
   * <p>Generated from setter {@link ConstructorDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibility visibility
   * @return current instance of builder
   */
  public ConstructorDtoBuilder visibility(AccessModifier visibility) {
    this.visibility = changedValue(visibility);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public ConstructorDtoBuilder conditional(
      BooleanSupplier condition, Consumer<ConstructorDtoBuilder> yesCondition) {
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
  public ConstructorDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<ConstructorDtoBuilder> trueCase,
      Consumer<ConstructorDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a parameter with the given type and name.
   *
   * @param type the parameter type
   * @param name the parameter name
   * @return current instance of builder
   */
  public ConstructorDtoBuilder add2Parameters(Class<?> type, String name) {
    return add2Parameters(
        MethodParameterDtoBuilder.create().parameterTypeName(type).parameterName(name).build());
  }

  /**
   * Adds a parameter with the given type and name.
   *
   * @param type the parameter type
   * @param name the parameter name
   * @return current instance of builder
   */
  public ConstructorDtoBuilder add2Parameters(TypeName type, String name) {
    return add2Parameters(
        MethodParameterDtoBuilder.create().parameterTypeName(type).parameterName(name).build());
  }

  /**
   * Sets the constructor body code (shortcut for configuring the method code DTO).
   *
   * @param code codeformat with placeholders
   * @return current instance of builder
   */
  public ConstructorDtoBuilder code(String code) {
    return methodCodeDto(MethodCodeDtoBuilder.create().codeFormat(code).build());
  }

  /**
   * Sets the constructor body code using a format string and arguments (shortcut for configuring
   * the method code DTO).
   *
   * @param format codeformat with placeholders
   * @param args arguments referenced by format specifiers
   * @return current instance of builder
   */
  public ConstructorDtoBuilder code(String format, Object... args) {
    return code(String.format(format, args));
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * ConstructorDto result = builder.build();
   * }</pre>
   */
  @Override
  public ConstructorDto build() {
    ConstructorDto result = new ConstructorDto();
    this.annotations.ifSet(result::setAnnotations);
    this.javadoc.ifSet(result::setJavadoc);
    this.methodCodeDto.ifSet(result::setMethodCodeDto);
    this.parameters.ifSet(result::setParameters);
    this.visibility.ifSet(result::setVisibility);
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
        .append("javadoc", this.javadoc)
        .append("methodCodeDto", this.methodCodeDto)
        .append("parameters", this.parameters)
        .append("visibility", this.visibility)
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
    default ConstructorDto with(Consumer<ConstructorDtoBuilder> b) {
      ConstructorDtoBuilder builder;
      try {
        builder = new ConstructorDtoBuilder(ConstructorDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'ConstructorDtoBuilder.With' should only be implemented by classes, which could be casted to 'ConstructorDto'",
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
    default ConstructorDtoBuilder with() {
      try {
        return new ConstructorDtoBuilder(ConstructorDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'ConstructorDtoBuilder.With' should only be implemented by classes, which could be casted to 'ConstructorDto'",
            ex);
      }
    }
  }
}
