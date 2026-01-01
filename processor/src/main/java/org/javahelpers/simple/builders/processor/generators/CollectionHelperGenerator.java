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

import static org.javahelpers.simple.builders.processor.generators.MethodGeneratorUtil.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates collection helper methods for List, Set, and array fields.
 *
 * <p>This generator creates:
 *
 * <ul>
 *   <li>add2FieldName methods for adding single elements to List/Set fields
 *   <li>Array-from-List conversion methods for array fields
 *   <li>ArrayListBuilder consumer methods for array fields
 * </ul>
 *
 * <p>This generator respects configuration flags:
 *
 * <ul>
 *   <li>{@code shouldGenerateAddToCollectionHelpers()} for add2 methods
 *   <li>{@code shouldGenerateBuilderConsumer()} for ArrayListBuilder methods
 * </ul>
 */
public class CollectionHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 30;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    TypeName fieldType = field.getFieldType();

    if (fieldType instanceof TypeNameArray) {
      return true;
    }

    if (context.getConfiguration().shouldGenerateAddToCollectionHelpers()) {
      if (fieldType instanceof TypeNameList listType && listType.isParameterized()) {
        return true;
      }
      if (fieldType instanceof TypeNameSet setType && setType.isParameterized()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    List<MethodDto> methods = new ArrayList<>();
    TypeName fieldType = field.getFieldType();

    if (fieldType instanceof TypeNameArray arrayType) {
      TypeName elementType = arrayType.getTypeOfArray();

      TypeNameGeneric listType = new TypeNameGeneric(map2TypeName(List.class), elementType);
      MethodDto method1 =
          createFieldSetterForArrayFromList(
              field.getFieldNameEstimated(),
              field.getFieldName(),
              listType,
              elementType,
              builderType,
              context);
      methods.add(method1);

      if (context.getConfiguration().shouldGenerateBuilderConsumer()) {
        TypeName collectionBuilderType = map2TypeName(ArrayListBuilder.class);
        MethodDto method2 =
            createFieldConsumerWithArrayBuilder(
                field.getFieldNameEstimated(),
                field.getFieldName(),
                collectionBuilderType,
                elementType,
                builderType,
                context);
        methods.add(method2);
      }
    } else if (context.getConfiguration().shouldGenerateAddToCollectionHelpers()) {
      if (fieldType instanceof TypeNameList listType && listType.isParameterized()) {
        MethodDto addMethod =
            createAddToCollectionMethod(
                field.getFieldName(), listType, listType.getElementType(), builderType, context);
        methods.add(addMethod);
      } else if (fieldType instanceof TypeNameSet setType && setType.isParameterized()) {
        MethodDto addMethod =
            createAddToCollectionMethod(
                field.getFieldName(), setType, setType.getElementType(), builderType, context);
        methods.add(addMethod);
      }
    }

    return methods;
  }

  private MethodDto createFieldSetterForArrayFromList(
      String fieldName,
      String fieldNameInBuilder,
      TypeName listType,
      TypeName elementType,
      TypeName builderType,
      ProcessingContext context) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(listType);

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParams:N.toArray(new $elementType:T[0]));
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAMS, fieldName);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.addArgument(ARG_ELEMENT_TYPE, elementType);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldName));
    return methodDto;
  }

  private MethodDto createAddToCollectionMethod(
      String fieldName,
      TypeName fieldType,
      TypeName elementType,
      TypeName builderType,
      ProcessingContext context) {
    MethodDto methodDto = new MethodDto();
    String methodName = "add2" + StringUtils.capitalize(fieldName);
    methodDto.setMethodName(methodName);
    methodDto.setReturnType(builderType);

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName("element");
    parameter.setParameterTypeName(elementType);
    methodDto.addParameter(parameter);

    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    String collectionImpl;
    TypeName collectionVarType;
    if (fieldType instanceof TypeNameList listType) {
      collectionImpl = listType.isConcreteImplementation() ? listType.getClassName() : "ArrayList";
      collectionVarType = fieldType;
    } else if (fieldType instanceof TypeNameSet setType) {
      collectionImpl = setType.isConcreteImplementation() ? setType.getClassName() : "HashSet";
      collectionVarType = fieldType;
    } else {
      throw new IllegalArgumentException("Unsupported field type: " + fieldType);
    }

    methodDto.setCode(
        """
        $collectionVarType:T newCollection;
        if (this.$fieldName:N.isSet()) {
          newCollection = new $collectionImpl:T<>(this.$fieldName:N.value());
        } else {
          newCollection = new $collectionImpl:T<>();
        }
        newCollection.add(element);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(newCollection);
        return this;
        """);
    methodDto.addArgument("collectionVarType", collectionVarType);
    methodDto.addArgument("collectionImpl", new TypeName("java.util", collectionImpl));
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_ELEMENT_TYPE, elementType);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);

    methodDto.setJavadoc(
        """
        Adds a single element to <code>%s</code>.

        @param element the element to add
        @return current instance of builder
        """
            .formatted(fieldName));

    return methodDto;
  }

  private MethodDto createFieldConsumerWithArrayBuilder(
      String fieldName,
      String fieldNameInBuilder,
      TypeName collectionBuilderType,
      TypeName elementType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(collectionBuilderType, elementType);
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), builderTypeGeneric);

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(fieldName, context));
    methodDto.setReturnType(returnBuilderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet() ? new $helperType:T(java.util.List.of(this.$fieldName:N.value())) : new $helperType:T();
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(builder.build().toArray(new $elementType:T[0]));
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, builderTypeGeneric);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.addArgument(ARG_ELEMENT_TYPE, elementType);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> using the fluent builder consumer.

        @param %s consumer for %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldName));
    return methodDto;
  }
}
