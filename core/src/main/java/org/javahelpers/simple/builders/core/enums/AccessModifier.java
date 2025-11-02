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

package org.javahelpers.simple.builders.core.enums;

/**
 * Enum representing Java access modifiers for generated builder classes and methods.
 *
 * <p>This enum is used to control the visibility of generated builders and their methods through
 * the {@link org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Options} annotation.
 *
 * <p>Note: PROTECTED is intentionally not included as builders follow the Builder pattern, not
 * inheritance, and should not be extended.
 */
public enum AccessModifier {

  /** Default access */
  DEFAULT("public"),

  /** Public access - accessible from anywhere */
  PUBLIC("public"),

  /** Package-private access (default) - accessible only within the same package */
  PACKAGE_PRIVATE(""),

  /** Private access - accessible only within the same class */
  PRIVATE("private");

  private final String javaKeyword;

  AccessModifier(String javaKeyword) {
    this.javaKeyword = javaKeyword;
  }

  /**
   * Get the Java keyword for this access modifier.
   *
   * @return the Java keyword (e.g., "public", "private"), or empty string for package-private
   */
  public String getJavaKeyword() {
    return javaKeyword;
  }
}
