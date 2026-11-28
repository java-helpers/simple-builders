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

package org.javahelpers.codegen.model.javadoc;

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
import org.javahelpers.codegen.builders.ArrayListBuilder;
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.HashSetBuilder;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.imports.RegularImport;
import org.javahelpers.codegen.model.imports.StaticImport;
import org.javahelpers.codegen.model.method.MethodCodePlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeStringPlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.codegen.model.type.TypeName;

/**
 * Builder for {@code org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto with method chaining and validation.
 * Use the static {@code create()} method to obtain a new builder instance, configure the desired
 * properties using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * JavadocCodeBlockDto result = JavadocCodeBlockDtoBuilder.create()
 *     .codeFormat("example codeFormat")
 *     .codeFormat("Hello %s", "World")
 *     .codeFormat(() -> "example codeFormat")
 *     .codeFormat(sb -> sb.append("text"))
 *     .build();
 * }</pre>
 */
public class JavadocCodeBlockDtoBuilder implements IBuilderBase<JavadocCodeBlockDto> {

  /** Tracked value for <code>codeArguments</code>: the replacement collection (not null). */
  private TrackedValue<List<MethodCodePlaceholder<?>>> codeArguments = unsetValue();

  /** Tracked value for <code>codeBlockImports</code>: the replacement collection (not null). */
  private TrackedValue<Set<ImportStatement>> codeBlockImports = unsetValue();

  /** Tracked value for <code>codeFormat</code>: Codeformat. */
  private TrackedValue<String> codeFormat = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto}.
   */
  public JavadocCodeBlockDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto}
   * by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public JavadocCodeBlockDtoBuilder(JavadocCodeBlockDto instance) {
    this.codeArguments = initialValue(instance.getCodeArguments());
    this.codeBlockImports = initialValue(instance.getCodeBlockImports());
    this.codeFormat = initialValue(instance.getCodeFormat());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * JavadocCodeBlockDtoBuilder builder = JavadocCodeBlockDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto}
   */
  public static JavadocCodeBlockDtoBuilder create() {
    return new JavadocCodeBlockDtoBuilder();
  }

  /**
   * Adds a single element to <code>codeArguments</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeArguments(List)
   * setCodeArguments(List<MethodCodePlaceholder<?>> codeArguments)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder add2CodeArguments(MethodCodePlaceholder<?> element) {
    List<MethodCodePlaceholder<?>> newCollection;
    if (this.codeArguments.isSet()) {
      newCollection = new ArrayList<>(this.codeArguments.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.codeArguments = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>codeArguments</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeArguments(List)
   * setCodeArguments(List<MethodCodePlaceholder<?>> codeArguments)}
   *
   * @param codeArgumentsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeArguments(
      Consumer<ArrayListBuilder<MethodCodePlaceholder<?>>> codeArgumentsBuilderConsumer) {
    ArrayListBuilder<MethodCodePlaceholder<?>> builder =
        this.codeArguments.isSet()
            ? new ArrayListBuilder<MethodCodePlaceholder<?>>(this.codeArguments.value())
            : new ArrayListBuilder<MethodCodePlaceholder<?>>();
    codeArgumentsBuilderConsumer.accept(builder);
    this.codeArguments = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>codeArguments</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeArguments(List)
   * setCodeArguments(List<MethodCodePlaceholder<?>> codeArguments)}
   *
   * @param codeArgumentsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeArguments(
      Supplier<List<MethodCodePlaceholder<?>>> codeArgumentsSupplier) {
    this.codeArguments = changedValue(codeArgumentsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>codeArguments</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeArguments(List)
   * setCodeArguments(List<MethodCodePlaceholder<?>> codeArguments)}
   *
   * @param codeArguments the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeArguments(MethodCodePlaceholder<?>... codeArguments) {
    this.codeArguments = changedValue(List.of(codeArguments));
    return this;
  }

  /**
   * Sets the value for <code>codeArguments</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeArguments(List)
   * setCodeArguments(List<MethodCodePlaceholder<?>> codeArguments)}
   *
   * @param codeArguments the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeArguments(List<MethodCodePlaceholder<?>> codeArguments) {
    this.codeArguments = changedValue(codeArguments);
    return this;
  }

  /**
   * Adds a single element to <code>codeBlockImports</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeBlockImports(Set)
   * setCodeBlockImports(Set<ImportStatement> codeBlockImports)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder add2CodeBlockImports(ImportStatement element) {
    Set<ImportStatement> newCollection;
    if (this.codeBlockImports.isSet()) {
      newCollection = new HashSet<>(this.codeBlockImports.value());
    } else {
      newCollection = new HashSet<>();
    }
    newCollection.add(element);
    this.codeBlockImports = changedValue(newCollection);
    return this;
  }

  /**
   * Adds a code block import for the given class.
   *
   * @param clazz class to import
   * @return this builder
   */
  public JavadocCodeBlockDtoBuilder add2CodeBlockImports(Class<?> clazz) {
    return add2CodeBlockImports(new RegularImport(TypeName.of(clazz)));
  }

  /**
   * Adds a code block import for the given type.
   *
   * @param typeName type to import
   * @return this builder
   */
  public JavadocCodeBlockDtoBuilder add2CodeBlockImports(TypeName typeName) {
    return add2CodeBlockImports(new RegularImport(typeName));
  }

  /**
   * Sets the value for <code>codeBlockImports</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeBlockImports(Set)
   * setCodeBlockImports(Set<ImportStatement> codeBlockImports)}
   *
   * @param codeBlockImports the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeBlockImports(ImportStatement... codeBlockImports) {
    this.codeBlockImports = changedValue(new LinkedHashSet<>(Arrays.asList(codeBlockImports)));
    return this;
  }

  /**
   * Sets the value for <code>codeBlockImports</code> using a builder consumer that produces the
   * value.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeBlockImports(Set)
   * setCodeBlockImports(Set<ImportStatement> codeBlockImports)}
   *
   * @param codeBlockImportsBuilderConsumer consumer providing an instance of a builder for the
   *     replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeBlockImports(
      Consumer<HashSetBuilder<ImportStatement>> codeBlockImportsBuilderConsumer) {
    HashSetBuilder<ImportStatement> builder =
        this.codeBlockImports.isSet()
            ? new HashSetBuilder<ImportStatement>(this.codeBlockImports.value())
            : new HashSetBuilder<ImportStatement>();
    codeBlockImportsBuilderConsumer.accept(builder);
    this.codeBlockImports = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>codeBlockImports</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeBlockImports(Set)
   * setCodeBlockImports(Set<ImportStatement> codeBlockImports)}
   *
   * @param codeBlockImportsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeBlockImports(
      Supplier<Set<ImportStatement>> codeBlockImportsSupplier) {
    this.codeBlockImports = changedValue(codeBlockImportsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>codeBlockImports</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeBlockImports(Set)
   * setCodeBlockImports(Set<ImportStatement> codeBlockImports)}
   *
   * @param codeBlockImports the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeBlockImports(Set<ImportStatement> codeBlockImports) {
    this.codeBlockImports = changedValue(codeBlockImports);
    return this;
  }

  /**
   * Sets the value for <code>codeFormat</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeFormat(String) setCodeFormat(String
   * codeFormat)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.codeFormat(sb -> sb.append("text"));
   * }</pre>
   *
   * @param codeFormatStringBuilderConsumer consumer providing an instance of Codeformat
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeFormat(
      Consumer<StringBuilder> codeFormatStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    codeFormatStringBuilderConsumer.accept(builder);
    this.codeFormat = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>codeFormat</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeFormat(String) setCodeFormat(String
   * codeFormat)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.codeFormat(() -> "example codeFormat");
   * }</pre>
   *
   * @param codeFormatSupplier supplier for Codeformat
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeFormat(Supplier<String> codeFormatSupplier) {
    this.codeFormat = changedValue(codeFormatSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>codeFormat</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeFormat(String) setCodeFormat(String
   * codeFormat)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.codeFormat("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeFormat(String format, Object... args) {
    this.codeFormat = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>codeFormat</code>.
   *
   * <p>Generated from setter {@link JavadocCodeBlockDto#setCodeFormat(String) setCodeFormat(String
   * codeFormat)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.codeFormat("example codeFormat");
   * }</pre>
   *
   * @param codeFormat Codeformat
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder codeFormat(String codeFormat) {
    this.codeFormat = changedValue(codeFormat);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public JavadocCodeBlockDtoBuilder conditional(
      BooleanSupplier condition, Consumer<JavadocCodeBlockDtoBuilder> yesCondition) {
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
  public JavadocCodeBlockDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<JavadocCodeBlockDtoBuilder> trueCase,
      Consumer<JavadocCodeBlockDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Adds a text placeholder argument for the code format (shortcut mirroring {@link
   * CodeTemplateDto#addArgument(String, String)}).
   *
   * @param name name in codeformat
   * @param value value to fill in codeformat
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder add2CodeArguments(String name, String value) {
    return add2CodeArguments(new MethodCodeStringPlaceholder(name, value));
  }

  /**
   * Adds a type placeholder argument for the code format; the type is registered as code-block
   * import (shortcut mirroring {@link CodeTemplateDto#addArgument(String, TypeName)}).
   *
   * @param name name in codeformat
   * @param value type value to fill in codeformat
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder add2CodeArguments(String name, TypeName value) {
    add2CodeBlockImports(value);
    return add2CodeArguments(new MethodCodeTypePlaceholder(name, value));
  }

  /**
   * Adds a static import for a method/field used in the code block (shortcut mirroring {@link
   * CodeTemplateDto#addStaticImport}).
   *
   * @param clazz the class containing the static member
   * @param memberName the name of the static member
   * @return current instance of builder
   */
  public JavadocCodeBlockDtoBuilder add2StaticImports(Class<?> clazz, String memberName) {
    return add2CodeBlockImports(new StaticImport(TypeName.of(clazz), memberName));
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * JavadocCodeBlockDto result = builder.build();
   * }</pre>
   */
  @Override
  public JavadocCodeBlockDto build() {
    JavadocCodeBlockDto result = new JavadocCodeBlockDto();
    this.codeArguments.ifSet(result::setCodeArguments);
    this.codeBlockImports.ifSet(result::setCodeBlockImports);
    this.codeFormat.ifSet(result::setCodeFormat);
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
        .append("codeArguments", this.codeArguments)
        .append("codeBlockImports", this.codeBlockImports)
        .append("codeFormat", this.codeFormat)
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
    default JavadocCodeBlockDto with(Consumer<JavadocCodeBlockDtoBuilder> b) {
      JavadocCodeBlockDtoBuilder builder;
      try {
        builder = new JavadocCodeBlockDtoBuilder(JavadocCodeBlockDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'JavadocCodeBlockDtoBuilder.With' should only be implemented by classes, which could be casted to 'JavadocCodeBlockDto'",
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
    default JavadocCodeBlockDtoBuilder with() {
      try {
        return new JavadocCodeBlockDtoBuilder(JavadocCodeBlockDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'JavadocCodeBlockDtoBuilder.With' should only be implemented by classes, which could be casted to 'JavadocCodeBlockDto'",
            ex);
      }
    }
  }
}
