package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.processor.testing.ProcessorAsserts.contains;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createMockAnnotation;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.printDiagnosticsOnVerbose;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.junit.jupiter.api.Test;

/**
 * Test that TYPE_USE annotations are properly handled in generated builders.
 *
 * <p>TYPE_USE annotations apply to type usage, not declarations:
 *
 * <pre>
 * List&lt;@NotNull String&gt; items;
 * </pre>
 *
 * <p>These annotations should be preserved on:
 *
 * <ul>
 *   <li>Builder field member variables (TrackedValue fields)
 *   <li>Method parameters in setter methods
 *   <li>Method parameters in helper methods (add2FieldName, etc.)
 *   <li>Return types where applicable
 * </ul>
 *
 * @see <a href="https://github.com/java-helpers/simple-builders/issues/92">Issue #92</a>
 */
class TypeUseAnnotationTest {

  private Compilation compileSources(JavaFileObject... sources) {
    Compilation compilation = createCompiler().compile(sources);
    printDiagnosticsOnVerbose(compilation);
    return compilation;
  }

  @Test
  void typeUseAnnotations_onListElements_copiedToBuilderMethods() {
    String packageName = "test.typeuse.list";

    JavaFileObject notNullAnnotation =
        createMockAnnotation(packageName + ".annotations", "NotNull", "ElementType.TYPE_USE");

    JavaFileObject person =
        JavaFileObjects.forSourceString(
            packageName + ".Person",
            """
            package test.typeuse.list;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import test.typeuse.list.annotations.NotNull;
            import java.util.List;

            @SimpleBuilder
            public class Person {
              private final List<@NotNull String> nicknames;

              public Person(List<@NotNull String> nicknames) {
                this.nicknames = nicknames;
              }

              public List<@NotNull String> getNicknames() { return nicknames; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, person);
    String generatedCode = loadGeneratedSource(compilation, "PersonBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "PersonBuilder", generatedCode);

    // Verify that TYPE_USE annotations are preserved in builder field
    ProcessorAsserts.assertingResult(
        generatedCode,
        // Builder field should have TYPE_USE annotation
        contains("TrackedValue<List<@NotNull String>> nicknames"),
        // Setter method parameter should have TYPE_USE annotation
        contains("nicknames(List<@NotNull String> nicknames)"),
        // VarArgs method parameter should have TYPE_USE annotation
        contains("nicknames(@NotNull String... nicknames)"),
        // add2Nicknames method parameter should have TYPE_USE annotation
        contains("add2Nicknames(@NotNull String element)"));
  }

  @Test
  void typeUseAnnotations_onSetElements_copiedToBuilderMethods() {
    String packageName = "test.typeuse.set";

    JavaFileObject notNullAnnotation =
        createMockAnnotation(packageName + ".annotations", "NotNull", "ElementType.TYPE_USE");

    JavaFileObject product =
        JavaFileObjects.forSourceString(
            packageName + ".Product",
            """
            package test.typeuse.set;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import test.typeuse.set.annotations.NotNull;
            import java.util.Set;

            @SimpleBuilder
            public class Product {
              private final Set<@NotNull String> tags;

              public Product(Set<@NotNull String> tags) {
                this.tags = tags;
              }

              public Set<@NotNull String> getTags() { return tags; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, product);
    String generatedCode = loadGeneratedSource(compilation, "ProductBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ProductBuilder", generatedCode);

    // Verify that TYPE_USE annotations are preserved
    ProcessorAsserts.assertingResult(
        generatedCode,
        // Builder field should have TYPE_USE annotation
        contains("TrackedValue<Set<@NotNull String>> tags"),
        // Setter method parameter should have TYPE_USE annotation
        contains("tags(Set<@NotNull String> tags)"),
        // VarArgs method parameter should have TYPE_USE annotation
        contains("tags(@NotNull String... tags)"),
        // add2Tags method parameter should have TYPE_USE annotation
        contains("add2Tags(@NotNull String element)"));
  }

  @Test
  void typeUseAnnotations_onMapValues_copiedToBuilderMethods() {
    String packageName = "test.typeuse.map";

    JavaFileObject notNullAnnotation =
        createMockAnnotation(packageName + ".annotations", "NotNull", "ElementType.TYPE_USE");

    JavaFileObject config =
        JavaFileObjects.forSourceString(
            packageName + ".Config",
            """
            package test.typeuse.map;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import test.typeuse.map.annotations.NotNull;
            import java.util.Map;

            @SimpleBuilder
            public class Config {
              private final Map<String, @NotNull String> properties;

              public Config(Map<String, @NotNull String> properties) {
                this.properties = properties;
              }

              public Map<String, @NotNull String> getProperties() { return properties; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, config);
    String generatedCode = loadGeneratedSource(compilation, "ConfigBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ConfigBuilder", generatedCode);

    // Verify that TYPE_USE annotations are preserved
    ProcessorAsserts.assertingResult(
        generatedCode,
        // Builder field should have TYPE_USE annotation
        contains("TrackedValue<Map<String, @NotNull String>> properties"),
        // Setter method parameter should have TYPE_USE annotation
        contains("properties(Map<String, @NotNull String> properties)"));
  }

  @Test
  void typeUseAnnotations_multipleOnSameType_allCopied() {
    String packageName = "test.typeuse.multiple";

    JavaFileObject notNullAnnotation =
        createMockAnnotation(packageName + ".annotations", "NotNull", "ElementType.TYPE_USE");

    JavaFileObject validAnnotation =
        createMockAnnotation(packageName + ".annotations", "Validated", "ElementType.TYPE_USE");

    JavaFileObject data =
        JavaFileObjects.forSourceString(
            packageName + ".Data",
            """
            package test.typeuse.multiple;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import test.typeuse.multiple.annotations.NotNull;
            import test.typeuse.multiple.annotations.Validated;
            import java.util.List;

            @SimpleBuilder
            public class Data {
              private final List<@NotNull @Validated String> values;

              public Data(List<@NotNull @Validated String> values) {
                this.values = values;
              }

              public List<@NotNull @Validated String> getValues() { return values; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, validAnnotation, data);
    String generatedCode = loadGeneratedSource(compilation, "DataBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "DataBuilder", generatedCode);

    // Verify that both TYPE_USE annotations are preserved
    ProcessorAsserts.assertingResult(
        generatedCode,
        // Builder field should have both TYPE_USE annotations
        contains("TrackedValue<List<@NotNull @Validated String>> values"),
        // Method parameters should have both annotations
        contains("values(@NotNull @Validated String... values)"),
        contains("add2Values(@NotNull @Validated String element)"));
  }

  @Test
  void typeUseAnnotations_onNestedGenerics_copiedCorrectly() {
    String packageName = "test.typeuse.nested";

    JavaFileObject notNullAnnotation =
        createMockAnnotation(packageName + ".annotations", "NotNull", "ElementType.TYPE_USE");

    JavaFileObject container =
        JavaFileObjects.forSourceString(
            packageName + ".Container",
            """
            package test.typeuse.nested;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import test.typeuse.nested.annotations.NotNull;
            import java.util.List;
            import java.util.Map;

            @SimpleBuilder
            public class Container {
              private final List<Map<String, @NotNull String>> data;

              public Container(List<Map<String, @NotNull String>> data) {
                this.data = data;
              }

              public List<Map<String, @NotNull String>> getData() { return data; }
            }
            """);

    Compilation compilation = compileSources(notNullAnnotation, container);
    String generatedCode = loadGeneratedSource(compilation, "ContainerBuilder");
    ProcessorAsserts.assertGenerationSucceeded(compilation, "ContainerBuilder", generatedCode);

    // Verify that TYPE_USE annotations are preserved in nested generics
    ProcessorAsserts.assertingResult(
        generatedCode,
        // Builder field should have TYPE_USE annotation in nested position
        contains("TrackedValue<List<Map<String, @NotNull String>>> data"),
        // Setter method parameter should have TYPE_USE annotation
        contains("data(List<Map<String, @NotNull String>> data)"));
  }
}
