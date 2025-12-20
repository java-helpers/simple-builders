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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PersonDtoBuilderTest {

  @Test
  void testBasicBuilder() {
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
            .mannschaft(mb -> mb.sponsoren(sb -> sb.add(SponsorDtoBuilder.create().name("TestSponsor2").build())))
            .build();
    assertNotNull(personDto);
    assertNotNull(personDto.getBirthdate());
    assertEquals("Testname", personDto.getName());
    assertNotNull(personDto.getMannschaft());
    assertEquals("Testmannschaft", personDto.getMannschaft().getName());
    assertEquals(4, personDto.getNickNames().size());
    assertEquals(2, personDto.getMannschaft().getSponsoren().size());
  }

  private String nameSupplier() {
    return "Testname";
  }

  @Test
  void testSupplierMethods() {
    PersonDto person =
        PersonDtoBuilder.create()
            .name(() -> "John Doe")
            .birthdate(() -> LocalDate.of(1990, 5, 15))
            .build();

    assertNotNull(person);
    assertEquals("John Doe", person.getName());
    assertEquals(LocalDate.of(1990, 5, 15), person.getBirthdate());
  }

  @Test
  void testConditionalLogic() {
    boolean isPremiumUser = true;
    boolean hasNickname = false;

    PersonDto person =
        PersonDtoBuilder.create()
            .name("Jane Smith")
            .conditional(
                () -> isPremiumUser,
                builder -> builder.birthdate(LocalDate.of(1990, 1, 1)),
                builder -> builder.birthdate(LocalDate.of(2000, 1, 1)))
            .conditional(
                () -> hasNickname,
                builder -> builder.nickNames(List.of("JJ")))
            .build();

    assertNotNull(person);
    assertEquals("Jane Smith", person.getName());
    assertEquals(LocalDate.of(1990, 1, 1), person.getBirthdate());
  }

  @Test
  void testVarArgsHelpers() {
    PersonDto person =
        PersonDtoBuilder.create()
            .name("Alice")
            .nickNames("Ally", "Al", "Liz")
            .build();

    assertNotNull(person);
    assertEquals("Alice", person.getName());
    assertNotNull(person.getNickNames());
    assertEquals(3, person.getNickNames().size());
    assertTrue(person.getNickNames().contains("Ally"));
  }

  @Test
  void testCollectionBuilders() {
    PersonDto person =
        PersonDtoBuilder.create()
            .name("Bob")
            .nickNames(list -> list.add("Bobby").add("Rob").add("Robert"))
            .mannschaft(
                teamBuilder ->
                    teamBuilder
                        .name("Dream Team")
                        .sponsoren(
                            sponsors ->
                                sponsors
                                    .add(SponsorDtoBuilder.create().name("TechCorp").build())
                                    .add(SponsorDtoBuilder.create().name("SportsCo").build())))
            .build();

    assertNotNull(person);
    assertEquals("Bob", person.getName());
    assertEquals(3, person.getNickNames().size());
    assertNotNull(person.getMannschaft());
    assertEquals("Dream Team", person.getMannschaft().getName());
    assertEquals(2, person.getMannschaft().getSponsoren().size());
  }

  @Test
  void testNestedBuilderConsumers() {
    PersonDto person =
        PersonDtoBuilder.create()
            .name("Charlie")
            .mannschaft(
                team ->
                    team.name("Champions")
                        .sponsoren(
                            sponsors ->
                                sponsors.add(
                                    sponsor -> sponsor.name("MegaCorp"))))
            .build();

    assertNotNull(person);
    assertEquals("Charlie", person.getName());
    assertNotNull(person.getMannschaft());
    assertEquals("Champions", person.getMannschaft().getName());
    assertEquals(1, person.getMannschaft().getSponsoren().size());
    assertEquals("MegaCorp", person.getMannschaft().getSponsoren().iterator().next().getName());
  }

  @Test
  void testCombinedFeatures() {
    boolean addExtraInfo = true;

    PersonDto person =
        PersonDtoBuilder.create()
            .name(() -> "David")
            .birthdate(LocalDate.of(1985, 3, 20))
            .nickNames("Dave", "Davey")
            .conditional(
                () -> addExtraInfo,
                builder ->
                    builder
                        .mannschaft(
                            team ->
                                team.name("Elite Squad")
                                    .sponsoren(
                                        sponsors ->
                                            sponsors.add(
                                                sponsor -> sponsor.name("GlobalTech")))))
            .build();

    assertNotNull(person);
    assertEquals("David", person.getName());
    assertEquals(LocalDate.of(1985, 3, 20), person.getBirthdate());
    assertEquals(2, person.getNickNames().size());
    assertNotNull(person.getMannschaft());
    assertEquals("Elite Squad", person.getMannschaft().getName());
    assertEquals(1, person.getMannschaft().getSponsoren().size());
  }
}
