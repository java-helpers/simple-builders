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

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.JavaLangMapper;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds conditional methods to generated builders.
 *
 * <p>This enhancer generates methods for conditional builder modification:
 *
 * <ul>
 *   <li>{@code conditional(BooleanSupplier, Consumer, Consumer)} - applies different logic based on
 *       condition
 *   <li>{@code conditional(BooleanSupplier, Consumer)} - applies logic only when condition is true
 * </ul>
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // Conditional method with true/false branches:
 * public BookDtoBuilder conditional(BooleanSupplier condition,
 *                                   Consumer<BookDtoBuilder> trueAction,
 *                                   Consumer<BookDtoBuilder> falseAction) {
 *   if (condition.getAsBoolean()) {
 *     trueAction.accept(this);
 *   } else {
 *     falseAction.accept(this);
 *   }
 *   return this;
 * }
 *
 * // Conditional method with only true branch:
 * public BookDtoBuilder conditional(BooleanSupplier condition, Consumer<BookDtoBuilder> action) {
 *   if (condition.getAsBoolean()) {
 *     action.accept(this);
 *   }
 *   return this;
 * }
 *
 * // Usage example:
 * BookDto book = BookDto.create()
 *     .title("Default Title")
 *     .conditional(() -> pages > 100,
 *         builder -> builder.subtitle("Extended Edition"),
 *         builder -> builder.subtitle("Standard Edition"))
 *     .build();
 * </pre>
 *
 * <p>These methods enable functional programming patterns where builder modifications can be
 * applied conditionally based on runtime evaluations.
 *
 * <p>Priority: 80 (high - should be applied early but after core methods)
 */
public class ConditionalEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 80;

  // Ordering constants for method generation order
  private static final int ORDERING_CONDITIONAL =
      1100; // After builder methods (1000), before toString (2000)
  private static final int ORDERING_CONDITIONAL_POSITIVE_ONLY =
      1100; // After builder methods (1000), before toString (2000)

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldGenerateConditionalLogic();
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    // Add conditional(BooleanSupplier, Consumer, Consumer) method
    MethodDto conditionalMethod = createConditionalMethod(builderDto, context);
    builderDto.addCoreMethod(conditionalMethod);

    // Add conditional(BooleanSupplier, Consumer) method
    MethodDto conditionalPositiveMethod = createConditionalPositiveOnlyMethod(builderDto, context);
    builderDto.addCoreMethod(conditionalPositiveMethod);

    context.debug(
        "Added conditional methods to builder %s", builderDto.getBuilderTypeName().getClassName());
  }

  /** Creates the conditional(BooleanSupplier, Consumer, Consumer) method. */
  private MethodDto createConditionalMethod(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    MethodDto method = new MethodDto();
    method.setMethodName("conditional");
    method.setReturnType(builderDto.getBuilderTypeName());
    method.setOrdering(ORDERING_CONDITIONAL);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);

    // Add parameters
    addConditionalParameters(method, builderDto.getBuilderTypeName());

    // Create method implementation
    method.setCode(
        """
        if (condition.getAsBoolean()) {
            trueCase.accept(this);
        } else if (falseCase != null) {
            falseCase.accept(this);
        }
        return this;
        """);

    method.setJavadoc(
        """
        Conditionally applies builder modifications based on a condition evaluation.

        @param condition the condition to evaluate
        @param trueCase the consumer to apply if condition is true
        @param falseCase the consumer to apply if condition is false (can be null)
        @return this builder instance
        """);

    return method;
  }

  /** Creates the conditional(BooleanSupplier, Consumer) method. */
  private MethodDto createConditionalPositiveOnlyMethod(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    MethodDto method = new MethodDto();
    method.setMethodName("conditional");
    method.setReturnType(builderDto.getBuilderTypeName());
    method.setOrdering(ORDERING_CONDITIONAL_POSITIVE_ONLY);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(Modifier.PUBLIC);

    // Add parameters
    addConditionalPositiveOnlyParameters(method, builderDto.getBuilderTypeName());

    // Create method implementation
    method.setCode("return conditional(condition, yesCondition, null);");

    method.setJavadoc(
        """
        Conditionally applies builder modifications if the condition is true.

        @param condition the condition to evaluate
        @param yesCondition the consumer to apply if condition is true
        @return this builder instance
        """);

    return method;
  }

  /** Adds parameters for the conditional(BooleanSupplier, Consumer, Consumer) method. */
  private void addConditionalParameters(MethodDto method, TypeName builderType) {
    // BooleanSupplier condition parameter
    addParameter(method, "condition", JavaLangMapper.map2TypeName(BooleanSupplier.class));
    // Consumer<BuilderType> trueCase parameter
    addParameter(method, "trueCase", createConsumerType(builderType));
    // Consumer<BuilderType> falseCase parameter
    addParameter(method, "falseCase", createConsumerType(builderType));
  }

  /** Adds parameters for the conditional(BooleanSupplier, Consumer) method. */
  private void addConditionalPositiveOnlyParameters(MethodDto method, TypeName builderType) {
    // BooleanSupplier condition parameter
    addParameter(method, "condition", JavaLangMapper.map2TypeName(BooleanSupplier.class));
    // Consumer<BuilderType> yesCondition parameter
    addParameter(method, "yesCondition", createConsumerType(builderType));
  }

  /** Adds a parameter to the method. */
  private void addParameter(MethodDto method, String name, TypeName type) {
    org.javahelpers.simple.builders.processor.dtos.MethodParameterDto parameter =
        new org.javahelpers.simple.builders.processor.dtos.MethodParameterDto();
    parameter.setParameterName(name);
    parameter.setParameterTypeName(type);
    method.addParameter(parameter);
  }

  /** Creates a Consumer<BuilderType> type. */
  private TypeName createConsumerType(TypeName builderType) {
    org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric consumerType =
        new org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric(
            JavaLangMapper.map2TypeName(Consumer.class), builderType);
    return consumerType;
  }
}
