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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;

/**
 * AnnotationValidator validates the positions of annotations. Responsible for throwing exceptions
 * if annotations are used in a wrong way.
 */
public class AnnotationValidator {

  private AnnotationValidator() {}

  /**
   * Validate annotated elements.
   *
   * @param annotatedElement annotated element to be validated
   * @throws BuilderException if annotation-target does not match supported types
   */
  public static void validateAnnotatedElement(Element annotatedElement) throws BuilderException {
    if (annotatedElement.getKind() != ElementKind.CLASS) {
      throw new BuilderException(
          annotatedElement,
          "The " + SimpleBuilder.class.getSimpleName() + " should annotated " + " on class.");
    }

    if (annotatedElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new BuilderException(
          annotatedElement,
          "The " + SimpleBuilder.class.getSimpleName() + " should not be abstract");
    }

    // Only allow @SimpleBuilder on top-level classes
    if (annotatedElement instanceof javax.lang.model.element.TypeElement typeElement
        && typeElement.getNestingKind() != NestingKind.TOP_LEVEL) {
      throw new BuilderException(
          annotatedElement,
          "The "
              + SimpleBuilder.class.getSimpleName()
              + " should be declared on a top-level class only");
    }
  }
}
