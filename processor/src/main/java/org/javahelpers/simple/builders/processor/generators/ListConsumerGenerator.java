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
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates Consumer-based methods for List fields with collection builder support.
 *
 * <p>This generator creates methods that accept Consumer&lt;ArrayListBuilder&gt; or
 * Consumer&lt;ArrayListBuilderWithElementBuilders&gt; depending on whether the element type has a
 * builder.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For List<String> tags field (no builder for String):
 * public BookDtoBuilder tags(Consumer<ArrayListBuilder<String>> tagsBuilderConsumer) {
 *   ArrayListBuilder<String> builder = new ArrayListBuilder<>();
 *   tagsBuilderConsumer.accept(builder);
 *   this.tags = changedValue(builder.build());
 *   return this;
 * }
 *
 * // For List<PersonDto> authors field (PersonDto has @SimpleBuilder):
 * public BookDtoBuilder authors(Consumer<ArrayListBuilderWithElementBuilders<PersonDto, PersonDtoBuilder>> authorsBuilderConsumer) {
 *   ArrayListBuilderWithElementBuilders<PersonDto, PersonDtoBuilder> builder =
 *       new ArrayListBuilderWithElementBuilders<>(PersonDtoBuilder::create);
 *   authorsBuilderConsumer.accept(builder);
 *   this.authors = changedValue(builder.build());
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 53 (medium - List consumers are useful but basic setters come first)
 *
 * <p>This generator respects the configuration flag {@code shouldUseArrayListBuilder()}.
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
