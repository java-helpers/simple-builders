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

import java.util.List;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates ArrayListBuilder consumer methods for array fields.
 *
 * <p>This generator creates methods that accept a Consumer&lt;ArrayListBuilder&lt;T&gt;&gt; to
 * build array fields using a fluent builder API. This provides a convenient way to construct arrays
 * using the ArrayListBuilder utility.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For String[] keywords field:
 * public BookDtoBuilder keywords(Consumer<ArrayListBuilder<String>> keywordsBuilderConsumer) {
 *   ArrayListBuilder<String> builder = this.keywords.isSet()
 *     ? new ArrayListBuilder<>(java.util.List.of(this.keywords.value()))
 *     : new ArrayListBuilder<>();
 *   keywordsBuilderConsumer.accept(builder);
 *   this.keywords = changedValue(builder.build().toArray(new String[0]));
 *   return this;
 * }
 *
 * // For int[] pages field:
 * public BookDtoBuilder pages(Consumer<ArrayListBuilder<Integer>> pagesBuilderConsumer) {
 *   ArrayListBuilder<Integer> builder = this.pages.isSet()
 *     ? new ArrayListBuilder<>(java.util.List.of(this.pages.value()))
 *     : new ArrayListBuilder<>();
 *   pagesBuilderConsumer.accept(builder);
 *   this.pages = changedValue(builder.build().toArray(new Integer[0]));
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 25 (medium - builder consumers are useful but basic setters come first)
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateBuilderConsumer()}.
 */
public class ArrayBuilderConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 25;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }

    return field.getFieldType() instanceof TypeNameArray;
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    TypeName fieldType = field.getFieldType();

    if (!(fieldType instanceof TypeNameArray arrayType)) {
      return List.of();
    }

    TypeName elementType = arrayType.getTypeOfArray();
    TypeName collectionBuilderType = map2TypeName(ArrayListBuilder.class);

    MethodDto method =
        createFieldConsumerWithArrayBuilder(
            field, collectionBuilderType, elementType, builderType, context);

    return List.of(method);
  }

  private MethodDto createFieldConsumerWithArrayBuilder(
      FieldDto field,
      TypeName collectionBuilderType,
      TypeName elementType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    String fieldName = field.getFieldNameEstimated();
    String fieldNameInBuilder = field.getFieldName();
    TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(collectionBuilderType, elementType);
    TypeNameGeneric consumerType = MethodGeneratorUtil.createConsumerType(builderTypeGeneric);

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
            .formatted(fieldName, parameter.getParameterName(), field.getJavaDoc()));
    return methodDto;
  }
}
