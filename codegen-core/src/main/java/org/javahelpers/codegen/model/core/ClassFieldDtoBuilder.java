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

package org.javahelpers.codegen.model.core;

import static org.javahelpers.codegen.builders.TrackedValue.changedValue;
import static org.javahelpers.codegen.builders.TrackedValue.initialValue;
import static org.javahelpers.codegen.builders.TrackedValue.unsetValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.HashSetBuilder;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.imports.RegularImport;
import org.javahelpers.codegen.model.imports.StaticImport;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameSet;

/**
 * Builder for {@code org.javahelpers.codegen.model.core.ClassFieldDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.core.ClassFieldDto with method chaining and validation. Use the
 * static {@code create()} method to obtain a new builder instance, configure the desired properties
 * using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * ClassFieldDto result = ClassFieldDtoBuilder.create()
 *     .fieldName("example fieldName")
 *     .fieldName("Hello %s", "World")
 *     .fieldName(() -> "example fieldName")
 *     .fieldName(sb -> sb.append("text"))
 *     .javadoc(new JavadocDto())
 *     .javadoc(JavadocDto::new)
 *     .javadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .literalInitializer("example literalInitializer")
 *     .literalInitializer("Hello %s", "World")
 *     .literalInitializer(() -> "example literalInitializer")
 *     .literalInitializer(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class ClassFieldDtoBuilder implements IBuilderBase<ClassFieldDto> {

  /** Tracked value for <code>fieldName</code>: fieldName. */
  private TrackedValue<String> fieldName = unsetValue();

  /** Tracked value for <code>fieldType</code>: fieldType. */
  private TrackedValue<TypeName> fieldType = unsetValue();

  /** Tracked value for <code>fieldTypeImports</code>: the replacement collection (not null). */
  private TrackedValue<Set<ImportStatement>> fieldTypeImports = unsetValue();

  /** Tracked value for <code>javadoc</code>: javadoc. */
  private TrackedValue<JavadocDto> javadoc = unsetValue();

  /** Tracked value for <code>literalInitializer</code>: literalInitializer. */
  private TrackedValue<String> literalInitializer = unsetValue();

  /** Tracked value for <code>visibility</code>: visibility. */
  private TrackedValue<AccessModifier> visibility = unsetValue();

  /** Empty constructor of builder for {@code org.javahelpers.codegen.model.core.ClassFieldDto}. */
  public ClassFieldDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.core.ClassFieldDto} by a
   * instance.
   *
   * @param instance object instance for initialisiation
   */
  public ClassFieldDtoBuilder(ClassFieldDto instance) {
    this.fieldName = initialValue(instance.getFieldName());
    this.fieldType = initialValue(instance.getFieldType());
    this.fieldTypeImports = initialValue(instance.getFieldTypeImports());
    this.javadoc = initialValue(instance.getJavadoc());
    this.literalInitializer = initialValue(instance.getLiteralInitializer());
    this.visibility = initialValue(instance.getVisibility());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.core.ClassFieldDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * ClassFieldDtoBuilder builder = ClassFieldDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.core.ClassFieldDto}
   */
  public static ClassFieldDtoBuilder create() {
    return new ClassFieldDtoBuilder();
  }

  /**
   * Sets the value for <code>fieldName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldName(String) setFieldName(String
   * fieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param fieldNameStringBuilderConsumer consumer providing an instance of fieldName
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldName(Consumer<StringBuilder> fieldNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    fieldNameStringBuilderConsumer.accept(builder);
    this.fieldName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>fieldName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldName(String) setFieldName(String
   * fieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldName(() -> "example fieldName");
   * }</pre>
   *
   * @param fieldNameSupplier supplier for fieldName
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldName(Supplier<String> fieldNameSupplier) {
    this.fieldName = changedValue(fieldNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>fieldName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldName(String) setFieldName(String
   * fieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldName(String format, Object... args) {
    this.fieldName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>fieldName</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldName(String) setFieldName(String
   * fieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldName("example fieldName");
   * }</pre>
   *
   * @param fieldName fieldName
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldName(String fieldName) {
    this.fieldName = changedValue(fieldName);
    return this;
  }

  /**
   * Sets the value for <code>fieldType</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldTypeSupplier supplier for fieldType
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldType(Supplier<TypeName> fieldTypeSupplier) {
    this.fieldType = changedValue(fieldTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>fieldType</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldType fieldType
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldType(TypeName fieldType) {
    this.fieldType = changedValue(fieldType);
    return this;
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeList(Class<?> elementClass) {
    return fieldType(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeList(TypeName elementType) {
    return fieldType(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeSet(Class<?> elementClass) {
    return fieldType(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeSet(TypeName elementType) {
    return fieldType(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeArray(Class<?> innerClass) {
    return fieldType(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeArray(TypeName innerType) {
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
  public ClassFieldDtoBuilder fieldTypeGeneric(Class<?> outerType, Class<?>... innerTypeArguments) {
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
  public ClassFieldDtoBuilder fieldTypeGeneric(TypeName outerType, TypeName... innerTypeArguments) {
    return fieldType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeMap(Class<?> keyClass, Class<?> valueClass) {
    return fieldType(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldTypeMap(TypeName keyType, TypeName valueType) {
    return fieldType(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>fieldType</code> by class.
   *
   * @param fieldType the class to set as <code>fieldType</code>
   * @return fluent {@code builder} instance
   */
  public ClassFieldDtoBuilder fieldType(Class<?> fieldType) {
    return fieldType(TypeName.of(fieldType));
  }

  /**
   * Sets the value for <code>fieldType</code> from package and class name.
   *
   * @param packageName the package of the type
   * @param className the simple name of the type
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldType(String packageName, String className) {
    return fieldType(new TypeName(packageName, className));
  }

  /**
   * Adds a single element to <code>fieldTypeImports</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldTypeImports(Set)
   * setFieldTypeImports(Set<ImportStatement> fieldTypeImports)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder add2FieldTypeImports(ImportStatement element) {
    Set<ImportStatement> newCollection;
    if (this.fieldTypeImports.isSet()) {
      newCollection = new HashSet<>(this.fieldTypeImports.value());
    } else {
      newCollection = new HashSet<>();
    }
    newCollection.add(element);
    this.fieldTypeImports = changedValue(newCollection);
    return this;
  }

  /**
   * Adds an import for the field type for the given class.
   *
   * @param clazz class to import
   * @return this builder
   */
  public ClassFieldDtoBuilder add2FieldTypeImports(Class<?> clazz) {
    return add2FieldTypeImports(new RegularImport(TypeName.of(clazz)));
  }

  /**
   * Adds an import for the field type for the given type.
   *
   * @param typeName type to import
   * @return this builder
   */
  public ClassFieldDtoBuilder add2FieldTypeImports(TypeName typeName) {
    return add2FieldTypeImports(new RegularImport(typeName));
  }

  /**
   * Sets the value for <code>fieldTypeImports</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldTypeImports(Set)
   * setFieldTypeImports(Set<ImportStatement> fieldTypeImports)}
   *
   * @param fieldTypeImports the replacement collection (not null)
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldTypeImports(ImportStatement... fieldTypeImports) {
    this.fieldTypeImports = changedValue(new LinkedHashSet<>(Arrays.asList(fieldTypeImports)));
    return this;
  }

  /**
   * Sets the value for <code>fieldTypeImports</code> using a builder consumer that produces the
   * value.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldTypeImports(Set)
   * setFieldTypeImports(Set<ImportStatement> fieldTypeImports)}
   *
   * @param fieldTypeImportsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldTypeImports(
      Consumer<HashSetBuilder<ImportStatement>> fieldTypeImportsBuilderConsumer) {
    HashSetBuilder<ImportStatement> builder =
        this.fieldTypeImports.isSet()
            ? new HashSetBuilder<ImportStatement>(this.fieldTypeImports.value())
            : new HashSetBuilder<ImportStatement>();
    fieldTypeImportsBuilderConsumer.accept(builder);
    this.fieldTypeImports = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>fieldTypeImports</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldTypeImports(Set)
   * setFieldTypeImports(Set<ImportStatement> fieldTypeImports)}
   *
   * @param fieldTypeImportsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldTypeImports(
      Supplier<Set<ImportStatement>> fieldTypeImportsSupplier) {
    this.fieldTypeImports = changedValue(fieldTypeImportsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>fieldTypeImports</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setFieldTypeImports(Set)
   * setFieldTypeImports(Set<ImportStatement> fieldTypeImports)}
   *
   * @param fieldTypeImports the replacement collection (not null)
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder fieldTypeImports(Set<ImportStatement> fieldTypeImports) {
    this.fieldTypeImports = changedValue(fieldTypeImports);
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link ClassFieldDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public ClassFieldDtoBuilder javadoc(Consumer<JavadocDtoBuilder> javadocBuilderConsumer) {
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
   * <p>Generated from setter {@link ClassFieldDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public ClassFieldDtoBuilder javadoc(Supplier<JavadocDto> javadocSupplier) {
    this.javadoc = changedValue(javadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javadoc</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setJavadoc(JavadocDto) setJavadoc(JavadocDto
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
  public ClassFieldDtoBuilder javadoc(JavadocDto javadoc) {
    this.javadoc = changedValue(javadoc);
    return this;
  }

  /**
   * Sets the value for <code>literalInitializer</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link ClassFieldDto#setLiteralInitializer(String)
   * setLiteralInitializer(String literalInitializer)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.literalInitializer(sb -> sb.append("text"));
   * }</pre>
   *
   * @param literalInitializerStringBuilderConsumer consumer providing an instance of
   *     literalInitializer
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder literalInitializer(
      Consumer<StringBuilder> literalInitializerStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    literalInitializerStringBuilderConsumer.accept(builder);
    this.literalInitializer = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>literalInitializer</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ClassFieldDto#setLiteralInitializer(String)
   * setLiteralInitializer(String literalInitializer)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.literalInitializer(() -> "example literalInitializer");
   * }</pre>
   *
   * @param literalInitializerSupplier supplier for literalInitializer
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder literalInitializer(Supplier<String> literalInitializerSupplier) {
    this.literalInitializer = changedValue(literalInitializerSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>literalInitializer</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link ClassFieldDto#setLiteralInitializer(String)
   * setLiteralInitializer(String literalInitializer)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.literalInitializer("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder literalInitializer(String format, Object... args) {
    this.literalInitializer = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>literalInitializer</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setLiteralInitializer(String)
   * setLiteralInitializer(String literalInitializer)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.literalInitializer("example literalInitializer");
   * }</pre>
   *
   * @param literalInitializer literalInitializer
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder literalInitializer(String literalInitializer) {
    this.literalInitializer = changedValue(literalInitializer);
    return this;
  }

  /**
   * Sets the value for <code>visibility</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link ClassFieldDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibilitySupplier supplier for visibility
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder visibility(Supplier<AccessModifier> visibilitySupplier) {
    this.visibility = changedValue(visibilitySupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>visibility</code>.
   *
   * <p>Generated from setter {@link ClassFieldDto#setVisibility(AccessModifier)
   * setVisibility(AccessModifier visibility)}
   *
   * @param visibility visibility
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder visibility(AccessModifier visibility) {
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
  public ClassFieldDtoBuilder conditional(
      BooleanSupplier condition, Consumer<ClassFieldDtoBuilder> yesCondition) {
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
  public ClassFieldDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<ClassFieldDtoBuilder> trueCase,
      Consumer<ClassFieldDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a static import for a method/field used in the field initializer (shortcut mirroring
   * {@link ClassFieldDto#addStaticImport(Class, String)}).
   *
   * @param clazz the class containing the static member
   * @param memberName the name of the static member
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder add2StaticImports(Class<?> clazz, String memberName) {
    return add2FieldTypeImports(new StaticImport(TypeName.of(clazz), memberName));
  }

  /**
   * Adds a static import for a method/field used in the field initializer (shortcut mirroring
   * {@link ClassFieldDto#addStaticImport(TypeName, String)}).
   *
   * @param type the type containing the static member
   * @param memberName the name of the static member
   * @return current instance of builder
   */
  public ClassFieldDtoBuilder add2StaticImports(TypeName type, String memberName) {
    return add2FieldTypeImports(new StaticImport(type, memberName));
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * ClassFieldDto result = builder.build();
   * }</pre>
   */
  @Override
  public ClassFieldDto build() {
    ClassFieldDto result = new ClassFieldDto();
    this.fieldName.ifSet(result::setFieldName);
    this.fieldType.ifSet(result::setFieldType);
    this.fieldTypeImports.ifSet(result::setFieldTypeImports);
    this.javadoc.ifSet(result::setJavadoc);
    this.literalInitializer.ifSet(result::setLiteralInitializer);
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
        .append("fieldName", this.fieldName)
        .append("fieldType", this.fieldType)
        .append("fieldTypeImports", this.fieldTypeImports)
        .append("javadoc", this.javadoc)
        .append("literalInitializer", this.literalInitializer)
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
    default ClassFieldDto with(Consumer<ClassFieldDtoBuilder> b) {
      ClassFieldDtoBuilder builder;
      try {
        builder = new ClassFieldDtoBuilder(ClassFieldDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'ClassFieldDtoBuilder.With' should only be implemented by classes, which could be casted to 'ClassFieldDto'",
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
    default ClassFieldDtoBuilder with() {
      try {
        return new ClassFieldDtoBuilder(ClassFieldDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'ClassFieldDtoBuilder.With' should only be implemented by classes, which could be casted to 'ClassFieldDto'",
            ex);
      }
    }
  }
}
