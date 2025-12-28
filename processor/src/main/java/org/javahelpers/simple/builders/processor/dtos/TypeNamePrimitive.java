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

/**
 * Represents a primitive type in the Java language. This class provides type-safe constants for all
 * Java primitive types and serves as a way to unambiguously identify primitive types during
 * annotation processing.
 */
public class TypeNamePrimitive extends TypeName {
  /** Represents the {@code void} primitive type. */
  public static final TypeNamePrimitive VOID = type(PrimitiveTypeEnum.VOID);

  /** Represents the {@code boolean} primitive type. */
  public static final TypeNamePrimitive BOOLEAN = type(PrimitiveTypeEnum.BOOLEAN);

  /** Represents the {@code byte} primitive type. */
  public static final TypeNamePrimitive BYTE = type(PrimitiveTypeEnum.BYTE);

  /** Represents the {@code short} primitive type. */
  public static final TypeNamePrimitive SHORT = type(PrimitiveTypeEnum.SHORT);

  /** Represents the {@code int} primitive type. */
  public static final TypeNamePrimitive INT = type(PrimitiveTypeEnum.INT);

  /** Represents the {@code long} primitive type. */
  public static final TypeNamePrimitive LONG = type(PrimitiveTypeEnum.LONG);

  /** Represents the {@code char} primitive type. */
  public static final TypeNamePrimitive CHAR = type(PrimitiveTypeEnum.CHAR);

  /** Represents the {@code float} primitive type. */
  public static final TypeNamePrimitive FLOAT = type(PrimitiveTypeEnum.FLOAT);

  /** Represents the {@code double} primitive type. */
  public static final TypeNamePrimitive DOUBLE = type(PrimitiveTypeEnum.DOUBLE);

  /** The primitive type represented by this instance. */
  private final PrimitiveTypeEnum type;

  /**
   * Constructor for TypeName by primitive type enum.
   *
   * @param primitiveType primitive enum type
   */
  public TypeNamePrimitive(PrimitiveTypeEnum primitiveType) {
    super("", primitiveType.name().toLowerCase());
    this.type = primitiveType;
  }

  /**
   * Getter for type of primitive.
   *
   * @return {@code PrimitiveTypeEnum} of primitive
   */
  public PrimitiveTypeEnum getType() {
    return type;
  }

  /**
   * Factory method to create a new TypeNamePrimitive instance for the specified primitive type.
   *
   * @param typeEnum the primitive type enum value
   * @return a new TypeNamePrimitive instance representing the specified primitive type
   */
  protected static TypeNamePrimitive type(PrimitiveTypeEnum typeEnum) {
    return new TypeNamePrimitive(typeEnum);
  }

  /**
   * Enumerates all primitive types supported by the Java language. Each enum constant represents a
   * specific primitive type that can be used during annotation processing.
   */
  public enum PrimitiveTypeEnum {
    /** The void primitive type. */
    VOID,
    /** The boolean primitive type. */
    BOOLEAN,
    /** The byte primitive type. */
    BYTE,
    /** The short primitive type. */
    SHORT,
    /** The int primitive type. */
    INT,
    /** The long primitive type. */
    LONG,
    /** The char primitive type. */
    CHAR,
    /** The float primitive type. */
    FLOAT,
    /** The double primitive type. */
    DOUBLE;
  }
}
