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

package org.javahelpers.simple.builders.processor.generators;

import static javax.lang.model.type.TypeKind.ARRAY;
import static org.javahelpers.simple.builders.processor.generators.MethodGeneratorUtil.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangAnalyser.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.builders.HashMapBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilderWithElementBuilders;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for builder fields.
 *
 * <p>This generator creates methods that accept Consumer functional interfaces for various
 * scenarios:
 *
 * <ul>
 *   <li>Builder consumers - for fields whose type has a @SimpleBuilder annotation
 *   <li>Field consumers - for concrete classes with empty constructors
 *   <li>StringBuilder consumers - for String and Optional&lt;String&gt; fields
 *   <li>Collection builders - for List, Set, and Map fields with collection builder support
 * </ul>
 *
 * <p>Consumer methods follow a chain-of-responsibility pattern where the first applicable consumer
 * type is generated.
 */
public class ConsumerMethodGenerator implements MethodGenerator {

  private static final int PRIORITY = 50;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      FieldDto field,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      ProcessingContext context) {
    if (field.getFieldType() instanceof TypeNameVariable) {
      return false;
    }
    if (isFunctionalInterface(fieldTypeElement)) {
      return false;
    }
    return true;
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      TypeName builderType,
      ProcessingContext context) {

    List<MethodDto> methods = new ArrayList<>();

    if (tryAddBuilderConsumer(field, fieldParameter, builderType, context, methods)) {
      return methods;
    }
    if (tryAddFieldConsumer(field, fieldTypeElement, builderType, context, methods)) {
      return methods;
    }
    if (tryAddListConsumer(field, fieldParameter, builderType, context, methods)) {
      return methods;
    }
    if (tryAddMapConsumer(field, builderType, context, methods)) {
      return methods;
    }
    if (tryAddSetConsumer(field, fieldParameter, builderType, context, methods)) {
      return methods;
    }
    tryAddStringBuilderConsumer(field, builderType, context, methods);

    return methods;
  }

  private boolean tryAddBuilderConsumer(
      FieldDto field,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context,
      List<MethodDto> methods) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    Optional<TypeName> fieldBuilderOpt = resolveBuilderType(fieldParameter, context);
    if (fieldBuilderOpt.isPresent()) {
      TypeName fieldBuilderType = fieldBuilderOpt.get();
      MethodDto method =
          createFieldConsumerWithBuilder(field, fieldBuilderType, builderType, context);
      methods.add(method);
      return true;
    }
    return false;
  }

  private boolean tryAddFieldConsumer(
      FieldDto field,
      TypeElement fieldTypeElement,
      TypeName builderType,
      ProcessingContext context,
      List<MethodDto> methods) {
    if (!context.getConfiguration().shouldGenerateFieldConsumer()) {
      return false;
    }
    if (!isJavaClass(field.getFieldType())
        && fieldTypeElement != null
        && fieldTypeElement.getKind() == javax.lang.model.element.ElementKind.CLASS
        && !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        && hasEmptyConstructor(fieldTypeElement, context)) {
      MethodDto method =
          createFieldConsumer(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              field.getFieldType(),
              builderType,
              context);
      methods.add(method);
      return true;
    }
    return false;
  }

  private boolean tryAddStringBuilderConsumer(
      FieldDto field, TypeName builderType, ProcessingContext context, List<MethodDto> methods) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    if (shouldGenerateStringBuilderConsumer(field.getFieldType())) {
      String transform =
          isOptionalString(field.getFieldType())
              ? "Optional.of(builder.toString())"
              : "builder.toString()";
      MethodDto method =
          createStringBuilderConsumer(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              transform,
              builderType,
              context);
      methods.add(method);
      return true;
    }
    return false;
  }

  private boolean tryAddListConsumer(
      FieldDto field,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context,
      List<MethodDto> methods) {
    if (!(field.getFieldType() instanceof TypeNameList fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getElementType();

    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseArrayListBuilderWithElementBuilders()) {
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(ArrayListBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          createFieldConsumerWithElementBuilders(
              field, collectionBuilderType, elementBuilderType.get(), builderType, context);
      methods.add(method);
    } else if (context.getConfiguration().shouldUseArrayListBuilder()) {
      TypeName collectionBuilderType = map2TypeName(ArrayListBuilder.class);
      MethodDto method =
          createFieldConsumerWithBuilder(
              field, collectionBuilderType, elementType, builderType, context);
      methods.add(method);
    } else {
      return false;
    }
    return true;
  }

  private boolean tryAddMapConsumer(
      FieldDto field, TypeName builderType, ProcessingContext context, List<MethodDto> methods) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    if (!context.getConfiguration().shouldUseHashMapBuilder()) {
      return false;
    }
    if (!(field.getFieldType() instanceof TypeNameMap fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return false;
    }

    TypeNameGeneric builderTargetTypeName =
        new TypeNameGeneric(
            map2TypeName(HashMapBuilder.class),
            fieldTypeGeneric.getKeyType(),
            fieldTypeGeneric.getValueType());
    MethodDto mapConsumerWithBuilder =
        createFieldConsumerWithBuilder(field, builderTargetTypeName, builderType, context);
    methods.add(mapConsumerWithBuilder);
    return true;
  }

  private boolean tryAddSetConsumer(
      FieldDto field,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context,
      List<MethodDto> methods) {
    if (!(field.getFieldType() instanceof TypeNameSet fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getElementType();

    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseHashSetBuilderWithElementBuilders()) {
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(HashSetBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          createFieldConsumerWithElementBuilders(
              field, collectionBuilderType, elementBuilderType.get(), builderType, context);
      methods.add(method);
    } else if (context.getConfiguration().shouldUseHashSetBuilder()) {
      TypeName collectionBuilderType = map2TypeName(HashSetBuilder.class);
      MethodDto method =
          createFieldConsumerWithBuilder(
              field, collectionBuilderType, elementType, builderType, context);
      methods.add(method);
    } else {
      return false;
    }
    return true;
  }

  private MethodDto createFieldConsumer(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      TypeName fieldType,
      TypeName builderType,
      ProcessingContext context) {
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        $helperType:T consumer = this.$fieldName:N.isSet() ? this.$fieldName:N.value() : new $helperType:T();
        $dtoMethodParam:N.accept(consumer);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(consumer);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, fieldType);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> by executing the provided consumer.

        @param %s consumer providing an instance of %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavadoc));
    return methodDto;
  }

  private MethodDto createStringBuilderConsumer(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      String transform,
      TypeName builderType,
      ProcessingContext context) {
    TypeName stringBuilderType = map2TypeName(StringBuilder.class);
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), stringBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + "StringBuilderConsumer");
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        StringBuilder builder = new StringBuilder();
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument("transform", transform);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setReturnType(builderType);
    methodDto.setPriority(MethodDto.PRIORITY_LOW);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> by executing the provided consumer.

        @param %s consumer providing an instance of %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavadoc));
    return methodDto;
  }

  private MethodDto createFieldConsumerWithBuilder(
      FieldDto field,
      TypeName consumerBuilderType,
      TypeName builderType,
      ProcessingContext context) {
    return createFieldConsumerWithBuilder(
        field,
        consumerBuilderType,
        "this.$fieldName:N.value()",
        "",
        Map.of(),
        builderType,
        context);
  }

  private MethodDto createFieldConsumerWithBuilder(
      FieldDto field,
      TypeName consumerBuilderType,
      TypeName builderTargetType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric builderTypeGeneric =
        new TypeNameGeneric(consumerBuilderType, builderTargetType);
    return createFieldConsumerWithBuilder(field, builderTypeGeneric, returnBuilderType, context);
  }

  private MethodDto createFieldConsumerWithElementBuilders(
      FieldDto field,
      TypeName collectionBuilderType,
      TypeName elementBuilderType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    return createFieldConsumerWithBuilder(
        field,
        collectionBuilderType,
        "this.$fieldName:N.value(), $elementBuilderType:T::create",
        "$elementBuilderType:T::create",
        Map.of("elementBuilderType", elementBuilderType),
        returnBuilderType,
        context);
  }

  private MethodDto createFieldConsumerWithBuilder(
      FieldDto field,
      TypeName consumerBuilderType,
      String constructorArgsWithValue,
      String additionalConstructorArgs,
      Map<String, TypeName> additionalArguments,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), consumerBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(field.getFieldName() + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(field.getFieldName(), context));
    methodDto.setReturnType(returnBuilderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    String buildExpression = calculateBuildExpression(field.getFieldType());

    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet() ? new $helperType:T(%s) : new $helperType:T(%s);
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($buildExpression:N);
        return this;
        """
            .formatted(constructorArgsWithValue, additionalConstructorArgs));
    methodDto.addArgument(ARG_FIELD_NAME, field.getFieldName());
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, consumerBuilderType);
    methodDto.addArgument("buildExpression", buildExpression);
    additionalArguments.forEach(methodDto::addArgument);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> using a builder consumer that produces the value.

        @param %s consumer providing an instance of a builder for %s
        @return current instance of builder
        """
            .formatted(field.getFieldName(), parameter.getParameterName(), field.getJavaDoc()));
    return methodDto;
  }

  private boolean shouldGenerateStringBuilderConsumer(TypeName fieldType) {
    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      return true;
    }
    return isOptionalString(fieldType);
  }

  private Optional<TypeName> resolveBuilderType(VariableElement param, ProcessingContext context) {
    TypeMirror typeOfParameter = param.asType();
    if (typeOfParameter.getKind() == ARRAY || typeOfParameter.getKind().isPrimitive()) {
      return Optional.empty();
    }
    javax.lang.model.element.Element elementOfParameter = context.asElement(typeOfParameter);
    if (!(elementOfParameter instanceof TypeElement typeElement)) {
      return Optional.empty();
    }
    if (hasGenericTypes(typeElement)) {
      context.debug(
          "  -> Skipping builder lookup for generic type %s", typeElement.getSimpleName());
      return Optional.empty();
    }
    return resolveBuilderTypeFromTypeElement(typeElement, context);
  }

  private Optional<TypeName> resolveBuilderType(
      TypeName elementType, TypeMirror elementTypeMirror, ProcessingContext context) {
    if (elementTypeMirror == null) {
      return Optional.empty();
    }
    if (elementType instanceof TypeNameVariable || elementType instanceof TypeNamePrimitive) {
      context.debug("  -> Skipping type variable or primitive: %s", elementType);
      return Optional.empty();
    }
    javax.lang.model.element.Element element = context.asElement(elementTypeMirror);
    if (!(element instanceof TypeElement typeElement)) {
      context.debug("  -> Element is not a TypeElement: %s", element);
      return Optional.empty();
    }
    return resolveBuilderTypeFromTypeElement(typeElement, context);
  }

  private Optional<TypeName> resolveBuilderTypeFromTypeElement(
      TypeElement typeElement, ProcessingContext context) {
    Optional<javax.lang.model.element.AnnotationMirror> foundBuilderAnnotation =
        findAnnotation(
            typeElement, org.javahelpers.simple.builders.core.annotations.SimpleBuilder.class);
    if (foundBuilderAnnotation.isEmpty()) {
      context.debug("  -> Type %s has no @SimpleBuilder", typeElement.getSimpleName());
      return Optional.empty();
    }

    String packageName = context.getPackageName(typeElement);
    String simpleClassName = typeElement.getSimpleName().toString();
    String builderSuffix = context.getConfiguration().getBuilderSuffix();
    context.debug(
        "  -> Found @SimpleBuilder on type %s.%s, will use %s%s",
        packageName, simpleClassName, simpleClassName, builderSuffix);
    return Optional.of(new TypeName(packageName, simpleClassName + builderSuffix));
  }

  private TypeMirror extractFirstTypeArgument(TypeMirror typeMirror) {
    if (typeMirror instanceof javax.lang.model.type.DeclaredType declaredType) {
      List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
      if (!typeArguments.isEmpty()) {
        return typeArguments.get(0);
      }
    }
    return null;
  }

  /**
   * Wraps an expression with a concrete collection constructor if needed to preserve the specific
   * collection type. Only wraps concrete implementations (ArrayList, LinkedList, HashSet, TreeSet,
   * HashMap, TreeMap, etc.). Returns the base expression unchanged for interface types.
   *
   * @param fieldType the field type to check
   * @param baseExpression the base expression to potentially wrap
   * @return the wrapped expression for concrete collections, or base expression otherwise
   */
  private String wrapConcreteCollectionType(TypeName fieldType, String baseExpression) {
    if (fieldType instanceof TypeNameList listType && listType.isConcreteImplementation()) {
      return "new " + listType.getClassName() + "<>(" + baseExpression + ")";
    } else if (fieldType instanceof TypeNameSet setType && setType.isConcreteImplementation()) {
      return "new " + setType.getClassName() + "<>(" + baseExpression + ")";
    } else if (fieldType instanceof TypeNameMap mapType && mapType.isConcreteImplementation()) {
      return "new " + mapType.getClassName() + "<>(" + baseExpression + ")";
    }
    return baseExpression;
  }

  /**
   * Calculates the build expression wrapper for builder consumers.
   *
   * @param fieldType the original field type
   * @return the wrapped expression or the base expression if no wrapping needed
   */
  private String calculateBuildExpression(TypeName fieldType) {
    return wrapConcreteCollectionType(fieldType, "builder.build()");
  }
}
