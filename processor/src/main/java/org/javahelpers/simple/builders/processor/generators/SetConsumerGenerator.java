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
import java.util.Optional;
import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilderWithElementBuilders;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for Set fields with collection builder support.
 *
 * <p>This generator creates methods that accept {@code Consumer<HashSetBuilder>} or {@code
 * Consumer<HashSetBuilderWithElementBuilders>} depending on whether the element type has a builder.
 * The consumer configures the collection builder, which is then built and assigned.
 *
 * <p><b>Important behavior:</b> A collection builder is created, passed to the consumer for
 * configuration (adding elements, configuring nested builders, etc.), then automatically built. For
 * element types with builders, {@code HashSetBuilderWithElementBuilders} provides additional
 * methods to add elements via their builders.
 *
 * <p><b>Requirements:</b> Only applies to {@code Set<T>} fields. Uses {@code HashSetBuilder<T>} for
 * simple element types, or {@code HashSetBuilderWithElementBuilders<T, TBuilder>} when the element
 * type has a {@code @SimpleBuilder} annotation.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code usingHashSetBuilder} to {@code DISABLED}. See the configuration documentation for details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.Set;
 * import java.util.function.Consumer;
 *
 * @SimpleBuilder
 * public record BookDto(Set<String> categories, Set<AuthorDto> authors) {}
 *
 * @SimpleBuilder
 * public record AuthorDto(String name) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .categories(c -> c.add("Fiction").add("Adventure"))
 *     .authors(a -> a
 *         .add(b -> b.name("John Doe"))
 *         .add(b -> b.name("Jane Smith")))
 *     .build();
 * }</pre>
 */
public class SetConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 52;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    // Only apply to Set fields
    if (!(field.getFieldType() instanceof TypeNameSet fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return false;
    }
    // Don't apply if field itself has a builder (higher priority)
    if (field.getFieldType().getBuilderType().isPresent()) {
      return false;
    }
    return context.getConfiguration().shouldUseHashSetBuilder()
        || context.getConfiguration().shouldUseHashSetBuilderWithElementBuilders();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!(field.getFieldType() instanceof TypeNameSet fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return Collections.emptyList();
    }

    TypeName elementType = fieldTypeGeneric.getElementType();
    Optional<TypeName> elementBuilderType = fieldTypeGeneric.getElementBuilderType();

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseHashSetBuilderWithElementBuilders()) {
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(HashSetBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          MethodGeneratorUtil.createFieldConsumerWithElementBuilders(
              field, collectionBuilderType, elementBuilderType.get(), builderType, context);
      return List.of(method);
    } else if (context.getConfiguration().shouldUseHashSetBuilder()) {
      TypeName hashSetBuilderType = map2TypeName(HashSetBuilder.class);
      TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(hashSetBuilderType, elementType);
      MethodDto method =
          MethodGeneratorUtil.createFieldConsumerWithBuilder(
              field,
              builderTypeGeneric,
              "this.$fieldName:N.value()",
              "",
              Map.of(),
              builderType,
              context);
      return List.of(method);
    }

    return Collections.emptyList();
  }
}
