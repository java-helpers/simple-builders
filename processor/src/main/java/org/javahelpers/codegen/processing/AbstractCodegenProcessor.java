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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.classgen.ClassCodeGenerator;
import org.javahelpers.simple.builders.processor.classgen.CodeGenerationException;
import org.javahelpers.simple.builders.processor.classgen.GenerationEnvironment;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.processing.AnnotationProcessingContext;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.logging.PerformanceTracker;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Driver of a generic annotation-processing pipeline: the framework owns the processing lifecycle,
 * the domain plugs in its hooks — collect, plan, register, analyse/map, generate, finalize.
 *
 * <p>The round spine:
 *
 * <pre>
 *   ctx      = domain.createContext(env, logger)                       [in init]
 *   elements = findElements(annotations, roundEnv, ctx)                [default: elementSpec()]
 *   tasks    = domain.plan(elements, ctx)
 *   domain.registerPlannedOutputs(tasks, ctx)                          [up-front type recognition]
 *   for each task (guarded, strict-mode reporting):
 *     def     = domain.analyse(task, ctx)
 *     targets = domain.mapToTargets(def, task, ctx)
 *     for each target:
 *       existsGuard → engine.generate(target, formatterResolver.resolve(target.mode))
 *     domain.afterGenerate(def, targets, task, ctx)
 *   domain.endRound(ctx)
 *   last round: domain.onProcessingOver(ctx)                           [deferred outputs, reports]
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

  private final CodegenBackendRegistry backendRegistry = new CodegenBackendRegistry();
  private ClassCodeGenerator codeGenerator;
  private SourceFormatterResolver formatterResolver;
  private boolean supportedJdk = true;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    CompilerArgumentsReader reader = new CompilerArgumentsReader(processingEnv);
    this.logger = createLogger(processingEnv, reader);
    this.context = createContext(processingEnv, reader, logger);
    this.codeGenerator = createGenerator(processingEnv, logger, context);
    this.formatterResolver = createFormatterResolver(reader, logger);
    onInit(processingEnv, reader, context);

    SourceVersion current = processingEnv.getSourceVersion();
    this.supportedJdk = current.ordinal() >= requiredSourceVersion().ordinal();
    if (!this.supportedJdk) {
      String releaseNumber = requiredSourceVersion().toString().replace("RELEASE_", "");
      context.error(
          "%s requires Java %s or higher for annotation processing. Detected: '%s'. Please upgrade to JDK %s+ or disable the processor.",
          processorTag(), releaseNumber, current, releaseNumber);
    }
  }

  /**
   * Runs one processing round — see the class javadoc for the spine. Claims the supported
   * annotations only when {@link #claimsAnnotations()} says so — under the default {@code "*"} set
   * it does not claim, because claiming would hide foreign annotations from later processors (hence
   * the {@code java:S3516} suppression).
   */
  @Override
  @SuppressWarnings("java:S3516")
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!supportedJdk) {
      // Fail fast: init() already emitted the error; do not attempt any processing.
      return claimsAnnotations();
    }
    if (roundEnv.processingOver()) {
      onProcessingOver(context);
      return claimsAnnotations();
    }

    PerformanceTracker tracker = context.getPerformanceTracker();
    context.info("%s: PROCESSING ROUND START", processorTag());

    tracker.startPhase();
    ElementCollection collected = findElements(annotations, roundEnv, context);
    tracker.endPhase(elementSpec().collectionPhase());
    context.debug("%s: Processing round started.", processorTag());

    List<CodegenTask<K>> tasks = plan(collected, context);
    if (collected.isEmpty() || isFinished(context)) {
      return claimsAnnotations();
    }

    registerPlannedOutputs(tasks, context);

    int successfulGenerations = 0;
    for (CodegenTask<K> task : tasks) {
      context.debugStartOperation("Processing element: " + task.element().getSimpleName());
      tracker.startClass(task.element().getSimpleName().toString());
      try {
        D definition = analyse(task, context);
        List<GenerationTargetClassDto> targets = mapToTargets(definition, task, context);
        tracker.startPhase();
        for (GenerationTargetClassDto target : targets) {
          generateTarget(target);
        }
        tracker.endPhase(generationPhase());
        afterGenerate(definition, targets, task, context);
        successfulGenerations++;
      } catch (Exception ex) {
        // By default generation failures are warnings so other targets are still generated.
        // In opt-in strict mode they are promoted to errors that fail the build.
        context.reportBasedOnStrictMode(
            task.reportingElement(),
            "%s: Failed to generate %s - %s",
            processorTag(),
            generatedKind(),
            ex.getMessage());
      } finally {
        context.debugEndOperation();
      }
    }

    if (successfulGenerations > 0) {
      context.info(
          "%s: Successfully generated %d %s(s) in this processing round",
          processorTag(), successfulGenerations, generatedKind());
    }

    // Reset indentation level at the end of each processing round to prevent cascading errors
    context.resetIndentation();
    endRound(context);
    return claimsAnnotations();
  }

  @Override
  public final Set<String> getSupportedOptions() {
    Set<String> options = new LinkedHashSet<>(optionRegistry().supportedOptions());
    options.addAll(additionalSupportedOptions());
    return options;
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return supportedAnnotationTypes();
  }

  /**
   * The annotation type names this processor supports. Default: the {@link
   * SupportedAnnotationTypes} annotation on the processor class when present, else {@code "*"} —
   * the framework declares support by method. Domains either annotate their processor class or
   * override this function (e.g. to compose a computed set).
   *
   * @return the supported annotation type names
   */
  protected Set<String> supportedAnnotationTypes() {
    SupportedAnnotationTypes annotation = getClass().getAnnotation(SupportedAnnotationTypes.class);
    return annotation != null ? Set.copyOf(List.of(annotation.value())) : Set.of("*");
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
      String kind = generatedKind();
      String capitalized = Character.toUpperCase(kind.charAt(0)) + kind.substring(1);
      throw new CodeGenerationException(
          """
              %s class '%s' already exists. This may be a manually written %s or a previously generated %s.
              To resolve this:
              1. If you have a manual %s, consider renaming it or removing the annotation triggering generation
              2. If this is from a previous compilation, clean and rebuild the project
              3. Check that you're not trying to generate multiple %ss for the same target type
              """
              .formatted(capitalized, qualifiedName, kind, kind, kind, kind));
    }
  }

  /**
   * Creates the logger. Default: verbose when the domain's declared verbose option or the javac
   * {@code -verbose} flag requests it — see {@link ProcessorOptionRegistry#isVerboseRequested}.
   */
  protected ProcessingLogger createLogger(
      ProcessingEnvironment processingEnv, CompilerArgumentsReader reader) {
    return optionRegistry().isVerboseRequested(reader)
        ? ProcessingLogger.createVerbose(processingEnv)
        : ProcessingLogger.create(processingEnv);
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

  /** Which elements the domain generates code for. */
  protected abstract AnnotationElementSpec elementSpec();

  /**
   * The processor's declared compiler options. Default: an empty registry — a processor without
   * options declares nothing.
   */
  protected ProcessorOptionRegistry optionRegistry() {
    return new ProcessorOptionRegistry(List.of());
  }

  /**
   * Whether {@link #process} claims the supported annotations. Default {@code false} — claiming
   * would hide them from later processors, which is especially wrong under the default {@code "*"}
   * supported set. Override to claim when the processor owns a concrete annotation set.
   *
   * @return {@code true} to claim the supported annotations
   */
  protected boolean claimsAnnotations() {
    return false;
  }

  /**
   * The minimum source level the processor supports. Default: {@link
   * SourceVersion#latestSupported()}.
   */
  protected SourceVersion requiredSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Diagnostic tag prefixing messages, e.g. {@code "simple-builders"}. Default: the processor's
   * simple class name.
   */
  protected String processorTag() {
    return getClass().getSimpleName();
  }

  /** What one generated unit is called in diagnostics, e.g. {@code "builder"}. Default: class. */
  protected String generatedKind() {
    return "class";
  }

  /** Domain-specific initialization, after the framework wiring is in place. */
  protected void onInit(
      ProcessingEnvironment processingEnv, CompilerArgumentsReader reader, C context) {}

  /**
   * Finds the elements of this round the domain generates code for. Default delegates to {@link
   * #elementSpec()}'s collector; override for custom element discovery.
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
   * Plans the tasks of this round from the collected elements: resolves per-element configuration,
   * expands multi-target declarations and applies domain scoping rules.
   *
   * @param collected the elements matched by {@link #findElements}
   * @param context the processing context
   * @return the generation plan, possibly empty
   */
  protected abstract List<CodegenTask<K>> plan(ElementCollection collected, C context);

  /**
   * Registers the types this round will generate, up front, so references to them resolve during
   * analysis. Domains register into {@link AnnotationProcessingContext#getGeneratedTypes()}.
   * Default: no registration.
   *
   * @param tasks the planned tasks
   * @param context the processing context
   */
  protected void registerPlannedOutputs(List<CodegenTask<K>> tasks, C context) {}

  /**
   * Whether the domain generates in exactly one round and that round already ran. Default {@code
   * false} — every round with matching elements is processed.
   *
   * @param context the processing context
   * @return {@code true} to skip this round
   */
  protected boolean isFinished(C context) {
    return false;
  }

  /**
   * Analyses one planned task into its domain definition — the analysis half of the
   * analysis/generation separation.
   *
   * @param task the planned task
   * @param context the processing context
   * @return the domain definition
   * @throws Exception when analysis fails; reported as a per-element failure
   */
  protected abstract D analyse(CodegenTask<K> task, C context) throws Exception;

  /**
   * Maps a domain definition to render targets — the generation half of the analysis/generation
   * separation, separated by the {@link GenerationTargetClassDto} boundary.
   *
   * @param definition the analysed definition
   * @param task the planned task
   * @param context the processing context
   * @return the class definitions to generate (usually one)
   * @throws Exception when mapping fails; reported as a per-element failure
   */
  protected abstract List<GenerationTargetClassDto> mapToTargets(
      D definition, CodegenTask<K> task, C context) throws Exception;

  /**
   * Runs after all targets of a task generated successfully — e.g. deferred-output accumulation and
   * per-class metrics. Default: nothing.
   *
   * @param definition the analysed definition
   * @param targets the generated class definitions
   * @param task the planned task
   * @param context the processing context
   */
  protected void afterGenerate(
      D definition, List<GenerationTargetClassDto> targets, CodegenTask<K> task, C context) {}

  /** The performance phase wrapping target generation. Default {@code "code-generation"}. */
  protected String generationPhase() {
    return "code-generation";
  }

  /** Callback for the last processing round — deferred outputs and final reports. */
  protected void onProcessingOver(C context) {}

  /** Callback at the end of a generating round — domain finalization. */
  protected void endRound(C context) {}
}
