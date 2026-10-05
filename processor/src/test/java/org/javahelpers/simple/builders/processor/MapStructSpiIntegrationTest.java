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
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.assertNoWarningContaining;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.ap.MappingProcessor;

/**
 * Integration test for the MapStruct SPI implementations: {@code MapStructBuilderProvider} supplies
 * the builder, {@code MapStructAccessorNamingStrategy} hides the generated helper methods so
 * MapStruct neither binds them nor reports them as unmapped target properties.
 */
class MapStructSpiIntegrationTest {

  @Test
  void mapStruct_shouldUseGeneratedBuilder() {
    Compilation compilation = mapStructCompiler().compile(personDto(), personDtoMapper());
    assertThat(compilation).succeeded();

    String mapperImpl = loadGeneratedSource(compilation, "PersonDtoMapperImpl");
    assertTrue(
        mapperImpl.contains("PersonDtoBuilder.create()"),
        "MapStruct should instantiate the generated builder");
    assertTrue(mapperImpl.contains(".build()"), "MapStruct should finish via build()");
  }

  @Test
  void mapStruct_processorOrderReversed_shouldStillUseGeneratedBuilder() {
    // MapStruct processing the mapper before BuilderProcessor's first round must not lose the
    // builder: the marked bean defers via TypeHierarchyErroneousException until the registry
    // holds the planned builder
    Compilation compilation =
        mapStructCompiler(new MappingProcessor(), new BuilderProcessor())
            .compile(personDto(), personDtoMapper());
    assertThat(compilation).succeeded();

    String mapperImpl = loadGeneratedSource(compilation, "PersonDtoMapperImpl");
    assertTrue(
        mapperImpl.contains("PersonDtoBuilder.create()"),
        "Reversed processor order must still bind the generated builder");
    assertTrue(mapperImpl.contains(".build()"), "MapStruct should finish via build()");
  }

  @Test
  void mapStruct_shouldNotReportHelpersAsUnmappedTargetProperties() {
    Compilation compilation = mapStructCompiler().compile(personDto(), personDtoMapper());
    assertThat(compilation).succeeded();

    assertNoWarningContaining(compilation, "unmapped target property");
  }

  @Test
  void mapStruct_disabledIntegration_shouldMapViaSetters() {
    // Our AdditionalSupportedOptionsProvider declares the option, so MapStruct forwards the -A
    // value into the SPI environment's options
    Compilation compilation =
        mapStructCompiler()
            .withOptions("-Asimplebuilder.usingMapStructIntegration=DISABLED")
            .compile(mutableDto(), mutableDtoMapper());
    assertThat(compilation).succeeded();

    String mapperImpl = loadGeneratedSource(compilation, "MutableDtoMapperImpl");
    assertFalse(
        mapperImpl.contains("MutableDtoBuilder"),
        "Disabled integration must leave the generated builder unused");
    assertTrue(mapperImpl.contains(".setName("), "MapStruct should fall back to setter mapping");
  }

  /** A javac compiler with BuilderProcessor ahead of MapStruct and stable mapper output. */
  private static Compiler mapStructCompiler() {
    return mapStructCompiler(new BuilderProcessor(), new MappingProcessor());
  }

  /** A javac compiler with the given processors in invocation order and stable mapper output. */
  private static Compiler mapStructCompiler(Processor... processors) {
    return createCompiler(processors)
        .withOptions(
            "-Amapstruct.suppressGeneratorTimestamp=true",
            "-Amapstruct.suppressGeneratorVersionInfoComment=true");
  }

  private static JavaFileObject personDto() {
    return ProcessorTestUtils.forSource(
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
  }

  private static JavaFileObject personDtoMapper() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.mapstruct.Mapper;

        @Mapper
        public interface PersonDtoMapper {

          PersonDto copy(PersonDto source);
        }
        """);
  }

  private static JavaFileObject mutableDto() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

        @SimpleBuilder
        public class MutableDto {
          private String name;

          public String getName() {
            return name;
          }

          public void setName(String name) {
            this.name = name;
          }
        }
        """);
  }

  private static JavaFileObject mutableDtoMapper() {
    return ProcessorTestUtils.forSource(
        """
        package test;

        import org.mapstruct.Mapper;

        @Mapper
        public interface MutableDtoMapper {

          MutableDto copy(MutableDto source);
        }
        """);
  }
}
