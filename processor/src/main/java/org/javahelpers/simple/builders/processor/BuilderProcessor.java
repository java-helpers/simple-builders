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

package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.processing.BuilderDefinitionCreator.extractFromElement;
import static org.javahelpers.simple.builders.processor.processing.PipelinePhases.PHASE_CONFIGURATION_RESOLUTION;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.javahelpers.codegen.processing.AbstractCodegenProcessor;
import org.javahelpers.codegen.processing.CodegenProcessorConfiguration;
import org.javahelpers.codegen.processing.CodegenTask;
import org.javahelpers.codegen.processing.ElementCollector;
import org.javahelpers.codegen.processing.ElementGroups;
import org.javahelpers.codegen.processing.ProcessorOptionRegistry;
import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder.Template;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilderFors;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.generators.integration.JacksonModuleGenerator;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderToGenerationTypeMapper;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameList;
import org.javahelpers.simple.builders.processor.model.type.TypeNameMap;
import org.javahelpers.simple.builders.processor.model.type.TypeNameSet;
import org.javahelpers.simple.builders.processor.processing.BuilderConfigurationReader;
import org.javahelpers.simple.builders.processor.processing.BuilderProcessingContext;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsEnum;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * BuilderProcessor is an annotation processor for execution in generate-sources phase. The
 * BuilderProcessor using the Java way for generating builders, plugged into the generic pipeline
 * driven by {@link AbstractCodegenProcessor}.
 */
@AutoService(Processor.class)
public class BuilderProcessor
    extends AbstractCodegenProcessor<
        BuilderProcessingContext, BuilderConfiguration, BuilderDefinitionDto> {
  /** Group of elements carrying {@code @SimpleBuilder} or a template-meta-annotated annotation. */
  private static final String GROUP_ANNOTATED_ELEMENTS = "annotatedElements";

  /** Group of holder classes declaring external types via {@code @SimpleBuilderFor}. */
  private static final String GROUP_EXTERNAL_TYPE_HOLDERS = "externalTypeHolders";

  /** The processor's declared compiler options — the supported set is derived, not duplicated. */
  private static final ProcessorOptionRegistry OPTION_REGISTRY =
      ProcessorOptionRegistry.builder()
          .options(List.of(CompilerArgumentsEnum.values()))
          .verboseOption(CompilerArgumentsEnum.VERBOSE)
          .formatterProfileOption(CompilerArgumentsEnum.FORMATTER_PROFILE)
          .build();

  /**
   * Which elements builders are generated for: types carrying {@code @SimpleBuilder} or a
   * user-defined {@code @SimpleBuilder.Template}-meta-annotated annotation (opt-out via
   * {@code @Ignore4BuilderGeneration}), plus classes declaring external types with
   * {@code @SimpleBuilderFor}.
   */
  private static final ElementCollector ELEMENT_COLLECTOR =
      ElementCollector.builder()
          .selector(
              selector ->
                  selector
                      .groupName(GROUP_ANNOTATED_ELEMENTS)
                      .metaMarker(Template.class)
                      .optOutAnnotation(Ignore4BuilderGeneration.class))
          .selector(
              selector ->
                  selector
                      .groupName(GROUP_EXTERNAL_TYPE_HOLDERS)
                      .annotationTypes(SimpleBuilderFor.class, SimpleBuilderFors.class))
          .build();

  /** The processor's static declarations handed to the framework driver. */
  private static final CodegenProcessorConfiguration PROCESSOR_CONFIGURATION =
      CodegenProcessorConfiguration.builder()
          .elementCollector(ELEMENT_COLLECTOR)
          .optionRegistry(OPTION_REGISTRY)
          .requiredSourceVersion("RELEASE_17")
          .loggerPrefix("simple-builders")
          .build();

  /** Creates the processor — javac instantiates it reflectively. */
  public BuilderProcessor() {
    super(PROCESSOR_CONFIGURATION);
  }

  private JacksonModuleGenerator jacksonModuleGenerator;

  @Override
  protected BuilderProcessingContext createContext(
      ProcessingEnvironment processingEnv,
      CompilerArgumentsReader reader,
      ProcessingLogger logger) {
    BuilderConfiguration globalConfig =
        BuilderConfigurationReader.readCompilerConfiguration(reader, logger);
    logger.debug("Loaded global configuration from compiler arguments: %s", globalConfig);
    SimpleBuildersSpiIntegration.initCompilation(processingEnv.getElementUtils());
    this.jacksonModuleGenerator = new JacksonModuleGenerator(processingEnv, logger, globalConfig);
    BuilderProcessingContext ctx =
        new BuilderProcessingContext(logger, globalConfig, processingEnv);

    // Initialize GeneratorRegistry once during processor initialization
    ctx.debugStartOperation("Initializing generator registry");
    try {
      ctx.initGeneratorRegistry();
    } finally {
      ctx.debugEndOperation();
    }
    return ctx;
  }

  @Override
  protected List<CodegenTask<BuilderConfiguration>> planTasks(
      ElementGroups collected, BuilderProcessingContext ctx) {
    List<Element> annotatedElements = collected.byGroup(GROUP_ANNOTATED_ELEMENTS);
    List<Element> holderElements = collected.byGroup(GROUP_EXTERNAL_TYPE_HOLDERS);
    ctx.debug("Found %d annotated elements.", annotatedElements.size());
    ctx.debug("Found %d type(s) for generation with @SimpleBuilderFor.", holderElements.size());
    List<CodegenTask<BuilderConfiguration>> tasks =
        resolveGenerationPlan(
            annotatedElements,
            holderElements,
            ctx.getConfigurationReader(),
            ctx.getPerformanceTracker());
    ctx.debug(
        "%d of %d annotated element(s) are inside the builderGenerationPackages scope.",
        tasks.size(), annotatedElements.size());
    return tasks;
  }

  @Override
  protected boolean isFinished(BuilderProcessingContext ctx) {
    // simple-builders generates in exactly one round — the first round carrying its
    // annotations; elements first appearing in later rounds get no builder.
    return SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration(
        processingEnv.getElementUtils());
  }

  /**
   * Resolves the configuration per element and applies the {@code builderGenerationPackages} scope,
   * returning the tasks for this round. External types listed in {@code @SimpleBuilderFor} are
   * expanded afterwards so a directly annotated DTO always wins over an external-type request for
   * the same builder name.
   */
  private List<CodegenTask<BuilderConfiguration>> resolveGenerationPlan(
      List<Element> sortedElements,
      List<Element> sortedHolders,
      BuilderConfigurationReader reader,
      PerformanceTracker tracker) {
    List<CodegenTask<BuilderConfiguration>> tasks = new ArrayList<>();
    // Builder names already planned in this round: needed for conflict detection, so a directly
    // annotated type wins over an external-type declaration producing the same builder name
    // (the conflicting @SimpleBuilderFor entry is skipped with a warning).
    Set<TypeName> alreadyPlannedBuilders = new HashSet<>();
    for (Element annotatedElement : sortedElements) {
      context.debugStartOperation("Processing element: " + annotatedElement.getSimpleName());
      try {
        planGenerationOfAnnotatedElement(annotatedElement, reader, alreadyPlannedBuilders, tracker)
            .ifPresent(tasks::add);
      } catch (BuilderException ex) {
        // By default builder generation failures are warnings so other builders are still
        // generated. In opt-in strict mode they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            annotatedElement, "Failed to generate builder - %s", ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }

    for (Element holder : sortedHolders) {
      context.debugStartOperation("Processing @SimpleBuilderFor holder: " + holder.getSimpleName());
      try {
        tasks.addAll(planGenerationOfTypeByHolder(holder, reader, alreadyPlannedBuilders, tracker));
      } catch (BuilderException ex) {
        context.reportBasedOnStrictMode(holder, "Failed to generate builder - %s", ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }
    return tasks;
  }

  /**
   * Resolves the configuration of one annotated element and plans its builder, or returns empty
   * when the element is outside the {@code builderGenerationPackages} scope.
   */
  private Optional<CodegenTask<BuilderConfiguration>> planGenerationOfAnnotatedElement(
      Element annotatedElement,
      BuilderConfigurationReader reader,
      Set<TypeName> alreadyPlannedBuilders,
      PerformanceTracker tracker)
      throws BuilderException {
    tracker.startPhase();
    BuilderConfiguration config = reader.resolveConfiguration(annotatedElement);
    tracker.endPhase(PHASE_CONFIGURATION_RESOLUTION);

    if (!context.getBuilderScopeResolver().isInGenerationScope(annotatedElement, config)) {
      return Optional.empty();
    }
    String builderPackage = effectiveBuilderPackage(annotatedElement, config);
    alreadyPlannedBuilders.add(builderTypeName(annotatedElement, builderPackage, config));
    return Optional.of(
        new CodegenTask<>(annotatedElement, config, annotatedElement, builderPackage));
  }

  /**
   * Expands a {@code @SimpleBuilderFor} holder into the external types listed in the {@code value}
   * attributes of its annotation instances and plans a builder for each of them. Each instance
   * resolves its own configuration, so repeated declarations may place their generated builders
   * into different packages via {@code packageName}; without a configured package the holder's
   * package is used.
   */
  private List<CodegenTask<BuilderConfiguration>> planGenerationOfTypeByHolder(
      Element holder,
      BuilderConfigurationReader reader,
      Set<TypeName> alreadyPlannedBuilders,
      PerformanceTracker tracker)
      throws BuilderException {
    Objects.requireNonNull(holder, "holder must not be null");
    List<AnnotationMirror> annotationInstances =
        JavaLangAnalyser.findAnnotationInstances(
            holder, SimpleBuilderFor.class, SimpleBuilderFors.class, context);

    List<CodegenTask<BuilderConfiguration>> results = new ArrayList<>();
    for (AnnotationMirror annotationInstance : annotationInstances) {
      List<TypeElement> targets = extractExternalTargetTypes(holder, annotationInstance);
      if (targets.isEmpty()) {
        context.warning(
            holder,
            "@SimpleBuilderFor on '%s' resolves no types - nothing to generate",
            holder.getSimpleName());
        continue;
      }

      tracker.startPhase();
      BuilderConfiguration config = reader.resolveHolderConfiguration(holder, annotationInstance);
      tracker.endPhase(PHASE_CONFIGURATION_RESOLUTION);

      String builderPackage = effectiveBuilderPackage(holder, config);
      for (TypeElement target : targets) {
        planExternalTarget(target, holder, config, builderPackage, alreadyPlannedBuilders)
            .ifPresent(results::add);
      }
    }
    return results;
  }

  /**
   * Plans a builder for a single type listed in {@code @SimpleBuilderFor}, or reports on the holder
   * why no builder is generated for it. An explicit declaration always generates a builder - the
   * target type's own annotations (such as {@code @Ignore4BuilderGeneration}) are not consulted,
   * and the {@code builderGenerationPackages} scope only filters annotated types, so a scope that
   * would exclude an explicitly named type is a contradictory configuration and only warns.
   */
  private Optional<CodegenTask<BuilderConfiguration>> planExternalTarget(
      TypeElement target,
      Element holder,
      BuilderConfiguration config,
      String builderPackage,
      Set<TypeName> alreadyPlannedBuilders) {
    if (!config.builderGenerationPackages().isEmpty()
        && !config.builderGenerationPackages().includes(builderPackage)) {
      context.warning(
          holder,
          "@SimpleBuilderFor on '%s' generates builder for '%s' in package '%s', which is outside builderGenerationPackages - the explicit declaration takes precedence",
          holder.getSimpleName(),
          target.getQualifiedName(),
          builderPackage);
    }
    TypeName builderName = builderTypeName(target, builderPackage, config);
    if (!alreadyPlannedBuilders.add(builderName)) {
      context.warning(
          holder,
          "skipping '%s' declared in @SimpleBuilderFor on '%s' - builder '%s' is already generated elsewhere",
          target.getQualifiedName(),
          holder.getSimpleName(),
          builderName.getFullQualifiedName());
      return Optional.empty();
    }
    return Optional.of(new CodegenTask<>(target, config, holder, builderPackage));
  }

  /**
   * Resolves the types one {@code @SimpleBuilderFor} annotation instance declares builders for: the
   * types listed in {@code value} first (a type listed there wins over the same type found by
   * scanning), then the constructible top-level types of every {@code sourcePackages} package,
   * ordered by qualified name for deterministic output.
   */
  private List<TypeElement> extractExternalTargetTypes(
      Element holder, AnnotationMirror annotationInstance) throws BuilderException {
    Map<String, TypeElement> targets = new LinkedHashMap<>();
    // For an array-valued attribute javac always delivers a list, even for a single entry.
    List<Object> annotationAttributeArray =
        JavaLangAnalyser.findAnnotationAttributeArray(annotationInstance, "value", context);
    for (Object item : annotationAttributeArray) {
      TypeElement target = resolveExternalTargetType(holder, item);
      targets.put(target.getQualifiedName().toString(), target);
    }
    for (String packageName : readSourcePackageNames(annotationInstance)) {
      for (TypeElement target : findPackageTargetTypes(holder, packageName)) {
        targets.putIfAbsent(target.getQualifiedName().toString(), target);
      }
    }
    return List.copyOf(targets.values());
  }

  /**
   * Reads the {@code sourcePackages} attribute of one {@code @SimpleBuilderFor} annotation
   * instance.
   */
  private List<String> readSourcePackageNames(AnnotationMirror annotationInstance) {
    return JavaLangAnalyser.findAnnotationAttributeArray(
            annotationInstance, "sourcePackages", context)
        .stream()
        .map(item -> item instanceof AnnotationValue value ? value.getValue() : null)
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .toList();
  }

  /**
   * Finds the constructible top-level types of one {@code sourcePackages} entry: only concrete
   * classes and records qualify; interfaces, abstract classes, enums and annotation types are
   * skipped with a debug note.
   */
  private List<TypeElement> findPackageTargetTypes(Element holder, String packageName) {
    PackageElement packageElement = context.getPackageElement(packageName);
    if (packageElement == null) {
      context.warning(
          holder,
          "package '%s' in @SimpleBuilderFor on '%s' could not be resolved - skipped",
          packageName,
          holder.getSimpleName());
      return List.of();
    }
    List<TypeElement> targets = new ArrayList<>();
    for (TypeElement type : ElementFilter.typesIn(packageElement.getEnclosedElements())) {
      if (isConstructibleTopLevelType(type)) {
        targets.add(type);
      } else {
        context.debug(
            "skipping %s '%s' in package '%s' declared by @SimpleBuilderFor on '%s' - no builder can be generated",
            type.getKind().toString().toLowerCase(),
            type.getQualifiedName(),
            packageName,
            holder.getSimpleName());
      }
    }
    targets.sort(Comparator.comparing(type -> type.getQualifiedName().toString()));
    return targets;
  }

  /** Whether a package member is a type a builder can be generated for. */
  private static boolean isConstructibleTopLevelType(TypeElement type) {
    if (type.getKind() == ElementKind.RECORD) {
      return true;
    }
    return type.getKind() == ElementKind.CLASS && !type.getModifiers().contains(Modifier.ABSTRACT);
  }

  /** Resolves one entry of a {@code @SimpleBuilderFor} {@code value} attribute to its type. */
  private TypeElement resolveExternalTargetType(Element holder, Object item)
      throws BuilderException {
    return JavaLangAnalyser.resolveTypeElement(item, context)
        .orElseThrow(
            () ->
                new BuilderException(
                    holder,
                    "Value '%s' in @SimpleBuilderFor on '%s' could not be resolved to a type",
                    item instanceof AnnotationValue value ? value.getValue() : null,
                    holder.getSimpleName()));
  }

  /** Computes the type name of the builder a given target type would produce. */
  private TypeName builderTypeName(
      Element target, String builderPackage, BuilderConfiguration config) {
    return new TypeName(builderPackage, target.getSimpleName() + config.getBuilderSuffix());
  }

  /**
   * The builder type this task will generate — registered up front so references to it resolve
   * during analysis. The package may differ from the target's for {@code @SimpleBuilderFor}
   * targets.
   */
  @Override
  protected List<TypeName> predictTaskResultTypeNames(
      CodegenTask<BuilderConfiguration> task, BuilderProcessingContext ctx) {
    if (!(task.element() instanceof TypeElement targetType)) {
      return List.of();
    }
    return List.of(builderTypeName(targetType, task.outputPackage(), task.configuration()));
  }

  /** SPI-exports the registered builder on top of the generic generated-type registration. */
  @Override
  protected void onTaskResultRegistered(
      CodegenTask<BuilderConfiguration> task, TypeName plannedName, BuilderProcessingContext ctx) {
    if (task.element() instanceof TypeElement targetType) {
      SimpleBuildersSpiIntegration.registerBuilder(
          JavaLangMapper.mapToTypeName(targetType, ctx),
          ctx.getBuilderScopeResolver().generatedBuilderContract(plannedName),
          task.configuration().getSetterPrefix());
    }
  }

  @Override
  protected List<BuilderDefinitionDto> analyse(
      CodegenTask<BuilderConfiguration> task, BuilderProcessingContext ctx)
      throws BuilderException {
    return List.of(extractFromElement(task.element(), ctx));
  }

  @Override
  protected GenerationTargetClassDto mapToTargetSpecification(
      BuilderDefinitionDto definition,
      BuilderConfiguration configuration,
      BuilderProcessingContext ctx) {
    return new BuilderToGenerationTypeMapper(configuration).toRenderingDto(definition);
  }

  @Override
  protected void onTaskGenerated(
      PlannedGeneration<BuilderConfiguration, BuilderDefinitionDto> generation,
      BuilderProcessingContext ctx) {
    BuilderDefinitionDto definition = generation.definition();
    GenerationTargetClassDto renderingDto = generation.target();
    int collectionCount =
        (int)
            definition.getAllFieldsForBuilder().stream()
                .filter(
                    f ->
                        f.getFieldType() instanceof TypeNameList
                            || f.getFieldType() instanceof TypeNameSet
                            || f.getFieldType() instanceof TypeNameMap)
                .count();

    // Collect info for Jackson Module if enabled (the generator logs each registration)
    jacksonModuleGenerator.addEntry(definition, generation.task().element());

    // End class-level timing (started by the driver before analysis)
    ctx.getPerformanceTracker()
        .endClass(definition.getAllFieldsForBuilder().size(), collectionCount);

    // Close the generation scope with the summary of what was generated
    ctx.debugEndOperation(
        "Generated builder with %d fields and %d methods for %s",
        definition.getAllFieldsForBuilder().size(),
        renderingDto.getMethods().size(),
        definition.getBuilderTypeName().getClassName());
  }

  /**
   * The effective builder package: the configured {@code packageName} option when set, otherwise
   * the package of the given element.
   */
  private String effectiveBuilderPackage(Element reportingElement, BuilderConfiguration config) {
    String configured = config.getPackageName();
    return configured == null ? context.getPackageName(reportingElement) : configured;
  }

  /** Generates all Jackson modules after the last processing round and reports the metrics. */
  @Override
  protected void onProcessingOver(BuilderProcessingContext ctx) {
    SimpleBuildersSpiIntegration.finishCompilation();
    ctx.getPerformanceTracker().generateReport(logger);
    for (GenerationTargetClassDto moduleDefinition :
        jacksonModuleGenerator.getModuleDefinitions()) {
      ctx.info(
          "Generating Jackson module in package '%s'",
          moduleDefinition.getTypeName().getPackageName());
      try {
        generateTarget(moduleDefinition);
      } catch (Exception e) {
        ctx.reportBasedOnStrictMode(
            "Error generating Jackson module for package %s: %s",
            moduleDefinition.getTypeName().getPackageName(), e.getMessage());
      } finally {
        // generateTarget leaves its generation-scope operation open — close it here
        ctx.debugEndOperation();
      }
    }
    // Reset indentation after Jackson module generation as well
    ctx.resetIndentation();
  }

  @Override
  protected void endRound(BuilderProcessingContext ctx) {
    // The generating round is done — the registry is final for SPI adapters from here on.
    SimpleBuildersSpiIntegration.finishCompilation();
  }
}
