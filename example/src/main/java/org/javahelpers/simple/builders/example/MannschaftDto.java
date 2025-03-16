package org.javahelpers.simple.builders.example;

import java.util.List;
import java.util.Set;
import org.javahelpers.simple.builders.annotations.BuilderForDtos;

@BuilderForDtos
public class MannschaftDto {
  private String name;
  private Set<SponsorDto> sponsoren;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<SponsorDto> getSponsoren() {
    return sponsoren;
  }

  public void setSponsoren(Set<SponsorDto> sponsoren) {
    this.sponsoren = sponsoren;
  }
  
  
}