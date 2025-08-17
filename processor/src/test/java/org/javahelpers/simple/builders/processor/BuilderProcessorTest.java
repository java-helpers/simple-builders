package org.javahelpers.simple.builders.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Tests for the {@link BuilderProcessor} class. */
class BuilderProcessorTest extends AbstractBuilderProcessorTest {

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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Verify the generated code contains expected methods (use explicit strings for readability)
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public Person build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public PersonBuilder name(String name)"),
                "name(String) setter missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public PersonBuilder age(int age)"),
                "age(int) setter missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static PersonBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    ProcessorTestUtils.assertingResult(
        generatedCode,
        ProcessorTestUtils.containsWithMessage(
            "public HasSetString build()", "build() method missing"),
        ProcessorTestUtils.containsWithMessage(
            "public static HasSetStringBuilder create()", "static create() missing"));
  }

  @Test
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
            "package "
                + packageName
                + ";\n"
                + "public class Helper {\n"
                + "  public Helper() {}\n"
                + "}\n");

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasSetCustom build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasSetCustomBuilder create()"),
                "static create() missing"));
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
            "package otherpkg;\n" + "public class Helper {\n" + "  public Helper() {}\n" + "}\n");

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public UsesOtherPackageHelper build()"),
                "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static UsesOtherPackageHelperBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasLocalDate build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasLocalDateBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasMixed build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasMixedBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasObjectArray build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasObjectArrayBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Even without setters, builder should exist with create() and build()
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public OnlyGetters build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static OnlyGettersBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Verify the generated code contains expected methods
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public EmptyClass build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static EmptyClassBuilder create()"),
                "static create() missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public Numbers build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public NumbersBuilder a(int a)"), "a(int) setter missing"));
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasList build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasListBuilder create()"),
                "static create() missing"));
  }

  @Test
  void shouldHandleMapField() {
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Verify essential methods exist
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasMap build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasMapBuilder create()"),
                "static create() missing"));
  }

  @Test
  void shouldHandleArrayField() {
    // Given
    String packageName = "test";
    String className = "HasArray";
    String builderClassName = className + "Builder";

    JavaFileObject sourceFile =
        ProcessorTestUtils.simpleBuilderClass(
            packageName,
            className,
            """
                private int[] values;

                public int[] getValues() { return values; }
                public void setValues(int[] values) { this.values = values; }
                """);

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasArray build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasArrayBuilder create()"),
                "static create() missing"));
  }

  @Test
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
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Ensure builder compiles and does not use primitive type argument like <int>
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasConsumer build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasConsumerBuilder create()"),
                "static create() missing"),
        () ->
            Assertions.assertFalse(
                generatedCode.contains("<int>"), "Should not use <int> in generics"));
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
            "package "
                + packageName
                + ";\n"
                + "public class Helper {\n"
                + "  public Helper() {}\n"
                + "  private String v;\n"
                + "  public String getV() { return v; }\n"
                + "  public void setV(String v) { this.v = v; }\n"
                + "}\n");

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public OuterWithHelper build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static OuterWithHelperBuilder create()"),
                "static create() missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public OuterWithHelperBuilder helper(Helper helper)"),
                "helper(Helper) setter missing"));
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
            "package "
                + packageName
                + ";\n"
                + "public class HelperNoEmpty {\n"
                + "  private final int x;\n"
                + "  public HelperNoEmpty(int x) { this.x = x; }\n"
                + "  public int getX() { return x; }\n"
                + "}\n");

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public OuterWithNoEmptyHelper build()"),
                "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static OuterWithNoEmptyHelperBuilder create()"),
                "static create() missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains(
                    "public OuterWithNoEmptyHelperBuilder helper(HelperNoEmpty helper)"),
                "helper(HelperNoEmpty) setter missing"));
  }

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
            "package "
                + packageName
                + ";\n"
                + "public class Helper {\n"
                + "  public Helper() {}\n"
                + "}\n");

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasListCustom build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasListCustomBuilder create()"),
                "static create() missing"));
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
            "package "
                + packageName
                + ";\n"
                + "public class Helper {\n"
                + "  public Helper() {}\n"
                + "}\n");

    // When
    Compilation compilation = compile(sourceFile, helperSource);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public HasMapCustom build()"), "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static HasMapCustomBuilder create()"),
                "static create() missing"));
  }
}
