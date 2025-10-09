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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor14;
import org.javahelpers.simple.builders.processor.dtos.*;

/** Helper functions to create simple builder types from java.lang types. */
public final class JavaLangMapper {
  /** Private constructor to prevent instantiation of utility class. */
  private JavaLangMapper() {
    // Utility class
  }

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

  /** Maps the declared type parameters of the given type element into GenericParameterDto list. */
  public static List<GenericParameterDto> map2GenericParameterDtos(
      TypeElement type, ProcessingContext context) {
    return type.getTypeParameters().stream()
        .map(tp -> map2GenericParameterDto(tp, context))
        .toList();
  }

  /** Maps a single {@code TypeParameterElement} to {@code GenericParameterDto}. */
  public static GenericParameterDto map2GenericParameterDto(
      TypeParameterElement tp, ProcessingContext context) {
    GenericParameterDto g = new GenericParameterDto();
    g.setName(tp.getSimpleName().toString());
    tp.getBounds().stream()
        .filter(b -> !"java.lang.Object".equals(b.toString()))
        .map(b -> extractType(b, context))
        .forEach(g::addUpperBound);
    return g;
  }

  /**
   * Mapping a method parameter to {@code
   * org.javahelpers.simple.builders.processor.dtos.MethodParameterDto}.
   *
   * @param param Parameter variable to be mapped
   * @param context processing context
   * @return MethodParameterDto holding the information of the param.
   */
  public static MethodParameterDto map2MethodParameter(
      VariableElement param, ProcessingContext context) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName().toString());
    TypeMirror typeMirror = param.asType();
    TypeName typeName = extractType(typeMirror, context);
    if (typeName == null) {
      return null;
    }
    result.setParameterTypeName(typeName);
    return result;
  }

  /**
   * Maps a list of {@code TypeMirror} to a list of simple-builder {@code TypeName}s using {@link
   * #extractType(TypeMirror, ProcessingContext)}.
   */
  private static List<TypeName> extractTypeForList(
      List<TypeMirror> typeMirrors, ProcessingContext context) {
    List<TypeName> result = new ArrayList<>(typeMirrors.size());
    for (TypeMirror tm : typeMirrors) {
      result.add(extractType(tm, context));
    }
    return result;
  }

  private static TypeName extractType(TypeMirror typeOfParameter, ProcessingContext context) {
    return typeOfParameter.accept(
        new SimpleTypeVisitor14<TypeName, Void>() {

          @Override
          public TypeName visitPrimitive(PrimitiveType t, Void p) {
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
            TypeElement elementOfParameter = (TypeElement) context.asElement(typeOfParameter);
            String simpleClassName = elementOfParameter.getSimpleName().toString();
            String packageName = context.getPackageName(elementOfParameter);
            TypeName rawType = new TypeName(packageName, simpleClassName);
            TypeMirror enclosingType = t.getEnclosingType();
            TypeName enclosing =
                (enclosingType.getKind() != NONE)
                        && !t.asElement().getModifiers().contains(Modifier.STATIC)
                    ? enclosingType.accept(this, null)
                    : null;
            if (t.getTypeArguments().isEmpty() && !(enclosing instanceof TypeNameGeneric)) {
              return rawType;
            }

            List<TypeMirror> typesExtracted = new ArrayList<>(t.getTypeArguments());
            if (typesExtracted.isEmpty()) {
              return rawType;
            } else {
              List<TypeName> argTypes = extractTypeForList(typesExtracted, context);
              // Represent all generics uniformly
              return new TypeNameGeneric(rawType, argTypes);
            }
          }

          @Override
          public TypeNameArray visitArray(ArrayType t, Void p) {
            return new TypeNameArray(extractType(t.getComponentType(), context), false);
          }

          @Override
          public TypeName visitTypeVariable(TypeVariable t, Void p) {
            String name = t.asElement().getSimpleName().toString();
            return new TypeNameVariable(name);
          }

          @Override
          protected TypeName defaultAction(TypeMirror e, Void p) {
            throw new IllegalArgumentException("Unexpected type mirror: " + e);
          }
        },
        null);
  }
}
