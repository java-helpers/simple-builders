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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilderWithElementBuilders;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for Set fields with collection builder support.
 *
 * <p>This generator creates methods that accept Consumer&lt;HashSetBuilder&gt; or
 * Consumer&lt;HashSetBuilderWithElementBuilders&gt; depending on whether the element type has a
 * builder.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For Set<String> tags field (no builder for String):
 * public BookDtoBuilder tags(Consumer<HashSetBuilder<String>> tagsBuilderConsumer) {
 *   HashSetBuilder<String> builder = new HashSetBuilder<>();
 *   tagsBuilderConsumer.accept(builder);
 *   this.tags = changedValue(builder.build());
 *   return this;
 * }
 *
 * // For Set<PersonDto> authors field (PersonDto has @SimpleBuilder):
 * public BookDtoBuilder authors(Consumer<HashSetBuilderWithElementBuilders<PersonDto, PersonDtoBuilder>> authorsBuilderConsumer) {
 *   HashSetBuilderWithElementBuilders<PersonDto, PersonDtoBuilder> builder =
 *       new HashSetBuilderWithElementBuilders<>(PersonDtoBuilder::create);
 *   authorsBuilderConsumer.accept(builder);
 *   this.authors = changedValue(builder.build());
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 52 (medium - Set consumers are useful but basic setters come first)
 *
 * <p>This generator respects the configuration flag {@code shouldUseHashSetBuilder()}.
 */
public class SetConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 52;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    // Only apply to Set fields
    if (!(field.getFieldType() instanceof TypeNameSet fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return false;
    }
    // Don't apply if field itself has a builder (higher priority)
    if (field.getFieldType().getBuilderType().isPresent()) {
      return false;
    }
    return context.getConfiguration().shouldUseHashSetBuilder()
        || context.getConfiguration().shouldUseHashSetBuilderWithElementBuilders();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!(field.getFieldType() instanceof TypeNameSet fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return Collections.emptyList();
    }

    TypeName elementType = fieldTypeGeneric.getElementType();
    Optional<TypeName> elementBuilderType = fieldTypeGeneric.getElementBuilderType();

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseHashSetBuilderWithElementBuilders()) {
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(HashSetBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          createFieldConsumerWithElementBuilders(
              field, collectionBuilderType, elementBuilderType.get(), builderType, context);
      return List.of(method);
    } else if (context.getConfiguration().shouldUseHashSetBuilder()) {
      TypeName collectionBuilderType = map2TypeName(HashSetBuilder.class);
      MethodDto method =
          createFieldConsumerWithBuilder(
              field, collectionBuilderType, elementType, builderType, context);
      return List.of(method);
    }

    return Collections.emptyList();
  }

  private MethodDto createFieldConsumerWithBuilder(
      FieldDto field,
      TypeName consumerBuilderType,
      TypeName builderTargetType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric builderTypeGeneric =
        new TypeNameGeneric(consumerBuilderType, builderTargetType);
    return createFieldConsumerWithBuilder(
        field,
        builderTypeGeneric,
        "this.$fieldName:N.value()",
        "",
        Map.of(),
        returnBuilderType,
        context);
  }

  private MethodDto createFieldConsumerWithElementBuilders(
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

  private MethodDto createFieldConsumerWithBuilder(
      FieldDto field,
      TypeName consumerBuilderType,
      String constructorArgsWithValue,
      String additionalConstructorArgs,
      Map<String, TypeName> additionalArguments,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), consumerBuilderType);
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

  private String calculateBuildExpression(TypeName fieldType) {
    return wrapConcreteCollectionType(fieldType, "builder.build()");
  }

  private String wrapConcreteCollectionType(TypeName fieldType, String baseExpression) {
    if (fieldType instanceof TypeNameSet setType && setType.isConcreteImplementation()) {
      return "new " + setType.getClassName() + "<>(" + baseExpression + ")";
    }
    return baseExpression;
  }
}
