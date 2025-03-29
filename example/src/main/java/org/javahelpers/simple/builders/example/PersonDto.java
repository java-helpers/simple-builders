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

package org.javahelpers.simple.builders.example;

import java.time.LocalDate;
import java.util.List;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

@SimpleBuilder
public class PersonDto {
  private String name;
  private int groesse;
  private List<String> nickNames;
  private java.time.LocalDate birthdate;
  private MannschaftDto mannschaft;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getGroesse() {
    return groesse;
  }

  public void setGroesse(int groesse) {
    this.groesse = groesse;
  }

  public List<String> getNickNames() {
    return nickNames;
  }

  public void setNickNames(List<String> nickNames) {
    this.nickNames = nickNames;
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
