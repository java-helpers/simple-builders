package org.javahelpers.simple.builders.example.goal;

import org.javahelpers.simple.builders.example.PersonDto;

public class Builder {

  private Builder(){}

  public static PersonDtoBuilder forClass(Class<PersonDto> targetClass){
    return PersonDtoBuilder.create();
  }
}