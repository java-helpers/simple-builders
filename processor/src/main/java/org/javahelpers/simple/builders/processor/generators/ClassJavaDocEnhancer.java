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

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import java.util.List;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.dtos.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.dtos.TypeNameList;
import org.javahelpers.simple.builders.processor.dtos.TypeNameMap;
import org.javahelpers.simple.builders.processor.dtos.TypeNameSet;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

/**
 * Enhancer that adds class-level JavaDoc to generated builder classes.
 *
 * <p>This enhancer adds comprehensive JavaDoc documentation to the generated builder class,
 * including information about the target DTO class and the builder's purpose. The JavaDoc follows
 * standard conventions and provides useful information for developers using the builder.
 *
 * <p>The JavaDoc includes:
 *
 * <ul>
 *   <li>Purpose of the builder class
 *   <li>Reference to the target DTO class
 *   <li>Usage information
 * </ul>
 *
 * <p>Priority: 200 (high - class documentation should be applied early but after core
 * infrastructure)
 */
public class ClassJavaDocEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 200;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return true; // Class JavaDoc is always needed for builders
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    ClassName dtoClass =
        ClassName.get(
            builderDto.getBuildingTargetTypeName().getPackageName(),
            builderDto.getBuildingTargetTypeName().getClassName());

    CodeBlock javadoc = createClassJavadoc(builderDto, dtoClass);
    builderDto.setClassJavadoc(javadoc.toString());

    context.debug(
        "Added class JavaDoc to builder %s", builderDto.getBuilderTypeName().getClassName());
  }

  /**
   * Creates comprehensive JavaDoc for the builder class.
   *
   * @param builderDto the builder definition containing field information
   * @param dtoClass the target DTO class
   * @return CodeBlock containing the JavaDoc content
   */
  private CodeBlock createClassJavadoc(BuilderDefinitionDto builderDto, ClassName dtoClass) {
    String simpleDtoName = dtoClass.simpleName();
    String genericPart = getGenericPart(builderDto);

    // Generate realistic example based on actual fields
    String exampleCode = generateExampleCode(builderDto, simpleDtoName, genericPart);

    String fullDtoName = simpleDtoName + genericPart;

    return CodeBlock.of(
        """
        Builder for {@code $1T}.
        <p>
        This builder provides a fluent API for creating instances of $1T with
        method chaining and validation. Use the static {@code create()} method
        to obtain a new builder instance, configure the desired properties using
        the setter methods, and then call {@code build()} to create the final DTO.
        <p>
        Example usage:
        <pre>{@code
        $2L dto = $2L.create()$3N
            .build();
        }</pre>
        """,
        dtoClass,
        fullDtoName,
        exampleCode);
  }

  /** Extracts generic type parameters for the DTO class name. */
  private String getGenericPart(BuilderDefinitionDto builderDto) {
    if (builderDto.getGenerics().isEmpty()) {
      return "";
    }

    StringBuilder generics = new StringBuilder("<");
    boolean first = true;
    for (var generic : builderDto.getGenerics()) {
      if (!first) {
        generics.append(", ");
      }
      generics.append(generic.getName());
      first = false;
    }
    generics.append(">");
    return generics.toString();
  }

  /** Generates realistic example code based on actual fields and available methods. */
  private String generateExampleCode(
      BuilderDefinitionDto builderDto, String simpleDtoName, String genericPart) {
    List<FieldDto> fields = builderDto.getAllFieldsForBuilder();
    if (fields.isEmpty()) {
      return "    // No fields to configure";
    }

    StringBuilder example = new StringBuilder();
    int examplesShown = 0;
    int maxExamples = 8; // Increased limit to show more patterns

    // Prioritize showing different field types and patterns
    boolean stringExampleShown = false;
    boolean optionalExampleShown = false;

    // Show examples for fields with good examples, prioritizing variety
    for (FieldDto field : fields) {
      if (examplesShown >= maxExamples) {
        break;
      }

      String fieldName = field.getFieldName();
      TypeName fieldType = field.getFieldType();
      String className = fieldType.getClassName();

      // Skip if we already have examples of this type (except for showing patterns)
      boolean shouldSkip =
          switch (className) {
            case "String" -> stringExampleShown && examplesShown > 2;
            case "Optional" -> optionalExampleShown;
            default -> false;
          };

      if (shouldSkip) {
        continue;
      }

      // Basic setter example
      String basicExample = generateExampleValue(fieldName, fieldType);
      if (basicExample != null) {
        example.append(String.format("\n    .%s(%s)", fieldName, basicExample));
        examplesShown++;

        // Track which types we've shown
        switch (className) {
          case "String" -> stringExampleShown = true;
          case "Optional" -> optionalExampleShown = true;
        }

        // Show additional method patterns for this field if available
        String additionalExample =
            generateAdditionalMethodExample(fieldName, fieldType, builderDto);
        if (additionalExample != null && examplesShown < maxExamples) {
          example.append(String.format("\n    %s", additionalExample));
          examplesShown++;
        }
      }
    }

    return example.toString();
  }

  /**
   * Generates realistic example values based on field name and type. Returns null if no good
   * example can be generated for this type.
   */
  private String generateExampleValue(String fieldName, TypeName fieldType) {
    String className = fieldType.getClassName();

    // String types - use field name as hint for realistic values
    switch (className) {
      case "String" -> {
        return generateStringValue(fieldName);
      }
      case "int", "Integer" -> {
        return "42";
      }
      case "long", "Long" -> {
        return "123L";
      }
      case "double", "Double" -> {
        return "19.99";
      }
      case "float", "Float" -> {
        return "3.14f";
      }
      case "BigDecimal" -> {
        return "new BigDecimal(\"19.99\")";
      }
      case "boolean", "Boolean" -> {
        return "true";
      }
    }

    // Collection types
    if (fieldType instanceof TypeNameList listType) {
      if (listType.getElementType().getClassName().equals("String")) {
        return "List.of(\"item1\", \"item2\")";
      } else if (listType.getElementType().getClassName().equals("Integer")) {
        return "List.of(1, 2, 3)";
      }
    }
    if (fieldType instanceof TypeNameSet setType) {
      if (setType.getElementType().getClassName().equals("String")) {
        return "Set.of(\"item1\", \"item2\")";
      } else if (setType.getElementType().getClassName().equals("Integer")) {
        return "Set.of(1, 2, 3)";
      }
    }
    if (fieldType instanceof TypeNameMap mapType
        && mapType.getKeyType().getClassName().equals("String")
        && mapType.getValueType().getClassName().equals("String")) {
      return "Map.of(\"key1\", \"value1\")";
    }

    // Skip generic types that fall back to TypeNameList/TypeNameSet/TypeNameMap
    // Skip default fallback cases - we only show examples for types we can handle well
    return null;
  }

  /**
   * Generates examples for additional method patterns based on field type and available generators.
   * Returns null if no additional patterns are available for this field type.
   */
  private String generateAdditionalMethodExample(
      String fieldName, TypeName fieldType, BuilderDefinitionDto builderDto) {
    String className = fieldType.getClassName();

    // Optional unboxed method (for Optional<T> fields)
    if (isParameterizedOptional(fieldType)) {
      String innerType = getOptionalInnerType(fieldType);
      if ("String".equals(innerType)) {
        return String.format(".%s(\"Optional Value\") // Optional unboxed", fieldName);
      }
    }

    // Supplier method for common types
    if (isSupplierMethodAvailable(fieldType)) {
      String supplierExample = generateSupplierExample(fieldName, className);
      if (supplierExample != null) {
        return supplierExample;
      }
    }

    // VarArgs method for collection types
    if (fieldType instanceof TypeNameList || fieldType instanceof TypeNameSet) {
      return String.format(".%s(\"item1\", \"item2\", \"item3\") // VarArgs", fieldName);
    }

    // String format method for String fields
    if ("String".equals(className)) {
      return String.format(".%s(\"Format-%d-%s\", 42, \"value\") // String format", fieldName);
    }

    // Collection helper (add2FieldName) for collection types
    if (fieldType instanceof TypeNameList || fieldType instanceof TypeNameSet) {
      return String.format(".add2%s(\"newItem\") // Collection helper", capitalize(fieldName));
    }

    return null;
  }

  /** Checks if supplier methods are available for the given field type. */
  private boolean isSupplierMethodAvailable(TypeName fieldType) {
    String className = fieldType.getClassName();
    return switch (className) {
      case "LocalDate", "LocalDateTime", "LocalTime", "Instant", "ZonedDateTime" -> true;
      case "String", "Integer", "Long", "Double", "Float", "Boolean" -> true;
      default -> false;
    };
  }

  /** Generates supplier method examples. */
  private String generateSupplierExample(String fieldName, String className) {
    return switch (className) {
      case "LocalDate" -> String.format(".%s(() -> LocalDate.now()) // Supplier", fieldName);
      case "LocalDateTime" ->
          String.format(".%s(() -> LocalDateTime.now()) // Supplier", fieldName);
      case "LocalTime" -> String.format(".%s(() -> LocalTime.now()) // Supplier", fieldName);
      case "Instant" -> String.format(".%s(() -> Instant.now()) // Supplier", fieldName);
      case "ZonedDateTime" ->
          String.format(".%s(() -> ZonedDateTime.now()) // Supplier", fieldName);
      case "String" -> String.format(".%s(() -> \"Computed Value\") // Supplier", fieldName);
      case "Integer" -> String.format(".%s(() -> 42) // Supplier", fieldName);
      case "Long" -> String.format(".%s(() -> 123L) // Supplier", fieldName);
      case "Double" -> String.format(".%s(() -> 19.99) // Supplier", fieldName);
      case "Float" -> String.format(".%s(() -> 3.14f) // Supplier", fieldName);
      case "Boolean" -> String.format(".%s(() -> true) // Supplier", fieldName);
      default -> null;
    };
  }

  /** Checks if the type is a parameterized Optional. */
  private boolean isParameterizedOptional(TypeName fieldType) {
    return fieldType instanceof TypeNameGeneric genericType
        && "Optional".equals(genericType.getClassName())
        && !genericType.getInnerTypeArguments().isEmpty();
  }

  /** Gets the inner type of an Optional. */
  private String getOptionalInnerType(TypeName fieldType) {
    if (isParameterizedOptional(fieldType)) {
      TypeNameGeneric genericType = (TypeNameGeneric) fieldType;
      return genericType.getInnerTypeArguments().get(0).getClassName();
    }
    return null;
  }

  /** Capitalizes the first letter of a string. */
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  /** Generates consistent default string values for examples. */
  private String generateStringValue(String fieldName) {
    return "\"Example\"";
  }
}
