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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/** Tests for {@code xyzUpdate(UnaryOperator<T>)} helper generation. */
class UpdateHelperGeneratorTest {

  @Test
  void updateHelpers_DefaultEnabled_GeneratesMethodsWithSignaturesGuardAndExamples() {
    Compilation compilation = ProcessorTestUtils.createCompiler().compile(personSource());

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "PersonDtoBuilder");

    // Exact generated signatures for reference and primitive fields
    ProcessorAsserts.assertContaining(
        generated, "public PersonDtoBuilder nameUpdate(UnaryOperator<String> nameUpdater)");
    ProcessorAsserts.assertContaining(
        generated,
        "public PersonDtoBuilder quantityUpdate(UnaryOperator<Integer> quantityUpdater)");

    // Unset-field guard and update code
    ProcessorAsserts.assertContaining(generated, "if (!this.name.isSet())");
    ProcessorAsserts.assertContaining(
        generated, "throw new IllegalStateException(\"Cannot update 'name' before it is set\")");
    ProcessorAsserts.assertContaining(
        generated, "this.quantity = changedValue(quantityUpdater.apply(this.quantity.value()));");

    // Method-level Javadoc examples initialize the field, then update it
    ProcessorAsserts.assertContaining(
        generated, "builder.name(\"example value\").nameUpdate(String::trim);");
    ProcessorAsserts.assertContaining(generated, "builder.quantity(42).quantityUpdate(Math::abs);");

    // Class-level example chain contains the updater as a separate fragment line
    ProcessorAsserts.assertContaining(generated, ".nameUpdate(String::trim)");
    ProcessorAsserts.assertContaining(generated, ".quantityUpdate(Math::abs)");
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
            .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.WARNING)
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
  void updateHelpers_WithCopyInteraction_UsesInitialValueAsSet() {
    JavaFileObject source =
        ProcessorTestUtils.forSource(
            """
            package test;

            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.enums.OptionState;

            @SimpleBuilder(options = @SimpleBuilder.Options(
                generateUpdateHelpers = OptionState.ENABLED,
                generateWithInterface = OptionState.ENABLED))
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
  void updateHelpers_TypeAwareExamples_AndNestedBuilderGuidance() {
    JavaFileObject dto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "KitchenSinkDto",
            """
                private String title;
                private java.time.LocalDate date;
                private Boolean flag;
                private java.util.List<String> tags;
                private Mannschaft team;
                private HelperPlain helper;
                private String[] names;
                public String getTitle() { return title; }
                public void setTitle(String title) { this.title = title; }
                public java.time.LocalDate getDate() { return date; }
                public void setDate(java.time.LocalDate date) { this.date = date; }
                public Boolean getFlag() { return flag; }
                public void setFlag(Boolean flag) { this.flag = flag; }
                public java.util.List<String> getTags() { return tags; }
                public void setTags(java.util.List<String> tags) { this.tags = tags; }
                public Mannschaft getTeam() { return team; }
                public void setTeam(Mannschaft team) { this.team = team; }
                public HelperPlain getHelper() { return helper; }
                public void setHelper(HelperPlain helper) { this.helper = helper; }
                public String[] getNames() { return names; }
                public void setNames(String[] names) { this.names = names; }
            """);

    JavaFileObject mannschaft =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "Mannschaft",
            """
                private String name;
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            """);

    JavaFileObject helper =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class HelperPlain {
              public HelperPlain(String arg) {}
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(dto, mannschaft, helper);

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "KitchenSinkDtoBuilder");

    // Type-aware update expressions for common JDK types
    ProcessorAsserts.assertContaining(
        generated, "builder.date(LocalDate.now()).dateUpdate(value -> value.plusDays(1));");
    ProcessorAsserts.assertContaining(generated, "builder.flag(true).flagUpdate(value -> !value);");
    ProcessorAsserts.assertContaining(
        generated,
        "builder.tags(List.of(\"example value\")).tagsUpdate(list -> list.stream().sorted().toList());");
    ProcessorAsserts.assertContaining(
        generated, "builder.title(\"example value\").titleUpdate(String::trim);");

    // No artificial identity fallback anywhere
    ProcessorAsserts.assertNotContaining(generated, "UnaryOperator.identity()");

    // Arrays and unsupported types get an update method but no example
    ProcessorAsserts.assertContaining(generated, "namesUpdate(");
    ProcessorAsserts.assertNotContaining(generated, ".namesUpdate(");
    ProcessorAsserts.assertContaining(generated, "helperUpdate(");
    ProcessorAsserts.assertNotContaining(generated, ".helperUpdate(");

    // Nested builder DTO gets the update method with builder-consumer guidance but no example
    ProcessorAsserts.assertContaining(
        generated,
        "public KitchenSinkDtoBuilder teamUpdate(UnaryOperator<Mannschaft> teamUpdater)");
    ProcessorAsserts.assertContaining(
        generated, "prefer the builder-consumer helper {@link #team(Consumer)}.");
    ProcessorAsserts.assertNotContaining(generated, ".teamUpdate(");
  }

  @Test
  void updateHelpers_GeneratedApi_IsUsableFromCompiledSource() {
    JavaFileObject usage =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class Usage {
              public static PersonDto build() {
                return PersonDtoBuilder.create()
                    .name("  bob ")
                    .nameUpdate(String::trim)
                    .quantity(-10)
                    .quantityUpdate(Math::abs)
                    .build();
              }
            }
            """);

    Compilation compilation = ProcessorTestUtils.createCompiler().compile(personSource(), usage);

    assertThat(compilation).succeeded();
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
}
