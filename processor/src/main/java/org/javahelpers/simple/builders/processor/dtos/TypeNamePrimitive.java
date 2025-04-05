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

package org.javahelpers.simple.builders.processor.dtos;

/** TypeNamePrimitive is the unambiguously definition of a primitive type. */
public class TypeNamePrimitive extends TypeName {
  public static final TypeNamePrimitive VOID = type(PrimitiveTypeEnum.VOID);
  public static final TypeNamePrimitive BOOLEAN = type(PrimitiveTypeEnum.BOOLEAN);
  public static final TypeNamePrimitive BYTE = type(PrimitiveTypeEnum.BYTE);
  public static final TypeNamePrimitive SHORT = type(PrimitiveTypeEnum.SHORT);
  public static final TypeNamePrimitive INT = type(PrimitiveTypeEnum.INT);
  public static final TypeNamePrimitive LONG = type(PrimitiveTypeEnum.LONG);
  public static final TypeNamePrimitive CHAR = type(PrimitiveTypeEnum.CHAR);
  public static final TypeNamePrimitive FLOAT = type(PrimitiveTypeEnum.FLOAT);
  public static final TypeNamePrimitive DOUBLE = type(PrimitiveTypeEnum.DOUBLE);

  private final PrimitiveTypeEnum type;

  protected TypeNamePrimitive(PrimitiveTypeEnum primitiveType) {
    super("", primitiveType.name().toLowerCase());
    this.type = primitiveType;
  }

  public PrimitiveTypeEnum getType() {
    return type;
  }

  protected static TypeNamePrimitive type(PrimitiveTypeEnum typeEnum) {
    return new TypeNamePrimitive(typeEnum);
  }

  public enum PrimitiveTypeEnum {
    VOID,
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    CHAR,
    FLOAT,
    DOUBLE;
  }
}
