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

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/** Compile-testing coverage for builder generation and usage package scopes. */
class BuilderScopeProcessingTest {

  @Test
  void bothScopesUnset_UsesBuilderOfReferencedType() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .compile(dto("test", "ScopeDto", "ReferencedDto"), referencedDto("test"));

    assertThat(compilation).succeeded();
    assertBuilderConsumer(compilation, "ScopeDtoBuilder", "ReferencedDtoBuilder");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "ScopeDtoBuilder"),
        "public ScopeDtoBuilder referenced(ReferencedDto referenced)");
  }

  @Test
  void usageScopeOnly_TrustsReferencedTypeGeneratedInSameCompilation() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderUsagePackages=test")
            .compile(dto("test", "ScopeDto", "ReferencedDto"), referencedDto("test"));

    assertThat(compilation).succeeded();
    assertBuilderConsumer(compilation, "ScopeDtoBuilder", "ReferencedDtoBuilder");
  }

  @Test
  void generationScope_IncludesExactPackageAndSubpackages() {
    Compilation exact =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderGenerationPackages=test")
            .compile(dto("test", "ExactDto", "ReferencedDto"), referencedDto("test"));
    Compilation subpackage =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderGenerationPackages=test")
            .compile(dto("test.sub", "SubDto", "ReferencedDto"), referencedDto("test.sub"));

    assertThat(exact).succeeded();
    assertThat(subpackage).succeeded();
    assertBuilderConsumer(exact, "ExactDtoBuilder", "ReferencedDtoBuilder");
    assertBuilderConsumer(subpackage, "SubDtoBuilder", "ReferencedDtoBuilder");
  }

  @Test
  void generationScope_ExcludesDtoOutsideScope() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions("-Asimplebuilder.builderGenerationPackages=other.pkg")
            .compile(dto("test", "OutOfScopeDto", "ReferencedDto"), referencedDto("test"));

    assertThat(compilation).succeeded();
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "OutOfScopeDto", "An out-of-scope DTO must not get a builder");
  }

  @Test
  void usageScope_RequiresExistingPrecompiledBuilder() {
    JavaFileObject dto = dto("test", "LibraryUsageDto", "LibraryDto", "lib");
    JavaFileObject libraryDto = referencedDto("lib", "LibraryDto");

    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.builderGenerationPackages=test",
                "-Asimplebuilder.builderUsagePackages=lib")
            .compile(dto, libraryDto);

    assertThat(compilation).succeeded();
    String generated =
        ProcessorTestUtils.loadGeneratedSource(compilation, "LibraryUsageDtoBuilder");
    ProcessorAsserts.assertContaining(
        generated, "public LibraryUsageDtoBuilder referenced(LibraryDto referenced)");
    ProcessorAsserts.assertNotContaining(
        generated, "referencedBuilderConsumer", "LibraryDtoBuilder");
    ProcessorAsserts.assertNoBuilderGenerated(
        compilation, "LibraryDto", "The library type must not be generated in this compilation");
  }

  @Test
  void usageScope_ReferencesBuilderWhenGenerationScopeAlsoIncludesLibrary() {
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.builderGenerationPackages=test,lib",
                "-Asimplebuilder.builderUsagePackages=lib")
            .compile(
                dto("test", "LibraryUsageDto", "LibraryDto", "lib"),
                referencedDto("lib", "LibraryDto"));

    assertThat(compilation).succeeded();
    assertBuilderConsumer(compilation, "LibraryUsageDtoBuilder", "LibraryDtoBuilder");
    ProcessorAsserts.assertContaining(
        ProcessorTestUtils.loadGeneratedSource(compilation, "LibraryDtoBuilder"),
        "public LibraryDto build()");
  }

  @Test
  void inlineOptions_ConfigureBuilderScopes() {
    JavaFileObject dto =
        ProcessorTestUtils.forSource(
            """
            package test;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

            @SimpleBuilder(options = @SimpleBuilder.Options(
                builderGenerationPackages = "test",
                builderUsagePackages = "test"
            ))
            public class InlineScopeDto {
              private ReferencedDto referenced;
              public ReferencedDto getReferenced() { return referenced; }
              public void setReferenced(ReferencedDto referenced) { this.referenced = referenced; }
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler().compile(dto, referencedDto("test"));

    assertThat(compilation).succeeded();
    assertBuilderConsumer(compilation, "InlineScopeDtoBuilder", "ReferencedDtoBuilder");
  }

  @Test
  void optOutTakesPrecedenceOverScopes() {
    Compilation optedOut =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.builderGenerationPackages=test",
                "-Asimplebuilder.builderUsagePackages=test")
            .compile(dto("test", "OptedOutFieldDto", "OptedOutDto"), optedOutDto());
    Compilation unannotated =
        ProcessorTestUtils.createCompiler()
            .withOptions(
                "-Asimplebuilder.builderGenerationPackages=test",
                "-Asimplebuilder.builderUsagePackages=test")
            .compile(dto("test", "UnannotatedFieldDto", "UnannotatedDto"), unannotatedDto());

    assertThat(optedOut).succeeded();
    assertThat(unannotated).succeeded();
    assertNoBuilderConsumer(
        optedOut, "OptedOutFieldDtoBuilder", "OptedOutDto", "OptedOutDtoBuilder");
    assertNoBuilderConsumer(
        unannotated, "UnannotatedFieldDtoBuilder", "UnannotatedDto", "UnannotatedDtoBuilder");
  }

  private static JavaFileObject dto(String packageName, String className, String fieldType) {
    return dto(packageName, className, fieldType, packageName);
  }

  private static JavaFileObject dto(
      String packageName, String className, String fieldType, String fieldPackage) {
    String importLine =
        packageName.equals(fieldPackage) ? "" : "import " + fieldPackage + "." + fieldType + ";\n";
    return ProcessorTestUtils.forSource(
        """
        package %s;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
        %s
        @SimpleBuilder
        public class %s {
          private %s referenced;
          public %s getReferenced() { return referenced; }
          public void setReferenced(%s referenced) { this.referenced = referenced; }
        }
        """
            .formatted(packageName, importLine, className, fieldType, fieldType, fieldType));
  }

  private static JavaFileObject referencedDto(String packageName) {
    return referencedDto(packageName, "ReferencedDto");
  }

  private static JavaFileObject referencedDto(String packageName, String className) {
    return ProcessorTestUtils.forSource(
        """
        package %s;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
        @SimpleBuilder
        public class %s { public %s() {} }
        """
            .formatted(packageName, className, className));
  }

  private static JavaFileObject optedOutDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;
        import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
        @SimpleBuilder
        @Ignore4BuilderGeneration
        public class OptedOutDto { public OptedOutDto() {} }
        """);
  }

  private static JavaFileObject unannotatedDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;
        public class UnannotatedDto { public UnannotatedDto() {} }
        """);
  }

  private static void assertBuilderConsumer(
      Compilation compilation, String builderName, String referencedBuilderName) {
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, builderName);
    ProcessorAsserts.assertContaining(
        generated,
        "referencedBuilderConsumer",
        referencedBuilderName + " builder",
        "referencedBuilderConsumer.accept(builder)");
  }

  private static void assertNoBuilderConsumer(
      Compilation compilation,
      String builderName,
      String fieldTypeName,
      String referencedBuilderName) {
    String generated = ProcessorTestUtils.loadGeneratedSource(compilation, builderName);
    ProcessorAsserts.assertContaining(generated, "referenced(" + fieldTypeName + " referenced)");
    ProcessorAsserts.assertNotContaining(
        generated, "referencedBuilderConsumer", referencedBuilderName);
  }
}
