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

package org.javahelpers.simple.builders.processor.model.type;

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDtoBuilder;

/**
 * Builder for {@code org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto with method chaining
 * and validation. Use the static {@code create()} method to obtain a new builder instance,
 * configure the desired properties using the setter methods, and then call {@code build()} to
 * create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * BuilderNestedTypeDto result = BuilderNestedTypeDtoBuilder.create()
 *     .javadoc(new JavadocDto())
 *     .javadoc(JavadocDto::new)
 *     .javadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .typeName("example typeName")
 *     .typeName("Hello %s", "World")
 *     .typeName(() -> "example typeName")
 *     .typeName(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class BuilderNestedTypeDtoBuilder implements IBuilderBase<BuilderNestedTypeDto> {

  /** Tracked value for <code>javadoc</code>: javadoc. */
  private TrackedValue<JavadocDto> javadoc = unsetValue();

  /** Tracked value for <code>kind</code>: kind. */
  private TrackedValue<NestedTypeDto.NestedTypeKind> kind = unsetValue();

  /** Tracked value for <code>typeName</code>: typeName. */
  private TrackedValue<String> typeName = unsetValue();

  /** Tracked value for <code>visibility</code>: visibility. */
  private TrackedValue<AccessModifier> visibility = unsetValue();

  private TrackedValue<List<BuilderMethodDto>> methods = unsetValue();
  private TrackedValue<List<AnnotationDto>> annotations = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto}.
   */
  public BuilderNestedTypeDtoBuilder() {}

  /**
   * Initialisation of builder for {@code
   * org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto} by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public BuilderNestedTypeDtoBuilder(BuilderNestedTypeDto instance) {
    this.javadoc = initialValue(instance.getJavadoc());
    this.kind = initialValue(instance.getKind());
    this.typeName = initialValue(instance.getTypeName());
    this.visibility = initialValue(instance.getVisibility());
  }

  /**
   * Creating a new builder for {@code
   * org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderNestedTypeDtoBuilder builder = BuilderNestedTypeDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code
   *     org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto}
   */
  public static BuilderNestedTypeDtoBuilder create() {
    return new BuilderNestedTypeDtoBuilder();
  }

  /**
   * Sets the value for <code>javadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setJavadoc(JavadocDto)
   * setJavadoc(JavadocDto javadoc)}
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
  public BuilderNestedTypeDtoBuilder javadoc(Consumer<JavadocDtoBuilder> javadocBuilderConsumer) {
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
   * <p>Generated from setter {@link BuilderNestedTypeDto#setJavadoc(JavadocDto)
   * setJavadoc(JavadocDto javadoc)}
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
  public BuilderNestedTypeDtoBuilder javadoc(Supplier<JavadocDto> javadocSupplier) {
    this.javadoc = changedValue(javadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setJavadoc(JavadocDto)
   * setJavadoc(JavadocDto javadoc)}
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
  public BuilderNestedTypeDtoBuilder javadoc(JavadocDto javadoc) {
    this.javadoc = changedValue(javadoc);
    return this;
  }

  /**
   * Sets the value for <code>kind</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setKind(NestedTypeDto.NestedTypeKind)
   * setKind(NestedTypeDto.NestedTypeKind kind)}
   *
   * @param kindSupplier supplier for kind
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder kind(Supplier<NestedTypeDto.NestedTypeKind> kindSupplier) {
    this.kind = changedValue(kindSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>kind</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setKind(NestedTypeDto.NestedTypeKind)
   * setKind(NestedTypeDto.NestedTypeKind kind)}
   *
   * @param kind kind
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder kind(NestedTypeDto.NestedTypeKind kind) {
    this.kind = changedValue(kind);
    return this;
  }

  /**
   * Sets the value for <code>typeName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setTypeName(String) setTypeName(String
   * typeName)}
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
  public BuilderNestedTypeDtoBuilder typeName(
      Consumer<StringBuilder> typeNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    typeNameStringBuilderConsumer.accept(builder);
    this.typeName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>typeName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setTypeName(String) setTypeName(String
   * typeName)}
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
  public BuilderNestedTypeDtoBuilder typeName(Supplier<String> typeNameSupplier) {
    this.typeName = changedValue(typeNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>typeName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setTypeName(String) setTypeName(String
   * typeName)}
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
  public BuilderNestedTypeDtoBuilder typeName(String format, Object... args) {
    this.typeName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>typeName</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setTypeName(String) setTypeName(String
   * typeName)}
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
  public BuilderNestedTypeDtoBuilder typeName(String typeName) {
    this.typeName = changedValue(typeName);
    return this;
  }

  /**
   * Sets the value for <code>visibility</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibilitySupplier supplier for visibility
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder visibility(Supplier<AccessModifier> visibilitySupplier) {
    this.visibility = changedValue(visibilitySupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>visibility</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibility visibility
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder visibility(AccessModifier visibility) {
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
  public BuilderNestedTypeDtoBuilder conditional(
      BooleanSupplier condition, Consumer<BuilderNestedTypeDtoBuilder> yesCondition) {
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
  public BuilderNestedTypeDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<BuilderNestedTypeDtoBuilder> trueCase,
      Consumer<BuilderNestedTypeDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a single element to <code>methods</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setMethods(List)
   * setMethods(List<BuilderMethodDto> methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2Methods(BuilderMethodDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder add2Methods(BuilderMethodDto element) {
    List<BuilderMethodDto> newCollection;
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
  public BuilderNestedTypeDtoBuilder add2Methods(
      Consumer<BuilderMethodDtoBuilder> methodBuilderConsumer) {
    BuilderMethodDtoBuilder elementBuilder = BuilderMethodDtoBuilder.create();
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
  public final BuilderNestedTypeDtoBuilder add2Methods(
      Consumer<BuilderMethodDtoBuilder>... methodBuilderConsumers) {
    for (Consumer<BuilderMethodDtoBuilder> consumer : methodBuilderConsumers) {
      add2Methods(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>methods</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setMethods(List)
   * setMethods(List<BuilderMethodDto> methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(() -> List.of(BuilderMethodDtoBuilder.create().build()));
   * }</pre>
   *
   * @param methodsSupplier supplier for methods
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder methods(Supplier<List<BuilderMethodDto>> methodsSupplier) {
    this.methods = changedValue(methodsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>methods</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setMethods(List)
   * setMethods(List<BuilderMethodDto> methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(List.of(BuilderMethodDtoBuilder.create().build()));
   * }</pre>
   *
   * @param methods methods
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder methods(List<BuilderMethodDto> methods) {
    this.methods = changedValue(methods);
    return this;
  }

  /**
   * Adds a single element to <code>annotations</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setAnnotations(List)
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
  public BuilderNestedTypeDtoBuilder add2Annotations(AnnotationDto element) {
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
  public BuilderNestedTypeDtoBuilder add2Annotations(Class<?> annotationType) {
    return add2Annotations(AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>annotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder add2Annotations(String packageName, String className) {
    return add2Annotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>annotations</code>, configured through its builder.
   *
   * @param annotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder add2Annotations(
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
  public final BuilderNestedTypeDtoBuilder add2Annotations(
      Consumer<AnnotationDtoBuilder>... annotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : annotationBuilderConsumers) {
      add2Annotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(() -> List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param annotationsSupplier supplier for annotations
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder annotations(
      Supplier<List<AnnotationDto>> annotationsSupplier) {
    this.annotations = changedValue(annotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link BuilderNestedTypeDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param annotations annotations
   * @return current instance of builder
   */
  public BuilderNestedTypeDtoBuilder annotations(List<AnnotationDto> annotations) {
    this.annotations = changedValue(annotations);
    return this;
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderNestedTypeDto result = builder.build();
   * }</pre>
   */
  @Override
  public BuilderNestedTypeDto build() {
    BuilderNestedTypeDto result = new BuilderNestedTypeDto();
    this.javadoc.ifSet(result::setJavadoc);
    this.kind.ifSet(result::setKind);
    this.typeName.ifSet(result::setTypeName);
    this.visibility.ifSet(result::setVisibility);
    this.methods.ifSet(result::setMethods);
    this.annotations.ifSet(result::setAnnotations);
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
        .append("javadoc", this.javadoc)
        .append("kind", this.kind)
        .append("typeName", this.typeName)
        .append("visibility", this.visibility)
        .append("methods", this.methods)
        .append("annotations", this.annotations)
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
    default BuilderNestedTypeDto with(Consumer<BuilderNestedTypeDtoBuilder> b) {
      BuilderNestedTypeDtoBuilder builder;
      try {
        builder = new BuilderNestedTypeDtoBuilder(BuilderNestedTypeDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderNestedTypeDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderNestedTypeDto'",
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
    default BuilderNestedTypeDtoBuilder with() {
      try {
        return new BuilderNestedTypeDtoBuilder(BuilderNestedTypeDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderNestedTypeDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderNestedTypeDto'",
            ex);
      }
    }
  }
}
