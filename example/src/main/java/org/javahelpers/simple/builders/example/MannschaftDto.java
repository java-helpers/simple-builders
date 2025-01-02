package org.javahelpers.simple.builders.example;

import org.javahelpers.simple.builders.annotations.BuilderForDtos;

@BuilderForDtos
public class MannschaftDto {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  
}