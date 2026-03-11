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
package org.javahelpers.simple.builders.processor.generators;

import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.processor.dtos.AnnotationDto;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.JavaLangMapper;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds the @BuilderImplementation annotation to generated builder classes.
 *
 * <p>This enhancer adds the {@code @BuilderImplementation} annotation to indicate which DTO class
 * this builder is designed to build. This helps with documentation and tooling support by clearly
 * establishing the relationship between the builder and its target class.
 *
 * <p><b>Important behavior:</b> Adds the {@code @BuilderImplementation} annotation with the target
 * DTO class as the {@code forClass} parameter. This metadata enables tools to discover which
 * builder corresponds to which DTO class.
 *
 * <p><b>Requirements:</b> Applies to all builders by default. This annotation provides essential
 * metadata about the builder-DTO relationship.
 *
 * <p>This enhancer is enabled by default and can be deactivated by setting the configuration flag
 * {@code usingBuilderImplementationAnnotation} to {@code DISABLED}. See the configuration
 * documentation for details.
 *
 * <h3>Example of generated annotation</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 *
 * @SimpleBuilder
 * public record BookDto(String title, String author) {}
 *
 * // Generated builder with @BuilderImplementation annotation:
 * // @BuilderImplementation(forClass = BookDto.class)
 * // public class BookDtoBuilder { ... }
 * }</pre>
 */
public class BuilderImplementationAnnotationEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 115;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldUseBuilderImplementationAnnotation();
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    AnnotationDto builderImplementationAnnotation =
        createBuilderImplementationAnnotation(builderDto);
    builderDto.addClassAnnotation(builderImplementationAnnotation);

    context.debug(
        "Added @BuilderImplementation annotation to builder %s",
        builderDto.getBuilderTypeName().getClassName());
  }

  /**
   * Creates the @BuilderImplementation annotation.
   *
   * @param builderDto the builder definition
   * @return the annotation DTO for @BuilderImplementation
   */
  private AnnotationDto createBuilderImplementationAnnotation(BuilderDefinitionDto builderDto) {
    AnnotationDto annotation = new AnnotationDto();
    annotation.setAnnotationType(JavaLangMapper.map2TypeName(BuilderImplementation.class));

    // Add forClass member with the target DTO class
    annotation.addMember(
        "forClass", builderDto.getBuildingTargetTypeName().getClassName() + ".class");

    return annotation;
  }
}
