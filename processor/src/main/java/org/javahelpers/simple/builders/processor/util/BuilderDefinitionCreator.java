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

package org.javahelpers.simple.builders.processor.util;

import static java.util.stream.Collectors.toSet;
import static javax.lang.model.type.TypeKind.ARRAY;
import static org.javahelpers.simple.builders.processor.util.AnnotationValidator.validateAnnotatedElement;
import static org.javahelpers.simple.builders.processor.util.JavaLangAnalyser.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2MethodParameter;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2TypeName;
import static org.javahelpers.simple.builders.processor.util.TypeNameAnalyser.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.builders.HashMapBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
import org.javahelpers.simple.builders.core.builders.HashSetBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.util.TrackedValue;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;

/** Class for creating a specific BuilderDefinitionDto for an annotated DTO class. */
public class BuilderDefinitionCreator {
  private static final String BUILDER_SUFFIX = "Builder";

  // Template argument keys for code generation
  private static final String ARG_FIELD_NAME = "fieldName";
  private static final String ARG_DTO_METHOD_PARAM = "dtoMethodParam";
  private static final String ARG_DTO_METHOD_PARAMS = "dtoMethodParams";
  private static final String ARG_BUILDER_FIELD_WRAPPER = "builderFieldWrapper";
  private static final String ARG_HELPER_TYPE = "helperType";

  // Type constants
  private static final TypeName TRACKED_VALUE_TYPE = TypeName.of(TrackedValue.class);

  // Parameter name suffixes
  private static final String SUFFIX_CONSUMER = "Consumer";
  private static final String SUFFIX_SUPPLIER = "Supplier";

  private BuilderDefinitionCreator() {
    // Private constructor to prevent instantiation
  }

  /**
   * Extracts a BuilderDefinition from the annotated element.
   *
   * @param annotatedElement the annotated type element to extract the builder definition from
   * @param context the processing context for annotations processing
   * @return the builder definition
   * @throws BuilderException if validation or generation failed
   */
  public static BuilderDefinitionDto extractFromElement(
      Element annotatedElement, ProcessingContext context) throws BuilderException {
    validateAnnotatedElement(annotatedElement);
    TypeElement annotatedType = (TypeElement) annotatedElement;

    context.debug("Extracting builder definition from: %s", annotatedType.getQualifiedName());

    BuilderDefinitionDto result = initializeBuilderDefinition(annotatedType, context);

    // Track field names to resolve conflicts during field creation
    Map<String, FieldDto> fieldNameRegistry = new HashMap<>();

    List<FieldDto> constructorFields =
        extractConstructorFields(annotatedType, result, context, fieldNameRegistry);
    result.addAllFieldsInConstructor(constructorFields);

    List<FieldDto> setterFields =
        extractSetterFields(annotatedType, result, context, fieldNameRegistry);
    result.addAllFields(setterFields);

    // Create the With interface if enabled in configuration
    if (context.getConfiguration().shouldGenerateWithInterface()) {
      NestedTypeDto withInterface = createWithInterface(result, context);
      result.addNestedType(withInterface);
    }

    return result;
  }

  /** Initializes the builder definition with package, class name, and generics. */
  private static BuilderDefinitionDto initializeBuilderDefinition(
      TypeElement annotatedType, ProcessingContext context) {
    BuilderDefinitionDto result = new BuilderDefinitionDto();
    String packageName = context.getPackageName(annotatedType);
    String simpleClassName = annotatedType.getSimpleName().toString();
    String builderSuffix = context.getConfiguration().getBuilderSuffix();
    result.setBuilderTypeName(new TypeName(packageName, simpleClassName + builderSuffix));
    result.setBuildingTargetTypeName(new TypeName(packageName, simpleClassName));
    result.setConfiguration(context.getConfiguration());

    context.debug(
        "Builder will be generated as: %s.%s", packageName, simpleClassName + builderSuffix);

    // Extract generics from the annotated type via mapper (stream-based)
    JavaLangMapper.map2GenericParameterDtos(annotatedType, context).forEach(result::addGeneric);

    return result;
  }

  /**
   * Extracts fields from the constructor parameters.
   *
   * @param annotatedType the type being processed
   * @param builderDef the builder definition being constructed
   * @param context processing context
   * @param fieldNameRegistry registry to track field names and resolve conflicts
   * @return list of fields extracted from constructor parameters
   */
  private static List<FieldDto> extractConstructorFields(
      TypeElement annotatedType,
      BuilderDefinitionDto builderDef,
      ProcessingContext context,
      Map<String, FieldDto> fieldNameRegistry) {
    List<FieldDto> constructorFields = new LinkedList<>();
    Optional<ExecutableElement> constructorOpt = findConstructorForBuilder(annotatedType, context);
    if (constructorOpt.isPresent()) {
      ExecutableElement ctor = constructorOpt.get();
      context.debug(
          "Analyzing constructor: %s with %d parameter(s)",
          ctor.getSimpleName(), ctor.getParameters().size());
      for (VariableElement param : ctor.getParameters()) {
        Optional<FieldDto> fieldFromCtor =
            createFieldFromConstructor(
                annotatedType, param, builderDef.getBuilderTypeName(), context, fieldNameRegistry);
        if (fieldFromCtor.isPresent()) {
          FieldDto field = fieldFromCtor.get();
          logFieldAddition(field, context);
          constructorFields.add(field);
        }
      }
    }
    return constructorFields;
  }

  /**
   * Extracts fields from setter methods, avoiding duplicates from constructor fields.
   *
   * @param result the builder definition containing constructor fields to check for duplicates
   * @return list of fields extracted from setter methods
   */
  private static List<FieldDto> extractSetterFields(
      TypeElement annotatedType,
      BuilderDefinitionDto result,
      ProcessingContext context,
      Map<String, FieldDto> fieldNameRegistry) {
    List<FieldDto> setterFields = new LinkedList<>();

    // Build a set of constructor field names to avoid duplicates from setters
    Set<String> ctorFieldNames =
        result.getConstructorFieldsForBuilder().stream()
            .map(FieldDto::getFieldName)
            .collect(toSet());

    List<ExecutableElement> methods = findAllPossibleSettersOfClass(annotatedType, context);
    int processedCount = 0;
    int addedCount = 0;
    int skippedCount = 0;

    for (ExecutableElement mth : methods) {
      context.debug(
          "Analyzing method: %s with %d parameter(s)",
          mth.getSimpleName(), mth.getParameters().size());

      if (isMethodRelevantForBuilder(mth, context)) {
        // Extract the original field name from the setter method (before any renaming)
        String methodName = mth.getSimpleName().toString();
        String originalFieldName =
            StringUtils.uncapitalize(Strings.CI.removeStart(methodName, "set"));

        // Skip if constructor already handles this field
        if (ctorFieldNames.contains(originalFieldName)) {
          skippedCount++;
          context.debug(
              "Skipping setter field '%s' - already handled by constructor", originalFieldName);
          continue;
        }

        Optional<FieldDto> maybeField =
            createFieldFromSetter(mth, result.getBuilderTypeName(), context, fieldNameRegistry);
        if (maybeField.isPresent()) {
          processedCount++;
          FieldDto field = maybeField.get();
          addedCount++;
          logFieldAddition(field, context);
          setterFields.add(field);
        } else {
          skippedCount++;
        }
      } else {
        skippedCount++;
      }
    }

    context.debug(
        "Processed %d possible setters: added %d fields, skipped %d",
        processedCount, addedCount, skippedCount);

    return setterFields;
  }

  /** Logs the addition of a field with its type information. */
  private static void logFieldAddition(FieldDto field, ProcessingContext context) {
    String fieldTypeName = field.getFieldType().getClassName();
    if (field.getFieldType().getPackageName() != null
        && !field.getFieldType().getPackageName().isEmpty()) {
      fieldTypeName = field.getFieldType().getPackageName() + "." + fieldTypeName;
    }
    context.debug("  -> Adding field: %s (type: %s)", field.getFieldName(), fieldTypeName);
  }

  private static boolean isMethodRelevantForBuilder(
      ExecutableElement mth, ProcessingContext context) {
    if (!hasNoThrowablesDeclared(mth)) {
      context.debug("  -> Skipping: declares throwables");
      return false;
    }
    if (!hasNoReturnValue(mth)) {
      context.debug("  -> Skipping: has return value");
      return false;
    }
    if (!hasNotAnnotation(IgnoreInBuilder.class, mth)) {
      context.debug("  -> Skipping: has @IgnoreInBuilder annotation");
      return false;
    }
    if (!isNotPrivate(mth)) {
      context.debug("  -> Skipping: is private");
      return false;
    }
    if (!isNotStatic(mth)) {
      context.debug("  -> Skipping: is static");
      return false;
    }
    return true;
  }

  private static void addAdditionalHelperMethodsForField(
      FieldDto field,
      List<AnnotationDto> annotations,
      TypeName builderType,
      ProcessingContext context) {
    String fieldNameInBuilder = field.getFieldName();
    String fieldJavaDoc = field.getJavaDoc();

    // Check for String type (not array) and add format method
    if (isString(field.getFieldType())
        && !(field.getFieldType() instanceof TypeNameArray)
        && context.getConfiguration().shouldGenerateStringFormatHelpers()) {
      String fieldName = field.getFieldNameEstimated();
      MethodDto method =
          createStringFormatMethodWithTransform(
              fieldName,
              fieldNameInBuilder,
              fieldJavaDoc,
              "String.format(format, args)",
              annotations,
              builderType,
              context);
      field.addMethod(method);
    }

    if ((field.getFieldType() instanceof TypeNameArray arrayType)) {
      TypeName elementType = arrayType.getTypeOfArray();

      // Add method accepting List<ElementType> and converting to array
      TypeNameGeneric listType = new TypeNameGeneric(map2TypeName(List.class), elementType);
      String fieldName = field.getFieldNameEstimated();
      MethodDto method1 =
          createFieldSetterForArrayFromList(
              fieldName, fieldNameInBuilder, listType, elementType, builderType, context);
      field.addMethod(method1);

      // Add Consumer<ArrayListBuilder<ElementType>> method only if builder consumers are enabled
      if (context.getConfiguration().shouldGenerateBuilderConsumer()) {
        TypeName collectionBuilderType = map2TypeName(ArrayListBuilder.class);
        MethodDto method2 =
            createFieldConsumerWithArrayBuilder(
                fieldName,
                fieldNameInBuilder,
                collectionBuilderType,
                elementType,
                builderType,
                context);
        field.addMethod(method2);
      }
      return;
    }

    // Only process generic types (List, Set, Map, Optional, etc.)
    if (!(field.getFieldType() instanceof TypeNameGeneric fieldTypeGeneric)) {
      return;
    }

    List<TypeName> innerTypes = fieldTypeGeneric.getInnerTypeArguments();
    int innerTypesCnt = innerTypes.size();
    if (isList(field.getFieldType()) && innerTypesCnt == 1) {
      // Only add varargs helper if enabled in configuration
      if (context.getConfiguration().shouldGenerateVarArgsHelpers()) {
        String fieldName = field.getFieldNameEstimated();
        MethodDto method =
            createFieldSetterWithTransform(
                fieldName,
                fieldNameInBuilder,
                fieldJavaDoc,
                "List.of(%s)",
                new TypeNameArray(innerTypes.get(0), false),
                builderType,
                context);
        field.addMethod(method);
      }
    } else if (isSet(field.getFieldType()) && innerTypesCnt == 1) {
      // Only add varargs helper if enabled in configuration
      if (context.getConfiguration().shouldGenerateVarArgsHelpers()) {
        String fieldName = field.getFieldNameEstimated();
        MethodDto method =
            createFieldSetterWithTransform(
                fieldName,
                fieldNameInBuilder,
                fieldJavaDoc,
                "Set.of(%s)",
                new TypeNameArray(innerTypes.get(0), true),
                builderType,
                context);
        field.addMethod(method);
      }
    } else if (isMap(field.getFieldType()) && innerTypesCnt == 2) {
      // Only add varargs helper if enabled in configuration
      if (context.getConfiguration().shouldGenerateVarArgsHelpers()) {
        TypeName mapEntryType =
            new TypeNameArray(
                new TypeNameGeneric("java.util", "Map.Entry", innerTypes.get(0), innerTypes.get(1)),
                false);
        String fieldName = field.getFieldNameEstimated();
        MethodDto method =
            createFieldSetterWithTransform(
                fieldName,
                fieldNameInBuilder,
                fieldJavaDoc,
                "Map.ofEntries(%s)",
                mapEntryType,
                builderType,
                context);
        field.addMethod(method);
      }
    } else if (isOptional(field.getFieldType()) && innerTypesCnt == 1) {
      String fieldName = field.getFieldNameEstimated();

      // Only generate unboxed optional method if enabled in configuration
      if (context.getConfiguration().shouldGenerateUnboxedOptional()) {
        // Add setter that accepts the inner type T and wraps it in Optional.ofNullable()
        MethodDto method =
            createFieldSetterWithTransform(
                fieldName,
                fieldNameInBuilder,
                fieldJavaDoc,
                "Optional.ofNullable(%s)",
                innerTypes.get(0),
                builderType,
                context);
        field.addMethod(method);
      }

      // If Optional<String>, add format method
      TypeName innerType = innerTypes.get(0);
      if (isString(innerType) && context.getConfiguration().shouldGenerateStringFormatHelpers()) {
        MethodDto method =
            createStringFormatMethodWithTransform(
                fieldName,
                fieldNameInBuilder,
                fieldJavaDoc,
                "Optional.of(String.format(format, args))",
                List.of(),
                builderType,
                context);
        field.addMethod(method);
      }
    }
  }

  private static void addConsumerMethodsForField(
      FieldDto field,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      TypeName builderType,
      ProcessingContext context) {
    // Do not generate consumer methods for generic type variables (e.g., T)
    if (field.getFieldType() instanceof TypeNameVariable) {
      return;
    }
    // Skip consumer generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }

    if (!tryAddBuilderConsumer(field, fieldParameter, builderType, context)
        && !tryAddFieldConsumer(field, fieldTypeElement, builderType, context)
        && !tryAddListConsumer(field, fieldParameter, builderType, context)
        && !tryAddMapConsumer(field, builderType, context)
        && !tryAddSetConsumer(field, fieldParameter, builderType, context)) {
      tryAddStringBuilderConsumer(field, builderType, context);
    }
  }

  /** Tries to add a direct builder-based consumer when the field type itself has a builder. */
  private static boolean tryAddBuilderConsumer(
      FieldDto field,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context) {
    // Builder consumers are controlled by generateBuilderConsumer
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    Optional<TypeName> fieldBuilderOpt = resolveBuilderType(fieldParameter, context);
    if (fieldBuilderOpt.isPresent()) {
      TypeName fieldBuilderType = fieldBuilderOpt.get();
      MethodDto method =
          BuilderDefinitionCreator.createFieldConsumerWithBuilder(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              fieldBuilderType,
              builderType,
              context);
      field.addMethod(method);
      return true;
    }
    return false;
  }

  /** Tries to add a field consumer when the field type has an accessible empty constructor. */
  private static boolean tryAddFieldConsumer(
      FieldDto field,
      TypeElement fieldTypeElement,
      TypeName builderType,
      ProcessingContext context) {
    // Check if field consumer generation is enabled in configuration
    if (!context.getConfiguration().shouldGenerateFieldConsumer()) {
      return false;
    }
    if (!isJavaClass(field.getFieldType())
        && fieldTypeElement != null
        && fieldTypeElement.getKind() == javax.lang.model.element.ElementKind.CLASS
        && !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        && hasEmptyConstructor(fieldTypeElement, context)) {
      // Only generate a Consumer for concrete classes with an accessible empty constructor
      MethodDto method =
          createFieldConsumer(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              field.getFieldType(),
              builderType,
              context);
      field.addMethod(method);
      return true;
    }
    return false;
  }

  /** Tries to add StringBuilder-based consumer for String and Optional<String>. */
  private static boolean tryAddStringBuilderConsumer(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    // StringBuilder is a builder pattern, controlled by generateBuilderConsumer
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    if (shouldGenerateStringBuilderConsumer(field.getFieldType())) {
      String transform =
          isOptionalString(field.getFieldType())
              ? "Optional.of(builder.toString())"
              : "builder.toString()";
      MethodDto method =
          createStringBuilderConsumer(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              transform,
              builderType,
              context);
      field.addMethod(method);
      return true;
    }
    return false;
  }

  /** Tries to add List-specific consumer methods. Returns true if handled. */
  private static boolean tryAddListConsumer(
      FieldDto field,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context) {
    if (!(isList(field.getFieldType())
        && field.getFieldType() instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1)) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getInnerTypeArguments().get(0);

    // Get the TypeMirror of the element type from the parameter's type
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    // Only generate builder consumer methods if enabled
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseArrayListBuilderWithElementBuilders()) {
      // Element type has a builder - use ArrayListBuilderWithElementBuilders if enabled
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(ArrayListBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          createFieldConsumerWithElementBuilders(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              collectionBuilderType,
              elementBuilderType.get(),
              builderType,
              context);
      field.addMethod(method);
    } else if (context.getConfiguration().shouldUseArrayListBuilder()) {
      // Regular ArrayListBuilder if enabled
      TypeName collectionBuilderType = map2TypeName(ArrayListBuilder.class);
      MethodDto method =
          createFieldConsumerWithBuilder(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              collectionBuilderType,
              elementType,
              builderType,
              context);
      field.addMethod(method);
    } else {
      return false;
    }
    return true;
  }

  /** Tries to add Map-specific consumer methods. Returns true if handled. */
  private static boolean tryAddMapConsumer(
      FieldDto field, TypeName builderType, ProcessingContext context) {
    // Check if builder consumers are enabled
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }
    // Check if HashMapBuilder is enabled
    if (!context.getConfiguration().shouldUseHashMapBuilder()) {
      return false;
    }
    if (!(isMap(field.getFieldType())
        && field.getFieldType() instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 2)) {
      return false;
    }

    TypeNameGeneric builderTargetTypeName =
        new TypeNameGeneric(
            map2TypeName(HashMapBuilder.class),
            fieldTypeGeneric.getInnerTypeArguments().get(0),
            fieldTypeGeneric.getInnerTypeArguments().get(1));
    MethodDto mapConsumerWithBuilder =
        BuilderDefinitionCreator.createFieldConsumerWithBuilder(
            field.getFieldName(),
            field.getFieldName(),
            field.getJavaDoc(),
            builderTargetTypeName,
            builderType,
            context);
    field.addMethod(mapConsumerWithBuilder);
    return true;
  }

  /** Tries to add Set-specific consumer methods. Returns true if handled. */
  private static boolean tryAddSetConsumer(
      FieldDto field,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context) {
    if (!(isSet(field.getFieldType())
        && field.getFieldType() instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1)) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getInnerTypeArguments().get(0);

    // Get the TypeMirror of the element type from the parameter's type
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    // Only generate builder consumer methods if enabled
    if (!context.getConfiguration().shouldGenerateBuilderConsumer()) {
      return false;
    }

    if (elementBuilderType.isPresent()
        && context.getConfiguration().shouldUseHashSetBuilderWithElementBuilders()) {
      // Element type has a builder - use HashSetBuilderWithElementBuilders if enabled
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(HashSetBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      MethodDto method =
          createFieldConsumerWithElementBuilders(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              collectionBuilderType,
              elementBuilderType.get(),
              builderType,
              context);
      field.addMethod(method);
    } else if (context.getConfiguration().shouldUseHashSetBuilder()) {
      // Regular HashSetBuilder if enabled
      TypeName collectionBuilderType = map2TypeName(HashSetBuilder.class);
      MethodDto method =
          createFieldConsumerWithBuilder(
              field.getFieldName(),
              field.getFieldName(),
              field.getJavaDoc(),
              collectionBuilderType,
              elementType,
              builderType,
              context);
      field.addMethod(method);
    } else {
      return false;
    }
    return true;
  }

  private static void addSupplierMethodsForField(
      FieldDto field,
      TypeElement fieldTypeElement,
      TypeName builderType,
      ProcessingContext context) {
    // Check if supplier generation is enabled in configuration
    if (!context.getConfiguration().shouldGenerateFieldSupplier()) {
      return;
    }
    // Skip supplier generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }
    // For all fields including Optional<T>, use the real field type for suppliers
    String fieldName = field.getFieldNameEstimated();
    String fieldNameInBuilder = field.getFieldName();
    MethodDto method =
        createFieldSupplier(
            fieldName,
            fieldNameInBuilder,
            field.getJavaDoc(),
            field.getFieldType(),
            builderType,
            context);
    field.addMethod(method);
  }

  private static Optional<FieldDto> createFieldFromSetter(
      ExecutableElement mth,
      TypeName builderType,
      ProcessingContext context,
      Map<String, FieldDto> fieldNameRegistry) {
    String methodName = mth.getSimpleName().toString();
    String fieldName = StringUtils.uncapitalize(Strings.CI.removeStart(methodName, "set"));

    List<? extends VariableElement> parameters = mth.getParameters();
    if (parameters.size() != 1) {
      // Should never happen, just to be sure here
      context.warning(mth, "Unexpected state of method.");
      return Optional.empty();
    }

    // Finding generics declared on the setter itself (field-specific), e.g., <T extends
    // Serializable>
    // If there are field-specific generics, no field in builder could be generated for it, so it
    // needs to be ignored
    if (JavaLangAnalyser.hasGenericTypes(mth)) {
      context.warning(
          mth.getEnclosingElement(),
          "Field '%s' has field-specific generics, so it will be ignored",
          fieldName);
      return Optional.empty();
    }

    VariableElement fieldParameter = parameters.get(0);
    TypeElement dtoType = (TypeElement) mth.getEnclosingElement();

    // Extract only the @param Javadoc for the single setter parameter (if present)
    String fullJavaDoc = context.getDocComment(mth);
    String javaDoc = JavaLangAnalyser.extractParamJavaDoc(fullJavaDoc, fieldParameter);
    if (javaDoc == null) {
      javaDoc = fieldName;
    }

    // Check for field name conflicts and rename if necessary
    String finalFieldName =
        resolveFieldNameConflict(fieldName, fieldParameter, fieldNameRegistry, context);

    // Pass both original field name (for methods) and final field name (for builder field)
    Optional<FieldDto> result =
        createFieldDto(
            fieldName, finalFieldName, javaDoc, fieldParameter, dtoType, builderType, context);

    if (result.isPresent()) {
      fieldNameRegistry.put(finalFieldName, result.get());
    }

    return result;
  }

  /**
   * Creates a FieldDto from a constructor parameter, including a simple builder setter to supply
   * the constructor argument.
   */
  private static Optional<FieldDto> createFieldFromConstructor(
      TypeElement annotatedType,
      VariableElement param,
      TypeName builderType,
      ProcessingContext context,
      Map<String, FieldDto> fieldNameRegistry) {
    String fieldName = param.getSimpleName().toString();
    // Set javadoc (default to field name if no javadoc found)
    String javaDoc =
        JavaLangAnalyser.extractParamJavaDoc(context.getDocComment(annotatedType), param);
    if (javaDoc == null) {
      javaDoc = fieldName;
    }

    // Check for field name conflicts and rename if necessary
    String finalFieldName = resolveFieldNameConflict(fieldName, param, fieldNameRegistry, context);

    // Pass both original field name (for methods) and final field name (for builder field)
    Optional<FieldDto> result =
        createFieldDto(
            fieldName, finalFieldName, javaDoc, param, annotatedType, builderType, context);

    if (result.isPresent()) {
      fieldNameRegistry.put(finalFieldName, result.get());
    }

    return result;
  }

  /**
   * Resolves field name conflicts by checking the registry and renaming if necessary. If a field
   * with the same name already exists but has a different type, the new field is renamed by
   * appending the simple type name.
   *
   * @param fieldName the proposed field name
   * @param param the parameter element (to get type information)
   * @param fieldNameRegistry the registry tracking existing field names
   * @param context the processing context for logging warnings
   * @return the final field name (either original or renamed)
   */
  private static String resolveFieldNameConflict(
      String fieldName,
      VariableElement param,
      Map<String, FieldDto> fieldNameRegistry,
      ProcessingContext context) {

    FieldDto existing = fieldNameRegistry.get(fieldName);
    if (existing == null) {
      // No conflict
      return fieldName;
    }

    // Conflict detected: rename the new field by appending the simple type name
    MethodParameterDto paramDto = map2MethodParameter(param, context);
    if (paramDto == null) {
      // If we can't determine the type, just return the original name
      return fieldName;
    }

    TypeName newType = paramDto.getParameterType();
    String existingTypeName = existing.getFieldType().getClassName();
    String newTypeName = newType.getClassName();
    String renamedFieldName = fieldName + newTypeName;

    context.warning(
        null,
        """
          Builder field conflict: field '%s' (type %s) renamed to '%s' in builder to avoid conflict with existing field (type %s). \
        The reason could be having helperfunctions in the DTO or a mistake in the DTO (e.g., two setters with the same name but different field types). \
        Please check it and if the DTO is correct, you can get rid of this warning by setting the IgnoreInBuilder annotation on one of the setters for this field.\
        """,
        fieldName,
        newTypeName,
        renamedFieldName,
        existingTypeName);

    return renamedFieldName;
  }

  /**
   * Common method to create a FieldDto with all builder methods (setter, supplier, consumer,
   * helpers).
   *
   * @param fieldName the estimated field name (used for method names)
   * @param fieldNameInBuilder the builder field name (used for storage, may be renamed)
   * @param javaDoc the javadoc for the field
   * @param param the parameter element (from constructor or setter)
   * @param dtoType the DTO type containing this field
   * @param context processing context
   * @return Optional containing the FieldDto, or empty if field cannot be created
   */
  private static Optional<FieldDto> createFieldDto(
      String fieldName,
      String fieldNameInBuilder,
      String javaDoc,
      VariableElement param,
      TypeElement dtoType,
      TypeName builderType,
      ProcessingContext context) {
    MethodParameterDto paramDto = map2MethodParameter(param, context);
    if (paramDto == null) {
      return Optional.empty();
    }

    TypeName fieldType = paramDto.getParameterType();
    TypeMirror fieldTypeMirror = param.asType();
    Element rawElement = context.asElement(fieldTypeMirror);
    TypeElement fieldTypeElement = rawElement instanceof TypeElement te ? te : null;

    FieldDto field = new FieldDto();
    field.setFieldName(fieldNameInBuilder); // Use renamed field name for builder field storage
    field.setFieldNameEstimated(fieldName);
    field.setFieldType(fieldType);
    field.setJavaDoc(javaDoc);

    // Note: setterName will be set explicitly by the caller before field renaming

    // Find matching getter on the DTO type using the builder field name
    JavaLangAnalyser.findGetterForField(dtoType, fieldNameInBuilder, fieldTypeMirror, context)
        .ifPresent(getter -> field.setGetterName(getter.getSimpleName().toString()));

    // Extract annotations from the field parameter
    List<AnnotationDto> annotations = FieldAnnotationExtractor.extractAnnotations(param, context);

    // Check if field has non-null constraint (annotation or primitive type)
    if (FieldAnnotationExtractor.hasNonNullConstraint(param)
        || fieldTypeMirror.getKind().isPrimitive()) {
      field.setNonNullable(true);
    }

    // Add basic setter method with annotations - use ORIGINAL field name for method name
    MethodDto method =
        createFieldSetterWithTransform(
            fieldName,
            fieldNameInBuilder,
            javaDoc,
            null,
            fieldType,
            annotations,
            builderType,
            context);
    field.addMethod(method);

    // Add consumer/supplier/helper methods - use ORIGINAL field name for method names
    addConsumerMethodsForField(field, param, fieldTypeElement, builderType, context);
    addSupplierMethodsForField(field, fieldTypeElement, builderType, context);
    addAdditionalHelperMethodsForField(field, annotations, builderType, context);

    return Optional.of(field);
  }

  /**
   * Creates a field setter method with optional transform, without annotations.
   *
   * @param fieldName the name of the method
   * @param fieldNameInBuilder the name of the builder field
   * @param transform optional transform expression (e.g., "Optional.of(%s)")
   * @param fieldType the type of the field
   * @param fieldJavadoc the javadoc for the field
   * @return the method DTO for the setter
   */
  private static MethodDto createFieldSetterWithTransform(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      String transform,
      TypeName fieldType,
      TypeName builderType,
      ProcessingContext context) {
    return createFieldSetterWithTransform(
        fieldName,
        fieldNameInBuilder,
        fieldJavadoc,
        transform,
        fieldType,
        List.of(),
        builderType,
        context);
  }

  /**
   * Creates a field setter method with optional transform and annotations.
   *
   * @param fieldName the name of the field
   * @param fieldJavadoc the javadoc for the field
   * @param transform optional transform expression (e.g., "Optional.of(%s)")
   * @param fieldType the type of the field
   * @param annotations annotations to apply to the parameter
   * @return the method DTO for the setter
   */
  private static MethodDto createFieldSetterWithTransform(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      String transform,
      TypeName fieldType,
      List<AnnotationDto> annotations,
      TypeName builderType,
      ProcessingContext context) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(fieldType);
    // Add annotations to the parameter
    annotations.forEach(parameter::addAnnotation);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    String params;
    if (StringUtils.isBlank(transform)) {
      params = parameter.getParameterName();
    } else {
      params = String.format(transform, parameter.getParameterName());
    }
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParams:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAMS, params);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    // Direct setters have highest priority, transform methods have high priority
    methodDto.setPriority(transform == null ? MethodDto.PRIORITY_HIGHEST : MethodDto.PRIORITY_HIGH);
    // Set javadoc
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavadoc));
    return methodDto;
  }

  private static MethodDto createFieldConsumer(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      TypeName fieldType,
      TypeName builderType,
      ProcessingContext context) {
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        $helperType:T consumer = this.$fieldName:N.isSet() ? this.$fieldName:N.value() : new $helperType:T();
        $dtoMethodParam:N.accept(consumer);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(consumer);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, fieldType);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> by executing the provided consumer.

        @param %s consumer providing an instance of %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavadoc));
    return methodDto;
  }

  private static MethodDto createStringBuilderConsumer(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      String transform,
      TypeName builderType,
      ProcessingContext context) {
    TypeName stringBuilderType = map2TypeName(StringBuilder.class);
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), stringBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + "StringBuilderConsumer");
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        StringBuilder builder = new StringBuilder();
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument("transform", transform);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setReturnType(builderType);
    methodDto.setPriority(MethodDto.PRIORITY_LOW);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> by executing the provided consumer.

        @param %s consumer providing an instance of %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavadoc));
    return methodDto;
  }

  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      TypeName consumerBuilderType,
      TypeName builderTargetType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric builderTypeGeneric =
        new TypeNameGeneric(consumerBuilderType, builderTargetType);
    return BuilderDefinitionCreator.createFieldConsumerWithBuilder(
        fieldName,
        fieldNameInBuilder,
        fieldJavadoc,
        builderTypeGeneric,
        returnBuilderType,
        context);
  }

  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavaDoc,
      TypeName consumerBuilderType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    return createFieldConsumerWithBuilder(
        fieldName,
        fieldNameInBuilder,
        fieldJavaDoc,
        consumerBuilderType,
        "this.$fieldName:N.value()",
        "",
        Map.of(),
        returnBuilderType,
        context);
  }

  /**
   * Creates a consumer method for collection builders with element builders. Used for
   * ArrayListBuilderWithElementBuilders and HashSetBuilderWithElementBuilders.
   */
  private static MethodDto createFieldConsumerWithElementBuilders(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavaDoc,
      TypeName collectionBuilderType,
      TypeName elementBuilderType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    return createFieldConsumerWithBuilder(
        fieldName,
        fieldNameInBuilder,
        fieldJavaDoc,
        collectionBuilderType,
        "this.$fieldName:N.value(), $elementBuilderType:T::create",
        "$elementBuilderType:T::create",
        Map.of("elementBuilderType", elementBuilderType),
        returnBuilderType,
        context);
  }

  /**
   * Creates a consumer method for a field with a builder type.
   *
   * @param fieldName the field name
   * @param builderType the builder type (e.g., ArrayListBuilder or
   *     ArrayListBuilderWithElementBuilders)
   * @param constructorArgsWithValue constructor arguments when field is already set
   * @param constructorArgsEmpty constructor arguments when field is empty
   * @param additionalArguments additional template arguments to add to the method (must be TypeName
   *     values)
   * @param context processing context
   */
  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavaDoc,
      TypeName consumerBuilderType,
      String constructorArgsWithValue,
      String additionalConstructorArgs,
      Map<String, TypeName> additionalArguments,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), consumerBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(returnBuilderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet() ? new $helperType:T(%s) : new $helperType:T(%s);
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(builder.build());
        return this;
        """
            .formatted(constructorArgsWithValue, additionalConstructorArgs));
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, consumerBuilderType);
    additionalArguments.forEach(methodDto::addArgument);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> using a builder consumer that produces the value.

        @param %s consumer providing an instance of a builder for %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavaDoc));
    return methodDto;
  }

  private static MethodDto createFieldSupplier(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavaDoc,
      TypeName fieldType,
      TypeName builderType,
      ProcessingContext context) {
    TypeNameGeneric supplierType = new TypeNameGeneric(map2TypeName(Supplier.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_SUPPLIER);
    parameter.setParameterTypeName(supplierType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParam:N.get());
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> by invoking the provided supplier.

        @param %s supplier for %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldJavaDoc));
    return methodDto;
  }

  private static MethodDto createStringFormatMethodWithTransform(
      String fieldName,
      String fieldNameInBuilder,
      String fieldJavadoc,
      String transform,
      List<AnnotationDto> annotations,
      TypeName builderType,
      ProcessingContext context) {
    TypeName stringType = map2TypeName(String.class);

    MethodParameterDto formatParam = new MethodParameterDto();
    formatParam.setParameterName("format");
    formatParam.setParameterTypeName(stringType);
    // Apply annotations to the format parameter (it's a String value)
    annotations.forEach(formatParam::addAnnotation);

    MethodParameterDto argsParam = new MethodParameterDto();
    argsParam.setParameterName("args");
    argsParam.setParameterTypeName(new TypeNameArray(TypeName.of(Object.class), false));

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(formatParam);
    methodDto.addParameter(argsParam);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument("transform", transform);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @param %s %s
        @return current instance of builder
        """
            .formatted(
                fieldName,
                formatParam.getParameterName(),
                fieldJavadoc,
                argsParam.getParameterName(),
                fieldJavadoc));
    return methodDto;
  }

  /**
   * Creates a field setter method that accepts a List and converts it to an array.
   *
   * @param fieldName the field name
   * @param listType the List<ElementType> parameter type
   * @param elementType the element type of the array
   * @param builderType the builder type to return
   * @return the method DTO for the setter
   */
  private static MethodDto createFieldSetterForArrayFromList(
      String fieldName,
      String fieldNameInBuilder,
      TypeName listType,
      TypeName elementType,
      TypeName builderType,
      ProcessingContext context) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(listType);

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(builderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParams:N.toArray(new $elementType:T[0]));
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAMS, fieldName);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.addArgument("elementType", elementType);
    methodDto.setPriority(MethodDto.PRIORITY_HIGH);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code>.

        @param %s %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldName));
    return methodDto;
  }

  /**
   * Creates a consumer method for array fields with ArrayListBuilder. This allows building arrays
   * using the fluent ArrayListBuilder API.
   */
  private static MethodDto createFieldConsumerWithArrayBuilder(
      String fieldName,
      String fieldNameInBuilder,
      TypeName collectionBuilderType,
      TypeName elementType,
      TypeName returnBuilderType,
      ProcessingContext context) {
    TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(collectionBuilderType, elementType);
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), builderTypeGeneric);

    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(generateSetterName(fieldName, context));
    methodDto.setReturnType(returnBuilderType);
    methodDto.addParameter(parameter);
    setMethodAccessModifier(methodDto, getMethodAccessModifier(context));
    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet() ? new $helperType:T(java.util.List.of(this.$fieldName:N.value())) : new $helperType:T();
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(builder.build().toArray(new $elementType:T[0]));
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldNameInBuilder);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, builderTypeGeneric);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    methodDto.addArgument("elementType", elementType);
    methodDto.setPriority(MethodDto.PRIORITY_MEDIUM);
    methodDto.setJavadoc(
        """
        Sets the value for <code>%s</code> using the fluent builder consumer.

        @param %s consumer for %s
        @return current instance of builder
        """
            .formatted(fieldName, parameter.getParameterName(), fieldName));
    return methodDto;
  }

  /**
   * Checks if a StringBuilder consumer should be generated for the given field type. This applies
   * to plain String fields and Optional&lt;String&gt; fields.
   *
   * @param fieldType the type of the field
   * @return true if StringBuilder consumer should be generated, false otherwise
   */
  private static boolean shouldGenerateStringBuilderConsumer(TypeName fieldType) {
    // Check for plain String (not array)
    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      return true;
    }

    // Check for Optional<String>
    return isOptionalString(fieldType);
  }

  private static Optional<TypeName> resolveBuilderType(
      VariableElement param, ProcessingContext context) {
    TypeMirror typeOfParameter = param.asType();
    if (typeOfParameter.getKind() == ARRAY || typeOfParameter.getKind().isPrimitive()) {
      return Optional.empty();
    }
    Element elementOfParameter = context.asElement(typeOfParameter);
    if (!(elementOfParameter instanceof TypeElement typeElement)) {
      // Can happen for primitives or certain compiler-internal types; nothing to build
      return Optional.empty();
    }
    // For direct field types disallow generics here in the caller
    if (JavaLangAnalyser.hasGenericTypes(typeElement)) {
      context.debug(
          "  -> Skipping builder lookup for generic type %s", typeElement.getSimpleName());
      return Optional.empty();
    }
    return resolveBuilderTypeFromTypeElement(typeElement, context);
  }

  /**
   * Finds the builder type for a given element type (used for collection elements).
   *
   * @param elementType the type of the collection element
   * @param elementTypeMirror the TypeMirror of the element type (from the parameter)
   * @param context processing context
   * @return Optional containing the builder TypeName if the element type has @SimpleBuilder
   */
  private static Optional<TypeName> resolveBuilderType(
      TypeName elementType, TypeMirror elementTypeMirror, ProcessingContext context) {

    // Skip cases without type mirror
    if (elementTypeMirror == null) {
      return Optional.empty();
    }

    // Skip type variables and primitives
    if (elementType instanceof TypeNameVariable || elementType instanceof TypeNamePrimitive) {
      context.debug("  -> Skipping type variable or primitive: %s", elementType);
      return Optional.empty();
    }

    // Try to get the element from the TypeMirror
    Element element = context.asElement(elementTypeMirror);
    if (!(element instanceof TypeElement typeElement)) {
      context.debug("  -> Element is not a TypeElement: %s", element);
      return Optional.empty();
    }
    // For collection element types: delegate to shared helper
    return resolveBuilderTypeFromTypeElement(typeElement, context);
  }

  /**
   * Shared helper resolving a builder {@link TypeName} from a {@link TypeElement} if it is
   * annotated with {@code @SimpleBuilder}. Generics policy is enforced by the callers.
   *
   * @param typeElement the type element to inspect
   * @param context processing context
   */
  private static Optional<TypeName> resolveBuilderTypeFromTypeElement(
      TypeElement typeElement, ProcessingContext context) {
    // Check annotation presence first
    Optional<AnnotationMirror> foundBuilderAnnotation =
        findAnnotation(typeElement, SimpleBuilder.class);
    if (foundBuilderAnnotation.isEmpty()) {
      context.debug("  -> Type %s has no @SimpleBuilder", typeElement.getSimpleName());
      return Optional.empty();
    }

    String packageName = context.getPackageName(typeElement);
    String simpleClassName = typeElement.getSimpleName().toString();
    String builderSuffix = context.getConfiguration().getBuilderSuffix();
    context.debug(
        "  -> Found @SimpleBuilder on type %s.%s, will use %s%s",
        packageName, simpleClassName, simpleClassName, builderSuffix);
    return Optional.of(new TypeName(packageName, simpleClassName + builderSuffix));
  }

  /**
   * Extracts the first type argument from a parameterized type mirror. For example, from
   * List&lt;Task&gt;, this extracts the Task TypeMirror.
   *
   * @param typeMirror the parameterized type mirror
   * @return the first type argument, or null if not a parameterized type
   */
  private static TypeMirror extractFirstTypeArgument(TypeMirror typeMirror) {
    if (typeMirror instanceof javax.lang.model.type.DeclaredType declaredType) {
      List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
      if (!typeArguments.isEmpty()) {
        return typeArguments.get(0);
      }
    }
    return null;
  }

  /**
   * Creates the "With" interface definition that allows the DTO to implement fluent modification
   * methods.
   *
   * @param builderDef the builder definition containing type information
   * @param context the processing context
   * @return the nested type definition for the With interface
   */
  private static NestedTypeDto createWithInterface(
      BuilderDefinitionDto builderDef, ProcessingContext context) {
    context.debug(
        "Creating With interface for: %s", builderDef.getBuilderTypeName().getClassName());

    NestedTypeDto withInterface = new NestedTypeDto();
    withInterface.setTypeName("With");
    withInterface.setKind(NestedTypeDto.NestedTypeKind.INTERFACE);
    withInterface.setPublic(true);
    withInterface.setJavadoc(
        "Interface that can be implemented by the DTO to provide fluent modification methods.");

    // Create the first method: DtoType with(Consumer<BuilderType> b)
    MethodDto withConsumerMethod = createWithConsumerMethod(builderDef);
    withInterface.addMethod(withConsumerMethod);

    // Create the second method: BuilderType with()
    MethodDto withBuilderMethod = createWithBuilderMethod(builderDef);
    withInterface.addMethod(withBuilderMethod);

    return withInterface;
  }

  /**
   * Creates the `DtoType with(Consumer<BuilderType> b)` method definition.
   *
   * @param builderDef the builder definition
   * @return the method definition
   */
  private static MethodDto createWithConsumerMethod(BuilderDefinitionDto builderDef) {
    MethodDto method = new MethodDto();
    method.setMethodName("with");

    // Return type is the DTO type
    TypeName dtoType = builderDef.getBuildingTargetTypeName();
    method.setReturnType(dtoType);

    // Parameter: Consumer<BuilderType> b
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName("b");
    // For interface methods, we store the full type as a string
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), builderDef.getBuilderTypeName());
    parameter.setParameterTypeName(consumerType);
    method.addParameter(parameter);

    // Add implementation with validation to catch wrong implementations
    method.setCode(
        """
        $builderType:T builder;
        try {
          builder = new $builderType:T($dtoType:T.class.cast(this));
        } catch ($classcastexception:T ex) {
          throw new $illegalargumentexception:T("The interface '$builderType:T.With' should only be implemented by classes, which could be casted to '$dtoType:T'", ex);
        }
        b.accept(builder);
        return builder.build();
        """);
    method.addArgument("builderType", builderDef.getBuilderTypeName());
    method.addArgument("dtoType", builderDef.getBuildingTargetTypeName());
    method.addArgument("classcastexception", map2TypeName(ClassCastException.class));
    method.addArgument("illegalargumentexception", map2TypeName(IllegalArgumentException.class));

    method.setJavadoc(
        """
      Applies modifications to a builder initialized from this instance and returns the built object.

      @param b the consumer to apply modifications
      @return the modified instance
      """);

    return method;
  }

  /**
   * Creates the `BuilderType with()` method definition.
   *
   * @param builderDef the builder definition
   * @return the method definition
   */
  private static MethodDto createWithBuilderMethod(BuilderDefinitionDto builderDef) {
    MethodDto method = new MethodDto();
    method.setMethodName("with");

    // Return type is the Builder type
    method.setReturnType(builderDef.getBuilderTypeName());

    // Add implementation with validation to catch wrong implementations
    method.setCode(
        """
        try {
          return new $builderType:T($dtoType:T.class.cast(this));
        } catch ($classcastexception:T ex) {
          throw new $illegalargumentexception:T("The interface '$builderType:T.With' should only be implemented by classes, which could be casted to '$dtoType:T'", ex);
        }
        """);
    method.addArgument("builderType", builderDef.getBuilderTypeName());
    method.addArgument("dtoType", builderDef.getBuildingTargetTypeName());
    method.addArgument("classcastexception", map2TypeName(ClassCastException.class));
    method.addArgument("illegalargumentexception", map2TypeName(IllegalArgumentException.class));

    method.setJavadoc(
        """
      Creates a builder initialized from this instance.

      @return a builder initialized with this instance's values
      """);

    return method;
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
  private static String generateSetterName(String fieldName, ProcessingContext context) {
    String suffix = context.getConfiguration().getSetterSuffix();
    if (StringUtils.isBlank(suffix)) {
      return fieldName;
    }
    return StringUtils.trim(suffix) + StringUtils.capitalize(fieldName);
  }

  /**
   * Gets the method access modifier from the builder configuration.
   *
   * @param context the processing context
   * @return the Modifier for method access, or null for package-private
   */
  private static Modifier getMethodAccessModifier(ProcessingContext context) {
    return JavapoetMapper.map2Modifier(context.getConfiguration().getMethodAccess());
  }

  /**
   * Sets the access modifier on a MethodDto if the modifier is not null.
   *
   * @param method the MethodDto to update
   * @param modifier the access modifier to set, or null for package-private
   */
  private static void setMethodAccessModifier(MethodDto method, Modifier modifier) {
    if (modifier != null) {
      method.setModifier(modifier);
    }
  }
}
