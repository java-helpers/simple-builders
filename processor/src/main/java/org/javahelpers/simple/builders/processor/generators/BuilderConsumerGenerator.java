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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for fields whose type has a @SimpleBuilder annotation.
 *
 * <p>This generator creates methods that accept a {@code Consumer<FieldBuilder>} to configure
 * nested builder instances. The field's builder is created, passed to the consumer for
 * configuration, then built and assigned to the field.
 *
 * <p><b>Important behavior:</b> A new builder instance is created for the field type, the consumer
 * configures it, and then {@code build()} is called automatically. This enables fluent nested
 * object construction without manually creating and building the nested builder.
 *
 * <p><b>Requirements:</b> Only applies to fields whose type is annotated with
 * {@code @SimpleBuilder}. The field type must have a generated builder with a static factory
 * method.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateBuilderConsumer} to {@code DISABLED}. See the configuration documentation for
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
 * public record BookDto(String title, AuthorDto author) {}
 *
 * @SimpleBuilder
 * public record AuthorDto(String name, String email) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .title("My Book")
 *     .author(a -> a
 *         .name("John Doe")
 *         .email("john@example.com"))
 *     .build();
 * }</pre>
 */
public class BuilderConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 55;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    return field.getFieldType().getBuilderType().isPresent();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    Optional<TypeName> fieldBuilderOpt = field.getFieldType().getBuilderType();
    if (fieldBuilderOpt.isEmpty()) {
      return Collections.emptyList();
    }

    TypeName fieldBuilderType = fieldBuilderOpt.get();
    MethodDto method =
        MethodGeneratorUtil.createFieldConsumerWithBuilder(
            field,
            fieldBuilderType,
            "this.$fieldName:N.value()",
            "",
            Map.of(),
            builderType,
            context);
    return List.of(method);
  }
}
