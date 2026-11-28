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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.imports.RegularImport;
import org.javahelpers.codegen.model.imports.StaticImport;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.method.ConstructorDto;
import org.javahelpers.codegen.model.method.ConstructorDtoBuilder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.NestedTypeDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameSet;

/**
 * Builder for {@code org.javahelpers.codegen.model.core.GenerationTargetClassDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.core.GenerationTargetClassDto with method chaining and validation.
 * Use the static {@code create()} method to obtain a new builder instance, configure the desired
 * properties using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * GenerationTargetClassDto result = GenerationTargetClassDtoBuilder.create()
 *     .classAnnotations(Set.of(AnnotationDtoBuilder.create().build()))
 *     .classAnnotations(() -> Set.of(AnnotationDtoBuilder.create().build()))
 *     .add2ClassAnnotations(element -> {})
 *     .add2ClassAnnotations(first -> {}, second -> {})
 *     .add2ClassAnnotations(AnnotationDtoBuilder.create().build())
 *     .classFields(List.of(ClassFieldDtoBuilder.create().build()))
 *     .classFields(() -> List.of(ClassFieldDtoBuilder.create().build()))
 *     .add2ClassFields(element -> {})
 *     .add2ClassFields(first -> {}, second -> {})
 *     .add2ClassFields(ClassFieldDtoBuilder.create().build())
 *     .classJavadoc(new JavadocDto())
 *     .classJavadoc(JavadocDto::new)
 *     .classJavadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .constructors(List.of(ConstructorDtoBuilder.create().build()))
 *     .constructors(() -> List.of(ConstructorDtoBuilder.create().build()))
 *     .add2Constructors(element -> {})
 *     .add2Constructors(first -> {}, second -> {})
 *     .add2Constructors(ConstructorDtoBuilder.create().build())
 *     .generics(List.of(GenericParameterDtoBuilder.create().build()))
 *     .generics(() -> List.of(GenericParameterDtoBuilder.create().build()))
 *     .add2Generics(element -> {})
 *     .add2Generics(first -> {}, second -> {})
 *     .add2Generics(GenericParameterDtoBuilder.create().build())
 *     .methods(List.of(MethodDtoBuilder.create().build()))
 *     .methods(() -> List.of(MethodDtoBuilder.create().build()))
 *     .add2Methods(element -> {})
 *     .add2Methods(first -> {}, second -> {})
 *     .add2Methods(MethodDtoBuilder.create().build())
 *     .nestedTypes(List.of(NestedTypeDtoBuilder.create().build()))
 *     .nestedTypes(() -> List.of(NestedTypeDtoBuilder.create().build()))
 *     .add2NestedTypes(element -> {})
 *     .add2NestedTypes(first -> {}, second -> {})
 *     .add2NestedTypes(NestedTypeDtoBuilder.create().build())
 *     .build();
 * }</pre>
 */
public class GenerationTargetClassDtoBuilder implements IBuilderBase<GenerationTargetClassDto> {

  /** Tracked value for <code>classAccessModifier</code>: classAccessModifier. */
  private TrackedValue<AccessModifier> classAccessModifier = unsetValue();

  /** Tracked value for <code>classAnnotations</code>: the replacement collection (not null). */
  private TrackedValue<Set<AnnotationDto>> classAnnotations = unsetValue();

  /** Tracked value for <code>classFields</code>: the replacement collection (not null). */
  private TrackedValue<List<ClassFieldDto>> classFields = unsetValue();

  /** Tracked value for <code>classJavadoc</code>: the class JavaDoc to set. */
  private TrackedValue<JavadocDto> classJavadoc = unsetValue();

  /** Tracked value for <code>constructors</code>: the replacement collection (not null). */
  private TrackedValue<List<ConstructorDto>> constructors = unsetValue();

  /**
   * Tracked value for <code>formattingMode</code>: the formatting mode, or null to use the global
   * default.
   */
  private TrackedValue<FormattingMode> formattingMode = unsetValue();

  /** Tracked value for <code>generics</code>: the replacement collection (not null). */
  private TrackedValue<List<GenericParameterDto>> generics = unsetValue();

  /** Tracked value for <code>imports</code>: the replacement collection (not null). */
  private TrackedValue<Set<ImportStatement>> imports = unsetValue();

  /** Tracked value for <code>interfaces</code>: the replacement collection (not null). */
  private TrackedValue<Set<InterfaceName>> interfaces = unsetValue();

  /** Tracked value for <code>methods</code>: the replacement collection (not null). */
  private TrackedValue<List<MethodDto>> methods = unsetValue();

  /** Tracked value for <code>nestedTypes</code>: the replacement collection (not null). */
  private TrackedValue<List<NestedTypeDto>> nestedTypes = unsetValue();

  /** Tracked value for <code>superType</code>: superType. */
  private TrackedValue<TypeName> superType = unsetValue();

  /** Tracked value for <code>typeName</code>: typeName. */
  private TrackedValue<TypeName> typeName = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.codegen.model.core.GenerationTargetClassDto}.
   */
  public GenerationTargetClassDtoBuilder() {}

  /**
   * Initialisation of builder for {@code
   * org.javahelpers.codegen.model.core.GenerationTargetClassDto} by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public GenerationTargetClassDtoBuilder(GenerationTargetClassDto instance) {
    this.classAccessModifier = initialValue(instance.getClassAccessModifier());
    this.classAnnotations = initialValue(instance.getClassAnnotations());
    this.classFields = initialValue(instance.getClassFields());
    this.classJavadoc = initialValue(instance.getClassJavadoc());
    this.constructors = initialValue(instance.getConstructors());
    this.formattingMode = initialValue(instance.getFormattingMode());
    this.generics = initialValue(instance.getGenerics());
    this.imports = initialValue(instance.getImports());
    this.interfaces = initialValue(instance.getInterfaces());
    this.methods = initialValue(instance.getMethods());
    this.nestedTypes = initialValue(instance.getNestedTypes());
    this.superType = initialValue(instance.getSuperType());
    this.typeName = initialValue(instance.getTypeName());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.core.GenerationTargetClassDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * GenerationTargetClassDtoBuilder builder = GenerationTargetClassDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.core.GenerationTargetClassDto}
   */
  public static GenerationTargetClassDtoBuilder create() {
    return new GenerationTargetClassDtoBuilder();
  }

  /**
   * Sets the value for <code>classAccessModifier</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAccessModifier(AccessModifier)
   * setClassAccessModifier(AccessModifier classAccessModifier)}
   *
   * @param classAccessModifierSupplier supplier for classAccessModifier
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classAccessModifier(
      Supplier<AccessModifier> classAccessModifierSupplier) {
    this.classAccessModifier = changedValue(classAccessModifierSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>classAccessModifier</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAccessModifier(AccessModifier)
   * setClassAccessModifier(AccessModifier classAccessModifier)}
   *
   * @param classAccessModifier classAccessModifier
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classAccessModifier(AccessModifier classAccessModifier) {
    this.classAccessModifier = changedValue(classAccessModifier);
    return this;
  }

  /**
   * Adds a single element to <code>classAnnotations</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAnnotations(Set)
   * setClassAnnotations(Set<AnnotationDto> classAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2ClassAnnotations(AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2ClassAnnotations(AnnotationDto element) {
    Set<AnnotationDto> newCollection;
    if (this.classAnnotations.isSet()) {
      newCollection = new HashSet<>(this.classAnnotations.value());
    } else {
      newCollection = new HashSet<>();
    }
    newCollection.add(element);
    this.classAnnotations = changedValue(newCollection);
    return this;
  }

  /**
   * Adds one annotation of the given type to <code>classAnnotations</code>.
   *
   * @param annotationType the annotation type (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2ClassAnnotations(Class<?> annotationType) {
    return add2ClassAnnotations(
        AnnotationDtoBuilder.create().annotationType(annotationType).build());
  }

  /**
   * Adds one annotation of the given type to <code>classAnnotations</code>.
   *
   * @param packageName package of the annotation type
   * @param className simple name of the annotation type
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2ClassAnnotations(
      String packageName, String className) {
    return add2ClassAnnotations(
        AnnotationDtoBuilder.create().annotationType(packageName, className).build());
  }

  /**
   * Appends one element to <code>classAnnotations</code>, configured through its builder.
   *
   * @param classAnnotationBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2ClassAnnotations(
      Consumer<AnnotationDtoBuilder> classAnnotationBuilderConsumer) {
    AnnotationDtoBuilder elementBuilder = AnnotationDtoBuilder.create();
    classAnnotationBuilderConsumer.accept(elementBuilder);
    return add2ClassAnnotations(elementBuilder.build());
  }

  /**
   * Appends elements to <code>classAnnotations</code>, each configured through its builder.
   *
   * @param classAnnotationBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final GenerationTargetClassDtoBuilder add2ClassAnnotations(
      Consumer<AnnotationDtoBuilder>... classAnnotationBuilderConsumers) {
    for (Consumer<AnnotationDtoBuilder> consumer : classAnnotationBuilderConsumers) {
      add2ClassAnnotations(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>classAnnotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAnnotations(Set)
   * setClassAnnotations(Set<AnnotationDto> classAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classAnnotations(() -> Set.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param classAnnotationsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classAnnotations(
      Supplier<Set<AnnotationDto>> classAnnotationsSupplier) {
    this.classAnnotations = changedValue(classAnnotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>classAnnotations</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAnnotations(Set)
   * setClassAnnotations(Set<AnnotationDto> classAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classAnnotations(Set.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param classAnnotations the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classAnnotations(Set<AnnotationDto> classAnnotations) {
    this.classAnnotations = changedValue(classAnnotations);
    return this;
  }

  /**
   * Adds a single element to <code>classFields</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassFields(List)
   * setClassFields(List<ClassFieldDto> classFields)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2ClassFields(ClassFieldDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2ClassFields(ClassFieldDto element) {
    List<ClassFieldDto> newCollection;
    if (this.classFields.isSet()) {
      newCollection = new ArrayList<>(this.classFields.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.classFields = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>classFields</code>, configured through its builder.
   *
   * @param classFieldBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2ClassFields(
      Consumer<ClassFieldDtoBuilder> classFieldBuilderConsumer) {
    ClassFieldDtoBuilder elementBuilder = ClassFieldDtoBuilder.create();
    classFieldBuilderConsumer.accept(elementBuilder);
    return add2ClassFields(elementBuilder.build());
  }

  /**
   * Appends elements to <code>classFields</code>, each configured through its builder.
   *
   * @param classFieldBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final GenerationTargetClassDtoBuilder add2ClassFields(
      Consumer<ClassFieldDtoBuilder>... classFieldBuilderConsumers) {
    for (Consumer<ClassFieldDtoBuilder> consumer : classFieldBuilderConsumers) {
      add2ClassFields(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>classFields</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassFields(List)
   * setClassFields(List<ClassFieldDto> classFields)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classFields(() -> List.of(ClassFieldDtoBuilder.create().build()));
   * }</pre>
   *
   * @param classFieldsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classFields(
      Supplier<List<ClassFieldDto>> classFieldsSupplier) {
    this.classFields = changedValue(classFieldsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>classFields</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassFields(List)
   * setClassFields(List<ClassFieldDto> classFields)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classFields(List.of(ClassFieldDtoBuilder.create().build()));
   * }</pre>
   *
   * @param classFields the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classFields(List<ClassFieldDto> classFields) {
    this.classFields = changedValue(classFields);
    return this;
  }

  /**
   * Sets the value for <code>classJavadoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassJavadoc(JavadocDto)
   * setClassJavadoc(JavadocDto classJavadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classJavadoc(javadocDtoBuilder -> javadocDtoBuilder);
   * }</pre>
   *
   * @param classJavadocBuilderConsumer consumer providing an instance of a builder for the class
   *     JavaDoc to set
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classJavadoc(
      Consumer<JavadocDtoBuilder> classJavadocBuilderConsumer) {
    JavadocDtoBuilder builder =
        this.classJavadoc.isSet()
            ? new JavadocDtoBuilder(this.classJavadoc.value())
            : JavadocDtoBuilder.create();
    classJavadocBuilderConsumer.accept(builder);
    this.classJavadoc = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>classJavadoc</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassJavadoc(JavadocDto)
   * setClassJavadoc(JavadocDto classJavadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classJavadoc(JavadocDto::new);
   * }</pre>
   *
   * @param classJavadocSupplier supplier for the class JavaDoc to set
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classJavadoc(Supplier<JavadocDto> classJavadocSupplier) {
    this.classJavadoc = changedValue(classJavadocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>classJavadoc</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassJavadoc(JavadocDto)
   * setClassJavadoc(JavadocDto classJavadoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classJavadoc(new JavadocDto());
   * }</pre>
   *
   * @param classJavadoc the class JavaDoc to set
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classJavadoc(JavadocDto classJavadoc) {
    this.classJavadoc = changedValue(classJavadoc);
    return this;
  }

  /**
   * Adds a single element to <code>constructors</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setConstructors(List)
   * setConstructors(List<ConstructorDto> constructors)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2Constructors(ConstructorDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Constructors(ConstructorDto element) {
    List<ConstructorDto> newCollection;
    if (this.constructors.isSet()) {
      newCollection = new ArrayList<>(this.constructors.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.constructors = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>constructors</code>, configured through its builder.
   *
   * @param constructorBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Constructors(
      Consumer<ConstructorDtoBuilder> constructorBuilderConsumer) {
    ConstructorDtoBuilder elementBuilder = ConstructorDtoBuilder.create();
    constructorBuilderConsumer.accept(elementBuilder);
    return add2Constructors(elementBuilder.build());
  }

  /**
   * Appends elements to <code>constructors</code>, each configured through its builder.
   *
   * @param constructorBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final GenerationTargetClassDtoBuilder add2Constructors(
      Consumer<ConstructorDtoBuilder>... constructorBuilderConsumers) {
    for (Consumer<ConstructorDtoBuilder> consumer : constructorBuilderConsumers) {
      add2Constructors(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>constructors</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setConstructors(List)
   * setConstructors(List<ConstructorDto> constructors)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructors(() -> List.of(ConstructorDtoBuilder.create().build()));
   * }</pre>
   *
   * @param constructorsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder constructors(
      Supplier<List<ConstructorDto>> constructorsSupplier) {
    this.constructors = changedValue(constructorsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>constructors</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setConstructors(List)
   * setConstructors(List<ConstructorDto> constructors)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructors(List.of(ConstructorDtoBuilder.create().build()));
   * }</pre>
   *
   * @param constructors the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder constructors(List<ConstructorDto> constructors) {
    this.constructors = changedValue(constructors);
    return this;
  }

  /**
   * Sets the value for <code>formattingMode</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setFormattingMode(FormattingMode)
   * setFormattingMode(FormattingMode formattingMode)}
   *
   * @param formattingModeSupplier supplier for the formatting mode, or null to use the global
   *     default
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder formattingMode(
      Supplier<FormattingMode> formattingModeSupplier) {
    this.formattingMode = changedValue(formattingModeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>formattingMode</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setFormattingMode(FormattingMode)
   * setFormattingMode(FormattingMode formattingMode)}
   *
   * @param formattingMode the formatting mode, or null to use the global default
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder formattingMode(FormattingMode formattingMode) {
    this.formattingMode = changedValue(formattingMode);
    return this;
  }

  /**
   * Adds a single element to <code>generics</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setGenerics(List)
   * setGenerics(List<GenericParameterDto> generics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2Generics(GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Generics(GenericParameterDto element) {
    List<GenericParameterDto> newCollection;
    if (this.generics.isSet()) {
      newCollection = new ArrayList<>(this.generics.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.generics = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>generics</code>, configured through its builder.
   *
   * @param genericBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Generics(
      Consumer<GenericParameterDtoBuilder> genericBuilderConsumer) {
    GenericParameterDtoBuilder elementBuilder = GenericParameterDtoBuilder.create();
    genericBuilderConsumer.accept(elementBuilder);
    return add2Generics(elementBuilder.build());
  }

  /**
   * Appends elements to <code>generics</code>, each configured through its builder.
   *
   * @param genericBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final GenerationTargetClassDtoBuilder add2Generics(
      Consumer<GenericParameterDtoBuilder>... genericBuilderConsumers) {
    for (Consumer<GenericParameterDtoBuilder> consumer : genericBuilderConsumers) {
      add2Generics(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>generics</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setGenerics(List)
   * setGenerics(List<GenericParameterDto> generics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.generics(() -> List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param genericsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder generics(
      Supplier<List<GenericParameterDto>> genericsSupplier) {
    this.generics = changedValue(genericsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>generics</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setGenerics(List)
   * setGenerics(List<GenericParameterDto> generics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.generics(List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param generics the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder generics(List<GenericParameterDto> generics) {
    this.generics = changedValue(generics);
    return this;
  }

  /**
   * Adds a single element to <code>imports</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Imports(ImportStatement element) {
    Set<ImportStatement> newCollection;
    if (this.imports.isSet()) {
      newCollection = new HashSet<>(this.imports.value());
    } else {
      newCollection = new HashSet<>();
    }
    newCollection.add(element);
    this.imports = changedValue(newCollection);
    return this;
  }

  /**
   * Adds an import for the given class.
   *
   * @param clazz class to import
   * @return this builder
   */
  public GenerationTargetClassDtoBuilder add2Imports(Class<?> clazz) {
    return add2Imports(new RegularImport(TypeName.of(clazz)));
  }

  /**
   * Adds an import for the given type.
   *
   * @param typeName type to import
   * @return this builder
   */
  public GenerationTargetClassDtoBuilder add2Imports(TypeName typeName) {
    return add2Imports(new RegularImport(typeName));
  }

  /**
   * Sets the value for <code>imports</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * @param imports the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder imports(ImportStatement... imports) {
    this.imports = changedValue(new LinkedHashSet<>(Arrays.asList(imports)));
    return this;
  }

  /**
   * Sets the value for <code>imports</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * @param importsBuilderConsumer consumer providing an instance of a builder for the replacement
   *     collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder imports(
      Consumer<HashSetBuilder<ImportStatement>> importsBuilderConsumer) {
    HashSetBuilder<ImportStatement> builder =
        this.imports.isSet()
            ? new HashSetBuilder<ImportStatement>(this.imports.value())
            : new HashSetBuilder<ImportStatement>();
    importsBuilderConsumer.accept(builder);
    this.imports = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>imports</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * @param importsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder imports(Supplier<Set<ImportStatement>> importsSupplier) {
    this.imports = changedValue(importsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>imports</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * @param imports the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder imports(Set<ImportStatement> imports) {
    this.imports = changedValue(imports);
    return this;
  }

  /**
   * Adds a single element to <code>interfaces</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setInterfaces(Set)
   * setInterfaces(Set<InterfaceName> interfaces)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Interfaces(InterfaceName element) {
    Set<InterfaceName> newCollection;
    if (this.interfaces.isSet()) {
      newCollection = new HashSet<>(this.interfaces.value());
    } else {
      newCollection = new HashSet<>();
    }
    newCollection.add(element);
    this.interfaces = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>interfaces</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setInterfaces(Set)
   * setInterfaces(Set<InterfaceName> interfaces)}
   *
   * @param interfaces the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder interfaces(InterfaceName... interfaces) {
    this.interfaces = changedValue(new LinkedHashSet<>(Arrays.asList(interfaces)));
    return this;
  }

  /**
   * Sets the value for <code>interfaces</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setInterfaces(Set)
   * setInterfaces(Set<InterfaceName> interfaces)}
   *
   * @param interfacesBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder interfaces(
      Consumer<HashSetBuilder<InterfaceName>> interfacesBuilderConsumer) {
    HashSetBuilder<InterfaceName> builder =
        this.interfaces.isSet()
            ? new HashSetBuilder<InterfaceName>(this.interfaces.value())
            : new HashSetBuilder<InterfaceName>();
    interfacesBuilderConsumer.accept(builder);
    this.interfaces = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>interfaces</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setInterfaces(Set)
   * setInterfaces(Set<InterfaceName> interfaces)}
   *
   * @param interfacesSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder interfaces(
      Supplier<Set<InterfaceName>> interfacesSupplier) {
    this.interfaces = changedValue(interfacesSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>interfaces</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setInterfaces(Set)
   * setInterfaces(Set<InterfaceName> interfaces)}
   *
   * @param interfaces the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder interfaces(Set<InterfaceName> interfaces) {
    this.interfaces = changedValue(interfaces);
    return this;
  }

  /**
   * Adds a single element to <code>methods</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setMethods(List)
   * setMethods(List<MethodDto> methods)}
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
  public GenerationTargetClassDtoBuilder add2Methods(MethodDto element) {
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
  public GenerationTargetClassDtoBuilder add2Methods(
      Consumer<MethodDtoBuilder> methodBuilderConsumer) {
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
  public final GenerationTargetClassDtoBuilder add2Methods(
      Consumer<MethodDtoBuilder>... methodBuilderConsumers) {
    for (Consumer<MethodDtoBuilder> consumer : methodBuilderConsumers) {
      add2Methods(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>methods</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setMethods(List)
   * setMethods(List<MethodDto> methods)}
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
  public GenerationTargetClassDtoBuilder methods(Supplier<List<MethodDto>> methodsSupplier) {
    this.methods = changedValue(methodsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>methods</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setMethods(List)
   * setMethods(List<MethodDto> methods)}
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
  public GenerationTargetClassDtoBuilder methods(List<MethodDto> methods) {
    this.methods = changedValue(methods);
    return this;
  }

  /**
   * Adds a single element to <code>nestedTypes</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setNestedTypes(List)
   * setNestedTypes(List<NestedTypeDto> nestedTypes)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2NestedTypes(NestedTypeDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2NestedTypes(NestedTypeDto element) {
    List<NestedTypeDto> newCollection;
    if (this.nestedTypes.isSet()) {
      newCollection = new ArrayList<>(this.nestedTypes.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.nestedTypes = changedValue(newCollection);
    return this;
  }

  /**
   * Appends one element to <code>nestedTypes</code>, configured through its builder.
   *
   * @param nestedTypeBuilderConsumer consumer configuring the new element (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2NestedTypes(
      Consumer<NestedTypeDtoBuilder> nestedTypeBuilderConsumer) {
    NestedTypeDtoBuilder elementBuilder = NestedTypeDtoBuilder.create();
    nestedTypeBuilderConsumer.accept(elementBuilder);
    return add2NestedTypes(elementBuilder.build());
  }

  /**
   * Appends elements to <code>nestedTypes</code>, each configured through its builder.
   *
   * @param nestedTypeBuilderConsumers consumers configuring the new elements (not null)
   * @return current instance of builder
   */
  @SafeVarargs
  public final GenerationTargetClassDtoBuilder add2NestedTypes(
      Consumer<NestedTypeDtoBuilder>... nestedTypeBuilderConsumers) {
    for (Consumer<NestedTypeDtoBuilder> consumer : nestedTypeBuilderConsumers) {
      add2NestedTypes(consumer);
    }
    return this;
  }

  /**
   * Sets the value for <code>nestedTypes</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setNestedTypes(List)
   * setNestedTypes(List<NestedTypeDto> nestedTypes)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nestedTypes(() -> List.of(NestedTypeDtoBuilder.create().build()));
   * }</pre>
   *
   * @param nestedTypesSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder nestedTypes(
      Supplier<List<NestedTypeDto>> nestedTypesSupplier) {
    this.nestedTypes = changedValue(nestedTypesSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>nestedTypes</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setNestedTypes(List)
   * setNestedTypes(List<NestedTypeDto> nestedTypes)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nestedTypes(List.of(NestedTypeDtoBuilder.create().build()));
   * }</pre>
   *
   * @param nestedTypes the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder nestedTypes(List<NestedTypeDto> nestedTypes) {
    this.nestedTypes = changedValue(nestedTypes);
    return this;
  }

  /**
   * Sets the value for <code>superType</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setSuperType(TypeName)
   * setSuperType(TypeName superType)}
   *
   * @param superTypeSupplier supplier for superType
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder superType(Supplier<TypeName> superTypeSupplier) {
    this.superType = changedValue(superTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>superType</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setSuperType(TypeName)
   * setSuperType(TypeName superType)}
   *
   * @param superType superType
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder superType(TypeName superType) {
    this.superType = changedValue(superType);
    return this;
  }

  /**
   * Sets <code>superType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeList(Class<?> elementClass) {
    return superType(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>superType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeList(TypeName elementType) {
    return superType(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>superType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeSet(Class<?> elementClass) {
    return superType(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>superType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeSet(TypeName elementType) {
    return superType(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>superType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeArray(Class<?> innerClass) {
    return superType(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>superType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeArray(TypeName innerType) {
    return superType(TypeNameArray.of(innerType));
  }

  /**
   * Sets <code>superType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeGeneric(
      Class<?> outerType, Class<?>... innerTypeArguments) {
    return superType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>superType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeGeneric(
      TypeName outerType, TypeName... innerTypeArguments) {
    return superType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>superType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeMap(Class<?> keyClass, Class<?> valueClass) {
    return superType(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>superType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superTypeMap(TypeName keyType, TypeName valueType) {
    return superType(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>superType</code> by class.
   *
   * @param superType the class to set as <code>superType</code>
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder superType(Class<?> superType) {
    return superType(TypeName.of(superType));
  }

  /**
   * Sets the value for <code>superType</code> from package and class name.
   *
   * @param packageName the package of the type
   * @param className the simple name of the type
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder superType(String packageName, String className) {
    return superType(new TypeName(packageName, className));
  }

  /**
   * Sets the value for <code>typeName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setTypeName(TypeName)
   * setTypeName(TypeName typeName)}
   *
   * @param typeNameSupplier supplier for typeName
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder typeName(Supplier<TypeName> typeNameSupplier) {
    this.typeName = changedValue(typeNameSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>typeName</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setTypeName(TypeName)
   * setTypeName(TypeName typeName)}
   *
   * @param typeName typeName
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder typeName(TypeName typeName) {
    this.typeName = changedValue(typeName);
    return this;
  }

  /**
   * Sets <code>typeName</code> by class.
   *
   * @param typeName the class to set as <code>typeName</code>
   * @return fluent {@code builder} instance
   */
  public GenerationTargetClassDtoBuilder typeName(Class<?> typeName) {
    return typeName(TypeName.of(typeName));
  }

  /**
   * Sets the value for <code>typeName</code> from package and class name.
   *
   * @param packageName the package of the type
   * @param className the simple name of the type
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder typeName(String packageName, String className) {
    return typeName(new TypeName(packageName, className));
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public GenerationTargetClassDtoBuilder conditional(
      BooleanSupplier condition, Consumer<GenerationTargetClassDtoBuilder> yesCondition) {
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
  public GenerationTargetClassDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<GenerationTargetClassDtoBuilder> trueCase,
      Consumer<GenerationTargetClassDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a static import for a method/field used in the generated class (shortcut mirroring {@link
   * GenerationTargetClassDto#addStaticImport(Class, String)}).
   *
   * @param clazz the class containing the static member
   * @param memberName the name of the static member
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2StaticImports(Class<?> clazz, String memberName) {
    return add2Imports(new StaticImport(TypeName.of(clazz), memberName));
  }

  /**
   * Adds a static import for a method/field used in the generated class (shortcut mirroring {@link
   * GenerationTargetClassDto#addStaticImport(TypeName, String)}).
   *
   * @param type the type containing the static member
   * @param memberName the name of the static member
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2StaticImports(TypeName type, String memberName) {
    return add2Imports(new StaticImport(type, memberName));
  }

  /**
   * Adds an interface to be implemented by the generated class.
   *
   * @param clazz the interface class
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Interfaces(Class<?> clazz) {
    return add2Interfaces(new InterfaceName(clazz.getPackageName(), clazz.getSimpleName()));
  }

  /**
   * Adds an interface to be implemented by the generated class.
   *
   * @param packageName the package of the interface
   * @param simpleName the simple name of the interface
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Interfaces(String packageName, String simpleName) {
    return add2Interfaces(new InterfaceName(packageName, simpleName));
  }

  /**
   * Adds an unbounded generic parameter with the given name.
   *
   * @param name the name of the generic parameter
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Generics(String name) {
    return add2Generics(GenericParameterDtoBuilder.create().name(name).build());
  }

  /**
   * Adds a generic parameter with the given name and upper bounds.
   *
   * @param name the name of the generic parameter
   * @param upperBounds the upper bounds of the generic parameter
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder add2Generics(String name, TypeName... upperBounds) {
    return add2Generics(
        GenericParameterDtoBuilder.create().name(name).upperBounds(upperBounds).build());
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * GenerationTargetClassDto result = builder.build();
   * }</pre>
   */
  @Override
  public GenerationTargetClassDto build() {
    GenerationTargetClassDto result = new GenerationTargetClassDto();
    this.classAccessModifier.ifSet(result::setClassAccessModifier);
    this.classAnnotations.ifSet(result::setClassAnnotations);
    this.classFields.ifSet(result::setClassFields);
    this.classJavadoc.ifSet(result::setClassJavadoc);
    this.constructors.ifSet(result::setConstructors);
    this.formattingMode.ifSet(result::setFormattingMode);
    this.generics.ifSet(result::setGenerics);
    this.imports.ifSet(result::setImports);
    this.interfaces.ifSet(result::setInterfaces);
    this.methods.ifSet(result::setMethods);
    this.nestedTypes.ifSet(result::setNestedTypes);
    this.superType.ifSet(result::setSuperType);
    this.typeName.ifSet(result::setTypeName);
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
        .append("classAccessModifier", this.classAccessModifier)
        .append("classAnnotations", this.classAnnotations)
        .append("classFields", this.classFields)
        .append("classJavadoc", this.classJavadoc)
        .append("constructors", this.constructors)
        .append("formattingMode", this.formattingMode)
        .append("generics", this.generics)
        .append("imports", this.imports)
        .append("interfaces", this.interfaces)
        .append("methods", this.methods)
        .append("nestedTypes", this.nestedTypes)
        .append("superType", this.superType)
        .append("typeName", this.typeName)
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
    default GenerationTargetClassDto with(Consumer<GenerationTargetClassDtoBuilder> b) {
      GenerationTargetClassDtoBuilder builder;
      try {
        builder = new GenerationTargetClassDtoBuilder(GenerationTargetClassDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'GenerationTargetClassDtoBuilder.With' should only be implemented by classes, which could be casted to 'GenerationTargetClassDto'",
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
    default GenerationTargetClassDtoBuilder with() {
      try {
        return new GenerationTargetClassDtoBuilder(GenerationTargetClassDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'GenerationTargetClassDtoBuilder.With' should only be implemented by classes, which could be casted to 'GenerationTargetClassDto'",
            ex);
      }
    }
  }
}
