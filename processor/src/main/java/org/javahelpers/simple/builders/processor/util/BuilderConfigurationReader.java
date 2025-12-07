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

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
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
 *   <li>{@code @SimpleBuilder(options = ...)} inline options (highest priority)
 *   <li>Custom template annotations (e.g., {@code @CustomBuilder})
 *   <li>Global compiler arguments
 *   <li>Built-in defaults (lowest priority)
 * </ol>
 *
 * <p>Note: If {@code @SimpleBuilder} is present, custom template annotations are ignored.
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

  public BuilderConfiguration resolveWithDirectAnnotation(Element element) {
    return BuilderConfiguration.DEFAULT
        .merge(this.globalConfiguration)
        .merge(this.readFromInlineOptions(element));
  }

  public BuilderConfiguration resolveWithTemplateAnnotation(
      SimpleBuilder.Template templateAnnotation) {
    if (templateAnnotation == null || templateAnnotation.options() == null) {
      // TODO add a debug logging
      return BuilderConfiguration.DEFAULT.merge(this.globalConfiguration);
    }
    return BuilderConfiguration.DEFAULT
        .merge(this.globalConfiguration)
        .merge(this.buildConfigurationFromOptions(templateAnnotation.options()));
  }

  /**
   * Reads builder configuration from {@code @SimpleBuilder(options = ...)} inline options.
   *
   * <p>Returns null if the element has no {@code @SimpleBuilder} annotation.
   *
   * @param element the annotated element to analyze
   * @return configuration from the inline options, or null if not present
   */
  public BuilderConfiguration readFromInlineOptions(Element element) {
    AnnotationMirror simpleBuilderMirror =
        extractAnnotationMirror(
            element, "org.javahelpers.simple.builders.core.annotations.SimpleBuilder");
    return extractOptionsFromAnnotationMirror(simpleBuilderMirror);
  }

  private AnnotationMirror extractAnnotationMirror(Element element, String annotationName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().toString().equals(annotationName)) {
        return mirror;
      }
    }
    return null;
  }

  /**
   * Extracts configuration from the 'options' attribute of an annotation mirror. Used for
   * inline @SimpleBuilder(options = ...) where reflection doesn't work.
   *
   * @param annotationMirror the annotation mirror (either @SimpleBuilder or @Template)
   * @return the configuration extracted from the options attribute
   */
  private BuilderConfiguration extractOptionsFromAnnotationMirror(
      AnnotationMirror annotationMirror) {
    if (annotationMirror == null) {
      return null;
    }

    // Find the 'options' attribute
    AnnotationMirror optionsMirror = null;
    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
        annotationMirror.getElementValues();

    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        elementValues.entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals("options")) {
        Object value = entry.getValue().getValue();
        if (value instanceof AnnotationMirror) {
          optionsMirror = (AnnotationMirror) value;
        }
        break;
      }
    }

    if (optionsMirror == null) {
      // No options specified, return empty configuration
      return null;
    }

    // Parse the options annotation using AnnotationMirror (can't use reflection here)
    return parseOptionsFromMirror(optionsMirror);
  }

  /**
   * Parses SimpleBuilder.Options from AnnotationMirror. Only contains explicitly set values (not
   * defaults).
   */
  private BuilderConfiguration parseOptionsFromMirror(AnnotationMirror optionsMirror) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> values =
        optionsMirror.getElementValues();

    BuilderConfiguration.Builder builder = BuilderConfiguration.builder();

    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        values.entrySet()) {
      String name = entry.getKey().getSimpleName().toString();
      Object value = entry.getValue().getValue();
      String enumValue = extractEnumName(value);

      switch (name) {
        case "generateFieldSupplier" -> builder.generateSupplier(OptionState.valueOf(enumValue));
        case "generateFieldConsumer" -> builder.generateConsumer(OptionState.valueOf(enumValue));
        case "generateBuilderConsumer" ->
            builder.generateBuilderConsumer(OptionState.valueOf(enumValue));
        case "generateConditionalHelper" ->
            builder.generateConditionalLogic(OptionState.valueOf(enumValue));
        case "builderAccess" -> builder.builderAccess(AccessModifier.valueOf(enumValue));
        case "builderConstructorAccess" ->
            builder.builderConstructorAccess(AccessModifier.valueOf(enumValue));
        case "methodAccess" -> builder.methodAccess(AccessModifier.valueOf(enumValue));
        case "generateVarArgsHelpers" ->
            builder.generateVarArgsHelpers(OptionState.valueOf(enumValue));
        case "generateStringFormatHelpers" ->
            builder.generateStringFormatHelpers(OptionState.valueOf(enumValue));
        case "generateUnboxedOptional" ->
            builder.generateUnboxedOptional(OptionState.valueOf(enumValue));
        case "usingArrayListBuilder" ->
            builder.usingArrayListBuilder(OptionState.valueOf(enumValue));
        case "usingArrayListBuilderWithElementBuilders" ->
            builder.usingArrayListBuilderWithElementBuilders(OptionState.valueOf(enumValue));
        case "usingHashSetBuilder" -> builder.usingHashSetBuilder(OptionState.valueOf(enumValue));
        case "usingHashSetBuilderWithElementBuilders" ->
            builder.usingHashSetBuilderWithElementBuilders(OptionState.valueOf(enumValue));
        case "usingHashMapBuilder" -> builder.usingHashMapBuilder(OptionState.valueOf(enumValue));
        case "usingGeneratedAnnotation" ->
            builder.usingGeneratedAnnotation(OptionState.valueOf(enumValue));
        case "usingBuilderImplementationAnnotation" ->
            builder.usingBuilderImplementationAnnotation(OptionState.valueOf(enumValue));
        case "implementsBuilderBase" ->
            builder.implementsBuilderBase(OptionState.valueOf(enumValue));
        case "generateWithInterface" ->
            builder.generateWithInterface(OptionState.valueOf(enumValue));
        case "builderSuffix" -> builder.builderSuffix(value.toString());
        case "setterSuffix" -> builder.setterSuffix(value.toString());
      }
    }

    return builder.build();
  }

  private String extractEnumName(Object value) {
    String enumString = value.toString();
    return enumString.contains(".")
        ? enumString.substring(enumString.lastIndexOf('.') + 1)
        : enumString;
  }

  /**
   * Reads builder configuration from a custom template annotation on the element.
   *
   * <p>Only checks for custom template annotations if {@code @SimpleBuilder} is NOT present. Looks
   * for any custom annotation on the element that is itself annotated with
   * {@code @SimpleBuilder.Template}.
   *
   * <p>Returns null if no template annotation is found or if {@code @SimpleBuilder} is present.
   *
   * @param element the annotated element to analyze
   * @param elementUtils the Elements utility for annotation processing
   * @return configuration from the template annotation, or null if not present
   */
  public BuilderConfiguration readFromTemplate(Element element, Elements elementUtils) {
    // If @SimpleBuilder is present, ignore template annotations (inline options take full
    // precedence)
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (mirror
          .getAnnotationType()
          .toString()
          .equals("org.javahelpers.simple.builders.core.annotations.SimpleBuilder")) {
        return null;
      }
    }

    // Check all annotations on the element to find one annotated with @SimpleBuilder.Template
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      Element annotationElement = mirror.getAnnotationType().asElement();

      // Check using AnnotationMirror for template annotations
      // (this gives us only explicitly set values)
      for (AnnotationMirror metaMirror : annotationElement.getAnnotationMirrors()) {
        String metaAnnotationName = metaMirror.getAnnotationType().toString();
        if (metaAnnotationName.equals(
                "org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template")
            || metaAnnotationName.equals(
                "org.javahelpers.simple.builders.core.annotations.SimpleBuilder$Template")) {
          // Found template via mirror - extract options using AnnotationMirror parsing
          // This approach only gives us explicitly set values, not annotation defaults
          return extractOptionsFromTemplateMirror(metaMirror, elementUtils);
        }
      }
    }

    return null;
  }

  /**
   * Extracts configuration from @SimpleBuilder.Template(options = ...) using AnnotationMirror.
   * Fallback for same-round compiled templates where reflection doesn't work.
   */
  private BuilderConfiguration extractOptionsFromTemplateMirror(
      AnnotationMirror templateMirror, Elements elementUtils) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> templateValues =
        elementUtils.getElementValuesWithDefaults(templateMirror);

    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        templateValues.entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals("options")) {
        Object value = entry.getValue().getValue();
        if (value instanceof AnnotationMirror) {
          AnnotationMirror optionsMirror = (AnnotationMirror) value;
          return parseOptionsFromMirror(optionsMirror);
        }
      }
    }
    return null;
  }

  /** Builds configuration directly from SimpleBuilder.Options using reflection. */
  private BuilderConfiguration buildConfigurationFromOptions(SimpleBuilder.Options options) {
    return BuilderConfiguration.builder()
        .generateSupplier(options.generateFieldSupplier())
        .generateConsumer(options.generateFieldConsumer())
        .generateBuilderConsumer(options.generateBuilderConsumer())
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
   * Resolves the complete builder configuration for an element by chaining all configuration
   * sources in priority order.
   *
   * <p>Priority chain (highest to lowest):
   *
   * <ol>
   *   <li>{@code @SimpleBuilder(options = ...)} inline options (highest priority)
   *   <li>Custom template annotations (only if {@code @SimpleBuilder} not present)
   *   <li>Global compiler arguments
   *   <li>Built-in defaults
   * </ol>
   *
   * <p>Note: If {@code @SimpleBuilder} is present, custom template annotations are completely
   * ignored. The merge chain ensures that for each field: inline options override compiler args,
   * which override defaults.
   *
   * @param element the annotated element to resolve configuration for
   * @param elementUtils the Elements utility for annotation processing
   * @return the fully resolved configuration with all sources merged
   */
  public BuilderConfiguration resolveConfiguration(Element element, Elements elementUtils) {
    // Start with DEFAULT as the base
    // Layer 2: Merge global configuration from compiler arguments
    // Layer 3: Merge template configuration (only if @SimpleBuilder not present)
    return BuilderConfiguration.DEFAULT
        .merge(globalConfiguration)
        .merge(readFromTemplate(element, elementUtils))
        .merge(readFromInlineOptions(element));
  }
}
