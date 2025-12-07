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

package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.util.BuilderDefinitionCreator.extractFromElement;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.apache.commons.lang3.tuple.Pair;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template;
import org.javahelpers.simple.builders.processor.dtos.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.enums.CompilerArgumentsEnum;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.util.BuilderConfigurationReader;
import org.javahelpers.simple.builders.processor.util.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.util.JavaCodeGenerator;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;
import org.javahelpers.simple.builders.processor.util.ProcessingLogger;

/**
 * BuilderProcessor is an annotation processor for execution in generate-sources phase. The
 * BuilderProcessor using the Java way for generating builders, by implementing {@code
 * javax.annotation.processing.AbstractProcessor}.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
public class BuilderProcessor extends AbstractProcessor {
  private ProcessingContext context;
  private JavaCodeGenerator codeGenerator;
  private boolean supportedJdk = true;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    ProcessingLogger logger = new ProcessingLogger(processingEnv);

    // Read global configuration from compiler arguments
    CompilerArgumentsReader reader = new CompilerArgumentsReader(processingEnv);
    BuilderConfiguration globalConfig = reader.readBuilderConfiguration();

    this.context =
        new ProcessingContext(
            processingEnv.getElementUtils(), processingEnv.getTypeUtils(), logger, globalConfig);
    context.debug("Loaded global configuration from compiler arguments: %s", globalConfig);
    this.codeGenerator = new JavaCodeGenerator(processingEnv.getFiler(), logger);

    SourceVersion current = processingEnv.getSourceVersion();
    this.supportedJdk = isAtLeastJava17(current);
    if (!this.supportedJdk) {
      context.error(
          "simple-builders requires Java 17 or higher for annotation processing. Detected: '%s'. Please upgrade to JDK 17+ or disable the processor.",
          current);
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!supportedJdk) {
      // Fail fast: we already emitted an error in init(); do not attempt any processing.
      return false;
    }

    BuilderConfigurationReader reader = context.getConfigurationReader();

    // Find all elements to process and their configuration:
    // 1. Elements annotated with @SimpleBuilder
    // 2. Elements annotated with custom annotations that have @SimpleBuilder.Template
    Set<Pair<Element, BuilderConfiguration>> elementsToProcess = new HashSet<>();

    // Find all @SimpleBuilder annotations
    TypeElement simpleBuilderAnnotation =
        context.getTypeElement(SimpleBuilder.class.getCanonicalName());
    if (simpleBuilderAnnotation != null) {
      roundEnv.getElementsAnnotatedWith(simpleBuilderAnnotation).stream()
          .forEach(
              element ->
                  elementsToProcess.add(Pair.of(element, reader.readFromInlineOptions(element))));
    }

    // Find all Annotations with @SimpleBuilder.Template
    List<Pair<TypeElement, BuilderConfiguration>> annotationsWithTemplate =
        extractingAnnotationsWithTemplate(annotations);
    for (Pair<TypeElement, BuilderConfiguration> annotationWithConfigPair :
        annotationsWithTemplate) {
      TypeElement annotation = annotationWithConfigPair.getLeft();
      BuilderConfiguration config = annotationWithConfigPair.getRight();
      roundEnv.getElementsAnnotatedWith(annotation).stream()
          .forEach(element -> elementsToProcess.add(Pair.of(element, config)));
    }

    context.debug("===============================");
    context.info("simple-builders: PROCESSING ROUND START");
    context.debug("===============================");
    context.debug(
        "simple-builders: Processing round started. Found %d annotated elements.",
        elementsToProcess.size());

    for (Pair<Element, BuilderConfiguration> annotatedElementWithConfig : elementsToProcess) {
      Element annotatedElement = annotatedElementWithConfig.getLeft();
      BuilderConfiguration config = annotatedElementWithConfig.getRight();
      try {
        context.debug("------------------------------------");
        context.debug("simple-builders: Processing element: %s", annotatedElement.getSimpleName());
        context.debug("------------------------------------");
        process(annotatedElement, config);
        context.info(
            "simple-builders: Successfully generated builder for: %s",
            annotatedElement.getSimpleName());
      } catch (BuilderException ex) {
        // All builder generation failures are warnings to allow other builders to be generated
        context.warning(
            annotatedElement, "simple-builders: Failed to generate builder - %s", ex.getMessage());
      }
    }
    return true;
  }

  @Override
  public Set<String> getSupportedOptions() {
    Set<String> options = new HashSet<>();
    for (CompilerArgumentsEnum arg : CompilerArgumentsEnum.values()) {
      options.add(arg.getOptionName()); // e.g., "verbose"
      options.add(arg.getCompilerArgument()); // e.g., "simplebuilder.verbose"
    }
    return options;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  private void process(Element annotatedElement, BuilderConfiguration config)
      throws BuilderException {
    context.initConfigurationForProcessingTarget(config);
    BuilderDefinitionDto builderDef = extractFromElement(annotatedElement, context);
    codeGenerator.generateBuilder(builderDef);
  }

  /**
   * Checks whether the provided SourceVersion is at least Java 17 in a backwards compatible way.
   */
  private static boolean isAtLeastJava17(SourceVersion current) {
    try {
      SourceVersion seventeen = SourceVersion.valueOf("RELEASE_17");
      return current.ordinal() >= seventeen.ordinal();
    } catch (IllegalArgumentException ex) {
      // Running on a JDK where RELEASE_17 does not exist (e.g., JDK 8)
      return false;
    }
  }

  private static List<Pair<TypeElement, BuilderConfiguration>> extractingAnnotationsWithTemplate(
      Set<? extends TypeElement> annotationsFound) {
    List<Pair<TypeElement, BuilderConfiguration>> result = new ArrayList<>();
    for (TypeElement annotation : annotationsFound) {
      // Only process real annotation specifications
      if (annotation.getKind() != javax.lang.model.element.ElementKind.ANNOTATION_TYPE) {
        continue;
      }
      // Skip @SimpleBuilder annotation because we only want to find annotations with
      // @SimpleBuilder.Template
      if (annotation
          .getQualifiedName()
          .toString()
          .equals(org.javahelpers.simple.builders.core.annotations.SimpleBuilder.class.getName())) {
        continue;
      }
      Template templateAnnotation =
          annotation.getAnnotation(
              org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template.class);
      if (templateAnnotation != null) {
        result.add(Pair.of(annotation, new BuilderConfiguration()));
      }
    }
    return result;
  }

  private static BuilderConfiguration extractSimpleBuilderConfiguration(Element element) {
    return new BuilderConfiguration();
  }
}
