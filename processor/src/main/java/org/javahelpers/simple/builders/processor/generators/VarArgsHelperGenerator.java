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
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates varargs helper methods for collection fields.
 *
 * <p>This generator creates convenience methods that accept varargs parameters for List, Set, and
 * Map fields, making it easier to set collection values without explicitly creating collection
 * instances.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>For {@code List<String>}: {@code names(String... names)}
 *   <li>For {@code Set<Integer>}: {@code ids(Integer... ids)}
 *   <li>For {@code Map<K,V>}: {@code entries(Map.Entry<K,V>... entries)}
 * </ul>
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateVarArgsHelpers()}.
 */
public class VarArgsHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 40;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateVarArgsHelpers()) {
      return false;
    }
    return (field.getFieldType() instanceof TypeNameList listType && listType.isParameterized())
        || (field.getFieldType() instanceof TypeNameSet setType && setType.isParameterized())
        || (field.getFieldType() instanceof TypeNameMap mapType && mapType.isParameterized());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    TypeName fieldType = field.getFieldType();
    TypeName parameterType = null;

    if (fieldType instanceof TypeNameList listType) {
      parameterType = new TypeNameArray(listType.getElementType());
    } else if (fieldType instanceof TypeNameSet setType) {
      parameterType = new TypeNameArray(setType.getElementType());
    } else if (fieldType instanceof TypeNameMap mapType) {
      parameterType =
          new TypeNameArray(
              new TypeNameGeneric(
                  "java.util.Map", "Entry", mapType.getKeyType(), mapType.getValueType()));
    }

    if (parameterType == null) {
      return Collections.emptyList();
    }

    MethodDto varArgsMethod =
        createFieldSetterByVarArgs(field, parameterType, builderType, context);
    return Collections.singletonList(varArgsMethod);
  }

  /**
   * Creates a field setter method for collection varargs with automatic transform calculation. The
   * transform is calculated based on the original field type to preserve specific collection
   * implementations (e.g., ArrayList, LinkedList, HashSet, TreeSet, HashMap, TreeMap).
   *
   * @param field the field definition containing name, type, and javadoc
   * @param parameterType the type of the method parameter (varargs array type)
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the setter
   */
  private MethodDto createFieldSetterByVarArgs(
      FieldDto field, TypeName parameterType, TypeName builderType, ProcessingContext context) {
    String baseExpression;
    TypeName fieldType = field.getFieldType();

    if (fieldType instanceof TypeNameList listType) {
      baseExpression =
          listType.isConcreteImplementation() ? "java.util.List.of(%s)" : "List.of(%s)";
    } else if (fieldType instanceof TypeNameSet setType) {
      baseExpression = setType.isConcreteImplementation() ? "java.util.Set.of(%s)" : "Set.of(%s)";
    } else if (fieldType instanceof TypeNameMap mapType) {
      baseExpression =
          mapType.isConcreteImplementation() ? "java.util.Map.ofEntries(%s)" : "Map.ofEntries(%s)";
    } else {
      return null;
    }
    String transform = wrapConcreteCollectionType(fieldType, baseExpression);

    return createFieldSetterWithTransform(
        field.getFieldNameEstimated(),
        field.getFieldName(),
        field.getJavaDoc(),
        transform,
        parameterType,
        List.of(),
        builderType,
        context);
  }

  /**
   * Creates a field setter method with optional transform and annotations.
   *
   * @param fieldName the name of the method (estimated field name)
   * @param fieldNameInBuilder the name of the builder field (may be renamed)
   * @param fieldJavadoc the javadoc for the field
   * @param transform optional transform expression (e.g., "Optional.of(%s)")
   * @param fieldType the type of the field
   * @param annotations annotations to apply to the parameter
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the setter
   */
  private MethodDto createFieldSetterWithTransform(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      String transform,
      TypeName fieldType,
      List<AnnotationDto> annotations,
      TypeName builderType,
      ProcessingContext context) {

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(fieldType);

    if (annotations != null) {
      annotations.forEach(parameter::addAnnotation);
    }

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(fieldName, context));
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
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAMS, params);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);

    methodDto.setPriority(MethodDto.PRIORITY_HIGH);

    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavadoc));

    return methodDto;
  }

  /**
   * Wraps an expression with a concrete collection constructor if needed to preserve the specific
   * collection type. Only wraps concrete implementations (ArrayList, LinkedList, HashSet, TreeSet,
   * HashMap, TreeMap, etc.). Returns the base expression unchanged for interface types.
   *
   * @param fieldType the field type to check
   * @param baseExpression the base expression to potentially wrap
   * @return the wrapped expression for concrete collections, or base expression otherwise
   */
  private String wrapConcreteCollectionType(TypeName fieldType, String baseExpression) {
    if (fieldType instanceof TypeNameList listType && listType.isConcreteImplementation()) {
      return "new " + listType.getClassName() + "<>(" + baseExpression + ")";
    } else if (fieldType instanceof TypeNameSet setType && setType.isConcreteImplementation()) {
      return "new " + setType.getClassName() + "<>(" + baseExpression + ")";
    } else if (fieldType instanceof TypeNameMap mapType && mapType.isConcreteImplementation()) {
      return "new " + mapType.getClassName() + "<>(" + baseExpression + ")";
    }
    return baseExpression;
  }
}
