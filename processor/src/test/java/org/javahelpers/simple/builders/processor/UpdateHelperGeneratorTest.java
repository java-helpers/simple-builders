/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for {@code xyzUpdate(UnaryOperator<T>)} helper generation. */
class UpdateHelperGeneratorTest {

  @TempDir Path tempDirectory;

  @Test
  void updateHelpers_DefaultEnabled_GeneratesMethods() {
    Compilation compilation = ProcessorTestUtils.createCompiler().compile(personSource());

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonDtoBuilder");

    ProcessorAsserts.assertContaining(generated, "nameUpdate(");
    ProcessorAsserts.assertContaining(generated, "quantityUpdate(");
  }

  @Test
  void updateHelpers_DisabledByCompilerOption_DoesNotGenerateMethods() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.generateUpdateHelpers=DISABLED")
            .compile(personSource());

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonDtoBuilder");

    ProcessorAsserts.assertNotContaining(generated, "nameUpdate(");
    ProcessorAsserts.assertNotContaining(generated, "quantityUpdate(");
  }

  @Test
  void updateHelpers_CompilerOptionEnabled_GeneratesStringAndPrimitiveMethods() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.generateUpdateHelpers=ENABLED")
            .compile(personSource());

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonDtoBuilder");

    ProcessorAsserts.assertContaining(
        generated, "public PersonDtoBuilder nameUpdate(UnaryOperator<String> nameUpdater)");
    ProcessorAsserts.assertContaining(
        generated,
        "public PersonDtoBuilder quantityUpdate(UnaryOperator<Integer> quantityUpdater)");
    ProcessorAsserts.assertContaining(
        generated, "throw new IllegalStateException(\"Cannot update 'name' before it is set\")");
    ProcessorAsserts.assertContaining(
        generated, "this.quantity = changedValue(quantityUpdater.apply(this.quantity.value()));");
    assertTrue(
        generated.contains(".nameUpdate(String::trim);"),
        "String updater methods should use a type-aware example");
    assertTrue(
        generated.contains(".quantityUpdate(Math::abs);"),
        "Primitive updater methods should use a type-aware example");
  }

  @Test
  void updateHelpers_CompilerOptionEnabled_ContainsUnsetFieldGuard() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.generateUpdateHelpers=ENABLED")
            .compile(personSource());

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonDtoBuilder");

    ProcessorAsserts.assertContaining(generated, "if (!this.name.isSet())");
    ProcessorAsserts.assertContaining(generated, "Cannot update 'name' before it is set");
  }

  @Test
  void updateHelpers_RuntimeUpdatingAndUnsetFieldFailure() throws Exception {
    try (URLClassLoader classLoader = compileRuntimePerson()) {
      Class<?> builderClass = classLoader.loadClass("test.PersonDtoBuilder");
      Method create = builderClass.getMethod("create");
      Method name = builderClass.getMethod("name", String.class);
      Method quantity = builderClass.getMethod("quantity", int.class);
      Method nameUpdate = builderClass.getMethod("nameUpdate", UnaryOperator.class);
      Method quantityUpdate = builderClass.getMethod("quantityUpdate", UnaryOperator.class);
      Method build = builderClass.getMethod("build");

      Object builder = create.invoke(null);
      InvocationTargetException exception =
          assertThrows(
              InvocationTargetException.class,
              () -> nameUpdate.invoke(builder, (UnaryOperator<String>) String::trim));
      assertEquals("Cannot update 'name' before it is set", exception.getCause().getMessage());

      name.invoke(builder, "  bob ");
      nameUpdate.invoke(builder, (UnaryOperator<String>) String::trim);
      nameUpdate.invoke(builder, (UnaryOperator<String>) String::toUpperCase);
      quantity.invoke(builder, 10);
      quantityUpdate.invoke(builder, (UnaryOperator<Integer>) value -> value * 2);

      Object person = build.invoke(builder);
      assertEquals("BOB", person.getClass().getMethod("name").invoke(person));
      assertEquals(20, person.getClass().getMethod("quantity").invoke(person));
    }
  }

  @Test
  void updateHelpers_RuntimeListFieldCanBeUpdatedAfterDirectSetter() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeList()) {
      Class<?> builderClass = classLoader.loadClass("test.ListDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      Method tags = builderClass.getMethod("tags", List.class);
      Method tagsUpdate = builderClass.getMethod("tagsUpdate", UnaryOperator.class);
      Method build = builderClass.getMethod("build");

      tags.invoke(builder, List.of("a"));
      tagsUpdate.invoke(
          builder,
          (UnaryOperator<List<String>>)
              values -> {
                var updated = new ArrayList<>(values);
                updated.add("b");
                return updated;
              });

      Object dto = build.invoke(builder);
      assertEquals(List.of("a", "b"), dto.getClass().getMethod("tags").invoke(dto));
    }
  }

  @Test
  void updateHelpers_RuntimeListFieldCanBeUpdatedAfterConsumer() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeList()) {
      Class<?> builderClass = classLoader.loadClass("test.ListDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      Method tagsConsumer = builderClass.getMethod("tags", Consumer.class);
      Method tagsUpdate = builderClass.getMethod("tagsUpdate", UnaryOperator.class);
      Method build = builderClass.getMethod("build");

      tagsConsumer.invoke(
          builder,
          (Consumer<Object>)
              values -> {
                try {
                  values.getClass().getMethod("add", Object.class).invoke(values, "a");
                } catch (ReflectiveOperationException ex) {
                  throw new RuntimeException(ex);
                }
              });
      tagsUpdate.invoke(
          builder,
          (UnaryOperator<List<String>>)
              values -> {
                var updated = new ArrayList<>(values);
                updated.add("b");
                return updated;
              });

      Object dto = build.invoke(builder);
      assertEquals(List.of("a", "b"), dto.getClass().getMethod("tags").invoke(dto));
    }
  }

  @Test
  void updateHelpers_WithCopyInteraction_UsesInitialValueAsSet() {
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder(options = @SimpleBuilder.Options(
                generateUpdateHelpers = org.javahelpers.simple.builders.core.enums.OptionState.ENABLED,
                generateWithInterface = org.javahelpers.simple.builders.core.enums.OptionState.ENABLED))
            public record PersonWith(String name) implements PersonWithBuilder.With {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(source);

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonWithBuilder");

    ProcessorAsserts.assertContaining(
        generated, "public PersonWithBuilder nameUpdate(UnaryOperator<String> nameUpdater)");
    ProcessorAsserts.assertContaining(generated, "initialValue(instance.name())");
  }

  @Test
  void updateHelpers_WithCopyInteraction_UpdatesCopiedValue() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeWithPerson()) {
      Class<?> personClass = classLoader.loadClass("test.PersonWith");
      Object person = personClass.getConstructor(String.class).newInstance("  bob ");
      Method with = personClass.getMethod("with", java.util.function.Consumer.class);

      Object updated =
          with.invoke(
              person,
              (java.util.function.Consumer<Object>)
                  builder -> invokeUpdater(builder, (UnaryOperator<String>) String::toUpperCase));

      assertEquals("  BOB ", updated.getClass().getMethod("name").invoke(updated));
    }
  }

  @Test
  void updateHelpers_AnnotationOptionEnabled_GeneratesMethod() {
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;

            @SimpleBuilder(options = @SimpleBuilder.Options(
                generateUpdateHelpers = OptionState.ENABLED))
            public record AnnotatedPerson(String name) {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(source);

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "AnnotatedPersonBuilder");

    ProcessorAsserts.assertContaining(
        generated, "public AnnotatedPersonBuilder nameUpdate(UnaryOperator<String> nameUpdater)");
  }

  @Test
  void updateHelpers_ComponentDeactivation_DisablesGeneration() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.generateUpdateHelpers=ENABLED",
                "-Asimplebuilder.deactivateGenerationComponents=UpdateHelperGenerator")
            .compile(personSource());

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonDtoBuilder");

    ProcessorAsserts.assertNotContaining(generated, "nameUpdate(");
    ProcessorAsserts.assertNotContaining(generated, "quantityUpdate(");
  }

  @Test
  void updateHelpers_SameSignatureCollision_PlainSetterWins() {
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;

            import java.util.function.UnaryOperator;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record CollisionDto(String test, UnaryOperator<String> testUpdate) {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(source);

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "CollisionDtoBuilder");
    long conflictWarnings =
        compilation.diagnostics().stream()
            .filter(diagnostic -> diagnostic.getKind() == javax.tools.Diagnostic.Kind.WARNING)
            .filter(diagnostic -> diagnostic.getMessage(null).contains("Method conflict resolved"))
            .count();

    assertTrue(conflictWarnings > 0, "Expected an update-helper/setter conflict warning");
    ProcessorAsserts.assertContaining(generated, "testUpdate(UnaryOperator<String> testUpdate)");
    ProcessorAsserts.assertNotContaining(
        generated, "testUpdate(UnaryOperator<String> testUpdater)");
    ProcessorAsserts.assertContaining(
        generated, "testUpdateUpdate(UnaryOperator<UnaryOperator<String>> testUpdateUpdater)");
  }

  @Test
  void updateHelpers_SameSignatureCollision_PlainSetterCanBeCalled() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeCollision()) {
      Class<?> builderClass = classLoader.loadClass("test.CollisionDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      UnaryOperator<String> updater = String::trim;

      builderClass.getMethod("testUpdate", UnaryOperator.class).invoke(builder, updater);

      Object dto = builderClass.getMethod("build").invoke(builder);
      assertEquals(updater, dto.getClass().getMethod("testUpdate").invoke(dto));
    }
  }

  @Test
  void updateHelpers_DifferentTypeCollision_GeneratesBothMethods() {
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder
            public record DifferentTypeDto(String test, String testUpdate) {}
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(source);

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "DifferentTypeDtoBuilder");

    ProcessorAsserts.assertContaining(generated, "testUpdate(String testUpdate)");
    ProcessorAsserts.assertContaining(generated, "testUpdate(UnaryOperator<String> testUpdater)");
  }

  @Test
  void updateHelpers_DifferentTypeCollision_BothMethodsCanBeCalled() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeDifferentType()) {
      Class<?> builderClass = classLoader.loadClass("test.DifferentTypeDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      builderClass.getMethod("test", String.class).invoke(builder, "  bob ");
      builderClass.getMethod("testUpdate", String.class).invoke(builder, "updated");
      builderClass
          .getMethod("testUpdate", UnaryOperator.class)
          .invoke(builder, (UnaryOperator<String>) String::trim);

      Object dto = builderClass.getMethod("build").invoke(builder);
      assertEquals("bob", dto.getClass().getMethod("test").invoke(dto));
      assertEquals("updated", dto.getClass().getMethod("testUpdate").invoke(dto));
    }
  }

  @Test
  void updateHelpers_NullResult_NonNullFieldFailsAtBuild() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeNullResult()) {
      Class<?> builderClass = classLoader.loadClass("test.NullResultDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      Method name = builderClass.getMethod("name", String.class);
      Method nameUpdate = builderClass.getMethod("nameUpdate", UnaryOperator.class);
      Method build = builderClass.getMethod("build");

      name.invoke(builder, "x");
      assertDoesNotThrow(() -> nameUpdate.invoke(builder, (UnaryOperator<String>) value -> null));

      InvocationTargetException exception =
          assertThrows(InvocationTargetException.class, () -> build.invoke(builder));
      assertTrue(exception.getCause().getMessage().contains("marked as non-null"));
    }
  }

  @Test
  void updateHelpers_NullResult_PrimitiveFieldFailsAtBuild() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeNullResult()) {
      Class<?> builderClass = classLoader.loadClass("test.NullResultDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      Method quantity = builderClass.getMethod("quantity", int.class);
      Method quantityUpdate = builderClass.getMethod("quantityUpdate", UnaryOperator.class);
      Method build = builderClass.getMethod("build");

      quantity.invoke(builder, 1);
      assertDoesNotThrow(
          () -> quantityUpdate.invoke(builder, (UnaryOperator<Integer>) value -> null));

      InvocationTargetException exception =
          assertThrows(InvocationTargetException.class, () -> build.invoke(builder));
      assertTrue(exception.getCause().getMessage().contains("marked as non-null"));
    }
  }

  @Test
  void updateHelpers_NullResult_NullableFieldBuildsWithNull() throws Exception {
    try (URLClassLoader classLoader = compileRuntimeNullResult()) {
      Class<?> builderClass = classLoader.loadClass("test.NullResultDtoBuilder");
      Object builder = builderClass.getMethod("create").invoke(null);
      Method description = builderClass.getMethod("description", String.class);
      Method descriptionUpdate = builderClass.getMethod("descriptionUpdate", UnaryOperator.class);
      Method build = builderClass.getMethod("build");

      description.invoke(builder, "x");
      assertDoesNotThrow(
          () -> descriptionUpdate.invoke(builder, (UnaryOperator<String>) value -> null));

      Object dto = build.invoke(builder);
      assertNull(dto.getClass().getMethod("getDescription").invoke(dto));
    }
  }

  private static JavaFileObject personSource() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public record PersonDto(String name, int quantity) {}
        """);
  }

  private URLClassLoader compileRuntimePerson() throws Exception {
    return compileRuntimeSource(
        "PersonDto.java",
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public record PersonDto(String name, int quantity) {}
        """);
  }

  private URLClassLoader compileRuntimeList() throws Exception {
    return compileRuntimeSource(
        "ListDto.java",
        """
        package test;

        import java.util.List;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public record ListDto(List<String> tags, String name) {}
        """);
  }

  private URLClassLoader compileRuntimeWithPerson() throws Exception {
    return compileRuntimeSource(
        "PersonWith.java",
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
        import org.javahelpers.simple.builders.core.enums.OptionState;

        @SimpleBuilder(options = @SimpleBuilder.Options(
            generateUpdateHelpers = OptionState.ENABLED,
            generateWithInterface = OptionState.ENABLED))
        public record PersonWith(String name) implements PersonWithBuilder.With {}
        """);
  }

  private URLClassLoader compileRuntimeCollision() throws Exception {
    return compileRuntimeSource(
        "CollisionDto.java",
        """
        package test;

        import java.util.function.UnaryOperator;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public record CollisionDto(String test, UnaryOperator<String> testUpdate) {}
        """);
  }

  private URLClassLoader compileRuntimeDifferentType() throws Exception {
    return compileRuntimeSource(
        "DifferentTypeDto.java",
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public record DifferentTypeDto(String test, String testUpdate) {}
        """);
  }

  private URLClassLoader compileRuntimeNullResult() throws Exception {
    return compileRuntimeSources(
        Map.of(
            "NotNull.java",
            """
            package jakarta.validation.constraints;

            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.FIELD, ElementType.PARAMETER})
            public @interface NotNull {}
            """,
            "NullResultDto.java",
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import jakarta.validation.constraints.NotNull;

            @SimpleBuilder
            public class NullResultDto {
              private String name;
              private int quantity;
              private String description;

              public String getName() { return name; }
              public void setName(@NotNull String name) { this.name = name; }
              public int getQuantity() { return quantity; }
              public void setQuantity(int quantity) { this.quantity = quantity; }
              public String getDescription() { return description; }
              public void setDescription(String description) { this.description = description; }
            }
            """));
  }

  private URLClassLoader compileRuntimeSource(String fileName, String source) throws Exception {
    return compileRuntimeSources(Map.of(fileName, source));
  }

  private URLClassLoader compileRuntimeSources(Map<String, String> sources) throws Exception {
    Path sourceDirectory = Files.createDirectories(tempDirectory.resolve("src/test"));
    Path classDirectory = Files.createDirectories(tempDirectory.resolve("classes"));
    for (Map.Entry<String, String> source : sources.entrySet()) {
      Files.writeString(sourceDirectory.resolve(source.getKey()), source.getValue());
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      fileManager.setLocation(
          javax.tools.StandardLocation.CLASS_OUTPUT, List.of(classDirectory.toFile()));
      Iterable<? extends JavaFileObject> sourceFiles =
          fileManager.getJavaFileObjects(
              sources.keySet().stream()
                  .map(sourceDirectory::resolve)
                  .map(Path::toFile)
                  .toArray(java.io.File[]::new));
      List<String> options =
          List.of(
              "-classpath",
              System.getProperty("java.class.path"),
              "-processorpath",
              System.getProperty("java.class.path"),
              "-Asimplebuilder.generateUpdateHelpers=ENABLED");
      JavaCompiler.CompilationTask task =
          compiler.getTask(null, fileManager, null, options, null, sourceFiles);
      task.setProcessors(List.<Processor>of(new BuilderProcessor()));
      assertTrue(task.call(), "Runtime test source should compile");
    }

    return new URLClassLoader(
        new URL[] {classDirectory.toUri().toURL()},
        UpdateHelperGeneratorTest.class.getClassLoader());
  }

  private static void invokeUpdater(Object builder, UnaryOperator<String> updater) {
    try {
      builder.getClass().getMethod("nameUpdate", UnaryOperator.class).invoke(builder, updater);
    } catch (ReflectiveOperationException ex) {
      throw new RuntimeException(ex);
    }
  }
}
