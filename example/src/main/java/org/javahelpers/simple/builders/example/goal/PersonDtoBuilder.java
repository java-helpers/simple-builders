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

package org.javahelpers.simple.builders.example.goal;

import java.util.function.Consumer;
import javax.annotation.processing.Generated;
import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.example.MannschaftDto;
import org.javahelpers.simple.builders.example.PersonDto;

@Generated("org.javahelpers.simple.builders.processor")
@BuilderImplementation(forClass=PersonDto.class)
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