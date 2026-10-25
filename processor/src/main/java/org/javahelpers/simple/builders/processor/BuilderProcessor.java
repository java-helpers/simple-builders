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
import static org.javahelpers.simple.builders.processor.processing.PipelinePhases.PHASE_BUILDER_DEFINITION_EXTRACTION;
import static org.javahelpers.simple.builders.processor.processing.PipelinePhases.PHASE_CODE_GENERATION;
import static org.javahelpers.simple.builders.processor.processing.PipelinePhases.PHASE_CONFIGURATION_RESOLUTION;
import static org.javahelpers.simple.builders.processor.processing.PipelinePhases.PHASE_DTO_MAPPING;
import static org.javahelpers.simple.builders.processor.processing.PipelinePhases.PHASE_ELEMENT_COLLECTION;

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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.javahelpers.codegen.processing.AbstractCodegenProcessor;
import org.javahelpers.codegen.processing.AnnotationElementSpec;
import org.javahelpers.codegen.processing.CodegenTask;
import org.javahelpers.codegen.processing.ElementCollection;
import org.javahelpers.codegen.processing.ElementSelector;
import org.javahelpers.codegen.processing.ProcessingTarget;
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
  private static final String GROUP_ANNOTATED_ELEMENTS = "annotatedElements";
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
  private static final AnnotationElementSpec ELEMENT_SPEC =
      AnnotationElementSpec.builder()
          .selector(
              ElementSelector.builder()
                  .name(GROUP_ANNOTATED_ELEMENTS)
                  .metaMarker(Template.class)
                  .optOutAnnotation(Ignore4BuilderGeneration.class)
                  .build())
          .selector(
              ElementSelector.builder()
                  .name(GROUP_EXTERNAL_TYPE_HOLDERS)
                  .annotationTypes(SimpleBuilderFor.class, SimpleBuilderFors.class)
                  .build())
          .collectionPhase(PHASE_ELEMENT_COLLECTION)
          .build();

  /** The global configuration resolved from compiler arguments — needed by several hooks. */
  private BuilderConfiguration globalConfig;

  private JacksonModuleGenerator jacksonModuleGenerator;

  @Override
  protected BuilderProcessingContext createContext(
      ProcessingEnvironment processingEnv,
      CompilerArgumentsReader reader,
      ProcessingLogger logger) {
    this.globalConfig = BuilderConfigurationReader.readCompilerConfiguration(reader, logger);
    logger.debug("Loaded global configuration from compiler arguments: %s", globalConfig);
    SimpleBuildersSpiIntegration.initCompilation(processingEnv.getElementUtils());
    return new BuilderProcessingContext(logger, globalConfig, processingEnv);
  }

  @Override
  protected void onInit(
      ProcessingEnvironment processingEnv,
      CompilerArgumentsReader reader,
      BuilderProcessingContext ctx) {
    this.jacksonModuleGenerator = new JacksonModuleGenerator(processingEnv, logger, globalConfig);

    // Initialize GeneratorRegistry once during processor initialization
    ctx.debugStartOperation("Initializing generator registry");
    try {
      ctx.getGeneratorRegistry();
    } finally {
      ctx.debugEndOperation();
    }
  }

  @Override
  protected AnnotationElementSpec elementSpec() {
    return ELEMENT_SPEC;
  }

  @Override
  protected ProcessorOptionRegistry optionRegistry() {
    return OPTION_REGISTRY;
  }

  @Override
  protected SourceVersion requiredSourceVersion() {
    return SourceVersion.RELEASE_17;
  }

  @Override
  protected String processorTag() {
    return "simple-builders";
  }

  @Override
  protected String generatedKind() {
    return "builder";
  }

  @Override
  protected String generationPhase() {
    return PHASE_CODE_GENERATION;
  }

  @Override
  protected List<CodegenTask<BuilderConfiguration>> plan(
      ElementCollection collected, BuilderProcessingContext ctx) {
    List<Element> sortedElements = collected.byName(GROUP_ANNOTATED_ELEMENTS);
    List<Element> sortedHolders = collected.byName(GROUP_EXTERNAL_TYPE_HOLDERS);
    ctx.debug("simple-builders: Found %d annotated elements.", sortedElements.size());
    ctx.debug(
        "simple-builders: Found %d type(s) for generation with @SimpleBuilderFor.",
        sortedHolders.size());
    List<CodegenTask<BuilderConfiguration>> tasks =
        resolveGenerationPlan(
            sortedElements,
            sortedHolders,
            ctx.getConfigurationReader(),
            ctx.getPerformanceTracker());
    ctx.debug(
        "simple-builders: %d of %d annotated element(s) are inside the builderGenerationPackages scope.",
        tasks.size(), sortedElements.size());
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
            annotatedElement, "simple-builders: Failed to generate builder - %s", ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }

    for (Element holder : sortedHolders) {
      context.debugStartOperation("Processing @SimpleBuilderFor holder: " + holder.getSimpleName());
      try {
        tasks.addAll(planGenerationOfTypeByHolder(holder, reader, alreadyPlannedBuilders, tracker));
      } catch (BuilderException ex) {
        context.reportBasedOnStrictMode(
            holder, "simple-builders: Failed to generate builder - %s", ex.getMessage());
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
    alreadyPlannedBuilders.add(
        builderTypeName(
            annotatedElement, effectiveBuilderPackage(annotatedElement, config), config));
    return Optional.of(new CodegenTask<>(annotatedElement, config, annotatedElement));
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
            "simple-builders: @SimpleBuilderFor on '%s' resolves no types - nothing to generate",
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
          "simple-builders: @SimpleBuilderFor on '%s' generates builder for '%s' in package '%s', which is outside builderGenerationPackages - the explicit declaration takes precedence",
          holder.getSimpleName(),
          target.getQualifiedName(),
          builderPackage);
    }
    TypeName builderName = builderTypeName(target, builderPackage, config);
    if (!alreadyPlannedBuilders.add(builderName)) {
      context.warning(
          holder,
          "simple-builders: skipping '%s' declared in @SimpleBuilderFor on '%s' - builder '%s' is already generated elsewhere",
          target.getQualifiedName(),
          holder.getSimpleName(),
          builderName.getFullQualifiedName());
      return Optional.empty();
    }
    return Optional.of(new CodegenTask<>(target, config, holder));
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
    for (Object item :
        JavaLangAnalyser.findAnnotationAttributeValues(annotationInstance, "value", context)) {
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
    return JavaLangAnalyser.findAnnotationAttributeValues(
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
          "simple-builders: package '%s' in @SimpleBuilderFor on '%s' could not be resolved - skipped",
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
            "simple-builders: skipping %s '%s' in package '%s' declared by @SimpleBuilderFor on '%s' - no builder can be generated",
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
   * Registers the types whose builders will be generated this round with the scope resolver, so it
   * can trust them without a type search. The actual builder type name is registered, which may
   * differ from the target's package for {@code @SimpleBuilderFor} targets.
   */
  @Override
  protected TypeName predictedTypeName(
      CodegenTask<BuilderConfiguration> task, BuilderProcessingContext ctx) {
    if (!(task.element() instanceof TypeElement targetType)) {
      return null;
    }
    return builderTypeName(
        targetType,
        effectiveBuilderPackage(task.reportingElement(), task.configuration()),
        task.configuration());
  }

  /** SPI-exports the registered builder, on top of the generic generated-type registration. */
  @Override
  protected void registerPlannedOutput(
      CodegenTask<BuilderConfiguration> task, BuilderProcessingContext ctx) {
    super.registerPlannedOutput(task, ctx);
    if (task.element() instanceof TypeElement targetType) {
      ctx.getBuilderScopeResolver()
          .resolveGeneratedBuilder(targetType)
          .ifPresent(
              resolvedBuilder ->
                  SimpleBuildersSpiIntegration.registerBuilder(
                      JavaLangMapper.mapToTypeName(targetType, ctx),
                      resolvedBuilder,
                      task.configuration().getSetterPrefix()));
    }
  }

  @Override
  protected BuilderDefinitionDto analyse(
      CodegenTask<BuilderConfiguration> task, BuilderProcessingContext ctx)
      throws BuilderException {
    ctx.initProcessingTarget(new ProcessingTarget<>(task.configuration(), builderPackageOf(task)));
    PerformanceTracker tracker = ctx.getPerformanceTracker();
    tracker.startPhase();
    BuilderDefinitionDto builderDef = extractFromElement(task.element(), ctx);
    tracker.endPhase(PHASE_BUILDER_DEFINITION_EXTRACTION);
    return builderDef;
  }

  @Override
  protected List<GenerationTargetClassDto> mapToTargets(
      BuilderDefinitionDto definition,
      CodegenTask<BuilderConfiguration> task,
      BuilderProcessingContext ctx) {
    PerformanceTracker tracker = ctx.getPerformanceTracker();
    tracker.startPhase();
    GenerationTargetClassDto renderingDto =
        new BuilderToGenerationTypeMapper(task.configuration()).toRenderingDto(definition);
    tracker.endPhase(PHASE_DTO_MAPPING);
    return List.of(renderingDto);
  }

  @Override
  protected void afterGenerate(
      BuilderDefinitionDto definition,
      List<GenerationTargetClassDto> targets,
      CodegenTask<BuilderConfiguration> task,
      BuilderProcessingContext ctx) {
    GenerationTargetClassDto renderingDto = targets.get(0);
    int collectionCount =
        (int)
            definition.getAllFieldsForBuilder().stream()
                .filter(
                    f ->
                        f.getFieldType() instanceof TypeNameList
                            || f.getFieldType() instanceof TypeNameSet
                            || f.getFieldType() instanceof TypeNameMap)
                .count();

    // Collect info for Jackson Module if enabled
    jacksonModuleGenerator.addEntry(definition, task.element());
    ctx.debug("Jackson module entry added");

    // End class-level timing (started by the driver before analysis)
    ctx.getPerformanceTracker()
        .endClass(definition.getAllFieldsForBuilder().size(), collectionCount);

    // Add summary of what was generated
    ctx.debugEndOperation(
        "Generated builder with %d fields and %d methods for %s",
        definition.getAllFieldsForBuilder().size(),
        renderingDto.getMethods().size(),
        definition.getBuilderTypeName().getClassName());
  }

  /**
   * The package the builder is generated into: the configured {@code packageName} option when set,
   * otherwise the package of the element the generation is reported on - the
   * {@code @SimpleBuilderFor} holder for external types, the annotated type itself otherwise.
   */
  private String builderPackageOf(CodegenTask<BuilderConfiguration> task) {
    return effectiveBuilderPackage(task.reportingElement(), task.configuration());
  }

  /**
   * The effective builder package: the configured {@code packageName} option when set, otherwise
   * the package of the given element.
   */
  private String effectiveBuilderPackage(Element reportingElement, BuilderConfiguration config) {
    String configured = config.getPackageName();
    return configured == null ? context.getPackageName(reportingElement) : configured;
  }

  @Override
  protected List<GenerationTargetClassDto> deferredTargets(BuilderProcessingContext ctx) {
    return jacksonModuleGenerator.getModuleDefinitions();
  }

  @Override
  protected String deferredTargetsKind() {
    return "Jackson module";
  }

  /** Generates all Jackson modules after the last processing round and reports the metrics. */
  @Override
  protected void onProcessingOver(BuilderProcessingContext ctx) {
    SimpleBuildersSpiIntegration.finishCompilation();
    ctx.getPerformanceTracker().generateReport(logger);
    super.onProcessingOver(ctx);
    // Reset indentation after Jackson module generation as well
    ctx.resetIndentation();
  }

  @Override
  protected void endRound(BuilderProcessingContext ctx) {
    // The generating round is done — the registry is final for SPI adapters from here on.
    SimpleBuildersSpiIntegration.finishCompilation();
  }
}
