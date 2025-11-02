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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.processor.dtos.BuilderConfiguration;

/**
 * Reads builder configuration from annotated elements.
 *
 * <p>This class analyzes {@link SimpleBuilder.Options} and {@link SimpleBuilder.Template}
 * annotations on an element and extracts the raw configuration values without merging.
 *
 * <p>Priority order:
 *
 * <ol>
 *   <li>{@code @SimpleBuilder.Options} on the element (highest priority)
 *   <li>{@code @SimpleBuilder.Template} referenced by the element
 *   <li>Global compiler arguments
 *   <li>Built-in defaults (lowest priority)
 * </ol>
 */
public class BuilderConfigurationReader {
  private final BuilderConfiguration globalConfiguration;

  /**
   * Creates a new BuilderConfigurationReader.
   *
   * @param globalConfiguration the global configuration from compiler arguments
   */
  public BuilderConfigurationReader(BuilderConfiguration globalConfiguration) {
    this.globalConfiguration = globalConfiguration;
  }

  /**
   * Reads builder configuration from an annotated element's {@code @SimpleBuilder.Options}
   * annotation.
   *
   * <p>Returns empty Optional if the element has no {@code @SimpleBuilder.Options} annotation.
   *
   * @param element the annotated element to analyze
   * @return Optional containing the configuration from the annotation, or empty if not present
   */
  public BuilderConfiguration readFromOptions(Element element) {
    SimpleBuilder.Options options = element.getAnnotation(SimpleBuilder.Options.class);

    if (options == null) {
      return null;
    }

    // Read raw values from annotation without merging
    return BuilderConfiguration.builder()
        .generateSupplier(options.generateFieldSupplier())
        .generateConsumer(options.generateFieldConsumer())
        .generateBuilderProvider(options.generateBuilderProvider())
        .generateConditionalLogic(options.generateConditionalHelper())
        .builderAccess(options.builderAccess())
        .builderConstructorAccess(options.builderConstructorAccess())
        .methodAccess(options.methodAccess())
        .generateVarArgsHelpers(options.generateVarArgsHelpers())
        .generateStringFormatHelpers(options.generateStringFormatHelpers())
        .generateUnboxedOptional(options.generateUnboxedOptional())
        .usingArrayListBuilder(options.usingArrayListBuilder())
        .usingArrayListBuilderWithElementBuilders(
            options.usingArrayListBuilderWithElementBuilders())
        .usingHashSetBuilder(options.usingHashSetBuilder())
        .usingHashSetBuilderWithElementBuilders(options.usingHashSetBuilderWithElementBuilders())
        .usingHashMapBuilder(options.usingHashMapBuilder())
        .usingGeneratedAnnotation(options.usingGeneratedAnnotation())
        .usingBuilderImplementationAnnotation(options.usingBuilderImplementationAnnotation())
        .implementsBuilderBase(options.implementsBuilderBase())
        .generateWithInterface(options.generateWithInterface())
        .builderSuffix(options.builderSuffix())
        .setterSuffix(options.setterSuffix())
        .build();
  }

  /**
   * Reads builder configuration from a template annotation on the element.
   *
   * <p>Looks for any custom annotation on the element that is itself annotated with
   * {@code @SimpleBuilder.Template}. For example, if the element has {@code @FullFeaturedBuilder},
   * and {@code @FullFeaturedBuilder} is annotated with {@code @SimpleBuilder.Template}, this method
   * reads the configuration from that template.
   *
   * <p>Returns empty Optional if no template annotation is found.
   *
   * @param element the annotated element to analyze
   * @return Optional containing the configuration from the template annotation, or empty if not
   *     present
   */
  public BuilderConfiguration readFromTemplate(Element element) {
    // Check all annotations on the element to find one annotated with @SimpleBuilder.Template
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      try {
        // Get the annotation class
        String annotationClassName = mirror.getAnnotationType().toString();
        Class<?> annotationClass = Class.forName(annotationClassName);

        // Check if this annotation is annotated with @SimpleBuilder.Template
        SimpleBuilder.Template template =
            annotationClass.getAnnotation(SimpleBuilder.Template.class);

        if (template != null) {
          // Found a template annotation, read its options
          SimpleBuilder.Options options = template.options();

          return BuilderConfiguration.builder()
              .generateSupplier(options.generateFieldSupplier())
              .generateConsumer(options.generateFieldConsumer())
              .generateBuilderProvider(options.generateBuilderProvider())
              .generateConditionalLogic(options.generateConditionalHelper())
              .builderAccess(options.builderAccess())
              .builderConstructorAccess(options.builderConstructorAccess())
              .methodAccess(options.methodAccess())
              .generateVarArgsHelpers(options.generateVarArgsHelpers())
              .generateStringFormatHelpers(options.generateStringFormatHelpers())
              .generateUnboxedOptional(options.generateUnboxedOptional())
              .usingArrayListBuilder(options.usingArrayListBuilder())
              .usingArrayListBuilderWithElementBuilders(
                  options.usingArrayListBuilderWithElementBuilders())
              .usingHashSetBuilder(options.usingHashSetBuilder())
              .usingHashSetBuilderWithElementBuilders(
                  options.usingHashSetBuilderWithElementBuilders())
              .usingHashMapBuilder(options.usingHashMapBuilder())
              .usingGeneratedAnnotation(options.usingGeneratedAnnotation())
              .usingBuilderImplementationAnnotation(options.usingBuilderImplementationAnnotation())
              .implementsBuilderBase(options.implementsBuilderBase())
              .generateWithInterface(options.generateWithInterface())
              .builderSuffix(options.builderSuffix())
              .setterSuffix(options.setterSuffix())
              .build();
        }
      } catch (ClassNotFoundException e) {
        // Annotation class not found, skip it
      }
    }

    return null;
  }

  /**
   * Resolves the complete builder configuration for an element by chaining all configuration
   * sources in priority order.
   *
   * <p>Priority chain (highest to lowest):
   *
   * <ol>
   *   <li>{@code @SimpleBuilder.Options} on the element
   *   <li>{@code @SimpleBuilder.Template} on a meta-annotation
   *   <li>Global compiler arguments
   *   <li>Built-in defaults
   * </ol>
   *
   * @param element the annotated element to resolve configuration for
   * @return the fully resolved configuration with all sources merged
   */
  public BuilderConfiguration resolveConfiguration(Element element) {
    // Start with DEFAULT as the base
    // Layer 2: Merge global configuration from compiler arguments
    // Layer 3: Merge template configuration if present
    // Layer 4: Merge options configuration if present (highest priority)
    return BuilderConfiguration.DEFAULT
        .merge(globalConfiguration)
        .merge(readFromTemplate(element))
        .merge(readFromOptions(element));
  }
}
