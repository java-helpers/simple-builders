package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.notContains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Tests for the {@link BuilderProcessor} class. */
class BuilderProcessorTest {

  protected BuilderProcessor processor;
  protected Compiler compiler;

  @BeforeEach
  protected void setUp() {
    processor = new BuilderProcessor();
    compiler = Compiler.javac().withProcessors(processor);
  }

  @Test
  void shouldFailWhenAnnotationPlacedOnInterface() {
    // Given: @SimpleBuilder used on an interface instead of a class
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.WrongTargetInterface",
            """
                package test;
                import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                @SimpleBuilder
                public interface WrongTargetInterface { void x(); }
            """);

    // When
    Compilation compilation =
        Compiler.javac().withProcessors(new BuilderProcessor()).compile(source);

    // Then: processor swallows validation exceptions; assert no builder was generated
    assertThat(compilation).succeeded();
    org.junit.jupiter.api.Assertions.assertFalse(
        compilation.generatedFiles().stream()
            .anyMatch(f -> f.getName().endsWith("test/WrongTargetInterfaceBuilder.java")),
        "Builder should not be generated for interface target");
  }

  @Test
  void shouldFailWhenAnnotationPlacedOnAbstractClass() {
    // Given: @SimpleBuilder used on an abstract class
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.AbstractAnnotated",
            """
                package test;
                import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                @SimpleBuilder
                public abstract class AbstractAnnotated { }
            """);

    // When
    Compilation compilation =
        Compiler.javac().withProcessors(new BuilderProcessor()).compile(source);

    // Then: processor swallows validation exceptions; assert no builder was generated
    assertThat(compilation).succeeded();
    org.junit.jupiter.api.Assertions.assertFalse(
        compilation.generatedFiles().stream()
            .anyMatch(f -> f.getName().endsWith("test/AbstractAnnotatedBuilder.java")),
        "Builder should not be generated for abstract class target");
  }

  @Test
  @Disabled("Constructor-Parameters are not supported yet")
  void shouldIgnoreConstructorParametersAndOnlyUseSetters() {
    // Given
    String packageName = "test";
    String className = "CtorAndSetter";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int a; // has no setter, only in constructor
                private String name; // will be set via setter

                public CtorAndSetter() {}
                public CtorAndSetter(int a) { this.a = a; }

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                // a has no setter intentionally
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // Setter-based API should appear (name) and Constructor params (a)
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public CtorAndSetterBuilder a(int a)",
        "instance.setA(a);",
        "public CtorAndSetterBuilder name(String name)",
        "instance.setName(name);");
  }

  @Test
  @Disabled("Records are not supported for builder generation yet")
  void shouldHandleRecordWithoutGeneratingBuilder() {
    // Given
    String packageName = "test";
    String recordName = "PersonRecord";
    String builderClassName = recordName + "Builder";

    JavaFileObject recordFile =
        JavaFileObjects.forSourceString(
            packageName + "." + recordName,
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record PersonRecord(String name, int age) {
              public String upperName() { return name.toUpperCase(); }
            }
            """);

    // When
    Compilation compilation = compile(recordFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // Setter-based API should support record Constructor params (name, age)
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public PersonRecordBuilder name(String name)",
        "instance.setName(name);",
        "public PersonRecordBuilder age(int age)",
        "instance.setAge(age);");
  }

  @Test
  void shouldGenerateCollectionSettersAndProviders() {
    // Given
    String packageName = "test";
    String className = "WithCollections";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.List<String> names;
                private java.util.Set<String> tags;
                private java.util.Map<String, Integer> map;

                public java.util.List<String> getNames() { return names; }
                public void setNames(java.util.List<String> names) { this.names = names; }

                public java.util.Set<String> getTags() { return tags; }
                public void setTags(java.util.Set<String> tags) { this.tags = tags; }

                public java.util.Map<String, Integer> getMap() { return map; }
                public void setMap(java.util.Map<String, Integer> map) { this.map = map; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Direct setters and varargs convenience for List and Set; direct setter for Map
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public WithCollectionsBuilder names(List<String> names)",
        "public WithCollectionsBuilder names(Supplier<List<String>> namesSupplier)",
        "public WithCollectionsBuilder names(String... names)",
        "instance.setNames(List.of(names));",
        "public WithCollectionsBuilder tags(Set<String> tags)",
        "public WithCollectionsBuilder tags(Supplier<Set<String>> tagsSupplier)",
        "public WithCollectionsBuilder tags(String... tags)",
        "instance.setTags(Set.of(tags));",
        "public WithCollectionsBuilder map(Map<String, Integer> map)",
        "public WithCollectionsBuilder map(Supplier<Map<String, Integer>> mapSupplier)",
        "instance.setMap(map);");
  }

  @Test
  void shouldDeclareInstanceFieldAsPrivateFinalWithJavadoc() {
    // Given
    String packageName = "test";
    String className = "FieldDoc";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int x; public int getX(){return x;} public void setX(int x){this.x=x;}
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    ProcessorAsserts.assertContaining(
        generatedCode, "Inner instance of builder.", "private final FieldDoc instance;");
  }

  @Test
  void shouldGenerateCreateMethodWithModifiersAndJavadoc() {
    // Given
    String packageName = "test";
    String className = "CreateDoc";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int x;
                public int getX(){return x;}
                public void setX(int x){this.x=x;}
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    ProcessorAsserts.assertContaining(
        generatedCode,
        "public static CreateDocBuilder create()",
        "Creating a new builder for {@code test.CreateDoc}",
        "@return builder for {@code test.CreateDoc}");
  }

  @Test
  void shouldGenerateBuildOverrideAndReturnInstance() {
    // Given
    String packageName = "test";
    String className = "BuildDoc";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int x;
                public int getX(){return x;}
                public void setX(int x){this.x=x;}
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    ProcessorAsserts.assertContaining(
        generatedCode, "@Override", "public BuildDoc build()", "return instance;");
  }

  @Test
  void shouldReturnThisFromFluentMethods() {
    // Given
    String packageName = "test";
    String className = "FluentReturn";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name; public String getName(){return name;} public void setName(String name){this.name=name;}
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    ProcessorAsserts.assertContaining(generatedCode, "instance.setName(name);", "return this;");
  }

  @Test
  void shouldContainReturnJavadocForConsumerSupplierAndConsumerByBuilder() {
    // Given
    String packageName = "test";
    String className = "ReturnDoc";
    String builderClassName = className + "Builder";

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;
                private HelperAnno helper;
                private HelperPlain helperPlain;
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public void setHelper(HelperAnno helper) { this.helper = helper; }
                public void setHelperPlain(HelperPlain helperPlain) { this.helperPlain = helperPlain; }
            """);

    JavaFileObject helperAnno =
        JavaFileObjects.forSourceString(
            packageName + ".HelperAnno",
            """
                package test;
                import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                @SimpleBuilder
                public class HelperAnno { public HelperAnno() {} }
            """);

    JavaFileObject helperPlain =
        JavaFileObjects.forSourceString(
            packageName + ".HelperPlain",
            """
                package test;
                public class HelperPlain { public HelperPlain() {} }
            """);

    // When
    Compilation compilation = compile(dto, helperAnno, helperPlain);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // '@return current instance of builder' should be present for all method types
    ProcessorAsserts.assertContaining(
        generatedCode,
        "helperBuilderConsumer",
        "@return current instance of builder",
        "helperPlainConsumer",
        "@return current instance of builder",
        "nameSupplier",
        "@return current instance of builder");
  }

  @Test
  void shouldGenerateProxyWithMixedParamsAndArrayVarargs() {
    // Given
    String packageName = "test";
    String className = "MixedProxy";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                public void doMixed(int a, String b, int[] nums) { /* no-op */ }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Expect last parameter mapped to varargs in builder signature and proper call
    ProcessorAsserts.assertContaining(
        generatedCode, "doMixed(int a, String b, int... nums)", "instance.doMixed(a,b,nums);");
  }

  @Test
  void shouldIncludeNestedPackageInClassJavadoc() {
    // Given
    String packageName = "test.nested";
    String className = "NestedDoc";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int x; public int getX(){return x;} public void setX(int x){this.x=x;}
                """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Javadoc line should contain the nested package name and the class indicator
    ProcessorAsserts.assertContaining(generatedCode, "Builder for {", "test.nested");
  }

  @Test
  void shouldGenerateClassJavadocAndBaseInterface() {
    // Given
    String packageName = "test";
    String className = "DocTarget";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int n;

                public int getN() { return n; }
                public void setN(int n) { this.n = n; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Class javadoc and interface
    ProcessorAsserts.assertContaining(
        generatedCode, "Builder for {", "implements IBuilderBase<DocTarget>");
  }

  @Test
  void shouldGenerateBothConstructorsWithJavadocs() {
    // Given
    String packageName = "test";
    String className = "CtorDoc";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String x;
                public String getX() { return x; }
                public void setX(String x) { this.x = x; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    ProcessorAsserts.assertContaining(
        generatedCode,
        "Empty constructor of builder for",
        "Initialisation of builder for",
        "@param instance object instance for initialisiation",
        "this.instance = instance;",
        "this.instance = new CtorDoc();");
  }

  @Test
  void shouldGenerateMethodJavadocsForAllMethodTypes() {
    // Given
    String packageName = "test";
    String className = "JavadocTarget";
    String builderClassName = className + "Builder";

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;
                private HelperPlain helperPlain;
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public void setHelperPlain(HelperPlain helperPlain) { this.helperPlain = helperPlain; }
                public void doSomething(int a, String b) { /* no-op */ }
                """);

    JavaFileObject helper =
        JavaFileObjects.forSourceString(
            packageName + ".HelperPlain",
            """
                package test;
                public class HelperPlain {
                  public HelperPlain() {}
                }
            """);

    // When
    Compilation compilation = compile(dto, helper);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // PROXY method javadoc has @param for each parameter and @return
    ProcessorAsserts.assertContaining(
        generatedCode,
        "Calling <code>doSomething</code> on dto-instance with parameters.",
        "@param a value for a.",
        "@param b value for b.",
        "@return current instance of builder");

    // SUPPLIER method javadoc and code
    ProcessorAsserts.assertContaining(generatedCode, "supplier for field", "nameSupplier");

    // CONSUMER method javadoc and code
    ProcessorAsserts.assertContaining(
        generatedCode, "consumer providing instance of field", "helperPlainConsumer");
  }

  @Test
  void shouldGenerateConsumerByBuilderForAnnotatedField() {
    // Given
    String packageName = "test";
    String className = "UsesAnnotatedHelper";
    String builderClassName = className + "Builder";

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private HelperAnno helper;

                public HelperAnno getHelper() { return helper; }
                public void setHelper(HelperAnno helper) { this.helper = helper; }
            """);

    JavaFileObject helper =
        JavaFileObjects.forSourceString(
            packageName + ".HelperAnno",
            """
                package test;
                import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                @SimpleBuilder
                public class HelperAnno {
                  public HelperAnno() {}
                }
            """);

    // When
    Compilation compilation = compile(dto, helper);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Expect consumer-by-builder method using HelperAnnoBuilder and builder.build()
    ProcessorAsserts.assertContaining(
        generatedCode,
        "helperBuilderConsumer",
        "HelperAnnoBuilder builder = new HelperAnnoBuilder();",
        "helperBuilderConsumer.accept(builder);",
        "instance.setHelper(builder.build());");
  }

  @Test
  void shouldGenerateConsumerForCustomTypeWithEmptyConstructor() {
    // Given
    String packageName = "test";
    String className = "UsesPlainHelper";
    String builderClassName = className + "Builder";

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private HelperPlain helperPlain;

                public HelperPlain getHelperPlain() { return helperPlain; }
                public void setHelperPlain(HelperPlain helperPlain) { this.helperPlain = helperPlain; }
            """);

    JavaFileObject helper =
        JavaFileObjects.forSourceString(
            packageName + ".HelperPlain",
            """
                package test;
                public class HelperPlain {
                  public HelperPlain() {}
                }
            """);

    // When
    Compilation compilation = compile(dto, helper);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Expect consumer method with local var named 'consumer' of HelperPlain
    ProcessorAsserts.assertContaining(
        generatedCode,
        "HelperPlain consumer = new HelperPlain();",
        "helperPlainConsumer.accept(consumer);",
        "instance.setHelperPlain(consumer);");
  }

  @Test
  void shouldGenerateSupplierMethodForField() {
    // Given
    String packageName = "test";
    String className = "HasSupplier";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;

                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Expect supplier-based setter usage
    ProcessorAsserts.assertContaining(
        generatedCode, "nameSupplier", "instance.setName(nameSupplier.get());");
  }

  protected Compilation compile(JavaFileObject... sourceFiles) {
    return compiler.compile(sourceFiles);
  }

  @Test
  void shouldFailCompilationOnLowerReleaseOption() {
    // Given: a minimal @SimpleBuilder-annotated class
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ForcedOldRelease",
            """
                package test;
                import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
                @SimpleBuilder
                public class ForcedOldRelease { public ForcedOldRelease() {} }
            """);

    // When: compile with a lower language level to simulate older Java (no production code change)
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new BuilderProcessor())
            .withOptions("--release", "11")
            .compile(source);

    // Then: compilation must fail with the expected error
    assertThat(compilation).failed();
    assertThat(compilation)
        .hadErrorContaining(
            "simple-builders requires Java 17 or higher for annotation processing.");
  }

  @Test
  void shouldGenerateBuilderForSimpleClass() {
    // Given
    String packageName = "test";
    String className = "Person";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;
                private int age;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public int getAge() {
                    return age;
                }

                public void setAge(int age) {
                    this.age = age;
                }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Verify the generated code contains builder methods (build/create are checked centrally)
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public PersonBuilder name(String name)",
        "public PersonBuilder age(int age)");
  }

  @Test
  void shouldHandleSetOfStrings() {
    // Given
    String packageName = "test";
    String className = "HasSetString";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.Set<String> tags;

                public java.util.Set<String> getTags() { return tags; }
                public void setTags(java.util.Set<String> tags) { this.tags = tags; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(
        compilation, builderClassName, loadGeneratedSource(compilation, builderClassName));
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public HasSetStringBuilder tags(Set<String> tags)",
        "instance.setTags(tags);",
        "public HasSetStringBuilder tags(Consumer<HashSetBuilder<String>> tagsBuilderConsumer)",
        "instance.setTags(builder.build());",
        "public HasSetStringBuilder tags(String... tags)",
        "instance.setTags(Set.of(tags));",
        "public HasSetStringBuilder tags(Supplier<Set<String>> tagsSupplier)",
        "instance.setTags(tagsSupplier.get());");
  }

  @Test
  void shouldGenerateSetterForClassInDifferentPackage() {
    // Given
    String packageName = "test";
    String className = "UsesOtherPackageHelper";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private otherpkg.Helper helper;

                public otherpkg.Helper getHelper() {
                  return helper;
                }
                public void setHelper(otherpkg.Helper helper) {
                  this.helper = helper;
                }
            """);

    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            "otherpkg.Helper",
            """
                package otherpkg;
                public class Helper {
                  public Helper() {}
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(
        generatedCode, "public UsesOtherPackageHelperBuilder helper(Helper helper)");
  }

  @Test
  // Todo: what should that test validate?
  void shouldHandleSetOfCustomType() {
    // Given
    String packageName = "test";
    String className = "HasSetCustom";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.Set<Helper> helpers;

                public java.util.Set<Helper> getHelpers() { return helpers; }
                public void setHelpers(java.util.Set<Helper> helpers) { this.helpers = helpers; }
            """);

    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            packageName + ".Helper",
            """
                package test;
                public class Helper {
                  public Helper() {}
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public HasSetCustomBuilder helpers(Set<Helper> helpers)",
        "instance.setHelpers(helpers);",
        "public HasSetCustomBuilder helpers(Consumer<HashSetBuilder<Helper>> helpersBuilderConsumer)",
        "instance.setHelpers(builder.build());",
        "public HasSetCustomBuilder helpers(Helper... helpers)",
        "instance.setHelpers(Set.of(helpers));",
        "public HasSetCustomBuilder helpers(Supplier<Set<Helper>> helpersSupplier)",
        "instance.setHelpers(helpersSupplier.get());");
  }

  @Test
  void shouldHandleHelperInDifferentPackage() {
    // Given
    String packageName = "test";
    String className = "UsesOtherPackageHelper";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private otherpkg.Helper helper;

                public otherpkg.Helper getHelper() { return helper; }
                public void setHelper(otherpkg.Helper helper) { this.helper = helper; }
            """);

    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            "otherpkg.Helper",
            """
                package otherpkg;
                public class Helper {
                  public Helper() {}
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    assertGenerationSucceeded(
        compilation, builderClassName, loadGeneratedSource(compilation, builderClassName));
    // build/create are checked centrally; no additional builder setter checks here
  }

  @Test
  void shouldFilterOutNonRelevantMethods() {
    // Given
    String packageName = "test";
    String className = "HasVariousMethods";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                // valid proxy candidate
                public void ok() {}

                // valid setter
                public void setOk(int ok) {}

                // should be filtered
                private void setHidden(int hidden) {}
                public static void setUtil(int util) {}
                public void setRisky(int risk) throws Exception {}
                public int returnsInt() { return 42; }
                public int getJustGetter() { return 25; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // Expect only ok() proxy to be present
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public HasVariousMethodsBuilder ok()"),
        contains("instance.ok();"),
        contains("public HasVariousMethodsBuilder ok(int ok)"),
        contains("instance.setOk(ok);"),
        notContains("hidden"),
        notContains("util(int util)"),
        notContains("risky(int risk)"),
        notContains("returnsInt"),
        notContains("justGetter"));
  }

  @Test
  void shouldNotGenerateConsumersForPrimitiveAndArrayFields() {
    // Given
    String packageName = "test";
    String className = "PrimAndArray";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int count;
                private String[] names;

                public int getCount() { return count; }
                public void setCount(int count) { this.count = count; }

                public String[] getNames() { return names; }
                public void setNames(String[] names) { this.names = names; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // Expect simple setters and suppliers only; no consumer methods for primitive/array
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public PrimAndArrayBuilder count(Supplier"),
        contains("public PrimAndArrayBuilder names(Supplier"),
        notContains("public PrimAndArrayBuilder count(Consumer"),
        notContains("public PrimAndArrayBuilder names(Consumer"));
  }

  @Test
  void shouldNotGenerateSpecialMethodsForMapBeyondSetterAndSupplier() {
    // Given
    String packageName = "test";
    String className = "HasMap";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.Map<String, Integer> map;

                public java.util.Map<String, Integer> getMap() { return map; }
                public void setMap(java.util.Map<String, Integer> map) { this.map = map; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // Expect only direct setter and supplier; no varargs/consumer for Map
    ProcessorAsserts.assertingResult(
        generatedCode,
        contains("public HasMapBuilder map("),
        contains("mapSupplier)"),
        notContains("map(String..."),
        notContains("map(java.lang.String..."),
        notContains("mapBuilderConsumer"),
        notContains("mapConsumer"));
  }

  @Test
  void shouldNotGenerateConsumerForAbstractHelper() {
    // Given
    String packageName = "test";
    String className = "UsesAbstract";
    String builderClassName = className + "Builder";

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private HelperAbs helperAbs;

                public HelperAbs getHelperAbs() { return helperAbs; }
                public void setHelperAbs(HelperAbs helperAbs) { this.helperAbs = helperAbs; }
            """);

    JavaFileObject helperAbs =
        JavaFileObjects.forSourceString(
            packageName + ".HelperAbs",
            """
                package test;
                public abstract class HelperAbs {
                  public HelperAbs() {}
                }
            """);

    // When
    Compilation compilation = compile(dto, helperAbs);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // No consumer method should be generated for abstract helper type
    ProcessorAsserts.assertNotContaining(
        generatedCode, "helperAbsConsumer", "helperAbsBuilderConsumer");
  }

  @Test
  void shouldGenerateAnnotationsAndCreateBuildMethods() {
    // Given
    String packageName = "test";
    String className = "AnnoTarget";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int a;
                public int getA() { return a; }
                public void setA(int a) { this.a = a; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(
        generatedCode,
        "@Generated(",
        "@BuilderImplementation(",
        "public static AnnoTargetBuilder create()",
        "return new AnnoTargetBuilder(instance);",
        "public AnnoTarget build()",
        "return instance;");
  }

  @Test
  void shouldGenerateVarargsConvenienceForCollections() {
    // Given
    String packageName = "test";
    String className = "HasCollectionsConvenience";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.List<String> names;
                private java.util.Set<String> tags;

                public java.util.List<String> getNames() { return names; }
                public void setNames(java.util.List<String> names) { this.names = names; }

                public java.util.Set<String> getTags() { return tags; }
                public void setTags(java.util.Set<String> tags) { this.tags = tags; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public HasCollectionsConvenienceBuilder names(String... names)",
        "instance.setNames(List.of(names));",
        "public HasCollectionsConvenienceBuilder tags(String... tags)",
        "instance.setTags(Set.of(tags));");
  }

  @Test
  void shouldHandleJavaTimeField() {
    // Given
    String packageName = "test";
    String className = "HasLocalDate";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.time.LocalDate date;

                public java.time.LocalDate getDate() { return date; }
                public void setDate(java.time.LocalDate date) { this.date = date; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(
        generatedCode,
        "public HasLocalDateBuilder date(LocalDate date)",
        "instance.setDate(date);");
  }

  @Test
  void shouldHandleMixedFields() {
    // Given
    String packageName = "test";
    String className = "HasMixed";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int a;
                private String[] names;
                private java.util.List<String> list;
                private java.util.Map<String, Integer> map;
                private java.time.LocalDate date;

                public int getA() { return a; }
                public void setA(int a) { this.a = a; }

                public String[] getNames() { return names; }
                public void setNames(String[] names) { this.names = names; }

                public java.util.List<String> getList() { return list; }
                public void setList(java.util.List<String> list) { this.list = list; }

                public java.util.Map<String, Integer> getMap() { return map; }
                public void setMap(java.util.Map<String, Integer> map) { this.map = map; }

                public java.time.LocalDate getDate() { return date; }
                public void setDate(java.time.LocalDate date) { this.date = date; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // TODO add asserts here for fields and build method
  }

  @Test
  void shouldHandleObjectArrayField() {
    // Given
    String packageName = "test";
    String className = "HasObjectArray";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String[] names;

                public String[] getNames() { return names; }
                public void setNames(String[] names) { this.names = names; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // TODO: check test is needed?
  }

  @Test
  void shouldHandleClassWithOnlyGetters() {
    // Given
    String packageName = "test";
    String className = "OnlyGetters";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private String name;

                public String getName() { return name; }
                // no setter on purpose
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // Even without setters, builder should exist (build/create checked centrally). No extra
    // asserts.
    // TODO Without Setters or Constructors there is no need for a builder. we should have a warning
    // in build
  }

  @Test
  void shouldHandleEmptyClass() {
    // Given
    String packageName = "test";
    String className = "EmptyClass";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                // No fields or methods
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // TODO Without Setters or Constructors there is no need for a builder. we should have a warning
    // in build
  }

  @Test
  void shouldGenerateBuilderForPrimitiveOnlyClass() {
    // Given
    String packageName = "test";
    String className = "Numbers";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int a;

                public int getA() { return a; }
                public void setA(int a) { this.a = a; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    ProcessorAsserts.assertContaining(generatedCode, "public NumbersBuilder a(int a)");
  }

  @Test
  void shouldHandleListField() {
    // Given
    String packageName = "test";
    String className = "HasList";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.List<String> names;

                public java.util.List<String> getNames() { return names; }
                public void setNames(java.util.List<String> names) { this.names = names; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // TODO: add assterts
  }

  @Test
  // TODO: class code does not match testname
  void shouldBoxPrimitiveTypeArgumentsInGenerics() {
    // Given
    String packageName = "test";
    String className = "HasConsumer";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.function.Consumer<Integer> consumer;

                public java.util.function.Consumer<Integer> getConsumer() { return consumer; }
                public void setConsumer(java.util.function.Consumer<Integer> consumer) { this.consumer = consumer; }
            """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);
    // Ensure builder compiles and does not use primitive type argument like <int>
    ProcessorAsserts.assertNotContaining(generatedCode, "<int>");
  }

  @Test
  void shouldHandleNestedTypeWithEmptyConstructor() {
    // Given
    String packageName = "test";
    String className = "OuterWithHelper";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private Helper helper;

                public Helper getHelper() { return helper; }
                public void setHelper(Helper helper) { this.helper = helper; }
            """);

    // And a top-level helper class with empty constructor
    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            packageName + ".Helper",
            """
                package test;
                public class Helper {
                  public Helper() {}
                  private String v;
                  public String getV() { return v; }
                  public void setV(String v) { this.v = v; }
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // TODO: add asserts
  }

  @Test
  void shouldHandleNestedTypeWithoutEmptyConstructor() {
    // Given
    String packageName = "test";
    String className = "OuterWithNoEmptyHelper";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private HelperNoEmpty helper;

                public HelperNoEmpty getHelper() { return helper; }
                public void setHelper(HelperNoEmpty helper) { this.helper = helper; }
            """);

    // And a top-level helper class without empty constructor
    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            packageName + ".HelperNoEmpty",
            """
                package test;
                public class HelperNoEmpty {
                  private final int x;
                  public HelperNoEmpty(int x) { this.x = x; }
                  public int getX() { return x; }
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // TODO: sdd asserts, because of missing empty constructor there could be no consumer
  }

  // TODO adding list of custom types without empty constructor

  @Test
  void shouldHandleListOfCustomType() {
    // Given
    String packageName = "test";
    String className = "HasListCustom";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.List<Helper> helpers;

                public java.util.List<Helper> getHelpers() { return helpers; }
                public void setHelpers(java.util.List<Helper> helpers) { this.helpers = helpers; }
            """);

    // And a top-level Helper class referenced by the list
    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            packageName + ".Helper",
            """
                package test;
                public class Helper {
                  public Helper() {}
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // TODO: Adding asserts
  }

  @Test
  void shouldHandleMapWithCustomValueType() {
    // Given
    String packageName = "test";
    String className = "HasMapCustom";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private java.util.Map<String, Helper> map;

                public java.util.Map<String, Helper> getMap() { return map; }
                public void setMap(java.util.Map<String, Helper> map) { this.map = map; }
            """);

    // And a top-level Helper class referenced by the map
    JavaFileObject helperSource =
        JavaFileObjects.forSourceString(
            packageName + ".Helper",
            """
                package test;
                public class Helper {
                  public Helper() {}
                }
            """);

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode = loadGeneratedSource(compilation, builderClassName);
    assertGenerationSucceeded(compilation, builderClassName, generatedCode);

    // TODO: Adding asserts
  }
}
