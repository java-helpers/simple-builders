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

import static java.util.stream.Collectors.toSet;
import static javax.lang.model.type.TypeKind.ARRAY;
import static org.javahelpers.simple.builders.processor.util.AnnotationValidator.validateAnnotatedElement;
import static org.javahelpers.simple.builders.processor.util.JavaLangAnalyser.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2MethodParameter;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.core.builders.HashMapBuilder;
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
    String simpleClassName = annotatedType.getSimpleName().toString();
    result.setBuilderTypeName(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
    result.setBuildingTargetTypeName(new TypeName(packageName, simpleClassName));

    // Extract generics from the annotated type via mapper (stream-based)
    JavaLangMapper.map2GenericParameterDtos(annotatedType, elementUtils, typeUtils)
        .forEach(result::addGeneric);

    // Extract constructors and their parameters (treat as constructor fields)
    Optional<ExecutableElement> constructorOpt =
        findConstructorForBuilder(annotatedType, elementUtils);
    if (constructorOpt.isPresent()) {
      ExecutableElement ctor = constructorOpt.get();
      for (VariableElement param : ctor.getParameters()) {
        Optional<FieldDto> fieldFromCtor =
            createFieldFromConstructor(annotatedType, param, elementUtils, typeUtils);
        fieldFromCtor.ifPresent(result::addFieldInConstructor);
      }
    }

    // Todo: moving into helper function
    List<? extends Element> allMembers = elementUtils.getAllMembers(annotatedType);
    List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);

    // Build a set of constructor field names to avoid duplicates from setters
    Set<String> ctorFieldNames =
        result.getConstructorFieldsForBuilder().stream()
            .map(FieldDto::getFieldName)
            .collect(toSet());

    for (ExecutableElement mth : methods) {
      // nur public
      if (isMethodRelevantForBuilder(mth)) {
        // Avoid adding setter-derived fields that duplicate constructor params
        Optional<FieldDto> maybeField =
            createFieldFromSetter(mth, result.getGenerics(), elementUtils, typeUtils);
        if (maybeField.isPresent()) {
          FieldDto field = maybeField.get();
          if (!ctorFieldNames.contains(field.getFieldName())) {
            result.addField(field);
          }
        }
      }
    }

    return result;
  }

  private static boolean isMethodRelevantForBuilder(ExecutableElement mth) {
    return isSetterForField(mth)
        && isNoMethodOfObjectClass(mth)
        && hasNoThrowablesDeclared(mth)
        && hasNoReturnValue(mth)
        && hasNotAnnotation(IgnoreInBuilder.class, mth)
        && isNotPrivate(mth)
        && isNotStatic(mth);
  }

  private static void addAdditionalHelperMethodsForField(
      FieldDto result, String fieldName, TypeName fieldType) {
    List<TypeName> innerTypes;
    int innerTypesCnt;
    if (fieldType instanceof TypeNameGeneric fieldTypeGeneric) {
      innerTypes = fieldTypeGeneric.getInnerTypeArguments();
      innerTypesCnt = innerTypes.size();
    } else {
      innerTypes = null;
      innerTypesCnt = 0;
    }

    if (isList(fieldType) && innerTypesCnt == 1) {
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "List.of(%s)", new TypeNameArray(innerTypes.get(0), false)));
    } else if (isSet(fieldType) && innerTypesCnt == 1) {
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "Set.of(%s)", new TypeNameArray(innerTypes.get(0), true)));
    } else if (isMap(fieldType) && innerTypesCnt == 2) {
      TypeName mapEntryType =
          new TypeNameArray(
              new TypeNameGeneric("java.util", "Map.Entry", innerTypes.get(0), innerTypes.get(1)),
              false);
      result.addMethod(
          createFieldSetterWithTransform(fieldName, "Map.ofEntries(%s)", mapEntryType));
    }
  }

  private static void addConsumerMethodsForField(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      Elements elementUtils,
      Types typeUtils) {
    // Do not generate supplier methods for generic type variables (e.g., T)
    if (fieldType instanceof TypeNameVariable) {
      return;
    }
    // Skip consumer generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }
    Optional<TypeName> builderTypeOpt = findBuilderType(fieldParameter, elementUtils, typeUtils);
    if (builderTypeOpt.isPresent()) {
      TypeName builderType = builderTypeOpt.get();
      result.addMethod(
          BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderType));
    } else if (!isJavaClass(fieldType)
        && fieldTypeElement != null
        && fieldTypeElement.getKind() == javax.lang.model.element.ElementKind.CLASS
        && !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        && hasEmptyConstructor(fieldTypeElement, elementUtils)) {
      // Only generate a Consumer for concrete classes with an accessible empty constructor
      result.addMethod(createFieldConsumer(fieldName, fieldType));
    } else if (isList(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1) {
      result.addMethod(
          createFieldConsumerWithBuilder(
              fieldName,
              map2TypeName(ArrayListBuilder.class),
              fieldTypeGeneric.getInnerTypeArguments().get(0)));
    } else if (isMap(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 2) {
      TypeName builderTargetTypeName =
          new TypeNameGeneric(
              map2TypeName(HashMapBuilder.class),
              fieldTypeGeneric.getInnerTypeArguments().get(0),
              fieldTypeGeneric.getInnerTypeArguments().get(1));
      MethodDto mapConsumerWithBuilder =
          BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderTargetTypeName);
      result.addMethod(mapConsumerWithBuilder);
    } else if (isSet(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1) {
      result.addMethod(
          createFieldConsumerWithBuilder(
              fieldName,
              map2TypeName(HashSetBuilder.class),
              fieldTypeGeneric.getInnerTypeArguments().get(0)));
    }
  }

  private static void addSupplierMethodsForField(
      FieldDto result, String fieldName, TypeName fieldType, TypeElement fieldTypeElement) {
    // Skip supplier generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }
    result.addMethod(createFieldSupplier(fieldName, fieldType));
  }

  private static Optional<FieldDto> createFieldFromSetter(
      ExecutableElement mth,
      List<GenericParameterDto> dtoGenerics,
      Elements elementUtils,
      Types typeUtils) {
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
    Element rawElement = typeUtils.asElement(fieldTypeMirror);
    TypeElement fieldTypeElement = rawElement instanceof TypeElement te ? te : null;

    // Extract only the @param Javadoc for the single setter parameter (if present)
    String fullJavaDoc = elementUtils.getDocComment(mth);
    String parameterJavaDocExtracted =
        JavaLangAnalyser.extractParamJavaDoc(fullJavaDoc, fieldParameter);
    String parameterJavaDoc =
        parameterJavaDocExtracted == null ? fieldName : parameterJavaDocExtracted;
    result.setJavaDoc(parameterJavaDoc);

    // extracting type of field
    MethodParameterDto fieldParameterDto =
        map2MethodParameter(fieldParameter, elementUtils, typeUtils);
    if (fieldParameterDto == null) {
      // TODO: Logging
      return Optional.empty();
    }
    TypeName fieldType = fieldParameterDto.getParameterType();
    result.setFieldType(fieldType);

    // Determine if a corresponding getter exists on the DTO and record its name
    TypeElement dtoType = (TypeElement) mth.getEnclosingElement();
    JavaLangAnalyser.findGetterForField(
            dtoType, fieldName, fieldTypeMirror, elementUtils, typeUtils)
        .ifPresent(
            getterExecutable -> result.setGetterName(getterExecutable.getSimpleName().toString()));

    // Finding generics declared on the setter itself (field-specific), e.g., <T extends
    // Serializable>
    if (CollectionUtils.isNotEmpty(mth.getTypeParameters())) {
      List<String> dtoGenericsNames =
          dtoGenerics.stream().map(GenericParameterDto::getName).toList();
      List<String> mthGenericsNames =
          mth.getTypeParameters().stream().map(tp -> tp.getSimpleName().toString()).toList();
      // If there are field-specific generics, no field in builder could be generated for it, so it
      // needs to be ignored
      if (CollectionUtils.containsAll(dtoGenericsNames, mthGenericsNames)) {
        // TODO: Logging
        return Optional.empty();
      }
    }

    // simple setter
    result.addMethod(createFieldSetterWithTransform(fieldName, null, fieldType));

    // add consumer/supplier generation via helpers
    addConsumerMethodsForField(
        result, fieldName, fieldType, fieldParameter, fieldTypeElement, elementUtils, typeUtils);
    addSupplierMethodsForField(result, fieldName, fieldType, fieldTypeElement);
    addAdditionalHelperMethodsForField(result, fieldName, fieldType);

    return Optional.of(result);
  }

  /**
   * Determines which constructor to use for builder initialization. Currently selects the
   * constructor with the highest number of parameters. Returns empty if no constructor has
   * parameters (i.e., only default constructor or none found).
   */
  private static Optional<ExecutableElement> findConstructorForBuilder(
      TypeElement annotatedType, Elements elementUtils) {
    List<ExecutableElement> ctors =
        ElementFilter.constructorsIn(elementUtils.getAllMembers(annotatedType));
    ExecutableElement selected = null;
    int maxParams = -1;
    for (ExecutableElement ctor : ctors) {
      int p = ctor.getParameters().size();
      if (p > maxParams) {
        maxParams = p;
        selected = ctor;
      }
    }
    return (selected != null && maxParams > 0) ? Optional.of(selected) : Optional.empty();
  }

  /**
   * Creates a FieldDto from a constructor parameter, including a simple builder setter to supply
   * the constructor argument.
   */
  private static Optional<FieldDto> createFieldFromConstructor(
      TypeElement dtoType, VariableElement param, Elements elementUtils, Types typeUtils) {
    MethodParameterDto paramDto = map2MethodParameter(param, elementUtils, typeUtils);
    if (paramDto == null) {
      return Optional.empty();
    }
    String fieldName = param.getSimpleName().toString();
    TypeName fieldType = paramDto.getParameterType();
    FieldDto ctorField = new FieldDto();
    ctorField.setFieldName(fieldName);
    ctorField.setFieldType(fieldType);
    // Find matching getter on the DTO type
    JavaLangAnalyser.findGetterForField(dtoType, fieldName, param.asType(), elementUtils, typeUtils)
        .ifPresent(getter -> ctorField.setGetterName(getter.getSimpleName().toString()));
    // Provide a simple setter in builder to set the constructor argument
    ctorField.addMethod(createFieldSetterWithTransform(fieldName, null, fieldType));
    return Optional.of(ctorField);
  }

  private static MethodDto createFieldSetterWithTransform(
      String fieldName, String transform, TypeName fieldType) {
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
        this.$fieldName:N = $dtoMethodParams:N;
        return this;
        """);
    methodDto.addArgument("fieldName", fieldName);
    methodDto.addArgument("dtoMethodParams", params);
    return methodDto;
  }

  private static MethodDto createFieldConsumer(String fieldName, TypeName fieldType) {
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
        this.$fieldName:N = consumer;
        return this;
        """);
    methodDto.addArgument("fieldName", fieldName);
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
        this.$fieldName:N = builder.build();
        return this;
        """);
    methodDto.addArgument("fieldName", fieldName);
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    methodDto.addArgument("helperType", builderType);
    return methodDto;
  }

  private static MethodDto createFieldSupplier(String fieldName, TypeName fieldType) {
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
        this.$fieldName:N = $dtoMethodParam:N.get();
        return this;
        """);
    methodDto.addArgument("fieldName", fieldName);
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    return methodDto;
  }

  private static Optional<TypeName> findBuilderType(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    TypeMirror typeOfParameter = param.asType();
    if (typeOfParameter.getKind() == ARRAY || typeOfParameter.getKind().isPrimitive()) {
      return Optional.empty();
    }
    Element elementOfParameter = typeUtils.asElement(typeOfParameter);
    if (elementOfParameter == null) {
      // Can happen for primitives or certain compiler-internal types; nothing to build
      return Optional.empty();
    }
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
