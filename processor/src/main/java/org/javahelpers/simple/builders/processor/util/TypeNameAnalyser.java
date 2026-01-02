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
package org.javahelpers.simple.builders.processor.util;

import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;

/** Helperclass to extract insights from TypeName. */
public class TypeNameAnalyser {

  private static final String JAVA_UTIL_PACKAGE = "java.util";
  private static final String JAVA_LANG_PACKAGE = "java.lang";

  private TypeNameAnalyser() {}

  /**
   * Helper to check if the type is a java-base class. Check is done by comparing the package name.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if type is a java-base class
   */
  public static boolean isJavaClass(TypeName typeName) {
    return Strings.CI.equalsAny(
        typeName.getPackageName(), JAVA_LANG_PACKAGE, "java.time", JAVA_UTIL_PACKAGE);
  }

  /**
   * Helper to check if the type is a {@code java.util.Optional}.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if it is a {@code java.util.Optional}
   */
  public static boolean isOptional(TypeName typeName) {
    return Strings.CI.equals(typeName.getPackageName(), JAVA_UTIL_PACKAGE)
        && Strings.CI.equals(typeName.getClassName(), "Optional");
  }

  /**
   * Helper to check if the type is a {@code java.lang.String}.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if it is a {@code java.lang.String}
   */
  public static boolean isString(TypeName typeName) {
    return Strings.CI.equals(typeName.getPackageName(), JAVA_LANG_PACKAGE)
        && Strings.CI.equals(typeName.getClassName(), "String");
  }

  /**
   * Checks if the field type is Optional&lt;String&gt;.
   *
   * @param fieldType the type of the field
   * @return true if the type is Optional&lt;String&gt;, false otherwise
   */
  public static boolean isOptionalString(TypeName fieldType) {
    if (fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && isOptional(fieldType)
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1) {
      return isString(fieldTypeGeneric.getInnerTypeArguments().get(0));
    }
    return false;
  }

  /**
   * Checks if an Optional type is properly parameterized (not a raw type).
   *
   * <p>A parameterized Optional has exactly 1 type argument (the wrapped type). A raw Optional has
   * 0 type arguments.
   *
   * @param typeName Type to be validated
   * @return {@code true} if it's an Optional with exactly 1 type argument
   */
  public static boolean isParameterizedOptional(TypeName typeName) {
    return typeName instanceof TypeNameGeneric genericType
        && isOptional(typeName)
        && genericType.getInnerTypeArguments().size() == 1;
  }
}
