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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.core.DeprecationInfoDto;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
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
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDtoBuilder;

/**
 * Builder for {@code org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto with method chaining and
 * validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the
 * final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * BuilderFieldDto result = BuilderFieldDtoBuilder.create()
 *     .defaultValue("example defaultValue")
 *     .defaultValue("Hello %s", "World")
 *     .defaultValue(() -> "example defaultValue")
 *     .defaultValue(sb -> sb.append("text"))
 *     .fieldGenerics(List.of(GenericParameterDtoBuilder.create().build()))
 *     .fieldGenerics(() -> List.of(GenericParameterDtoBuilder.create().build()))
 *     .add2FieldGenerics(element -> {})
 *     .add2FieldGenerics(first -> {}, second -> {})
 *     .add2FieldGenerics(GenericParameterDtoBuilder.create().build())
 *     .fieldNameInBuilder("example fieldNameInBuilder")
 *     .fieldNameInBuilder("Hello %s", "World")
 *     .fieldNameInBuilder(() -> "example fieldNameInBuilder")
 *     .fieldNameInBuilder(sb -> sb.append("text"))
 *     .getterInfo(GetterInfoDtoBuilder.create().build())
 *     .getterInfo(() -> GetterInfoDtoBuilder.create().build())
 *     .getterInfo(getterInfoDtoBuilder -> getterInfoDtoBuilder)
 *     .javaDoc(new JavadocDto())
 *     .javaDoc(JavadocDto::new)
 *     .javaDoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .nonNullable(true)
 *     .nonNullable(() -> true)
 *     .originalFieldName("example originalFieldName")
 *     .originalFieldName("Hello %s", "World")
 *     .originalFieldName(() -> "example originalFieldName")
 *     .originalFieldName(sb -> sb.append("text"))
 *     .originalJavaDocDescription("example originalJavaDocDescription")
 *     .originalJavaDocDescription("Hello %s", "World")
 *     .originalJavaDocDescription(() -> "example originalJavaDocDescription")
 *     .originalJavaDocDescription(sb -> sb.append("text"))
 *     .parameterAnnotations(List.of(AnnotationDtoBuilder.create().build()))
 *     .parameterAnnotations(() -> List.of(AnnotationDtoBuilder.create().build()))
 *     .add2ParameterAnnotations(element -> {})
 *     .add2ParameterAnnotations(first -> {}, second -> {})
 *     .add2ParameterAnnotations(AnnotationDtoBuilder.create().build())
 *     .setterName("example setterName")
 *     .setterName("Hello %s", "World")
 *     .setterName(() -> "example setterName")
 *     .setterName(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class BuilderFieldDtoBuilder implements IBuilderBase<BuilderFieldDto> {

  /** Tracked value for <code>defaultValue</code>: defaultValue. */
  private TrackedValue<String> defaultValue = unsetValue();

  /** Tracked value for <code>deprecationInfo</code>: deprecationInfo. */
  private TrackedValue<DeprecationInfoDto> deprecationInfo = unsetValue();

  /** Tracked value for <code>fieldGenerics</code>: fieldGenerics. */
  private TrackedValue<List<GenericParameterDto>> fieldGenerics = unsetValue();

  /** Tracked value for <code>fieldNameInBuilder</code>: fieldNameInBuilder. */
  private TrackedValue<String> fieldNameInBuilder = unsetValue();

  /** Tracked value for <code>fieldType</code>: fieldType. */
  private TrackedValue<TypeName> fieldType = unsetValue();

  /** Tracked value for <code>getterInfo</code>: getterInfo. */
  private TrackedValue<GetterInfoDto> getterInfo = unsetValue();

  /** Tracked value for <code>javaDoc</code>: javaDoc. */
  private TrackedValue<JavadocDto> javaDoc = unsetValue();

  /** Tracked value for <code>nonNullable</code>: nonNullable. */
  private TrackedValue<Boolean> nonNullable = unsetValue();

  /** Tracked value for <code>originalFieldName</code>: originalFieldName. */
  private TrackedValue<String> originalFieldName = unsetValue();

  /** Tracked value for <code>originalJavaDocDescription</code>: originalJavaDocDescription. */
  private TrackedValue<String> originalJavaDocDescription = unsetValue();

  /** Tracked value for <code>parameterAnnotations</code>: parameterAnnotations. */
  private TrackedValue<List<AnnotationDto>> parameterAnnotations = unsetValue();

  /** Tracked value for <code>setterName</code>: setterName. */
  private TrackedValue<String> setterName = unsetValue();

  private TrackedValue<List<BuilderMethodDto>> methods = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}.
   */
  public BuilderFieldDtoBuilder() {}

  /**
   * Initialisation of builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto} by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public BuilderFieldDtoBuilder(BuilderFieldDto instance) {
    this.deprecationInfo = initialValue(instance.getDeprecationInfo());
    this.fieldGenerics = initialValue(instance.getFieldGenerics());
    this.fieldNameInBuilder = initialValue(instance.getFieldNameInBuilder());
    this.fieldType = initialValue(instance.getFieldType());
    this.getterInfo = initialValue(instance.getGetterInfo());
    this.javaDoc = initialValue(instance.getJavaDoc());
    this.nonNullable = initialValue(instance.isNonNullable());
    this.originalFieldName = initialValue(instance.getOriginalFieldName());
    this.originalJavaDocDescription = initialValue(instance.getOriginalJavaDocDescription());
    this.parameterAnnotations = initialValue(instance.getParameterAnnotations());
  }

  /**
   * Creating a new builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderFieldDtoBuilder builder = BuilderFieldDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code
   *     org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}
   */
  public static BuilderFieldDtoBuilder create() {
    return new BuilderFieldDtoBuilder();
  }

  /**
   * Sets the value for <code>defaultValue</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue(sb -> sb.append("text"));
   * }</pre>
   *
   * @param defaultValueStringBuilderConsumer consumer providing an instance of defaultValue
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(
      Consumer<StringBuilder> defaultValueStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    defaultValueStringBuilderConsumer.accept(builder);
    this.defaultValue = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>defaultValue</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue(() -> "example defaultValue");
   * }</pre>
   *
   * @param defaultValueSupplier supplier for defaultValue
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(Supplier<String> defaultValueSupplier) {
    this.defaultValue = changedValue(defaultValueSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>defaultValue</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(String format, Object... args) {
    this.defaultValue = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>defaultValue</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue("example defaultValue");
   * }</pre>
   *
   * @param defaultValue defaultValue
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(String defaultValue) {
    this.defaultValue = changedValue(defaultValue);
    return this;
  }

  /**
   * Sets the value for <code>deprecationInfo</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDeprecationInfo(DeprecationInfoDto)
   * setDeprecationInfo(DeprecationInfoDto deprecationInfo)}
   *
   * @param deprecationInfoSupplier supplier for deprecationInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder deprecationInfo(
      Supplier<DeprecationInfoDto> deprecationInfoSupplier) {
    this.deprecationInfo = changedValue(deprecationInfoSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>deprecationInfo</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDeprecationInfo(DeprecationInfoDto)
   * setDeprecationInfo(DeprecationInfoDto deprecationInfo)}
   *
   * @param deprecationInfo deprecationInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder deprecationInfo(DeprecationInfoDto deprecationInfo) {
    this.deprecationInfo = changedValue(deprecationInfo);
    return this;
  }

  /**
   * Adds a single element to <code>fieldGenerics</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2FieldGenerics(GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2FieldGenerics(GenericParameterDto element) {
    List<GenericParameterDto> newCollection;
    if (this.fieldGenerics.isSet()) {
      newCollection = new ArrayList<>(this.fieldGenerics.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.fieldGenerics = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>fieldGenerics</code>, configured through its builder.
   *
   * @param fieldGenericBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2FieldGenerics(
      Consumer<GenericParameterDtoBuilder> fieldGenericBuilderConsumer) {
    GenericParameterDtoBuilder elementBuilder = new GenericParameterDtoBuilder();
    fieldGenericBuilderConsumer.accept(elementBuilder);
    return add2FieldGenerics(elementBuilder.build());
  }

  /**
   * Appends elements to <code>fieldGenerics</code>, each configured through its builder.
   *
   * @param fieldGenericBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final BuilderFieldDtoBuilder add2FieldGenerics(
      Consumer<GenericParameterDtoBuilder>... fieldGenericBuilderConsumers) {
    for (Consumer<GenericParameterDtoBuilder> consumer : fieldGenericBuilderConsumers) {
      add2FieldGenerics(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>fieldGenerics</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(() -> List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param fieldGenericsSupplier supplier for fieldGenerics
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldGenerics(
      Supplier<List<GenericParameterDto>> fieldGenericsSupplier) {
    this.fieldGenerics = changedValue(fieldGenericsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>fieldGenerics</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param fieldGenerics fieldGenerics
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldGenerics(List<GenericParameterDto> fieldGenerics) {
    this.fieldGenerics = changedValue(fieldGenerics);
    return this;
  }

  /**
   * Sets the value for <code>fieldNameInBuilder</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder(sb -> sb.append("text"));
   * }</pre>
   *
   * @param fieldNameInBuilderStringBuilderConsumer consumer providing an instance of
   *     fieldNameInBuilder
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(
      Consumer<StringBuilder> fieldNameInBuilderStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    fieldNameInBuilderStringBuilderConsumer.accept(builder);
    this.fieldNameInBuilder = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>fieldNameInBuilder</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder(() -> "example fieldNameInBuilder");
   * }</pre>
   *
   * @param fieldNameInBuilderSupplier supplier for fieldNameInBuilder
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(Supplier<String> fieldNameInBuilderSupplier) {
    this.fieldNameInBuilder = changedValue(fieldNameInBuilderSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>fieldNameInBuilder</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(String format, Object... args) {
    this.fieldNameInBuilder = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>fieldNameInBuilder</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder("example fieldNameInBuilder");
   * }</pre>
   *
   * @param fieldNameInBuilder fieldNameInBuilder
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(String fieldNameInBuilder) {
    this.fieldNameInBuilder = changedValue(fieldNameInBuilder);
    return this;
  }

  /**
   * Sets the value for <code>fieldType</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldTypeSupplier supplier for fieldType
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldType(Supplier<TypeName> fieldTypeSupplier) {
    this.fieldType = changedValue(fieldTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>fieldType</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldType fieldType
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldType(TypeName fieldType) {
    this.fieldType = changedValue(fieldType);
    return this;
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeList(Class<?> elementClass) {
    return fieldType(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeList(TypeName elementType) {
    return fieldType(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeSet(Class<?> elementClass) {
    return fieldType(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeSet(TypeName elementType) {
    return fieldType(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeArray(Class<?> innerClass) {
    return fieldType(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeArray(TypeName innerType) {
    return fieldType(TypeNameArray.of(innerType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeGeneric(
      Class<?> outerType, Class<?>... innerTypeArguments) {
    return fieldType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeGeneric(
      TypeName outerType, TypeName... innerTypeArguments) {
    return fieldType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeMap(Class<?> keyClass, Class<?> valueClass) {
    return fieldType(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeMap(TypeName keyType, TypeName valueType) {
    return fieldType(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>fieldType</code> by class.
   *
   * @param fieldType the class to set as <code>fieldType</code>
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldType(Class<?> fieldType) {
    return fieldType(TypeName.of(fieldType));
  }

  /**
   * Sets the value for <code>fieldType</code> from a package and simple class name.
   *
   * @param packageName package of the type
   * @param className simple name of the type
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldType(String packageName, String className) {
    return fieldType(new TypeName(packageName, className));
  }

  /**
   * Sets the value for <code>getterInfo</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterInfo(getterInfoDtoBuilder -> getterInfoDtoBuilder);
   * }</pre>
   *
   * @param getterInfoBuilderConsumer consumer providing an instance of a builder for getterInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder getterInfo(
      Consumer<GetterInfoDtoBuilder> getterInfoBuilderConsumer) {
    GetterInfoDtoBuilder builder =
        this.getterInfo.isSet()
            ? new GetterInfoDtoBuilder(this.getterInfo.value())
            : GetterInfoDtoBuilder.create();
    getterInfoBuilderConsumer.accept(builder);
    this.getterInfo = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>getterInfo</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterInfo(() -> GetterInfoDtoBuilder.create().build());
   * }</pre>
   *
   * @param getterInfoSupplier supplier for getterInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder getterInfo(Supplier<GetterInfoDto> getterInfoSupplier) {
    this.getterInfo = changedValue(getterInfoSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>getterInfo</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterInfo(GetterInfoDtoBuilder.create().build());
   * }</pre>
   *
   * @param getterInfo getterInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder getterInfo(GetterInfoDto getterInfo) {
    this.getterInfo = changedValue(getterInfo);
    return this;
  }

  /**
   * Sets the value for <code>javaDoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javaDoc(javadocDtoBuilder -> javadocDtoBuilder);
   * }</pre>
   *
   * @param javaDocBuilderConsumer consumer providing an instance of a builder for javaDoc
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder javaDoc(Consumer<JavadocDtoBuilder> javaDocBuilderConsumer) {
    JavadocDtoBuilder builder =
        this.javaDoc.isSet()
            ? new JavadocDtoBuilder(this.javaDoc.value())
            : JavadocDtoBuilder.create();
    javaDocBuilderConsumer.accept(builder);
    this.javaDoc = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>javaDoc</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javaDoc(JavadocDto::new);
   * }</pre>
   *
   * @param javaDocSupplier supplier for javaDoc
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder javaDoc(Supplier<JavadocDto> javaDocSupplier) {
    this.javaDoc = changedValue(javaDocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javaDoc</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javaDoc(new JavadocDto());
   * }</pre>
   *
   * @param javaDoc javaDoc
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder javaDoc(JavadocDto javaDoc) {
    this.javaDoc = changedValue(javaDoc);
    return this;
  }

  /**
   * Sets the value for <code>nonNullable</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setNonNullable(boolean) setNonNullable(boolean
   * nonNullable)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nonNullable(() -> true);
   * }</pre>
   *
   * @param nonNullableSupplier boolean supplier for nonNullable
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder nonNullable(BooleanSupplier nonNullableSupplier) {
    this.nonNullable = changedValue(nonNullableSupplier.getAsBoolean());
    return this;
  }

  /**
   * Sets the value for <code>nonNullable</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setNonNullable(boolean) setNonNullable(boolean
   * nonNullable)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nonNullable(true);
   * }</pre>
   *
   * @param nonNullable nonNullable
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder nonNullable(boolean nonNullable) {
    this.nonNullable = changedValue(nonNullable);
    return this;
  }

  /**
   * Sets the value for <code>originalFieldName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param originalFieldNameStringBuilderConsumer consumer providing an instance of
   *     originalFieldName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(
      Consumer<StringBuilder> originalFieldNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    originalFieldNameStringBuilderConsumer.accept(builder);
    this.originalFieldName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>originalFieldName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName(() -> "example originalFieldName");
   * }</pre>
   *
   * @param originalFieldNameSupplier supplier for originalFieldName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(Supplier<String> originalFieldNameSupplier) {
    this.originalFieldName = changedValue(originalFieldNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>originalFieldName</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(String format, Object... args) {
    this.originalFieldName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>originalFieldName</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName("example originalFieldName");
   * }</pre>
   *
   * @param originalFieldName originalFieldName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(String originalFieldName) {
    this.originalFieldName = changedValue(originalFieldName);
    return this;
  }

  /**
   * Sets the value for <code>originalJavaDocDescription</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription(sb -> sb.append("text"));
   * }</pre>
   *
   * @param originalJavaDocDescriptionStringBuilderConsumer consumer providing an instance of
   *     originalJavaDocDescription
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(
      Consumer<StringBuilder> originalJavaDocDescriptionStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    originalJavaDocDescriptionStringBuilderConsumer.accept(builder);
    this.originalJavaDocDescription = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>originalJavaDocDescription</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription(() -> "example originalJavaDocDescription");
   * }</pre>
   *
   * @param originalJavaDocDescriptionSupplier supplier for originalJavaDocDescription
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(
      Supplier<String> originalJavaDocDescriptionSupplier) {
    this.originalJavaDocDescription = changedValue(originalJavaDocDescriptionSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>originalJavaDocDescription</code> by using
   * String.format(format, args). See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(String format, Object... args) {
    this.originalJavaDocDescription = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>originalJavaDocDescription</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription("example originalJavaDocDescription");
   * }</pre>
   *
   * @param originalJavaDocDescription originalJavaDocDescription
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(String originalJavaDocDescription) {
    this.originalJavaDocDescription = changedValue(originalJavaDocDescription);
    return this;
  }

  /**
   * Adds a single element to <code>parameterAnnotations</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2ParameterAnnotations(AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2ParameterAnnotations(AnnotationDto element) {
    List<AnnotationDto> newCollection;
    if (this.parameterAnnotations.isSet()) {
      newCollection = new ArrayList<>(this.parameterAnnotations.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.parameterAnnotations = changedValue(newCollection);
    return this;
  }

  /**
   * Adds one annotation of the given type to <code>parameterAnnotations</code>.
   *
   * @param annotationType the annotation type (not null)
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2ParameterAnnotations(Class<?> annotationType) {
    return add2ParameterAnnotations(
        AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>parameterAnnotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2ParameterAnnotations(String packageName, String className) {
    return add2ParameterAnnotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>parameterAnnotations</code>, configured through its builder.
   *
   * @param parameterAnnotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2ParameterAnnotations(
      Consumer<AnnotationDtoBuilder> parameterAnnotationBuilderConsumer) {
    AnnotationDtoBuilder elementBuilder = AnnotationDtoBuilder.create();
    parameterAnnotationBuilderConsumer.accept(elementBuilder);
    return add2ParameterAnnotations(elementBuilder.build());
  }

  /**
   * Appends elements to <code>parameterAnnotations</code>, each configured through its builder.
   *
   * @param parameterAnnotationBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final BuilderFieldDtoBuilder add2ParameterAnnotations(
      Consumer<AnnotationDtoBuilder>... parameterAnnotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : parameterAnnotationBuilderConsumers) {
      add2ParameterAnnotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>parameterAnnotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(() -> List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parameterAnnotationsSupplier supplier for parameterAnnotations
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder parameterAnnotations(
      Supplier<List<AnnotationDto>> parameterAnnotationsSupplier) {
    this.parameterAnnotations = changedValue(parameterAnnotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>parameterAnnotations</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parameterAnnotations parameterAnnotations
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder parameterAnnotations(List<AnnotationDto> parameterAnnotations) {
    this.parameterAnnotations = changedValue(parameterAnnotations);
    return this;
  }

  /**
   * Sets the value for <code>setterName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param setterNameStringBuilderConsumer consumer providing an instance of setterName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(
      Consumer<StringBuilder> setterNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    setterNameStringBuilderConsumer.accept(builder);
    this.setterName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>setterName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName(() -> "example setterName");
   * }</pre>
   *
   * @param setterNameSupplier supplier for setterName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(Supplier<String> setterNameSupplier) {
    this.setterName = changedValue(setterNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>setterName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(String format, Object... args) {
    this.setterName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>setterName</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName("example setterName");
   * }</pre>
   *
   * @param setterName setterName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(String setterName) {
    this.setterName = changedValue(setterName);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public BuilderFieldDtoBuilder conditional(
      BooleanSupplier condition, Consumer<BuilderFieldDtoBuilder> yesCondition) {
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
  public BuilderFieldDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<BuilderFieldDtoBuilder> trueCase,
      Consumer<BuilderFieldDtoBuilder> falseCase) {
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
   * <p>Generated from setter {@link BuilderFieldDto#setMethods(List)
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
  public BuilderFieldDtoBuilder add2Methods(BuilderMethodDto element) {
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
  public BuilderFieldDtoBuilder add2Methods(
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
  public final BuilderFieldDtoBuilder add2Methods(
      Consumer<BuilderMethodDtoBuilder>... methodBuilderConsumers) {
    for (Consumer<BuilderMethodDtoBuilder> consumer : methodBuilderConsumers) {
      add2Methods(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>methods</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setMethods(List)
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
  public BuilderFieldDtoBuilder methods(Supplier<List<BuilderMethodDto>> methodsSupplier) {
    this.methods = changedValue(methodsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>methods</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setMethods(List)
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
  public BuilderFieldDtoBuilder methods(List<BuilderMethodDto> methods) {
    this.methods = changedValue(methods);
    return this;
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderFieldDto result = builder.build();
   * }</pre>
   */
  @Override
  public BuilderFieldDto build() {
    if (this.nonNullable.isSet() && this.nonNullable.value() == null) {
      throw new IllegalStateException(
          "Field 'nonNullable' is marked as non-null but null value was provided");
    }
    BuilderFieldDto result = new BuilderFieldDto();
    this.defaultValue.ifSet(result::setDefaultValue);
    this.deprecationInfo.ifSet(result::setDeprecationInfo);
    this.fieldGenerics.ifSet(result::setFieldGenerics);
    this.fieldNameInBuilder.ifSet(result::setFieldNameInBuilder);
    this.fieldType.ifSet(result::setFieldType);
    this.getterInfo.ifSet(result::setGetterInfo);
    this.javaDoc.ifSet(result::setJavaDoc);
    this.nonNullable.ifSet(result::setNonNullable);
    this.originalFieldName.ifSet(result::setOriginalFieldName);
    this.originalJavaDocDescription.ifSet(result::setOriginalJavaDocDescription);
    this.parameterAnnotations.ifSet(result::setParameterAnnotations);
    this.setterName.ifSet(result::setSetterName);
    this.methods.ifSet(result::setMethods);
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
        .append("defaultValue", this.defaultValue)
        .append("deprecationInfo", this.deprecationInfo)
        .append("fieldGenerics", this.fieldGenerics)
        .append("fieldNameInBuilder", this.fieldNameInBuilder)
        .append("fieldType", this.fieldType)
        .append("getterInfo", this.getterInfo)
        .append("javaDoc", this.javaDoc)
        .append("nonNullable", this.nonNullable)
        .append("originalFieldName", this.originalFieldName)
        .append("originalJavaDocDescription", this.originalJavaDocDescription)
        .append("parameterAnnotations", this.parameterAnnotations)
        .append("setterName", this.setterName)
        .append("methods", this.methods)
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
    default BuilderFieldDto with(Consumer<BuilderFieldDtoBuilder> b) {
      BuilderFieldDtoBuilder builder;
      try {
        builder = new BuilderFieldDtoBuilder(BuilderFieldDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderFieldDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderFieldDto'",
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
    default BuilderFieldDtoBuilder with() {
      try {
        return new BuilderFieldDtoBuilder(BuilderFieldDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderFieldDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderFieldDto'",
            ex);
      }
    }
  }
}
