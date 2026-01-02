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
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Generates varargs helper methods for collection fields.
 *
 * <p>This generator creates convenience methods that accept varargs parameters for List, Set, and
 * Map fields, making it easier to set collection values without explicitly creating collection
 * instances.
 *
 * <h3>Generated Methods Example:</h3>
 *
 * <pre>
 * // For List<String> tags field:
 * public BookDtoBuilder tags(String... tags) {
 *   this.tags = changedValue(List.of(tags));
 *   return this;
 * }
 *
 * // For Set<Integer> ratings field:
 * public BookDtoBuilder ratings(Integer... ratings) {
 *   this.ratings = changedValue(Set.of(ratings));
 *   return this;
 * }
 * </pre>
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>For {@code List<String>}: {@code names(String... names)}
 *   <li>For {@code Set<Integer>}: {@code ids(Integer... ids)}
 *   <li>For {@code Map<K,V>}: {@code entries(Map.Entry<K,V>... entries)}
 * </ul>
 *
 * <p>Priority: 60 (medium-high - convenience methods are useful but basic setters come first)
 *
 * <p>This generator respects the configuration flag {@code shouldGenerateVarArgsHelpers()}.
 */
public class VarArgsHelperGenerator implements MethodGenerator {

  private static final int PRIORITY = 40;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    if (!context.getConfiguration().shouldGenerateVarArgsHelpers()) {
      return false;
    }
    return (field.getFieldType() instanceof TypeNameList listType && listType.isParameterized())
        || (field.getFieldType() instanceof TypeNameSet setType && setType.isParameterized())
        || (field.getFieldType() instanceof TypeNameMap mapType && mapType.isParameterized());
  }

  @Override
  public List<MethodDto> generateMethods(
      FieldDto field, TypeName builderType, ProcessingContext context) {

    TypeName fieldType = field.getFieldType();
    TypeName parameterType = null;

    if (fieldType instanceof TypeNameList listType) {
      parameterType = new TypeNameArray(listType.getElementType());
    } else if (fieldType instanceof TypeNameSet setType) {
      parameterType = new TypeNameArray(setType.getElementType());
    } else if (fieldType instanceof TypeNameMap mapType) {
      parameterType =
          new TypeNameArray(
              new TypeNameGeneric(
                  "java.util.Map", "Entry", mapType.getKeyType(), mapType.getValueType()));
    }

    if (parameterType == null) {
      return Collections.emptyList();
    }

    MethodDto varArgsMethod =
        createFieldSetterByVarArgs(field, parameterType, builderType, context);
    return Collections.singletonList(varArgsMethod);
  }

  /**
   * Creates a field setter method for collection varargs with automatic transform calculation. The
   * transform is calculated based on the original field type to preserve specific collection
   * implementations (e.g., ArrayList, LinkedList, HashSet, TreeSet, HashMap, TreeMap).
   *
   * @param field the field definition containing name, type, and javadoc
   * @param parameterType the type of the method parameter (varargs array type)
   * @param builderType the builder type for the return type
   * @param context processing context
   * @return the method DTO for the setter
   */
  private MethodDto createFieldSetterByVarArgs(
      FieldDto field, TypeName parameterType, TypeName builderType, ProcessingContext context) {
    String baseExpression;
    TypeName fieldType = field.getFieldType();

    if (fieldType instanceof TypeNameList listType) {
      baseExpression =
          listType.isConcreteImplementation() ? "java.util.List.of(%s)" : "List.of(%s)";
    } else if (fieldType instanceof TypeNameSet setType) {
      baseExpression = setType.isConcreteImplementation() ? "java.util.Set.of(%s)" : "Set.of(%s)";
    } else if (fieldType instanceof TypeNameMap mapType) {
      baseExpression =
          mapType.isConcreteImplementation() ? "java.util.Map.ofEntries(%s)" : "Map.ofEntries(%s)";
    } else {
      return null;
    }
    String transform = MethodGeneratorUtil.wrapConcreteCollectionType(fieldType, baseExpression);

    return MethodGeneratorUtil.createFieldSetterWithTransform(
        field.getFieldNameEstimated(),
        field.getFieldName(),
        field.getJavaDoc(),
        transform,
        parameterType,
        List.of(),
        builderType,
        context);
  }
}
