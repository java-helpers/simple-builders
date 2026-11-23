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

package org.javahelpers.codegen.testing;

import static org.javahelpers.codegen.testing.ProcessorAsserts.assertContaining;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.javahelpers.codegen.ClassCodeGenerator;
import org.javahelpers.codegen.CodegenBackend;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.ClassFieldDtoBuilder;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.imports.StaticImport;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocTagDto;
import org.javahelpers.codegen.model.method.ConstructorDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodeDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodeStringPlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.method.MethodParameterDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.NestedTypeDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Shared conformance suite for {@link ClassCodeGenerator} implementations.
 *
 * <p>Backends under test are discovered via {@link ServiceLoader}: each backend module registers a
 * {@link TestBackendProvider} on its test classpath (see {@link TestBackendProvider} for the
 * contract). Every test in this class then runs once per discovered backend, with the provider's
 * {@link TestBackendProvider#name() name} in the test display name — so a failure always identifies
 * the failing generator. New backends (issue #364) gain identical coverage without writing or
 * copying any test.
 *
 * <p>Each fixture is asserted on two levels:
 *
 * <ul>
 *   <li><b>Compilability</b> — the emitted source is fed through {@code javac} and must compile
 *       without errors
 *   <li><b>Semantic content</b> — exact declarations and blocks, ignoring whitespace
 * </ul>
 *
 * <p>Backend modules must configure surefire's {@code dependenciesToScan} to include {@code
 * codegen-testing} so this class is discovered from the dependency jar.
 */
public class GeneratorConformanceTest {

  private static final String PACKAGE = "it.conformance";
  private static final Path WORK_DIR = Path.of("target", "generator-conformance");

  /**
   * Provides all backends registered via {@link ServiceLoader}, named for the test display.
   *
   * @return one named argument per discovered {@link TestBackendProvider}
   */
  static Stream<Arguments> backends() {
    return ServiceLoader.load(TestBackendProvider.class).stream()
        .map(ServiceLoader.Provider::get)
        .map(provider -> Arguments.of(Named.of(provider.name(), provider)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void simpleClassWithFieldsMethodsAndConstructor_compiles(TestBackendProvider backend)
      throws Exception {
    GenerationTargetClassDto classDef =
        baseClass("SimpleClass")
            .classFields(
                field("name", TypeName.of(String.class)),
                field("age", TypeNamePrimitive.INT),
                field("active", TypeNamePrimitive.BOOLEAN))
            .constructors(
                ConstructorDtoBuilder.create()
                    .visibility(AccessModifier.PUBLIC)
                    .methodCodeDto(
                        MethodCodeDtoBuilder.create().codeFormat("this.name = name;").build())
                    .parameters(param("name", TypeName.of(String.class)))
                    .build())
            .methods(
                MethodDtoBuilder.create()
                    .methodName("describe")
                    .returnType(TypeName.of(String.class))
                    .modifier(AccessModifier.PUBLIC)
                    .code("return name + \"#\" + age;")
                    .build())
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        public class SimpleClass {
          private String name;
          private int age;
          private boolean active;
          public SimpleClass(String name) {
            this.name = name;
          }
          public String describe() {
            return name + "#" + age;
          }
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void genericClassAndMethodBounds_compile(TestBackendProvider backend) throws Exception {
    var generic =
        GenericParameterDtoBuilder.create()
            .name("T")
            .upperBounds(
                new TypeNameGeneric(TypeName.of(Comparable.class), new TypeNameVariable("T")))
            .build();

    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("pick")
            .returnType(new TypeNameVariable("R"))
            .modifier(AccessModifier.PUBLIC)
            .isStatic(true)
            .code("return first;")
            .genericParameters(
                GenericParameterDtoBuilder.create()
                    .name("R")
                    .upperBounds(TypeName.of(Number.class))
                    .build())
            .parameters(
                param("first", new TypeNameVariable("R")),
                param("second", new TypeNameVariable("R")))
            .build();
    GenerationTargetClassDto classDef =
        baseClass("GenericBox").generics(generic).methods(method).build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        public class GenericBox<T extends Comparable<T>> {
          public static <R extends Number> R pick(R first, R second) {
            return first;
          }
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void collectionArrayAndOptionalTypes_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef =
        baseClass("ContainerClass")
            .classFields(
                field("names", new TypeNameGeneric(List.class, String.class)),
                field(
                    "scores",
                    new TypeNameGeneric(
                        TypeName.of(java.util.Map.class),
                        TypeName.of(String.class),
                        TypeName.of(Integer.class))),
                field("numbers", new TypeNameArray(TypeNamePrimitive.INT)),
                field("nickname", new TypeNameGeneric(java.util.Optional.class, String.class)))
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        private List<String> names;
        private Map<String, Integer> scores;
        private int[] numbers;
        private Optional<String> nickname;
        """);
  }

  /**
   * Two field types sharing the simple name {@code Date} from different packages must never be
   * rendered ambiguously — at least one of them has to stay fully qualified (issue #417).
   */
  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void collidingSimpleNames_areDisambiguated(TestBackendProvider backend) throws Exception {
    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("touch")
            .returnType(TypeNamePrimitive.VOID)
            .modifier(AccessModifier.PUBLIC)
            .code("if (when == null) { return; }")
            .parameters(param("when", new TypeName("java.sql", "Date")))
            .build();
    GenerationTargetClassDto classDef =
        baseClass("CollidingDates")
            .classFields(
                field("utilDate", new TypeName("java.util", "Date")),
                field("sqlDate", new TypeName("java.sql", "Date")))
            .methods(method)
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    // The colliding type that wins the import renders by simple name; the loser stays qualified.
    assertContaining(
        source,
        """
        private java.util.Date utilDate;
        private Date sqlDate;
        public void touch(java.sql.Date when) {
          if (when == null) {
            return;
          }
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void nestedTypesWithMembers_compile(TestBackendProvider backend) throws Exception {
    NestedTypeDto nested =
        NestedTypeDtoBuilder.create()
            .typeName("Helper")
            .kind(NestedTypeDto.NestedTypeKind.CLASS)
            .visibility(AccessModifier.PUBLIC)
            .methods(
                MethodDtoBuilder.create()
                    .methodName("assist")
                    .returnType(TypeNamePrimitive.VOID)
                    .modifier(AccessModifier.PUBLIC)
                    .code("int touched = 0;")
                    .build())
            .build();
    GenerationTargetClassDto classDef = baseClass("OuterClass").nestedTypes(nested).build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        public class Helper {
          public void assist() {
            int touched = 0;
          }
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void annotationsOnClassMembersAndTypeUse_compile(TestBackendProvider backend) throws Exception {
    AnnotationDto deprecated =
        AnnotationDtoBuilder.create()
            .annotationType(TypeName.of(Deprecated.class))
            .members(
                () ->
                    Stream.of(Map.entry("since", "\"0.9\""), Map.entry("forRemoval", "true"))
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (first, second) -> first,
                                LinkedHashMap::new)))
            .build();

    AnnotationDto generated =
        AnnotationDtoBuilder.create()
            .annotationType(new TypeName("javax.annotation.processing", "Generated"))
            .members(Map.of("value", "\"conformance\""))
            .build();

    TypeName annotatedString = TypeName.of(String.class);
    annotatedString.addAnnotation(
        AnnotationDtoBuilder.create()
            .annotationType(TypeName.of(TestTypeUseAnnotation.class))
            .build());
    GenerationTargetClassDto classDef =
        baseClass("AnnotatedClass")
            .classAnnotations(new LinkedHashSet<>(List.of(deprecated, generated)))
            .classFields(field("flagged", annotatedString))
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        @Deprecated(since = "0.9", forRemoval = true)
        @Generated("conformance")
        public class AnnotatedClass {
          @TestTypeUseAnnotation
          private String flagged;
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void codeTemplatePlaceholders_resolve(TestBackendProvider backend) throws Exception {
    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("parse")
            .returnType(TypeNamePrimitive.LONG)
            .modifier(AccessModifier.PUBLIC)
            .methodCodeDto(
                MethodCodeDtoBuilder.create()
                    .codeFormat("int $l:L = 0;\n$b:B box = null;\nreturn $t:T.parseLong($s:S);")
                    .codeArguments(
                        new MethodCodeStringPlaceholder("l", "count"),
                        new MethodCodeTypePlaceholder("b", TypeNamePrimitive.INT),
                        new MethodCodeTypePlaceholder("t", TypeName.of(Long.class)),
                        new MethodCodeStringPlaceholder("s", "42"))
                    .build())
            .build();
    GenerationTargetClassDto classDef = baseClass("TemplateClass").methods(method).build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        public long parse() {
          int count = 0;
          Integer box = null;
          return Long.parseLong("42");
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void superTypeAndInterfaces_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef =
        baseClass("DerivedClass")
            .superType(new TypeNameGeneric(java.util.ArrayList.class, String.class))
            .interfaces(
                new LinkedHashSet<>(
                    List.of(
                        new InterfaceName("java.io", "Serializable"),
                        new InterfaceName("java.lang", "Cloneable"))))
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        public class DerivedClass extends ArrayList<String> implements Serializable, Cloneable {
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void staticImportsAndStaticFactory_compile(TestBackendProvider backend) throws Exception {
    ClassFieldDto field =
        ClassFieldDtoBuilder.create()
            .fieldName("initial")
            .fieldType(new TypeNameGeneric(List.class, String.class))
            .visibility(AccessModifier.PRIVATE)
            .literalInitializer("emptyList()")
            .build();

    MethodDto factory =
        MethodDtoBuilder.create()
            .methodName("of")
            .returnType(TypeNamePrimitive.INT)
            .modifier(AccessModifier.PUBLIC)
            .isStatic(true)
            .code("return names.length;")
            .parameters(param("names", new TypeNameArray(TypeName.of(String.class))))
            .build();
    GenerationTargetClassDto classDef =
        baseClass("FactoryClass")
            .imports(new StaticImport(TypeName.of(java.util.Collections.class), "emptyList"))
            .classFields(field)
            .methods(factory)
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        import static java.util.Collections.emptyList;
        """,
        """
        private List<String> initial = emptyList();
        """,
        """
        public static int of(String... names) {
          return names.length;
        }
        """);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void javadocIsEmittedForClassAndMembers(TestBackendProvider backend) throws Exception {
    var javadoc =
        JavadocDtoBuilder.create()
            .format("Echoes the value.")
            .args()
            .tags(
                new JavadocTagDto("param", "value the input"),
                new JavadocTagDto("return", "the echoed value"))
            .build();
    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("echo")
            .returnType(TypeName.of(String.class))
            .modifier(AccessModifier.PUBLIC)
            .javadoc(javadoc)
            .code("return value;")
            .parameters(param("value", TypeName.of(String.class)))
            .build();
    GenerationTargetClassDto classDef =
        baseClass("DocumentedClass")
            .classJavadoc(
                JavadocDtoBuilder.create().format("Documented conformance class.").args().build())
            .methods(method)
            .build();

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source,
        """
        /**
         * Documented conformance class.
         */
        public class DocumentedClass {
          /**
           * Echoes the value.
           * @param value the input
           * @return the echoed value
           */
          public String echo(String value) {
            return value;
          }
        }
        """);
  }

  /** Generates {@code classDef} through {@code provider}'s backend and returns the source text. */
  private String generate(TestBackendProvider provider, GenerationTargetClassDto classDef)
      throws Exception {
    TestingGenerationEnvironment env = TestingGenerationEnvironment.create();
    CodegenBackend backend = provider.create(env, env.getLogger());
    backend.generator().generateClass(classDef, backend.formatterFor(FormattingMode.NONE));
    return env.generatedSource();
  }

  /**
   * Compiles {@code source} with the JDK compiler on the test classpath; fails when javac reports
   * an error.
   */
  private void assertCompiles(GenerationTargetClassDto classDef, String source) throws IOException {
    String qualifiedName = classDef.getTypeName().getFullQualifiedName();
    Path sourceFile = WORK_DIR.resolve(qualifiedName.replace('.', '/') + ".java");
    Files.createDirectories(sourceFile.getParent());
    Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
    Path classes = Files.createDirectories(WORK_DIR.resolve("classes"));

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      boolean ok =
          compiler
              .getTask(
                  null,
                  fileManager,
                  diagnostics,
                  List.of(
                      "-proc:none",
                      "-classpath",
                      System.getProperty("java.class.path"),
                      "-d",
                      classes.toString()),
                  null,
                  fileManager.getJavaFileObjects(sourceFile))
              .call();
      assertTrue(
          ok,
          () ->
              "generated source for "
                  + qualifiedName
                  + " must compile:\n"
                  + diagnostics.getDiagnostics().stream()
                      .map(Object::toString)
                      .collect(Collectors.joining("\n"))
                  + "\n--- source ---\n"
                  + source);
    }
  }

  private static GenerationTargetClassDtoBuilder baseClass(String simpleName) {
    return GenerationTargetClassDtoBuilder.create()
        .typeName(new TypeName(PACKAGE, simpleName))
        .classAccessModifier(AccessModifier.PUBLIC)
        .formattingMode(FormattingMode.NONE);
  }

  private static ClassFieldDto field(String name, TypeName type) {
    return ClassFieldDtoBuilder.create()
        .fieldName(name)
        .fieldType(type)
        .visibility(AccessModifier.PRIVATE)
        .build();
  }

  private static MethodParameterDto param(String name, TypeName type) {
    return MethodParameterDtoBuilder.create().parameterName(name).parameterTypeName(type).build();
  }
}
