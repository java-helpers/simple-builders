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
import static org.javahelpers.simple.builders.processor.generators.field.MethodGeneratorUtil.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates Supplier-based methods for builder fields.
 *
 * <p>This generator creates methods that accept {@code Supplier<T>} functional interfaces for lazy
 * initialization of field values. The supplier is invoked immediately when the setter is called,
 * and the result is stored in the builder.
 *
 * <p><b>Important behavior:</b> The supplier is evaluated eagerly when the method is called, not
 * lazily when {@code build()} is invoked. This is useful for deferred initialization, dynamic value
 * generation, or passing method references.
 *
 * <p><b>Requirements:</b> Applies to all fields except functional interface types (to avoid
 * ambiguity with the field type itself being a functional interface).
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateFieldSupplier} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.function.Supplier;
 *
 * @SimpleBuilder
 * public record ExampleDto(String title, int pages) {}
 *
 * // Usage of generated Builder:
 * var result = ExampleDtoBuilder.builder()
 *     .title(() -> "Generated Title")
 *     .pages(() -> calculatePages())
 *     .build();
 * }</pre>
 */
public class SupplierMethodGenerator implements MethodGenerator {

  private static final int PRIORITY = 60;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldGenerateFieldSupplier();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    MethodDto supplierMethod =
        createFieldSupplier(
            field.getOriginalFieldName(),
            field.getFieldNameInBuilder(),
            field.getJavaDoc(),
            field.getFieldType(),
            builderType,
            context);

    return Collections.singletonList(supplierMethod);
  }

  /**
   * Creates a supplier method that accepts a {@code Supplier<T>} and invokes it to get the field
   * value.
   *
   * @param fieldName the estimated field name (used for method name)
   * @param fieldNameInBuilder the builder field name (may be renamed)
   * @param fieldJavaDoc the javadoc for the field
   * @param fieldType the type of the field
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the supplier
   */
  private MethodDto createFieldSupplier(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavaDoc,
      TypeName fieldType,
      TypeName builderType,
      ProcessingContext context) {
    TypeNameGeneric supplierType = new TypeNameGeneric(map2TypeName(Supplier.class), fieldType);
    String parameterName = fieldName + SUFFIX_SUPPLIER;

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(parameterName);
    parameter.setParameterTypeName(supplierType);

    MethodDto methodDto = new MethodDto(generateBuilderMethodName(fieldName, context), builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParam:N.get());
        return this;
        """);
    methodDto.addArgument("fieldName", fieldNameInBuilder);
    methodDto.addArgument("dtoMethodParam", parameterName);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);

    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> by invoking the provided supplier.

        @param %s supplier for %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavaDoc));

    return methodDto;
  }
}
