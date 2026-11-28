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
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;

/**
 * Builder for {@code org.javahelpers.codegen.model.type.NestedTypeDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.type.NestedTypeDto with method chaining and validation. Use the
 * static {@code create()} method to obtain a new builder instance, configure the desired properties
 * using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * NestedTypeDto result = NestedTypeDtoBuilder.create()
 *     .annotations(List.of(AnnotationDtoBuilder.create().build()))
 *     .annotations(() -> List.of(AnnotationDtoBuilder.create().build()))
 *     .add2Annotations(element -> {})
 *     .add2Annotations(first -> {}, second -> {})
 *     .add2Annotations(AnnotationDtoBuilder.create().build())
 *     .javadoc(new JavadocDto())
 *     .javadoc(JavadocDto::new)
 *     .javadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .methods(List.of(MethodDtoBuilder.create().build()))
 *     .methods(() -> List.of(MethodDtoBuilder.create().build()))
 *     .add2Methods(element -> {})
 *     .add2Methods(first -> {}, second -> {})
 *     .add2Methods(MethodDtoBuilder.create().build())
 *     .typeName("example typeName")
 *     .typeName("Hello %s", "World")
 *     .typeName(() -> "example typeName")
 *     .typeName(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class NestedTypeDtoBuilder implements IBuilderBase<NestedTypeDto> {

  /** Tracked value for <code>annotations</code>: the replacement collection (not null). */
  private TrackedValue<List<AnnotationDto>> annotations = unsetValue();

  /** Tracked value for <code>javadoc</code>: javadoc. */
  private TrackedValue<JavadocDto> javadoc = unsetValue();

  /** Tracked value for <code>kind</code>: kind. */
  private TrackedValue<org.javahelpers.codegen.model.type.NestedTypeDto.NestedTypeKind> kind =
      unsetValue();

  /** Tracked value for <code>methods</code>: the replacement collection (not null). */
  private TrackedValue<List<MethodDto>> methods = unsetValue();

  /** Tracked value for <code>typeName</code>: typeName. */
  private TrackedValue<String> typeName = unsetValue();

  /** Tracked value for <code>visibility</code>: visibility. */
  private TrackedValue<AccessModifier> visibility = unsetValue();

  /** Empty constructor of builder for {@code org.javahelpers.codegen.model.type.NestedTypeDto}. */
  public NestedTypeDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.type.NestedTypeDto} by a
   * instance.
   *
   * @param instance object instance for initialisiation
   */
  public NestedTypeDtoBuilder(NestedTypeDto instance) {
    this.annotations = initialValue(instance.getAnnotations());
    this.javadoc = initialValue(instance.getJavadoc());
    this.kind = initialValue(instance.getKind());
    this.methods = initialValue(instance.getMethods());
    this.typeName = initialValue(instance.getTypeName());
    this.visibility = initialValue(instance.getVisibility());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.type.NestedTypeDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * NestedTypeDtoBuilder builder = NestedTypeDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.type.NestedTypeDto}
   */
  public static NestedTypeDtoBuilder create() {
    return new NestedTypeDtoBuilder();
  }

  /**
   * Adds a single element to <code>annotations</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setAnnotations(List)
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
  public NestedTypeDtoBuilder add2Annotations(AnnotationDto element) {
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
  public NestedTypeDtoBuilder add2Annotations(Class<?> annotationType) {
    return add2Annotations(AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>annotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder add2Annotations(String packageName, String className) {
    return add2Annotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>annotations</code>, configured through its builder.
   *
   * @param annotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder add2Annotations(
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
  public final NestedTypeDtoBuilder add2Annotations(
      Consumer<AnnotationDtoBuilder>... annotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : annotationBuilderConsumers) {
      add2Annotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link NestedTypeDto#setAnnotations(List)
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
  public NestedTypeDtoBuilder annotations(Supplier<List<AnnotationDto>> annotationsSupplier) {
    this.annotations = changedValue(annotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setAnnotations(List)
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
  public NestedTypeDtoBuilder annotations(List<AnnotationDto> annotations) {
    this.annotations = changedValue(annotations);
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link NestedTypeDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public NestedTypeDtoBuilder javadoc(Consumer<JavadocDtoBuilder> javadocBuilderConsumer) {
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
   * <p>Generated from setter {@link NestedTypeDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public NestedTypeDtoBuilder javadoc(Supplier<JavadocDto> javadocSupplier) {
    this.javadoc = changedValue(javadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public NestedTypeDtoBuilder javadoc(JavadocDto javadoc) {
    this.javadoc = changedValue(javadoc);
    return this;
  }

  /**
   * Sets the value for <code>kind</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link NestedTypeDto#setKind(NestedTypeDto.NestedTypeKind)
   * setKind(NestedTypeDto.NestedTypeKind kind)}
   *
   * @param kindSupplier supplier for kind
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder kind(
      Supplier<org.javahelpers.codegen.model.type.NestedTypeDto.NestedTypeKind> kindSupplier) {
    this.kind = changedValue(kindSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>kind</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setKind(NestedTypeDto.NestedTypeKind)
   * setKind(NestedTypeDto.NestedTypeKind kind)}
   *
   * @param kind kind
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder kind(
      org.javahelpers.codegen.model.type.NestedTypeDto.NestedTypeKind kind) {
    this.kind = changedValue(kind);
    return this;
  }

  /**
   * Adds a single element to <code>methods</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setMethods(List) setMethods(List<MethodDto>
   * methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2Methods(MethodDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder add2Methods(MethodDto element) {
    List<MethodDto> newCollection;
    if (this.methods.isSet()) {
      newCollection = new ArrayList<>(this.methods.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.methods = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>methods</code>, configured through its builder.
   *
   * @param methodBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder add2Methods(Consumer<MethodDtoBuilder> methodBuilderConsumer) {
    MethodDtoBuilder elementBuilder = MethodDtoBuilder.create();
    methodBuilderConsumer.accept(elementBuilder);
    return add2Methods(elementBuilder.build());
  }

  /**
   * Appends elements to <code>methods</code>, each configured through its builder.
   *
   * @param methodBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final NestedTypeDtoBuilder add2Methods(
      Consumer<MethodDtoBuilder>... methodBuilderConsumers) {
    for (Consumer<MethodDtoBuilder> consumer : methodBuilderConsumers) {
      add2Methods(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>methods</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link NestedTypeDto#setMethods(List) setMethods(List<MethodDto>
   * methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(() -> List.of(MethodDtoBuilder.create().build()));
   * }</pre>
   *
   * @param methodsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder methods(Supplier<List<MethodDto>> methodsSupplier) {
    this.methods = changedValue(methodsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>methods</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setMethods(List) setMethods(List<MethodDto>
   * methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(List.of(MethodDtoBuilder.create().build()));
   * }</pre>
   *
   * @param methods the replacement collection (not null)
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder methods(List<MethodDto> methods) {
    this.methods = changedValue(methods);
    return this;
  }

  /**
   * Sets the value for <code>typeName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link NestedTypeDto#setTypeName(String) setTypeName(String typeName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.typeName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param typeNameStringBuilderConsumer consumer providing an instance of typeName
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder typeName(Consumer<StringBuilder> typeNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    typeNameStringBuilderConsumer.accept(builder);
    this.typeName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>typeName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link NestedTypeDto#setTypeName(String) setTypeName(String typeName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.typeName(() -> "example typeName");
   * }</pre>
   *
   * @param typeNameSupplier supplier for typeName
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder typeName(Supplier<String> typeNameSupplier) {
    this.typeName = changedValue(typeNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>typeName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link NestedTypeDto#setTypeName(String) setTypeName(String typeName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.typeName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder typeName(String format, Object... args) {
    this.typeName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>typeName</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setTypeName(String) setTypeName(String typeName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.typeName("example typeName");
   * }</pre>
   *
   * @param typeName typeName
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder typeName(String typeName) {
    this.typeName = changedValue(typeName);
    return this;
  }

  /**
   * Sets the value for <code>visibility</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link NestedTypeDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibilitySupplier supplier for visibility
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder visibility(Supplier<AccessModifier> visibilitySupplier) {
    this.visibility = changedValue(visibilitySupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>visibility</code>.
   *
   * <p>Generated from setter {@link NestedTypeDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibility visibility
   * @return current instance of builder
   */
  public NestedTypeDtoBuilder visibility(AccessModifier visibility) {
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
  public NestedTypeDtoBuilder conditional(
      BooleanSupplier condition, Consumer<NestedTypeDtoBuilder> yesCondition) {
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
  public NestedTypeDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<NestedTypeDtoBuilder> trueCase,
      Consumer<NestedTypeDtoBuilder> falseCase) {
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
   * NestedTypeDto result = builder.build();
   * }</pre>
   */
  @Override
  public NestedTypeDto build() {
    NestedTypeDto result = new NestedTypeDto();
    this.annotations.ifSet(result::setAnnotations);
    this.javadoc.ifSet(result::setJavadoc);
    this.kind.ifSet(result::setKind);
    this.methods.ifSet(result::setMethods);
    this.typeName.ifSet(result::setTypeName);
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
        .append("kind", this.kind)
        .append("methods", this.methods)
        .append("typeName", this.typeName)
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
    default NestedTypeDto with(Consumer<NestedTypeDtoBuilder> b) {
      NestedTypeDtoBuilder builder;
      try {
        builder = new NestedTypeDtoBuilder(NestedTypeDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'NestedTypeDtoBuilder.With' should only be implemented by classes, which could be casted to 'NestedTypeDto'",
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
    default NestedTypeDtoBuilder with() {
      try {
        return new NestedTypeDtoBuilder(NestedTypeDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'NestedTypeDtoBuilder.With' should only be implemented by classes, which could be casted to 'NestedTypeDto'",
            ex);
      }
    }
  }
}
