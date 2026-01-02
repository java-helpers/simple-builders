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

import org.javahelpers.simple.builders.processor.dtos.AnnotationDto;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds the @JsonPOJOBuilder annotation to generated builder classes.
 *
 * <p>This enhancer adds the Jackson {@code @JsonPOJOBuilder} annotation to enable Jackson
 * deserialization support for the generated builders. This allows Jackson to properly deserialize
 * JSON into DTO instances using the builder pattern.
 *
 * <p>The annotation includes the {@code withPrefix} parameter to specify the setter prefix used by
 * the builder (typically "set" or a custom prefix).
 *
 * <h3>Generated Annotation Example:</h3>
 *
 * <pre>
 * @JsonPOJOBuilder(withPrefix = "")
 * public class BookDtoBuilder {
 *   // ... builder implementation
 * }
 *
 * @JsonPOJOBuilder(withPrefix = "set")
 * public class PersonDtoBuilder {
 *   // ... builder implementation
 * }
 * </pre>
 *
 * <p>This enhancer only applies when:
 *
 * <ul>
 *   <li>Jackson deserializer annotation support is enabled in configuration
 *   <li>The {@code com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder} class is available on
 *       the classpath
 * </ul>
 *
 * <p>Priority: 110 (very high - annotations should be added early)
 *
 * <p>This enhancer respects the configuration flag {@code usingJacksonDeserializerAnnotation()}.
 */
public class JacksonAnnotationEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 110;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return context.getConfiguration().shouldUseJacksonDeserializerAnnotation()
        && isJacksonAvailable(context);
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    AnnotationDto jacksonAnnotation = createJsonPOJOBuilderAnnotation(builderDto);
    builderDto.addClassAnnotation(jacksonAnnotation);

    context.debug(
        "Added @JsonPOJOBuilder annotation to builder %s",
        builderDto.getBuilderTypeName().getClassName());
  }

  /**
   * Checks if Jackson is available on the classpath.
   *
   * @param context the processing context
   * @return true if Jackson is available, false otherwise
   */
  private boolean isJacksonAvailable(ProcessingContext context) {
    return context.getTypeElement("com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder")
        != null;
  }

  /**
   * Creates the @JsonPOJOBuilder annotation.
   *
   * @param builderDto the builder definition
   * @return the annotation DTO for @JsonPOJOBuilder
   */
  private AnnotationDto createJsonPOJOBuilderAnnotation(BuilderDefinitionDto builderDto) {
    AnnotationDto annotation = new AnnotationDto();
    annotation.setAnnotationType(
        new TypeName("com.fasterxml.jackson.databind.annotation", "JsonPOJOBuilder"));

    // Add withPrefix member with the setter prefix from configuration
    String setterPrefix = builderDto.getConfiguration().getSetterSuffix();
    if (setterPrefix != null) {
      annotation.addMember("withPrefix", "\"" + setterPrefix + "\"");
    } else {
      annotation.addMember("withPrefix", "\"\"");
    }

    return annotation;
  }
}
