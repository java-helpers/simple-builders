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
    Assertions.assertTrue(
        generatedCode.contains("public " + className + " build()"), "build() method missing");
    Assertions.assertTrue(
        generatedCode.contains("public " + builderClassName + " name(String name)"),
        "name(String) setter missing");
    Assertions.assertTrue(
        generatedCode.contains("public " + builderClassName + " age(int age)"),
        "age(int) setter missing");
    Assertions.assertTrue(
        generatedCode.contains("public static " + builderClassName + " create()"),
        "static create() missing");
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
    Assertions.assertTrue(
        generatedCode.contains("public " + className + " build()"), "build() method missing");
    Assertions.assertTrue(
        generatedCode.contains("public static " + builderClassName + " create()"),
        "static create() missing");
  }
}
