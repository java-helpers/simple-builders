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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates add2FieldName helper methods for List and Set fields.
 *
 * <p>This generator creates methods that add single elements to collection fields, supporting both
 * List and Set types. The generated methods follow the pattern "add2{#FieldName}" and always use
 * this naming convention regardless of setter suffix configuration.
 *
 * <p><b>Important behavior:</b> The generated methods preserve immutability by creating a new
 * collection instance. If the field already has a value, the method creates a copy of the existing
 * collection, adds the new element, and assigns the new collection. If the field is not yet set, a
 * new collection is created with the single element.
 *
 * <p><b>Requirements:</b> Only applies to parameterized collection types ({@code List<T>} or {@code
 * Set<T>}). Raw types like {@code List} or {@code Set} are not supported.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateAddToCollectionHelpers} to {@code DISABLED}. See the configuration documentation
 * for details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.List;
 * import java.util.Set;
 *
 * @SimpleBuilder
 * public record ExampleDto(List<String> tags, Set<String> categories) {}
 *
 * // Usage of generated Builder:
 * var result = ExampleDtoBuilder.builder()
 *     .add2Tags("tag1")
 *     .add2Tags("tag2")
 *     .add2Categories("cat1")
 *     .build();
 * }</pre>
 */
public class AddToCollectionGenerator implements MethodGenerator {

  private static final int PRIORITY = 30;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateAddToCollectionHelpers()) {
      return false;
    }

    TypeName fieldType = field.getFieldType();

    return (fieldType instanceof TypeNameList listType && listType.isParameterized())
        || (fieldType instanceof TypeNameSet setType && setType.isParameterized());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    List<MethodDto> methods = new ArrayList<>();
    TypeName fieldType = field.getFieldType();

    if (fieldType instanceof TypeNameList listType && listType.isParameterized()) {
      MethodDto addMethod =
          createAddToCollectionMethod(
              field.getFieldNameEstimated(),
              field.getFieldName(),
              listType,
              listType.getElementType(),
              builderType,
              context);
      methods.add(addMethod);
    } else if (fieldType instanceof TypeNameSet setType && setType.isParameterized()) {
      MethodDto addMethod =
          createAddToCollectionMethod(
              field.getFieldNameEstimated(),
              field.getFieldName(),
              setType,
              setType.getElementType(),
              builderType,
              context);
      methods.add(addMethod);
    }

    return methods;
  }

  private MethodDto createAddToCollectionMethod(
      String fieldNameEstimated,
      String fieldName,
      TypeName fieldType,
      TypeName elementType,
      TypeName builderType,
      ProcessingContext context) {
    String methodName = "add2" + StringUtils.capitalize(fieldNameEstimated);
    MethodDto methodDto = new MethodDto(methodName, builderType);

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName("element");
    parameter.setParameterTypeName(elementType);
    methodDto.addParameter(parameter);

    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));

    String collectionImpl;
    TypeName collectionVarType;
    if (fieldType instanceof TypeNameList listType) {
      collectionImpl = listType.isConcreteImplementation() ? listType.getClassName() : "ArrayList";
      collectionVarType = fieldType;
    } else if (fieldType instanceof TypeNameSet setType) {
      collectionImpl = setType.isConcreteImplementation() ? setType.getClassName() : "HashSet";
      collectionVarType = fieldType;
    } else {
      throw new IllegalArgumentException("Unsupported field type: " + fieldType);
    }

    methodDto.setCode(
        """
        $collectionVarType:T newCollection;
        if (this.$fieldName:N.isSet()) {
          newCollection = new $collectionImpl:T<>(this.$fieldName:N.value());
        } else {
          newCollection = new $collectionImpl:T<>();
        }
        newCollection.add(element);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(newCollection);
        return this;
        """);
    methodDto.addArgument("collectionVarType", collectionVarType);
    methodDto.addArgument("collectionImpl", new TypeName("java.util", collectionImpl));
    methodDto.addArgument("fieldName", fieldName);
    methodDto.addArgument("elementType", elementType);
    methodDto.addArgument("builderFieldWrapper", TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);

    methodDto.setJavadoc(
        """
        Adds a single element to <code>%s</code>.

        @param element the element to add
        @return current instance of builder
        """
            .formatted(fieldName));

    return methodDto;
  }
}
