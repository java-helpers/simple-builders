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
import static org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil.TRACKED_VALUE_TYPE;
import static org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil.addExampleChainFragmentTemplate;
import static org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil.generateBuilderMethodName;
import static org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil.getMethodAccessModifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.generators.util.JavadocConstants;
import org.javahelpers.simple.builders.processor.generators.util.JavadocExampleValues;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocCodeBlockDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive.PrimitiveTypeEnum;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates update helper methods for builder fields.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateUpdateHelpers} to {@code DISABLED}. Each generated method accepts a {@code
 * UnaryOperator<T>} and applies it to the field's current value. Throws {@link
 * IllegalStateException} if the value has not been set yet.
 *
 * <p>If a DTO already has a field whose setter has the same signature, the setter wins and the
 * update helper is skipped with a warning.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * var result = PersonDtoBuilder.create()
 *     .name("  bob ")
 *     .nameUpdate(String::trim)
 *     .quantity(10)
 *     .quantityUpdate(Math::abs)
 *     .build();
 * }</pre>
 */
public class UpdateHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 20;

  /**
   * Guidance appended to update methods for fields backed by a nested {@code @SimpleBuilder} DTO.
   */
  private static final String NESTED_DTO_GUIDANCE =
      "For changing multiple values of a nested DTO, prefer the corresponding builder-consumer helper.";

  /** Update operator example for numeric types. */
  private static final String MATH_ABS_EXAMPLE = "Math::abs";

  /** Update operator example for boolean types. */
  private static final String NEGATION_EXAMPLE = "value -> !value";

  /** Update operator examples for primitive field types. */
  private static final Map<PrimitiveTypeEnum, String> PRIMITIVE_UPDATE_EXAMPLES =
      Map.of(
          PrimitiveTypeEnum.INT, MATH_ABS_EXAMPLE,
          PrimitiveTypeEnum.LONG, MATH_ABS_EXAMPLE,
          PrimitiveTypeEnum.FLOAT, MATH_ABS_EXAMPLE,
          PrimitiveTypeEnum.DOUBLE, MATH_ABS_EXAMPLE,
          PrimitiveTypeEnum.BOOLEAN, NEGATION_EXAMPLE);

  /** Update operator examples for common JDK reference types, resolved via {@link TypeName#is}. */
  private static final Map<Class<?>, String> REFERENCE_UPDATE_EXAMPLES =
      Map.ofEntries(
          Map.entry(String.class, "String::trim"),
          Map.entry(Integer.class, MATH_ABS_EXAMPLE),
          Map.entry(Long.class, MATH_ABS_EXAMPLE),
          Map.entry(Float.class, MATH_ABS_EXAMPLE),
          Map.entry(Double.class, MATH_ABS_EXAMPLE),
          Map.entry(Boolean.class, NEGATION_EXAMPLE),
          Map.entry(LocalDate.class, "value -> value.plusDays(1)"),
          Map.entry(LocalTime.class, "value -> value.plusHours(1)"),
          Map.entry(LocalDateTime.class, "value -> value.plusDays(1)"),
          Map.entry(List.class, "List::copyOf"),
          Map.entry(ArrayList.class, "ArrayList::new"),
          Map.entry(LinkedList.class, "LinkedList::new"),
          Map.entry(Set.class, "Set::copyOf"),
          Map.entry(HashSet.class, "HashSet::new"),
          Map.entry(Map.class, "Map::copyOf"),
          Map.entry(HashMap.class, "HashMap::new"));

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldGenerateUpdateHelpers();
  }

  @Override
  public List<BuilderMethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    String originalFieldName = field.getOriginalFieldName();
    String parameterName = originalFieldName + "Updater";
    TypeNameGeneric updaterType =
        new TypeNameGeneric(map2TypeName(UnaryOperator.class), field.getFieldType());

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(parameterName);
    parameter.setParameterTypeName(updaterType);

    BuilderMethodDto methodDto =
        new BuilderMethodDto(
            generateBuilderMethodName(originalFieldName, context) + "Update", builderType);
    methodDto.setModifier(getMethodAccessModifier(context));
    methodDto.addParameter(parameter);
    methodDto.setCode(
        """
        if (!this.$fieldName:N.isSet()) {
          throw new $illegalStateException:T("Cannot update '$originalFieldName:N' before it is set");
        }
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($param:N.apply(this.$fieldName:N.value()));
        return this;
        """);
    methodDto.addArgument("fieldName", field.getFieldNameInBuilder());
    methodDto.addArgument("originalFieldName", originalFieldName);
    methodDto.addArgument("param", parameterName);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
    methodDto.addArgument("illegalStateException", map2TypeName(IllegalStateException.class));
    methodDto.getMethodCodeDto().addCodeBlockImport(IllegalStateException.class);
    methodDto.setPriority(BuilderMethodDto.PRIORITY_LOW);
    String description =
        """
        Updates the current value of <code>%s</code> in place by applying the given operator, instead of reading it out, changing it and setting it again.
        Useful for adjustments relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow.
        The value must have been set before (directly or via an existing instance).
        """
            .strip()
            .formatted(originalFieldName);
    if (field.getFieldType().getBuilderType().isPresent()) {
      description += "\n" + NESTED_DTO_GUIDANCE;
    }
    methodDto.setJavadoc(
        new JavadocDto(description)
            .addParam(
                parameterName,
                "operator applied to the current value; its result becomes the new value")
            .addReturn(JavadocConstants.RETURN_BUILDER_INSTANCE)
            .addThrows(
                "IllegalStateException",
                "if <code>%s</code> has not been set yet".formatted(originalFieldName)));

    addUpdateExample(methodDto, field, context);
    return Collections.singletonList(methodDto);
  }

  /**
   * Attaches Javadoc examples for the update method when both an initial value and a meaningful
   * update expression exist for the field type.
   */
  private static void addUpdateExample(
      BuilderMethodDto methodDto, FieldDto field, ProcessingContext context) {
    Optional<String> initialValue = JavadocExampleValues.getExampleValue(field.getFieldType());
    Optional<String> updateExpression = getUpdateExample(field.getFieldType());
    if (initialValue.isEmpty() || updateExpression.isEmpty()) {
      return;
    }
    JavadocCodeBlockDto methodExample = new JavadocCodeBlockDto();
    methodExample.setCodeFormat(
        "builder.%s(%s).%s(%s);"
            .formatted(
                generateBuilderMethodName(field.getOriginalFieldName(), context),
                initialValue.get(),
                methodDto.getMethodName(),
                updateExpression.get()));
    methodDto.getJavadoc().setExampleUsageCodeBlock(methodExample);
    addExampleChainFragmentTemplate(methodDto, "#{methodName}(" + updateExpression.get() + ")");
  }

  /**
   * Returns a meaningful update expression for the field type, or empty for unsupported types,
   * arrays and nested DTOs.
   */
  private static Optional<String> getUpdateExample(TypeName fieldType) {
    return resolvePrimitiveUpdateExample(fieldType)
        .or(() -> resolveReferenceUpdateExample(fieldType));
  }

  private static Optional<String> resolvePrimitiveUpdateExample(TypeName fieldType) {
    if (fieldType instanceof TypeNamePrimitive primitive) {
      return Optional.ofNullable(PRIMITIVE_UPDATE_EXAMPLES.get(primitive.getType()));
    }
    return Optional.empty();
  }

  private static Optional<String> resolveReferenceUpdateExample(TypeName fieldType) {
    return REFERENCE_UPDATE_EXAMPLES.entrySet().stream()
        .filter(entry -> fieldType.is(entry.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();
  }
}
