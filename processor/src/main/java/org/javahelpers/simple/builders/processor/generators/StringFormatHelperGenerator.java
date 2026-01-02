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
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.isParameterizedOptional;
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.isString;

import java.util.ArrayList;
import java.util.List;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates String.format helper methods for String and Optional&lt;String&gt; fields.
 *
 * <p>This generator creates convenience methods that accept a format string and varargs arguments,
 * internally using {@code String.format()} to produce the final value.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For String title field:
 * public BookDtoBuilder title(String format, Object... args) {
 *   this.title = changedValue(String.format(format, args));
 *   return this;
 * }
 *
 * // For Optional<String> subtitle field:
 * public BookDtoBuilder subtitle(String format, Object... args) {
 *   this.subtitle = changedValue(Optional.ofNullable(String.format(format, args)));
 *   return this;
 * }
 * </pre>
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>For {@code String name}: {@code name(String format, Object... args)}
 *   <li>For {@code Optional<String> message}: {@code message(String format, Object... args)}
 * </ul>
 *
 * <p>Priority: 80 (high - String formatting is commonly used utility)
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateStringFormatHelpers()}.
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
      if (!innerTypes.isEmpty() && isString(innerTypes.get(0))) {
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
              field.getFieldNameEstimated(),
              field.getFieldName(),
              field.getJavaDoc(),
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
                field.getFieldNameEstimated(),
                field.getFieldName(),
                field.getJavaDoc(),
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
   * @param fieldJavadoc the javadoc for the field
   * @param transform the transform expression (e.g., "String.format(format, args)")
   * @param annotations annotations to apply to the format parameter
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the String.format helper
   */
  private MethodDto createStringFormatMethodWithTransform(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
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

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(formatParam);
    methodDto.addParameter(argsParam);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument("transform", transform);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @param %s %s
        @return current instance of builder
        """
            .formatted(
                fieldName,
                formatParam.getParameterName(),
                fieldJavadoc,
                argsParam.getParameterName(),
                fieldJavadoc));
    return methodDto;
  }
}
