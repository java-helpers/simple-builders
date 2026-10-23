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

package org.javahelpers.simple.builders.processor.model.type;

import java.util.Optional;

/**
 * A builder resolved for a referenced field or element type, together with the instantiation paths
 * generated code must use to call it.
 *
 * <p>Each component records the concrete instantiation the contract check selected - constructor
 * call or static factory call - so generation sites emit exactly what was resolved.
 *
 * @param typeName the builder type to reference
 * @param funcForEmptyBuilder how to obtain an empty builder instance (e.g. {@code new
 *     PersonDtoBuilder()} or {@code PersonDtoBuilder.create()})
 * @param funcForPrefilledBuilder how to obtain a builder prefilled with a value (e.g. {@code new
 *     PersonDtoBuilder(person)}), or empty when the builder offers no prefilled path; helpers
 *     needing one are then not generated
 * @param buildMethodName the name of the parameterless method returning the built value (e.g.
 *     {@code build})
 */
public record ResolvedBuilder(
    TypeName typeName,
    TypeInstantiation funcForEmptyBuilder,
    Optional<TypeInstantiation> funcForPrefilledBuilder,
    String buildMethodName) {

  /**
   * A resolution instantiated exclusively through constructors, building with {@code build()}.
   *
   * @param typeName the builder type to reference
   */
  public ResolvedBuilder(TypeName typeName) {
    this(
        typeName, new TypeInstantiation.ConstructorCall(), new TypeInstantiation.ConstructorCall());
  }

  /**
   * A resolution using {@code build} as terminal method name - the default for builders.
   *
   * @param typeName the builder type to reference
   * @param funcForEmptyBuilder how to obtain an empty builder instance
   * @param funcForPrefilledBuilder how to obtain a builder prefilled with a value
   */
  public ResolvedBuilder(
      TypeName typeName,
      TypeInstantiation funcForEmptyBuilder,
      TypeInstantiation funcForPrefilledBuilder) {
    this(typeName, funcForEmptyBuilder, funcForPrefilledBuilder, "build");
  }

  /**
   * A resolution with a non-optional prefilled instantiation path.
   *
   * @param typeName the builder type to reference
   * @param funcForEmptyBuilder how to obtain an empty builder instance
   * @param funcForPrefilledBuilder how to obtain a builder prefilled with a value
   * @param buildMethodName the name of the parameterless method returning the built value
   */
  public ResolvedBuilder(
      TypeName typeName,
      TypeInstantiation funcForEmptyBuilder,
      TypeInstantiation funcForPrefilledBuilder,
      String buildMethodName) {
    this(
        typeName,
        funcForEmptyBuilder,
        Optional.ofNullable(funcForPrefilledBuilder),
        buildMethodName);
  }

  /**
   * Renders the terminal call producing the built value: {@code
   * builderExpression.buildMethodName()}.
   *
   * @param builderExpression expression evaluating to the builder instance (e.g. {@code builder})
   * @return the build call code
   */
  public String buildCall(String builderExpression) {
    return builderExpression + "." + buildMethodName + "()";
  }
}
