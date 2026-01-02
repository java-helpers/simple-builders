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
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.isParameterizedOptional;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates unboxed Optional helper methods for Optional&lt;T&gt; fields.
 *
 * <p>This generator creates convenience methods that accept the inner type T directly and wrap it
 * in Optional.ofNullable() automatically. This makes it easier to set Optional values without
 * explicitly wrapping them.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For Optional<String> subtitle field:
 * public BookDtoBuilder subtitle(String subtitle) {
 *   this.subtitle = changedValue(Optional.ofNullable(subtitle));
 *   return this;
 * }
 *
 * // For Optional<Integer> rating field:
 * public BookDtoBuilder rating(Integer rating) {
 *   this.rating = changedValue(Optional.ofNullable(rating));
 *   return this;
 * }
 * </pre>
 *
 * <p>Example: For {@code Optional<String> name}, generates: {@code name(String name)}
 *
 * <p>Priority: 70 (high - Optional unboxing is very useful for Optional fields)
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateUnboxedOptional()}.
 */
public class OptionalHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 70;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateUnboxedOptional()) {
      return false;
    }
    return isParameterizedOptional(field.getFieldType());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    TypeNameGeneric genericType = (TypeNameGeneric) field.getFieldType();
    List<TypeName> innerTypes = genericType.getInnerTypeArguments();

    if (innerTypes.isEmpty()) {
      return Collections.emptyList();
    }

    MethodDto method =
        createFieldSetterWithTransform(field, "Optional.ofNullable(%s)", builderType, context);

    return Collections.singletonList(method);
  }

  /**
   * Creates a field setter method with optional transform.
   *
   * @param field the field DTO containing all field information
   * @param transform optional transform expression (e.g., "Optional.ofNullable(%s)")
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the setter
   */
  private MethodDto createFieldSetterWithTransform(
      FieldDto field, String transform, TypeName builderType, ProcessingContext context) {

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(field.getFieldName());
    parameter.setParameterTypeName(field.getFieldType());

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(field.getFieldName(), context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    String params;
    if (StringUtils.isBlank(transform)) {
      params = parameter.getParameterName();
    } else {
      params = String.format(transform, parameter.getParameterName());
    }

    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParams:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, field.getFieldName());
    methodDto.addArgument(ARG_DTO_METHOD_PARAMS, params);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);

    methodDto.setPriority(MethodDto.PRIORITY_HIGH);

    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @return current instance of builder
        """
            .formatted(
                field.getFieldName(),
                parameter.getParameterName(),
                field.getJavaDoc() != null ? field.getJavaDoc() : ""));

    return methodDto;
  }
}
