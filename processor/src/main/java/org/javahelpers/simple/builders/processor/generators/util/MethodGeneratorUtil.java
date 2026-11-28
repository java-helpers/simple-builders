/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.generators.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.codegen.analysis.JavaLangMapper;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.method.MethodParameterDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.util.JavadocExampleValues;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDtoBuilder;
import org.javahelpers.simple.builders.processor.model.type.PrefillCall;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.processing.BuilderProcessingContext;

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

  public static final TypeName TRACKED_VALUE_TYPE =
      TypeName.of(org.javahelpers.simple.builders.core.util.TrackedValue.class);

  private MethodGeneratorUtil() {
    // Utility class - prevent instantiation
  }

  /**
   * Generates the name of builder methods according to configuration and field name.
   *
   * <p>If the prefix is empty, returns the fieldName as-is. If the prefix is set, capitalizes the
   * first letter of fieldName and prepends the prefix.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>fieldName="name", prefix="" → "name"
   *   <li>fieldName="name", prefix="with" → "withName"
   *   <li>fieldName="age", prefix="set" → "setAge"
   * </ul>
   *
   * @param fieldName the field name
   * @param context the processing context containing the configuration with the method name prefix
   * @return the method name with prefix applied
   */
  public static String generateBuilderMethodName(
      String fieldName, BuilderProcessingContext context) {
    return generateBuilderMethodName(fieldName, context.getConfiguration().getSetterPrefix());
  }

  /**
   * Generates a builder method name by prepending the given setter prefix to the capitalized field
   * name — the same name derivation the context-based overload resolves from {@code setterPrefix},
   * for callers without a {@link BuilderProcessingContext} (e.g. SPI adapters).
   *
   * @param fieldName the field name
   * @param setterPrefix the configured setter prefix, or {@code null}/empty for none
   * @return the method name with prefix applied
   */
  public static String generateBuilderMethodName(String fieldName, String setterPrefix) {
    if (setterPrefix == null || setterPrefix.isEmpty()) {
      return fieldName;
    }
    return setterPrefix + StringUtils.capitalize(fieldName);
  }

  /**
   * Gets the access modifier for methods from the processing context.
   *
   * @param context the processing context
   * @return the AccessModifier for method access, or null for package-private
   */
  public static AccessModifier getMethodAccessModifier(BuilderProcessingContext context) {
    return context.getConfiguration().getMethodAccess();
  }

  /**
   * Creates a {@link BuilderMethodDto} with the method name derived from the field name and
   * configuration, and the access modifier set from the processing context.
   *
   * @param fieldName the field name to derive the method name from
   * @param returnType the return type of the method
   * @param context the processing context
   * @return a new {@link BuilderMethodDto} with name and modifier set
   */
  public static BuilderMethodDto createBuilderMethod(
      String fieldName, TypeName returnType, BuilderProcessingContext context) {
    return BuilderMethodDtoBuilder.create()
        .methodName(generateBuilderMethodName(fieldName, context))
        .returnType(returnType)
        .modifier(getMethodAccessModifier(context))
        .build();
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
   * @param field the field DTO containing all field information
   * @param transform optional transform expression (e.g., "Optional.of(%s)")
   * @param parameterType the type to use for the method parameter (may differ from
   *     field.getFieldType())
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the setter
   */
  public static BuilderMethodDto createBuilderMethodForFieldWithTransform(
      BuilderFieldDto field,
      String transform,
      TypeName parameterType,
      TypeName builderType,
      BuilderProcessingContext context) {

    MethodParameterDto parameter =
        MethodParameterDtoBuilder.create()
            .parameterName(field.getOriginalFieldName())
            .parameterTypeName(parameterType)
            .annotations(
                field.getParameterAnnotations() != null
                    ? field.getParameterAnnotations()
                    : List.of())
            .build();

    BuilderMethodDto methodDto =
        createBuilderMethod(field.getOriginalFieldName(), builderType, context);
    methodDto.addParameter(parameter);

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
    methodDto.addArgument("fieldName", field.getFieldNameInBuilder());
    methodDto.addArgument("dtoMethodParams", params);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);

    methodDto.setPriority(
        transform == null ? BuilderMethodDto.PRIORITY_HIGHEST : BuilderMethodDto.PRIORITY_HIGH);

    String fieldJavadocDesc = field.getJavaDocDescriptionOrFieldName();
    methodDto.setJavadoc(
        JavadocDtoBuilder.create()
            .description("Sets the value for <code>%s</code>.", field.getOriginalFieldName())
            .param(parameter.getParameterName(), fieldJavadocDesc)
            .returns(BuilderJavadocConstants.RETURN_BUILDER_INSTANCE)
            .build());

    return methodDto;
  }

  /**
   * Creates a field consumer method that accepts a builder for the field value.
   *
   * @param field the field DTO
   * @param fieldBuilder the resolved builder used to construct the field value, including the
   *     instantiation paths to emit
   * @param existingValueConstructorArgs constructor arguments when field already has a value
   * @param emptyConstructorArgs constructor arguments when field is not yet set
   * @param additionalTemplateArguments additional code template arguments for method generation
   * @param parentBuilderType the parent builder type that this method returns
   * @param context the processing context
   * @return the method DTO for the consumer
   */
  public static BuilderMethodDto createFieldConsumerWithBuilder(
      BuilderFieldDto field,
      ResolvedBuilder fieldBuilder,
      String existingValueConstructorArgs,
      String emptyConstructorArgs,
      Map<String, TypeName> additionalTemplateArguments,
      TypeName parentBuilderType,
      BuilderProcessingContext context) {
    TypeName fieldBuilderType = fieldBuilder.typeName();
    TypeNameGeneric consumerType = createConsumerType(fieldBuilderType);
    MethodParameterDto parameter =
        MethodParameterDtoBuilder.create()
            .parameterName(field.getFieldNameInBuilder() + BUILDER_SUFFIX + SUFFIX_CONSUMER)
            .parameterTypeName(consumerType)
            .build();
    BuilderMethodDto methodDto =
        createBuilderMethod(field.getOriginalFieldName(), parentBuilderType, context);
    methodDto.addParameter(parameter);

    String buildExpression =
        wrapConcreteCollectionType(field.getFieldType(), fieldBuilder.buildCall("builder"));

    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet()
          ? %s
          : %s;
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($buildExpression:N);
        return this;
        """
            .formatted(
                fieldBuilder
                    .funcForPrefilledBuilder()
                    .orElseThrow()
                    .instantiationCode("$helperType:T", existingValueConstructorArgs),
                fieldBuilder
                    .funcForEmptyBuilder()
                    .instantiationCode("$helperType:T", emptyConstructorArgs)));
    methodDto.addArgument("fieldName", field.getFieldNameInBuilder());
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    methodDto.addArgument("helperType", fieldBuilderType);
    methodDto.addArgument("buildExpression", buildExpression);
    additionalTemplateArguments.forEach(methodDto::addArgument);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
    methodDto.setPriority(BuilderMethodDto.PRIORITY_MEDIUM);
    String fieldJavadocDesc = field.getJavaDocDescriptionOrFieldName();
    methodDto.setJavadoc(
        JavadocDtoBuilder.create()
            .description(
                "Sets the value for <code>%s</code> using a builder consumer that produces the value.",
                field.getFieldNameInBuilder())
            .param(
                parameter.getParameterName(),
                "consumer providing an instance of a builder for %s",
                fieldJavadocDesc)
            .returns(BuilderJavadocConstants.RETURN_BUILDER_INSTANCE)
            .build());

    return methodDto;
  }

  /**
   * Creates the private prefilling method backing a {@link PrefillCall}: obtains an empty builder
   * and calls one field function per prefilled property of the value.
   *
   * @param prefillCall the resolved field-prefilling instantiation
   * @return the method DTO for the prefilling method
   */
  public static BuilderMethodDto createFieldPrefillMethod(PrefillCall prefillCall) {
    BuilderMethodDto methodDto =
        BuilderMethodDtoBuilder.create()
            .methodName(prefillCall.methodName())
            .returnType(prefillCall.generatedType())
            .modifier(AccessModifier.PRIVATE)
            .isStatic(true)
            .ordering(2100) // last: private helpers go below toString
            .add2Parameters(
                el -> el.parameterName("value").parameterTypeName(prefillCall.sourceType()))
            .build();
    StringBuilder code = new StringBuilder();
    code.append("$builderType:T builder = ")
        .append(prefillCall.funcForEmptyInstance().instantiationCode("$builderType:T", ""))
        .append(";\n");
    for (PrefillCall.Field prefilledField : prefillCall.prefilledFields()) {
      code.append("builder.")
          .append(prefillCall.fieldMethodFor(prefilledField))
          .append("(value.")
          .append(prefilledField.accessor())
          .append(");\n");
    }
    code.append("return builder;");
    methodDto.setCode(code.toString());
    methodDto.addArgument("builderType", prefillCall.generatedType());
    return methodDto;
  }

  /**
   * Creates a Consumer<BuilderType> type.
   *
   * @param builderType the builder type
   * @return a Consumer<BuilderType> type
   */
  public static TypeNameGeneric createConsumerType(TypeName builderType) {
    return new TypeNameGeneric(JavaLangMapper.map2TypeName(Consumer.class), builderType);
  }

  /**
   * Wraps an expression with a concrete collection constructor if needed.
   *
   * <p>This utility method checks if the field type is a concrete collection implementation (e.g.,
   * {@code ArrayList}, {@code HashSet}, {@code HashMap}) and wraps the base expression with the
   * appropriate constructor call. This is useful for custom generators that need to handle concrete
   * collection types.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>For {@code ArrayList<String>}: wraps {@code List.of(args)} → {@code new
   *       ArrayList<>(List.of(args))}
   *   <li>For {@code HashSet<Integer>}: wraps {@code Set.of(args)} → {@code new
   *       HashSet<>(Set.of(args))}
   *   <li>For {@code List<String>} (interface): returns {@code List.of(args)} unchanged
   * </ul>
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
   * @param elementBuilder the resolved element builder; its empty-instantiation path is passed to
   *     the collection builder as a method reference
   * @param returnBuilderType the return builder type
   * @param context the processing context
   * @return the method DTO for the consumer
   */
  public static BuilderMethodDto createFieldConsumerWithElementBuilders(
      BuilderFieldDto field,
      TypeName collectionBuilderType,
      ResolvedBuilder elementBuilder,
      TypeName returnBuilderType,
      BuilderProcessingContext context) {
    // The collection builder receives a supplier for element builders: the resolved
    // instantiation path as a method reference
    String elementSupplier =
        elementBuilder.funcForEmptyBuilder().methodReference("$elementBuilderType:T");
    return createFieldConsumerWithBuilder(
        field,
        new ResolvedBuilder(collectionBuilderType),
        "this.$fieldName:N.value(), " + elementSupplier,
        elementSupplier,
        Map.of("elementBuilderType", elementBuilder.typeName()),
        returnBuilderType,
        context);
  }

  /**
   * Creates a simple field consumer method that accepts a Consumer for the field value.
   *
   * <p>This method creates a consumer that initializes the field value if not set, accepts the
   * consumer to modify it, and stores the result.
   *
   * @param field the field DTO containing field information
   * @param fieldType the type of the field (used for instantiation)
   * @param builderType the builder type for the return type
   * @param context the processing context
   * @return the method DTO for the simple field consumer
   */
  public static BuilderMethodDto createSimpleFieldConsumer(
      BuilderFieldDto field,
      TypeName fieldType,
      TypeName builderType,
      BuilderProcessingContext context) {
    TypeNameGeneric consumerType = createConsumerType(fieldType);
    MethodParameterDto parameter =
        MethodParameterDtoBuilder.create()
            .parameterName(field.getOriginalFieldName() + SUFFIX_CONSUMER)
            .parameterTypeName(consumerType)
            .build();

    BuilderMethodDto methodDto =
        createBuilderMethod(field.getOriginalFieldName(), builderType, context);
    methodDto.addParameter(parameter);

    methodDto.setCode(
        """
        $helperType:T consumer = this.$fieldName:N.isSet()
          ? this.$fieldName:N.value()
          : new $helperType:T();
        $dtoMethodParam:N.accept(consumer);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(consumer);
        return this;
        """);
    methodDto.addArgument("fieldName", field.getFieldNameInBuilder());
    methodDto.addArgument("dtoMethodParam", parameter.getParameterName());
    methodDto.addArgument("helperType", fieldType);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
    methodDto.setPriority(BuilderMethodDto.PRIORITY_MEDIUM);

    String fieldJavadocDesc = field.getJavaDocDescriptionOrFieldName();
    methodDto.setJavadoc(
        JavadocDtoBuilder.create()
            .description(
                "Sets the value for <code>%s</code> by executing the provided consumer.",
                field.getOriginalFieldName())
            .param(
                parameter.getParameterName(),
                "consumer providing an instance of %s",
                fieldJavadocDesc)
            .returns(BuilderJavadocConstants.RETURN_BUILDER_INSTANCE)
            .build());

    return methodDto;
  }

  /**
   * Adds the fluent-chain fragment for Javadoc examples to a method.
   *
   * <p>This helper method retrieves an example value for the given field type and formats it as a
   * fluent-chain fragment (e.g., {@code methodName(exampleValue)}). The fragment is stored on the
   * BuilderMethodDto for later use by the ClassJavaDocEnhancer to synthesize both method-level and
   * class-level Javadoc examples.
   *
   * <p>If no example value is available for the field type, the fragment is not added.
   *
   * @param methodDto the method DTO to add the fragment to
   * @param fieldType the field type to get an example value for
   * @param fieldName the original field name used for String examples; blank names use a default
   * @param builderResolver resolves a type's builder for builder-backed examples
   */
  public static void addExampleChainFragment(
      BuilderMethodDto methodDto,
      TypeName fieldType,
      String fieldName,
      Function<TypeName, Optional<ResolvedBuilder>> builderResolver) {
    addExampleChainFragmentTemplate(
        methodDto, "#{methodName}(#{exampleValue})", fieldType, fieldName, builderResolver);
  }

  /**
   * Adds the fluent-chain fragment for Javadoc examples to a varargs method.
   *
   * <p>This helper method retrieves an example value for the given element type and formats it as a
   * fluent-chain fragment showing two values (e.g., {@code methodName(value1, value2)}) to make it
   * clear that the method accepts multiple arguments. The fragment is stored on the
   * BuilderMethodDto for later use by the ClassJavaDocEnhancer to synthesize both method-level and
   * class-level Javadoc examples.
   *
   * <p>If no example value is available for the element type, the fragment is not added.
   *
   * @param methodDto the method DTO to add the fragment to
   * @param elementType the element type to get an example value for
   * @param fieldName the original field name used for String examples; blank names use a default
   * @param builderResolver resolves a type's builder for builder-backed examples
   */
  public static void addExampleChainFragmentVarArgs(
      BuilderMethodDto methodDto,
      TypeName elementType,
      String fieldName,
      Function<TypeName, Optional<ResolvedBuilder>> builderResolver) {
    addExampleChainFragmentTemplate(
        methodDto,
        "#{methodName}(#{exampleValue}, #{exampleValue})",
        elementType,
        fieldName,
        builderResolver);
  }

  /**
   * Adds the fluent-chain fragment for Javadoc examples to a method using a supplier pattern.
   *
   * <p>This helper method retrieves an example value for the given field type and formats it as a
   * fluent-chain fragment with a supplier. For types with an empty constructor, a method reference
   * is used (e.g., {@code methodName(Type::new)}); otherwise a lambda is used (e.g., {@code
   * methodName(() -> exampleValue)}). The fragment is stored on the BuilderMethodDto for later use
   * by the ClassJavaDocEnhancer to synthesize both method-level and class-level Javadoc examples.
   *
   * <p>If no example value is available for the field type, the fragment is not added.
   *
   * @param methodDto the method DTO to add the fragment to
   * @param fieldType the field type to get an example value for
   * @param fieldName the original field name used for String examples; blank names use a default
   * @param builderResolver resolves a type's builder for builder-backed examples
   */
  public static void addExampleChainFragmentWithSupplier(
      BuilderMethodDto methodDto,
      TypeName fieldType,
      String fieldName,
      Function<TypeName, Optional<ResolvedBuilder>> builderResolver) {
    if (fieldType.hasEmptyConstructor()) {
      addExampleChainFragmentTemplate(
          methodDto, "#{methodName}(" + fieldType.getClassName() + "::new)");
    } else {
      addExampleChainFragmentTemplate(
          methodDto, "#{methodName}(() -> #{exampleValue})", fieldType, fieldName, builderResolver);
    }
  }

  /**
   * Returns an example resolver that yields a builder-backed snippet ({@code
   * <Builder>.create().build()}) for types with a resolved builder.
   *
   * @param builderResolver resolves a type to its builder, e.g. {@code context::resolveBuilderType}
   * @return a resolver yielding the builder-backed example snippet, or empty when no builder exists
   */
  public static Function<TypeName, Optional<String>> builderBackedExampleResolver(
      Function<TypeName, Optional<ResolvedBuilder>> builderResolver) {
    return t ->
        builderResolver
            .apply(t)
            .map(
                resolvedBuilder -> resolvedBuilder.typeName().getClassName() + ".create().build()");
  }

  /**
   * Adds the fluent-chain fragment for Javadoc examples to a method using a template string.
   *
   * <p>This helper method replaces placeholders in the template string with actual values:
   *
   * <ul>
   *   <li>{@code #{methodName}} - replaced with the method name
   *   <li>{@code #{exampleValue}} - replaced with an example value for the given field type (if
   *       fieldType is provided)
   * </ul>
   *
   * <p>The fragment is stored on the BuilderMethodDto for later use by the ClassJavaDocEnhancer to
   * synthesize both method-level and class-level Javadoc examples.
   *
   * @param methodDto the method DTO to add the fragment to
   * @param template the template string with placeholders (e.g., {@code
   *     #{methodName}(#{exampleValue})})
   * @param fieldType the field type to get an example value for (can be {@code null})
   * @param fieldName the original field name used for String examples; blank names use a default
   * @param builderResolver resolves a type's builder for builder-backed examples
   */
  public static void addExampleChainFragmentTemplate(
      BuilderMethodDto methodDto,
      String template,
      TypeName fieldType,
      String fieldName,
      Function<TypeName, Optional<ResolvedBuilder>> builderResolver) {
    String fragment = template.replace("#{methodName}", methodDto.getMethodName());

    if (fieldType != null) {
      Optional<String> exampleValue =
          JavadocExampleValues.getExampleValue(
              fieldType, fieldName, builderBackedExampleResolver(builderResolver));
      if (exampleValue.isEmpty()) {
        return;
      }
      fragment = fragment.replace("#{exampleValue}", exampleValue.get());
    }

    methodDto.setExampleChainFragment(fragment);
  }

  /**
   * Adds the fluent-chain fragment for Javadoc examples to a method using a template string.
   *
   * <p>This helper method replaces placeholders in the template string with actual values:
   *
   * <ul>
   *   <li>{@code #{methodName}} - replaced with the method name
   * </ul>
   *
   * <p>The fragment is stored on the BuilderMethodDto for later use by the ClassJavaDocEnhancer to
   * synthesize both method-level and class-level Javadoc examples.
   *
   * @param methodDto the method DTO to add the fragment to
   * @param template the template string with placeholders (e.g., {@code #{methodName}(sb ->
   *     sb.append("text"))})
   */
  public static void addExampleChainFragmentTemplate(BuilderMethodDto methodDto, String template) {
    String fragment = template.replace("#{methodName}", methodDto.getMethodName());
    methodDto.setExampleChainFragment(fragment);
  }
}
