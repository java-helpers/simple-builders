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

import java.util.Collections;
import java.util.List;
import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.model.type.TypeNameList;
import org.javahelpers.simple.builders.processor.model.type.TypeNameMap;
import org.javahelpers.simple.builders.processor.model.type.TypeNameSet;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Generates varargs helper methods for List, Set, and Map fields.
 *
 * <p>This generator creates convenience methods that accept varargs parameters for List, Set, and
 * Map fields, making it easier to set collection values without explicitly creating collection
 * instances. The varargs are converted to the appropriate collection type.
 *
 * <p><b>Important behavior:</b> The varargs are converted using {@code List.of()}, {@code
 * Set.of()}, or {@code Map.ofEntries()} depending on the field type. For concrete collection types
 * (e.g., {@code ArrayList}, {@code HashSet}), the result is wrapped in the appropriate constructor.
 *
 * <p><b>Requirements:</b> Only applies to parameterized {@code List<T>}, {@code Set<T>}, or {@code
 * Map<K, V>} fields. For maps, accepts {@code Map.Entry<K, V>} varargs.
 *
 * <p>This generator is enabled by default and can be deactivated by setting the configuration flag
 * {@code generateVarArgsHelpers} to {@code DISABLED}. See the configuration documentation for
 * details.
 *
 * <h3>Example to demonstrate the generated methods</h3>
 *
 * <pre>{@code
 * // ExampleDto for demonstration
 * import org.javahelpers.simple.builders.annotation.SimpleBuilder;
 * import java.util.List;
 * import java.util.Set;
 * import java.util.Map;
 *
 * @SimpleBuilder
 * public record BookDto(List<String> tags, Set<String> categories, Map<String, Integer> ratings) {}
 *
 * // Usage of generated Builder:
 * var result = BookDtoBuilder.builder()
 *     .tags("java", "builder", "pattern")
 *     .categories("programming", "design")
 *     .ratings(Map.entry("quality", 5), Map.entry("readability", 4))
 *     .build();
 * }</pre>
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

    MethodDto method =
        MethodGeneratorUtil.createBuilderMethodForFieldWithTransform(
            field, transform, parameterType, builderType, context);

    // Add code block imports for collection factory methods
    if (fieldType instanceof TypeNameList) {
      method.getMethodCodeDto().addCodeBlockImport(new TypeName("java.util", "List"));
    } else if (fieldType instanceof TypeNameSet) {
      method.getMethodCodeDto().addCodeBlockImport(new TypeName("java.util", "Set"));
    } else if (fieldType instanceof TypeNameMap) {
      method.getMethodCodeDto().addCodeBlockImport(new TypeName("java.util", "Map"));
    }

    return method;
  }
}
