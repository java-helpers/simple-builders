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
 * List and Set types. The generated methods follow the pattern "add2FieldName" and always use this
 * naming convention regardless of setter suffix configuration.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For List<String> tags field:
 * public BookDtoBuilder add2Tags(String element) {
 *   if (this.tags.isSet()) {
 *     newCollection = new ArrayList<>(this.tags.value());
 *   } else {
 *     newCollection = new ArrayList<>();
 *   }
 *   newCollection.add(element);
 *   this.tags = changedValue(newCollection);
 *   return this;
 * }
 *
 * // For Set<String> categories field:
 * public BookDtoBuilder add2Categories(String element) {
 *   if (this.categories.isSet()) {
 *     newCollection = new HashSet<>(this.categories.value());
 *   } else {
 *     newCollection = new HashSet<>();
 *   }
 *   newCollection.add(element);
 *   this.categories = changedValue(newCollection);
 *   return this;
 * }
 * </pre>
 *
 * <p>Priority: 30 (medium - collection helpers are useful but basic setters come first)
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateAddToCollectionHelpers()}.
 *
 * <p>Feature #86: Supporting addToField for Sets/Lists
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
    MethodDto methodDto = new MethodDto();
    String methodName = "add2" + StringUtils.capitalize(fieldNameEstimated);
    methodDto.setMethodName(methodName);
    methodDto.setReturnType(builderType);

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
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_ELEMENT_TYPE, elementType);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
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
