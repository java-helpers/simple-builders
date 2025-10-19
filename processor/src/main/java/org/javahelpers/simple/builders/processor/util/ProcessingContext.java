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

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Context object that wraps Elements, Types, and logging utilities from annotation processing,
 * providing domain-specific methods for type and element operations.
 *
 * <p>This eliminates repetitive parameter passing and provides a cleaner API.
 */
public final class ProcessingContext {
  private final Elements elementUtils;
  private final Types typeUtils;
  private final ProcessingLogger logger;

  /**
   * Creates a new processing context.
   *
   * @param elementUtils utility for operating on program elements
   * @param typeUtils utility for operating on types
   * @param logger the logging utility for the annotation processor
   */
  public ProcessingContext(Elements elementUtils, Types typeUtils, ProcessingLogger logger) {
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;
    this.logger = logger;
  }

  /**
   * Get a type element by its fully qualified class name.
   *
   * @param qualifiedName the canonical class name (e.g., "java.lang.String")
   * @return the type element, or null if not found
   */
  public TypeElement getTypeElement(String qualifiedName) {
    return elementUtils.getTypeElement(qualifiedName);
  }

  /**
   * Get the package containing an element.
   *
   * @param element the element
   * @return the package element
   */
  public PackageElement getPackageOf(Element element) {
    return elementUtils.getPackageOf(element);
  }

  /**
   * Get the package name of an element.
   *
   * @param element the element
   * @return the qualified package name
   */
  public String getPackageName(Element element) {
    return elementUtils.getPackageOf(element).getQualifiedName().toString();
  }

  /**
   * Get all members of a type, including inherited members.
   *
   * @param typeElement the type to inspect
   * @return list of all members
   */
  @SuppressWarnings("java:S1452")
  public List<? extends Element> getAllMembers(TypeElement typeElement) {
    return elementUtils.getAllMembers(typeElement);
  }

  /**
   * Get the Javadoc comment for an element.
   *
   * @param element the element
   * @return the doc comment, or null if none
   */
  public String getDocComment(Element element) {
    return elementUtils.getDocComment(element);
  }

  /**
   * Convert a type mirror to its corresponding element.
   *
   * @param typeMirror the type mirror
   * @return the element, or null if not representable as an element
   */
  public Element asElement(TypeMirror typeMirror) {
    return typeUtils.asElement(typeMirror);
  }

  /**
   * Check if two types are the same type.
   *
   * @param type1 first type
   * @param type2 second type
   * @return true if the types are the same
   */
  public boolean isSameType(TypeMirror type1, TypeMirror type2) {
    return typeUtils.isSameType(type1, type2);
  }

  /**
   * Logs an info-level message that appears in normal Maven output.
   *
   * @param message the info message to log
   */
  public void info(String message) {
    logger.info(message);
  }

  /**
   * Logs an info-level message with a formatted string.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void info(String format, Object... args) {
    logger.info(format, args);
  }

  /**
   * Logs a debug message visible when Maven is run with -X flag.
   *
   * @param message the debug message to log
   */
  public void debug(String message) {
    logger.debug(message);
  }

  /**
   * Logs a debug message with a formatted string.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void debug(String format, Object... args) {
    logger.debug(format, args);
  }

  /**
   * Reports a warning at the location of the given element with a formatted message.
   *
   * @param element the element where the warning occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void warning(Element element, String format, Object... args) {
    logger.warning(element, format, args);
  }

  /**
   * Reports an error with a formatted message.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void error(String format, Object... args) {
    logger.error(format, args);
  }
}
