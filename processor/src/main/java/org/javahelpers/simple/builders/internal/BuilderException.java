package org.javahelpers.simple.builders.internal;

import javax.lang.model.element.Element;

public class BuilderException extends Exception {
  private final Element element;

  public BuilderException(Element element, Throwable cause) {
    super(cause.getMessage(), cause);
    this.element = element;
  }

  public BuilderException(Element element, String message, Object... args) {
    super(String.format(message, args));
    this.element = element;
  }

  public Element getElement() {
    return element;
  }
}
