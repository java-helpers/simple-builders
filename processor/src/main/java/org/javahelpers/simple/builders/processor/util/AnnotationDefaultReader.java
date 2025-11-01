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

import java.lang.reflect.Method;

/**
 * Utility class for reading default values from annotations via reflection.
 *
 * <p>This class provides methods to extract default values from annotation methods, which is useful
 * for maintaining a single source of truth for configuration defaults.
 */
public final class AnnotationDefaultReader {

  private AnnotationDefaultReader() {
    // Utility class - prevent instantiation
  }

  /**
   * Read a boolean default value from an annotation method.
   *
   * @param annotationClass The annotation class containing the method
   * @param methodName The annotation method name
   * @param fallback Fallback value if reflection fails
   * @return The default value from the annotation, or fallback if not found
   */
  public static boolean getBooleanDefault(
      Class<?> annotationClass, String methodName, boolean fallback) {
    try {
      Method method = annotationClass.getMethod(methodName);
      Object defaultValue = method.getDefaultValue();
      return defaultValue != null ? (Boolean) defaultValue : fallback;
    } catch (Exception e) {
      // Fallback to provided value if reflection fails
      return fallback;
    }
  }

  /**
   * Read an enum default value from an annotation method and convert to String.
   *
   * @param annotationClass The annotation class containing the method
   * @param methodName The annotation method name
   * @param fallback Fallback value if reflection fails
   * @return The enum name as String, or fallback if not found
   */
  public static String getEnumDefaultAsString(
      Class<?> annotationClass, String methodName, String fallback) {
    try {
      Method method = annotationClass.getMethod(methodName);
      Object defaultValue = method.getDefaultValue();
      return defaultValue != null ? ((Enum<?>) defaultValue).name() : fallback;
    } catch (Exception e) {
      // Fallback to provided value if reflection fails
      return fallback;
    }
  }

  /**
   * Read a String default value from an annotation method.
   *
   * @param annotationClass The annotation class containing the method
   * @param methodName The annotation method name
   * @param fallback Fallback value if reflection fails
   * @return The default value from the annotation, or fallback if not found
   */
  public static String getStringDefault(
      Class<?> annotationClass, String methodName, String fallback) {
    try {
      Method method = annotationClass.getMethod(methodName);
      Object defaultValue = method.getDefaultValue();
      return defaultValue != null ? (String) defaultValue : fallback;
    } catch (Exception e) {
      // Fallback to provided value if reflection fails
      return fallback;
    }
  }

  /**
   * Read an integer default value from an annotation method.
   *
   * @param annotationClass The annotation class containing the method
   * @param methodName The annotation method name
   * @param fallback Fallback value if reflection fails
   * @return The default value from the annotation, or fallback if not found
   */
  public static int getIntDefault(Class<?> annotationClass, String methodName, int fallback) {
    try {
      Method method = annotationClass.getMethod(methodName);
      Object defaultValue = method.getDefaultValue();
      return defaultValue != null ? (Integer) defaultValue : fallback;
    } catch (Exception e) {
      // Fallback to provided value if reflection fails
      return fallback;
    }
  }
}
