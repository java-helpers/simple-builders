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

package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.processing.BuilderDefinitionCreator.extractFromElement;
import static org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker.PHASE_BUILDER_DEFINITION_EXTRACTION;
import static org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker.PHASE_CODE_GENERATION;
import static org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker.PHASE_CONFIGURATION_RESOLUTION;
import static org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker.PHASE_DTO_MAPPING;
import static org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker.PHASE_ELEMENT_COLLECTION;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.classgen.roaster.RoasterCodeGenerator;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.generators.integration.JacksonModuleGenerator;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderToGenerationTypeMapper;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.type.TypeNameList;
import org.javahelpers.simple.builders.processor.model.type.TypeNameMap;
import org.javahelpers.simple.builders.processor.model.type.TypeNameSet;
import org.javahelpers.simple.builders.processor.processing.BuilderConfigurationReader;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsEnum;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * BuilderProcessor is an annotation processor for execution in generate-sources phase. The
 * BuilderProcessor using the Java way for generating builders, by implementing {@code
 * javax.annotation.processing.AbstractProcessor}.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
public class BuilderProcessor extends AbstractProcessor {
  private ProcessingContext context;
  private ProcessingLogger logger;
  private RoasterCodeGenerator codeGenerator;
  private JacksonModuleGenerator jacksonModuleGenerator;
  private boolean supportedJdk = true;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.logger = new ProcessingLogger(processingEnv);
    logger.debug("Starting BuilderProcessor...");

    // Read global configuration from compiler arguments
    CompilerArgumentsReader reader = new CompilerArgumentsReader(processingEnv);
    BuilderConfiguration globalConfig = reader.readBuilderConfiguration();
    logger.debug("Loaded global configuration from compiler arguments: %s", globalConfig);

    this.context = new ProcessingContext(logger, globalConfig, processingEnv);
    this.codeGenerator =
        new RoasterCodeGenerator(processingEnv, logger, context.getPerformanceTracker());
    this.jacksonModuleGenerator = new JacksonModuleGenerator(processingEnv, logger, globalConfig);

    // Initialize GeneratorRegistry once during processor initialization
    context.debugStartOperation("Initializing generator registry");
    try {
      context.getGeneratorRegistry();
    } finally {
      context.debugEndOperation();
    }

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

    // Generate Jackson Module if processing is over and feature is enabled
    if (roundEnv.processingOver()) {
      generateJacksonModules(context.getPerformanceTracker());
      return false;
    }

    PerformanceTracker tracker = context.getPerformanceTracker();
    context.info("simple-builders: PROCESSING ROUND START");

    tracker.startPhase();
    Set<Element> elementsToProcess = collectElementsToProcess(annotations, roundEnv);
    // Sort elements alphabetically by simple name for deterministic processing
    List<Element> sortedElements =
        elementsToProcess.stream()
            .sorted(Comparator.comparing(element -> element.getSimpleName().toString()))
            .toList();
    tracker.endPhase(PHASE_ELEMENT_COLLECTION);

    context.debug(
        "simple-builders: Processing round started. Found %d annotated elements.",
        elementsToProcess.size());

    // Resolve configuration and apply generation scopes before processing any builder. This lets
    // the scope resolver know every builder that will be generated in this round.
    List<ElementToGenerate> elementsToGenerate =
        resolveGenerationPlan(sortedElements, context.getConfigurationReader(), tracker);
    context.debug(
        "simple-builders: %d of %d annotated element(s) are inside the builderGenerationPackages scope.",
        elementsToGenerate.size(), sortedElements.size());
    registerGeneratedTypes(elementsToGenerate);

    int successfulGenerations = generateBuilders(elementsToGenerate, tracker);

    // Log summary of builder generation
    if (successfulGenerations > 0) {
      context.info(
          "simple-builders: Successfully generated %d builder(s) in this processing round",
          successfulGenerations);
    }

    // Reset indentation level at the end of each processing round to prevent cascading errors
    context.resetIndentation();
    return true;
  }

  /** Generates all Jackson modules after the last processing round and reports the metrics. */
  private void generateJacksonModules(PerformanceTracker tracker) {
    tracker.generateReport(logger);

    List<GenerationTargetClassDto> moduleClassDefs = jacksonModuleGenerator.getModuleDefinitions();
    for (GenerationTargetClassDto moduleClassDef : moduleClassDefs) {
      String packageName = moduleClassDef.getTypeName().getPackageName();
      context.info("Generating Jackson Module in package '%s'", packageName);
      try {
        codeGenerator.generateClass(moduleClassDef);
      } catch (BuilderException e) {
        // By default Jackson module generation failures are warnings. In opt-in strict mode
        // they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            "simple-builders: Error generating Jackson module for package %s: %s",
            packageName, e.getMessage());
      }
    }
    // Reset indentation after Jackson module generation as well
    context.resetIndentation();
  }

  /**
   * Collects all elements to process in this round: any element annotated with an annotation that
   * is meta-annotated with {@code @SimpleBuilder.Template} (including {@code @SimpleBuilder}
   * itself), minus elements opted out via {@code @Ignore4BuilderGeneration}.
   */
  private Set<Element> collectElementsToProcess(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<Element> elementsToProcess = new HashSet<>();
    for (TypeElement annotation : extractingAnnotationsWithTemplate(annotations)) {
      elementsToProcess.addAll(roundEnv.getElementsAnnotatedWith(annotation));
    }

    // Only directly-declared annotations are checked: the type itself must carry
    // the opt-out; a parent carrying it does not cascade, matching the fact that
    // @Ignore4BuilderGeneration is intentionally NOT @Inherited.
    elementsToProcess.removeIf(
        element -> {
          Optional<AnnotationMirror> ignoreAnnotation =
              JavaLangAnalyser.findAnnotation(element, Ignore4BuilderGeneration.class);
          if (ignoreAnnotation.isPresent()) {
            context.debug(
                "Skipping element '%s' due to @Ignore4BuilderGeneration opt-out.",
                element.getSimpleName());
            return true;
          }
          return false;
        });
    return elementsToProcess;
  }

  /**
   * Resolves the configuration per element and applies the {@code builderGenerationPackages} scope,
   * returning the elements that will have a builder generated in this round.
   */
  private List<ElementToGenerate> resolveGenerationPlan(
      List<Element> sortedElements, BuilderConfigurationReader reader, PerformanceTracker tracker) {
    List<ElementToGenerate> elementsToGenerate = new ArrayList<>();
    for (Element annotatedElement : sortedElements) {
      context.debugStartOperation("Processing element: " + annotatedElement.getSimpleName());
      try {
        tracker.startPhase();
        BuilderConfiguration config = reader.resolveConfiguration(annotatedElement);
        tracker.endPhase(PHASE_CONFIGURATION_RESOLUTION);

        if (!context.getBuilderScopeResolver().isInGenerationScope(annotatedElement, config)) {
          continue;
        }
        elementsToGenerate.add(new ElementToGenerate(annotatedElement, config));
      } catch (BuilderException ex) {
        // By default builder generation failures are warnings so other builders are still
        // generated. In opt-in strict mode they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            annotatedElement, "simple-builders: Failed to generate builder - %s", ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }
    return elementsToGenerate;
  }

  /**
   * Registers the types whose builders will be generated this round with the scope resolver, so it
   * can trust them without a type search.
   */
  private void registerGeneratedTypes(List<ElementToGenerate> elementsToGenerate) {
    context
        .getBuilderScopeResolver()
        .registerGeneratedTypes(
            elementsToGenerate.stream()
                .map(ElementToGenerate::element)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .toList());
  }

  /** Generates a builder for each planned element and returns the number of successes. */
  private int generateBuilders(
      List<ElementToGenerate> elementsToGenerate, PerformanceTracker tracker) {
    int successfulGenerations = 0;
    for (ElementToGenerate elementToGenerate : elementsToGenerate) {
      Element annotatedElement = elementToGenerate.element();
      context.debugStartOperation("Processing element: " + annotatedElement.getSimpleName());
      tracker.startClass(annotatedElement.getSimpleName().toString());
      try {
        process(annotatedElement, elementToGenerate.config());
        successfulGenerations++;
      } catch (BuilderException ex) {
        // By default builder generation failures are warnings so other builders are still
        // generated. In opt-in strict mode they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            annotatedElement, "simple-builders: Failed to generate builder - %s", ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }
    return successfulGenerations;
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
    PerformanceTracker tracker = context.getPerformanceTracker();
    // Track Builder Definition Extraction
    tracker.startPhase();
    BuilderDefinitionDto builderDef = extractFromElement(annotatedElement, context);
    tracker.endPhase(PHASE_BUILDER_DEFINITION_EXTRACTION);

    // Compute class-level metrics now that the definition is available
    int fieldCount = builderDef.getAllFieldsForBuilder().size();
    int collectionCount =
        (int)
            builderDef.getAllFieldsForBuilder().stream()
                .filter(
                    f ->
                        f.getFieldType() instanceof TypeNameList
                            || f.getFieldType() instanceof TypeNameSet
                            || f.getFieldType() instanceof TypeNameMap)
                .count();

    // Track DTO Mapping
    tracker.startPhase();
    GenerationTargetClassDto renderingDto =
        new BuilderToGenerationTypeMapper(config).toRenderingDto(builderDef);
    tracker.endPhase(PHASE_DTO_MAPPING);

    // Track Code Generation (parent phase; sub-phases tracked inside RoasterCodeGenerator)
    tracker.startPhase();

    codeGenerator.generateClass(renderingDto);
    tracker.endPhase(PHASE_CODE_GENERATION);

    // Collect info for Jackson Module if enabled
    jacksonModuleGenerator.addEntry(builderDef, annotatedElement);
    context.debug("Jackson module entry added");

    // End class-level timing (started in caller before config resolution)
    tracker.endClass(fieldCount, collectionCount);

    // Add summary of what was generated
    context.debugEndOperation(
        "Generated builder with %d fields and %d methods for %s",
        builderDef.getAllFieldsForBuilder().size(),
        renderingDto.getMethods().size(),
        builderDef.getBuilderTypeName().getClassName());
  }

  private record ElementToGenerate(Element element, BuilderConfiguration config) {}

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

  private static List<TypeElement> extractingAnnotationsWithTemplate(
      Set<? extends TypeElement> annotationsFound) {
    List<TypeElement> result = new ArrayList<>();
    for (TypeElement annotation : annotationsFound) {
      if (shouldSkipAnnotation(annotation)) {
        continue;
      }
      Template templateAnnotation =
          annotation.getAnnotation(
              org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template.class);
      if (templateAnnotation != null) {
        result.add(annotation);
      }
    }
    return result;
  }

  private static boolean shouldSkipAnnotation(TypeElement annotation) {
    // Only process real annotation specifications. @SimpleBuilder is now a built-in template, so
    // it is processed through the same path as custom template annotations.
    return annotation.getKind() != javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
  }
}
