package org.javahelpers.simple.builders.internal;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class ProcessingLogger {
  private final Messager messager;

  public ProcessingLogger(Messager messager) {
    this.messager = messager;
  }

  public void error(Element e, String message) {
    messager.printMessage(Diagnostic.Kind.ERROR, message, e);
  }

  public void log(String message) {
    messager.printMessage(Diagnostic.Kind.NOTE, message);
  }
}
