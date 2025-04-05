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

import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.type.TypeKind.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameArray;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.dtos.TypeNamePrimitive;

/** Helper functions to create simple builder types from java.lang types. */
public final class JavaLangMapper {

  /**
   * Mapper for {@code java.util.Set<javax.lang.model.element.Modifier>} to extract the relevant
   * modifier. If there is a public modifier, this is returned. If there is a protected modifier,
   * this will be returned. Default is returned in all other cases.
   *
   * @param modifier Set of modifiers to be checked
   * @return DEFAULT, PUBLIC or PROTECTED
   */
  public static Modifier mapRelevantModifier(Set<Modifier> modifier) {
    if (modifier.contains(PUBLIC)) {
      return PUBLIC;
    } else if (modifier.contains(PROTECTED)) {
      return PROTECTED;
    }
    return DEFAULT;
  }

  /**
   * Mapping a Java-Class to a TypeName. This method does not expact sealed or annonymous classes.
   *
   * @param clazz Class to be mapped
   * @return Typename
   */
  public static TypeName map2TypeName(Class<?> clazz) {
    return new TypeName(clazz.getPackageName(), clazz.getSimpleName());
  }

  /**
   * Mapping a method parameter to {@code
   * org.javahelpers.simple.builders.processor.dtos.MethodParameterDto}.
   *
   * @param param Parameter variable to be mapped
   * @param elementUtils Instance of helper for elements
   * @param typeUtils Instance of helper for types
   * @return MethodParameterDto holding the information of the param.
   */
  public static MethodParameterDto map2MethodParameter(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName().toString());
    TypeMirror typeMirror = param.asType();
    TypeName typeName = extractType(typeMirror, elementUtils, typeUtils);
    if (typeName == null) {
      return null;
    }
    result.setParameterTypeName(typeName);
    return result;
  }

  private static TypeName extractType(
      TypeMirror typeOfParameter, Elements elementUtils, Types typeUtils) {
    return typeOfParameter.accept(
        new SimpleTypeVisitor14<TypeName, Void>() {

          @Override
          public TypeName visitPrimitive(PrimitiveType t, Void _p) {
            return switch (t.getKind()) {
              case BOOLEAN -> TypeNamePrimitive.BOOLEAN;
              case BYTE -> TypeNamePrimitive.BYTE;
              case SHORT -> TypeNamePrimitive.SHORT;
              case INT -> TypeNamePrimitive.INT;
              case LONG -> TypeNamePrimitive.LONG;
              case CHAR -> TypeNamePrimitive.CHAR;
              case FLOAT -> TypeNamePrimitive.FLOAT;
              case DOUBLE -> TypeNamePrimitive.DOUBLE;
              default -> throw new IllegalStateException("Unsupported Primitive type");
            };
          }

          @Override
          public TypeName visitDeclared(DeclaredType t, Void p) {
            TypeElement elementOfParameter = (TypeElement) typeUtils.asElement(typeOfParameter);
            String simpleClassName = elementOfParameter.getSimpleName().toString();
            String packageName =
                elementUtils.getPackageOf(elementOfParameter).getQualifiedName().toString();
            TypeName rawType = new TypeName(packageName, simpleClassName);
            TypeMirror enclosingType = t.getEnclosingType();
            TypeName enclosing =
                (enclosingType.getKind() != TypeKind.NONE)
                        && !t.asElement().getModifiers().contains(Modifier.STATIC)
                    ? enclosingType.accept(this, null)
                    : null;
            if (t.getTypeArguments().isEmpty() && !(enclosing instanceof TypeNameGeneric)) {
              return rawType;
            }

            List<TypeMirror> typesExtracted = new ArrayList<>(t.getTypeArguments());
            if (typesExtracted.isEmpty()) {
              return rawType;
            } else if (typesExtracted.size() == 1) {
              return new TypeNameGeneric(
                  rawType, extractType(typesExtracted.get(0), elementUtils, typeUtils));
            } else {
              // TODO: Multi-Type not supported yet
              return rawType;
            }
          }

          @Override
          public TypeNameArray visitArray(ArrayType t, Void _p) {
            return new TypeNameArray(
                extractType(t.getComponentType(), elementUtils, typeUtils), false);
          }

          @Override
          protected TypeName defaultAction(TypeMirror e, Void _p) {
            throw new IllegalArgumentException("Unexpected type mirror: " + e);
          }
        },
        null);
  }
}
