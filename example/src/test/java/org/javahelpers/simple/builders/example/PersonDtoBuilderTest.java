package org.javahelpers.simple.builders.example;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class PersonDtoBuilderTest {

  @Test
  public void testBuilder() {
    PersonDto personDto = PersonDtoBuilder
            .create()
            .birthdate(LocalDate.now())
            .mannschaft(mb -> mb.name("Testmannschaft"))
            .name("Testname").build();
    assertNotNull(personDto);
    assertNotNull(personDto.getBirthdate());
    assertEquals("Testname", personDto.getName());
    assertNotNull(personDto.getMannschaft());
    assertEquals("Testmannschaft", personDto.getMannschaft().getName());
  }
}
