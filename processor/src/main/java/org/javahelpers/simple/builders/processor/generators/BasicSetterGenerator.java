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

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates basic setter methods for builder fields.
 *
 * <p>This generator creates the primary setter method for each field, which accepts the field type
 * directly and stores it in the builder. The setter method:
 *
 * <ul>
 *   <li>Accepts a parameter of the field's type
 *   <li>Stores the value in a TrackedValue wrapper
 *   <li>Returns the builder instance for method chaining
 *   <li>Applies any field annotations to the parameter
 *   <li>Includes javadoc documentation
 * </ul>
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * public BookDtoBuilder title(String title) {
 *   this.title = changedValue(title);
 *   return this;
 * }
 *
 * public BookDtoBuilder pages(int pages) {
 *   this.pages = changedValue(pages);
 *   return this;
 * }
 *
 * public BookDtoBuilder tags(List<String> tags) {
 *   this.tags = changedValue(tags);
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 100 (highest - basic setters are fundamental to builder functionality)
 *
 * <p>This generator always applies to all fields and has the highest priority to ensure the basic
 * setter is always generated first.
 */
public class BasicSetterGenerator implements MethodGenerator {

  private static final int PRIORITY = 100;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    return true;
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    MethodDto setterMethod = createFieldSetterWithTransform(field, null, builderType, context);

    return Collections.singletonList(setterMethod);
  }

  /**
   * Creates a setter method with custom transformation logic.
   *
   * @param field the field DTO containing all field information
   * @param transform the transformation expression to apply
   * @param builderType the builder type
   * @param context processing context
   * @return the method DTO for the setter
   */
  protected MethodDto createFieldSetterWithTransform(
      FieldDto field, String transform, TypeName builderType, ProcessingContext context) {

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(field.getFieldName());
    parameter.setParameterTypeName(field.getFieldType());

    if (field.getParameterAnnotations() != null) {
      field.getParameterAnnotations().forEach(parameter::addAnnotation);
    }

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

    methodDto.setPriority(transform == null ? MethodDto.PRIORITY_HIGHEST : MethodDto.PRIORITY_HIGH);

    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @return current instance of builder
        """
            .formatted(
                field.getFieldName(),
                field.getFieldName(),
                field.getJavaDoc() != null ? field.getJavaDoc() : ""));

    return methodDto;
  }
}
