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

package org.javahelpers.simple.builders.example.custom;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Custom generator that adds validation methods for String fields.
 *
 * <p>This generator demonstrates how to create custom method generators
 * that extend the simple-builders framework. It adds validation methods
 * for String fields that check for null/empty values.</p>
 *
 * <p><b>Example generated method:</b></p>
 * <pre>{@code
 * public PersonDtoBuilder validateName() {
 *     if (name == null || name.trim().isEmpty()) {
 *         throw new IllegalArgumentException("Name cannot be null or empty");
 *     }
 *     return this;
 * }
 * }</pre>
 */
public class StringValidationGenerator implements MethodGenerator {

    private static final int PRIORITY = 60;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
        // Only apply to String fields
        return "java.lang.String".equals(field.getFieldType().getFullQualifiedName());
    }

    @Override
    public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
        String fieldInDto = field.getOriginalFieldName();
        String fieldInBuilder = field.getFieldNameInBuilder();
        String methodName = "validate" + StringUtils.capitalize(fieldInDto);
        
        String methodBody = String.format(
            "if (!%s.isSet() || %s.value().trim().isEmpty()) {\n" +
            "    throw new IllegalArgumentException(\"%s cannot be null or empty\");\n" +
            "}\n" +
            "return this;",
            fieldInBuilder, fieldInBuilder, StringUtils.capitalize(fieldInDto)
        );

        MethodDto validationMethod = new MethodDto(methodName, builderType);
        validationMethod.setCode(methodBody);
        validationMethod.setJavadoc(
            "Validates that the " + fieldInDto + " field is not null or empty.\n" +
            "\n" +
            "@return this builder instance for chaining\n" +
            "@throws IllegalArgumentException if " + fieldInDto + " is null or empty"
        );

        return List.of(validationMethod);
    }
}
