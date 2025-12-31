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
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for String and Optional&lt;String&gt; fields using
 * StringBuilder.
 *
 * <p>This generator creates methods that accept a Consumer&lt;StringBuilder&gt; to build string
 * values.
 */
public class StringBuilderConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 45;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    // Don't apply if field has a builder or empty constructor (higher priority)
    if (field.getFieldType().getBuilderType().isPresent()
        || field.getFieldType().hasEmptyConstructor()) {
      return false;
    }
    return shouldGenerateStringBuilderConsumer(field.getFieldType());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!shouldGenerateStringBuilderConsumer(field.getFieldType())) {
      return Collections.emptyList();
    }

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
    return List.of(method);
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

  private boolean shouldGenerateStringBuilderConsumer(TypeName fieldType) {
    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      return true;
    }
    return isOptionalString(fieldType);
  }
}
