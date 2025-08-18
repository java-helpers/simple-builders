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

package org.javahelpers.simple.builders.processor.util;

import static org.javahelpers.simple.builders.processor.dtos.TypeNamePrimitive.PrimitiveTypeEnum.BOOLEAN;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.util.Map;
import java.util.stream.Collectors;
import org.javahelpers.simple.builders.processor.dtos.*;

/** Helper functions to create JavaPoet types from DTOs of simple builder. */
public final class JavapoetMapper {

  private JavapoetMapper() {}

  /**
   * Mapper for parameterType. Maps into javapoet classes.
   *
   * @param parameterType simple-builder dto to be mapped
   * @return javapoet TypeName
   */
  public static TypeName map2ParameterType(
      org.javahelpers.simple.builders.processor.dtos.TypeName parameterType) {
    ClassName classNameParameter =
        ClassName.get(parameterType.getPackageName(), parameterType.getClassName());
    if (parameterType instanceof TypeNameArray parameterTypeArray) {
      return ArrayTypeName.of(map2ParameterType(parameterTypeArray.getTypeOfArray()));
    } else if (parameterType instanceof TypeNamePrimitive parameterTypePrim) {
      return switch (parameterTypePrim.getType()) {
        case BOOLEAN -> TypeName.BOOLEAN;
        case BYTE -> TypeName.BYTE;
        case CHAR -> TypeName.CHAR;
        case DOUBLE -> TypeName.DOUBLE;
        case FLOAT -> TypeName.FLOAT;
        case INT -> TypeName.INT;
        case LONG -> TypeName.LONG;
        case SHORT -> TypeName.SHORT;
        default -> null;
      };
    } else if (parameterType instanceof TypeNameParameterized param) {
      java.util.List<com.palantir.javapoet.TypeName> args = new java.util.ArrayList<>();
      for (org.javahelpers.simple.builders.processor.dtos.TypeName tn : param.getTypeArguments()) {
        TypeName mapped = map2ParameterType(tn);
        if (mapped.isPrimitive()) mapped = mapped.box();
        args.add(mapped);
      }
      return ParameterizedTypeName.get(classNameParameter, args.toArray(new TypeName[0]));
    } else if (parameterType.getInnerType().isPresent()) {
      TypeName inner = map2ParameterType(parameterType.getInnerType().get());
      // Box primitives when used as type arguments, e.g., Consumer<Integer> instead of
      // Consumer<int>
      if (inner.isPrimitive()) {
        inner = inner.box();
      }
      return ParameterizedTypeName.get(classNameParameter, inner);
    }
    return classNameParameter;
  }

  /**
   * Mapper for typename. Maps into javapoet classes.
   *
   * @param typeName simple-builder dto to be mapped
   * @return javapoet TypeName
   */
  public static ClassName map2ClassName(
      org.javahelpers.simple.builders.processor.dtos.TypeName typeName) {
    return ClassName.get(typeName.getPackageName(), typeName.getClassName());
  }

  /**
   * CodeBlock creating by definition in {@code MethodCodeDto}.
   *
   * @param codeDto code definition
   * @return {@CodeBlock} of javapoet
   */
  public static CodeBlock map2CodeBlock(
      org.javahelpers.simple.builders.processor.dtos.MethodCodeDto codeDto) {
    Map<String, Object> arguments =
        codeDto.getCodeArguments().stream()
            .collect(
                Collectors.toMap(
                    MethodCodePlaceholder::getLabel, JavapoetMapper::toCodeblockValue));
    return CodeBlock.builder().addNamed(codeDto.getCodeFormat(), arguments).build();
  }

  private static Object toCodeblockValue(MethodCodePlaceholder placeHolderValue) {
    if (placeHolderValue instanceof MethodCodeStringPlaceholder stringPlaceholder) {
      return stringPlaceholder.getValue();
    } else if (placeHolderValue instanceof MethodCodeTypePlaceholder typePlaceholder) {
      return map2ParameterType(typePlaceholder.getValue());
    } else {
      throw new UnsupportedOperationException("");
    }
  }
}
