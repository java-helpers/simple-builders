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

package org.javahelpers.simple.builders.processor.dtos;

import java.util.ArrayList;
import java.util.List;

/** DTO for holding information of code implementation. */
public class MethodCodeDto {
  /** Format of code. Holding placeholder for dynamic values. */
  private String codeFormat;

  /** List of placeholders in CodeFormat. Containing dynamic values too. */
  private final List<MethodCodePlaceholder<?>> codeArguments = new ArrayList<>();

  /**
   * Setting format of code.
   *
   * @param codeFormat Codeformat
   */
  public void setCodeFormat(String codeFormat) {
    this.codeFormat = codeFormat;
  }

  /**
   * Adding an argument for Codeformat. Helperfunction to set text value.
   *
   * @param name name in codeformat
   * @param value value to fill in codeformat
   */
  public void addArgument(String name, String value) {
    codeArguments.add(new MethodCodeStringPlaceholder(name, value));
  }

  /**
   * Adding an argument for Codeformat. Helperfunction to set TypeName value.
   *
   * @param name name in codeformat
   * @param value value to fill in codeformat
   */
  public void addArgument(String name, TypeName value) {
    codeArguments.add(new MethodCodeTypePlaceholder(name, value));
  }

  /**
   * Getter for Codeformat.
   *
   * @return codeformat
   */
  public String getCodeFormat() {
    return codeFormat;
  }

  /**
   * Getter for arguments in Codeformat.
   *
   * @return argument values
   */
  public List<MethodCodePlaceholder<?>> getCodeArguments() {
    return codeArguments;
  }
}
