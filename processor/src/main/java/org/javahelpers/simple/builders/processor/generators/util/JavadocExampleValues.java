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

package org.javahelpers.simple.builders.processor.generators.util;

import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive.PrimitiveTypeEnum;

/**
 * Provides default example values for Javadoc code examples.
 *
 * <p>This class provides sensible default values for common types to be used in generated Javadoc
 * code examples. These values are designed to be realistic yet generic enough to work in most
 * contexts.
 */
public final class JavadocExampleValues {

  private static final String STRING_EXAMPLE = "\"example value\"";
  private static final String INT_EXAMPLE = "42";
  private static final String LONG_EXAMPLE = "42L";
  private static final String DOUBLE_EXAMPLE = "3.14";
  private static final String FLOAT_EXAMPLE = "3.14f";
  private static final String BOOLEAN_EXAMPLE = "true";
  private static final String CHAR_EXAMPLE = "'x'";

  /** Map of primitive type enums to their example values. */
  private static final Map<PrimitiveTypeEnum, String> PRIMITIVE_EXAMPLES =
      Map.of(
          PrimitiveTypeEnum.INT, INT_EXAMPLE,
          PrimitiveTypeEnum.LONG, LONG_EXAMPLE,
          PrimitiveTypeEnum.DOUBLE, DOUBLE_EXAMPLE,
          PrimitiveTypeEnum.FLOAT, FLOAT_EXAMPLE,
          PrimitiveTypeEnum.BOOLEAN, BOOLEAN_EXAMPLE,
          PrimitiveTypeEnum.CHAR, CHAR_EXAMPLE);

  private JavadocExampleValues() {
    // Utility class - prevent instantiation
  }

  /**
   * Returns an example value for the given type name, if available.
   *
   * <p>This method returns example values for:
   *
   * <ul>
   *   <li>Primitive types (int, long, double, float, boolean, char)
   *   <li>String type (returns {@value STRING_EXAMPLE})
   * </ul>
   *
   * @param typeName the type name to get an example value for
   * @return an Optional containing the example value, or empty if no example is available
   */
  public static Optional<String> getExampleValue(TypeName typeName) {
    if (typeName instanceof TypeNamePrimitive primitive) {
      return Optional.ofNullable(PRIMITIVE_EXAMPLES.get(primitive.getType()));
    }
    // Check for String type
    if ("java.lang.String".equals(typeName.getFullQualifiedName())
        || "String".equals(typeName.getClassName())) {
      return Optional.of(STRING_EXAMPLE);
    }
    return Optional.empty();
  }
}
