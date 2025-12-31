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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for fields with concrete classes that have empty constructors.
 *
 * <p>This generator creates methods that accept a Consumer&lt;FieldType&gt; to configure field
 * instances created via their no-arg constructor.
 */
public class FieldConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 54;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateFieldConsumer()) {
      return false;
    }
    // Only apply if no builder consumer applies (builder has higher priority)
    if (field.getFieldType().getBuilderType().isPresent()) {
      return false;
    }

    // Don't apply to List/Set/Map fields - they have their own specific consumer generators
    if (field.getFieldType() instanceof TypeNameList
        || field.getFieldType() instanceof TypeNameSet
        || field.getFieldType() instanceof TypeNameMap) {
      return false;
    }

    return field.getFieldType().hasEmptyConstructor();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!field.getFieldType().hasEmptyConstructor()) {
      return Collections.emptyList();
    }

    MethodDto method =
        createFieldConsumer(
            field.getFieldName(),
            field.getFieldName(),
            field.getJavaDoc(),
            field.getFieldType(),
            builderType,
            context);
    return List.of(method);
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
}
