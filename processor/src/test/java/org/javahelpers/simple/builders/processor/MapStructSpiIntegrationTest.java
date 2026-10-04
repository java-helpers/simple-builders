/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons with the following conditions:
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.ap.MappingProcessor;

/**
 * Integration test for the MapStruct SPI implementations: {@code SimpleBuildersBuilderProvider}
 * supplies the builder, {@code SimpleBuildersAccessorNamingStrategy} hides the generated helper
 * methods so MapStruct neither binds them nor reports them as unmapped target properties.
 */
class MapStructSpiIntegrationTest {

  private static final JavaFileObject PERSON_DTO =
      ProcessorTestUtils.forSource(
          """
          package test;

          import java.util.List;
          import java.util.Optional;
          import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

          @SimpleBuilder
          public class PersonDto {
            private String name;
            private List<String> nicknames;
            private Optional<String> email;

            public String getName() {
              return name;
            }

            public List<String> getNicknames() {
              return nicknames;
            }

            public Optional<String> getEmail() {
              return email;
            }
          }
          """);

  private static final JavaFileObject PERSON_DTO_MAPPER =
      ProcessorTestUtils.forSource(
          """
          package test;

          import org.mapstruct.Mapper;

          @Mapper
          public interface PersonDtoMapper {

            PersonDto copy(PersonDto source);
          }
          """);

  private static Compiler compiler() {
    return Compiler.javac()
        .withProcessors(new BuilderProcessor(), new MappingProcessor())
        .withOptions(
            "-Amapstruct.suppressGeneratorTimestamp=true",
            "-Amapstruct.suppressGeneratorVersionInfoComment=true");
  }

  @Test
  void mapStruct_shouldUseGeneratedBuilder() {
    Compilation compilation = compiler().compile(PERSON_DTO, PERSON_DTO_MAPPER);
    assertThat(compilation).succeeded();
    ProcessorTestUtils.printDiagnosticsOnVerbose(compilation);

    String mapperImpl = loadGeneratedSource(compilation, "PersonDtoMapperImpl");
    assertTrue(
        mapperImpl.contains("PersonDtoBuilder.create()"),
        "MapStruct should instantiate the generated builder");
    assertTrue(mapperImpl.contains(".build()"), "MapStruct should finish via build()");
  }

  @Test
  void mapStruct_shouldNotReportHelpersAsUnmappedTargetProperties() {
    Compilation compilation = compiler().compile(PERSON_DTO, PERSON_DTO_MAPPER);
    assertThat(compilation).succeeded();
    ProcessorTestUtils.printDiagnosticsOnVerbose(compilation);

    String warnings =
        compilation.warnings().stream()
            .map(diagnostic -> diagnostic.getMessage(null))
            .reduce("", (left, right) -> left + "\n" + right);
    assertFalse(
        warnings.toLowerCase().contains("unmapped target property"),
        "Generated helper methods (add2*, *Update, Supplier/Consumer overloads) must not "
            + "surface as unmapped target properties, got: "
            + warnings);
  }
}
