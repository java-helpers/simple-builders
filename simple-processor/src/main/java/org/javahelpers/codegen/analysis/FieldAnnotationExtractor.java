/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.codegen.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;

/** Extractor for field annotations, converting them from Java model elements to DTOs. */
public final class FieldAnnotationExtractor {

  /** Fully qualified name of {@link Deprecated}. */
  private static final String DEPRECATED_FQN = "java.lang.Deprecated";

  private FieldAnnotationExtractor() {
    // Private constructor to prevent instantiation
  }

  /**
   * Extracts the {@code @Deprecated} annotation from the given element, preserving its members
   * (e.g. {@code since} and {@code forRemoval}).
   *
   * <p>Unlike {@link #extractAnnotations(VariableElement, TypeMappingContext)}, this method targets
   * a single annotation and works on any {@link Element} (method, field, parameter, record
   * component, type), not just {@link VariableElement}s.
   *
   * @param element the element to inspect, or {@code null}
   * @param context processing context
   * @return an {@link Optional} containing the {@code @Deprecated} annotation DTO, or empty if the
   *     element is not deprecated
   */
  public static Optional<AnnotationDto> extractDeprecatedAnnotation(
      Element element, TypeMappingContext context) {
    if (element == null) {
      return Optional.empty();
    }
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      Element annotationElement = mirror.getAnnotationType().asElement();
      if (!(annotationElement instanceof TypeElement annotationType)) {
        continue;
      }
      if (DEPRECATED_FQN.equals(annotationType.getQualifiedName().toString())) {
        return extractAnnotation(mirror, context);
      }
    }
    return Optional.empty();
  }

  /**
   * Extracts all annotations from a field parameter.
   *
   * @param param the parameter element containing annotations
   * @param context processing context
   * @return list of extracted annotations
   */
  public static List<AnnotationDto> extractAnnotations(
      VariableElement param, TypeMappingContext context) {
    List<AnnotationDto> annotations = new ArrayList<>();
    List<? extends AnnotationMirror> annotationMirrors = param.getAnnotationMirrors();

    for (AnnotationMirror mirror : annotationMirrors) {
      extractAnnotation(mirror, context).ifPresent(annotations::add);
    }

    return annotations;
  }

  /**
   * Extracts all annotations from a type mirror.
   *
   * @param typeMirror the type mirror containing annotations
   * @param context processing context
   * @return list of extracted annotations
   */
  public static List<AnnotationDto> extractAnnotations(
      javax.lang.model.type.TypeMirror typeMirror, TypeMappingContext context) {
    List<AnnotationDto> annotations = new ArrayList<>();
    List<? extends AnnotationMirror> annotationMirrors = typeMirror.getAnnotationMirrors();

    for (AnnotationMirror mirror : annotationMirrors) {
      extractAnnotation(mirror, context).ifPresent(annotations::add);
    }

    return annotations;
  }

  /**
   * Extracts a single annotation from an AnnotationMirror.
   *
   * @param mirror the annotation mirror to process
   * @param context processing context
   * @return Optional containing the extracted annotation, or empty if the annotation type cannot be
   *     resolved to a type element
   */
  private static Optional<AnnotationDto> extractAnnotation(
      AnnotationMirror mirror, TypeMappingContext context) {
    Element annotationElement = mirror.getAnnotationType().asElement();
    if (!(annotationElement instanceof TypeElement annotationType)) {
      return Optional.empty();
    }

    // Extract package and class name
    String packageName = context.getPackageName(annotationType);
    String simpleName = annotationType.getSimpleName().toString();

    // Extract annotation members (parameters)
    Map<? extends ExecutableElement, ? extends javax.lang.model.element.AnnotationValue>
        elementValues = mirror.getElementValues();
    Map<String, String> members = new LinkedHashMap<>();
    for (Map.Entry<? extends ExecutableElement, ? extends javax.lang.model.element.AnnotationValue>
        entry : elementValues.entrySet()) {
      String memberName = entry.getKey().getSimpleName().toString();
      String memberValue = formatAnnotationValue(entry.getValue());
      members.put(memberName, memberValue);
    }

    AnnotationDto annotationDto =
        AnnotationDtoBuilder.create()
            .annotationType(new TypeName(packageName, simpleName))
            .members(members)
            .build();

    context.debug("    -> Added annotation: %s", annotationType.getQualifiedName());
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
   * Checks if a parameter has a non-null constraint annotation.
   *
   * <p>Detects annotations named "NotNull" or "NonNull" (case-sensitive) from any package, making
   * it framework-agnostic and future-proof.
   *
   * @param param the parameter element to check
   * @return true if the parameter has a non-null constraint annotation
   */
  public static boolean hasNonNullConstraint(VariableElement param) {
    return param.getAnnotationMirrors().stream()
        .map(am -> am.getAnnotationType().asElement())
        .filter(TypeElement.class::isInstance)
        .map(el -> ((TypeElement) el).getSimpleName().toString())
        .anyMatch(FieldAnnotationExtractor::isNonNullAnnotation);
  }

  /**
   * Checks if the given annotation simple name represents a non-null constraint.
   *
   * @param simpleName the simple name of the annotation (e.g., "NotNull", "NonNull")
   * @return true if it's a recognized non-null annotation name
   */
  private static boolean isNonNullAnnotation(String simpleName) {
    return "NotNull".equals(simpleName) || "NonNull".equals(simpleName);
  }

  /**
   * Extracts the {@code value()} member from any annotation on the given element whose simple name
   * matches one of the provided names, regardless of package.
   *
   * <p>For example, if {@code annotationNames} contains {@code "Default"} and {@code
   * "DefaultValue"}, this will detect annotations from any package (e.g. a domain's own {@code
   * Default} annotation or {@code jakarta.ws.rs.DefaultValue}) as long as they have a {@code String
   * value()} member.
   *
   * @param element the element to check for annotations
   * @param annotationNames the set of annotation simple names to look for
   * @return an {@link Optional} containing the raw value string, or empty if no matching annotation
   *     with a {@code value()} member is present
   */
  public static Optional<String> extractAnnotationValue(
      Element element, Set<String> annotationNames) {
    return element.getAnnotationMirrors().stream()
        .filter(mirror -> isAnnotationWithName(mirror, annotationNames))
        .map(FieldAnnotationExtractor::getValueMember)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  /**
   * Checks if the given annotation mirror's simple name matches one of the provided names.
   *
   * @param mirror the annotation mirror to check
   * @param annotationNames the set of annotation simple names to match against
   * @return {@code true} if the annotation's simple name is in the provided set
   */
  private static boolean isAnnotationWithName(
      AnnotationMirror mirror, Set<String> annotationNames) {
    if (!(mirror.getAnnotationType().asElement() instanceof TypeElement type)) {
      return false;
    }
    return annotationNames.contains(type.getSimpleName().toString());
  }

  /**
   * Extracts the {@code value()} member from an annotation mirror as a string.
   *
   * @param mirror the annotation mirror to extract from
   * @return an {@link Optional} containing the value string, or empty if no {@code value()} member
   *     is present
   */
  private static Optional<String> getValueMember(AnnotationMirror mirror) {
    return mirror.getElementValues().entrySet().stream()
        .filter(e -> "value".equals(e.getKey().getSimpleName().toString()))
        .map(e -> e.getValue().getValue().toString())
        .findFirst();
  }

  /**
   * Formats a raw string default value as a Java expression based on the field type.
   *
   * <p>Interpretation rules:
   *
   * <ul>
   *   <li>{@code String} — wrapped in double quotes, e.g. {@code "GENERAL"}
   *   <li>{@code char} — wrapped in single quotes, e.g. {@code 'A'}
   *   <li>numeric/boolean primitives — used as-is, e.g. {@code 0.0}, {@code true}
   *   <li>enum types — a simple constant name is qualified with the enum class name, e.g. {@code
   *       GOOD} becomes {@code ItemCondition.GOOD}; already-qualified names or complex expressions
   *       are left as-is
   *   <li>complex types (List, custom objects) — used as a raw Java expression, e.g. {@code
   *       List.of()}
   * </ul>
   *
   * @param rawValue the raw string from the annotation's {@code value()} member
   * @param fieldType the {@link TypeName} of the target field
   * @return a formatted Java expression string suitable for code generation
   */
  public static String formatDefaultExpression(String rawValue, TypeName fieldType) {
    if (fieldType.equals(TypeName.of(String.class))) {
      return "\"%s\"".formatted(rawValue);
    }
    if (fieldType.equals(TypeNamePrimitive.CHAR)) {
      return "'%s'".formatted(rawValue);
    }
    if (fieldType.isEnumType() && isSimpleIdentifier(rawValue)) {
      return fieldType.getClassName() + "." + rawValue;
    }
    return rawValue;
  }

  /**
   * Checks whether the given raw value is a simple Java identifier (e.g. an enum constant name like
   * {@code GOOD}).
   *
   * <p>Values containing dots (e.g. {@code ItemCondition.GOOD}), parentheses (e.g. {@code new
   * IntegerDto(12)}), spaces, or other non-identifier characters are not simple identifiers and
   * will not be auto-qualified.
   *
   * @param rawValue the raw string to check
   * @return {@code true} if the value is a simple Java identifier
   */
  private static boolean isSimpleIdentifier(String rawValue) {
    return SourceVersion.isIdentifier(rawValue);
  }
}
