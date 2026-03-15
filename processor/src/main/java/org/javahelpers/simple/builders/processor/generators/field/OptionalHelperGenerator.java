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

import static org.javahelpers.simple.builders.processor.analysis.TypeNameAnalyser.isParameterizedOptional;

import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates unboxed Optional helper methods for {@code Optional<T>} fields.
 *
 * <p>This generator creates convenience methods that accept the inner type {@code T} directly and
 * wrap it in {@code Optional.ofNullable()} automatically. This makes it easier to set Optional
 * values without explicitly wrapping them.
 *
 * <p><b>Important behavior:</b> The method accepts the unwrapped type {@code T}, wraps it using
 * {@code Optional.ofNullable()}, and assigns it to the field. This allows passing {@code null}
 * values which will be converted to {@code Optional.empty()}.
 *
 * <p><b>Requirements:</b> Only applies to parameterized {@code Optional<T>} fields.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateUnboxedOptional} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.Optional;
 *
 * @SimpleBuilder
 * public record BookDto(String title, Optional<String> subtitle, Optional<Integer> rating) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .title("My Book")
 *     .subtitle("A Great Story")  // String -> Optional<String>
 *     .rating(5)                   // Integer -> Optional<Integer>
 *     .build();
 * }</pre>
 */
public class OptionalHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 70;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateUnboxedOptional()) {
      return false;
    }
    return isParameterizedOptional(field.getFieldType());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    TypeNameGeneric genericType = (TypeNameGeneric) field.getFieldType();
    List<TypeName> innerTypes = genericType.getInnerTypeArguments();

    if (CollectionUtils.isEmpty(innerTypes)) {
      return Collections.emptyList();
    }

    TypeName innerType = innerTypes.get(0);
    MethodDto method =
        MethodGeneratorUtil.createBuilderMethodForFieldWithTransform(
            field, "Optional.ofNullable(%s)", innerType, builderType, context);

    return Collections.singletonList(method);
  }
}
