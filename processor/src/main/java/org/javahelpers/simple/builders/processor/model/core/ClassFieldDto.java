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

package org.javahelpers.simple.builders.processor.model.core;

import java.util.LinkedHashSet;
import java.util.Set;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Represents a field declaration in the generated class.
 *
 * <p>This DTO contains only rendering information for a field. It is created from {@code FieldDto}
 * during finalization in {@code BuilderDefinitionCreator}.
 */
public class ClassFieldDto {
  /** Field name in generated class. */
  private String fieldName;

  /** Field type (e.g., TrackedValue<String> represented as TypeName). */
  private TypeName fieldType;

  /** Field visibility (typically PRIVATE). */
  private AccessModifier visibility;

  /** Literal initializer (e.g., "unsetValue()"). */
  private String literalInitializer;

  /** Field javadoc. */
  private JavadocDto javadoc;

  /** Types to import for this field (e.g., TrackedValue, the boxed inner type). */
  private final Set<TypeName> fieldTypeImports = new LinkedHashSet<>();

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public TypeName getFieldType() {
    return fieldType;
  }

  public void setFieldType(TypeName fieldType) {
    this.fieldType = fieldType;
  }

  public AccessModifier getVisibility() {
    return visibility;
  }

  public void setVisibility(AccessModifier visibility) {
    this.visibility = visibility;
  }

  public String getLiteralInitializer() {
    return literalInitializer;
  }

  public void setLiteralInitializer(String literalInitializer) {
    this.literalInitializer = literalInitializer;
  }

  public JavadocDto getJavadoc() {
    return javadoc;
  }

  public void setJavadoc(JavadocDto javadoc) {
    this.javadoc = javadoc;
  }

  public Set<TypeName> getFieldTypeImports() {
    return fieldTypeImports;
  }

  public void addFieldTypeImport(TypeName typeName) {
    this.fieldTypeImports.add(typeName);
  }
}
