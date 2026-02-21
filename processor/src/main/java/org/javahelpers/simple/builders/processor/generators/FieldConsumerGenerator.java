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

import static org.javahelpers.simple.builders.processor.generators.MethodGeneratorUtil.*;

import java.util.Collections;
import java.util.List;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for fields with concrete classes that have empty constructors.
 *
 * <p>This generator creates methods that accept a {@code Consumer<FieldType>} to configure field
 * instances created via their no-arg constructor.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For PersonDto publisher field:
 * public BookDtoBuilder publisher(Consumer<PersonDto> publisherConsumer) {
 *   PersonDto publisher = new PersonDto();
 *   publisherConsumer.accept(publisher);
 *   this.publisher = changedValue(publisher);
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 54 (medium - Consumer methods are useful but basic setters come first)
 *
 * <p>This generator applies to fields with types that have empty constructors and respects the
 * configuration flag {@code shouldGenerateFieldConsumer()}.
 */
public class FieldConsumerGenerator implements MethodGenerator {

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
