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

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.dtos.*;

/** Helper functions to create JavaPoet types from DTOs of simple builder. */
public final class JavapoetMapper {

  private JavapoetMapper() {}

  /**
   * Maps a list of simple-builder DTO type names to an array of JavaPoet {@code TypeName}s.
   * Primitives are boxed because JavaPoet requires reference types for type arguments.
   *
   * @param typeArguments the list of type arguments to map
   * @return array of JavaPoet TypeName instances
   */
  public static TypeName[] map2TypeArgumentsArray(
      List<org.javahelpers.simple.builders.processor.dtos.TypeName> typeArguments) {
    java.util.List<TypeName> args = new java.util.ArrayList<>(typeArguments.size());
    for (org.javahelpers.simple.builders.processor.dtos.TypeName tn : typeArguments) {
      TypeName mapped = map2ParameterType(tn);
      if (mapped.isPrimitive()) {
        mapped = mapped.box();
      }
      args.add(mapped);
    }
    return args.toArray(new TypeName[0]);
  }

  /**
   * Mapper for parameterType. Maps into javapoet classes.
   *
   * @param parameterType simple-builder dto to be mapped
   * @return javapoet TypeName
   */
  public static TypeName map2ParameterType(
      org.javahelpers.simple.builders.processor.dtos.TypeName parameterType) {
    // Handle generic type variables (e.g., T, K, V)
    if (parameterType
        instanceof org.javahelpers.simple.builders.processor.dtos.TypeNameVariable typeVariable) {
      return TypeVariableName.get(typeVariable.getClassName());
    }
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
    } else if (parameterType instanceof TypeNameGeneric param) {
      TypeName[] typeArgs = map2TypeArgumentsArray(param.getInnerTypeArguments());
      return ParameterizedTypeName.get(classNameParameter, typeArgs);
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
    if (StringUtils.isNoneEmpty(typeName.getPackageName())) {
      return ClassName.get(typeName.getPackageName(), typeName.getClassName());
    } else {
      return ClassName.bestGuess(typeName.getClassName());
    }
  }

  /**
   * Maps a base type and generic parameters to a JavaPoet ParameterizedTypeName.
   *
   * @param baseType the base type to parameterize
   * @param builderGenerics the list of generic parameters
   * @return a ParameterizedTypeName with the given type parameters
   */
  public static ParameterizedTypeName map2ParameterizedTypeName(
      org.javahelpers.simple.builders.processor.dtos.TypeName baseType,
      List<GenericParameterDto> builderGenerics) {
    ClassName baseTypeClassName = map2ClassName(baseType);
    return ParameterizedTypeName.get(
        baseTypeClassName, map2TypeVariables(builderGenerics).toArray(new TypeVariableName[0]));
  }

  /**
   * Maps a list of GenericParameterDto to JavaPoet TypeVariableName instances.
   *
   * @param builderGenerics the list of generic parameters to map
   * @return list of TypeVariableName representing the generic parameters
   */
  public static List<TypeVariableName> map2TypeVariables(
      List<GenericParameterDto> builderGenerics) {
    List<TypeVariableName> javapoetGenerics = new ArrayList<>();
    for (GenericParameterDto g : builderGenerics) {
      List<TypeName> bounds = new ArrayList<>();
      for (org.javahelpers.simple.builders.processor.dtos.TypeName b : g.getUpperBounds()) {
        bounds.add(map2ParameterType(b));
      }
      TypeVariableName tv =
          bounds.isEmpty()
              ? TypeVariableName.get(g.getName())
              : TypeVariableName.get(g.getName(), bounds.toArray(new TypeName[0]));
      javapoetGenerics.add(tv);
    }
    return javapoetGenerics;
  }

  /**
   * CodeBlock creating by definition in {@code MethodCodeDto}.
   *
   * @param codeDto code definition
   * @return {@code CodeBlock} of javapoet
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

  private static Object toCodeblockValue(MethodCodePlaceholder<?> placeHolderValue) {
    if (placeHolderValue instanceof MethodCodeStringPlaceholder stringPlaceholder) {
      return stringPlaceholder.getValue();
    } else if (placeHolderValue instanceof MethodCodeTypePlaceholder typePlaceholder) {
      return map2ParameterType(typePlaceholder.getValue());
    } else {
      throw new UnsupportedOperationException("");
    }
  }

  /**
   * Maps an AnnotationDto to a JavaPoet AnnotationSpec.
   *
   * @param annotationDto the annotation DTO to map
   * @return JavaPoet AnnotationSpec
   */
  public static AnnotationSpec map2AnnotationSpec(AnnotationDto annotationDto) {
    ClassName annotationType = map2ClassName(annotationDto.getAnnotationType());
    AnnotationSpec.Builder builder = AnnotationSpec.builder(annotationType);

    // Add annotation members (parameters)
    for (Map.Entry<String, String> member : annotationDto.getMembers().entrySet()) {
      // Use $L (literal) format since the values are already formatted as code strings
      builder.addMember(member.getKey(), "$L", member.getValue());
    }

    return builder.build();
  }

  /**
   * Maps a list of AnnotationDto to JavaPoet AnnotationSpec instances.
   *
   * @param annotations the list of annotations to map
   * @return list of AnnotationSpec
   */
  public static List<AnnotationSpec> map2AnnotationSpecs(List<AnnotationDto> annotations) {
    return annotations.stream().map(JavapoetMapper::map2AnnotationSpec).toList();
  }

  /**
   * Maps an AccessModifier enum value to a javax.lang.model.element.Modifier.
   *
   * @param accessModifier the access modifier to map
   * @return the corresponding Modifier
   */
  public static javax.lang.model.element.Modifier map2Modifier(AccessModifier accessModifier) {
    return switch (accessModifier) {
      case PUBLIC, DEFAULT -> javax.lang.model.element.Modifier.PUBLIC;
      case PROTECTED -> javax.lang.model.element.Modifier.PROTECTED;
      case PRIVATE -> javax.lang.model.element.Modifier.PRIVATE;
      case PACKAGE_PRIVATE -> null; // Package-private has no explicit modifier
    };
  }
}
