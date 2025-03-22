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

package org.javahelpers.simple.builders.internal.dtos;

import static org.javahelpers.simple.builders.internal.dtos.MethodTypes.PROXY;

import java.util.LinkedList;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public class MethodDto {
  private Optional<Modifier> modifier = Optional.empty();
  private MethodTypes methodType = PROXY;
  private String methodName;
  private String fullQualifiedType;
  private final LinkedList<MethodParameterDto> parameters = new LinkedList<>();

  public MethodTypes getMethodType() {
    return methodType;
  }

  public void setMethodType(MethodTypes methodType) {
    this.methodType = methodType;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getFieldSetterMethodName() {
    return "set" + StringUtils.capitalize(this.getMethodName());
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getFullQualifiedType() {
    return fullQualifiedType;
  }

  public void setFullQualifiedType(String fullQualifiedType) {
    this.fullQualifiedType = fullQualifiedType;
  }

  public void addParameter(MethodParameterDto paramDto) {
    this.parameters.add(paramDto);
  }

  public LinkedList<MethodParameterDto> getParameters() {
    return parameters;
  }

  public Optional<Modifier> getModifier() {
    return modifier;
  }

  public void setModifier(Modifier modifier) {
    this.modifier = Optional.ofNullable(modifier);
  }
}
