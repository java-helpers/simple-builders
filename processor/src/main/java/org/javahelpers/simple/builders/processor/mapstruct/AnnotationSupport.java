/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons with the following conditions:
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

package org.javahelpers.simple.builders.processor.mapstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Shared annotation-mirror helpers for the MapStruct SPI implementations: locating the
 * simple-builders annotations and reading {@code @SimpleBuilder}/{@code @SimpleBuilder.Template}
 * options without a {@code ProcessingContext}. Only explicitly set annotation values are read
 * ({@link AnnotationMirror#getElementValues()}) — defaults materialize to the same outcome the
 * callers already apply for absent values.
 */
final class AnnotationSupport {

  static final String SIMPLE_BUILDER_ANNOTATION =
      "org.javahelpers.simple.builders.core.annotations.SimpleBuilder";
  static final String SIMPLE_BUILDER_TEMPLATE_ANNOTATION =
      "org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template";
  static final String IGNORE_4_BUILDER_ANNOTATION =
      "org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration";

  private final Elements elementUtils;

  AnnotationSupport(Elements elementUtils) {
    this.elementUtils = elementUtils;
  }

  String qualifiedNameOf(AnnotationMirror mirror) {
    return ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
  }

  /**
   * Options mirrors relevant for builder generation on {@code beanElement}: {@code options()} of
   * {@code @SimpleBuilder} and of {@code @SimpleBuilder.Template} on builder template annotations.
   */
  List<AnnotationMirror> builderOptionsMirrors(TypeElement beanElement) {
    List<AnnotationMirror> optionsMirrors = new ArrayList<>();
    for (AnnotationMirror mirror : elementUtils.getAllAnnotationMirrors(beanElement)) {
      String annotationName = qualifiedNameOf(mirror);
      if (annotationName.equals(SIMPLE_BUILDER_ANNOTATION)) {
        addOptionsMirror(mirror, optionsMirrors);
      } else {
        // A custom builder template: read options of its @SimpleBuilder.Template meta-annotation.
        Element annotationType = mirror.getAnnotationType().asElement();
        for (AnnotationMirror metaMirror : annotationType.getAnnotationMirrors()) {
          if (qualifiedNameOf(metaMirror).equals(SIMPLE_BUILDER_TEMPLATE_ANNOTATION)) {
            addOptionsMirror(metaMirror, optionsMirrors);
          }
        }
      }
    }
    return optionsMirrors;
  }

  private void addOptionsMirror(AnnotationMirror mirror, List<AnnotationMirror> optionsMirrors) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        mirror.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().contentEquals("options")
          && entry.getValue().getValue() instanceof AnnotationMirror optionsMirror) {
        optionsMirrors.add(optionsMirror);
      }
    }
  }

  String stringOption(AnnotationMirror optionsMirror, String name) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        optionsMirror.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().contentEquals(name)) {
        Object value = entry.getValue().getValue();
        if (value instanceof String stringValue) {
          return stringValue;
        }
      }
    }
    return null;
  }

  /**
   * Whether {@code beanElement} is marked for builder generation ({@code @SimpleBuilder} or a
   * builder template annotation, not opted out via {@code @Ignore4BuilderGeneration}).
   */
  boolean isBuilderGenerationTarget(TypeElement beanElement) {
    boolean marked = false;
    for (AnnotationMirror mirror : elementUtils.getAllAnnotationMirrors(beanElement)) {
      String annotationName = qualifiedNameOf(mirror);
      if (annotationName.equals(IGNORE_4_BUILDER_ANNOTATION)) {
        return false;
      }
      if (annotationName.equals(SIMPLE_BUILDER_ANNOTATION)) {
        marked = true;
        continue;
      }
      // A custom builder template annotation (e.g. @SimpleMinimalBuilder or a project-defined
      // one): its type is meta-annotated with @SimpleBuilder.Template.
      for (AnnotationMirror metaMirror :
          mirror.getAnnotationType().asElement().getAnnotationMirrors()) {
        if (qualifiedNameOf(metaMirror).equals(SIMPLE_BUILDER_TEMPLATE_ANNOTATION)) {
          marked = true;
          break;
        }
      }
    }
    return marked;
  }

  /**
   * Reads a {@code simplebuilder.*} option following the project convention: the JVM system
   * property wins over the annotation processor option (which MapStruct does not forward to SPI
   * environments).
   */
  static String systemOption(Map<String, String> processorOptions, String key) {
    return System.getProperty(key, processorOptions.get(key));
  }
}
