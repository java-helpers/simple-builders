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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.TypeName;

/** Helperclass for extrating specific information from existing classes. */
public final class JavaLangAnalyser {

  private JavaLangAnalyser() {}

  public static boolean isNoMethodOfObjectClass(ExecutableElement mth) {
    String simpleNameOfParent = mth.getEnclosingElement().getSimpleName().toString();
    return !(StringUtils.equals("java.lang.Object", simpleNameOfParent)
        || StringUtils.equals("Object", simpleNameOfParent));
  }

  public static boolean hasNoThrowablesDeclared(ExecutableElement mth) {
    return mth.getThrownTypes().isEmpty();
  }

  public static boolean hasNoReturnValue(ExecutableElement mth) {
    return mth.getReturnType().getKind() == VOID;
  }

  public static boolean isNotPrivate(ExecutableElement mth) {
    return !mth.getModifiers().contains(PRIVATE);
  }

  public static boolean isNotStatic(ExecutableElement mth) {
    return !mth.getModifiers().contains(STATIC);
  }

  public static boolean isJavaClass(TypeName typeName) {
    return StringUtils.equalsAny(typeName.getPackageName(), "java.lang", "java.time", "java.util");
  }

  public static boolean isList(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "List");
  }

  public static boolean isMap(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "Map");
  }

  public static boolean isSet(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "Set");
  }

  public static boolean isSetterForField(ExecutableElement mth) {
    return StringUtils.startsWith(mth.getSimpleName(), "set") && mth.getParameters().size() == 1;
  }

  public static boolean hasEmptyConstructor(TypeElement typeElement, Elements elementUtils) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(elementUtils.getAllMembers(typeElement));
    return constructors.stream().anyMatch(c -> c.getParameters().isEmpty());
  }

  public static Optional<AnnotationMirror> findAnnotation(
      Element abstractMethodElement, Class<? extends Annotation> annotationClass) {

    for (AnnotationMirror annotationMirror : abstractMethodElement.getAnnotationMirrors()) {
      if (annotationMirror
          .getAnnotationType()
          .toString()
          .equals(annotationClass.getCanonicalName())) {
        return Optional.of(annotationMirror);
      }
    }

    return Optional.empty();
  }
}
