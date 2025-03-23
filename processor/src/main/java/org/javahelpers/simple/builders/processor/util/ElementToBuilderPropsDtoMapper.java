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

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;
import static org.javahelpers.simple.builders.processor.util.AnnotationValidator.validateAnnotatedElement;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;

/** Mapper for creation of a specific BuilderDefinitionDto for a DTO class. */
public class ElementToBuilderPropsDtoMapper {
  private static final String BUILDER_SUFFIX = "Builder";

  /**
   * Retrieving definition of builder from annotated element.
   *
   * @param annotatedElement annotated elment which is target of builder creation
   * @param elementUtils {@code javax.lang.model.util.Elements} utils
   * @param typeUtils {@code javax.lang.model.util.Types} utils
   * @return definition of builder
   * @throws BuilderException if validation or generation failed
   */
  public static BuilderDefinitionDto extractFromElement(
      Element annotatedElement, Elements elementUtils, Types typeUtils) throws BuilderException {
    validateAnnotatedElement(annotatedElement);
    TypeElement annotatedType = (TypeElement) annotatedElement;

    BuilderDefinitionDto result = new BuilderDefinitionDto();
    String packageName = elementUtils.getPackageOf(annotatedType).getQualifiedName().toString();
    String simpleClassName =
        StringUtils.removeStart(annotatedElement.getSimpleName().toString(), packageName + ".");
    result.setBuilderTypeName(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
    result.setBuildingTargetTypeName(new TypeName(packageName, simpleClassName));

    List<? extends Element> allMembers = elementUtils.getAllMembers(annotatedType);
    List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);

    for (ExecutableElement mth : methods) {
      // nur public
      if (isMethodRelevantForBuilder(mth)) {
        if (isSimpleSetter(mth)) {
          result.addField(mapFieldFromElement(mth, elementUtils, typeUtils));
        } else {
          result.addMethod(mapMethodFromElement(mth, elementUtils, typeUtils));
        }
      }
    }

    return result;
  }

  private static boolean isMethodRelevantForBuilder(ExecutableElement mth) {
    return isNoMethodOfObjectClass(mth)
        && hasNoThrowablesDeclared(mth)
        && hasNoReturnValue(mth)
        && isNotPrivate(mth)
        && isNotStatic(mth);
  }

  private static boolean isNoMethodOfObjectClass(ExecutableElement mth) {
    String simpleNameOfParent = mth.getEnclosingElement().getSimpleName().toString();
    return !(StringUtils.equals("java.lang.Object", simpleNameOfParent)
        || StringUtils.equals("Object", simpleNameOfParent));
  }

  private static boolean hasNoThrowablesDeclared(ExecutableElement mth) {
    return mth.getThrownTypes().isEmpty();
  }

  private static boolean hasNoReturnValue(ExecutableElement mth) {
    return mth.getReturnType().getKind() == VOID;
  }

  private static boolean isNotPrivate(ExecutableElement mth) {
    return !mth.getModifiers().contains(PRIVATE);
  }

  private static boolean isNotStatic(ExecutableElement mth) {
    return !mth.getModifiers().contains(STATIC);
  }

  private static MethodDto mapMethodFromElement(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    String methodName = mth.getSimpleName().toString();
    List<? extends VariableElement> parameters = mth.getParameters();

    MethodDto result = new MethodDto();
    result.setMethodName(methodName);
    result.setModifier(mapRelevantModifier(mth.getModifiers()));
    parameters.stream()
        .map(v -> mapMethodParameter(v, elementUtils, typeUtils))
        .forEach(result::addParameter);
    return result;
  }

  private static FieldDto mapFieldFromElement(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    String methodName = mth.getSimpleName().toString();
    String fieldName = StringUtils.uncapitalize(StringUtils.removeStart(methodName, "set"));

    FieldDto result = new FieldDto();
    result.setFieldName(fieldName);
    List<? extends VariableElement> parameters = mth.getParameters();
    if (parameters.size() != 1) {
      // Sollte eigentlich nie vorkommen, da das vorher raus gefiltert wurde
      return null;
    }
    VariableElement fieldParameter = parameters.get(0);
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldTypeMirror);

    // extracting type of field
    MethodParameterDto fieldParameterDto =
        mapMethodParameter(fieldParameter, elementUtils, typeUtils);
    TypeName fieldType = fieldParameterDto.getParameterType();

    // simple setter
    result.addFieldSetter(fieldName, fieldType, fieldName);

    // setting value by builder
    Optional<TypeName> builderTypeOpt = findBuilderType(fieldParameter, elementUtils, typeUtils);
    if (builderTypeOpt.isPresent()) {
      TypeName builderType = builderTypeOpt.get();
      // TODO: Hier extra Type
      result.addFieldConsumerByBuilder(
          fieldName,
          new TypeNameGeneric("java.util.function", "Consumer", builderType),
          fieldName + BUILDER_SUFFIX + "Consumer");
    } else if (!isJavaClass(fieldType) && hasEmptyConstructor(fieldTypeElement, elementUtils)) {
      // TODO: Consumer funktioniett nur, wenn Klasse kein Interface/Enum/Abstrakte Classe/Record
      result.addFieldConsumer(
          fieldName,
          new TypeNameGeneric("java.util.function", "Consumer", fieldType),
          fieldName + "Consumer");
    } else if (isList(fieldType)) {
      result.addFieldConsumerByBuilder(
          fieldName,
          new TypeNameGeneric(
              "java.util.function",
              "Consumer",
              new TypeNameGeneric(
                  "org.javahelpers.simple.builders.core.builders",
                  "ArrayListBuilder",
                  fieldType.getInnerType().get())),
          fieldName + "BuilderConsumer");
      // TODO: auch eine FieldType... Variante im Builder anbieten
    } else if (isMap(fieldType)) {
      // TODO MAP (having 2 inner classes, TypeNameGeneric is not able to adress that yet)
    } else if (isSet(fieldType)) {
      result.addFieldConsumerByBuilder(
          fieldName,
          new TypeNameGeneric(
              "java.util.function",
              "Consumer",
              new TypeNameGeneric(
                  "org.javahelpers.simple.builders.core.builders",
                  "HashSetBuilder",
                  fieldType.getInnerType().get())),
          fieldName + "BuilderConsumer");
      // TODO: auch eine FieldType... Variante im Builder anbieten
    }

    // setting value by supplier
    result.addFieldSupplier(
        fieldName,
        new TypeNameGeneric("java.util.function", "Supplier", fieldType),
        fieldName + "Supplier");

    return result;
  }

  private static boolean isJavaClass(TypeName typeName) {
    return StringUtils.equalsAny(typeName.getPackageName(), "java.lang", "java.time", "java.util");
  }

  private static boolean isList(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "List");
  }

  private static boolean isMap(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "Map");
  }

  private static boolean isSet(TypeName typeName) {
    if (typeName.getInnerType().isEmpty()) {
      return false;
    }
    return StringUtils.equalsIgnoreCase(typeName.getPackageName(), "java.util")
        && StringUtils.equalsIgnoreCase(typeName.getClassName(), "Set");
  }

  private static boolean isSimpleSetter(ExecutableElement mth) {
    return StringUtils.startsWith(mth.getSimpleName(), "set") && mth.getParameters().size() == 1;
  }

  private static Modifier mapRelevantModifier(Set<Modifier> modifier) {
    if (modifier.contains(PUBLIC)) {
      return PUBLIC;
    } else if (modifier.contains(PROTECTED)) {
      return PROTECTED;
    }
    return null;
  }

  private static MethodParameterDto mapMethodParameter(
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

  private static Optional<TypeName> findBuilderType(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    TypeMirror typeOfParameter = param.asType();
    Element elementOfParameter = typeUtils.asElement(typeOfParameter);
    String simpleClassName = elementOfParameter.getSimpleName().toString();
    String packageName =
        elementUtils.getPackageOf(elementOfParameter).getQualifiedName().toString();
    Optional<AnnotationMirror> foundBuilderAnnotation =
        findAnnotation(elementOfParameter, SimpleBuilder.class);
    boolean isClassWithoutGenerics = StringUtils.containsNone(simpleClassName, "<");
    if (foundBuilderAnnotation.isPresent() && isClassWithoutGenerics) {
      return Optional.of(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
    }
    return Optional.empty();
  }

  private static boolean hasEmptyConstructor(TypeElement typeElement, Elements elementUtils) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(elementUtils.getAllMembers(typeElement));
    return constructors.stream().anyMatch(c -> c.getParameters().isEmpty());
  }

  private static Optional<AnnotationMirror> findAnnotation(
      Element abstractMethodElement, Class<? extends Annotation> annotationClass) {

    for (AnnotationMirror annotationMirror : abstractMethodElement.getAnnotationMirrors()) {
      if (annotationMirror
          .getAnnotationType()
          .toString()
          .equals(annotationClass.getCanonicalName())) {
        return Optional.of(annotationMirror);
      }
    }

    return Optional.empty();
  }
}
