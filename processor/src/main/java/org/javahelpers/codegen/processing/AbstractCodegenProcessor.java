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

package org.javahelpers.codegen.processing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.classgen.ClassCodeGenerator;
import org.javahelpers.simple.builders.processor.classgen.CodeGenerationException;
import org.javahelpers.simple.builders.processor.classgen.GenerationEnvironment;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.AnnotationProcessingContext;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Driver of a generic annotation-processing pipeline: the framework owns the processing lifecycle,
 * the domain plugs in its hooks — collect, plan, register, analyse/map, generate, finalize.
 *
 * <p>A new processor declares its static surface once as {@link CodegenProcessorConfiguration}
 * (passed to the constructor — element spec, options, source level) and implements only the domain
 * hooks marked {@code [implement]}: {@link #createContext}, {@link #planTasks}, {@link
 * #registerPlannedGenerationTargets}, {@link #analyse} and {@link #mapToTargetSpecification}. Every
 * other step is framework-owned or an optional {@code [default]} the domain may override.
 *
 * <p>The round spine:
 *
 * <pre>
 *   ctx      = createContext(env, reader, logger)                       [implement — in init]
 *   elements = findElements(annotations, roundEnv, ctx)                 [default: configuration's elementSpec]
 *   tasks    = planTasks(elements, ctx)                                 [implement]
 *   registerPlannedTaskResults(tasks, ctx)                                  [framework; per-task names from registerPlannedGenerationTargets() — implement]
 *   loop 1 — specification (guarded, strict-mode reporting):            [framework]
 *     defs    = analyse(task, ctx)                                      [implement]
 *     target  = mapToTargetSpecification(def, cfg, ctx)  (per def)      [implement]
 *   loop 2 — generation:                                                [framework]
 *     for each target:
 *       existsGuard → engine.generate(target, formatterResolver.resolve(target.mode))  [framework]
 *     onTaskGenerated(def, target, task, ctx)                          [default: no-op]
 *   endRound(ctx)                                                       [default: no-op]
 *   last round: onProcessingOver(ctx)                                   [default: drains context.getDeferredTargets()]
 * </pre>
 *
 * <p>Per-element failures are caught and reported through {@link
 * AnnotationProcessingContext#reportBasedOnStrictMode}, so one broken element does not abort the
 * round; in strict mode they become errors.
 *
 * @param <C> the domain's processing context type
 * @param <K> the domain's configuration type carried by {@link CodegenTask}
 * @param <D> the domain's definition type — the analysis result mapped to render targets
 */
public abstract class AbstractCodegenProcessor<
        C extends AnnotationProcessingContext,
        K extends CodegenConfiguration,
        D extends CodegenDefinition>
    extends AbstractProcessor {

  /** Performance phase the element collection is reported under — framework-owned. */

  /** Performance phase target generation is reported under — framework-owned. */

  /** Default performance phase for {@link #analyse} — domains may name their own. */
  private static final String PHASE_ANALYSIS = "Analysis";

  /**
   * Default performance phase for {@link #mapToTargetSpecification} — domains may name their own.
   */
  private static final String PHASE_TARGET_MAPPING = "Target Mapping";

  /** The domain's context instance. */
  protected C context;

  /** Logger for the domain's hooks (init, last-round reports). */
  protected ProcessingLogger logger;

  private final CodegenProcessorConfiguration processorConfiguration;
  private final CodegenBackendRegistry backendRegistry = new CodegenBackendRegistry();
  private ClassCodeGenerator codeGenerator;
  private SourceFormatterResolver formatterResolver;
  private boolean supportedJdk = true;
  private boolean processedOnce;

  /**
   * Creates the processor on its static configuration. javac still instantiates the concrete
   * subclass through its no-arg constructor, which hands the configuration to this constructor.
   *
   * @param processorConfiguration the processor's static declarations
   */
  protected AbstractCodegenProcessor(CodegenProcessorConfiguration processorConfiguration) {
    this.processorConfiguration = processorConfiguration;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    CompilerArgumentsReader reader = new CompilerArgumentsReader(processingEnv);
    this.logger = createLogger(processingEnv, reader);
    this.context = createContext(processingEnv, reader, logger);
    this.codeGenerator = createGenerator(processingEnv, logger, context);
    this.formatterResolver = createFormatterResolver(reader, logger);

    SourceVersion current = processingEnv.getSourceVersion();
    this.supportedJdk = isAtLeast(current, requiredSourceVersion());
    if (!this.supportedJdk) {
      reportUnsupportedJdk(current);
    }
  }

  /** Reports that the running JDK cannot meet the required source level. */
  private void reportUnsupportedJdk(SourceVersion current) {
    String releaseNumber = requiredSourceVersion().replace("RELEASE_", "");
    context.error(
        "%s requires Java %s or higher for annotation processing. Detected: '%s'. Please upgrade to JDK %s+ or disable the processor.",
        logger.getTag(), releaseNumber, current, releaseNumber);
  }

  /**
   * Runs one processing round — see the class javadoc for the spine. Claims the supported
   * annotations only when {@link #claimsAnnotations(Set)} says so — under the default {@code "*"}
   * set it does not claim, because claiming would hide foreign annotations from later processors
   * (hence the {@code java:S3516} suppression).
   */
  @Override
  @SuppressWarnings("java:S3516")
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!supportedJdk) {
      // Fail fast: init() already emitted the error; do not attempt any processing.
      return false;
    }
    if (roundEnv.processingOver()) {
      onProcessingOver(context);
      return claimsAnnotations(getSupportedAnnotationTypes());
    }

    PerformanceTracker tracker = context.getPerformanceTracker();
    logger.infoTagged("PROCESSING ROUND START");

    tracker.startPhase();
    ElementCollection collected = findElements(annotations, roundEnv, context);
    tracker.endPhase("Element Collection");
    logger.debugTagged("Processing round started.");

    List<CodegenTask<K>> tasks = plan(collected, context);
    if (tasks.isEmpty()) {
      return claimsAnnotations(getSupportedAnnotationTypes());
    }

    // First loop: specification — analyse every task and map its render targets.
    List<PlannedWork<K, D>> planned = new ArrayList<>();
    for (CodegenTask<K> task : tasks) {
      context.debugStartOperation("Processing element: " + task.element().getSimpleName());
      tracker.startClass(task.element().getSimpleName().toString());
      try {
        context.initProcessingTarget(
            new ProcessingTarget<>(task.configuration(), task.outputPackage()));
        tracker.startPhase();
        List<D> definitions;
        try {
          definitions = analyse(task, context);
        } finally {
          tracker.endPhase("Analysis");
        }
        for (D definition : definitions) {
          tracker.startPhase();
          GenerationTargetClassDto target;
          try {
            target = mapToTargetSpecification(definition, task.configuration(), context);
          } finally {
            tracker.endPhase("Target Mapping");
          }
          planned.add(new PlannedWork<>(task, definition, target));
        }
      } catch (Exception ex) {
        // By default generation failures are warnings so other targets are still generated.
        // In opt-in strict mode they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            task.reportingElement(),
            logger.prefixed("Failed to generate target - %s"),
            ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }

    // Second loop: generation — every specification exists before any file is written.
    int successfulGenerations = 0;
    for (PlannedWork<K, D> work : planned) {
      context.debugStartOperation(
          "Generating targets for: " + work.task().element().getSimpleName());
      try {
        context.initProcessingTarget(
            new ProcessingTarget<>(work.task().configuration(), work.task().outputPackage()));
        tracker.startPhase();
        generateTarget(work.target());
        tracker.endPhase("Code Generation");
        onTaskGenerated(work.definition(), work.target(), work.task(), context);
        successfulGenerations++;
      } catch (Exception ex) {
        context.reportBasedOnStrictMode(
            work.task().reportingElement(),
            logger.prefixed("Failed to generate target - %s"),
            ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }

    if (successfulGenerations > 0) {
      logger.infoTagged(
          "Successfully generated %d target(s) in this processing round", successfulGenerations);
    }

    // Reset indentation level at the end of each processing round to prevent cascading errors
    context.resetIndentation();
    endRound(context);
    return claimsAnnotations(getSupportedAnnotationTypes());
  }

  @Override
  public final Set<String> getSupportedOptions() {
    Set<String> options = new LinkedHashSet<>(optionRegistry().supportedOptions());
    options.addAll(additionalSupportedOptions());
    return options;
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return supportedAnnotationTypesByAnnotation().orElse(Set.of("*"));
  }

  /**
   * The supported annotation type names declared by the {@link SupportedAnnotationTypes} annotation
   * on the processor class. The element spec's annotation types are for collection filtering — not
   * the supported-set declaration — so without the annotation the supported set stays {@code "*"}.
   *
   * @return the declared names, or empty when the class is not annotated
   */
  protected Optional<Set<String>> supportedAnnotationTypesByAnnotation() {
    SupportedAnnotationTypes annotation = getClass().getAnnotation(SupportedAnnotationTypes.class);
    return annotation != null
        ? Optional.of(Set.copyOf(List.of(annotation.value())))
        : Optional.empty();
  }

  /**
   * Additional supported option names beyond the declared registry — for wrapper or embedder
   * processors adding their own options without redeclaring the domain's set (the seam MapStruct
   * uses for its processor wrappers). Default: none.
   *
   * @return extra option names
   */
  protected Set<String> additionalSupportedOptions() {
    return Set.of();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Generates one target class through the configured engine after checking the target does not
   * already exist. Also usable by the domain's {@link #onProcessingOver} implementation for
   * deferred outputs.
   *
   * @param target the class definition to generate
   * @throws CodeGenerationException if the target exists or generation fails
   */
  protected final void generateTarget(GenerationTargetClassDto target)
      throws CodeGenerationException {
    assertTargetDoesNotExist(target);
    codeGenerator.generateClass(target, formatterResolver.resolve(target.getFormattingMode()));
  }

  /**
   * Fails the build with a descriptive error when the class to be generated already exists (a
   * manually written class or a leftover from a previous compilation).
   *
   * @param target the class definition to check
   * @throws CodeGenerationException if a class with the target qualified name already exists
   */
  private void assertTargetDoesNotExist(GenerationTargetClassDto target)
      throws CodeGenerationException {
    String qualifiedName = target.getTypeName().getFullQualifiedName();
    boolean exists;
    try {
      exists = context.getTypeElement(qualifiedName) != null;
    } catch (Exception e) {
      context.debug(
          "Error checking if class '%s' already exists: %s",
          qualifiedName, e.getMessage() != null ? e.getMessage() : "No message");
      exists = false;
    }
    if (exists) {
      throw new CodeGenerationException(
          """
              Target class '%s' already exists. This may be a manually written target or a previously generated target.
              To resolve this:
              1. If you have a manual target, consider renaming it or removing the annotation triggering generation
              2. If this is from a previous compilation, clean and rebuild the project
              3. Check that you're not trying to generate multiple targets for the same target type
              """
              .formatted(qualifiedName));
    }
  }

  /**
   * The diagnostic prefix the processor's tagged messages carry — see {@link
   * ProcessingLogger#prefixed}. Default: the processor's simple class name; override for a shorter
   * domain prefix.
   *
   * @return the logger prefix
   */
  protected String provideLoggerPrefix() {
    return getClass().getSimpleName();
  }

  /**
   * Creates the logger: verbose when the domain's declared verbose option requests it — see {@link
   * ProcessorOptionRegistry#isVerboseRequested} — and carrying {@link #provideLoggerPrefix()}.
   */
  private ProcessingLogger createLogger(
      ProcessingEnvironment processingEnv, CompilerArgumentsReader reader) {
    String loggerPrefix = provideLoggerPrefix();
    ProcessingLogger logger =
        optionRegistry().isVerboseRequested(reader)
            ? ProcessingLogger.createVerbose(processingEnv, loggerPrefix)
            : ProcessingLogger.create(processingEnv, loggerPrefix);
    logger.debug("Starting %s...", getClass().getSimpleName());
    return logger;
  }

  /** The backend registry resolving the code-generation engine and formatter factory. */
  protected CodegenBackendRegistry backendRegistry() {
    return backendRegistry;
  }

  /**
   * Creates the domain's processing context.
   *
   * @param processingEnv the processing environment
   * @param reader compiler-argument access — global configuration typically comes from here
   * @param logger logger for output
   * @return the domain's context instance
   */
  protected abstract C createContext(
      ProcessingEnvironment processingEnv, CompilerArgumentsReader reader, ProcessingLogger logger);

  /**
   * Creates the code generation engine — the {@link ClassCodeGenerator} backend port. Default: the
   * engine discovered by {@link CodegenBackendRegistry} over a {@link GenerationEnvironment} wired
   * from the processing environment and context.
   */
  protected ClassCodeGenerator createGenerator(
      ProcessingEnvironment processingEnv, ProcessingLogger logger, C context) {
    return backendRegistry.resolveCodeGenerator(
        new GenerationEnvironment(
            processingEnv,
            logger,
            context.getPerformanceTracker(),
            context::reportBasedOnStrictMode));
  }

  /**
   * Creates the source formatter resolver for the configured profile. Default: the profile from
   * {@link ProcessorOptionRegistry#formatterProfile} and the factory discovered by {@link
   * CodegenBackendRegistry}.
   */
  protected SourceFormatterResolver createFormatterResolver(
      CompilerArgumentsReader reader, ProcessingLogger logger) {
    return new SourceFormatterResolver(
        logger,
        optionRegistry().formatterProfile(reader),
        backendRegistry.resolveFormatterFactory());
  }

  private AnnotationElementSpec elementSpec() {
    return processorConfiguration.elementSpec();
  }

  private ProcessorOptionRegistry optionRegistry() {
    return processorConfiguration.optionRegistry();
  }

  /**
   * Whether {@link #process} claims the supported annotations. Default {@code false} — claiming
   * would hide them from later processors, which is especially wrong under the default {@code "*"}
   * supported set. Override to claim when the processor owns a concrete annotation set.
   *
   * @param supportedTypes the supported annotation type names the round was processed for
   * @return {@code true} to claim the supported annotations
   */
  protected boolean claimsAnnotations(Set<String> supportedTypes) {
    return false;
  }

  private String requiredSourceVersion() {
    return processorConfiguration.requiredSourceVersion();
  }

  /**
   * Whether {@code current} meets the named minimum. The name resolves inside the check so a JDK
   * whose {@link SourceVersion} lacks the constant (e.g. a processor built on a newer JDK running
   * under an older one) reports the graceful error path instead of a linkage failure.
   */
  private boolean isAtLeast(SourceVersion current, String requiredName) {
    try {
      return current.ordinal() >= SourceVersion.valueOf(requiredName).ordinal();
    } catch (IllegalArgumentException ex) {
      logger.debug(
          "Required source version name '%s' is unknown to this JDK — treating as unsupported",
          requiredName);
      return false;
    }
  }

  /**
   * Finds the elements of this round the domain generates code for. Default delegates to the
   * configured {@link CodegenProcessorConfiguration#elementSpec}'s collector; override for custom
   * element discovery.
   *
   * @param annotations the annotation types requested this round
   * @param roundEnv the round environment
   * @param context the processing context
   * @return the collected elements
   */
  protected ElementCollection findElements(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, C context) {
    return elementSpec().collect(annotations, roundEnv, logger);
  }

  /**
   * The planning step — runs the domain's {@link #planTasks}, applies the finished gate and the
   * one-shot marker, and registers the round's planned outputs. Framework bookkeeping lives here so
   * the domain function below stays pure planning.
   *
   * @param collected the elements matched by {@link #findElements}
   * @param context the processing context
   * @return the round's tasks, or empty when the round is gated out
   */
  private List<CodegenTask<K>> plan(ElementCollection collected, C context) {
    List<CodegenTask<K>> tasks = planTasks(collected, context);
    if (collected.isEmpty() || isFinished(context)) {
      return List.of();
    }
    if (!tasks.isEmpty()) {
      // The default one-shot is consumed by the first round that planned work — an empty round
      // leaves it open for a later one carrying matching elements.
      processedOnce = true;
    }
    registerPlannedTaskResults(tasks, context);
    return tasks;
  }

  /**
   * Plans the tasks of this round from the collected elements: resolves per-element configuration,
   * expands multi-target declarations and applies domain scoping rules.
   *
   * @param collected the elements matched by {@link #findElements}
   * @param context the processing context
   * @return the generation plan, possibly empty
   */
  protected abstract List<CodegenTask<K>> planTasks(ElementCollection collected, C context);

  /**
   * Registers the types this round will generate, up front, so references to them resolve during
   * analysis. Default: clears {@link AnnotationProcessingContext#getGeneratedTypes()} and registers
   * every name each task's {@link #registerPlannedGenerationTargets} predicts — a task may produce
   * several generated types. Each registration fires {@link #onPlannedOutputRegistered} for domain
   * extras (e.g. SPI publication); override this whole method only for a different shape.
   *
   * @param tasks the planned tasks
   * @param context the processing context
   */
  protected void registerPlannedTaskResults(List<CodegenTask<K>> tasks, C context) {
    context.getGeneratedTypes().clear();
    for (CodegenTask<K> task : tasks) {
      if (!(task.element() instanceof TypeElement targetType)) {
        continue;
      }
      TypeName elementName = JavaLangMapper.mapToTypeName(targetType, context);
      for (TypeName generated : registerPlannedGenerationTargets(task, context)) {
        context.getGeneratedTypes().add(elementName, generated);
        onPlannedOutputRegistered(task, generated, context);
      }
    }
  }

  /**
   * The names of the types this task will generate — the domain's task → result type-name
   * predictions, evaluated at registration time. Keep this pure: registrations and their side
   * effects belong to {@link #onPlannedOutputRegistered}.
   *
   * @param task the planned task
   * @param context the processing context
   * @return the generated type names (empty when the task registers nothing)
   */
  protected abstract List<TypeName> registerPlannedGenerationTargets(
      CodegenTask<K> task, C context);

  /**
   * Runs after a predicted type name is registered for a task — the extension point for domain
   * extras on registration (e.g. SPI publication). Default: nothing.
   *
   * @param task the planned task
   * @param generatedName the type name just registered for it
   * @param context the processing context
   */
  protected void onPlannedOutputRegistered(
      CodegenTask<K> task, TypeName generatedName, C context) {}

  /**
   * Whether the domain's generating round already ran. Default: one-shot — the base marks the
   * processor finished after the first round that planned tasks, so elements first appearing in a
   * later round get nothing. Override for a custom lifecycle rule (e.g. a cross-compilation
   * finished-marker).
   *
   * @param context the processing context
   * @return {@code true} to skip this round
   */
  protected boolean isFinished(C context) {
    return processedOnce;
  }

  /**
   * Analyses one planned task into its domain definitions — the analysis half of the
   * analysis/generation separation. A task may resolve to several definitions (e.g. a repeatable
   * annotation on one element).
   *
   * @param task the planned task
   * @param context the processing context
   * @return the domain definitions, possibly empty
   * @throws Exception when analysis fails; reported as a per-element failure
   */
  protected abstract List<D> analyse(CodegenTask<K> task, C context) throws Exception;

  /**
   * Maps a domain definition to its render target — the generation half of the analysis/generation
   * separation, separated by the {@link GenerationTargetClassDto} boundary. One definition
   * specifies exactly one target.
   *
   * @param definition the analysed definition
   * @param configuration the configuration the task resolved
   * @param context the processing context
   * @return the class definition to generate
   * @throws Exception when mapping fails; reported as a per-element failure
   */
  protected abstract GenerationTargetClassDto mapToTargetSpecification(
      D definition, K configuration, C context) throws Exception;

  /**
   * Event after a task's target generated successfully — the insert point for deferred-output
   * accumulation, per-class metrics, and domain bookkeeping. Default: nothing.
   *
   * @param definition the analysed definition
   * @param target the generated class definition
   * @param task the planned task
   * @param context the processing context
   */
  protected void onTaskGenerated(
      D definition, GenerationTargetClassDto target, CodegenTask<K> task, C context) {}

  /**
   * Callback for the last processing round — generates every target the domain pushed onto the
   * context through {@link AnnotationProcessingContext#addDeferredTarget} via the same guarded path
   * as round targets. Domain extensions log and push their deferred outputs, then call {@code
   * super.onProcessingOver(context)} to drain, wrapped in their finalization (SPI flush, reports).
   *
   * @param context the processing context
   */
  protected void onProcessingOver(C context) {
    for (GenerationTargetClassDto target : context.getDeferredTargets()) {
      try {
        generateTarget(target);
      } catch (Exception e) {
        context.reportBasedOnStrictMode(
            logger.prefixed("Error generating target for package %s: %s"),
            target.getTypeName().getPackageName(),
            e.getMessage());
      }
    }
  }

  /** Callback at the end of a generating round — domain finalization. */
  protected void endRound(C context) {}

  /** One task's specification output, carried from the analysis loop into the generation loop. */
  private record PlannedWork<K extends CodegenConfiguration, D>(
      CodegenTask<K> task, D definition, GenerationTargetClassDto target) {}
}
