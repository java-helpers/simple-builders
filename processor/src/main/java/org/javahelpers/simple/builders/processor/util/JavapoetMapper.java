package org.javahelpers.simple.builders.processor.util;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;

public final class JavapoetMapper {

  private JavapoetMapper() {}

  public static TypeName map2ParameterType(
      org.javahelpers.simple.builders.processor.dtos.TypeName parameterType) {
    ClassName classNameParameter =
        ClassName.get(parameterType.getPackageName(), parameterType.getClassName());
    if (parameterType.getInnerType().isPresent()) {
      return ParameterizedTypeName.get(
          classNameParameter, map2ParameterType(parameterType.getInnerType().get()));
    }
    return classNameParameter;
  }

  public static ClassName map2ClassName(
      org.javahelpers.simple.builders.processor.dtos.TypeName typeName) {
    return ClassName.get(typeName.getPackageName(), typeName.getClassName());
  }
}
