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

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.processing.Generated;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.codegen.model.method.MethodDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.method.MethodDto with method chaining and validation. Use the
 * static {@code create()} method to obtain a new builder instance, configure the desired properties
 * using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * MethodDto result = MethodDtoBuilder.create()
 *     .methodName("example methodName")
 *     .methodName("Hello %s", "World")
 *     .methodName(() -> "example methodName")
 *     .methodName(sb -> sb.append("text"))
 *     .methodNameUpdate(String::trim)
 *     .annotations(List.of(AnnotationDtoBuilder.create().build()))
 *     .annotations(() -> List.of(AnnotationDtoBuilder.create().build()))
 *     .annotations(t -> t.add(annotationDtoBuilder -> annotationDtoBuilder))
 *     .annotations(AnnotationDtoBuilder.create().build(), AnnotationDtoBuilder.create().build())
 *     .annotationsUpdate(list -> list.stream().sorted().toList())
 *     .add2Annotations(AnnotationDtoBuilder.create().build())
 *     .code("example code")
 *     .code("Hello %s", "World")
 *     .code(() -> "example code")
 *     .code(sb -> sb.append("text"))
 *     .codeUpdate(String::trim)
 *     .genericParameters(List.of(GenericParameterDtoBuilder.create().build()))
 *     .genericParameters(() -> List.of(GenericParameterDtoBuilder.create().build()))
 *     .genericParameters(t -> t.add(genericParameterDtoBuilder -> genericParameterDtoBuilder))
 *     .genericParameters(GenericParameterDtoBuilder.create().build(), GenericParameterDtoBuilder.create().build())
 *     .genericParametersUpdate(list -> list.stream().sorted().toList())
 *     .add2GenericParameters(GenericParameterDtoBuilder.create().build())
 *     .javadoc(new JavadocDto())
 *     .javadoc(JavadocDto::new)
 *     .javadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .methodCodeDto(new MethodCodeDto())
 *     .methodCodeDto(MethodCodeDto::new)
 *     .methodCodeDto(methodCodeDtoBuilder -> methodCodeDtoBuilder)
 *     .parameters(List.of(MethodParameterDtoBuilder.create().build()))
 *     .parameters(() -> List.of(MethodParameterDtoBuilder.create().build()))
 *     .parameters(t -> t.add(methodParameterDtoBuilder -> methodParameterDtoBuilder))
 *     .parameters(MethodParameterDtoBuilder.create().build(), MethodParameterDtoBuilder.create().build())
 *     .parametersUpdate(list -> list.stream().sorted().toList())
 *     .add2Parameters(MethodParameterDtoBuilder.create().build())
 *     .isStatic(true)
 *     .isStatic(() -> true)
 *     .isStaticUpdate(value -> !value)
 *     .build();
 * }</pre>
 */
@Generated("Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
@BuilderImplementation(forClass = MethodDto.class)
public class MethodDtoBuilder implements IBuilderBase<MethodDto> {

  /** Tracked value for <code>methodName</code>: methodName. */
  private TrackedValue<String> methodName = unsetValue();

  /** Tracked value for <code>returnType</code>: returnType. */
  private TrackedValue<TypeName> returnType = unsetValue();

  /** Tracked value for <code>annotations</code>: the replacement collection (not null). */
  private TrackedValue<List<AnnotationDto>> annotations = unsetValue();

  /** Tracked value for <code>code</code>: Codeformat with placeholders. */
  private TrackedValue<String> code = unsetValue();

  /** Tracked value for <code>genericParameters</code>: the replacement collection (not null). */
  private TrackedValue<List<GenericParameterDto>> genericParameters = unsetValue();

  /** Tracked value for <code>javadoc</code>: the Javadoc comment. */
  private TrackedValue<JavadocDto> javadoc = unsetValue();

  /** Tracked value for <code>methodCodeDto</code>: the structured method body (not null). */
  private TrackedValue<MethodCodeDto> methodCodeDto = unsetValue();

  /** Tracked value for <code>modifier</code>: access modifier of type {@code AccessModifier}. */
  private TrackedValue<AccessModifier> modifier = unsetValue();

  /** Tracked value for <code>parameters</code>: the replacement collection (not null). */
  private TrackedValue<List<MethodParameterDto>> parameters = unsetValue();

  /**
   * Tracked value for <code>isStatic</code>: true if the method should be static, false otherwise.
   */
  private TrackedValue<Boolean> isStatic = unsetValue();

  /** Empty constructor of builder for {@code org.javahelpers.codegen.model.method.MethodDto}. */
  public MethodDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.method.MethodDto} by a
   * instance.
   *
   * @param instance object instance for initialisiation
   */
  public MethodDtoBuilder(MethodDto instance) {
    this.methodName = initialValue(instance.getMethodName());
    this.returnType = initialValue(instance.getReturnType());
    this.annotations = initialValue(instance.getAnnotations());
    this.genericParameters = initialValue(instance.getGenericParameters());
    this.javadoc = initialValue(instance.getJavadoc());
    this.methodCodeDto = initialValue(instance.getMethodCodeDto());
    this.parameters = initialValue(instance.getParameters());
    this.isStatic = initialValue(instance.isStatic());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.method.MethodDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * MethodDtoBuilder builder = MethodDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.method.MethodDto}
   */
  public static MethodDtoBuilder create() {
    return new MethodDtoBuilder();
  }

  /**
   * Adds a single element to <code>annotations</code>.
   *
   * <p>Generated from setter {@link MethodDto#setAnnotations(List)
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
  public MethodDtoBuilder add2Annotations(AnnotationDto element) {
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
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link MethodDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(AnnotationDtoBuilder.create().build(), AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param annotations the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder annotations(AnnotationDto... annotations) {
    this.annotations = changedValue(List.of(annotations));
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link MethodDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(t -> t.add(annotationDtoBuilder -> annotationDtoBuilder));
   * }</pre>
   *
   * @param annotationsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder annotations(
      Consumer<ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder>>
          annotationsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder> builder =
        this.annotations.isSet()
            ? new ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder>(
                this.annotations.value(), AnnotationDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder>(
                AnnotationDtoBuilder::create);
    annotationsBuilderConsumer.accept(builder);
    this.annotations = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodDto#setAnnotations(List)
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
  public MethodDtoBuilder annotations(Supplier<List<AnnotationDto>> annotationsSupplier) {
    this.annotations = changedValue(annotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link MethodDto#setAnnotations(List)
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
  public MethodDtoBuilder annotations(List<AnnotationDto> annotations) {
    this.annotations = changedValue(annotations);
    return this;
  }

  /**
   * Updates the current value of <code>annotations</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #annotations(Consumer)}.
   *
   * <p>Generated from setter {@link MethodDto#setAnnotations(List)
   * setAnnotations(List<AnnotationDto> annotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.annotations(List.of(AnnotationDtoBuilder.create().build()))
   *     .annotationsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param annotationsUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>annotations</code> has not been set yet
   */
  public MethodDtoBuilder annotationsUpdate(UnaryOperator<List<AnnotationDto>> annotationsUpdater) {
    if (!this.annotations.isSet()) {
      throw new IllegalStateException("Cannot update 'annotations' before it is set");
    }
    this.annotations = changedValue(annotationsUpdater.apply(this.annotations.value()));
    return this;
  }

  /**
   * Sets the value for <code>code</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link MethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code(sb -> sb.append("text"));
   * }</pre>
   *
   * @param codeStringBuilderConsumer consumer providing an instance of Codeformat with placeholders
   * @return current instance of builder
   */
  public MethodDtoBuilder code(Consumer<StringBuilder> codeStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    codeStringBuilderConsumer.accept(builder);
    this.code = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>code</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code(() -> "example code");
   * }</pre>
   *
   * @param codeSupplier supplier for Codeformat with placeholders
   * @return current instance of builder
   */
  public MethodDtoBuilder code(Supplier<String> codeSupplier) {
    this.code = changedValue(codeSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>code</code> by using String.format(format, args). See {@link
   * String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link MethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public MethodDtoBuilder code(String format, Object... args) {
    this.code = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>code</code>.
   *
   * <p>Generated from setter {@link MethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code("example code");
   * }</pre>
   *
   * @param code Codeformat with placeholders
   * @return current instance of builder
   */
  public MethodDtoBuilder code(String code) {
    this.code = changedValue(code);
    return this;
  }

  /**
   * Updates the current value of <code>code</code> in place by applying the given operator, instead
   * of reading it out, changing it and setting it again. Useful for adjustments relative to the
   * current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination with
   * the <code>With</code> copy-and-modify flow. The value must have been set before (directly or
   * via an existing instance).
   *
   * <p>Generated from setter {@link MethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code("example code").codeUpdate(String::trim);
   * }</pre>
   *
   * @param codeUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>code</code> has not been set yet
   */
  public MethodDtoBuilder codeUpdate(UnaryOperator<String> codeUpdater) {
    if (!this.code.isSet()) {
      throw new IllegalStateException("Cannot update 'code' before it is set");
    }
    this.code = changedValue(codeUpdater.apply(this.code.value()));
    return this;
  }

  /**
   * Adds a single element to <code>genericParameters</code>.
   *
   * <p>Generated from setter {@link MethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2GenericParameters(GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public MethodDtoBuilder add2GenericParameters(GenericParameterDto element) {
    List<GenericParameterDto> newCollection;
    if (this.genericParameters.isSet()) {
      newCollection = new ArrayList<>(this.genericParameters.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.genericParameters = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>genericParameters</code>.
   *
   * <p>Generated from setter {@link MethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(GenericParameterDtoBuilder.create().build(), GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param genericParameters the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder genericParameters(GenericParameterDto... genericParameters) {
    this.genericParameters = changedValue(List.of(genericParameters));
    return this;
  }

  /**
   * Sets the value for <code>genericParameters</code> using a builder consumer that produces the
   * value.
   *
   * <p>Generated from setter {@link MethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(t -> t.add(genericParameterDtoBuilder -> genericParameterDtoBuilder));
   * }</pre>
   *
   * @param genericParametersBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder genericParameters(
      Consumer<ArrayListBuilderWithElementBuilders<GenericParameterDto, GenericParameterDtoBuilder>>
          genericParametersBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<GenericParameterDto, GenericParameterDtoBuilder> builder =
        this.genericParameters.isSet()
            ? new ArrayListBuilderWithElementBuilders<
                GenericParameterDto, GenericParameterDtoBuilder>(
                this.genericParameters.value(), GenericParameterDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<
                GenericParameterDto, GenericParameterDtoBuilder>(
                GenericParameterDtoBuilder::create);
    genericParametersBuilderConsumer.accept(builder);
    this.genericParameters = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>genericParameters</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(() -> List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param genericParametersSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder genericParameters(
      Supplier<List<GenericParameterDto>> genericParametersSupplier) {
    this.genericParameters = changedValue(genericParametersSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>genericParameters</code>.
   *
   * <p>Generated from setter {@link MethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param genericParameters the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder genericParameters(List<GenericParameterDto> genericParameters) {
    this.genericParameters = changedValue(genericParameters);
    return this;
  }

  /**
   * Updates the current value of <code>genericParameters</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance). For changing multiple elements of a nested DTO,
   * prefer the builder-consumer helper {@link #genericParameters(Consumer)}.
   *
   * <p>Generated from setter {@link MethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(List.of(GenericParameterDtoBuilder.create().build()))
   *     .genericParametersUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param genericParametersUpdater operator applied to the current value; its result becomes the
   *     new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>genericParameters</code> has not been set yet
   */
  public MethodDtoBuilder genericParametersUpdate(
      UnaryOperator<List<GenericParameterDto>> genericParametersUpdater) {
    if (!this.genericParameters.isSet()) {
      throw new IllegalStateException("Cannot update 'genericParameters' before it is set");
    }
    this.genericParameters =
        changedValue(genericParametersUpdater.apply(this.genericParameters.value()));
    return this;
  }

  /**
   * Sets the value for <code>isStatic</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodDto#setStatic(boolean) setStatic(boolean isStatic)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.isStatic(() -> true);
   * }</pre>
   *
   * @param isStaticSupplier supplier for true if the method should be static, false otherwise
   * @return current instance of builder
   */
  public MethodDtoBuilder isStatic(Supplier<Boolean> isStaticSupplier) {
    this.isStatic = changedValue(isStaticSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>isStatic</code>.
   *
   * <p>Generated from setter {@link MethodDto#setStatic(boolean) setStatic(boolean isStatic)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.isStatic(true);
   * }</pre>
   *
   * @param isStatic true if the method should be static, false otherwise
   * @return current instance of builder
   */
  public MethodDtoBuilder isStatic(boolean isStatic) {
    this.isStatic = changedValue(isStatic);
    return this;
  }

  /**
   * Updates the current value of <code>isStatic</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link MethodDto#setStatic(boolean) setStatic(boolean isStatic)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.isStatic(true).isStaticUpdate(value -> !value);
   * }</pre>
   *
   * @param isStaticUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>isStatic</code> has not been set yet
   */
  public MethodDtoBuilder isStaticUpdate(UnaryOperator<Boolean> isStaticUpdater) {
    if (!this.isStatic.isSet()) {
      throw new IllegalStateException("Cannot update 'isStatic' before it is set");
    }
    this.isStatic = changedValue(isStaticUpdater.apply(this.isStatic.value()));
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link MethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javadoc(javadocDtoBuilder -> javadocDtoBuilder);
   * }</pre>
   *
   * @param javadocBuilderConsumer consumer providing an instance of a builder for the Javadoc
   *     comment
   * @return current instance of builder
   */
  public MethodDtoBuilder javadoc(Consumer<JavadocDtoBuilder> javadocBuilderConsumer) {
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
   * <p>Generated from setter {@link MethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javadoc(JavadocDto::new);
   * }</pre>
   *
   * @param javadocSupplier supplier for the Javadoc comment
   * @return current instance of builder
   */
  public MethodDtoBuilder javadoc(Supplier<JavadocDto> javadocSupplier) {
    this.javadoc = changedValue(javadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code>.
   *
   * <p>Generated from setter {@link MethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javadoc(new JavadocDto());
   * }</pre>
   *
   * @param javadoc the Javadoc comment
   * @return current instance of builder
   */
  public MethodDtoBuilder javadoc(JavadocDto javadoc) {
    this.javadoc = changedValue(javadoc);
    return this;
  }

  /**
   * Updates the current value of <code>javadoc</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple values of a nested DTO, prefer the
   * builder-consumer helper {@link #javadoc(Consumer)}.
   *
   * <p>Generated from setter {@link MethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
   * javadoc)}
   *
   * @param javadocUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>javadoc</code> has not been set yet
   */
  public MethodDtoBuilder javadocUpdate(UnaryOperator<JavadocDto> javadocUpdater) {
    if (!this.javadoc.isSet()) {
      throw new IllegalStateException("Cannot update 'javadoc' before it is set");
    }
    this.javadoc = changedValue(javadocUpdater.apply(this.javadoc.value()));
    return this;
  }

  /**
   * Sets the value for <code>methodCodeDto</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link MethodDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodCodeDto(methodCodeDtoBuilder -> methodCodeDtoBuilder);
   * }</pre>
   *
   * @param methodCodeDtoBuilderConsumer consumer providing an instance of a builder for the
   *     structured method body (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder methodCodeDto(
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
   * <p>Generated from setter {@link MethodDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodCodeDto(MethodCodeDto::new);
   * }</pre>
   *
   * @param methodCodeDtoSupplier supplier for the structured method body (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder methodCodeDto(Supplier<MethodCodeDto> methodCodeDtoSupplier) {
    this.methodCodeDto = changedValue(methodCodeDtoSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>methodCodeDto</code>.
   *
   * <p>Generated from setter {@link MethodDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodCodeDto(new MethodCodeDto());
   * }</pre>
   *
   * @param methodCodeDto the structured method body (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder methodCodeDto(MethodCodeDto methodCodeDto) {
    this.methodCodeDto = changedValue(methodCodeDto);
    return this;
  }

  /**
   * Updates the current value of <code>methodCodeDto</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance). For changing multiple values of a nested DTO,
   * prefer the builder-consumer helper {@link #methodCodeDto(Consumer)}.
   *
   * <p>Generated from setter {@link MethodDto#setMethodCodeDto(MethodCodeDto)
   * setMethodCodeDto(MethodCodeDto methodCodeDto)}
   *
   * @param methodCodeDtoUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>methodCodeDto</code> has not been set yet
   */
  public MethodDtoBuilder methodCodeDtoUpdate(UnaryOperator<MethodCodeDto> methodCodeDtoUpdater) {
    if (!this.methodCodeDto.isSet()) {
      throw new IllegalStateException("Cannot update 'methodCodeDto' before it is set");
    }
    this.methodCodeDto = changedValue(methodCodeDtoUpdater.apply(this.methodCodeDto.value()));
    return this;
  }

  /**
   * Sets the value for <code>methodName</code> by executing the provided consumer.
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param methodNameStringBuilderConsumer consumer providing an instance of methodName
   * @return current instance of builder
   */
  public MethodDtoBuilder methodName(Consumer<StringBuilder> methodNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    methodNameStringBuilderConsumer.accept(builder);
    this.methodName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>methodName</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodName(() -> "example methodName");
   * }</pre>
   *
   * @param methodNameSupplier supplier for methodName
   * @return current instance of builder
   */
  public MethodDtoBuilder methodName(Supplier<String> methodNameSupplier) {
    this.methodName = changedValue(methodNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>methodName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public MethodDtoBuilder methodName(String format, Object... args) {
    this.methodName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>methodName</code>.
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodName("example methodName");
   * }</pre>
   *
   * @param methodName methodName
   * @return current instance of builder
   */
  public MethodDtoBuilder methodName(String methodName) {
    this.methodName = changedValue(methodName);
    return this;
  }

  /**
   * Updates the current value of <code>methodName</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methodName("example methodName").methodNameUpdate(String::trim);
   * }</pre>
   *
   * @param methodNameUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>methodName</code> has not been set yet
   */
  public MethodDtoBuilder methodNameUpdate(UnaryOperator<String> methodNameUpdater) {
    if (!this.methodName.isSet()) {
      throw new IllegalStateException("Cannot update 'methodName' before it is set");
    }
    this.methodName = changedValue(methodNameUpdater.apply(this.methodName.value()));
    return this;
  }

  /**
   * Sets the value for <code>modifier</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodDto#setModifier(AccessModifier)
   * setModifier(AccessModifier modifier)}
   *
   * @param modifierSupplier supplier for access modifier of type {@code AccessModifier}
   * @return current instance of builder
   */
  public MethodDtoBuilder modifier(Supplier<AccessModifier> modifierSupplier) {
    this.modifier = changedValue(modifierSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>modifier</code>.
   *
   * <p>Generated from setter {@link MethodDto#setModifier(AccessModifier)
   * setModifier(AccessModifier modifier)}
   *
   * @param modifier access modifier of type {@code AccessModifier}
   * @return current instance of builder
   */
  public MethodDtoBuilder modifier(AccessModifier modifier) {
    this.modifier = changedValue(modifier);
    return this;
  }

  /**
   * Updates the current value of <code>modifier</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link MethodDto#setModifier(AccessModifier)
   * setModifier(AccessModifier modifier)}
   *
   * @param modifierUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>modifier</code> has not been set yet
   */
  public MethodDtoBuilder modifierUpdate(UnaryOperator<AccessModifier> modifierUpdater) {
    if (!this.modifier.isSet()) {
      throw new IllegalStateException("Cannot update 'modifier' before it is set");
    }
    this.modifier = changedValue(modifierUpdater.apply(this.modifier.value()));
    return this;
  }

  /**
   * Adds a single element to <code>parameters</code>.
   *
   * <p>Generated from setter {@link MethodDto#setParameters(List)
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
  public MethodDtoBuilder add2Parameters(MethodParameterDto element) {
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
   * Sets the value for <code>parameters</code>.
   *
   * <p>Generated from setter {@link MethodDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(MethodParameterDtoBuilder.create().build(), MethodParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param parameters the replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder parameters(MethodParameterDto... parameters) {
    this.parameters = changedValue(List.of(parameters));
    return this;
  }

  /**
   * Sets the value for <code>parameters</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link MethodDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(t -> t.add(methodParameterDtoBuilder -> methodParameterDtoBuilder));
   * }</pre>
   *
   * @param parametersBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public MethodDtoBuilder parameters(
      Consumer<ArrayListBuilderWithElementBuilders<MethodParameterDto, MethodParameterDtoBuilder>>
          parametersBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<MethodParameterDto, MethodParameterDtoBuilder> builder =
        this.parameters.isSet()
            ? new ArrayListBuilderWithElementBuilders<
                MethodParameterDto, MethodParameterDtoBuilder>(
                this.parameters.value(), MethodParameterDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<
                MethodParameterDto, MethodParameterDtoBuilder>(MethodParameterDtoBuilder::create);
    parametersBuilderConsumer.accept(builder);
    this.parameters = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>parameters</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link MethodDto#setParameters(List)
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
  public MethodDtoBuilder parameters(Supplier<List<MethodParameterDto>> parametersSupplier) {
    this.parameters = changedValue(parametersSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>parameters</code>.
   *
   * <p>Generated from setter {@link MethodDto#setParameters(List)
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
  public MethodDtoBuilder parameters(List<MethodParameterDto> parameters) {
    this.parameters = changedValue(parameters);
    return this;
  }

  /**
   * Updates the current value of <code>parameters</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #parameters(Consumer)}.
   *
   * <p>Generated from setter {@link MethodDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(List.of(MethodParameterDtoBuilder.create().build()))
   *     .parametersUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param parametersUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>parameters</code> has not been set yet
   */
  public MethodDtoBuilder parametersUpdate(
      UnaryOperator<List<MethodParameterDto>> parametersUpdater) {
    if (!this.parameters.isSet()) {
      throw new IllegalStateException("Cannot update 'parameters' before it is set");
    }
    this.parameters = changedValue(parametersUpdater.apply(this.parameters.value()));
    return this;
  }

  /**
   * Sets the value for <code>returnType</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * @param returnTypeSupplier supplier for returnType
   * @return current instance of builder
   */
  public MethodDtoBuilder returnType(Supplier<TypeName> returnTypeSupplier) {
    this.returnType = changedValue(returnTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>returnType</code>.
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * @param returnType returnType
   * @return current instance of builder
   */
  public MethodDtoBuilder returnType(TypeName returnType) {
    this.returnType = changedValue(returnType);
    return this;
  }

  /**
   * Updates the current value of <code>returnType</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from parameter in constructor {@link MethodDto#MethodDto(String, TypeName)
   * MethodDto(String methodName, TypeName returnType)}
   *
   * @param returnTypeUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>returnType</code> has not been set yet
   */
  public MethodDtoBuilder returnTypeUpdate(UnaryOperator<TypeName> returnTypeUpdater) {
    if (!this.returnType.isSet()) {
      throw new IllegalStateException("Cannot update 'returnType' before it is set");
    }
    this.returnType = changedValue(returnTypeUpdater.apply(this.returnType.value()));
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public MethodDtoBuilder conditional(
      BooleanSupplier condition, Consumer<MethodDtoBuilder> yesCondition) {
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
  public MethodDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<MethodDtoBuilder> trueCase,
      Consumer<MethodDtoBuilder> falseCase) {
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
   * MethodDto result = builder.build();
   * }</pre>
   */
  @Override
  public MethodDto build() {
    if (this.isStatic.isSet() && this.isStatic.value() == null) {
      throw new IllegalStateException(
          "Field 'isStatic' is marked as non-null but null value was provided");
    }
    MethodDto result = new MethodDto(this.methodName.value(), this.returnType.value());
    this.annotations.ifSet(result::setAnnotations);
    this.code.ifSet(result::setCode);
    this.genericParameters.ifSet(result::setGenericParameters);
    this.javadoc.ifSet(result::setJavadoc);
    this.methodCodeDto.ifSet(result::setMethodCodeDto);
    this.modifier.ifSet(result::setModifier);
    this.parameters.ifSet(result::setParameters);
    this.isStatic.ifSet(result::setStatic);
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
        .append("methodName", this.methodName)
        .append("returnType", this.returnType)
        .append("annotations", this.annotations)
        .append("code", this.code)
        .append("genericParameters", this.genericParameters)
        .append("javadoc", this.javadoc)
        .append("methodCodeDto", this.methodCodeDto)
        .append("modifier", this.modifier)
        .append("parameters", this.parameters)
        .append("isStatic", this.isStatic)
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
    default MethodDto with(Consumer<MethodDtoBuilder> b) {
      MethodDtoBuilder builder;
      try {
        builder = new MethodDtoBuilder(MethodDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'MethodDtoBuilder.With' should only be implemented by classes, which could be casted to 'MethodDto'",
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
    default MethodDtoBuilder with() {
      try {
        return new MethodDtoBuilder(MethodDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'MethodDtoBuilder.With' should only be implemented by classes, which could be casted to 'MethodDto'",
            ex);
      }
    }
  }
}
