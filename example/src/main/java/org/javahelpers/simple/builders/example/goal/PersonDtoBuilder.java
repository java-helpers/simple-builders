package org.javahelpers.simple.builders.example.goal;

import java.util.function.Consumer;
import javax.annotation.processing.Generated;
import org.javahelpers.simple.builders.example.MannschaftDto;
import org.javahelpers.simple.builders.example.PersonDto;

@Generated("org.javahelpers.simple.builders.processor")
//todo @BuilderImplementation(forClass=PersonDto.class)
public class PersonDtoBuilder {
  private final PersonDto instance = new PersonDto();

  public static PersonDtoBuilder create(){
    return new PersonDtoBuilder();
  }

  public PersonDtoBuilder name(String name){
    instance.setName(name);
    return this;
  }

  public PersonDtoBuilder birthdate(java.time.LocalDate birthdate){
    instance.setBirthdate(birthdate);
    return this;
  }

  public PersonDtoBuilder mannschaft(MannschaftDto mannschaft){
    instance.setMannschaft(mannschaft);
    return this;
  }
  public PersonDtoBuilder mannschaft(Consumer<org.javahelpers.simple.builders.example.goal.MannschaftDtoBuilder> lamda){
    MannschaftDtoBuilder builder = MannschaftDtoBuilder.create();
    lamda.accept(builder);
    instance.setMannschaft(builder.build());
    return this;
  }

  public PersonDto build(){
    return instance;
  }
}