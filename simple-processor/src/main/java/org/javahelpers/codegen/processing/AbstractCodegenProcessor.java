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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.javahelpers.codegen.analysis.JavaLangMapper;
import org.javahelpers.codegen.ClassCodeGenerator;
import org.javahelpers.codegen.CodeGenerationException;
import org.javahelpers.codegen.GenerationEnvironment;
import org.javahelpers.codegen.roaster.RoasterCodeGenerator;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.processing.AnnotationProcessingContext;
import org.javahelpers.codegen.processing.CompilerArgumentsReader;
import org.javahelpers.codegen.processing.SourceFormatterResolver;
import org.javahelpers.codegen.logging.PerformanceTracker;
import org.javahelpers.codegen.logging.ProcessingLogger;

/**
 * Driver of a generic annotation-processing pipeline: the framework owns the processing lifecycle,
 * the domain plugs in its hooks — collect, plan, register, analyse/map, generate, finalize.
 *
 * <p>A new processor declares its static surface once as {@link CodegenProcessorConfiguration}
 * (passed to the constructor — element collector, options, source level, formatter factory) and
 * implements only the domain hooks marked {@code [implement]}: {@link #createContext}, {@link
 * #planTasks}, {@link #analyse} and {@link #mapToTargetSpecification}. Every other step is
 * framework-owned or an optional {@code [default]} the domain may override.
 *
 * <p>The round spine:
 *
 * <pre>
 *   ctx      = createContext(env, reader, logger)                       [implement — in init]
 *   onRoundStart(ctx)                                                 [default: no-op]
 *   elements = findElements(annotations, roundEnv)                 [default: configuration's elementCollector]
 *   skipsRound(elements, ctx)                                  [framework; empty or finished gate]
 *   tasks    = planTasks(elements, ctx)                                 [implement]
 *   onTasksPlanned(tasks, ctx)                                          [default: no-op]
 *   registerPlannedTaskResults(tasks, ctx)                                  [framework; per-task names from predictTaskResultTypeNames() — default: empty]
 *   loop 1 — specification (guarded, strict-mode reporting):            [framework]
 *     defs    = analyse(task, ctx)                                      [implement]
 *     target  = mapToTargetSpecification(def, cfg, ctx)  (per def)      [implement]
 *   loop 2 — generation:                                                [framework]
 *     for each target:
 *       existsGuard → engine.generate(target, formatterResolver.resolve(target.mode))  [framework]
 *     onTaskGenerated(generation, ctx)                              [default: no-op]
 *   endRound(ctx)                                                       [default: no-op]
 *   last round: onProcessingOver(ctx)                                   [default: no-op]
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

  /** The domain's context instance. */
  protected C context;

  /** Logger for the domain's hooks (init, last-round reports). */
  protected ProcessingLogger logger;

  private final CodegenProcessorConfiguration processorConfiguration;
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

    this.supportedJdk = verifySupportedJdk();
  }

  /**
   * Checks the running JDK against {@link #requiredSourceVersion} and reports a descriptive error
   * when it cannot meet the requirement.
   *
   * @return {@code true} when the running JDK supports the required source level
   */
  private boolean verifySupportedJdk() {
    if (requiredSourceVersion() == null) {
      return true;
    }
    SourceVersion current = processingEnv.getSourceVersion();
    boolean supported = isAtLeast(current, requiredSourceVersion());
    if (!supported) {
      String releaseNumber = requiredSourceVersion().replace("RELEASE_", "");
      context.error(
          "requires Java %s or higher for annotation processing. Detected: '%s'. Please upgrade to JDK %s+ or disable the processor.",
          releaseNumber, current, releaseNumber);
    }
    return supported;
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
    logger.info("PROCESSING ROUND START");
    onRoundStart(context);

    tracker.startPhase();
    ElementGroups collected = findElements(annotations, roundEnv);
    tracker.endPhase("Element Collection");
    logger.debug("Processing round started.");

    if (skipsRound(collected, context)) {
      return claimsAnnotations(getSupportedAnnotationTypes());
    }

    List<CodegenTask<K>> tasks = plan(collected, context);
    onTasksPlanned(tasks, context);

    // First loop: specification — analyse every task and map its render targets.
    List<PlannedGeneration<K, D>> planned = new ArrayList<>();
    for (CodegenTask<K> task : tasks) {
      context.debugStartOperation("Processing element: " + task.element().getSimpleName());
      tracker.startClass(task.element().getSimpleName().toString());
      try {
        context.setCurrentTask(task);
        planned.addAll(specify(task, context, tracker));
      } catch (Exception ex) {
        // By default generation failures are warnings so other targets are still generated.
        // In opt-in strict mode they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            task.reportingElement(), "Failed to generate target - %s", ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }

    // Second loop: generation — every specification exists before any file is written.
    int successfulGenerations = 0;
    for (PlannedGeneration<K, D> work : planned) {
      // generateTarget leaves its generation-scope operation open so onTaskGenerated can close it
      // with a summary line — restore the pre-task indentation whatever the domain did.
      int preTaskIndentation = logger.getCurrentIndentationLevel();
      context.debugStartOperation(
          "Generating targets for: " + work.task().element().getSimpleName());
      try {
        context.setCurrentTask(work.task());
        tracker.startPhase();
        generateTarget(work.target());
        tracker.endPhase("Code Generation");
        onTaskGenerated(work, context);
        successfulGenerations++;
      } catch (Exception ex) {
        context.reportBasedOnStrictMode(
            work.task().reportingElement(), "Failed to generate target - %s", ex.getMessage());
      } finally {
        while (logger.getCurrentIndentationLevel() > preTaskIndentation) {
          context.debugEndOperation();
        }
      }
    }

    if (successfulGenerations > 0) {
      logger.info(
          "Successfully generated %d target(s) in this processing round", successfulGenerations);
    }

    // Reset indentation level at the end of each processing round to prevent cascading errors
    context.resetIndentation();
    endRound(context);
    return claimsAnnotations(getSupportedAnnotationTypes());
  }

  /**
   * Analyses one task and maps every resulting definition to its render target, tracking both
   * phases on the class-level timer the caller started. Runs inside the task's debug scope.
   *
   * @param task the task to specify
   * @param context the processing context
   * @param tracker the round's performance tracker
   * @return the planned generations of this task (possibly empty)
   * @throws Exception forwarded from the domain's {@link #analyse}/{@link
   *     #mapToTargetSpecification}
   */
  private List<PlannedGeneration<K, D>> specify(
      CodegenTask<K> task, C context, PerformanceTracker tracker) throws Exception {
    tracker.startPhase();
    List<D> definitions;
    try {
      definitions = analyse(task, context);
    } finally {
      tracker.endPhase("Analysis");
    }
    if (definitions.isEmpty()) {
      context.debug("Analysis produced no targets for " + task.element().getSimpleName());
    }
    List<PlannedGeneration<K, D>> planned = new ArrayList<>();
    for (D definition : definitions) {
      tracker.startPhase();
      GenerationTargetClassDto target;
      try {
        target = mapToTargetSpecification(definition, task.configuration(), context);
      } finally {
        tracker.endPhase("Target Mapping");
      }
      planned.add(new PlannedGeneration<>(task, definition, target));
    }
    return planned;
  }

  @Override
  public final Set<String> getSupportedOptions() {
    return optionRegistry().supportedOptions();
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return supportedAnnotationTypesByAnnotation().orElse(Set.of("*"));
  }

  /**
   * The supported annotation type names declared by the {@link SupportedAnnotationTypes} annotation
   * on the processor class. The element collector's annotation types are for collection filtering —
   * not the supported-set declaration — so without the annotation the supported set stays {@code
   * "*"}.
   *
   * @return the declared names, or empty when the class is not annotated
   */
  protected Optional<Set<String>> supportedAnnotationTypesByAnnotation() {
    SupportedAnnotationTypes annotation = getClass().getAnnotation(SupportedAnnotationTypes.class);
    return annotation != null
        ? Optional.of(Set.copyOf(List.of(annotation.value())))
        : Optional.empty();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Generates one target class through the configured engine after checking the target does not
   * already exist. Also usable by the domain's {@link #onProcessingOver} implementation for
   * last-round outputs.
   *
   * <p>The engine leaves its generation-scope debug operation open so the caller can close it with
   * a summary line (see {@link #onTaskGenerated}); callers outside the task driver must close it
   * with {@code context.debugEndOperation()}.
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
   * Creates the logger: verbose when the domain's declared verbose option requests it — see {@link
   * ProcessorOptionRegistry#isVerboseRequested} — and carrying the prefix from {@link
   * CodegenProcessorConfiguration#loggerPrefix()}, or the processor's simple class name when unset.
   */
  private ProcessingLogger createLogger(
      ProcessingEnvironment processingEnv, CompilerArgumentsReader reader) {
    String configuredPrefix = processorConfiguration.loggerPrefix();
    String loggerPrefix = configuredPrefix != null ? configuredPrefix : getClass().getSimpleName();
    ProcessingLogger processingLogger =
        optionRegistry().isVerboseRequested(reader)
            ? ProcessingLogger.createVerbose(processingEnv, loggerPrefix)
            : ProcessingLogger.create(processingEnv, loggerPrefix);
    processingLogger.debug("Starting %s...", getClass().getSimpleName());
    return processingLogger;
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
   * Creates the code generation engine — the {@link ClassCodeGenerator} backend port the domain
   * wires over {@link #generationEnvironment}. Default: the Roaster backend; override to plug in a
   * different engine.
   *
   * @param processingEnv the processing environment
   * @param logger the processing logger
   * @param context the processing context
   * @return the domain's code generation engine
   */
  protected ClassCodeGenerator createGenerator(
      ProcessingEnvironment processingEnv, ProcessingLogger logger, C context) {
    return new RoasterCodeGenerator(generationEnvironment(processingEnv, logger, context));
  }

  /**
   * The {@link GenerationEnvironment} the engine runs against — processing environment, logger,
   * performance tracker and the strict-mode reporter, wired from the context the driver created.
   *
   * @param processingEnv the processing environment
   * @param logger the processing logger
   * @param context the processing context
   * @return the generation environment for the engine's constructor
   */
  protected GenerationEnvironment generationEnvironment(
      ProcessingEnvironment processingEnv, ProcessingLogger logger, C context) {
    return new GenerationEnvironment(
        processingEnv, logger, context.getPerformanceTracker(), context::reportBasedOnStrictMode);
  }

  /**
   * Creates the source formatter resolver for the configured profile — the profile name comes from
   * the processor's declared options.
   *
   * @param reader the compiler arguments reader
   * @param logger the processing logger
   * @return the formatter resolver
   */
  protected SourceFormatterResolver createFormatterResolver(
      CompilerArgumentsReader reader, ProcessingLogger logger) {
    return new SourceFormatterResolver(logger, optionRegistry().formatterProfile(reader));
  }

  private ElementCollector elementCollector() {
    return processorConfiguration.elementCollector();
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
   * configured {@link CodegenProcessorConfiguration#elementCollector}; override for custom element
   * discovery.
   *
   * @param annotations the annotation types requested this round
   * @param roundEnv the round environment
   * @return the collected elements
   */
  protected ElementGroups findElements(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    return elementCollector().collect(annotations, roundEnv, logger);
  }

  private boolean skipsRound(ElementGroups collected, C context) {
    if (collected.isEmpty()) {
      context.debug("No matching elements collected — skipping planning for this round.");
      return true;
    }
    if (isFinished(context)) {
      context.debug("Processing is already finished — skipping planning for this round.");
      return true;
    }
    return false;
  }

  /**
   * The planning step — runs the domain's {@link #planTasks}, applies the one-shot marker, and
   * registers the round's planned outputs. Framework bookkeeping lives here so the domain function
   * below stays pure planning.
   *
   * @param collected the elements matched by {@link #findElements}
   * @param context the processing context
   * @return the round's tasks, possibly empty when there is nothing to generate
   */
  private List<CodegenTask<K>> plan(ElementGroups collected, C context) {
    List<CodegenTask<K>> tasks = planTasks(collected, context);
    if (tasks.isEmpty()) {
      context.debug("Planning produced no tasks — nothing to generate this round.");
    } else {
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
  protected abstract List<CodegenTask<K>> planTasks(ElementGroups collected, C context);

  /**
   * Registers the types this round will generate, up front, so references to them resolve during
   * analysis. Default: clears {@link AnnotationProcessingContext#getGeneratedTypes()} and registers
   * every name each task's {@link #predictTaskResultTypeNames} predicts — a task may produce
   * several generated types. Each registration fires {@link #onTaskResultRegistered} for domain
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
      for (TypeName planned : predictTaskResultTypeNames(task, context)) {
        context.addGeneratedType(elementName, planned);
        onTaskResultRegistered(task, planned, context);
      }
    }
  }

  /**
   * Predicts the names of the types this task will generate — evaluated at registration time,
   * before anything is generated. The prediction lets generated types reference each other's
   * planned names (see {@link AnnotationProcessingContext#resolveGeneratedType}) without a
   * generation-order dependency: analysis of every task already knows every target name.
   *
   * <p>A domain whose generated types never reference other generated types needs no prediction —
   * registration then simply records nothing for the task (the default). Keep this pure:
   * registrations and their side effects belong to {@link #onTaskResultRegistered}.
   *
   * @param task the planned task
   * @param context the processing context
   * @return the predicted generated type names (empty when the task registers nothing)
   */
  protected List<TypeName> predictTaskResultTypeNames(CodegenTask<K> task, C context) {
    return List.of();
  }

  /**
   * Runs after a predicted type name is registered for a task — the extension point for domain
   * extras on registration (e.g. SPI publication). Default: nothing.
   *
   * @param task the planned task
   * @param plannedName the predicted type name just registered for it
   * @param context the processing context
   */
  protected void onTaskResultRegistered(CodegenTask<K> task, TypeName plannedName, C context) {
    // Extension point for domains — registration bookkeeping is already done by the driver.
  }

  /**
   * Event at the start of every processing round — the domain's hook for per-round preparation
   * (resetting round state, round-specific logging). Called after the processing-over check, before
   * element collection. Default: nothing.
   *
   * @param context the processing context
   */
  protected void onRoundStart(C context) {
    // Extension point for domains — the driver performs no per-round setup beyond logging.
  }

  /**
   * Event after the round's tasks were planned and their predicted results registered — the
   * domain's hook to inspect the plan before analysis starts. Called every generating round with
   * that round's task list, which may be empty when the round produced nothing. Default: nothing.
   *
   * @param tasks the tasks the domain's {@link #planTasks} produced this round
   * @param context the processing context
   */
  protected void onTasksPlanned(List<CodegenTask<K>> tasks, C context) {
    // Extension point for domains — planning bookkeeping is already done by the driver.
  }

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
  @SuppressWarnings("java:S112") // extension point — the domain cannot enumerate its failures
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
  @SuppressWarnings("java:S112") // extension point — the domain cannot enumerate its failures
  protected abstract GenerationTargetClassDto mapToTargetSpecification(
      D definition, K configuration, C context) throws Exception;

  /**
   * Event after a task's target generated successfully — the insert point for per-class metrics and
   * domain bookkeeping. Default: nothing.
   *
   * <p>Runs while the generation scope is still open — close it with {@code
   * context.debugEndOperation(summary)} for a closing summary line, or leave it; the driver
   * restores the pre-task indentation either way.
   *
   * @param generation the planned generation — task, analysed definition and generated target
   * @param context the processing context
   */
  protected void onTaskGenerated(PlannedGeneration<K, D> generation, C context) {
    // Extension point for domains — the target is already generated at this point.
  }

  /**
   * Callback when processing is over — the domain's last-round finalization: SPI flush, reports,
   * and last-round outputs generated directly through {@link #generateTarget}. Default: nothing.
   *
   * @param context the processing context
   */
  protected void onProcessingOver(C context) {
    // Extension point for domains — e.g. last-round outputs via {@link #generateTarget}.
  }

  /**
   * Called at the end of every round that ran planning — also when the plan was empty.
   *
   * @param context the processing context
   */
  protected void endRound(C context) {
    // Extension point for domains — e.g. per-round finalization.
  }

  /**
   * One planned generation, carried from the analysis loop into the generation loop and handed to
   * {@link #onTaskGenerated} as a whole — the record domains read for their generated-task
   * callback.
   */
  protected record PlannedGeneration<K extends CodegenConfiguration, D>(
      CodegenTask<K> task, D definition, GenerationTargetClassDto target) {}
}
