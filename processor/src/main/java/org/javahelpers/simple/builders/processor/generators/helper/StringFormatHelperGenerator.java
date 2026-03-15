/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.generators.helper;

import static org.javahelpers.simple.builders.processor.analysis.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.analysis.TypeNameAnalyser.isParameterizedOptional;
import static org.javahelpers.simple.builders.processor.analysis.TypeNameAnalyser.isString;
import static org.javahelpers.simple.builders.processor.generators.field.MethodGeneratorUtil.*;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates String.format helper methods for String and {@code Optional<String>} fields.
 *
 * <p>This generator creates convenience methods that accept a format string and varargs arguments,
 * internally using {@code String.format()} to produce the final value. This provides a concise way
 * to build formatted strings directly in the builder chain.
 *
 * <p><b>Important behavior:</b> The method accepts a format string and optional arguments, applies
 * {@code String.format()}, and assigns the result to the field. For {@code Optional<String>}
 * fields, the formatted result is wrapped in {@code Optional.of()}.
 *
 * <p><b>Requirements:</b> Only applies to {@code String} or {@code Optional<String>} fields. Does
 * not apply to String arrays.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateStringFormatHelpers} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.Optional;
 *
 * @SimpleBuilder
 * public record BookDto(String title, Optional<String> description) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .title("Book #%d: %s", 1, "Java Patterns")
 *     .description("Published in %d by %s", 2024, "Tech Press")
 *     .build();
 * }</pre>
 */
public class StringFormatHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 80;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateStringFormatHelpers()) {
      return false;
    }
    TypeName fieldType = field.getFieldType();

    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      return true;
    }

    if (isParameterizedOptional(fieldType)) {
      TypeNameGeneric genericType = (TypeNameGeneric) fieldType;
      List<TypeName> innerTypes = genericType.getInnerTypeArguments();
      if (CollectionUtils.isNotEmpty(innerTypes) && isString(innerTypes.get(0))) {
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

    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      MethodDto method =
          createStringFormatMethodWithTransform(
              field.getOriginalFieldName(),
              field.getFieldNameInBuilder(),
              "String.format(format, args)",
              field.getParameterAnnotations(),
              builderType,
              context);
      methods.add(method);
    } else if (isParameterizedOptional(fieldType)) {
      TypeNameGeneric genericType = (TypeNameGeneric) fieldType;
      List<TypeName> innerTypes = genericType.getInnerTypeArguments();
      if (!innerTypes.isEmpty() && isString(innerTypes.get(0))) {
        MethodDto method =
            createStringFormatMethodWithTransform(
                field.getOriginalFieldName(),
                field.getFieldNameInBuilder(),
                "Optional.of(String.format(format, args))",
                field.getParameterAnnotations(),
                builderType,
                context);
        methods.add(method);
      }
    }

    return methods;
  }

  /**
   * Creates a String.format helper method with transform.
   *
   * @param fieldName the name of the field (estimated)
   * @param fieldNameInBuilder the builder field name (may be renamed)
   * @param transform the transform expression (e.g., "String.format(format, args)")
   * @param annotations annotations to apply to the format parameter
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the String.format helper
   */
  private MethodDto createStringFormatMethodWithTransform(
      String fieldName,
      String fieldNameInBuilder,
      String transform,
      List<AnnotationDto> annotations,
      TypeName builderType,
      ProcessingContext context) {
    TypeName stringType = map2TypeName(String.class);

    MethodParameterDto formatParam = new MethodParameterDto();
    formatParam.setParameterName("format");
    formatParam.setParameterTypeName(stringType);
    if (annotations != null) {
      annotations.forEach(formatParam::addAnnotation);
    }

    MethodParameterDto argsParam = new MethodParameterDto();
    argsParam.setParameterName("args");
    argsParam.setParameterTypeName(new TypeNameArray(TypeName.of(Object.class)));

    MethodDto methodDto = new MethodDto(generateBuilderMethodName(fieldName, context), builderType);
    methodDto.addParameter(formatParam);
    methodDto.addParameter(argsParam);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument("fieldName", fieldNameInBuilder);
    methodDto.addArgument("transform", transform);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);
    methodDto.setJavadoc(
        """
        Sets the String value for <code>%s</code> by using String.format(format, args).
        See {@link String#format(String, Object...)} for details.

        @param %s A format string
        @param %s Arguments referenced by the format specifiers in the format string.
        @return current instance of builder
        """
            .formatted(fieldName, formatParam.getParameterName(), argsParam.getParameterName()));
    return methodDto;
  }
}
