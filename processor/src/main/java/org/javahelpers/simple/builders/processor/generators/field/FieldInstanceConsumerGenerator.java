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

package org.javahelpers.simple.builders.processor.generators.field;

import static org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil.*;

import java.util.Collections;
import java.util.List;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameList;
import org.javahelpers.simple.builders.processor.model.type.TypeNameMap;
import org.javahelpers.simple.builders.processor.model.type.TypeNameSet;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates Consumer-based methods for fields with concrete classes that have empty constructors.
 *
 * <p>This generator creates methods that accept a {@code Consumer<FieldType>} to configure field
 * instances. The field type is instantiated using its no-arg constructor, then passed to the
 * consumer for configuration.
 *
 * <p><b>Important behavior:</b> A new instance of the field type is created using its empty
 * constructor, then the consumer is invoked to configure it. This allows fluent configuration of
 * complex objects without manually creating them first.
 *
 * <p><b>Requirements:</b> Only applies to fields whose type has an accessible no-arg constructor.
 * Does not apply if the field type has a builder (higher priority) or if it's a standard collection
 * type with a specific consumer generator enabled.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateFieldConsumer} to {@code DISABLED}. See the configuration documentation for
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
 * public record BookDto(String title, PublisherDto publisher) {}
 *
 * // Needs to be a class, otherwise it would not have a NoArg-Constructor
 * public class PublisherDto {
 *   private String name;
 *   public PublisherDto() {}
 *   public void changeName(String name) { this.name = name; }
 * }
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .title("My Book")
 *     .publisher(p -> p.changeName("Publisher Inc."))
 *     .build();
 * }</pre>
 */
public class FieldInstanceConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 54;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateFieldConsumer()) {
      return false;
    }

    // Only apply if no builder consumer applies (builder has higher priority)
    if (field.getFieldType().getBuilderType().isPresent()) {
      return false;
    }

    // Don't apply to standard collection types when their specific consumer generators are enabled
    if (hasSpecificCollectionConsumer(field, context)) {
      return false;
    }

    return field.getFieldType().hasEmptyConstructor();
  }

  /**
   * Checks if a specific collection consumer generator is enabled for this field type.
   *
   * @param field the field to check
   * @param context the processing context
   * @return true if a specific consumer (List, Set, or Map) should be generated instead
   */
  private boolean hasSpecificCollectionConsumer(FieldDto field, ProcessingContext context) {
    TypeName fieldType = field.getFieldType();

    return (fieldType instanceof TypeNameList
            && context.getConfiguration().shouldUseArrayListBuilder())
        || (fieldType instanceof TypeNameSet
            && context.getConfiguration().shouldUseHashSetBuilder())
        || (fieldType instanceof TypeNameMap
            && context.getConfiguration().shouldUseHashMapBuilder());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!field.getFieldType().hasEmptyConstructor()) {
      return Collections.emptyList();
    }

    MethodDto method = createSimpleFieldConsumer(field, field.getFieldType(), builderType, context);
    return Collections.singletonList(method);
  }
}
