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

package org.javahelpers.simple.builders.processor.model.method;

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.method.MethodParameterDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto with method chaining and
 * validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the
 * final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * BuilderMethodDto result = BuilderMethodDtoBuilder.create()
 *     .methodName("example methodName")
 *     .methodName("Hello %s", "World")
 *     .methodName(() -> "example methodName")
 *     .methodName(sb -> sb.append("text"))
 *     .code("example code")
 *     .code("Hello %s", "World")
 *     .code(() -> "example code")
 *     .code(sb -> sb.append("text"))
 *     .constructorField(true)
 *     .constructorField(() -> true)
 *     .exampleChainFragment("example exampleChainFragment")
 *     .exampleChainFragment("Hello %s", "World")
 *     .exampleChainFragment(() -> "example exampleChainFragment")
 *     .exampleChainFragment(sb -> sb.append("text"))
 *     .javadoc(new JavadocDto())
 *     .javadoc(JavadocDto::new)
 *     .javadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .ordering(42)
 *     .ordering(() -> 42)
 *     .priority(42)
 *     .priority(() -> 42)
 *     .sourceDescription("example sourceDescription")
 *     .sourceDescription("Hello %s", "World")
 *     .sourceDescription(() -> "example sourceDescription")
 *     .sourceDescription(sb -> sb.append("text"))
 *     .sourceFieldName("example sourceFieldName")
 *     .sourceFieldName("Hello %s", "World")
 *     .sourceFieldName(() -> "example sourceFieldName")
 *     .sourceFieldName(sb -> sb.append("text"))
 *     .isStatic(true)
 *     .isStatic(() -> true)
 *     .build();
 * }</pre>
 */
public class BuilderMethodDtoBuilder implements IBuilderBase<BuilderMethodDto> {

  /** Tracked value for <code>methodName</code>: methodName. */
  private TrackedValue<String> methodName = unsetValue();

  /** Tracked value for <code>returnType</code>: returnType. */
  private TrackedValue<TypeName> returnType = unsetValue();

  /** Tracked value for <code>code</code>: code. */
  private TrackedValue<String> code = unsetValue();

  /** Tracked value for <code>constructorField</code>: constructorField. */
  private TrackedValue<Boolean> constructorField = unsetValue();

  /** Tracked value for <code>exampleChainFragment</code>: exampleChainFragment. */
  private TrackedValue<String> exampleChainFragment = unsetValue();

  /** Tracked value for <code>javadoc</code>: javadoc. */
  private TrackedValue<JavadocDto> javadoc = unsetValue();

  /** Tracked value for <code>modifier</code>: modifier. */
  private TrackedValue<AccessModifier> modifier = unsetValue();

  /** Tracked value for <code>ordering</code>: ordering. */
  private TrackedValue<Integer> ordering = unsetValue();

  /** Tracked value for <code>priority</code>: priority. */
  private TrackedValue<Integer> priority = unsetValue();

  /** Tracked value for <code>sourceDescription</code>: sourceDescription. */
  private TrackedValue<String> sourceDescription = unsetValue();

  /** Tracked value for <code>sourceFieldName</code>: sourceFieldName. */
  private TrackedValue<String> sourceFieldName = unsetValue();

  /** Tracked value for <code>isStatic</code>: isStatic. */
  private TrackedValue<Boolean> isStatic = unsetValue();

  private TrackedValue<List<MethodParameterDto>> parameters = unsetValue();
  private TrackedValue<List<GenericParameterDto>> genericParameters = unsetValue();
  private TrackedValue<List<AnnotationDto>> annotations = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto}.
   */
  public BuilderMethodDtoBuilder() {}

  /**
   * Initialisation of builder for {@code
   * org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto} by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public BuilderMethodDtoBuilder(BuilderMethodDto instance) {
    this.methodName = initialValue(instance.getMethodName());
    this.returnType = initialValue(instance.getReturnType());
    this.constructorField = initialValue(instance.isConstructorField());
    this.exampleChainFragment = initialValue(instance.getExampleChainFragment());
    this.javadoc = initialValue(instance.getJavadoc());
    this.ordering = initialValue(instance.getOrdering());
    this.priority = initialValue(instance.getPriority());
    this.sourceDescription = initialValue(instance.getSourceDescription());
    this.sourceFieldName = initialValue(instance.getSourceFieldName());
    this.isStatic = initialValue(instance.isStatic());
  }

  /**
   * Creating a new builder for {@code
   * org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderMethodDtoBuilder builder = BuilderMethodDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code
   *     org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto}
   */
  public static BuilderMethodDtoBuilder create() {
    return new BuilderMethodDtoBuilder();
  }

  /**
   * Sets the value for <code>code</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code(sb -> sb.append("text"));
   * }</pre>
   *
   * @param codeStringBuilderConsumer consumer providing an instance of code
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder code(Consumer<StringBuilder> codeStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    codeStringBuilderConsumer.accept(builder);
    this.code = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>code</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code(() -> "example code");
   * }</pre>
   *
   * @param codeSupplier supplier for code
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder code(Supplier<String> codeSupplier) {
    this.code = changedValue(codeSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>code</code> by using String.format(format, args). See {@link
   * String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setCode(String) setCode(String code)}
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
  public BuilderMethodDtoBuilder code(String format, Object... args) {
    this.code = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>code</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setCode(String) setCode(String code)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.code("example code");
   * }</pre>
   *
   * @param code code
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder code(String code) {
    this.code = changedValue(code);
    return this;
  }

  /**
   * Sets the value for <code>constructorField</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setConstructorField(boolean)
   * setConstructorField(boolean constructorField)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructorField(() -> true);
   * }</pre>
   *
   * @param constructorFieldSupplier boolean supplier for constructorField
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder constructorField(BooleanSupplier constructorFieldSupplier) {
    this.constructorField = changedValue(constructorFieldSupplier.getAsBoolean());
    return this;
  }

  /**
   * Sets the value for <code>constructorField</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setConstructorField(boolean)
   * setConstructorField(boolean constructorField)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructorField(true);
   * }</pre>
   *
   * @param constructorField constructorField
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder constructorField(boolean constructorField) {
    this.constructorField = changedValue(constructorField);
    return this;
  }

  /**
   * Sets the value for <code>exampleChainFragment</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setExampleChainFragment(String)
   * setExampleChainFragment(String exampleChainFragment)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleChainFragment(sb -> sb.append("text"));
   * }</pre>
   *
   * @param exampleChainFragmentStringBuilderConsumer consumer providing an instance of
   *     exampleChainFragment
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder exampleChainFragment(
      Consumer<StringBuilder> exampleChainFragmentStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    exampleChainFragmentStringBuilderConsumer.accept(builder);
    this.exampleChainFragment = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>exampleChainFragment</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setExampleChainFragment(String)
   * setExampleChainFragment(String exampleChainFragment)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleChainFragment(() -> "example exampleChainFragment");
   * }</pre>
   *
   * @param exampleChainFragmentSupplier supplier for exampleChainFragment
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder exampleChainFragment(
      Supplier<String> exampleChainFragmentSupplier) {
    this.exampleChainFragment = changedValue(exampleChainFragmentSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>exampleChainFragment</code> by using String.format(format,
   * args). See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setExampleChainFragment(String)
   * setExampleChainFragment(String exampleChainFragment)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleChainFragment("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder exampleChainFragment(String format, Object... args) {
    this.exampleChainFragment = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>exampleChainFragment</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setExampleChainFragment(String)
   * setExampleChainFragment(String exampleChainFragment)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleChainFragment("example exampleChainFragment");
   * }</pre>
   *
   * @param exampleChainFragment exampleChainFragment
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder exampleChainFragment(String exampleChainFragment) {
    this.exampleChainFragment = changedValue(exampleChainFragment);
    return this;
  }

  /**
   * Sets the value for <code>isStatic</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setStatic(boolean) setStatic(boolean
   * isStatic)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.isStatic(() -> true);
   * }</pre>
   *
   * @param isStaticSupplier boolean supplier for isStatic
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder isStatic(BooleanSupplier isStaticSupplier) {
    this.isStatic = changedValue(isStaticSupplier.getAsBoolean());
    return this;
  }

  /**
   * Sets the value for <code>isStatic</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setStatic(boolean) setStatic(boolean
   * isStatic)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.isStatic(true);
   * }</pre>
   *
   * @param isStatic isStatic
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder isStatic(boolean isStatic) {
    this.isStatic = changedValue(isStatic);
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public BuilderMethodDtoBuilder javadoc(Consumer<JavadocDtoBuilder> javadocBuilderConsumer) {
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
   * <p>Generated from setter {@link BuilderMethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public BuilderMethodDtoBuilder javadoc(Supplier<JavadocDto> javadocSupplier) {
    this.javadoc = changedValue(javadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public BuilderMethodDtoBuilder javadoc(JavadocDto javadoc) {
    this.javadoc = changedValue(javadoc);
    return this;
  }

  /**
   * Sets the value for <code>methodName</code> by executing the provided consumer.
   *
   * <p>Generated from parameter in constructor {@link BuilderMethodDto#BuilderMethodDto(String,
   * TypeName) BuilderMethodDto(String methodName, TypeName returnType)}
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
  public BuilderMethodDtoBuilder methodName(
      Consumer<StringBuilder> methodNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    methodNameStringBuilderConsumer.accept(builder);
    this.methodName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>methodName</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link BuilderMethodDto#BuilderMethodDto(String,
   * TypeName) BuilderMethodDto(String methodName, TypeName returnType)}
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
  public BuilderMethodDtoBuilder methodName(Supplier<String> methodNameSupplier) {
    this.methodName = changedValue(methodNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>methodName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from parameter in constructor {@link BuilderMethodDto#BuilderMethodDto(String,
   * TypeName) BuilderMethodDto(String methodName, TypeName returnType)}
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
  public BuilderMethodDtoBuilder methodName(String format, Object... args) {
    this.methodName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>methodName</code>.
   *
   * <p>Generated from parameter in constructor {@link BuilderMethodDto#BuilderMethodDto(String,
   * TypeName) BuilderMethodDto(String methodName, TypeName returnType)}
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
  public BuilderMethodDtoBuilder methodName(String methodName) {
    this.methodName = changedValue(methodName);
    return this;
  }

  /**
   * Sets the value for <code>modifier</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setModifier(AccessModifier)
   * setModifier(AccessModifier modifier)}
   *
   * @param modifierSupplier supplier for modifier
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder modifier(Supplier<AccessModifier> modifierSupplier) {
    this.modifier = changedValue(modifierSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>modifier</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setModifier(AccessModifier)
   * setModifier(AccessModifier modifier)}
   *
   * @param modifier modifier
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder modifier(AccessModifier modifier) {
    this.modifier = changedValue(modifier);
    return this;
  }

  /**
   * Sets the value for <code>ordering</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setOrdering(int) setOrdering(int ordering)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.ordering(() -> 42);
   * }</pre>
   *
   * @param orderingSupplier int supplier for ordering
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder ordering(IntSupplier orderingSupplier) {
    this.ordering = changedValue(orderingSupplier.getAsInt());
    return this;
  }

  /**
   * Sets the value for <code>ordering</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setOrdering(int) setOrdering(int ordering)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.ordering(42);
   * }</pre>
   *
   * @param ordering ordering
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder ordering(int ordering) {
    this.ordering = changedValue(ordering);
    return this;
  }

  /**
   * Sets the value for <code>priority</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setPriority(int) setPriority(int priority)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.priority(() -> 42);
   * }</pre>
   *
   * @param prioritySupplier int supplier for priority
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder priority(IntSupplier prioritySupplier) {
    this.priority = changedValue(prioritySupplier.getAsInt());
    return this;
  }

  /**
   * Sets the value for <code>priority</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setPriority(int) setPriority(int priority)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.priority(42);
   * }</pre>
   *
   * @param priority priority
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder priority(int priority) {
    this.priority = changedValue(priority);
    return this;
  }

  /**
   * Sets the value for <code>returnType</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link BuilderMethodDto#BuilderMethodDto(String,
   * TypeName) BuilderMethodDto(String methodName, TypeName returnType)}
   *
   * @param returnTypeSupplier supplier for returnType
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder returnType(Supplier<TypeName> returnTypeSupplier) {
    this.returnType = changedValue(returnTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>returnType</code>.
   *
   * <p>Generated from parameter in constructor {@link BuilderMethodDto#BuilderMethodDto(String,
   * TypeName) BuilderMethodDto(String methodName, TypeName returnType)}
   *
   * @param returnType returnType
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder returnType(TypeName returnType) {
    this.returnType = changedValue(returnType);
    return this;
  }

  /**
   * Sets <code>returnType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeList(Class<?> elementClass) {
    return returnType(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeList(TypeName elementType) {
    return returnType(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeSet(Class<?> elementClass) {
    return returnType(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeSet(TypeName elementType) {
    return returnType(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeArray(Class<?> innerClass) {
    return returnType(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeArray(TypeName innerType) {
    return returnType(TypeNameArray.of(innerType));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeGeneric(
      Class<?> outerType, Class<?>... innerTypeArguments) {
    return returnType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeGeneric(
      TypeName outerType, TypeName... innerTypeArguments) {
    return returnType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeMap(Class<?> keyClass, Class<?> valueClass) {
    return returnType(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>returnType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnTypeMap(TypeName keyType, TypeName valueType) {
    return returnType(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>returnType</code> by class.
   *
   * @param returnType the class to set as <code>returnType</code>
   * @return fluent {@code builder} instance
   */
  public BuilderMethodDtoBuilder returnType(Class<?> returnType) {
    return returnType(TypeName.of(returnType));
  }

  /**
   * Sets the value for <code>returnType</code> from a package and simple class name.
   *
   * @param packageName package of the type
   * @param className simple name of the type
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder returnType(String packageName, String className) {
    return returnType(new TypeName(packageName, className));
  }

  /**
   * Sets the value for <code>sourceDescription</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceDescription(String)
   * setSourceDescription(String sourceDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceDescription(sb -> sb.append("text"));
   * }</pre>
   *
   * @param sourceDescriptionStringBuilderConsumer consumer providing an instance of
   *     sourceDescription
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceDescription(
      Consumer<StringBuilder> sourceDescriptionStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    sourceDescriptionStringBuilderConsumer.accept(builder);
    this.sourceDescription = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>sourceDescription</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceDescription(String)
   * setSourceDescription(String sourceDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceDescription(() -> "example sourceDescription");
   * }</pre>
   *
   * @param sourceDescriptionSupplier supplier for sourceDescription
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceDescription(Supplier<String> sourceDescriptionSupplier) {
    this.sourceDescription = changedValue(sourceDescriptionSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>sourceDescription</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceDescription(String)
   * setSourceDescription(String sourceDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceDescription("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceDescription(String format, Object... args) {
    this.sourceDescription = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>sourceDescription</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceDescription(String)
   * setSourceDescription(String sourceDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceDescription("example sourceDescription");
   * }</pre>
   *
   * @param sourceDescription sourceDescription
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceDescription(String sourceDescription) {
    this.sourceDescription = changedValue(sourceDescription);
    return this;
  }

  /**
   * Sets the value for <code>sourceFieldName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceFieldName(String)
   * setSourceFieldName(String sourceFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceFieldName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param sourceFieldNameStringBuilderConsumer consumer providing an instance of sourceFieldName
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceFieldName(
      Consumer<StringBuilder> sourceFieldNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    sourceFieldNameStringBuilderConsumer.accept(builder);
    this.sourceFieldName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>sourceFieldName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceFieldName(String)
   * setSourceFieldName(String sourceFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceFieldName(() -> "example sourceFieldName");
   * }</pre>
   *
   * @param sourceFieldNameSupplier supplier for sourceFieldName
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceFieldName(Supplier<String> sourceFieldNameSupplier) {
    this.sourceFieldName = changedValue(sourceFieldNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>sourceFieldName</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceFieldName(String)
   * setSourceFieldName(String sourceFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceFieldName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceFieldName(String format, Object... args) {
    this.sourceFieldName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>sourceFieldName</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setSourceFieldName(String)
   * setSourceFieldName(String sourceFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.sourceFieldName("example sourceFieldName");
   * }</pre>
   *
   * @param sourceFieldName sourceFieldName
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder sourceFieldName(String sourceFieldName) {
    this.sourceFieldName = changedValue(sourceFieldName);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public BuilderMethodDtoBuilder conditional(
      BooleanSupplier condition, Consumer<BuilderMethodDtoBuilder> yesCondition) {
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
  public BuilderMethodDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<BuilderMethodDtoBuilder> trueCase,
      Consumer<BuilderMethodDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a single element to <code>parameters</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setParameters(List)
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
  public BuilderMethodDtoBuilder add2Parameters(MethodParameterDto element) {
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
  public BuilderMethodDtoBuilder add2Parameters(
      Consumer<MethodParameterDtoBuilder> parameterBuilderConsumer) {
    MethodParameterDtoBuilder elementBuilder = new MethodParameterDtoBuilder();
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
  public final BuilderMethodDtoBuilder add2Parameters(
      Consumer<MethodParameterDtoBuilder>... parameterBuilderConsumers) {
    for (Consumer<MethodParameterDtoBuilder> consumer : parameterBuilderConsumers) {
      add2Parameters(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>parameters</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(() -> List.of(MethodParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parametersSupplier supplier for parameters
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder parameters(Supplier<List<MethodParameterDto>> parametersSupplier) {
    this.parameters = changedValue(parametersSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>parameters</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setParameters(List)
   * setParameters(List<MethodParameterDto> parameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameters(List.of(MethodParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parameters parameters
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder parameters(List<MethodParameterDto> parameters) {
    this.parameters = changedValue(parameters);
    return this;
  }

  /**
   * Adds a single element to <code>genericParameters</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setGenericParameters(List)
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
  public BuilderMethodDtoBuilder add2GenericParameters(GenericParameterDto element) {
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
   * Appends one element to <code>genericParameters</code>, configured through its builder.
   *
   * @param genericParameterBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder add2GenericParameters(
      Consumer<GenericParameterDtoBuilder> genericParameterBuilderConsumer) {
    GenericParameterDtoBuilder elementBuilder = new GenericParameterDtoBuilder();
    genericParameterBuilderConsumer.accept(elementBuilder);
    return add2GenericParameters(elementBuilder.build());
  }

  /**
   * Appends elements to <code>genericParameters</code>, each configured through its builder.
   *
   * @param genericParameterBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final BuilderMethodDtoBuilder add2GenericParameters(
      Consumer<GenericParameterDtoBuilder>... genericParameterBuilderConsumers) {
    for (Consumer<GenericParameterDtoBuilder> consumer : genericParameterBuilderConsumers) {
      add2GenericParameters(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>genericParameters</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(() -> List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param genericParametersSupplier supplier for genericParameters
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder genericParameters(
      Supplier<List<GenericParameterDto>> genericParametersSupplier) {
    this.genericParameters = changedValue(genericParametersSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>genericParameters</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setGenericParameters(List)
   * setGenericParameters(List<GenericParameterDto> genericParameters)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.genericParameters(List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param genericParameters genericParameters
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder genericParameters(List<GenericParameterDto> genericParameters) {
    this.genericParameters = changedValue(genericParameters);
    return this;
  }

  /**
   * Adds a single element to <code>annotations</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setAnnotations(List)
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
  public BuilderMethodDtoBuilder add2Annotations(AnnotationDto element) {
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
  public BuilderMethodDtoBuilder add2Annotations(Class<?> annotationType) {
    return add2Annotations(AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>annotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder add2Annotations(String packageName, String className) {
    return add2Annotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>annotations</code>, configured through its builder.
   *
   * @param annotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public BuilderMethodDtoBuilder add2Annotations(
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
  public final BuilderMethodDtoBuilder add2Annotations(
      Consumer<AnnotationDtoBuilder>... annotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : annotationBuilderConsumers) {
      add2Annotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>annotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setAnnotations(List)
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
  public BuilderMethodDtoBuilder annotations(Supplier<List<AnnotationDto>> annotationsSupplier) {
    this.annotations = changedValue(annotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>annotations</code>.
   *
   * <p>Generated from setter {@link BuilderMethodDto#setAnnotations(List)
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
  public BuilderMethodDtoBuilder annotations(List<AnnotationDto> annotations) {
    this.annotations = changedValue(annotations);
    return this;
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderMethodDto result = builder.build();
   * }</pre>
   */
  @Override
  public BuilderMethodDto build() {
    if (this.constructorField.isSet() && this.constructorField.value() == null) {
      throw new IllegalStateException(
          "Field 'constructorField' is marked as non-null but null value was provided");
    }
    if (this.ordering.isSet() && this.ordering.value() == null) {
      throw new IllegalStateException(
          "Field 'ordering' is marked as non-null but null value was provided");
    }
    if (this.priority.isSet() && this.priority.value() == null) {
      throw new IllegalStateException(
          "Field 'priority' is marked as non-null but null value was provided");
    }
    if (this.isStatic.isSet() && this.isStatic.value() == null) {
      throw new IllegalStateException(
          "Field 'isStatic' is marked as non-null but null value was provided");
    }
    BuilderMethodDto result =
        new BuilderMethodDto(this.methodName.value(), this.returnType.value());
    this.code.ifSet(result::setCode);
    this.constructorField.ifSet(result::setConstructorField);
    this.exampleChainFragment.ifSet(result::setExampleChainFragment);
    this.javadoc.ifSet(result::setJavadoc);
    this.modifier.ifSet(result::setModifier);
    this.ordering.ifSet(result::setOrdering);
    this.priority.ifSet(result::setPriority);
    this.sourceDescription.ifSet(result::setSourceDescription);
    this.sourceFieldName.ifSet(result::setSourceFieldName);
    this.isStatic.ifSet(result::setStatic);
    this.parameters.ifSet(result::setParameters);
    this.genericParameters.ifSet(result::setGenericParameters);
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
        .append("methodName", this.methodName)
        .append("returnType", this.returnType)
        .append("code", this.code)
        .append("constructorField", this.constructorField)
        .append("exampleChainFragment", this.exampleChainFragment)
        .append("javadoc", this.javadoc)
        .append("modifier", this.modifier)
        .append("ordering", this.ordering)
        .append("priority", this.priority)
        .append("sourceDescription", this.sourceDescription)
        .append("sourceFieldName", this.sourceFieldName)
        .append("isStatic", this.isStatic)
        .append("parameters", this.parameters)
        .append("genericParameters", this.genericParameters)
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
    default BuilderMethodDto with(Consumer<BuilderMethodDtoBuilder> b) {
      BuilderMethodDtoBuilder builder;
      try {
        builder = new BuilderMethodDtoBuilder(BuilderMethodDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderMethodDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderMethodDto'",
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
    default BuilderMethodDtoBuilder with() {
      try {
        return new BuilderMethodDtoBuilder(BuilderMethodDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderMethodDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderMethodDto'",
            ex);
      }
    }
  }
}
