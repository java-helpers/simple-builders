package org.javahelpers.simple.builders.internal;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.annotations.BuilderForDtos;

public class AnnotationValidator {

  private AnnotationValidator() {}

  public static void validateAnnotatedElement(Element annotatedElement) throws BuilderException {
    if (annotatedElement.getKind() != ElementKind.CLASS) {
      throw new BuilderException(
          annotatedElement,
          "The " + BuilderForDtos.class.getSimpleName() + " should annotated " + " on class.");
    }

    if (annotatedElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new BuilderException(
          annotatedElement,
          "The " + BuilderForDtos.class.getSimpleName() + " should not be abstract");
    }
  }
}
