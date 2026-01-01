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

import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.InterfaceName;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds the IBuilderBase interface to generated builder classes.
 *
 * <p>This enhancer adds the {@code IBuilderBase<T>} interface to builders when enabled in the
 * configuration. This interface provides a contract for builder implementations and enables
 * polymorphic usage of builders.
 *
 * <p>The interface is parameterized with the target DTO type to ensure type safety.
 *
 * <p>Priority: 90 (high - interfaces should be added early in the generation process)
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

    context.debug(
        "Added IBuilderBase interface to builder %s",
        builderDto.getBuilderTypeName().getClassName());
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
