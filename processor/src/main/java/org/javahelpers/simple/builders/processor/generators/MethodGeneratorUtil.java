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

import java.util.List;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.dtos.GenericParameterDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.dtos.TypeNameVariable;
import org.javahelpers.simple.builders.processor.util.JavapoetMapper;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Utility class providing common functionality for method generators.
 *
 * <p>This class contains shared constants and helper methods used across multiple generator
 * implementations.
 */
public final class MethodGeneratorUtil {

  // Constants for method parameter suffixes
  public static final String SUFFIX_CONSUMER = "Consumer";
  public static final String SUFFIX_SUPPLIER = "Supplier";
  public static final String BUILDER_SUFFIX = "Builder";

  // Constants for code template arguments
  public static final String ARG_FIELD_NAME = "fieldName";
  public static final String ARG_DTO_METHOD_PARAM = "dtoMethodParam";
  public static final String ARG_DTO_METHOD_PARAMS = "dtoMethodParams";
  public static final String ARG_BUILDER_FIELD_WRAPPER = "builderFieldWrapper";
  public static final String ARG_HELPER_TYPE = "helperType";
  public static final String ARG_ELEMENT_TYPE = "elementType";

  public static final TypeName TRACKED_VALUE_TYPE =
      TypeName.of(org.javahelpers.simple.builders.core.util.TrackedValue.class);

  private MethodGeneratorUtil() {
    // Utility class - prevent instantiation
  }

  /**
   * Generates the name of setters on the builder according to configuration and field name.
   *
   * <p>If the suffix is empty, returns the fieldName as-is. If the suffix is set, capitalizes the
   * first letter of fieldName and prepends the suffix.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>fieldName="name", suffix="" → "name"
   *   <li>fieldName="name", suffix="with" → "withName"
   *   <li>fieldName="age", suffix="set" → "setAge"
   * </ul>
   *
   * @param fieldName the field name
   * @param context the processing context containing the configuration with the suffix
   * @return the method name with suffix applied
   */
  public static String generateBuilderMethodName(String fieldName, ProcessingContext context) {
    String suffix = context.getConfiguration().getSetterSuffix();
    if (suffix == null || suffix.isEmpty()) {
      return fieldName;
    }
    return suffix + StringUtils.capitalize(fieldName);
  }

  /**
   * Gets the method access modifier from the builder configuration.
   *
   * @param context the processing context
   * @return the Modifier for method access, or null for package-private
   */
  public static Modifier getMethodAccessModifier(ProcessingContext context) {
    return JavapoetMapper.map2Modifier(context.getConfiguration().getMethodAccess());
  }

  /**
   * Sets the access modifier on a MethodDto if the modifier is not null.
   *
   * @param method the MethodDto to update
   * @param modifier the access modifier to set, or null for package-private
   */
  public static void setMethodAccessModifier(MethodDto method, Modifier modifier) {
    if (modifier != null) {
      method.setModifier(modifier);
    }
  }

  /**
   * Creates a generic TypeName from a base type and generic parameters.
   *
   * <p>If the generic parameters list is empty, returns the base type as-is. Otherwise creates a
   * TypeNameGeneric with the base type and generic type variables.
   *
   * @param baseType the base type name
   * @param genericParameters the list of generic parameter DTOs
   * @return the generic type name, or the base type if no generics
   */
  public static TypeName createGenericTypeName(
      TypeName baseType, List<GenericParameterDto> genericParameters) {
    if (genericParameters.isEmpty()) {
      return baseType;
    }

    List<TypeName> typeVariables =
        genericParameters.stream()
            .map(GenericParameterDto::getName)
            .map(TypeNameVariable::new)
            .map(TypeName.class::cast)
            .toList();

    return new TypeNameGeneric(baseType, typeVariables);
  }
}
