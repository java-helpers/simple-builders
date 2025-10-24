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

    List<FieldDto> constructorFields = extractConstructorFields(annotatedType, context);
    result.addAllFieldsInConstructor(constructorFields);

    List<FieldDto> setterFields = extractSetterFields(annotatedType, result, context);
    result.addAllFields(setterFields);

    return result;
  }

  /** Initializes the builder definition with package, class name, and generics. */
  private static BuilderDefinitionDto initializeBuilderDefinition(
      TypeElement annotatedType, ProcessingContext context) {
    BuilderDefinitionDto result = new BuilderDefinitionDto();
    String packageName = context.getPackageName(annotatedType);
    String simpleClassName = annotatedType.getSimpleName().toString();
    result.setBuilderTypeName(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
    result.setBuildingTargetTypeName(new TypeName(packageName, simpleClassName));

    context.debug(
        "Builder will be generated as: %s.%s", packageName, simpleClassName + BUILDER_SUFFIX);

    // Extract generics from the annotated type via mapper (stream-based)
    JavaLangMapper.map2GenericParameterDtos(annotatedType, context).forEach(result::addGeneric);

    return result;
  }

  /**
   * Extracts fields from the constructor parameters.
   *
   * @return list of fields extracted from constructor parameters
   */
  private static List<FieldDto> extractConstructorFields(
      TypeElement annotatedType, ProcessingContext context) {
    List<FieldDto> constructorFields = new LinkedList<>();
    Optional<ExecutableElement> constructorOpt = findConstructorForBuilder(annotatedType, context);
    if (constructorOpt.isPresent()) {
      ExecutableElement ctor = constructorOpt.get();
      context.debug(
          "Analyzing constructor: %s with %d parameter(s)",
          ctor.getSimpleName(), ctor.getParameters().size());
      for (VariableElement param : ctor.getParameters()) {
        Optional<FieldDto> fieldFromCtor =
            createFieldFromConstructor(annotatedType, param, context);
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
      TypeElement annotatedType, BuilderDefinitionDto result, ProcessingContext context) {
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
        Optional<FieldDto> maybeField = createFieldFromSetter(mth, context);
        if (maybeField.isPresent()) {
          processedCount++;
          FieldDto field = maybeField.get();
          if (!ctorFieldNames.contains(field.getFieldName())) {
            addedCount++;
            logFieldAddition(field, context);
            setterFields.add(field);
            continue;
          }
        } else {
          context.debug("  -> Skipping method (already in constructor): %s", mth.getSimpleName());
        }
      }
      skippedCount++;
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
      FieldDto result, String fieldName, TypeName fieldType) {
    // Check for String type (not array) and add format method
    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      result.addMethod(
          createStringFormatMethodWithTransform(fieldName, "String.format(format, args)"));
    }

    // Only process generic types (List, Set, Map, Optional, etc.)
    if (!(fieldType instanceof TypeNameGeneric fieldTypeGeneric)) {
      return;
    }

    List<TypeName> innerTypes = fieldTypeGeneric.getInnerTypeArguments();
    int innerTypesCnt = innerTypes.size();

    if (isList(fieldType) && innerTypesCnt == 1) {
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "List.of(%s)", new TypeNameArray(innerTypes.get(0), false)));
    } else if (isSet(fieldType) && innerTypesCnt == 1) {
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "Set.of(%s)", new TypeNameArray(innerTypes.get(0), true)));
    } else if (isMap(fieldType) && innerTypesCnt == 2) {
      TypeName mapEntryType =
          new TypeNameArray(
              new TypeNameGeneric("java.util", "Map.Entry", innerTypes.get(0), innerTypes.get(1)),
              false);
      result.addMethod(
          createFieldSetterWithTransform(fieldName, "Map.ofEntries(%s)", mapEntryType));
    } else if (isOptional(fieldType) && innerTypesCnt == 1) {
      // Add setter that accepts the inner type T and wraps it in Optional.of()
      result.addMethod(
          createFieldSetterWithTransform(fieldName, "Optional.of(%s)", innerTypes.get(0)));

      // If Optional<String>, add format method
      TypeName innerType = innerTypes.get(0);
      if (isString(innerType)) {
        result.addMethod(
            createStringFormatMethodWithTransform(
                fieldName, "Optional.of(String.format(format, args))"));
      }
    }
  }

  private static void addConsumerMethodsForField(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      ProcessingContext context) {
    // Do not generate supplier methods for generic type variables (e.g., T)
    if (fieldType instanceof TypeNameVariable) {
      return;
    }
    // Skip consumer generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }

    if (tryAddBuilderConsumer(result, fieldName, fieldType, fieldParameter, context)) {
      // handled by direct builder helper
    } else if (tryAddFieldConsumer(result, fieldName, fieldType, fieldTypeElement, context)) {
      // handled by field helper
    } else if (tryAddListConsumer(result, fieldName, fieldType, fieldParameter, context)) {
      // handled by list helper
    } else if (tryAddMapConsumer(result, fieldName, fieldType)) {
      // handled by map helper
    } else if (tryAddSetConsumer(result, fieldName, fieldType, fieldParameter, context)) {
      // handled by set helper
    } else if (tryAddStringBuilderConsumer(result, fieldName, fieldType)) {
      // handled by string builder helper
    }
  }

  /** Tries to add a direct builder-based consumer when the field type itself has a builder. */
  private static boolean tryAddBuilderConsumer(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      ProcessingContext context) {
    Optional<TypeName> builderTypeOpt = resolveBuilderType(fieldParameter, context);
    if (builderTypeOpt.isPresent()) {
      TypeName builderType = builderTypeOpt.get();
      result.addMethod(
          BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderType));
      return true;
    }
    return false;
  }

  /** Tries to add a consumer using an empty constructor of a concrete non-java class. */
  private static boolean tryAddFieldConsumer(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      TypeElement fieldTypeElement,
      ProcessingContext context) {
    if (!isJavaClass(fieldType)
        && fieldTypeElement != null
        && fieldTypeElement.getKind() == javax.lang.model.element.ElementKind.CLASS
        && !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        && hasEmptyConstructor(fieldTypeElement, context)) {
      // Only generate a Consumer for concrete classes with an accessible empty constructor
      result.addMethod(createFieldConsumer(fieldName, fieldType));
      return true;
    }
    return false;
  }

  /** Tries to add StringBuilder-based consumer for String and Optional<String>. */
  private static boolean tryAddStringBuilderConsumer(
      FieldDto result, String fieldName, TypeName fieldType) {
    if (shouldGenerateStringBuilderConsumer(fieldType)) {
      String transform =
          isOptionalString(fieldType) ? "Optional.of(builder.toString())" : "builder.toString()";
      result.addMethod(createStringBuilderConsumer(fieldName, transform));
      return true;
    }
    return false;
  }

  /** Tries to add List-specific consumer methods. Returns true if handled. */
  private static boolean tryAddListConsumer(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      ProcessingContext context) {
    if (!(isList(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1)) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getInnerTypeArguments().get(0);

    // Get the TypeMirror of the element type from the parameter's type
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror, context);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    if (elementBuilderType.isPresent()) {
      // Element type has a builder - use ArrayListBuilderWithElementBuilders
      TypeName builderType =
          new TypeNameGeneric(
              map2TypeName(ArrayListBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      result.addMethod(
          createFieldConsumerWithElementBuilders(fieldName, builderType, elementBuilderType.get()));
    } else {
      // Regular ArrayListBuilder
      TypeName builderType = map2TypeName(ArrayListBuilder.class);
      result.addMethod(createFieldConsumerWithBuilder(fieldName, builderType, elementType));
    }
    return true;
  }

  /** Tries to add Map-specific consumer methods. Returns true if handled. */
  private static boolean tryAddMapConsumer(FieldDto result, String fieldName, TypeName fieldType) {
    if (!(isMap(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 2)) {
      return false;
    }

    TypeName builderTargetTypeName =
        new TypeNameGeneric(
            map2TypeName(HashMapBuilder.class),
            fieldTypeGeneric.getInnerTypeArguments().get(0),
            fieldTypeGeneric.getInnerTypeArguments().get(1));
    MethodDto mapConsumerWithBuilder =
        BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderTargetTypeName);
    result.addMethod(mapConsumerWithBuilder);
    return true;
  }

  /** Tries to add Set-specific consumer methods. Returns true if handled. */
  private static boolean tryAddSetConsumer(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      ProcessingContext context) {
    if (!(isSet(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1)) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getInnerTypeArguments().get(0);

    // Get the TypeMirror of the element type from the parameter's type
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror, context);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    if (elementBuilderType.isPresent()) {
      // Element type has a builder - use HashSetBuilderWithElementBuilders
      TypeName builderType =
          new TypeNameGeneric(
              map2TypeName(HashSetBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      result.addMethod(
          createFieldConsumerWithElementBuilders(fieldName, builderType, elementBuilderType.get()));
    } else {
      // Regular HashSetBuilder
      TypeName builderType = map2TypeName(HashSetBuilder.class);
      result.addMethod(createFieldConsumerWithBuilder(fieldName, builderType, elementType));
    }
    return true;
  }

  private static void addSupplierMethodsForField(
      FieldDto result, String fieldName, TypeName fieldType, TypeElement fieldTypeElement) {
    // Skip supplier generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }

    // For all fields including Optional<T>, use the real field type for suppliers
    result.addMethod(createFieldSupplier(fieldName, fieldType));
  }

  private static Optional<FieldDto> createFieldFromSetter(
      ExecutableElement mth, ProcessingContext context) {
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

    return createFieldDto(fieldName, javaDoc, fieldParameter, dtoType, context);
  }

  /**
   * Creates a FieldDto from a constructor parameter, including a simple builder setter to supply
   * the constructor argument.
   */
  private static Optional<FieldDto> createFieldFromConstructor(
      TypeElement dtoType, VariableElement param, ProcessingContext context) {
    String fieldName = param.getSimpleName().toString();
    // Set javadoc (default to field name if no javadoc found)
    String javaDoc = JavaLangAnalyser.extractParamJavaDoc(context.getDocComment(dtoType), param);
    if (javaDoc == null) {
      javaDoc = fieldName;
    }
    return createFieldDto(fieldName, javaDoc, param, dtoType, context);
  }

  /**
   * Common method to create a FieldDto with all builder methods (setter, supplier, consumer,
   * helpers).
   *
   * @param fieldName the name of the field
   * @param javaDoc the javadoc for the field
   * @param param the parameter element (from constructor or setter)
   * @param dtoType the DTO type containing this field
   * @param context processing context
   * @return Optional containing the FieldDto, or empty if field cannot be created
   */
  private static Optional<FieldDto> createFieldDto(
      String fieldName,
      String javaDoc,
      VariableElement param,
      TypeElement dtoType,
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
    field.setFieldName(fieldName);
    field.setFieldType(fieldType);
    field.setJavaDoc(javaDoc);

    // Find matching getter on the DTO type
    JavaLangAnalyser.findGetterForField(dtoType, fieldName, fieldTypeMirror, context)
        .ifPresent(getter -> field.setGetterName(getter.getSimpleName().toString()));

    // Add basic setter method
    field.addMethod(createFieldSetterWithTransform(fieldName, null, fieldType));

    // Add consumer/supplier/helper methods
    addConsumerMethodsForField(field, fieldName, fieldType, param, fieldTypeElement, context);
    addSupplierMethodsForField(field, fieldName, fieldType, fieldTypeElement);
    addAdditionalHelperMethodsForField(field, fieldName, fieldType);

    return Optional.of(field);
  }

  private static MethodDto createFieldSetterWithTransform(
      String fieldName, String transform, TypeName fieldType) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(fieldType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.PROXY);
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
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_DTO_METHOD_PARAMS, params);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    return methodDto;
  }

  private static MethodDto createFieldConsumer(String fieldName, TypeName fieldType) {
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER);
    methodDto.setCode(
        """
        $helperType:T consumer = this.$fieldName:N.isSet() ? this.$fieldName:N.value() : new $helperType:T();
        $dtoMethodParam:N.accept(consumer);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(consumer);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, fieldType);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    return methodDto;
  }

  private static MethodDto createStringBuilderConsumer(String fieldName, String transform) {
    TypeName stringBuilderType = new TypeName("java.lang", "StringBuilder");
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), stringBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + "StringBuilderConsumer");
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER);
    methodDto.setCode(
        """
        StringBuilder builder = new StringBuilder();
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument("transform", transform);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    return methodDto;
  }

  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName, TypeName builderType, TypeName builderTargetType) {
    TypeNameGeneric builderTypeGeneric = new TypeNameGeneric(builderType, builderTargetType);
    return BuilderDefinitionCreator.createFieldConsumerWithBuilder(fieldName, builderTypeGeneric);
  }

  private static MethodDto createFieldConsumerWithBuilder(String fieldName, TypeName builderType) {
    return createFieldConsumerWithBuilder(
        fieldName, builderType, "this.$fieldName:N.value()", "", Map.of());
  }

  /**
   * Creates a consumer method for collection builders with element builders. Used for
   * ArrayListBuilderWithElementBuilders and HashSetBuilderWithElementBuilders.
   */
  private static MethodDto createFieldConsumerWithElementBuilders(
      String fieldName, TypeName collectionBuilderType, TypeName elementBuilderType) {
    return createFieldConsumerWithBuilder(
        fieldName,
        collectionBuilderType,
        "this.$fieldName:N.value(), $elementBuilderType:T::create",
        "$elementBuilderType:T::create",
        Map.of("elementBuilderType", elementBuilderType));
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
   */
  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName,
      TypeName builderType,
      String constructorArgsWithValue,
      String constructorArgsEmpty,
      Map<String, TypeName> additionalArguments) {
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), builderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.CONSUMER_BY_BUILDER);
    methodDto.setCode(
        """
        $helperType:T builder = this.$fieldName:N.isSet() ? new $helperType:T(%s) : new $helperType:T(%s);
        $dtoMethodParam:N.accept(builder);
        this.$fieldName:N = $builderFieldWrapper:T.changedValue(builder.build());
        return this;
        """
            .formatted(constructorArgsWithValue, constructorArgsEmpty));
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, builderType);
    additionalArguments.forEach(methodDto::addArgument);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    return methodDto;
  }

  private static MethodDto createFieldSupplier(String fieldName, TypeName fieldType) {
    TypeNameGeneric supplierType = new TypeNameGeneric(map2TypeName(Supplier.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_SUPPLIER);
    parameter.setParameterTypeName(supplierType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(parameter);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.SUPPLIER);
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($dtoMethodParam:N.get());
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    return methodDto;
  }

  private static MethodDto createStringFormatMethodWithTransform(
      String fieldName, String transform) {
    TypeName stringType = new TypeName("java.lang", "String");

    MethodParameterDto formatParam = new MethodParameterDto();
    formatParam.setParameterName("format");
    formatParam.setParameterTypeName(stringType);

    MethodParameterDto argsParam = new MethodParameterDto();
    argsParam.setParameterName("args");
    argsParam.setParameterTypeName(new TypeNameArray(TypeName.of(Object.class), false));

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.addParameter(formatParam);
    methodDto.addParameter(argsParam);
    methodDto.setModifier(Modifier.PUBLIC);
    methodDto.setMethodType(MethodTypes.PROXY);
    methodDto.setCode(
        """
        this.$fieldName:N = $builderFieldWrapper:T.changedValue($transform:N);
        return this;
        """);
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument("transform", transform);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
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
    context.debug(
        "  -> Found @SimpleBuilder on type %s.%s, will use %sBuilder",
        packageName, simpleClassName, simpleClassName);
    return Optional.of(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
  }

  /**
   * Extracts the first type argument from a parameterized type mirror. For example, from
   * List&lt;Task&gt;, this extracts the Task TypeMirror.
   *
   * @param typeMirror the parameterized type mirror
   * @param context processing context
   * @return the first type argument, or null if not a parameterized type
   */
  private static TypeMirror extractFirstTypeArgument(
      TypeMirror typeMirror, ProcessingContext context) {
    if (typeMirror instanceof javax.lang.model.type.DeclaredType declaredType) {
      List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
      if (!typeArguments.isEmpty()) {
        return typeArguments.get(0);
      }
    }
    return null;
  }
}
