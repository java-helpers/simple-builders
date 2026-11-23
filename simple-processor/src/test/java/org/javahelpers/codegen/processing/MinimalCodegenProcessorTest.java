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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.codegen.testing.CodegenTestUtils.loadGeneratedSource;
import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNormalizedEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.codegen.analysis.JavaLangAnalyser;
import org.javahelpers.codegen.analysis.JavaLangMapper;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.logging.ProcessingLogger;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.ClassFieldDtoBuilder;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A complete codegen processor in minimal form — only the domain members that have no generic
 * default are implemented; collection, round-driving, generator and formatter wiring come from
 * {@link AbstractCodegenProcessor}.
 */
public class MinimalCodegenProcessorTest {

  /** Marks a type for delegate-builder generation in this test. */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface GenerateBuilder {}

  /** Analysis result: target type, builder name and the setters to delegate to. */
  record MinimalTargetDefinition(
      TypeName sourceTypeName, TypeName builderTypeName, List<ExecutableElement> setters)
      implements CodegenDefinition {
    @Override
    public TypeName getTypeName() {
      return sourceTypeName;
    }

    @Override
    public TypeName targetTypeName() {
      return builderTypeName;
    }
  }

  /**
   * A processor that generates a builder delegating to the target type's setters. Public so javac's
   * {@code -processor} mechanism can instantiate it.
   */
  public static class MinimalProcessor
      extends AbstractCodegenProcessor<
          AnnotationProcessingContext, CodegenConfiguration, MinimalTargetDefinition> {

    public MinimalProcessor() {
      this(
          CodegenProcessorConfiguration.builder()
              .elementCollector(ElementCollector.builder().selector(GenerateBuilder.class).build())
              .loggerPrefix("minimal")
              .build());
    }

    MinimalProcessor(CodegenProcessorConfiguration configuration) {
      super(configuration);
    }

    @Override
    protected AnnotationProcessingContext createContext(
        ProcessingEnvironment processingEnv,
        CompilerArgumentsReader reader,
        ProcessingLogger logger) {
      return new AnnotationProcessingContext(logger, processingEnv);
    }

    @Override
    protected List<CodegenTask<CodegenConfiguration>> planTasks(
        ElementGroups collected, AnnotationProcessingContext ctx) {
      return collected.all().stream()
          .map(
              element ->
                  CodegenTask.builder(CodegenConfiguration.class)
                      .element(element)
                      .outputPackage(ctx.getPackageName(element))
                      .build())
          .toList();
    }

    @Override
    protected List<MinimalTargetDefinition> analyse(
        CodegenTask<CodegenConfiguration> task, AnnotationProcessingContext ctx) {
      if (!(task.element() instanceof TypeElement target)) {
        return List.of();
      }
      return List.of(
          new MinimalTargetDefinition(
              JavaLangMapper.mapToTypeName(target, ctx),
              builderName(target, ctx),
              JavaLangAnalyser.findSetterMethods(target, ctx)));
    }

    @Override
    protected GenerationTargetClassDto mapToTargetSpecification(
        MinimalTargetDefinition definition,
        CodegenConfiguration configuration,
        AnnotationProcessingContext ctx) {
      GenerationTargetClassDto target =
          GenerationTargetClassDtoBuilder.create()
              .typeName(definition.builderTypeName())
              .classAccessModifier(AccessModifier.PUBLIC)
              .formattingMode(FormattingMode.NONE)
              .build();

      ClassFieldDto field =
          ClassFieldDtoBuilder.create()
              .fieldName("target")
              .fieldType(definition.sourceTypeName())
              .visibility(AccessModifier.PRIVATE)
              .literalInitializer("new " + definition.sourceTypeName().getClassName() + "()")
              .build();
      target.addClassField(field);

      for (ExecutableElement setter : definition.setters()) {
        String setterName = setter.getSimpleName().toString();
        String methodName = StringUtils.uncapitalize(Strings.CI.removeStart(setterName, "set"));
        MethodDto method =
            MethodDtoBuilder.create()
                .methodName(methodName)
                .returnType(definition.builderTypeName())
                .modifier(AccessModifier.PUBLIC)
                .code(
                    """
                    target.$setterName:N($paramName:N);
                    return this;""")
                .build();
        String paramName = setter.getParameters().get(0).getSimpleName().toString();
        method.addParameter(JavaLangMapper.map2MethodParameter(setter.getParameters().get(0), ctx));
        method.addArgument("setterName", setterName);
        method.addArgument("paramName", paramName);
        target.addMethod(method);
      }
      MethodDto build =
          MethodDtoBuilder.create()
              .methodName("build")
              .returnType(definition.sourceTypeName())
              .modifier(AccessModifier.PUBLIC)
              .code("return target;")
              .build();
      target.addMethod(build);
      return target;
    }

    private static TypeName builderName(TypeElement target, AnnotationProcessingContext ctx) {
      return new TypeName(ctx.getPackageName(target), target.getSimpleName() + "Builder");
    }
  }

  /** MinimalProcessor variant declaring an unreachable minimum source level. */
  static class StrictJdkProcessor extends MinimalProcessor {
    StrictJdkProcessor() {
      super(
          CodegenProcessorConfiguration.builder()
              .elementCollector(ElementCollector.builder().selector(GenerateBuilder.class).build())
              .loggerPrefix("minimal")
              .requiredSourceVersion("RELEASE_99")
              .build());
    }
  }

  /**
   * MinimalProcessor whose analysis always fails — and without a configured logger prefix, so the
   * simple class name becomes the diagnostic prefix.
   */
  static class FailingProcessor extends MinimalProcessor {
    FailingProcessor() {
      super(
          CodegenProcessorConfiguration.builder()
              .elementCollector(ElementCollector.builder().selector(GenerateBuilder.class).build())
              .build());
    }

    @Override
    protected List<MinimalTargetDefinition> analyse(
        CodegenTask<CodegenConfiguration> task, AnnotationProcessingContext ctx) {
      throw new IllegalStateException("boom");
    }
  }

  /** MinimalProcessor whose analysis produces nothing. */
  static class EmptyProcessor extends MinimalProcessor {
    @Override
    protected List<MinimalTargetDefinition> analyse(
        CodegenTask<CodegenConfiguration> task, AnnotationProcessingContext ctx) {
      return List.of();
    }
  }

  /** MinimalProcessor whose planning produces no tasks. */
  static class NoTasksProcessor extends MinimalProcessor {
    @Override
    protected List<CodegenTask<CodegenConfiguration>> planTasks(
        ElementGroups collected, AnnotationProcessingContext ctx) {
      return List.of();
    }
  }

  /** Verbose compiler option understood by {@link VerboseProcessor}. */
  private static final CompilerOption VERBOSE =
      new CompilerOption() {
        @Override
        public String getOptionName() {
          return "verbose";
        }

        @Override
        public String getCompilerArgument() {
          return "verbose";
        }
      };

  /** MinimalProcessor with the verbose option declared — {@code -Averbose=true} switches logger. */
  static class VerboseProcessor extends MinimalProcessor {
    VerboseProcessor() {
      super(
          CodegenProcessorConfiguration.builder()
              .elementCollector(ElementCollector.builder().selector(GenerateBuilder.class).build())
              .optionRegistry(ProcessorOptionRegistry.builder().verboseOption(VERBOSE).build())
              .build());
    }
  }

  @Test
  void minimalProcessorGeneratesDelegatingBuilder() {
    // Given
    JavaFileObject source =
        CodegenTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              private String name;
              public void setName(String name) { this.name = name; }
            }
            """);

    // When
    Compilation compilation =
        CodegenTestUtils.createCompiler(new MinimalProcessor()).compile(source);

    // Then
    assertThat(compilation).succeeded();
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    // The whole expected target as one textblock — pins structure, delegation and the target
    // field; whitespace-normalized compare, nothing generated is left unchecked.
    assertNormalizedEquals(
        """
        package test;
        public class PersonBuilder {

          private Person target = new Person();

          public PersonBuilder name(String name) {
            target.setName(name);
            return this;
          }

          public Person build() {
            return target;
          }
        }""",
        generatedCode,
        "generated PersonBuilder must match the expected source");
  }

  /**
   * A stale builder left in the generated-sources output is this processor's own previous output —
   * it must be regenerated silently instead of producing the "already exists" warning.
   *
   * <p>Runs through real javac (not compile-testing) because the check compares the element's
   * source location against the real {@code -s} output directory — the Filer's virtualized
   * locations cannot express that setup.
   */
  @Test
  void regeneratesOwnPreviousOutput(@TempDir Path workDir) throws IOException {
    Path generatedSources = workDir.resolve("generated-sources");
    Path stale = generatedSources.resolve("test").resolve("PersonBuilder.java");
    Files.createDirectories(stale.getParent());
    Files.writeString(
        stale,
        """
        package test;
        public class PersonBuilder {
          // leftover of a previous compilation
        }
        """);

    Path source = writeDto(workDir, "src");

    List<String> diagnostics = compileWithJavac(source, generatedSources, generatedSources);
    assertTrue(
        diagnostics.stream().noneMatch(d -> d.contains("already exists")),
        () -> "unexpected 'already exists' warning: " + diagnostics);

    String regenerated = Files.readString(stale);
    assertTrue(
        regenerated.contains("public PersonBuilder name(String name)"),
        () -> "stale file was not regenerated: " + regenerated);
  }

  /**
   * A hand-written class under the target's qualified name is not the processor's own output — the
   * clash must still surface the "already exists" warning.
   */
  @Test
  void warnsOnManualTargetClash(@TempDir Path workDir) throws IOException {
    Path generatedSources = workDir.resolve("generated-sources");
    Files.createDirectories(generatedSources);
    Path sourceRoot = workDir.resolve("src");
    Path manual = sourceRoot.resolve("test").resolve("PersonBuilder.java");
    Files.createDirectories(manual.getParent());
    Files.writeString(
        manual,
        """
        package test;
        public class PersonBuilder {
          // hand-written class clashing with the generation target
        }
        """);

    Path source = writeDto(workDir, "src");

    List<String> diagnostics = compileWithJavac(source, generatedSources, sourceRoot);
    assertTrue(
        diagnostics.stream().anyMatch(d -> d.contains("already exists")),
        () -> "expected 'already exists' warning: " + diagnostics);
  }

  /**
   * A {@code requiredSourceVersion} the running JDK cannot meet must report the graceful error and
   * skip processing.
   */
  @Test
  void reportsErrorOnUnsupportedJdk() {
    JavaFileObject source =
        CodegenTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              public void setName(String name) {}
            }
            """);

    Compilation compilation =
        CodegenTestUtils.createCompiler(new StrictJdkProcessor()).compile(source);

    assertThat(compilation).failed();
    assertTrue(
        compilation.errors().stream()
            .map(d -> d.getMessage(null))
            .anyMatch(m -> m.contains("requires Java 99 or higher")),
        () -> "expected unsupported-JDK error: " + compilation.errors());
  }

  /**
   * An exception thrown by {@code analyse} is reported per task — strict mode (the default)
   * promotes it to an error carrying the processor's class name as prefix.
   */
  @Test
  void failsWhenAnalysisThrows() {
    JavaFileObject source =
        CodegenTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              public void setName(String name) {}
            }
            """);

    Compilation compilation =
        CodegenTestUtils.createCompiler(new FailingProcessor()).compile(source);

    assertThat(compilation).failed();
    assertTrue(
        compilation.errors().stream()
            .map(d -> d.getMessage(null))
            .anyMatch(
                m ->
                    m.contains("Failed to generate target - boom")
                        && m.contains("FailingProcessor")),
        () -> "expected per-task failure error: " + compilation.errors());
  }

  /**
   * An empty {@code analyse} result is legal — the round produces no targets and the compilation
   * still succeeds.
   */
  @Test
  void succeedsWhenAnalysisIsEmpty() {
    JavaFileObject source =
        CodegenTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              public void setName(String name) {}
            }
            """);

    Compilation compilation = CodegenTestUtils.createCompiler(new EmptyProcessor()).compile(source);
    assertThat(compilation).succeeded();
  }

  /**
   * An empty {@code planTasks} result ends the round without work — the compilation still succeeds.
   */
  @Test
  void succeedsWhenNoTasksPlanned() {
    JavaFileObject source =
        CodegenTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              public void setName(String name) {}
            }
            """);

    Compilation compilation =
        CodegenTestUtils.createCompiler(new NoTasksProcessor()).compile(source);
    assertThat(compilation).succeeded();
  }

  /** {@code -Averbose=true} switches the logger to verbose output — generation still works. */
  @Test
  void generatesInVerboseMode() {
    JavaFileObject source =
        CodegenTestUtils.forSource(
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              public void setName(String name) {}
            }
            """);

    Compilation compilation =
        CodegenTestUtils.createCompiler(new VerboseProcessor())
            .withOptions("-Averbose=true")
            .compile(source);
    assertThat(compilation).succeeded();
    String generatedCode =
        CodegenTestUtils.readSource(
            compilation.generatedFiles().stream()
                .filter(f -> f.getName().endsWith("PersonBuilder.java"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no PersonBuilder.java generated")));
    assertTrue(generatedCode.contains("public PersonBuilder name(String name)"));
  }

  private static Path writeDto(Path workDir, String sourceRootName) throws IOException {
    Path source = sourceRootName.isEmpty() ? workDir : workDir.resolve(sourceRootName);
    source = source.resolve("test").resolve("Person.java");
    Files.createDirectories(source.getParent());
    Files.writeString(
        source,
        """
        package test;
        import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;

        @GenerateBuilder
        public class Person {
          private String name;
          public void setName(String name) { this.name = name; }
        }
        """);
    return source;
  }

  /**
   * Runs javac on {@code source} with the {@link MinimalProcessor}, {@code -s} pointing at {@code
   * generatedSources} and {@code -sourcepath} at {@code sourcePath}; returns all diagnostics.
   */
  private static List<String> compileWithJavac(Path source, Path generatedSources, Path sourcePath)
      throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      JavaCompiler.CompilationTask task =
          compiler.getTask(
              null,
              fileManager,
              diagnostics,
              List.of(
                  "-d",
                  source.getParent().getParent().resolve("classes").toString(),
                  "-s",
                  generatedSources.toString(),
                  "-sourcepath",
                  sourcePath.toString(),
                  "-processor",
                  MinimalProcessor.class.getName()),
              null,
              fileManager.getJavaFileObjects(source));
      task.call();
    }
    return diagnostics.getDiagnostics().stream().map(d -> d.getMessage(null)).toList();
  }
}
