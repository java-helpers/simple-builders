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

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilderConstructor;

/** Helperclass for extrating specific information from existing classes. */
public final class JavaLangAnalyser {

  private static final String PARAM_TAG = "@param ";

  private JavaLangAnalyser() {}

  /**
   * Helper function to filter methods from {@code java.lang.Object}.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if it is no method from java.lang.Object
   */
  public static boolean isNoMethodOfObjectClass(ExecutableElement mth) {
    String simpleNameOfParent = mth.getEnclosingElement().getSimpleName().toString();
    return !(Strings.CS.equals("java.lang.Object", simpleNameOfParent)
        || Strings.CS.equals("Object", simpleNameOfParent));
  }

  /**
   * Gets all methods of a class, including inherited methods, excluding methods from {@code
   * java.lang.Object}.
   *
   * @param context the processing context
   * @param typeElement the type element to get methods from
   * @return filtered list excluding Object class methods
   */
  public static List<ExecutableElement> findAllPossibleSettersOfClass(
      TypeElement typeElement, ProcessingContext context) {
    return ElementFilter.methodsIn(context.getAllMembers(typeElement)).stream()
        .filter(JavaLangAnalyser::isNoMethodOfObjectClass)
        .filter(JavaLangAnalyser::isSetterForField)
        .toList();
  }

  /**
   * Helper function to filter methods with specific annotations.
   *
   * @param <A> the annotation type
   * @param annotationClass is the class of an annotation to be searched on executable
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if it the specified annotation is not set on this method
   */
  public static <A extends Annotation> boolean hasNotAnnotation(
      Class<A> annotationClass, ExecutableElement mth) {
    return mth.getAnnotation(annotationClass) == null;
  }

  /**
   * Helper function to filter methods which throw exceptions.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if there is no throws clause
   */
  public static boolean hasNoThrowablesDeclared(ExecutableElement mth) {
    return mth.getThrownTypes().isEmpty();
  }

  /**
   * Helper function to filter all methods with a return value.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if there is no return value
   */
  public static boolean hasNoReturnValue(ExecutableElement mth) {
    return mth.getReturnType().getKind() == VOID;
  }

  /**
   * Helper to check if the method is private.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if method is not private
   */
  public static boolean isNotPrivate(ExecutableElement mth) {
    return !mth.getModifiers().contains(PRIVATE);
  }

  /**
   * Helper to check if the method is static.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if method is not static
   */
  public static boolean isNotStatic(ExecutableElement mth) {
    return !mth.getModifiers().contains(STATIC);
  }

  /**
   * Checks whether the given {@link TypeElement} declares generic type parameters.
   *
   * @param typeElement the type element to inspect
   * @return {@code true} if the type declares one or more type parameters; {@code false} otherwise
   */
  public static boolean hasGenericTypes(TypeElement typeElement) {
    if (typeElement == null) {
      return false;
    }
    return CollectionUtils.isNotEmpty(typeElement.getTypeParameters());
  }

  /**
   * Checks whether the given {@link ExecutableElement} declares generic type parameters.
   *
   * @param executableElement the executable element to inspect
   * @return {@code true} if the executable declares one or more type parameters; {@code false}
   *     otherwise
   */
  public static boolean hasGenericTypes(ExecutableElement executableElement) {
    if (executableElement == null) {
      return false;
    }
    return CollectionUtils.isNotEmpty(executableElement.getTypeParameters());
  }

  /**
   * Helper to check if the method is a setter for a field.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if the signature matches setters
   */
  public static boolean isSetterForField(ExecutableElement mth) {
    String name = mth.getSimpleName().toString();
    return name.startsWith("set")
        && StringUtils.length(name) > 3
        && StringUtils.isAllUpperCase(StringUtils.substring(name, 3, 4))
        && mth.getParameters().size() == 1;
  }

  /**
   * Check if the class (TypeElement) has an empty constructor.
   *
   * @param typeElement the type element to check
   * @param context processing context
   * @return {@code true}, if the element has an empty constructor
   */
  public static boolean hasEmptyConstructor(TypeElement typeElement, ProcessingContext context) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(context.getAllMembers(typeElement));
    return constructors.stream().anyMatch(c -> c.getParameters().isEmpty());
  }

  /**
   * Helper to search for a specific annotation on an element of a method.
   *
   * @param methodElement Element to be checked
   * @param annotationClass Annotation to be checked
   * @return {@code java.util.Optional<javax.lang.model.element.AnnotationMirror>}, if the element
   *     is annotated by annotationClass
   */
  public static Optional<AnnotationMirror> findAnnotation(
      Element methodElement, Class<? extends Annotation> annotationClass) {
    if (methodElement == null) {
      return Optional.empty();
    }

    for (AnnotationMirror annotationMirror : methodElement.getAnnotationMirrors()) {
      if (annotationMirror
          .getAnnotationType()
          .toString()
          .equals(annotationClass.getCanonicalName())) {
        return Optional.of(annotationMirror);
      }
    }

    return Optional.empty();
  }

  /**
   * Determines whether a given type element is a functional interface.
   *
   * <p>Prefers the explicit @FunctionalInterface annotation. Otherwise, returns true only if the
   * element is an interface and declares exactly one abstract instance method (ignoring static and
   * default methods). Inherited abstract methods are ignored for simplicity.
   *
   * @param typeElement the type element to check
   * @return {@code true} if the type is a functional interface, {@code false} otherwise
   */
  public static boolean isFunctionalInterface(TypeElement typeElement) {
    if (typeElement == null) {
      return false;
    }

    // Prefer explicit annotation
    if (JavaLangAnalyser.findAnnotation(typeElement, FunctionalInterface.class).isPresent()) {
      return true;
    }
    // Only interfaces can be functional interfaces
    if (typeElement.getKind() != javax.lang.model.element.ElementKind.INTERFACE) {
      return false;
    }
    // Heuristic: exactly one abstract method declared (ignores inherited ones for simplicity)
    long abstractDeclared =
        ElementFilter.methodsIn(typeElement.getEnclosedElements()).stream()
            .filter(m -> !m.getModifiers().contains(javax.lang.model.element.Modifier.STATIC))
            .filter(m -> !m.getModifiers().contains(javax.lang.model.element.Modifier.DEFAULT))
            .count();
    return abstractDeclared == 1;
  }

  /**
   * Extracts the Javadoc text following the @param tag for the given parameter name. Continuation
   * lines are supported until the next Javadoc tag (starting with '@') or an empty line.
   *
   * @param javaDoc the full raw Javadoc as returned by Elements.getDocComment(...)
   * @param parameter the parameter to look for
   * @return the extracted text or null if not found or empty
   */
  public static String extractParamJavaDoc(String javaDoc, VariableElement parameter) {
    if (javaDoc == null || parameter == null) {
      return null;
    }
    String parameterName = parameter.getSimpleName().toString();
    String[] lines = javaDoc.split("\r?\n");

    // Find the @param line for our parameter
    int indexOfParamTag = findParamTagLine(lines, parameterName);
    if (indexOfParamTag < 0) {
      return null;
    }

    // Extract text from the @param line and continuation lines
    return extractParamText(lines, indexOfParamTag);
  }

  /**
   * Finds the line index containing @param tag for the given parameter name.
   *
   * @param lines the javadoc lines
   * @param parameterName the parameter to search for
   * @return the line index, or -1 if not found
   */
  private static int findParamTagLine(String[] lines, String parameterName) {
    for (int i = 0; i < lines.length; i++) {
      String cleanedLine = cleanJavadocLine(lines[i]);
      if (cleanedLine.startsWith(PARAM_TAG)) {
        String rest = cleanedLine.substring(PARAM_TAG.length()).trim();
        int spaceIndex = rest.indexOf(' ');
        String name = spaceIndex >= 0 ? rest.substring(0, spaceIndex) : rest;
        if (Strings.CI.equals(parameterName, name)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Removes leading asterisk and whitespace from a Javadoc line.
   *
   * @param rawLine the raw line from Javadoc
   * @return the cleaned line
   */
  private static String cleanJavadocLine(String rawLine) {
    String line = rawLine.trim();
    if (line.startsWith("*")) {
      return line.substring(1).trim();
    }
    return line;
  }

  /**
   * Extracts the parameter documentation text starting from the @param line.
   *
   * @param lines the javadoc lines
   * @param startIndex the index of the @param line
   * @return the extracted documentation text or null if empty
   */
  private static String extractParamText(String[] lines, int startIndex) {
    StringBuilder sb = new StringBuilder();

    // Extract initial text from the @param line
    String firstLine = cleanJavadocLine(lines[startIndex]);
    String rest = firstLine.substring(PARAM_TAG.length()).trim();
    int spaceIndex = rest.indexOf(' ');
    if (spaceIndex >= 0) {
      String initialText = rest.substring(spaceIndex + 1).trim();
      if (!initialText.isEmpty()) {
        sb.append(initialText);
      }
    }

    // Append continuation lines until next tag or empty line
    for (int i = startIndex + 1; i < lines.length; i++) {
      String cleanedLine = cleanJavadocLine(lines[i]);

      if (cleanedLine.isEmpty() || cleanedLine.startsWith("@")) {
        break;
      }

      if (!sb.isEmpty()) {
        sb.append(' ');
      }
      sb.append(cleanedLine);
    }

    String result = sb.toString().trim();
    return result.isEmpty() ? null : result;
  }

  /**
   * Finds the getter method for a given field on the specified DTO type.
   *
   * <p>Prefers boolean-style "isX" over "getX" when both are present. The getter must have no
   * parameters and its return type must match the provided field type mirror.
   *
   * @param dtoType the enclosing DTO type element
   * @param fieldName the field name (uncapitalized)
   * @param fieldTypeMirror the expected return type of the getter
   * @param context processing context
   * @return Optional containing the getter ExecutableElement if found
   */
  public static Optional<ExecutableElement> findGetterForField(
      TypeElement dtoType,
      String fieldName,
      TypeMirror fieldTypeMirror,
      ProcessingContext context) {
    if (dtoType == null || fieldName == null || fieldTypeMirror == null) {
      return Optional.empty();
    }
    String cap = StringUtils.capitalize(fieldName);
    String getterCandidate = "get" + cap;
    String booleanGetterCandidate = "is" + cap;
    List<ExecutableElement> classMethods = ElementFilter.methodsIn(context.getAllMembers(dtoType));
    // Prefer boolean-style getter if present
    for (ExecutableElement candidate : classMethods) {
      String name = candidate.getSimpleName().toString();
      if ((name.equals(booleanGetterCandidate) || name.equals(getterCandidate))
          && candidate.getParameters().isEmpty()
          && context.isSameType(candidate.getReturnType(), fieldTypeMirror)) {
        return Optional.of(candidate);
      }
    }
    return Optional.empty();
  }

  /**
   * Determines which constructor to use for builder initialization. Prioritizes constructors
   * annotated with {@link SimpleBuilderConstructor}. If none is annotated, selects the constructor
   * with the highest number of parameters. Returns empty if no constructor has parameters (i.e.,
   * only default constructor or none found).
   *
   * @param annotatedType the type element to search for constructors
   * @param context the processing context providing access to elements and types utilities
   * @return Optional containing the selected constructor, or empty if none suitable
   */
  public static Optional<ExecutableElement> findConstructorForBuilder(
      TypeElement annotatedType, ProcessingContext context) {
    List<ExecutableElement> ctors =
        ElementFilter.constructorsIn(context.getAllMembers(annotatedType));

    // First, check if any constructor is annotated with @SimpleBuilderConstructor
    for (ExecutableElement ctor : ctors) {
      if (ctor.getAnnotation(SimpleBuilderConstructor.class) != null) {
        return Optional.of(ctor);
      }
    }

    // Fall back to heuristic: select constructor with the most parameters
    ExecutableElement selected = null;
    int maxParams = -1;
    for (ExecutableElement ctor : ctors) {
      int p = ctor.getParameters().size();
      if (p > maxParams) {
        maxParams = p;
        selected = ctor;
      }
    }
    return (selected != null && maxParams > 0) ? Optional.of(selected) : Optional.empty();
  }
}
