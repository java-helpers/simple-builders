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
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates array-from-List conversion methods for array fields.
 *
 * <p>This generator creates setter methods that accept a List parameter and convert it to an array
 * for array fields. This provides a more convenient way to set array values using List API.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For String[] keywords field:
 * public BookDtoBuilder keywords(List<String> keywords) {
 *   this.keywords = changedValue(keywords.toArray(new String[0]));
 *   return this;
 * }
 *
 * // For int[] pages field:
 * public BookDtoBuilder pages(List<Integer> pages) {
 *   this.pages = changedValue(pages.toArray(new Integer[0]));
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 35 (medium-high - array conversions are useful but basic setters come first)
 *
 * <p>This generator applies to all array fields and provides a convenient List-based API for
 * setting array values.
 */
public class ArrayConversionGenerator implements MethodGenerator {

  private static final int PRIORITY = 35;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
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
    TypeNameGeneric listType = new TypeNameGeneric(map2TypeName(List.class), elementType);

    MethodDto method =
        createFieldSetterForArrayFromList(field, listType, elementType, builderType, context);

    return List.of(method);
  }

  private MethodDto createFieldSetterForArrayFromList(
      FieldDto field,
      TypeName listType,
      TypeName elementType,
      TypeName builderType,
      ProcessingContext context) {
    String fieldName = field.getFieldNameEstimated();
    String fieldNameInBuilder = field.getFieldName();
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
            .formatted(fieldName, parameter.getParameterName(), field.getJavaDoc()));
    return methodDto;
  }
}
