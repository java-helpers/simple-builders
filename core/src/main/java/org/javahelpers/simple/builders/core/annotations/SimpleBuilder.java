/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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

package org.javahelpers.simple.builders.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;

/**
 * Annotation to mark classes for builder generation.
 *
 * <p>Triggers generation of a fluent builder class with support for various patterns and helper
 * methods. Can be used standalone or combined with {@link Options} for fine-grained control.
 *
 * <p>Available configuration options:
 *
 * <ul>
 *   <li><b>Field Setters:</b> generateFieldSupplier, generateFieldProvider, generateBuilderProvider
 *       (all default: true)
 *   <li><b>Conditional Logic:</b> generateConditionalHelper (default: true)
 *   <li><b>Access Control:</b> builderAccess, methodAccess (default: PUBLIC)
 *   <li><b>Collection Helpers:</b> generateVarArgsHelpers, usingArrayListBuilder,
 *       usingArrayListBuilderWithElementBuilders, usingHashSetBuilder,
 *       usingHashSetBuilderWithElementBuilders, usingHashMapBuilder (all default: true)
 *   <li><b>Integration:</b> generateWithInterface (default: true)
 * </ul>
 *
 * <p>Use {@link Template} to create reusable configuration presets.
 *
 * @see Options
 * @see Template
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface SimpleBuilder {

  /**
   * Configuration options for builder generation.
   *
   * <p>Allows fine-grained control over what gets generated in the builder class. Can be used with
   * {@link SimpleBuilder} or as part of {@link Template}.
   *
   * <p>All options have sensible defaults and can be overridden via compiler options using {@code
   * -A} flag.
   */
  @Retention(RetentionPolicy.CLASS)
  @Target(ElementType.TYPE)
  @interface Options {
    // === Generation Options ===
    /**
     * Generate a supplier method by which the user of this builder could define a function, which
     * supplies the value for this field. <br>
     * The generated method has the parameter-type {@code Supplier<T>} with T being the type of the
     * field. Default: ENABLED Compiler option: -Asimplebuilder.generateFieldSupplier
     */
    OptionState generateFieldSupplier() default OptionState.DEFAULT;

    /**
     * Generate a provider method with parameter-type {@code Provider<T>} with T being the type of
     * the field. <br>
     * This is only done for complex field types, so that users could use setter to change the
     * properties of that parameter. Default: ENABLED Compiler option:
     * -Asimplebuilder.generateFieldProvider
     */
    OptionState generateFieldProvider() default OptionState.DEFAULT;

    /**
     * Generate a builder provider method with parameter-type {@code Provider<Builder<T>>} with T
     * being the type of the field <br>
     * This is only done for complex field types, which have a recognized builder so that users
     * could use the chained builder methods to set the value of this complex field. <br>
     * Default: ENABLED Compiler option: -Asimplebuilder.generateBuilderProvider
     */
    OptionState generateBuilderProvider() default OptionState.DEFAULT;

    /**
     * Generate conditional logic method (conditional) <br>
     * Default: ENABLED Compiler option: -Asimplebuilder.generateConditionalHelper
     */
    OptionState generateConditionalHelper() default OptionState.DEFAULT;

    // === Access Control ===
    /**
     * Access level for generated builder class.
     *
     * <p>Default: {@link AccessModifier#PUBLIC PUBLIC}
     *
     * <p>Compiler option: -Asimplebuilder.builderAccess (values: PUBLIC, PROTECTED,
     * PACKAGE_PRIVATE, PRIVATE)
     */
    AccessModifier builderAccess() default AccessModifier.PUBLIC;

    /**
     * Access level for generated builder methods.
     *
     * <p>Default: {@link AccessModifier#PUBLIC PUBLIC}
     *
     * <p>Compiler option: -Asimplebuilder.methodAccess (values: PUBLIC, PROTECTED, PACKAGE_PRIVATE,
     * PRIVATE)
     */
    AccessModifier methodAccess() default AccessModifier.PUBLIC;

    // === Collection Options ===
    /**
     * Generate helper methods with VarArgs for Lists and Sets. <br>
     * Default: ENABLED Compiler option: -Asimplebuilder.generateVarArgsHelpers
     */
    OptionState generateVarArgsHelpers() default OptionState.DEFAULT;

    /**
     * Generate helper methods with a ArrayListBuilder supplier for lists instead of simple
     * supplier, which would not allow to use in a chanined way: <br>
     * Example with ArrayListBuilder: <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mb -> mb.add("Max").add("Moritz"))
     *     .build();
     * }</pre>
     *
     * Instead of <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mitglieder -> {
     *         mitglieder.add("Max");
     *         mitglieder.add("Moritz");
     *     })
     *     .build();
     * }</pre>
     *
     * Default: ENABLED Compiler option: -Asimplebuilder.usingArrayListBuilder
     */
    OptionState usingArrayListBuilder() default OptionState.DEFAULT;

    /**
     * Generate helper methods with a ArrayListBuilderWithElementBuilders supplier for lists of
     * complex objects instead of simple supplier, which would not allow to use in a chanined way:
     * <br>
     * Example with ArrayListBuilderWithElementBuilders: <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mb -> mb
     *         .add(pb -> pb.name("Max").alter(20))
     *         .add(pb -> pb.name("Moritz").alter(22)))
     *     .build();
     * }</pre>
     *
     * Instead of <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mitglieder -> {
     *         mitglieder.add(new PersonDto("Max", 20));
     *         mitglieder.add(new PersonDto("Moritz", 22));
     *     })
     *     .build();
     * }</pre>
     *
     * Default: ENABLED Compiler option: -Asimplebuilder.usingArrayListBuilderWithElementBuilders
     */
    OptionState usingArrayListBuilderWithElementBuilders() default OptionState.DEFAULT;

    /**
     * Generate helper methods with a ArrayListBuilder supplier for lists instead of simple
     * supplier, which would not allow to use in a chanined way: <br>
     * Example with ArrayListBuilder: <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mb -> mb.add("Max").add("Moritz"))
     *     .build();
     * }</pre>
     *
     * Instead of <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mitglieder -> {
     *         mitglieder.add("Max");
     *         mitglieder.add("Moritz");
     *     })
     *     .build();
     * }</pre>
     *
     * Default: ENABLED Compiler option: -Asimplebuilder.usingHashSetBuilder
     */
    OptionState usingHashSetBuilder() default OptionState.DEFAULT;

    /**
     * Generate helper methods with a HashSetBuilderWithElementBuilders supplier for lists of
     * complex objects instead of simple supplier, which would not allow to use in a chanined way:
     * <br>
     * Example with HashSetBuilderWithElementBuilders: <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mb -> mb
     *         .add(pb -> pb.name("Max").alter(20))
     *         .add(pb -> pb.name("Moritz").alter(22)))
     *     .build();
     * }</pre>
     *
     * Instead of <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mitglieder -> {
     *         mitglieder.add(new PersonDto("Max", 20));
     *         mitglieder.add(new PersonDto("Moritz", 22));
     *     })
     *     .build();
     * }</pre>
     *
     * Default: ENABLED Compiler option: -Asimplebuilder.usingHashSetBuilderWithElementBuilders
     */
    OptionState usingHashSetBuilderWithElementBuilders() default OptionState.DEFAULT;

    /**
     * Generate helper methods with a HashMapBuilder supplier for maps instead of simple supplier,
     * which would not allow to use in a chanined way: <br>
     * Example with HashMapBuilder: <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mb -> mb.put(20, "Max").put(22, "Moritz"))
     *     .build();
     * }</pre>
     *
     * Instead of <br>
     *
     * <pre>{@code
     * MannschaftDto mannschaft = MannschaftDtoBuilder()
     *     .create()
     *     .mitglieder(mitglieder -> {
     *         mitglieder.put(20, "Max");
     *         mitglieder.put(22, "Moritz");
     *     })
     *     .build();
     * }</pre>
     *
     * Default: ENABLED Compiler option: -Asimplebuilder.usingHashMapBuilder
     */
    OptionState usingHashMapBuilder() default OptionState.DEFAULT;

    /**
     * Generate With interface for integrating builder into DTOs. <br>
     * Default: ENABLED Compiler option: -Asimplebuilder.generateWithInterface
     */
    OptionState generateWithInterface() default OptionState.DEFAULT;
  }

  /**
   * Meta-annotation for creating custom SimpleBuilder annotation templates.
   *
   * <p>This allows you to create custom annotations that pre-configure SimpleBuilder options. The
   * custom annotation itself will be treated as @SimpleBuilder by the processor and will
   * automatically apply the configured options.
   *
   * <p>Example:
   *
   * <pre>{@code
   * @SimpleBuilder.Template(options = @SimpleBuilder.Options(
   *     generateSupplier = true,
   *     generateProvider = true,
   *     generateToString = true
   * ))
   * @Retention(RetentionPolicy.CLASS)
   * @Target(ElementType.TYPE)
   * public @interface FullFeaturedBuilder {
   * }
   *
   * // Usage - just use the template annotation, no @SimpleBuilder needed
   * @FullFeaturedBuilder
   * public class PersonDto {
   *     private String name;
   * }
   * }</pre>
   */
  @Retention(RetentionPolicy.CLASS)
  @Target(ElementType.ANNOTATION_TYPE)
  @Inherited
  @interface Template {
    /**
     * The options to apply when this template is used.
     *
     * @return the builder configuration options
     */
    Options options();
  }
}
