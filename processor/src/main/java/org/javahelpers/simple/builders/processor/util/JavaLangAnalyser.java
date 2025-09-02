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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/** Helperclass for extrating specific information from existing classes. */
public final class JavaLangAnalyser {

  private JavaLangAnalyser() {}

  /**
   * Helper function to filter methods from {@code java.lang.Object}.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if it is no method from java.lang.Object
   */
  public static boolean isNoMethodOfObjectClass(ExecutableElement mth) {
    String simpleNameOfParent = mth.getEnclosingElement().getSimpleName().toString();
    return !(StringUtils.equals("java.lang.Object", simpleNameOfParent)
        || StringUtils.equals("Object", simpleNameOfParent));
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
   * Helper to check if the method is a setter for a field.
   *
   * @param mth ExecutableElement to be validated
   * @return {@code true}, if the signature matches setters
   */
  public static boolean isSetterForField(ExecutableElement mth) {
    return StringUtils.startsWith(mth.getSimpleName(), "set") && mth.getParameters().size() == 1;
  }

  /**
   * Helper to check if an element has an empty Constructor.
   *
   * @param typeElement
   * @param elementUtils
   * @return {@code true}, if the element has an empty constructor
   */
  public static boolean hasEmptyConstructor(TypeElement typeElement, Elements elementUtils) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(elementUtils.getAllMembers(typeElement));
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
   */
  public static boolean isFunctionalInterface(TypeElement typeElement, Elements elementUtils) {
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
    boolean capture = false;
    StringBuilder sb = new StringBuilder();
    for (String rawLine : lines) {
      String line = rawLine.trim();
      if (line.startsWith("*")) {
        line = line.substring(1).trim();
      }
      if (line.startsWith("@")) {
        // new tag begins; stop capturing unless this is our @param start
        capture = false;
        if (line.startsWith("@param ")) {
          // format: @param <name> text...
          String rest = line.substring("@param ".length()).trim();
          int sp = rest.indexOf(' ');
          String name = sp >= 0 ? rest.substring(0, sp) : rest;
          String text = sp >= 0 ? rest.substring(sp + 1).trim() : "";
          if (Strings.CI.equals(parameterName, name)) {
            if (!text.isEmpty()) {
              sb.append(text);
            }
            capture = true;
          }
        }
        continue;
      }
      if (capture) {
        if (line.isEmpty()) {
          break;
        }
        if (sb.length() > 0) sb.append(' ');
        sb.append(line);
      } else {
        // TODO: add logging for not found javadoc for parameter
      }
    }
    String result = sb.toString().trim();
    return result.isEmpty() ? null : result;
  }
}
