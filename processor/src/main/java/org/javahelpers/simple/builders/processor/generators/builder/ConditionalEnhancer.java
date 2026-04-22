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
package org.javahelpers.simple.builders.processor.generators.builder;

import java.util.function.BooleanSupplier;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds conditional methods to generated builders.
 *
 * <p>This enhancer generates methods for conditional builder modification, allowing builder chains
 * to apply different configurations based on runtime conditions. Two overloads are provided: one
 * with true/false branches, and one with only a true branch.
 *
 * <p><b>Important behavior:</b> The condition is evaluated immediately when the method is called.
 * Based on the result, either the true action, false action, or no action is applied to the
 * builder. This enables functional programming patterns in builder chains.
 *
 * <p><b>Requirements:</b> Applies to all builders by default. These methods enable conditional
 * logic in fluent builder chains.
 *
 * <p>This enhancer is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateConditionalHelper} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.function.BooleanSupplier;
 *
 * @SimpleBuilder
 * public record BookDto(String title, String subtitle, int pages) {}
 *
 * // Usage of generated Builder:
 * boolean isExtended = true;
 * var result = BookDtoBuilder.create()
 *     .title("My Book")
 *     .conditional(() -> isExtended,
 *         b -> b.subtitle("Extended Edition").pages(500),
 *         b -> b.subtitle("Standard Edition").pages(250))
 *     .build();
 *
 * // Or with single branch:
 * var result2 = BookDtoBuilder.create()
 *     .title("My Book")
 *     .conditional(() -> isExtended, b -> b.subtitle("Extended Edition"))
 *     .build();
 * }</pre>
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
    MethodDto conditionalMethod = createConditionalMethod(builderDto);
    builderDto.addMethod(conditionalMethod);

    // Add conditional(BooleanSupplier, Consumer) method
    MethodDto conditionalPositiveMethod = createConditionalPositiveOnlyMethod(builderDto);
    builderDto.addMethod(conditionalPositiveMethod);

    context.debug(
        "Added conditional methods to builder %s", builderDto.getBuilderTypeName().getClassName());
  }

  /** Creates the conditional(BooleanSupplier, Consumer, Consumer) method. */
  private MethodDto createConditionalMethod(BuilderDefinitionDto builderDto) {
    MethodDto method = new MethodDto("conditional", builderDto.getBuilderTypeName());
    method.setOrdering(ORDERING_CONDITIONAL);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(AccessModifier.PUBLIC);

    // Add parameters
    addConditionalPositiveNegativeParameters(method, builderDto.getBuilderTypeName());

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
        new JavadocDto(
                "Conditionally applies builder modifications based on a condition evaluation.")
            .addParam("condition", "the condition to evaluate")
            .addParam("trueCase", "the consumer to apply if condition is true")
            .addParam("falseCase", "the consumer to apply if condition is false (can be null)")
            .addReturn("this builder instance"));

    return method;
  }

  /** Creates the conditional(BooleanSupplier, Consumer) method. */
  private MethodDto createConditionalPositiveOnlyMethod(BuilderDefinitionDto builderDto) {
    MethodDto method = new MethodDto("conditional", builderDto.getBuilderTypeName());
    method.setOrdering(ORDERING_CONDITIONAL_POSITIVE_ONLY);
    method.setPriority(MethodDto.PRIORITY_HIGHEST);
    method.setModifier(AccessModifier.PUBLIC);

    // Add parameters
    addConditionalPositiveOnlyParameters(method, builderDto.getBuilderTypeName());

    // Create method implementation
    method.setCode("return conditional(condition, yesCondition, null);");

    method.setJavadoc(
        new JavadocDto("Conditionally applies builder modifications if the condition is true.")
            .addParam("condition", "the condition to evaluate")
            .addParam("yesCondition", "the consumer to apply if condition is true")
            .addReturn("this builder instance"));

    return method;
  }

  /** Adds parameters for the conditional(BooleanSupplier, Consumer, Consumer) method. */
  private void addConditionalPositiveNegativeParameters(MethodDto method, TypeName builderType) {
    // BooleanSupplier condition parameter
    addParameter(method, "condition", JavaLangMapper.map2TypeName(BooleanSupplier.class));
    // Consumer<BuilderType> trueCase parameter
    addParameter(method, "trueCase", MethodGeneratorUtil.createConsumerType(builderType));
    // Consumer<BuilderType> falseCase parameter
    addParameter(method, "falseCase", MethodGeneratorUtil.createConsumerType(builderType));
  }

  /** Adds parameters for the conditional(BooleanSupplier, Consumer) method. */
  private void addConditionalPositiveOnlyParameters(MethodDto method, TypeName builderType) {
    // BooleanSupplier condition parameter
    addParameter(method, "condition", JavaLangMapper.map2TypeName(BooleanSupplier.class));
    // Consumer<BuilderType> yesCondition parameter
    addParameter(method, "yesCondition", MethodGeneratorUtil.createConsumerType(builderType));
  }

  /** Adds a parameter to the method. */
  private void addParameter(MethodDto method, String name, TypeName type) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(name);
    parameter.setParameterTypeName(type);
    method.addParameter(parameter);
  }
}
