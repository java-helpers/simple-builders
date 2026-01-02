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
import java.util.function.Supplier;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Supplier-based methods for builder fields.
 *
 * <p>This generator creates methods that accept Supplier&lt;T&gt; functional interfaces for lazy
 * initialization of field values. The supplier is invoked when the setter is called, and the result
 * is stored in the builder.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * public BookDtoBuilder title(Supplier<String> titleSupplier) {
 *   this.title = changedValue(titleSupplier.get());
 *   return this;
 * }
 *
 * public BookDtoBuilder pages(Supplier<Integer> pagesSupplier) {
 *   this.pages = changedValue(pagesSupplier.get());
 *   return this;
 * }
 * </pre>
 *
 * <p>Supplier methods are useful for:
 *
 * <ul>
 *   <li>Lazy computation of values
 *   <li>Deferred initialization
 *   <li>Dynamic value generation
 * </ul>
 *
 * <p>This generator applies to all fields except functional interfaces and respects the
 * configuration flag {@code shouldGenerateFieldSupplier()}.
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
            field.getFieldNameEstimated(),
            field.getFieldName(),
            field.getJavaDoc(),
            field.getFieldType(),
            builderType,
            context);

    return Collections.singletonList(supplierMethod);
  }

  /**
   * Creates a supplier method that accepts a Supplier&lt;T&gt; and invokes it to get the field
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
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_SUPPLIER);
    parameter.setParameterTypeName(supplierType);

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParam:N.get());
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
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
