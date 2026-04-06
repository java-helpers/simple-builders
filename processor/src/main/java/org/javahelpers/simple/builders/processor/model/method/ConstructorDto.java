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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Dedicated constructor representation (separate from MethodDto).
 *
 * <p>This DTO contains all information needed to render a constructor in the generated class.
 */
public class ConstructorDto {
  /** Constructor parameters. */
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  /** Constructor body code with type placeholders. */
  private MethodCodeDto methodCodeDto = new MethodCodeDto();

  /** Constructor visibility. */
  private AccessModifier visibility;

  /** Constructor javadoc. */
  private JavadocDto javadoc;

  /** Constructor annotations. */
  private final List<AnnotationDto> annotations = new ArrayList<>();

  public List<MethodParameterDto> getParameters() {
    return parameters;
  }

  public void addParameter(MethodParameterDto parameter) {
    this.parameters.add(parameter);
  }

  /**
   * Gets the constructor body as MethodCodeDto.
   *
   * @return the method code DTO containing body and type placeholders
   */
  public MethodCodeDto getMethodCodeDto() {
    return methodCodeDto;
  }

  /**
   * Sets the constructor body using MethodCodeDto.
   *
   * @param methodCodeDto the method code DTO
   */
  public void setMethodCodeDto(MethodCodeDto methodCodeDto) {
    this.methodCodeDto = methodCodeDto;
  }

  /**
   * Gets the constructor body as a plain string (for backward compatibility).
   *
   * @return the constructor body code
   * @deprecated Use {@link #getMethodCodeDto()} instead for proper type handling
   */
  @Deprecated
  public String getBody() {
    return methodCodeDto != null ? methodCodeDto.getCodeFormat() : null;
  }

  /**
   * Sets the constructor body as a plain string (for backward compatibility).
   *
   * @param body the constructor body code
   * @deprecated Use {@link #setMethodCodeDto(MethodCodeDto)} instead for proper type handling
   */
  @Deprecated
  public void setBody(String body) {
    if (this.methodCodeDto == null) {
      this.methodCodeDto = new MethodCodeDto();
    }
    this.methodCodeDto.setCodeFormat(body);
  }

  public AccessModifier getVisibility() {
    return visibility;
  }

  public void setVisibility(AccessModifier visibility) {
    this.visibility = visibility;
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

  /**
   * Gets code block imports from the method code DTO.
   *
   * @return set of types used in code blocks
   */
  public Set<TypeName> getCodeBlockImports() {
    return methodCodeDto != null ? methodCodeDto.getCodeBlockImports() : new LinkedHashSet<>();
  }

  /**
   * Adds a code block import to the method code DTO.
   *
   * @param typeName the type to import
   */
  public void addCodeBlockImport(TypeName typeName) {
    if (this.methodCodeDto == null) {
      this.methodCodeDto = new MethodCodeDto();
    }
    this.methodCodeDto.addCodeBlockImport(typeName);
  }
}
