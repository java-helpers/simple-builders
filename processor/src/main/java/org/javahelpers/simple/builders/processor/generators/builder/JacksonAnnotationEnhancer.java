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
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds the @JsonPOJOBuilder annotation to generated builder classes.
 *
 * <p>This enhancer adds the Jackson {@code @JsonPOJOBuilder} annotation to enable Jackson
 * deserialization support for the generated builders. This allows Jackson to properly deserialize
 * JSON into DTO instances using the builder pattern.
 *
 * <p><b>Important behavior:</b> The {@code @JsonPOJOBuilder} annotation is added to the builder
 * class with the {@code withPrefix} parameter matching the configured setter prefix (default is
 * empty string). This tells Jackson how to map JSON properties to builder methods.
 *
 * <p><b>Requirements:</b> Only applies when Jackson deserializer annotation support is enabled and
 * the {@code com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder} class is available on the
 * classpath.
 *
 * <p>This enhancer is disabled by default and can be activated by setting the configuration flag
 * {@code usingJacksonDeserializerAnnotation} to {@code ENABLED}. For detailed usage instructions,
 * see the <a
 * href="https://github.com/andreasigel/simple-builders/blob/main/CONFIGURATION.md#jackson-support">
 * Jackson Support section in CONFIGURATION.md</a>.
 *
 * <h3>Example to demonstrate the generated annotation</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
 *
 * @SimpleBuilder
 * @JsonDeserialize(builder = BookDtoBuilder.class)
 * public record BookDto(String title, String author) {}
 *
 * // Generated builder with Jackson annotation:
 * @JsonPOJOBuilder(withPrefix = "")
 * public class BookDtoBuilder {
 *   // ... builder methods
 * }
 *
 * // Usage with Jackson:
 * ObjectMapper mapper = new ObjectMapper();
 * String json = "{\"title\":\"My Book\",\"author\":\"John Doe\"}";
 * BookDto book = mapper.readValue(json, BookDto.class);
 * }</pre>
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
