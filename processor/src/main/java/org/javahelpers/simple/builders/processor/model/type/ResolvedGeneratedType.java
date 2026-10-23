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
 * A generated type resolved for a referenced field or element type, together with the
 * instantiation paths generated code must use to call it.
 *
 * <p>Each component records the concrete instantiation the contract check selected - constructor
 * call or static factory call - so generation sites emit exactly what was resolved.
 *
 * @param typeName the generated type to reference
 * @param funcForEmptyInstance how to obtain an empty instance (e.g. {@code new B()} or {@code
 *     B.create()})
 * @param funcForPrefilledInstance how to obtain an instance prefilled with a value (e.g. {@code
 *     new B(value)} or {@code B.of(value)}), or empty when the type offers no prefilled path;
 *     helpers needing one are then not generated
 * @param resultMethodName the name of the parameterless method returning the product (e.g. {@code
 *     build})
 */
public record ResolvedGeneratedType(
    TypeName typeName,
    TypeInstantiation funcForEmptyInstance,
    Optional<TypeInstantiation> funcForPrefilledInstance,
    String resultMethodName) {

  /**
   * A resolution with a non-optional prefilled instantiation path.
   *
   * @param typeName the generated type to reference
   * @param funcForEmptyInstance how to obtain an empty instance
   * @param funcForPrefilledInstance how to obtain an instance prefilled with a value
   * @param resultMethodName the name of the parameterless method returning the product
   */
  public ResolvedGeneratedType(
      TypeName typeName,
      TypeInstantiation funcForEmptyInstance,
      TypeInstantiation funcForPrefilledInstance,
      String resultMethodName) {
    this(
        typeName,
        funcForEmptyInstance,
        Optional.ofNullable(funcForPrefilledInstance),
        resultMethodName);
  }

  /**
   * Renders the terminal call producing the product: {@code expression.resultMethodName()}.
   *
   * @param expression expression evaluating to the generated-type instance (e.g. {@code generated})
   * @return the terminal call code
   */
  public String resultCall(String expression) {
    return expression + "." + resultMethodName + "()";
  }
}
