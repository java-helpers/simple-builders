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

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.processing.Generated;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.imports.ImportStatement;
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
import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

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
 *     .classAnnotations(t -> t.add(annotationDtoBuilder -> annotationDtoBuilder))
 *     .classAnnotations(AnnotationDtoBuilder.create().build(), AnnotationDtoBuilder.create().build())
 *     .classAnnotationsUpdate(TreeSet::new)
 *     .add2ClassAnnotations(AnnotationDtoBuilder.create().build())
 *     .classFields(List.of(ClassFieldDtoBuilder.create().build()))
 *     .classFields(() -> List.of(ClassFieldDtoBuilder.create().build()))
 *     .classFields(t -> t.add(classFieldDtoBuilder -> classFieldDtoBuilder))
 *     .classFields(ClassFieldDtoBuilder.create().build(), ClassFieldDtoBuilder.create().build())
 *     .classFieldsUpdate(list -> list.stream().sorted().toList())
 *     .add2ClassFields(ClassFieldDtoBuilder.create().build())
 *     .classJavadoc(new JavadocDto())
 *     .classJavadoc(JavadocDto::new)
 *     .classJavadoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .constructors(List.of(ConstructorDtoBuilder.create().build()))
 *     .constructors(() -> List.of(ConstructorDtoBuilder.create().build()))
 *     .constructors(t -> t.add(constructorDtoBuilder -> constructorDtoBuilder))
 *     .constructors(ConstructorDtoBuilder.create().build(), ConstructorDtoBuilder.create().build())
 *     .constructorsUpdate(list -> list.stream().sorted().toList())
 *     .add2Constructors(ConstructorDtoBuilder.create().build())
 *     .generics(List.of(GenericParameterDtoBuilder.create().build()))
 *     .generics(() -> List.of(GenericParameterDtoBuilder.create().build()))
 *     .generics(t -> t.add(genericParameterDtoBuilder -> genericParameterDtoBuilder))
 *     .generics(GenericParameterDtoBuilder.create().build(), GenericParameterDtoBuilder.create().build())
 *     .genericsUpdate(list -> list.stream().sorted().toList())
 *     .add2Generics(GenericParameterDtoBuilder.create().build())
 *     .importsUpdate(TreeSet::new)
 *     .interfacesUpdate(TreeSet::new)
 *     .methods(List.of(MethodDtoBuilder.create().build()))
 *     .methods(() -> List.of(MethodDtoBuilder.create().build()))
 *     .methods(t -> t.add(methodDtoBuilder -> methodDtoBuilder))
 *     .methods(MethodDtoBuilder.create().build(), MethodDtoBuilder.create().build())
 *     .methodsUpdate(list -> list.stream().sorted().toList())
 *     .add2Methods(MethodDtoBuilder.create().build())
 *     .nestedTypes(List.of(NestedTypeDtoBuilder.create().build()))
 *     .nestedTypes(() -> List.of(NestedTypeDtoBuilder.create().build()))
 *     .nestedTypes(t -> t.add(nestedTypeDtoBuilder -> nestedTypeDtoBuilder))
 *     .nestedTypes(NestedTypeDtoBuilder.create().build(), NestedTypeDtoBuilder.create().build())
 *     .nestedTypesUpdate(list -> list.stream().sorted().toList())
 *     .add2NestedTypes(NestedTypeDtoBuilder.create().build())
 *     .build();
 * }</pre>
 */
@Generated("Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
@BuilderImplementation(forClass = GenerationTargetClassDto.class)
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
   * Updates the current value of <code>classAccessModifier</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance).
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAccessModifier(AccessModifier)
   * setClassAccessModifier(AccessModifier classAccessModifier)}
   *
   * @param classAccessModifierUpdater operator applied to the current value; its result becomes the
   *     new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>classAccessModifier</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder classAccessModifierUpdate(
      UnaryOperator<AccessModifier> classAccessModifierUpdater) {
    if (!this.classAccessModifier.isSet()) {
      throw new IllegalStateException("Cannot update 'classAccessModifier' before it is set");
    }
    this.classAccessModifier =
        changedValue(classAccessModifierUpdater.apply(this.classAccessModifier.value()));
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
   * Sets the value for <code>classAnnotations</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAnnotations(Set)
   * setClassAnnotations(Set<AnnotationDto> classAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classAnnotations(AnnotationDtoBuilder.create().build(), AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param classAnnotations the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classAnnotations(AnnotationDto... classAnnotations) {
    this.classAnnotations = changedValue(Set.of(classAnnotations));
    return this;
  }

  /**
   * Sets the value for <code>classAnnotations</code> using a builder consumer that produces the
   * value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAnnotations(Set)
   * setClassAnnotations(Set<AnnotationDto> classAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classAnnotations(t -> t.add(annotationDtoBuilder -> annotationDtoBuilder));
   * }</pre>
   *
   * @param classAnnotationsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classAnnotations(
      Consumer<HashSetBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder>>
          classAnnotationsBuilderConsumer) {
    HashSetBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder> builder =
        this.classAnnotations.isSet()
            ? new HashSetBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder>(
                this.classAnnotations.value(), AnnotationDtoBuilder::create)
            : new HashSetBuilderWithElementBuilders<AnnotationDto, AnnotationDtoBuilder>(
                AnnotationDtoBuilder::create);
    classAnnotationsBuilderConsumer.accept(builder);
    this.classAnnotations = changedValue(builder.build());
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
   * Updates the current value of <code>classAnnotations</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance). For changing multiple elements of a nested DTO,
   * prefer the builder-consumer helper {@link #classAnnotations(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassAnnotations(Set)
   * setClassAnnotations(Set<AnnotationDto> classAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classAnnotations(Set.of(AnnotationDtoBuilder.create().build())).classAnnotationsUpdate(TreeSet::new);
   * }</pre>
   *
   * @param classAnnotationsUpdater operator applied to the current value; its result becomes the
   *     new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>classAnnotations</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder classAnnotationsUpdate(
      UnaryOperator<Set<AnnotationDto>> classAnnotationsUpdater) {
    if (!this.classAnnotations.isSet()) {
      throw new IllegalStateException("Cannot update 'classAnnotations' before it is set");
    }
    this.classAnnotations =
        changedValue(classAnnotationsUpdater.apply(this.classAnnotations.value()));
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
   * Sets the value for <code>classFields</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassFields(List)
   * setClassFields(List<ClassFieldDto> classFields)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classFields(ClassFieldDtoBuilder.create().build(), ClassFieldDtoBuilder.create().build());
   * }</pre>
   *
   * @param classFields the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classFields(ClassFieldDto... classFields) {
    this.classFields = changedValue(List.of(classFields));
    return this;
  }

  /**
   * Sets the value for <code>classFields</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassFields(List)
   * setClassFields(List<ClassFieldDto> classFields)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classFields(t -> t.add(classFieldDtoBuilder -> classFieldDtoBuilder));
   * }</pre>
   *
   * @param classFieldsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder classFields(
      Consumer<ArrayListBuilderWithElementBuilders<ClassFieldDto, ClassFieldDtoBuilder>>
          classFieldsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<ClassFieldDto, ClassFieldDtoBuilder> builder =
        this.classFields.isSet()
            ? new ArrayListBuilderWithElementBuilders<ClassFieldDto, ClassFieldDtoBuilder>(
                this.classFields.value(), ClassFieldDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<ClassFieldDto, ClassFieldDtoBuilder>(
                ClassFieldDtoBuilder::create);
    classFieldsBuilderConsumer.accept(builder);
    this.classFields = changedValue(builder.build());
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
   * Updates the current value of <code>classFields</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #classFields(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassFields(List)
   * setClassFields(List<ClassFieldDto> classFields)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.classFields(List.of(ClassFieldDtoBuilder.create().build()))
   *     .classFieldsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param classFieldsUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>classFields</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder classFieldsUpdate(
      UnaryOperator<List<ClassFieldDto>> classFieldsUpdater) {
    if (!this.classFields.isSet()) {
      throw new IllegalStateException("Cannot update 'classFields' before it is set");
    }
    this.classFields = changedValue(classFieldsUpdater.apply(this.classFields.value()));
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
   * Updates the current value of <code>classJavadoc</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple values of a nested DTO, prefer the
   * builder-consumer helper {@link #classJavadoc(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setClassJavadoc(JavadocDto)
   * setClassJavadoc(JavadocDto classJavadoc)}
   *
   * @param classJavadocUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>classJavadoc</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder classJavadocUpdate(
      UnaryOperator<JavadocDto> classJavadocUpdater) {
    if (!this.classJavadoc.isSet()) {
      throw new IllegalStateException("Cannot update 'classJavadoc' before it is set");
    }
    this.classJavadoc = changedValue(classJavadocUpdater.apply(this.classJavadoc.value()));
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
   * Sets the value for <code>constructors</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setConstructors(List)
   * setConstructors(List<ConstructorDto> constructors)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructors(ConstructorDtoBuilder.create().build(), ConstructorDtoBuilder.create().build());
   * }</pre>
   *
   * @param constructors the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder constructors(ConstructorDto... constructors) {
    this.constructors = changedValue(List.of(constructors));
    return this;
  }

  /**
   * Sets the value for <code>constructors</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setConstructors(List)
   * setConstructors(List<ConstructorDto> constructors)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructors(t -> t.add(constructorDtoBuilder -> constructorDtoBuilder));
   * }</pre>
   *
   * @param constructorsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder constructors(
      Consumer<ArrayListBuilderWithElementBuilders<ConstructorDto, ConstructorDtoBuilder>>
          constructorsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<ConstructorDto, ConstructorDtoBuilder> builder =
        this.constructors.isSet()
            ? new ArrayListBuilderWithElementBuilders<ConstructorDto, ConstructorDtoBuilder>(
                this.constructors.value(), ConstructorDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<ConstructorDto, ConstructorDtoBuilder>(
                ConstructorDtoBuilder::create);
    constructorsBuilderConsumer.accept(builder);
    this.constructors = changedValue(builder.build());
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
   * Updates the current value of <code>constructors</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #constructors(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setConstructors(List)
   * setConstructors(List<ConstructorDto> constructors)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.constructors(List.of(ConstructorDtoBuilder.create().build()))
   *     .constructorsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param constructorsUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>constructors</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder constructorsUpdate(
      UnaryOperator<List<ConstructorDto>> constructorsUpdater) {
    if (!this.constructors.isSet()) {
      throw new IllegalStateException("Cannot update 'constructors' before it is set");
    }
    this.constructors = changedValue(constructorsUpdater.apply(this.constructors.value()));
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
   * Updates the current value of <code>formattingMode</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance).
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setFormattingMode(FormattingMode)
   * setFormattingMode(FormattingMode formattingMode)}
   *
   * @param formattingModeUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>formattingMode</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder formattingModeUpdate(
      UnaryOperator<FormattingMode> formattingModeUpdater) {
    if (!this.formattingMode.isSet()) {
      throw new IllegalStateException("Cannot update 'formattingMode' before it is set");
    }
    this.formattingMode = changedValue(formattingModeUpdater.apply(this.formattingMode.value()));
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
   * Sets the value for <code>generics</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setGenerics(List)
   * setGenerics(List<GenericParameterDto> generics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.generics(GenericParameterDtoBuilder.create().build(), GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param generics the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder generics(GenericParameterDto... generics) {
    this.generics = changedValue(List.of(generics));
    return this;
  }

  /**
   * Sets the value for <code>generics</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setGenerics(List)
   * setGenerics(List<GenericParameterDto> generics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.generics(t -> t.add(genericParameterDtoBuilder -> genericParameterDtoBuilder));
   * }</pre>
   *
   * @param genericsBuilderConsumer consumer providing an instance of a builder for the replacement
   *     collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder generics(
      Consumer<ArrayListBuilderWithElementBuilders<GenericParameterDto, GenericParameterDtoBuilder>>
          genericsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<GenericParameterDto, GenericParameterDtoBuilder> builder =
        this.generics.isSet()
            ? new ArrayListBuilderWithElementBuilders<
                GenericParameterDto, GenericParameterDtoBuilder>(
                this.generics.value(), GenericParameterDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<
                GenericParameterDto, GenericParameterDtoBuilder>(
                GenericParameterDtoBuilder::create);
    genericsBuilderConsumer.accept(builder);
    this.generics = changedValue(builder.build());
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
   * Updates the current value of <code>generics</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #generics(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setGenerics(List)
   * setGenerics(List<GenericParameterDto> generics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.generics(List.of(GenericParameterDtoBuilder.create().build()))
   *     .genericsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param genericsUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>generics</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder genericsUpdate(
      UnaryOperator<List<GenericParameterDto>> genericsUpdater) {
    if (!this.generics.isSet()) {
      throw new IllegalStateException("Cannot update 'generics' before it is set");
    }
    this.generics = changedValue(genericsUpdater.apply(this.generics.value()));
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
   * Sets the value for <code>imports</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * @param imports the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder imports(ImportStatement... imports) {
    this.imports = changedValue(Set.of(imports));
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
   * Updates the current value of <code>imports</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setImports(Set)
   * setImports(Set<ImportStatement> imports)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.importsUpdate(TreeSet::new);
   * }</pre>
   *
   * @param importsUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>imports</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder importsUpdate(
      UnaryOperator<Set<ImportStatement>> importsUpdater) {
    if (!this.imports.isSet()) {
      throw new IllegalStateException("Cannot update 'imports' before it is set");
    }
    this.imports = changedValue(importsUpdater.apply(this.imports.value()));
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
    this.interfaces = changedValue(Set.of(interfaces));
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
   * Updates the current value of <code>interfaces</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setInterfaces(Set)
   * setInterfaces(Set<InterfaceName> interfaces)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.interfacesUpdate(TreeSet::new);
   * }</pre>
   *
   * @param interfacesUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>interfaces</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder interfacesUpdate(
      UnaryOperator<Set<InterfaceName>> interfacesUpdater) {
    if (!this.interfaces.isSet()) {
      throw new IllegalStateException("Cannot update 'interfaces' before it is set");
    }
    this.interfaces = changedValue(interfacesUpdater.apply(this.interfaces.value()));
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
   * Sets the value for <code>methods</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setMethods(List)
   * setMethods(List<MethodDto> methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(MethodDtoBuilder.create().build(), MethodDtoBuilder.create().build());
   * }</pre>
   *
   * @param methods the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder methods(MethodDto... methods) {
    this.methods = changedValue(List.of(methods));
    return this;
  }

  /**
   * Sets the value for <code>methods</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setMethods(List)
   * setMethods(List<MethodDto> methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(t -> t.add(methodDtoBuilder -> methodDtoBuilder));
   * }</pre>
   *
   * @param methodsBuilderConsumer consumer providing an instance of a builder for the replacement
   *     collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder methods(
      Consumer<ArrayListBuilderWithElementBuilders<MethodDto, MethodDtoBuilder>>
          methodsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<MethodDto, MethodDtoBuilder> builder =
        this.methods.isSet()
            ? new ArrayListBuilderWithElementBuilders<MethodDto, MethodDtoBuilder>(
                this.methods.value(), MethodDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<MethodDto, MethodDtoBuilder>(
                MethodDtoBuilder::create);
    methodsBuilderConsumer.accept(builder);
    this.methods = changedValue(builder.build());
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
   * Updates the current value of <code>methods</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #methods(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setMethods(List)
   * setMethods(List<MethodDto> methods)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.methods(List.of(MethodDtoBuilder.create().build())).methodsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param methodsUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>methods</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder methodsUpdate(
      UnaryOperator<List<MethodDto>> methodsUpdater) {
    if (!this.methods.isSet()) {
      throw new IllegalStateException("Cannot update 'methods' before it is set");
    }
    this.methods = changedValue(methodsUpdater.apply(this.methods.value()));
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
   * Sets the value for <code>nestedTypes</code>.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setNestedTypes(List)
   * setNestedTypes(List<NestedTypeDto> nestedTypes)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nestedTypes(NestedTypeDtoBuilder.create().build(), NestedTypeDtoBuilder.create().build());
   * }</pre>
   *
   * @param nestedTypes the replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder nestedTypes(NestedTypeDto... nestedTypes) {
    this.nestedTypes = changedValue(List.of(nestedTypes));
    return this;
  }

  /**
   * Sets the value for <code>nestedTypes</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setNestedTypes(List)
   * setNestedTypes(List<NestedTypeDto> nestedTypes)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nestedTypes(t -> t.add(nestedTypeDtoBuilder -> nestedTypeDtoBuilder));
   * }</pre>
   *
   * @param nestedTypesBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public GenerationTargetClassDtoBuilder nestedTypes(
      Consumer<ArrayListBuilderWithElementBuilders<NestedTypeDto, NestedTypeDtoBuilder>>
          nestedTypesBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<NestedTypeDto, NestedTypeDtoBuilder> builder =
        this.nestedTypes.isSet()
            ? new ArrayListBuilderWithElementBuilders<NestedTypeDto, NestedTypeDtoBuilder>(
                this.nestedTypes.value(), NestedTypeDtoBuilder::create)
            : new ArrayListBuilderWithElementBuilders<NestedTypeDto, NestedTypeDtoBuilder>(
                NestedTypeDtoBuilder::create);
    nestedTypesBuilderConsumer.accept(builder);
    this.nestedTypes = changedValue(builder.build());
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
   * Updates the current value of <code>nestedTypes</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple elements of a nested DTO, prefer the
   * builder-consumer helper {@link #nestedTypes(Consumer)}.
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setNestedTypes(List)
   * setNestedTypes(List<NestedTypeDto> nestedTypes)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nestedTypes(List.of(NestedTypeDtoBuilder.create().build()))
   *     .nestedTypesUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param nestedTypesUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>nestedTypes</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder nestedTypesUpdate(
      UnaryOperator<List<NestedTypeDto>> nestedTypesUpdater) {
    if (!this.nestedTypes.isSet()) {
      throw new IllegalStateException("Cannot update 'nestedTypes' before it is set");
    }
    this.nestedTypes = changedValue(nestedTypesUpdater.apply(this.nestedTypes.value()));
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
   * Updates the current value of <code>superType</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setSuperType(TypeName)
   * setSuperType(TypeName superType)}
   *
   * @param superTypeUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>superType</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder superTypeUpdate(UnaryOperator<TypeName> superTypeUpdater) {
    if (!this.superType.isSet()) {
      throw new IllegalStateException("Cannot update 'superType' before it is set");
    }
    this.superType = changedValue(superTypeUpdater.apply(this.superType.value()));
    return this;
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
   * Updates the current value of <code>typeName</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link GenerationTargetClassDto#setTypeName(TypeName)
   * setTypeName(TypeName typeName)}
   *
   * @param typeNameUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>typeName</code> has not been set yet
   */
  public GenerationTargetClassDtoBuilder typeNameUpdate(UnaryOperator<TypeName> typeNameUpdater) {
    if (!this.typeName.isSet()) {
      throw new IllegalStateException("Cannot update 'typeName' before it is set");
    }
    this.typeName = changedValue(typeNameUpdater.apply(this.typeName.value()));
    return this;
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
