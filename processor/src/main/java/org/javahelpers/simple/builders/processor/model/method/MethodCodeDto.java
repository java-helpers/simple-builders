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

package org.javahelpers.simple.builders.processor.model.method;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.model.imports.ImportStatement;
import org.javahelpers.simple.builders.processor.model.imports.RegularImport;
import org.javahelpers.simple.builders.processor.model.imports.StaticImport;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;

/** DTO for holding information of code implementation. */
public class MethodCodeDto {
  /** Format of code. Holding placeholder for dynamic values. */
  private String codeFormat;

  /** List of placeholders in CodeFormat. Containing dynamic values too. */
  private final List<MethodCodePlaceholder<?>> codeArguments = new ArrayList<>();

  /** Types used in the code body that aren't covered by arguments. */
  private final Set<ImportStatement> codeBlockImports = new LinkedHashSet<>();

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
    codeBlockImports.add(new RegularImport(value));
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
  @SuppressWarnings("java:S1452")
  public List<MethodCodePlaceholder<?>> getCodeArguments() {
    return codeArguments;
  }

  /**
   * Checks if this code DTO has code content.
   *
   * @return true if code format is not null and not blank
   */
  public boolean hasCode() {
    return !StringUtils.isBlank(codeFormat);
  }

  /**
   * Returns the set of imports used in the code body that aren't covered by arguments.
   *
   * @return set of imports used in code body
   */
  public Set<ImportStatement> getCodeBlockImports() {
    return codeBlockImports;
  }

  /**
   * Adding an import for a type used in the code block.
   *
   * @param typeName type to import
   */
  public void addCodeBlockImport(TypeName typeName) {
    this.codeBlockImports.add(new RegularImport(typeName));
  }

  /**
   * Adds a regular import for a type used in the code block (convenience method accepting Class).
   *
   * @param clazz the class to import
   */
  public void addImport(Class<?> clazz) {
    addCodeBlockImport(TypeName.of(clazz));
  }

  /**
   * Adds a static import for a method/field used in the code block (convenience method).
   *
   * @param clazz the class containing the static member
   * @param memberName the name of the static member
   */
  public void addStaticImport(Class<?> clazz, String memberName) {
    this.codeBlockImports.add(new StaticImport(TypeName.of(clazz), memberName));
  }

  /**
   * Adds type imports for a TypeName and its generic type arguments (convenience method).
   *
   * @param type the type to add imports for
   */
  public void addTypeImports(TypeName type) {
    if (type == null) {
      return;
    }

    // Add the main type
    addCodeBlockImport(type);

    // Add generic type arguments recursively
    if (type instanceof TypeNameGeneric genericType) {
      genericType.getInnerTypeArguments().forEach(this::addTypeImports);
    }
  }

  /**
   * Appends additional code to the existing code format with string formatting support.
   *
   * <p>This method concatenates the provided formatted code string to the current code format,
   * separated by a newline for proper formatting. This is useful for building up method bodies
   * incrementally, especially when constructing complex code with multiple sections.
   *
   * <p>Supports the same formatting syntax as {@link String#format(String, Object...)}, allowing
   * for dynamic value insertion using placeholders like %s, %d, etc.
   *
   * @param formatted the code fragment with format placeholders to append to the existing code
   *     format
   * @param args the arguments to be formatted into the string
   */
  public void append(String formatted, Object... args) {
    String formattedCode = args.length > 0 ? String.format(formatted, args) : formatted;
    if (StringUtils.isEmpty(codeFormat)) {
      codeFormat = formattedCode;
    } else {
      codeFormat += "\n" + formattedCode;
    }
  }
}
