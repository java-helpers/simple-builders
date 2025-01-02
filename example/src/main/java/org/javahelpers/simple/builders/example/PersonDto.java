package org.javahelpers.simple.builders.example;

import java.time.LocalDate;
import org.javahelpers.simple.builders.annotations.BuilderForDtos;

@BuilderForDtos
public class PersonDto{
  private String name;
  private java.time.LocalDate birthdate;
  private MannschaftDto mannschaft;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getBirthdate() {
    return birthdate;
  }

  public void setBirthdate(LocalDate birthdate) {
    this.birthdate = birthdate;
  }

  public MannschaftDto getMannschaft() {
    return mannschaft;
  }

  public void setMannschaft(MannschaftDto mannschaft) {
    this.mannschaft = mannschaft;
  }
}