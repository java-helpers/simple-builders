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
package org.javahelpers.simple.builders.processor.generators.builder;

import java.util.List;
import org.javahelpers.codegen.analysis.JavaLangMapper;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocTagDto;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDtoBuilder;
import org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto;
import org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDtoBuilder;
import org.javahelpers.simple.builders.processor.processing.BuilderProcessingContext;

/**
 * Enhancer that adds the "With" interface to generated builders.
 *
 * <p>The "With" interface provides fluent modification methods for DTO instances, allowing users to
 * create modified copies of existing DTOs using the builder pattern. This is particularly useful
 * for immutable DTOs and functional programming patterns.
 *
 * <p><b>Important behavior:</b> The {@code with(Consumer)} method creates a builder initialized
 * from the current DTO instance, applies the consumer's modifications, and returns a new built DTO.
 * The {@code with()} method returns a builder initialized from the current instance for further
 * chaining. Both methods preserve immutability by creating new instances.
 *
 * <p><b>Requirements:</b> Applies to all builders when enabled. The DTO class must implement the
 * generated "With" interface.
 *
 * <p>This enhancer is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateWithInterface} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.function.Consumer;
 *
 * @SimpleBuilder
 * public record BookDto(String title, String author, int pages) implements BookDtoWith {}
 *
 * // Usage of generated With interface:
 * BookDto original = BookDtoBuilder.create()
 *     .title("Original Title")
 *     .author("John Doe")
 *     .pages(250)
 *     .build();
 *
 * // Create modified copy:
 * BookDto modified = original.with(b -> b
 *     .title("Updated Title")
 *     .pages(300));
 *
 * // Or get builder for more changes:
 * BookDto furtherModified = original.with()
 *     .title("Another Title")
 *     .build();
 * }</pre>
 */
public class WithInterfaceEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 95;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, BuilderProcessingContext context) {
    return context.getConfiguration().shouldGenerateWithInterface();
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, BuilderProcessingContext context) {
    BuilderNestedTypeDto withInterface = createWithInterface(builderDto);
    builderDto.addNestedType(withInterface);
  }

  /**
   * Creates the "With" interface for the builder.
   *
   * @param builderDto the builder definition
   * @return the nested type DTO for the With interface
   */
  private BuilderNestedTypeDto createWithInterface(BuilderDefinitionDto builderDto) {
    return BuilderNestedTypeDtoBuilder.create()
        .typeName("With")
        .kind(NestedTypeDto.NestedTypeKind.INTERFACE)
        .visibility(AccessModifier.PUBLIC)
        .javadoc(
            j ->
                j.description(
                    "Interface that can be implemented by the DTO to provide fluent modification methods."))
        // First method: DtoType with(Consumer<BuilderType> b)
        // Second method: BuilderType with()
        .methods(List.of(createWithConsumerMethod(builderDto), createWithBuilderMethod(builderDto)))
        .build();
  }

  /**
   * Creates the `DtoType with(Consumer<BuilderType> b)` method definition.
   *
   * @param builderDef the builder definition
   * @return the method definition
   */
  private BuilderMethodDto createWithConsumerMethod(BuilderDefinitionDto builderDef) {
    // Return type is the DTO type
    TypeName dtoType = builderDef.getBuildingTargetTypeName();
    // For interface methods, we store the full type as a string
    TypeNameGeneric consumerType =
        MethodGeneratorUtil.createConsumerType(builderDef.getBuilderTypeName());
    BuilderMethodDto method =
        BuilderMethodDtoBuilder.create()
            .methodName("with")
            .returnType(dtoType)
            // Parameter: Consumer<BuilderType> b
            .add2Parameters(el -> el.parameterName("b").parameterTypeName(consumerType))
            .build();

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
        JavadocDtoBuilder.create()
            .description(
                "Initializes a builder from an instance of this class, using methods of this builder to change values and returns the new built object.")
            .add2Tags(new JavadocTagDto("param", "b the consumer to apply modifications"))
            .add2Tags(new JavadocTagDto("return", "the modified instance"))
            .build());

    return method;
  }

  /**
   * Creates the `BuilderType with()` method definition.
   *
   * @param builderDef the builder definition
   * @return the method definition
   */
  private BuilderMethodDto createWithBuilderMethod(BuilderDefinitionDto builderDef) {
    // Return type is the Builder type
    BuilderMethodDto method =
        BuilderMethodDtoBuilder.create()
            .methodName("with")
            .returnType(builderDef.getBuilderTypeName())
            .build();

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
        JavadocDtoBuilder.create()
            .description("Creates a builder initialized from this instance.")
            .add2Tags(
                new JavadocTagDto("return", "a builder initialized with this instance's values"))
            .build());

    return method;
  }
}
