/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate builders for types that cannot or should not be modified, such as classes
 * from third-party libraries.
 *
 * <p>Place this annotation on a dedicated holder class or on the package itself (in {@code
 * package-info.java}) and list the external types in {@link #value()}. For every listed type a
 * builder is generated following the same naming and generation conventions as for {@link
 * SimpleBuilder} annotated classes, without changing the target type and without runtime
 * reflection.
 *
 * <p>The generated builder is placed in the package of the class or package carrying this
 * annotation. Only members of the target type that are accessible from that package (e.g. public
 * constructors and setters, or package-visible members when the holder shares the target's package)
 * are used for builder generation. If no suitable construction mechanism is available, generation
 * fails with a compile-time error. The target type's own annotations are not consulted - the
 * explicit declaration wins, so even an {@link Ignore4BuilderGeneration} on the target does not
 * suppress generation.
 *
 * <p>Example, declared on the package in {@code package-info.java} (generates the builder into
 * {@code com.example}):
 *
 * <pre>{@code
 * @SimpleBuilderFor(ExternalUser.class)
 * package com.example;
 * }</pre>
 *
 * <p>or on a dedicated provider class:
 *
 * <pre>{@code
 * @SimpleBuilderFor(ExternalUser.class)
 * public class ExternalBuildersProvider {
 * }
 *
 * // Generated usage:
 * ExternalUser user = ExternalUserBuilder.create()
 *     .name("Ada")
 *     .email("ada@example.com")
 *     .build();
 * }</pre>
 *
 * <p>Multiple external types can be listed in a single annotation and {@link #options()} may be
 * omitted, in which case the compiler defaults apply:
 *
 * <pre>{@code
 * @SimpleBuilderFor({ExternalUser.class, ExternalOrder.class})
 * public class ExternalBuildersProvider {
 * }
 * }</pre>
 *
 * <p>Configuration uses the existing {@link SimpleBuilder.Options} model and may be overridden via
 * compiler options:
 *
 * <pre>{@code
 * @SimpleBuilderFor(
 *     value = ExternalUser.class,
 *     options = @SimpleBuilder.Options(
 *         builderSuffix = "Factory"
 *     )
 * )
 * public class ExternalBuildersProvider {
 * }
 * }</pre>
 *
 * @see SimpleBuilder
 * @see SimpleBuilder.Options
 * @see Ignore4BuilderGeneration
 */
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.CLASS)
public @interface SimpleBuilderFor {

  /**
   * The types for which builders are generated. Every listed type must be resolvable on the
   * classpath or in the current compilation and must be constructible through accessible Java APIs
   * (e.g. an accessible constructor). At least one type is required.
   *
   * @return the external types to generate builders for
   */
  Class<?>[] value();

  /**
   * Configuration options for the generated builders, reusing the {@link SimpleBuilder.Options}
   * model. When omitted, all members keep their {@code UNSET} default, so each option resolves as
   * documented for the corresponding {@link SimpleBuilder.Options} member — falling back to the
   * {@code -Asimplebuilder.*} compiler argument and then to the built-in default listed there.
   *
   * @return the configuration options, defaulting to all members {@code UNSET}
   */
  SimpleBuilder.Options options() default @SimpleBuilder.Options;
}
