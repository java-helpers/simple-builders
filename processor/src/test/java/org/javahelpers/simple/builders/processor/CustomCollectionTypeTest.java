/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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
package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for custom collection implementations with multiple type parameters where only a subset is
 * used for the collection interface.
 *
 * <p>This tests the critical edge case where a class like {@code ExampleClass<X,Y> implements
 * List<Y>} should correctly extract Y as the List element type, not both X and Y.
 */
class CustomCollectionTypeTest {

  private static Compilation compile(JavaFileObject... sources) {
    return ProcessorTestUtils.createCompiler().compile(sources);
  }

  @Test
  void customListWithMultipleTypeParameters_shouldNotGenerateVarargsHelper() {
    // Custom List with 2 type parameters - should be treated as generic type, not TypeNameList
    // This means NO varargs helper methods should be generated
    JavaFileObject customList =
        JavaFileObjects.forSourceLines(
            "test.CustomList",
            "package test;",
            "import java.util.ArrayList;",
            "public class CustomList<X, Y> extends ArrayList<Y> {",
            "  private X metadata;",
            "  public CustomList(X metadata) { this.metadata = metadata; }",
            "  public X getMetadata() { return metadata; }",
            "}");

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "CustomListDto",
            """
                private final CustomList<String, Integer> numbers;

                public CustomListDto(CustomList<String, Integer> numbers) {
                  this.numbers = numbers;
                }

                public CustomList<String, Integer> getNumbers() {
                  return numbers;
                }
            """);

    Compilation compilation = compile(customList, dto);
    String generatedCode = loadGeneratedSource(compilation, "CustomListDtoBuilder");
    assertGenerationSucceeded(compilation, "CustomListDtoBuilder", generatedCode);

    // Should NOT generate varargs method for custom collection types
    ProcessorAsserts.assertNotContaining(
        generatedCode, "public CustomListDtoBuilder numbers(Integer... numbers)");

    // Should still have the basic setter
    ProcessorAsserts.assertContaining(
        generatedCode, "public CustomListDtoBuilder numbers(CustomList<String, Integer> numbers)");
  }

  @Test
  void standardArrayListWithSingleTypeParameter_shouldGenerateVarargsHelper() {
    // Standard ArrayList should generate varargs helper
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "ArrayListDto",
            """
                private final java.util.ArrayList<Integer> numbers;

                public ArrayListDto(java.util.ArrayList<Integer> numbers) {
                  this.numbers = numbers;
                }

                public java.util.ArrayList<Integer> getNumbers() {
                  return numbers;
                }
            """);

    Compilation compilation = compile(dto);
    String generatedCode = loadGeneratedSource(compilation, "ArrayListDtoBuilder");
    assertGenerationSucceeded(compilation, "ArrayListDtoBuilder", generatedCode);

    // Should generate varargs method for standard ArrayList
    ProcessorAsserts.assertContaining(
        generatedCode, "public ArrayListDtoBuilder numbers(Integer... numbers)");
  }

  @Test
  void customSetWithMultipleTypeParameters_shouldNotGenerateVarargsHelper() {
    // Custom Set with 2 type parameters - should be treated as generic type, not TypeNameSet
    JavaFileObject customSet =
        JavaFileObjects.forSourceLines(
            "test.CustomSet",
            "package test;",
            "import java.util.HashSet;",
            "public class CustomSet<X, Y> extends HashSet<Y> {",
            "  private X metadata;",
            "  public CustomSet(X metadata) { this.metadata = metadata; }",
            "  public X getMetadata() { return metadata; }",
            "}");

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "CustomSetDto",
            """
                private final CustomSet<String, String> tags;

                public CustomSetDto(CustomSet<String, String> tags) {
                  this.tags = tags;
                }

                public CustomSet<String, String> getTags() {
                  return tags;
                }
            """);

    Compilation compilation = compile(customSet, dto);
    String generatedCode = loadGeneratedSource(compilation, "CustomSetDtoBuilder");
    assertGenerationSucceeded(compilation, "CustomSetDtoBuilder", generatedCode);

    // Should NOT generate varargs method for custom collection types
    ProcessorAsserts.assertNotContaining(
        generatedCode, "public CustomSetDtoBuilder tags(String... tags)");

    // Should still have the basic setter
    ProcessorAsserts.assertContaining(
        generatedCode, "public CustomSetDtoBuilder tags(CustomSet<String, String> tags)");
  }

  @Test
  void customMapWithMultipleTypeParameters_shouldNotGenerateVarargsHelper() {
    // Custom Map with 3 type parameters - should be treated as generic type, not TypeNameMap
    JavaFileObject customMap =
        JavaFileObjects.forSourceLines(
            "test.CustomMap",
            "package test;",
            "import java.util.HashMap;",
            "public class CustomMap<X, Y, Z> extends HashMap<Y, Z> {",
            "  private X metadata;",
            "  public CustomMap(X metadata) { this.metadata = metadata; }",
            "  public X getMetadata() { return metadata; }",
            "}");

    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "CustomMapDto",
            """
                private final CustomMap<Integer, String, Double> data;

                public CustomMapDto(CustomMap<Integer, String, Double> data) {
                  this.data = data;
                }

                public CustomMap<Integer, String, Double> getData() {
                  return data;
                }
            """);

    Compilation compilation = compile(customMap, dto);
    String generatedCode = loadGeneratedSource(compilation, "CustomMapDtoBuilder");
    assertGenerationSucceeded(compilation, "CustomMapDtoBuilder", generatedCode);

    // Should NOT generate varargs method for custom collection types
    ProcessorAsserts.assertNotContaining(
        generatedCode, "public CustomMapDtoBuilder data(Map.Entry<String, Double>... data)");

    // Should still have the basic setter
    ProcessorAsserts.assertContaining(
        generatedCode, "public CustomMapDtoBuilder data(CustomMap<Integer, String, Double> data)");
  }
}
