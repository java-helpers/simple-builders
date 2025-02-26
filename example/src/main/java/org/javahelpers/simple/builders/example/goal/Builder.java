package org.javahelpers.simple.builders.example.goal;

public class Builder {

  private Builder(){}

  public static PersonDtoBuilder forPersonDto(){
    return PersonDtoBuilder.create();
  }
  
  public static MannschaftDtoBuilder forMannschaftDto(){
    return MannschaftDtoBuilder.create();
  }
}