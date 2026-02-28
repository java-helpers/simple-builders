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
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates basic setter methods for all fields of the builder.
 *
 * <p>This generator creates the primary setter method for each field, which accepts the field type
 * directly and stores it in the builder. The method name follows the configured setter
 * prefix/suffix pattern (default is field name without prefix).
 *
 * <p><b>Important behavior:</b> The generated setter wraps the value using {@code changedValue()}
 * to track that the field has been explicitly set. This allows distinguishing between fields that
 * were never set and fields that were set to {@code null}.
 *
 * <p><b>Requirements:</b> Always applies to all fields. This is the fundamental setter that every
 * builder field must have.
 *
 * <p>This generator cannot be deactivated as it provides the core builder functionality. However,
 * the method naming can be configured using the setter prefix/suffix configuration options.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.List;
 *
 * @SimpleBuilder
 * public record ExampleDto(String title, int pages, List<String> tags) {}
 *
 * // Usage of generated Builder:
 * var result = ExampleDtoBuilder.builder()
 *     .title("My Book")
 *     .pages(250)
 *     .tags(List.of("java", "builder"))
 *     .build();
 * }</pre>
 */
public class BasicSetterGenerator implements MethodGenerator {

  private static final int PRIORITY = 100;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    return true;
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    MethodDto setterMethod =
        MethodGeneratorUtil.createBuilderMethodForFieldWithTransform(
            field, null, field.getFieldType(), builderType, context);

    return Collections.singletonList(setterMethod);
  }
}
