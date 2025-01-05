package org.javahelpers.simple.builders.example;

import java.util.List;
import org.javahelpers.simple.builders.annotations.BuilderForDtos;

@BuilderForDtos
public class MannschaftDto {
  private String name;
  private List<String> sponsoren;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getSponsoren() {
    return sponsoren;
  }

  public void setSponsoren(List<String> sponsoren) {
    this.sponsoren = sponsoren;
  }
  
  
}