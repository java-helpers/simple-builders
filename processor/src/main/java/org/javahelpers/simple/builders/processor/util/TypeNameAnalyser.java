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

import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.TypeName;

/** Helperclass to extract insights from TypeName. */
public class TypeNameAnalyser {

  private TypeNameAnalyser() {}

  /**
   * Helper to check if the type is a {@code java.util.Map}.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if it is a {@code java.util.Map}
   */
  public static boolean isMap(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "Map");
  }

  /**
   * Helper to check if the type is a java-base class. Check is done by comparing the package name.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if type is a java-base class
   */
  public static boolean isJavaClass(TypeName typeName) {
    return StringUtils.equalsAny(typeName.getPackageName(), "java.lang", "java.time", "java.util");
  }

  /**
   * Helper to check if the type is a {@code java.util.Set}.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if it is a {@code java.util.Set}
   */
  public static boolean isSet(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "Set");
  }

  /**
   * Helper to check if the type is a {@code java.util.List}.
   *
   * @param typeName Type to be validated
   * @return {@code true}, if it is a {@code java.util.List}
   */
  public static boolean isList(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "List");
  }
}
