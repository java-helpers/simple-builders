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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import org.javahelpers.simple.builders.processor.dtos.AnnotationDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;

/** Extractor for field annotations, converting them from Java model elements to DTOs. */
public final class FieldAnnotationExtractor {

  private FieldAnnotationExtractor() {
    // Private constructor to prevent instantiation
  }

  /**
   * Extracts annotations from a field parameter. Filters out annotations that should not be copied
   * to the builder.
   *
   * @param param the parameter element containing annotations
   * @param context processing context
   * @return list of annotations to be copied to the builder field
   */
  public static List<AnnotationDto> extractAnnotations(
      VariableElement param, ProcessingContext context) {
    List<AnnotationDto> annotations = new ArrayList<>();
    List<? extends AnnotationMirror> annotationMirrors = param.getAnnotationMirrors();

    context.debug(
        "  -> Extracting %d annotation(s) from field %s",
        annotationMirrors.size(), param.getSimpleName());

    for (AnnotationMirror mirror : annotationMirrors) {
      extractAnnotation(mirror, context).ifPresent(annotations::add);
    }

    return annotations;
  }

  /**
   * Extracts a single annotation from an AnnotationMirror. Filters out annotations that should not
   * be copied to the builder.
   *
   * @param mirror the annotation mirror to process
   * @param context processing context
   * @return Optional containing the extracted annotation, or empty if it should be skipped
   */
  private static Optional<AnnotationDto> extractAnnotation(
      AnnotationMirror mirror, ProcessingContext context) {
    // Get the annotation type element
    Element annotationElement = mirror.getAnnotationType().asElement();
    if (!(annotationElement instanceof TypeElement annotationType)) {
      return Optional.empty();
    }

    String annotationQualifiedName = annotationType.getQualifiedName().toString();

    // Filter out annotations that should not be copied to the builder
    if (shouldSkipAnnotation(annotationQualifiedName)) {
      context.debug("    -> Skipping annotation: %s", annotationQualifiedName);
      return Optional.empty();
    }

    // Create AnnotationDto
    AnnotationDto annotationDto = new AnnotationDto();

    // Extract package and class name
    String packageName = context.getPackageName(annotationType);
    String simpleName = annotationType.getSimpleName().toString();
    annotationDto.setAnnotationType(new TypeName(packageName, simpleName));

    // Extract annotation members (parameters)
    Map<? extends ExecutableElement, ? extends javax.lang.model.element.AnnotationValue>
        elementValues = mirror.getElementValues();
    for (Map.Entry<? extends ExecutableElement, ? extends javax.lang.model.element.AnnotationValue>
        entry : elementValues.entrySet()) {
      String memberName = entry.getKey().getSimpleName().toString();
      String memberValue = formatAnnotationValue(entry.getValue());
      annotationDto.addMember(memberName, memberValue);
    }

    context.debug("    -> Added annotation: %s", annotationQualifiedName);
    return Optional.of(annotationDto);
  }

  /**
   * Formats an annotation value as a code string suitable for code generation. Handles different
   * types of annotation values including primitives, strings, enums, classes, arrays, and nested
   * annotations.
   *
   * @param value the annotation value to format
   * @return formatted code string suitable for source code
   */
  private static String formatAnnotationValue(javax.lang.model.element.AnnotationValue value) {
    Object actualValue = value.getValue();

    // Handle enum constants - need to fully qualify them
    if (actualValue instanceof VariableElement varElement
        && varElement.getKind() == ElementKind.ENUM_CONSTANT) {
      return formatEnumConstant(varElement);
    }

    // Handle class literals - already properly formatted by toString()
    if (actualValue instanceof DeclaredType) {
      return value.toString();
    }

    // Handle arrays - need to recursively format elements
    if (actualValue instanceof List<?> list) {
      return formatArray(list);
    }

    // For all other types (primitives, strings, nested annotations), toString() works correctly
    return value.toString();
  }

  /**
   * Formats an enum constant with its fully qualified name.
   *
   * @param varElement the variable element representing the enum constant
   * @return fully qualified enum constant name
   */
  private static String formatEnumConstant(VariableElement varElement) {
    Element enumClass = varElement.getEnclosingElement();
    if (enumClass instanceof TypeElement enumType) {
      return enumType.getQualifiedName() + "." + varElement.getSimpleName();
    }
    return varElement.getSimpleName().toString();
  }

  /**
   * Formats an array of annotation values.
   *
   * @param list the list of array elements
   * @return formatted array string
   */
  private static String formatArray(List<?> list) {
    if (list.isEmpty()) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      if (list.get(i) instanceof javax.lang.model.element.AnnotationValue annotValue) {
        sb.append(formatAnnotationValue(annotValue));
      } else {
        sb.append(list.get(i).toString());
      }
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Determines if an annotation should be skipped (not copied to the builder).
   *
   * @param qualifiedName the fully qualified name of the annotation
   * @return true if the annotation should be skipped, false otherwise
   */
  private static boolean shouldSkipAnnotation(String qualifiedName) {
    // Skip SimpleBuilder framework annotations
    if (qualifiedName.startsWith("org.javahelpers.simple.builders.")) {
      return true;
    }
    // Skip standard Java annotations that are not relevant for builder fields
    return qualifiedName.equals("java.lang.Override")
        || qualifiedName.equals("java.lang.Deprecated")
        || qualifiedName.equals("java.lang.SuppressWarnings")
        || qualifiedName.startsWith("javax.annotation.processing.");
  }
}
