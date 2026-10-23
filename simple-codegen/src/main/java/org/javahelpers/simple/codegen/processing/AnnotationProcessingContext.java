/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.simple.codegen.processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.javahelpers.simple.codegen.analysis.TypeMappingContext;
import org.javahelpers.simple.codegen.logging.ActivePerformanceTracker;
import org.javahelpers.simple.codegen.logging.NoOpPerformanceTracker;
import org.javahelpers.simple.codegen.logging.PerformanceTracker;
import org.javahelpers.simple.codegen.logging.ProcessingLogger;
import org.javahelpers.simple.codegen.model.type.TypeName;

/**
 * Generic annotation-processing context: wraps {@link ProcessingEnvironment}, {@link Elements},
 * {@link Types}, logging and performance tracking, providing domain-agnostic helpers for type and
 * element operations.
 *
 * <p>Builder-specific state lives in {@link BuilderProcessingContext}, which extends this base.
 */
public class AnnotationProcessingContext implements TypeMappingContext {

  /** Element utility of the annotation processing framework. */
  protected final Elements elementUtils;

  /** Type utility of the annotation processing framework. */
  protected final Types typeUtils;

  /** Logger for info, debug, warning and error output. */
  protected final ProcessingLogger logger;

  /** The processing environment of the current round. */
  protected final ProcessingEnvironment processingEnv;

  /** Performance tracker for pipeline phase timing. */
  protected final PerformanceTracker performanceTracker;

  /** Static options supplied by the concrete context. */
  protected final AnnotationProcessingOptions options;

  /** Package generated code is written to, or {@code null} when not generating into a package. */
  private String generatedPackageName;

  /**
   * Creates the context with default options (strict mode enabled, performance tracking disabled).
   *
   * @param logger logger for output
   * @param processingEnv the annotation processing environment
   */
  public AnnotationProcessingContext(ProcessingLogger logger, ProcessingEnvironment processingEnv) {
    this(logger, processingEnv, AnnotationProcessingOptions.builder().build());
  }

  /**
   * Creates the context.
   *
   * @param logger logger for output
   * @param processingEnv the annotation processing environment
   * @param options static options of the concrete context
   */
  public AnnotationProcessingContext(
      ProcessingLogger logger,
      ProcessingEnvironment processingEnv,
      AnnotationProcessingOptions options) {
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();
    this.logger = logger;
    this.processingEnv = processingEnv;
    this.performanceTracker =
        options.performanceTrackingEnabled()
            ? new ActivePerformanceTracker(
                options.performanceOutputFile(), options.phaseHierarchy())
            : new NoOpPerformanceTracker();
    this.options = options;
  }

  /**
   * Sets the name of the package generated code is written to. Called when the current generation
   * target is known.
   *
   * @param generatedPackageName the qualified name of the package generated classes are placed in
   */
  protected void setGeneratedPackageName(String generatedPackageName) {
    this.generatedPackageName = generatedPackageName;
  }

  /**
   * Checks whether a member (constructor, method) is accessible from the package generated code is
   * written to.
   *
   * <p>Public members are always accessible. Private members are never accessible - generated code
   * lives in a separate top-level class. Package-private and protected members are only accessible
   * when the member's declaring package equals the generated package (protected access through
   * inheritance does not apply, as generated code does not extend the target type).
   *
   * @param member the member to check
   * @return {@code true} if generated code may call the member
   */
  @Override
  public boolean isAccessibleFromGeneratedPackage(Element member) {
    if (member.getModifiers().contains(Modifier.PUBLIC)) {
      return true;
    }
    if (member.getModifiers().contains(Modifier.PRIVATE)) {
      return false;
    }
    return generatedPackageName != null && getPackageName(member).equals(generatedPackageName);
  }

  /**
   * Returns the annotation values of an annotation mirror, including default values.
   *
   * @param annotationMirror the annotation mirror to read
   * @return the annotation's element values keyed by their method element
   */
  public Map<ExecutableElement, AnnotationValue> getElementValuesWithDefaults(
      AnnotationMirror annotationMirror) {
    Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
    elementUtils.getElementValuesWithDefaults(annotationMirror).forEach(elementValues::put);
    return elementValues;
  }

  /**
   * Gets the performance tracker for this processing context.
   *
   * <p>When performance tracking is disabled, returns a {@link NoOpPerformanceTracker} that has
   * zero overhead. When enabled via the supplied {@link AnnotationProcessingOptions}, returns an
   * {@link ActivePerformanceTracker} that measures execution times.
   *
   * @return the performance tracker instance
   */
  public PerformanceTracker getPerformanceTracker() {
    return performanceTracker;
  }

  /**
   * Starts a new performance tracking phase. The phase name is passed to {@link
   * #endPerformancePhase(String)} for recording.
   */
  public void startPerformancePhase() {
    performanceTracker.startPhase();
  }

  /**
   * Ends the current performance tracking phase and records its duration under the given name.
   *
   * @param phase the phase name to record
   */
  public void endPerformancePhase(String phase) {
    performanceTracker.endPhase(phase);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default: a generic context copies no type annotations; override to derive from
   * configuration.
   */
  @Override
  public boolean shouldCopyTypeAnnotations() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default: a generic context knows no generated types; override to resolve generated
   * replacement types.
   */
  @Override
  public Optional<TypeName> resolveGeneratedType(TypeElement typeElement) {
    return Optional.empty();
  }

  /**
   * Get the TypeElement for a given qualified class name.
   *
   * @param qualifiedName the canonical class name (e.g., "java.lang.String")
   * @return the type element, or null if not found
   */
  @Override
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
   * Get the package element for a qualified package name.
   *
   * @param name the qualified package name
   * @return the package element, or null if no such package is visible to the compilation
   */
  public PackageElement getPackageElement(String name) {
    return elementUtils.getPackageElement(name);
  }

  /**
   * Get the package name of an element.
   *
   * @param element the element
   * @return the qualified package name
   */
  @Override
  public String getPackageName(Element element) {
    return elementUtils.getPackageOf(element).getQualifiedName().toString();
  }

  /**
   * Get all members of a type, including inherited members.
   *
   * @param typeElement the type to inspect
   * @return list of all members (wildcard enforced by the javax.lang.model API)
   */
  @SuppressWarnings("java:S1452")
  @Override
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
  @Override
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
  @Override
  public boolean isSameType(TypeMirror type1, TypeMirror type2) {
    return typeUtils.isSameType(type1, type2);
  }

  /**
   * Get the erasure of a type (removes generic type information).
   *
   * @param typeMirror the type to erase
   * @return the erasure of the type
   */
  @Override
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
  @Override
  public boolean isAssignable(TypeMirror type1, TypeMirror type2) {
    return typeUtils.isAssignable(type1, type2);
  }

  /**
   * Returns the direct supertypes of a type.
   *
   * @param typeMirror the type
   * @return list of direct supertypes
   */
  @SuppressWarnings("java:S1452")
  @Override
  public java.util.List<? extends TypeMirror> directSupertypes(TypeMirror typeMirror) {
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
   * Logs a debug message visible when Maven is run with -X flag.
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
  @Override
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
   * Returns whether strict/fail-fast mode is enabled via the supplied {@link
   * AnnotationProcessingOptions}.
   *
   * @return {@code true} if strict mode is enabled, {@code false} otherwise
   */
  public boolean isStrictModeEnabled() {
    return options.strictModeEnabled();
  }

  /**
   * Reports an error or warning based on strict mode. In strict mode a build failure is emitted;
   * otherwise a warning is logged so processing can continue.
   *
   * @param element the element associated with the problem, for compiler location information
   * @param format the format string
   * @param args arguments referenced by the format specifiers
   */
  public void reportBasedOnStrictMode(Element element, String format, Object... args) {
    if (isStrictModeEnabled()) {
      logger.error(element, format, args);
    } else {
      logger.warning(element, format, args);
    }
  }

  /**
   * Reports an error or warning based on strict mode, without an element location.
   *
   * @param format the format string
   * @param args arguments referenced by the format specifiers
   */
  public void reportBasedOnStrictMode(String format, Object... args) {
    if (isStrictModeEnabled()) {
      logger.error(format, args);
    } else {
      logger.warning(format, args);
    }
  }
}
