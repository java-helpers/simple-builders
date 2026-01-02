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

import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.javahelpers.simple.builders.core.builders.HashMapBuilder;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for Map fields with HashMapBuilder support.
 *
 * <p>This generator creates methods that accept Consumer&lt;HashMapBuilder&gt; to build map
 * instances.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For Map<String, String> metadata field:
 * public BookDtoBuilder metadata(Consumer<HashMapBuilder<String, String>> metadataBuilderConsumer) {
 *   HashMapBuilder<String, String> builder = new HashMapBuilder<>();
 *   metadataBuilderConsumer.accept(builder);
 *   this.metadata = changedValue(builder.build());
 *   return this;
 * }
 *
 * // For Map<String, Integer> ratings field:
 * public BookDtoBuilder ratings(Consumer<HashMapBuilder<String, Integer>> ratingsBuilderConsumer) {
 *   HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
 *   ratingsBuilderConsumer.accept(builder);
 *   this.ratings = changedValue(builder.build());
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 51 (medium - Map consumers are useful but basic setters come first)
 *
 * <p>This generator respects the configuration flag {@code shouldUseHashMapBuilder()}.
 */
public class MapConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 51;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    BuilderConfiguration configuration = context.getConfiguration();
    TypeName fieldType = field.getFieldType();
    return
    // Builder consumer generation must be enabled
    configuration.shouldGenerateBuilderConsumer()
        // HashMapBuilder usage must be enabled
        && configuration.shouldUseHashMapBuilder()
        // Field must be a Map type
        && fieldType instanceof TypeNameMap fieldTypeGeneric
        // Map must be parameterized (has key/value types)
        && fieldTypeGeneric.isParameterized()
        // Field shouldn't have its own builder (higher priority)
        && !fieldTypeGeneric.getBuilderType().isPresent()
        // Field shouldn't have empty constructor (higher priority)
        && !fieldTypeGeneric.hasEmptyConstructor();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!(field.getFieldType() instanceof TypeNameMap fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return Collections.emptyList();
    }

    TypeNameGeneric builderTargetTypeName =
        new TypeNameGeneric(
            map2TypeName(HashMapBuilder.class),
            fieldTypeGeneric.getKeyType(),
            fieldTypeGeneric.getValueType());
    MethodDto mapConsumerWithBuilder =
        MethodGeneratorUtil.createFieldConsumerWithBuilder(
            field,
            builderTargetTypeName,
            "this.$fieldName:N.value()",
            "",
            Map.of(),
            builderType,
            context);
    return List.of(mapConsumerWithBuilder);
  }
}
