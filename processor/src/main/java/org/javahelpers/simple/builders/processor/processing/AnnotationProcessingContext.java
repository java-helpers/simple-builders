/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.processing;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Generic context object that wraps {@link Elements}, {@link Types}, and logging utilities from
 * annotation processing, providing domain-specific methods for type and element operations.
 *
 * <p>This is the annotation-processor independent part of {@link ProcessingContext}: it contains no
 * builder-specific configuration and can be reused by any annotation processor that works on the
 * shared code-generation model.
 */
public class AnnotationProcessingContext {
  private final Elements elementUtils;
  private final Types typeUtils;
  private final ProcessingLogger logger;
  private final ProcessingEnvironment processingEnv;
  private final PerformanceTracker performanceTracker;

  /**
   * Creates a new annotation processing context.
   *
   * @param logger the logging utility for the annotation processor
   * @param processingEnv the processing environment providing access to utilities and facilities
   * @param performanceTracker the performance tracker for measuring processing phases
   */
  public AnnotationProcessingContext(
      ProcessingLogger logger,
      ProcessingEnvironment processingEnv,
      PerformanceTracker performanceTracker) {
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();
    this.logger = logger;
    this.processingEnv = processingEnv;
    this.performanceTracker = performanceTracker;
  }

  /**
   * Gets the {@link Elements} utility of the processing environment.
   *
   * @return the elements utility
   */
  public Elements getElementUtils() {
    return elementUtils;
  }

  /**
   * Gets the {@link Types} utility of the processing environment.
   *
   * @return the types utility
   */
  public Types getTypeUtils() {
    return typeUtils;
  }

  /**
   * Gets the processing logger.
   *
   * @return the processing logger
   */
  public ProcessingLogger getLogger() {
    return logger;
  }

  /**
   * Gets the processing environment.
   *
   * @return the processing environment
   */
  public ProcessingEnvironment getProcessingEnvironment() {
    return processingEnv;
  }

  /**
   * Gets the performance tracker for this processing context.
   *
   * @return the performance tracker instance
   */
  public PerformanceTracker getPerformanceTracker() {
    return performanceTracker;
  }

  /**
   * Get the TypeElement for a given qualified class name.
   *
   * @param qualifiedName the canonical class name (e.g., "java.lang.String")
   * @return the type element, or null if not found
   */
  public TypeElement getTypeElement(String qualifiedName) {
    return elementUtils.getTypeElement(qualifiedName);
  }

  /**
   * Get the TypeElement for a given TypeName.
   *
   * @param typeName the TypeName containing package and class name
   * @return the type element, or null if not found
   */
  public TypeElement getTypeElement(TypeName typeName) {
    if (typeName == null) {
      return null;
    }
    return getTypeElement(typeName.getFullQualifiedName());
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
   * Get the erasure of a type (removes generic type information).
   *
   * @param typeMirror the type to erase
   * @return the erasure of the type
   */
  public TypeMirror erasure(TypeMirror typeMirror) {
    return typeUtils.erasure(typeMirror);
  }

  /**
   * Check if one type is assignable to another.
   *
   * @param type1 the type to check
   * @param type2 the target type
   * @return true if type1 is assignable to type2
   */
  public boolean isAssignable(TypeMirror type1, TypeMirror type2) {
    return typeUtils.isAssignable(type1, type2);
  }

  /**
   * Returns the direct supertypes of a type.
   *
   * @param typeMirror the type
   * @return list of direct supertypes
   */
  public List<? extends TypeMirror> directSupertypes(TypeMirror typeMirror) {
    return typeUtils.directSupertypes(typeMirror);
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
   * Logs a debug message visible when debug logging is enabled.
   *
   * @param message the debug message to log
   */
  public void debug(String message) {
    logger.debug(message);
  }

  /**
   * Logs a debug message with a formatted string. Only visible when enabled via -Averbose=true or
   * -Asimplebuilder.verbose=true.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void debug(String format, Object... args) {
    logger.debug(format, args);
  }

  /**
   * Starts a new hierarchical operation context for logging with formatted message.
   *
   * @param format the format string for the operation message
   * @param args arguments referenced by the format specifiers
   */
  public void debugStartOperation(String format, Object... args) {
    logger.debugStartOperation(format, args);
  }

  /** Ends the current hierarchical operation context for logging. */
  public void debugEndOperation() {
    logger.debugEndOperation();
  }

  /** Ends the current hierarchical operation context with a closing message for logging. */
  public void debugEndOperation(String format, Object... args) {
    logger.debugEndOperation(format, args);
  }

  /** Resets the indentation level to prevent cascading errors between processing runs. */
  public void resetIndentation() {
    logger.resetIndentation();
  }

  /**
   * Logs a warning message without requiring a specific element context.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void warning(String format, Object... args) {
    logger.warning(null, format, args);
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

  /**
   * Reports an error at the location of the given element with a formatted message.
   *
   * @param element the element where the error occurred, used for location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers in the format string
   */
  public void error(Element element, String format, Object... args) {
    logger.error(element, format, args);
  }
}
