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
 *   <li><b>Field Setters:</b> generateFieldSupplier, generateFieldConsumer, generateBuilderConsumer
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
     * field.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto person = PersonDtoBuilder.create()
     *     .name(() -> fetchNameFromDatabase())
     *     .age(() -> calculateAge())
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateFieldSupplier
     */
    OptionState generateFieldSupplier() default OptionState.UNSET;

    /**
     * Generate a consumer method with parameter-type {@code Consumer<T>} with T being the type of
     * the field. <br>
     * This is only done for complex field types, so that users could use setter to change the
     * properties of that parameter.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto person = PersonDtoBuilder.create()
     *     .address(addr -> {
     *         addr.setStreet("Main St");
     *         addr.setCity("Berlin");
     *     })
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateFieldConsumer
     */
    OptionState generateFieldConsumer() default OptionState.UNSET;

    /**
     * Generate a builder consumer method with parameter-type {@code Consumer<Builder<T>>} with T
     * being the type of the field <br>
     * This is only done for complex field types, which have a recognized builder so that users
     * could use the chained builder methods to set the value of this complex field.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto person = PersonDtoBuilder.create()
     *     .address(ab -> ab
     *         .street("Main St")
     *         .city("Berlin")
     *         .zipCode("10115"))
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateBuilderConsumer
     */
    OptionState generateBuilderConsumer() default OptionState.UNSET;

    /**
     * Generate conditional logic method (conditional) <br>
     * Allows conditional execution of builder methods based on a boolean supplier.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto person = PersonDtoBuilder.create()
     *     .name("John")
     *     .conditional(() -> includeEmail, b -> b.email("john@example.com"))
     *     .conditional(() -> isPremium, b -> b.memberLevel("GOLD"))
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateConditionalHelper
     */
    OptionState generateConditionalHelper() default OptionState.UNSET;

    // === Access Control ===
    /**
     * Access level for generated builder class.
     *
     * <p>Available values:
     *
     * <ul>
     *   <li><b>PUBLIC</b> - For public APIs (default)
     *   <li><b>PACKAGE_PRIVATE</b> - For internal use within a package
     *   <li><b>PRIVATE</b> - When using only static factory methods
     * </ul>
     *
     * <p>Example:
     *
     * <pre>{@code
     * @SimpleBuilder(options = @SimpleBuilder.Options(
     *     builderAccess = AccessModifier.PACKAGE_PRIVATE
     * ))
     * public class PersonDto {
     *     // Generates: class PersonDtoBuilder (package-private)
     * }
     * }</pre>
     *
     * <p>Default: {@link AccessModifier#PUBLIC PUBLIC}
     *
     * <p>Compiler option: -Asimplebuilder.builderAccess (values: PUBLIC, PACKAGE_PRIVATE, PRIVATE)
     */
    AccessModifier builderAccess() default AccessModifier.PUBLIC;

    /**
     * Access level for generated builder constructors.
     *
     * <p>Common pattern: Use PRIVATE constructors with PUBLIC static factory methods (create()).
     *
     * <p>Example:
     *
     * <pre>{@code
     * @SimpleBuilder(options = @SimpleBuilder.Options(
     *     builderConstructorAccess = AccessModifier.PRIVATE
     * ))
     * public class PersonDto {
     *     // Generates: private PersonDtoBuilder() and private PersonDtoBuilder(PersonDto)
     *     // Use via: PersonDtoBuilder.create() or PersonDtoBuilder.from(instance)
     * }
     * }</pre>
     *
     * <p>Default: {@link AccessModifier#PUBLIC PUBLIC}
     *
     * <p>Compiler option: -Asimplebuilder.builderConstructorAccess (values: PUBLIC,
     * PACKAGE_PRIVATE, PRIVATE)
     */
    AccessModifier builderConstructorAccess() default AccessModifier.PUBLIC;

    /**
     * Access level for generated builder methods.
     *
     * <p>Typically matches builder class access. Use PACKAGE_PRIVATE for internal APIs.
     *
     * <p>Example:
     *
     * <pre>{@code
     * @SimpleBuilder(options = @SimpleBuilder.Options(
     *     methodAccess = AccessModifier.PACKAGE_PRIVATE
     * ))
     * public class PersonDto {
     *     // Generates: PersonDtoBuilder name(String name) (package-private)
     * }
     * }</pre>
     *
     * <p>Default: {@link AccessModifier#PUBLIC PUBLIC}
     *
     * <p>Compiler option: -Asimplebuilder.methodAccess (values: PUBLIC, PACKAGE_PRIVATE, PRIVATE)
     */
    AccessModifier methodAccess() default AccessModifier.PUBLIC;

    // === Collection Options ===
    /**
     * Generate helper methods with VarArgs for Lists and Sets. <br>
     * Allows passing multiple elements directly instead of creating a list/set.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto person = PersonDtoBuilder.create()
     *     .hobbies("Reading", "Gaming", "Cooking") // VarArgs instead of List.of(...)
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateVarArgsHelpers
     */
    OptionState generateVarArgsHelpers() default OptionState.UNSET;

    /**
     * Generate String format helper methods for String fields. <br>
     * Allows using String.format() style for setting string values.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto person = PersonDtoBuilder.create()
     *     .name("Hello %s %s", firstName, lastName)
     *     .description("Age: %d, City: %s", age, city)
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateStringFormatHelpers
     */
    OptionState generateStringFormatHelpers() default OptionState.UNSET;

    /**
     * Generate unboxed optional methods that accept the inner type T directly instead of
     * Optional&lt;T&gt;. <br>
     * For Optional fields, this generates a setter that accepts T and wraps it with
     * Optional.ofNullable().
     *
     * <p>Example:
     *
     * <pre>{@code
     * // Field: Optional<String> email
     * PersonDto person = PersonDtoBuilder.create()
     *     .email("john@example.com") // String instead of Optional.of("john@example.com")
     *     .build();
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateUnboxedOptional
     */
    OptionState generateUnboxedOptional() default OptionState.UNSET;

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
    OptionState usingArrayListBuilder() default OptionState.UNSET;

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
    OptionState usingArrayListBuilderWithElementBuilders() default OptionState.UNSET;

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
    OptionState usingHashSetBuilder() default OptionState.UNSET;

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
    OptionState usingHashSetBuilderWithElementBuilders() default OptionState.UNSET;

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
    OptionState usingHashMapBuilder() default OptionState.UNSET;

    // === Annotations ===
    /**
     * Use {@code @Generated} annotation on the generated builder class. <br>
     * Marks the builder as generated code for tooling and analysis.
     *
     * <p>Example:
     *
     * <pre>{@code
     * @Generated("org.javahelpers.simple.builders.processor.BuilderProcessor")
     * public class PersonDtoBuilder {
     *     // ...
     * }
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.usingGeneratedAnnotation
     */
    OptionState usingGeneratedAnnotation() default OptionState.UNSET;

    /**
     * Use {@code @BuilderImplementation} annotation on the generated builder class. <br>
     * Links the generated builder back to the original DTO class.
     *
     * <p>Example:
     *
     * <pre>{@code
     * @BuilderImplementation(PersonDto.class)
     * public class PersonDtoBuilder {
     *     // ...
     * }
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.usingBuilderImplementationAnnotation
     */
    OptionState usingBuilderImplementationAnnotation() default OptionState.UNSET;

    // === Integration ===
    /**
     * Implement {@code IBuilderBase} interface in the generated builder class. <br>
     * Provides a common base interface for all generated builders.
     *
     * <p>Example:
     *
     * <pre>{@code
     * public class PersonDtoBuilder implements IBuilderBase<PersonDto> {
     *     @Override
     *     public PersonDto build() {
     *         // ...
     *     }
     * }
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.implementsBuilderBase
     */
    OptionState implementsBuilderBase() default OptionState.UNSET;

    /**
     * Generate With interface for integrating builder into DTOs. <br>
     * Creates a nested interface that can be implemented by the DTO for fluent updates.
     *
     * <p>Example:
     *
     * <pre>{@code
     * PersonDto updated = person.with(b -> b
     *     .name("New Name")
     *     .age(30));
     * }
     *
     * // Generated:
     * public interface WithPersonDto {
     *     default PersonDto with(Consumer<PersonDtoBuilder> updater) { ... }
     * }
     * }</pre>
     *
     * Default: ENABLED <br>
     * Compiler option: -Asimplebuilder.generateWithInterface
     */
    OptionState generateWithInterface() default OptionState.UNSET;

    // === Naming ===
    /**
     * Suffix to append to the DTO name to generate the builder class name. <br>
     * For example, with suffix "Builder", a DTO named "PersonDto" will generate "PersonDtoBuilder".
     *
     * <p>Example:
     *
     * <pre>{@code
     * @SimpleBuilder(options = @SimpleBuilder.Options(
     *     builderSuffix = "Factory"
     * ))
     * public class PersonDto {
     *     // Generates: PersonDtoFactory instead of PersonDtoBuilder
     * }
     * }</pre>
     *
     * Default: "Builder" <br>
     * Compiler option: -Asimplebuilder.builderSuffix
     */
    String builderSuffix() default "Builder";

    /**
     * Suffix to append to setter method names in the generated builder. <br>
     * For example, with suffix "with", a field named "name" will generate "withName()". <br>
     * When a suffix is set, the field name is capitalized after the suffix.
     *
     * <p>Example:
     *
     * <pre>{@code
     * @SimpleBuilder(options = @SimpleBuilder.Options(
     *     setterSuffix = "with"
     * ))
     * public class PersonDto {
     *     // Generates: withName(String) instead of name(String)
     * }
     *
     * PersonDto person = PersonDtoBuilder.create()
     *     .withName("John")
     *     .withAge(25)
     *     .build();
     * }</pre>
     *
     * Default: "" (empty - no suffix) <br>
     * Compiler option: -Asimplebuilder.setterSuffix
     */
    String setterSuffix() default "";
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
   *     generateFieldSupplier = true,
   *     generateFieldConsumer = true
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
