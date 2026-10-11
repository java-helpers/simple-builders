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

package org.javahelpers.simple.builders.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Verifies that MapStruct discovers and uses the generated {@code PersonDtoBuilder} through the
 * {@code MapStructBuilderProvider} SPI shipped in simple-builders-processor.
 */
class MapStructIntegrationTest {

  private static final Path GENERATED_MAPPER_IMPL =
      Path.of(
          "generated-example-builder",
          "org",
          "javahelpers",
          "simple",
          "builders",
          "example",
          "PersonDtoMapperImpl.java");

  @Test
  void shouldMapPersonDtoUsingGeneratedBuilder() {
    PersonDto source = PersonDtoBuilder.create()
        .name("Alice")
        .birthdate(LocalDate.of(1990, 1, 1))
        .build();

    PersonDto copy = Mappers.getMapper(PersonDtoMapper.class).copy(source);

    assertNotNull(copy);
    assertEquals("Alice", copy.getName());
    assertEquals(LocalDate.of(1990, 1, 1), copy.getBirthdate());
  }

  @Test
  void shouldGenerateMapperUsingPersonDtoBuilder() throws IOException {
    String mapperImpl = Files.readString(GENERATED_MAPPER_IMPL);
    assertTrue(
        mapperImpl.contains("PersonDtoBuilder.create()"),
        "MapStruct mapper implementation should use PersonDtoBuilder.create()");
    assertTrue(
        mapperImpl.contains(".build()"),
        "MapStruct mapper implementation should finish via build()");
  }
}
