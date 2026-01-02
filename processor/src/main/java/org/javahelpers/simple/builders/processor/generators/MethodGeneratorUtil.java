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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.JavaLangMapper;
import org.javahelpers.simple.builders.processor.util.JavapoetMapper;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Utility class providing common functionality for method generators.
 *
 * <p>This class contains shared constants and helper methods used across multiple generator
 * implementations.
 */
public final class MethodGeneratorUtil {

  // Constants for method parameter suffixes
  public static final String SUFFIX_CONSUMER = "Consumer";
  public static final String SUFFIX_SUPPLIER = "Supplier";
  public static final String BUILDER_SUFFIX = "Builder";

  // Constants for code template arguments
  public static final String ARG_FIELD_NAME = "fieldName";
  public static final String ARG_DTO_METHOD_PARAM = "dtoMethodParam";
  public static final String ARG_DTO_METHOD_PARAMS = "dtoMethodParams";
  public static final String ARG_BUILDER_FIELD_WRAPPER = "builderFieldWrapper";
  public static final String ARG_HELPER_TYPE = "helperType";
  public static final String ARG_ELEMENT_TYPE = "elementType";

  public static final TypeName TRACKED_VALUE_TYPE =
      TypeName.of(org.javahelpers.simple.builders.core.util.TrackedValue.class);

  private MethodGeneratorUtil() {
    // Utility class - prevent instantiation
  }

  /**
   * Generates the name of setters on the builder according to configuration and field name.
   *
   * <p>If the suffix is empty, returns the fieldName as-is. If the suffix is set, capitalizes the
   * first letter of fieldName and prepends the suffix.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>fieldName="name", suffix="" → "name"
   *   <li>fieldName="name", suffix="with" → "withName"
   *   <li>fieldName="age", suffix="set" → "setAge"
   * </ul>
   *
   * @param fieldName the field name
   * @param context the processing context containing the configuration with the suffix
   * @return the method name with suffix applied
   */
  public static String generateBuilderMethodName(String fieldName, ProcessingContext context) {
    String suffix = context.getConfiguration().getSetterSuffix();
    if (suffix == null || suffix.isEmpty()) {
      return fieldName;
    }
    return suffix + StringUtils.capitalize(fieldName);
  }

  /**
   * Gets the method access modifier from the builder configuration.
   *
   * @param context the processing context
   * @return the Modifier for method access, or null for package-private
   */
  public static Modifier getMethodAccessModifier(ProcessingContext context) {
    return JavapoetMapper.map2Modifier(context.getConfiguration().getMethodAccess());
  }

  /**
   * Sets the access modifier on a MethodDto if the modifier is not null.
   *
   * @param method the MethodDto to update
   * @param modifier the access modifier to set, or null for package-private
   */
  public static void setMethodAccessModifier(MethodDto method, Modifier modifier) {
    if (modifier != null) {
      method.setModifier(modifier);
    }
  }

  /**
   * Creates a generic TypeName from a base type and generic parameters.
   *
   * <p>If the generic parameters list is empty, returns the base type as-is. Otherwise creates a
   * TypeNameGeneric with the base type and generic type variables.
   *
   * @param baseType the base type name
   * @param genericParameters the list of generic parameter DTOs
   * @return the generic type name, or the base type if no generics
   */
  public static TypeName createGenericTypeName(
      TypeName baseType, List<GenericParameterDto> genericParameters) {
    if (genericParameters.isEmpty()) {
      return baseType;
    }

    List<TypeName> typeVariables =
        genericParameters.stream()
            .map(GenericParameterDto::getName)
            .map(TypeNameVariable::new)
            .map(TypeName.class::cast)
            .toList();

    return new TypeNameGeneric(baseType, typeVariables);
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
  public static MethodDto createFieldSetterWithTransform(
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

    methodDto.setPriority(transform == null ? MethodDto.PRIORITY_HIGHEST : MethodDto.PRIORITY_HIGH);

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
   * Creates a field consumer method that accepts a builder for the field value.
   *
   * @param field the field DTO
   * @param consumerBuilderType the builder type for the consumer
   * @param constructorArgsWithValue constructor arguments with field value
   * @param additionalConstructorArgs additional constructor arguments
   * @param additionalArguments additional method arguments
   * @param returnBuilderType the return builder type
   * @param context the processing context
   * @return the method DTO for the consumer
   */
  public static MethodDto createFieldConsumerWithBuilder(
      FieldDto field,
      TypeName consumerBuilderType,
      String constructorArgsWithValue,
      String additionalConstructorArgs,
      Map<String, TypeName> additionalArguments,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric consumerType =
        new TypeNameGeneric(JavaLangMapper.map2TypeName(Consumer.class), consumerBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(field.getFieldName() + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateBuilderMethodName(field.getFieldName(), context));
    methodDto.setReturnType(returnBuilderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    String buildExpression = calculateBuildExpression(field.getFieldType());

    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet() ? new $helperType:T(%s) : new $helperType:T(%s);
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($buildExpression:N);
        return this;
        """
            .formatted(constructorArgsWithValue, additionalConstructorArgs));
    methodDto.addArgument(ARG_FIELD_NAME, field.getFieldName());
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, consumerBuilderType);
    methodDto.addArgument("buildExpression", buildExpression);
    additionalArguments.forEach(methodDto::addArgument);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> using a builder consumer that produces the value.

        @param %s consumer providing an instance of a builder for %s
        @return current instance of builder
        """
            .formatted(field.getFieldName(), parameter.getParameterName(), field.getJavaDoc()));
    return methodDto;
  }

  /**
   * Calculates the build expression for a field type, wrapping concrete collections if needed.
   *
   * @param fieldType the field type
   * @return the build expression
   */
  private static String calculateBuildExpression(TypeName fieldType) {
    return wrapConcreteCollectionType(fieldType, "builder.build()");
  }

  /**
   * Wraps an expression with a concrete collection constructor if needed.
   *
   * @param fieldType the field type to check
   * @param baseExpression the base expression to potentially wrap
   * @return the wrapped expression for concrete collections, or base expression otherwise
   */
  public static String wrapConcreteCollectionType(TypeName fieldType, String baseExpression) {
    if (fieldType instanceof TypeNameList listType && listType.isConcreteImplementation()) {
      return "new " + listType.getClassName() + "<>(" + baseExpression + ")";
    } else if (fieldType instanceof TypeNameSet setType && setType.isConcreteImplementation()) {
      return "new " + setType.getClassName() + "<>(" + baseExpression + ")";
    } else if (fieldType instanceof TypeNameMap mapType && mapType.isConcreteImplementation()) {
      return "new " + mapType.getClassName() + "<>(" + baseExpression + ")";
    }
    return baseExpression;
  }

  /**
   * Creates a field consumer method that accepts a builder for collections with element builders.
   *
   * @param field the field DTO
   * @param collectionBuilderType the collection builder type
   * @param elementBuilderType the element builder type
   * @param returnBuilderType the return builder type
   * @param context the processing context
   * @return the method DTO for the consumer
   */
  public static MethodDto createFieldConsumerWithElementBuilders(
      FieldDto field,
      TypeName collectionBuilderType,
      TypeName elementBuilderType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    return createFieldConsumerWithBuilder(
        field,
        collectionBuilderType,
        "this.$fieldName:N.value(), $elementBuilderType:T::create",
        "$elementBuilderType:T::create",
        Map.of("elementBuilderType", elementBuilderType),
        returnBuilderType,
        context);
  }
}
