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

import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.annotation.InterfaceName;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds the IBuilderBase interface to generated builder classes.
 *
 * <p>This enhancer adds the {@code IBuilderBase<T>} interface to builders when enabled in the
 * configuration. This interface provides a contract for builder implementations and enables
 * polymorphic usage of builders.
 *
 * <p><b>Important behavior:</b> Makes the builder implement {@code IBuilderBase<T>} where {@code T}
 * is the target DTO type. This enables polymorphic builder usage and provides a common interface
 * for all builders.
 *
 * <p><b>Requirements:</b> Applies to all builders by default. The {@code IBuilderBase} interface
 * must be available on the classpath.
 *
 * <p>This enhancer is enabled by default and can be deactivated by setting the configuration flag
 * {@code implementsBuilderBase()} to {@code false}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated interface</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import org.javahelpers.simple.builders.core.IBuilderBase;
 *
 * @SimpleBuilder
 * public record BookDto(String title, String author) {}
 *
 * // Generated builder implementing IBuilderBase:
 * // public class BookDtoBuilder implements IBuilderBase<BookDto> { ... }
 *
 * // Usage - configure with concrete type, use polymorphically for build:
 * BookDtoBuilder builder = BookDtoBuilder.create()
 *     .title("My Book")
 *     .author("John Doe");
 *
 * // Can be passed to methods expecting IBuilderBase:
 * BookDto result = buildFromInterface(builder);
 *
 * static <T> T buildFromInterface(IBuilderBase<T> builder) {
 *     return builder.build();
 * }
 * }</pre>
 */
public class InterfaceEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 90;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldImplementBuilderBase();
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    // Add the IBuilderBase interface to the builder
    InterfaceName builderBaseInterface = createBuilderBaseInterface(builderDto);
    builderDto.addInterface(builderBaseInterface);
  }

  /**
   * Creates the IBuilderBase interface type parameterized with the DTO type.
   *
   * @param builderDto the builder definition containing the DTO type and generics
   * @return the parameterized interface type
   */
  private InterfaceName createBuilderBaseInterface(BuilderDefinitionDto builderDto) {
    InterfaceName interfaceType =
        new InterfaceName("org.javahelpers.simple.builders.core.interfaces", "IBuilderBase");

    // Use generic DTO type if the builder has generics, otherwise use base type
    TypeName parameterizedDtoType =
        MethodGeneratorUtil.createGenericTypeName(
            builderDto.getBuildingTargetTypeName(), builderDto.getGenerics());

    interfaceType.addTypeParameter(parameterizedDtoType);
    return interfaceType;
  }
}
