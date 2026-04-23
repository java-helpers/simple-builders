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

import static org.javahelpers.simple.builders.core.enums.AccessModifier.PRIVATE;

import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.model.imports.ImportStatement;
import org.javahelpers.simple.builders.processor.model.imports.RegularImport;
import org.javahelpers.simple.builders.processor.model.imports.StaticImport;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;

/**
 * Represents a field declaration in the generated class.
 *
 * <p>This DTO contains only rendering information for a field. It is created from {@code FieldDto}
 * during finalization in {@code BuilderDefinitionCreator}.
 */
public class ClassFieldDto {
  /** Field name in generated class. */
  private String fieldName;

  /** Field type. */
  private TypeName fieldType;

  /** Field visibility. */
  private AccessModifier visibility = PRIVATE;

  /** Literal initializer (e.g., "exampleValue" like in {@code String field="exampleValue";}). */
  private String literalInitializer;

  /** Field javadoc. */
  private JavadocDto javadoc;

  /** Types to import for this field (e.g., TrackedValue, the boxed inner type). */
  private final Set<ImportStatement> fieldTypeImports = new LinkedHashSet<>();

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Returns the field type.
   *
   * @return the field type
   */
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

  public Set<ImportStatement> getFieldTypeImports() {
    return fieldTypeImports;
  }

  // Convenience methods for adding imports without creating DTOs

  /**
   * Adds a regular import for this field (convenience method accepting Class).
   *
   * @param clazz the class to import
   */
  public void addImport(Class<?> clazz) {
    this.fieldTypeImports.add(new RegularImport(TypeName.of(clazz)));
  }

  /**
   * Adds a regular import for this field (convenience method accepting TypeName).
   *
   * @param typeName the type to import
   */
  public void addImport(TypeName typeName) {
    if (typeName instanceof TypeNameGeneric generic) {
      addImport(generic.getRawType());
      generic.getInnerTypeArguments().forEach(this::addImport);
    } else if (typeName instanceof TypeNameArray array) {
      addImport(array.getTypeOfArray());
    } else {
      this.fieldTypeImports.add(new RegularImport(typeName));
    }
  }

  /**
   * Adds a static import for this field (convenience method accepting Class and member name).
   *
   * @param clazz the class containing the static member
   * @param memberName the name of the static member
   */
  public void addStaticImport(Class<?> clazz, String memberName) {
    this.fieldTypeImports.add(new StaticImport(TypeName.of(clazz), memberName));
  }

  /**
   * Adds a static import for this field (convenience method accepting TypeName and member name).
   *
   * @param type the type containing the static member
   * @param memberName the name of the static member
   */
  public void addStaticImport(TypeName type, String memberName) {
    TypeName typeRaw = new TypeName(type.getPackageName(), type.getClassName());
    this.fieldTypeImports.add(new StaticImport(typeRaw, memberName));
  }

  /**
   * Returns a short string representation of this field using Apache Commons ToStringBuilder.
   *
   * @return concise string representation showing field name, type, and visibility
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("name", fieldName)
        .append("type", fieldType)
        .toString();
  }
}
