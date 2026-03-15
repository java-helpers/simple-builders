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

import static org.javahelpers.simple.builders.processor.analysis.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.generators.field.MethodGeneratorUtil.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.model.type.TypeNameList;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates Consumer-based methods for List fields with collection builder support.
 *
 * <p>This generator creates methods that accept {@code Consumer<ArrayListBuilder>} or {@code
 * Consumer<ArrayListBuilderWithElementBuilders>} depending on whether the element type has a
 * builder. The consumer configures the collection builder, which is then built and assigned.
 *
 * <p><b>Important behavior:</b> A collection builder is created, passed to the consumer for
 * configuration (adding elements, configuring nested builders, etc.), then automatically built. For
 * element types with builders, {@code ArrayListBuilderWithElementBuilders} provides additional
 * methods to add elements via their builders.
 *
 * <p><b>Requirements:</b> Only applies to {@code List<T>} fields. Uses {@code ArrayListBuilder<T>}
 * for simple element types, or {@code ArrayListBuilderWithElementBuilders<T, TBuilder>} when the
 * element type has a {@code @SimpleBuilder} annotation.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code usingArrayListBuilder} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.List;
 * import java.util.function.Consumer;
 *
 * @SimpleBuilder
 * public record BookDto(List<String> tags, List<AuthorDto> authors) {}
 *
 * @SimpleBuilder
 * public record AuthorDto(String name) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .tags(t -> t.add("java").add("builder"))
 *     .authors(a -> a
 *         .add(b -> b.name("John Doe"))
 *         .add(b -> b.name("Jane Smith")))
 *     .build();
 * }</pre>
 */
public class ListConsumerGenerator implements MethodGenerator {

  private static final int PRIORITY = 53;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    // Only apply to List fields
    if (!(field.getFieldType() instanceof TypeNameList fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return false;
    }
    // Don't apply if field itself has a builder (higher priority)
    if (field.getFieldType().getBuilderType().isPresent()) {
      return false;
    }

    return context.getConfiguration().shouldUseArrayListBuilder()
        || context.getConfiguration().shouldUseArrayListBuilderWithElementBuilders();
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    if (!(field.getFieldType() instanceof TypeNameList fieldTypeGeneric
        && fieldTypeGeneric.isParameterized())) {
      return Collections.emptyList();
    }

    TypeName elementType = fieldTypeGeneric.getElementType();
    Optional<TypeName> elementBuilderType = fieldTypeGeneric.getElementBuilderType();

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseArrayListBuilderWithElementBuilders()) {
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(ArrayListBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          MethodGeneratorUtil.createFieldConsumerWithElementBuilders(
              field, collectionBuilderType, elementBuilderType.get(), builderType, context);
      return List.of(method);
    } else if (context.getConfiguration().shouldUseArrayListBuilder()) {
      TypeName arrayListBuilderType = map2TypeName(ArrayListBuilder.class);
      TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(arrayListBuilderType, elementType);
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
