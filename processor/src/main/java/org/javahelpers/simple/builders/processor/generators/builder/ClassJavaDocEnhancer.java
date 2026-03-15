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
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds class-level JavaDoc to generated builder classes.
 *
 * <p>This enhancer adds comprehensive JavaDoc documentation to the generated builder class,
 * including information about the target DTO class and the builder's purpose. The JavaDoc follows
 * standard conventions and provides useful information for developers using the builder.
 *
 * <p><b>Important behavior:</b> Generates class-level JavaDoc that describes the builder's purpose,
 * references the target DTO class, and provides basic usage information. This documentation appears
 * in IDE tooltips and generated API documentation.
 *
 * <p><b>Requirements:</b> Always applies to all builders. Class-level documentation is essential
 * for API usability.
 *
 * <p>This enhancer cannot be deactivated as it provides essential documentation for generated
 * builders.
 *
 * <h3>Example of generated class JavaDoc</h3>
 *
 * <p>For a DTO like:
 *
 * <pre>{@code
 * @SimpleBuilder
 * public record BookDto(String title, String author) {}
 * }</pre>
 *
 * <p>The generated builder class will have this JavaDoc:
 *
 * <blockquote>
 *
 * Builder for {@code BookDto}.
 *
 * <p>This builder provides a fluent API for creating instances of BookDto with method chaining and
 * validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the
 * final DTO.
 *
 * </blockquote>
 */
public class ClassJavaDocEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 200;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return true; // Class JavaDoc is always needed for builders
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    TypeName targetType = builderDto.getBuildingTargetTypeName();
    String javadoc = createClassJavadoc(targetType);
    builderDto.setClassJavadoc(javadoc);
  }

  /**
   * Creates comprehensive JavaDoc for the builder class.
   *
   * @param targetType the target DTO type
   * @return String containing the JavaDoc content
   */
  private String createClassJavadoc(TypeName targetType) {
    String qualifiedClassName = targetType.getFullQualifiedName();
    return String.format(
        """
        Builder for {@code %s}.
        <p>
        This builder provides a fluent API for creating instances of %s with
        method chaining and validation. Use the static {@code create()} method
        to obtain a new builder instance, configure the desired properties using
        the setter methods, and then call {@code build()} to create the final DTO.
        """,
        qualifiedClassName, qualifiedClassName);
  }
}
