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

package org.javahelpers.simple.builders.processor.model.type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;

/**
 * Represents a nested type (interface or class) to be generated inside the builder.
 *
 * <p>For example, the "With" interface that allows DTOs to implement fluent modification methods.
 */
public class NestedTypeDto {

  /** The simple name of the nested type (e.g., "With"). */
  private String typeName;

  /** The kind of nested type (INTERFACE or CLASS). */
  private NestedTypeKind kind;

  /** Visibility of this nested type. */
  private AccessModifier visibility = AccessModifier.PUBLIC;

  /** Methods to be generated in this nested type. */
  private final List<MethodDto> methods = new LinkedList<>();

  /** Javadoc comment for this nested type. */
  private JavadocDto javadoc;

  /** Type-level annotations for this nested type. */
  private final List<AnnotationDto> annotations = new ArrayList<>();

  public enum NestedTypeKind {
    INTERFACE,
    CLASS
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public NestedTypeKind getKind() {
    return kind;
  }

  public void setKind(NestedTypeKind kind) {
    this.kind = kind;
  }

  public AccessModifier getVisibility() {
    return visibility;
  }

  public void setVisibility(AccessModifier visibility) {
    this.visibility = visibility;
  }

  public List<MethodDto> getMethods() {
    return methods;
  }

  public void addMethod(MethodDto method) {
    this.methods.add(method);
  }

  public JavadocDto getJavadoc() {
    return javadoc;
  }

  public void setJavadoc(JavadocDto javadoc) {
    this.javadoc = javadoc;
  }

  public List<AnnotationDto> getAnnotations() {
    return annotations;
  }

  public void addAnnotation(AnnotationDto annotation) {
    this.annotations.add(annotation);
  }
}
