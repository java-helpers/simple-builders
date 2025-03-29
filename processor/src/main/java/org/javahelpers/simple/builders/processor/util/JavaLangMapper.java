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

import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;

/** Helper functions to create simple builder types from java.lang types. */
public final class JavaLangMapper {

  public static Modifier mapRelevantModifier(Set<Modifier> modifier) {
    if (modifier.contains(PUBLIC)) {
      return PUBLIC;
    } else if (modifier.contains(PROTECTED)) {
      return PROTECTED;
    }
    return null;
  }

  public static TypeName map2TypeName(Class<?> clazz) {
    return new TypeName(clazz.getPackageName(), clazz.getSimpleName());
  }

  public static MethodParameterDto map2MethodParameter(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName().toString());
    TypeMirror typeOfParameter = param.asType();
    result.setParameterTypeName(extractType(typeOfParameter, elementUtils, typeUtils));
    return result;
  }

  private static TypeName extractType(
      TypeMirror typeOfParameter, Elements elementUtils, Types typeUtils) {
    TypeElement elementOfParameter = (TypeElement) typeUtils.asElement(typeOfParameter);
    String simpleClassName = elementOfParameter.getSimpleName().toString();
    String packageName =
        elementUtils.getPackageOf(elementOfParameter).getQualifiedName().toString();

    final List<TypeMirror> typesExtracted = new ArrayList<>();
    typeOfParameter.accept(
        new SimpleTypeVisitor14<Void, Void>() {
          @Override
          public Void visitDeclared(DeclaredType t, Void p) {
            List<? extends TypeMirror> typeArguments = t.getTypeArguments();
            if (!typeArguments.isEmpty()) {
              typesExtracted.addAll(typeArguments);
            }
            return null;
          }
        },
        null);

    if (typesExtracted.size() == 1) {
      return new TypeNameGeneric(
          packageName,
          simpleClassName,
          extractType(typesExtracted.get(0), elementUtils, typeUtils));
    } else {
      return new TypeName(packageName, simpleClassName);
    }
  }
}
