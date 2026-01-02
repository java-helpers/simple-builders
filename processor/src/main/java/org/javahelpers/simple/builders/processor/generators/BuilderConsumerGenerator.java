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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for fields whose type has a @SimpleBuilder annotation.
 *
 * <p>This generator creates methods that accept a Consumer&lt;FieldBuilder&gt; to configure nested
 * builder instances.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For PersonDto author field (where PersonDto has @SimpleBuilder):
 * public BookDtoBuilder author(Consumer<PersonDtoBuilder> authorBuilderConsumer) {
 *   PersonDtoBuilder builder = PersonDto.create();
 *   authorBuilderConsumer.accept(builder);
 *   this.author = changedValue(builder.build());
 *   return this;
 * }
 *
 * // For MannschaftDto mannschaft field (where MannschaftDto has @SimpleBuilder):
 * public PersonDtoBuilder mannschaft(Consumer<MannschaftDtoBuilder> mannschaftBuilderConsumer) {
 *   MannschaftDtoBuilder builder = MannschaftDto.create();
 *   mannschaftBuilderConsumer.accept(builder);
 *   this.mannschaft = changedValue(builder.build());
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 55 (medium-high - builder consumers are very useful for nested objects)
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateBuilderConsumer()}.
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
