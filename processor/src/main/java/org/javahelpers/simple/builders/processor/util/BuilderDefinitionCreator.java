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

import static javax.lang.model.type.TypeKind.ARRAY;
import static org.javahelpers.simple.builders.processor.util.AnnotationValidator.validateAnnotatedElement;
import static org.javahelpers.simple.builders.processor.util.JavaLangAnalyser.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2MethodParameter;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.mapRelevantModifier;
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;

/** Class for creating a specific BuilderDefinitionDto for an annotated DTO class. */
public class BuilderDefinitionCreator {
  private static final String BUILDER_SUFFIX = "Builder";

  /**
   * Extracting definition for a builder from annotated element.
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
        if (isSetterForField(mth)) {
          createFieldDto(mth, elementUtils, typeUtils).ifPresent(result::addField);
        } else {
          createMethodDto(mth, elementUtils, typeUtils).ifPresent(result::addMethod);
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

  private static Optional<MethodDto> createMethodDto(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    String methodName = mth.getSimpleName().toString();
    List<? extends VariableElement> parameters = mth.getParameters();

    MethodDto result = new MethodDto();
    result.setMethodName(methodName);
    result.setModifier(mapRelevantModifier(mth.getModifiers()));
    parameters.stream()
        .map(v -> map2MethodParameter(v, elementUtils, typeUtils))
        .forEach(result::addParameter);
    if (result.getParameters().stream().anyMatch(Objects::isNull)) {
      // TODO: Logging
      return Optional.empty();
    }

    List<String> paramList =
        result.getParameters().stream().map(MethodParameterDto::getParameterName).toList();
    String paramListJoin = String.join(",", paramList);
    result.setCode(
        """
        instance.$dtoMethod:N($dtoMethodParams:N);
        return this;
        """);
    result.addArgument("dtoMethod", methodName);
    result.addArgument("dtoMethodParams", paramListJoin);

    return Optional.of(result);
  }

  private static Optional<FieldDto> createFieldDto(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    String methodName = mth.getSimpleName().toString();
    String fieldName = StringUtils.uncapitalize(StringUtils.removeStart(methodName, "set"));

    FieldDto result = new FieldDto();
    result.setFieldName(fieldName);
    List<? extends VariableElement> parameters = mth.getParameters();
    if (parameters.size() != 1) {
      // TODO: Logging
      // Sollte eigentlich nie vorkommen, da das vorher raus gefiltert wurde
      return Optional.empty();
    }
    VariableElement fieldParameter = parameters.get(0);
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldTypeMirror);

    // extracting type of field
    MethodParameterDto fieldParameterDto =
        map2MethodParameter(fieldParameter, elementUtils, typeUtils);
    if (fieldParameterDto == null) {
      // TODO: Logging
      return Optional.empty();
    }
    TypeName fieldType = fieldParameterDto.getParameterType();

    // simple setter
    result.addMethod(createFieldSetter(fieldName, fieldType));

    // setting value by builder
    Optional<TypeName> builderTypeOpt = findBuilderType(fieldParameter, elementUtils, typeUtils);
    if (builderTypeOpt.isPresent()) {
      TypeName builderType = builderTypeOpt.get();
      result.addMethod(
          BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderType));
    } else if (!isJavaClass(fieldType) && hasEmptyConstructor(fieldTypeElement, elementUtils)) {
      // TODO: Consumer funktioniet nur, wenn Klasse kein Interface/Enum/Abstrakte Classe/Record
      result.addMethod(createFieldConsumer(fieldName, fieldType));
    } else if (isList(fieldType)) {
      result.addMethod(
          createFieldConsumerWithBuilder(
              fieldName, map2TypeName(ArrayListBuilder.class), fieldType.getInnerType().get()));
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "List.of(%s)", new TypeNameArray(fieldType.getInnerType().get(), false)));
    } else if (isMap(fieldType)) {
      // TODO MAP (having 2 inner classes, TypeNameGeneric is not able to adress that yet)
    } else if (isSet(fieldType)) {
      result.addMethod(
          createFieldConsumerWithBuilder(
              fieldName, map2TypeName(HashSetBuilder.class), fieldType.getInnerType().get()));
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "Set.of(%s)", new TypeNameArray(fieldType.getInnerType().get(), true)));
    }

    // setting value by supplier
    result.addMethod(createFieldSupplier(fieldName, fieldType));

    return Optional.of(result);
  }

  private static MethodDto createFieldSetter(String fieldName, TypeName fieldType) {
    return createFieldSetterWithTransform(fieldName, null, fieldType);
  }

  private static MethodDto createFieldSetterWithTransform(
      String fieldName, String transform, TypeName fieldType) {
    String fieldSetterMethodName = "set" + StringUtils.capitalize(fieldName);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(fieldType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.PROXY);
    String params;
    if (StringUtils.isBlank(transform)) {
      params = parameter.getParameterName();
    } else {
      params = String.format(transform, parameter.getParameterName());
    }
    methodDto.setCode(
        """
        instance.$dtoMethod:N($dtoMethodParams:N);
        return this;
        """);
    methodDto.addArgument("dtoMethod", fieldSetterMethodName);
    methodDto.addArgument("dtoMethodParams", params);
    return methodDto;
  }

  private static MethodDto createFieldConsumer(String fieldName, TypeName fieldType) {
    String fieldSetterMethodName = "set" + StringUtils.capitalize(fieldName);
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + "Consumer");
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER);
    methodDto.setCode(
        """
        $helperType:T consumer = new $helperType:T();
        $dtoMethodParam:N.accept(consumer);
        instance.$dtoMethod:N(consumer);
        return this;
        """);
    methodDto.addArgument("dtoMethod", fieldSetterMethodName);
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    methodDto.addArgument("helperType", fieldType);
    return methodDto;
  }

  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName, TypeName builderType, TypeName builderTargetType) {
    TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(builderType, builderTargetType);
    return BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderTypeGeneric);
  }

  private static MethodDto createFieldConsumerWithBuilder(String fieldName, TypeName builderType) {
    String fieldSetterMethodName = "set" + StringUtils.capitalize(fieldName);
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), builderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + BUILDER_SUFFIX + "Consumer");
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER_BY_BUILDER);
    methodDto.setCode(
        """
        $helperType:T builder = new $helperType:T();
        $dtoMethodParam:N.accept(builder);
        instance.$dtoMethod:N(builder.build());
        return this;
        """);
    methodDto.addArgument("dtoMethod", fieldSetterMethodName);
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    methodDto.addArgument("helperType", builderType);
    return methodDto;
  }

  private static MethodDto createFieldSupplier(String fieldName, TypeName fieldType) {
    String fieldSetterMethodName = "set" + StringUtils.capitalize(fieldName);
    TypeNameGeneric supplierType = new TypeNameGeneric(map2TypeName(Supplier.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + "Supplier");
    parameter.setParameterTypeName(supplierType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.SUPPLIER);
    methodDto.setCode(
        """
        instance.$dtoMethod:N($dtoMethodParam:N.get());
        return this;
        """);
    methodDto.addArgument("dtoMethod", fieldSetterMethodName);
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    return methodDto;
  }

  private static Optional<TypeName> findBuilderType(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    TypeMirror typeOfParameter = param.asType();
    if (typeOfParameter.getKind() == ARRAY) {
      return Optional.empty();
    }
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
}
