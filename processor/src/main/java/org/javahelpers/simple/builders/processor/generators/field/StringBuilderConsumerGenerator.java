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

package org.javahelpers.simple.builders.processor.generators.field;

import static org.javahelpers.simple.builders.processor.analysis.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.analysis.TypeNameAnalyser.*;
import static org.javahelpers.simple.builders.processor.generators.field.MethodGeneratorUtil.*;

import java.util.List;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates Consumer-based methods for String and {@code Optional<String>} fields using
 * StringBuilder.
 *
 * <p>This generator creates methods that accept a {@code Consumer<StringBuilder>} to build string
 * values. The consumer configures the StringBuilder, which is then converted to a String and
 * assigned to the field.
 *
 * <p><b>Important behavior:</b> A new {@code StringBuilder} is created, passed to the consumer for
 * configuration (appending text, formatting, etc.), then converted to a String. For {@code
 * Optional<String>} fields, the result is wrapped in {@code Optional.of()}.
 *
 * <p><b>Requirements:</b> Only applies to {@code String} or {@code Optional<String>} fields. Does
 * not apply to String arrays or if the field type has a builder or empty constructor.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateBuilderConsumer} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.Optional;
 * import java.util.function.Consumer;
 *
 * @SimpleBuilder
 * public record BookDto(String title, Optional<String> subtitle) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .title(sb -> sb.append("The ").append("Complete").append(" Guide"))
 *     .subtitle(sb -> sb.append("Volume ").append(1))
 *     .build();
 * }</pre>
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

    TypeName fieldType = field.getFieldType();
    // Only apply to String or Optional<String> fields (but not String arrays)
    return (isString(fieldType) && !(fieldType instanceof TypeNameArray))
        || isOptionalString(fieldType);
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    String transform =
        isOptionalString(field.getFieldType())
            ? "Optional.of(builder.toString())"
            : "builder.toString()";
    MethodDto method =
        createStringBuilderConsumer(
            field.getOriginalFieldName(),
            field.getFieldNameInBuilder(),
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
    TypeNameGeneric consumerType = createConsumerType(stringBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + "StringBuilderConsumer");
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto(generateBuilderMethodName(fieldName, context), builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        StringBuilder builder = new StringBuilder();
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument("fieldName", fieldNameInBuilder);
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    methodDto.addArgument("transform", transform);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
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
}
