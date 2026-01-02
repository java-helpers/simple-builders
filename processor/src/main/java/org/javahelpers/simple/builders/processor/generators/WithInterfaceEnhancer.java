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
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.NestedTypeDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.util.JavaLangMapper;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds the "With" interface to generated builders.
 *
 * <p>The "With" interface provides fluent modification methods for DTO instances, allowing users to
 * apply builder modifications directly to existing DTO objects. This is particularly useful for
 * functional programming patterns and method chaining.
 *
 * <p>This enhancer creates an interface with two methods:
 *
 * <ul>
 *   <li>{@code with(Consumer<BuilderType> modifier)} - applies modifications and returns the
 *       modified DTO
 *   <li>{@code with()} - creates a new builder initialized from this DTO instance
 * </ul>
 *
 * <h3>Generated Interface Example:</h3>
 *
 * <pre>
 * public interface BookDtoWith {
 *   default BookDto with(Consumer<BookDtoBuilder> modifier) {
 *     BookDtoBuilder builder = BookDtoBuilder.createFrom(this);
 *     modifier.accept(builder);
 *     return builder.build();
 *   }
 *
 *   default BookDtoBuilder with() {
 *     return BookDtoBuilder.createFrom(this);
 *   }
 * }
 *
 * // Usage example:
 * BookDto modifiedBook = originalBook.with(builder -> builder
 *     .title("Updated Title")
 *     .pages(500)
 * );
 * </pre>
 *
 * <p>Priority: 95 (critical infrastructure - should be applied early)
 *
 * <p>This enhancer respects the configuration flag {@code shouldGenerateWithInterface()}.
 */
public class WithInterfaceEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 95;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldGenerateWithInterface();
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    NestedTypeDto withInterface = createWithInterface(builderDto, context);
    builderDto.addNestedType(withInterface);

    context.debug(
        "Added With interface to builder %s", builderDto.getBuilderTypeName().getClassName());
  }

  /**
   * Creates the "With" interface for the builder.
   *
   * @param builderDto the builder definition
   * @param context the processing context
   * @return the nested type DTO for the With interface
   */
  private NestedTypeDto createWithInterface(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    context.debug(
        "Creating With interface for: %s", builderDto.getBuilderTypeName().getClassName());

    NestedTypeDto withInterface = new NestedTypeDto();
    withInterface.setTypeName("With");
    withInterface.setKind(NestedTypeDto.NestedTypeKind.INTERFACE);
    withInterface.setPublic(true);
    withInterface.setJavadoc(
        "Interface that can be implemented by the DTO to provide fluent modification methods.");

    // Create the first method: DtoType with(Consumer<BuilderType> b)
    MethodDto withConsumerMethod = createWithConsumerMethod(builderDto);
    withInterface.addMethod(withConsumerMethod);

    // Create the second method: BuilderType with()
    MethodDto withBuilderMethod = createWithBuilderMethod(builderDto);
    withInterface.addMethod(withBuilderMethod);

    return withInterface;
  }

  /**
   * Creates the `DtoType with(Consumer<BuilderType> b)` method definition.
   *
   * @param builderDef the builder definition
   * @return the method definition
   */
  private MethodDto createWithConsumerMethod(BuilderDefinitionDto builderDef) {
    MethodDto method = new MethodDto();
    method.setMethodName("with");

    // Return type is the DTO type
    TypeName dtoType = builderDef.getBuildingTargetTypeName();
    method.setReturnType(dtoType);

    // Parameter: Consumer<BuilderType> b
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName("b");
    // For interface methods, we store the full type as a string
    TypeNameGeneric consumerType =
        MethodGeneratorUtil.createConsumerType(builderDef.getBuilderTypeName());
    parameter.setParameterTypeName(consumerType);
    method.addParameter(parameter);

    // Add implementation with validation to catch wrong implementations
    method.setCode(
        """
        $builderType:T builder;
        try {
          builder = new $builderType:T($dtoType:T.class.cast(this));
        } catch ($classcastexception:T ex) {
          throw new $illegalargumentexception:T("The interface '$builderType:T.With' should only be implemented by classes, which could be casted to '$dtoType:T'", ex);
        }
        b.accept(builder);
        return builder.build();
        """);
    method.addArgument("builderType", builderDef.getBuilderTypeName());
    method.addArgument("dtoType", builderDef.getBuildingTargetTypeName());
    method.addArgument("classcastexception", JavaLangMapper.map2TypeName(ClassCastException.class));
    method.addArgument(
        "illegalargumentexception", JavaLangMapper.map2TypeName(IllegalArgumentException.class));

    method.setJavadoc(
        """
      Applies modifications to a builder initialized from this instance and returns the built object.

      @param b the consumer to apply modifications
      @return the modified instance
      """);

    return method;
  }

  /**
   * Creates the `BuilderType with()` method definition.
   *
   * @param builderDef the builder definition
   * @return the method definition
   */
  private MethodDto createWithBuilderMethod(BuilderDefinitionDto builderDef) {
    MethodDto method = new MethodDto();
    method.setMethodName("with");

    // Return type is the Builder type
    method.setReturnType(builderDef.getBuilderTypeName());

    // Add implementation with validation to catch wrong implementations
    method.setCode(
        """
        try {
          return new $builderType:T($dtoType:T.class.cast(this));
        } catch ($classcastexception:T ex) {
          throw new $illegalargumentexception:T("The interface '$builderType:T.With' should only be implemented by classes, which could be casted to '$dtoType:T'", ex);
        }
        """);
    method.addArgument("builderType", builderDef.getBuilderTypeName());
    method.addArgument("dtoType", builderDef.getBuildingTargetTypeName());
    method.addArgument("classcastexception", JavaLangMapper.map2TypeName(ClassCastException.class));
    method.addArgument(
        "illegalargumentexception", JavaLangMapper.map2TypeName(IllegalArgumentException.class));

    method.setJavadoc(
        """
      Creates a builder initialized from this instance.

      @return a builder initialized with this instance's values
      """);

    return method;
  }
}
