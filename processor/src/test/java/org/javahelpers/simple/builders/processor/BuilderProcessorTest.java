package org.javahelpers.simple.builders.processor;

import com.google.testing.compile.Compilation;
import java.util.List;
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
            List.of(
                "    private String name;",
                "    private int age;",
                "",
                "    public String getName() {",
                "        return name;",
                "    }",
                "",
                "    public void setName(String name) {",
                "        this.name = name;",
                "    }",
                "",
                "    public int getAge() {",
                "        return age;",
                "    }",
                "",
                "    public void setAge(int age) {",
                "        this.age = age;",
                "    }"));

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Verify the generated code contains expected methods
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + className + " build()"),
                "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + builderClassName + " name(String name)"),
                "name(String) setter missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + builderClassName + " age(int age)"),
                "age(int) setter missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static " + builderClassName + " create()"),
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
            packageName, className, List.of("    // No fields or methods"));

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    // Verify the generated code contains expected methods
    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + className + " build()"),
                "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static " + builderClassName + " create()"),
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
            List.of(
                "    private int a;",
                "",
                "    public int getA() { return a; }",
                "    public void setA(int a) { this.a = a; }"));

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + className + " build()"),
                "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + builderClassName + " a(int a)"),
                "a(int) setter missing"));
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
            List.of(
                "    private java.util.List<String> names;",
                "",
                "    public java.util.List<String> getNames() { return names; }",
                "    public void setNames(java.util.List<String> names) { this.names = names; }"));

    // When
    Compilation compilation = compile(sourceFile);

    // Then
    String generatedCode =
        ProcessorTestUtils.assertSucceededAndGetGenerated(compilation, builderClassName);

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public " + className + " build()"),
                "build() method missing"),
        () ->
            Assertions.assertTrue(
                generatedCode.contains("public static " + builderClassName + " create()"),
                "static create() missing"));
  }
}
