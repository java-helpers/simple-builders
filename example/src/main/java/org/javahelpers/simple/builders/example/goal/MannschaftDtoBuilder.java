package org.javahelpers.simple.builders.example.goal;


import javax.annotation.processing.Generated;
import org.javahelpers.simple.builders.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.example.MannschaftDto;

@Generated("org.javahelpers.simple.builders.processor")
@BuilderImplementation(forClass=MannschaftDto.class)
public class MannschaftDtoBuilder {
  private final org.javahelpers.simple.builders.example.MannschaftDto instance = new org.javahelpers.simple.builders.example.MannschaftDto();

  public static MannschaftDtoBuilder create(){
    return new MannschaftDtoBuilder();
  }

  public MannschaftDtoBuilder name(String name){
    instance.setName(name);
    return this;
  }

  public org.javahelpers.simple.builders.example.MannschaftDto build(){
    return instance;
  }
}