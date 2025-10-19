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

package org.javahelpers.simple.builders.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PersonDtoBuilderTest {

  @Test
  void testBuilder() {
    PersonDto personDto =
        PersonDtoBuilder.create()
            .birthdate(LocalDate.now())
            .nickNames(b -> b.add("Test1").add("Test2"))
            .nickNames(b -> b.add("Test3").add("Test4"))
            .mannschaft(
                mb ->
                    mb.name("Testmannschaft")
                        .sponsoren(
                            sb -> sb.add(SponsorDtoBuilder.create().name("TestSponsor").build())))
            .name(this::nameSupplier)
            .build();
    assertNotNull(personDto);
    assertNotNull(personDto.getBirthdate());
    assertEquals("Testname", personDto.getName());
    assertNotNull(personDto.getMannschaft());
    assertEquals("Testmannschaft", personDto.getMannschaft().getName());
    assertEquals(4, personDto.getNickNames().size());
  }

  private String nameSupplier() {
    return "Testname";
  }
}
