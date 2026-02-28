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
 * Generates Consumer-based methods for Map fields with map builder support.
 *
 * <p>This generator creates methods that accept {@code Consumer<HashMapBuilder<K, V>>} to build map
 * instances. The consumer configures the map builder by adding key-value pairs, which is then built
 * and assigned to the field.
 *
 * <p><b>Important behavior:</b> A {@code HashMapBuilder} is created, passed to the consumer for
 * configuration (adding entries via {@code put()} method), then automatically built. This provides
 * a fluent API for constructing maps.
 *
 * <p><b>Requirements:</b> Only applies to {@code Map<K, V>} fields. Uses {@code HashMapBuilder<K,
 * V>} for all map types.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code usingHashMapBuilder} to {@code DISABLED}. See the configuration documentation for details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.Map;
 * import java.util.function.Consumer;
 *
 * @SimpleBuilder
 * public record BookDto(Map<String, String> metadata, Map<String, Integer> ratings) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .metadata(m -> m.put("author", "John Doe").put("isbn", "123-456"))
 *     .ratings(r -> r.put("quality", 5).put("readability", 4))
 *     .build();
 * }</pre>
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
