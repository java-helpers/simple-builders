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
 * Generates basic setter methods for builder fields.
 *
 * <p>This generator creates the primary setter method for each field, which accepts the field type
 * directly and stores it in the builder. The setter method:
 *
 * <ul>
 *   <li>Accepts a parameter of the field's type
 *   <li>Stores the value in a TrackedValue wrapper
 *   <li>Returns the builder instance for method chaining
 *   <li>Applies any field annotations to the parameter
 *   <li>Includes javadoc documentation
 * </ul>
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * public BookDtoBuilder title(String title) {
 *   this.title = changedValue(title);
 *   return this;
 * }
 *
 * public BookDtoBuilder pages(int pages) {
 *   this.pages = changedValue(pages);
 *   return this;
 * }
 *
 * public BookDtoBuilder tags(List<String> tags) {
 *   this.tags = changedValue(tags);
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 100 (highest - basic setters are fundamental to builder functionality)
 *
 * <p>This generator always applies to all fields and has the highest priority to ensure the basic
 * setter is always generated first.
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
        MethodGeneratorUtil.createFieldSetterWithTransform(
            field.getFieldNameEstimated(),
            field.getFieldName(),
            field.getJavaDoc(),
            null,
            field.getFieldType(),
            field.getParameterAnnotations(),
            builderType,
            context);

    return Collections.singletonList(setterMethod);
  }
}
