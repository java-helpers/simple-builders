package org.javahelpers.simple.builders.processor;

import com.google.testing.compile.Compilation;
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
}
