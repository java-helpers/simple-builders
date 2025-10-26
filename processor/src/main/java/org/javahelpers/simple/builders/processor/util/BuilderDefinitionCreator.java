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

    List<FieldDto> constructorFields = extractConstructorFields(annotatedType, result, context);
    result.addAllFieldsInConstructor(constructorFields);

    List<FieldDto> setterFields = extractSetterFields(annotatedType, result, context);
    result.addAllFields(setterFields);

    // Create the With interface
    NestedTypeDto withInterface = createWithInterface(result, context);
    result.addNestedType(withInterface);

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
      TypeElement annotatedType, BuilderDefinitionDto builderDef, ProcessingContext context) {
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
                annotatedType, param, builderDef.getBuilderTypeName(), context);
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
        Optional<FieldDto> maybeField =
            createFieldFromSetter(mth, result.getBuilderTypeName(), context);
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
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      List<AnnotationDto> annotations,
      TypeName builderType) {
    // Check for String type (not array) and add format method
    if (isString(fieldType) && !(fieldType instanceof TypeNameArray)) {
      result.addMethod(
          createStringFormatMethodWithTransform(
              fieldName, "String.format(format, args)", annotations, builderType));
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
              fieldName, "List.of(%s)", new TypeNameArray(innerTypes.get(0), false), builderType));
    } else if (isSet(fieldType) && innerTypesCnt == 1) {
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "Set.of(%s)", new TypeNameArray(innerTypes.get(0), true), builderType));
    } else if (isMap(fieldType) && innerTypesCnt == 2) {
      TypeName mapEntryType =
          new TypeNameArray(
              new TypeNameGeneric("java.util", "Map.Entry", innerTypes.get(0), innerTypes.get(1)),
              false);
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "Map.ofEntries(%s)", mapEntryType, builderType));
    } else if (isOptional(fieldType) && innerTypesCnt == 1) {
      // Add setter that accepts the inner type T and wraps it in Optional.ofNullable()
      result.addMethod(
          createFieldSetterWithTransform(
              fieldName, "Optional.ofNullable(%s)", innerTypes.get(0), builderType));

      // If Optional<String>, add format method
      TypeName innerType = innerTypes.get(0);
      if (isString(innerType)) {
        result.addMethod(
            createStringFormatMethodWithTransform(
                fieldName, "Optional.of(String.format(format, args))", List.of(), builderType));
      }
    }
  }

  private static void addConsumerMethodsForField(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      TypeElement fieldTypeElement,
      TypeName builderType,
      ProcessingContext context) {
    // Do not generate supplier methods for generic type variables (e.g., T)
    if (fieldType instanceof TypeNameVariable) {
      return;
    }
    // Skip consumer generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }

    if (!tryAddBuilderConsumer(result, fieldName, fieldParameter, builderType, context)
        && !tryAddFieldConsumer(
            result, fieldName, fieldType, fieldTypeElement, builderType, context)
        && !tryAddListConsumer(result, fieldName, fieldType, fieldParameter, builderType, context)
        && !tryAddMapConsumer(result, fieldName, fieldType, builderType)
        && !tryAddSetConsumer(result, fieldName, fieldType, fieldParameter, builderType, context)) {
      tryAddStringBuilderConsumer(result, fieldName, fieldType, builderType);
    }
  }

  /** Tries to add a direct builder-based consumer when the field type itself has a builder. */
  private static boolean tryAddBuilderConsumer(
      FieldDto result,
      String fieldName,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context) {
    Optional<TypeName> fieldBuilderOpt = resolveBuilderType(fieldParameter, context);
    if (fieldBuilderOpt.isPresent()) {
      TypeName fieldBuilderType = fieldBuilderOpt.get();
      result.addMethod(
          BuilderDefinitionCreator.createFieldConsumerWithBuilder(
              fieldName, fieldBuilderType, builderType));
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
      TypeName builderType,
      ProcessingContext context) {
    if (!isJavaClass(fieldType)
        && fieldTypeElement != null
        && fieldTypeElement.getKind() == javax.lang.model.element.ElementKind.CLASS
        && !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        && hasEmptyConstructor(fieldTypeElement, context)) {
      // Only generate a Consumer for concrete classes with an accessible empty constructor
      result.addMethod(createFieldConsumer(fieldName, fieldType, builderType));
      return true;
    }
    return false;
  }

  /** Tries to add StringBuilder-based consumer for String and Optional<String>. */
  private static boolean tryAddStringBuilderConsumer(
      FieldDto result, String fieldName, TypeName fieldType, TypeName builderType) {
    if (shouldGenerateStringBuilderConsumer(fieldType)) {
      String transform =
          isOptionalString(fieldType) ? "Optional.of(builder.toString())" : "builder.toString()";
      result.addMethod(createStringBuilderConsumer(fieldName, transform, builderType));
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
      TypeName builderType,
      ProcessingContext context) {
    if (!(isList(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1)) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getInnerTypeArguments().get(0);

    // Get the TypeMirror of the element type from the parameter's type
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    if (elementBuilderType.isPresent()) {
      // Element type has a builder - use ArrayListBuilderWithElementBuilders
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(ArrayListBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      result.addMethod(
          createFieldConsumerWithElementBuilders(
              fieldName, collectionBuilderType, elementBuilderType.get(), builderType));
    } else {
      // Regular ArrayListBuilder
      TypeName collectionBuilderType = map2TypeName(ArrayListBuilder.class);
      result.addMethod(
          createFieldConsumerWithBuilder(
              fieldName, collectionBuilderType, elementType, builderType));
    }
    return true;
  }

  /** Tries to add Map-specific consumer methods. Returns true if handled. */
  private static boolean tryAddMapConsumer(
      FieldDto result, String fieldName, TypeName fieldType, TypeName builderType) {
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
        BuilderDefinitionCreator.createFieldConsumerWithBuilder(
            fieldName, builderTargetTypeName, builderType);
    result.addMethod(mapConsumerWithBuilder);
    return true;
  }

  /** Tries to add Set-specific consumer methods. Returns true if handled. */
  private static boolean tryAddSetConsumer(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      VariableElement fieldParameter,
      TypeName builderType,
      ProcessingContext context) {
    if (!(isSet(fieldType)
        && fieldType instanceof TypeNameGeneric fieldTypeGeneric
        && fieldTypeGeneric.getInnerTypeArguments().size() == 1)) {
      return false;
    }

    TypeName elementType = fieldTypeGeneric.getInnerTypeArguments().get(0);

    // Get the TypeMirror of the element type from the parameter's type
    TypeMirror fieldTypeMirror = fieldParameter.asType();
    TypeMirror elementTypeMirror = extractFirstTypeArgument(fieldTypeMirror);

    Optional<TypeName> elementBuilderType =
        resolveBuilderType(elementType, elementTypeMirror, context);

    if (elementBuilderType.isPresent()) {
      // Element type has a builder - use HashSetBuilderWithElementBuilders
      TypeName collectionBuilderType =
          new TypeNameGeneric(
              map2TypeName(HashSetBuilderWithElementBuilders.class),
              elementType,
              elementBuilderType.get());
      result.addMethod(
          createFieldConsumerWithElementBuilders(
              fieldName, collectionBuilderType, elementBuilderType.get(), builderType));
    } else {
      // Regular HashSetBuilder
      TypeName collectionBuilderType = map2TypeName(HashSetBuilder.class);
      result.addMethod(
          createFieldConsumerWithBuilder(
              fieldName, collectionBuilderType, elementType, builderType));
    }
    return true;
  }

  private static void addSupplierMethodsForField(
      FieldDto result,
      String fieldName,
      TypeName fieldType,
      TypeElement fieldTypeElement,
      TypeName builderType) {
    // Skip supplier generation for functional interfaces
    if (isFunctionalInterface(fieldTypeElement)) {
      return;
    }
    // For all fields including Optional<T>, use the real field type for suppliers
    result.addMethod(createFieldSupplier(fieldName, fieldType, builderType));
  }

  private static Optional<FieldDto> createFieldFromSetter(
      ExecutableElement mth, TypeName builderType, ProcessingContext context) {
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

    return createFieldDto(fieldName, javaDoc, fieldParameter, dtoType, builderType, context);
  }

  /**
   * Creates a FieldDto from a constructor parameter, including a simple builder setter to supply
   * the constructor argument.
   */
  private static Optional<FieldDto> createFieldFromConstructor(
      TypeElement annotatedType,
      VariableElement param,
      TypeName builderType,
      ProcessingContext context) {
    String fieldName = param.getSimpleName().toString();
    // Set javadoc (default to field name if no javadoc found)
    String javaDoc =
        JavaLangAnalyser.extractParamJavaDoc(context.getDocComment(annotatedType), param);
    if (javaDoc == null) {
      javaDoc = fieldName;
    }
    return createFieldDto(fieldName, javaDoc, param, annotatedType, builderType, context);
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
    field.setFieldName(fieldName);
    field.setFieldType(fieldType);
    field.setJavaDoc(javaDoc);

    // Find matching getter on the DTO type
    JavaLangAnalyser.findGetterForField(dtoType, fieldName, fieldTypeMirror, context)
        .ifPresent(getter -> field.setGetterName(getter.getSimpleName().toString()));

    // Extract annotations from the field parameter
    List<AnnotationDto> annotations = FieldAnnotationExtractor.extractAnnotations(param, context);

    // Check if field has non-null constraint (annotation or primitive type)
    if (FieldAnnotationExtractor.hasNonNullConstraint(param)
        || fieldTypeMirror.getKind().isPrimitive()) {
      field.setNonNullable(true);
    }

    // Add basic setter method with annotations
    field.addMethod(
        createFieldSetterWithTransform(fieldName, null, fieldType, annotations, builderType));

    // Add consumer/supplier/helper methods
    addConsumerMethodsForField(
        field, fieldName, fieldType, param, fieldTypeElement, builderType, context);
    addSupplierMethodsForField(field, fieldName, fieldType, fieldTypeElement, builderType);
    addAdditionalHelperMethodsForField(field, fieldName, fieldType, annotations, builderType);

    return Optional.of(field);
  }

  /**
   * Creates a field setter method with optional transform, without annotations.
   *
   * @param fieldName the name of the field
   * @param transform optional transform expression (e.g., "Optional.of(%s)")
   * @param fieldType the type of the field
   * @return the method DTO for the setter
   */
  private static MethodDto createFieldSetterWithTransform(
      String fieldName, String transform, TypeName fieldType, TypeName builderType) {
    return createFieldSetterWithTransform(fieldName, transform, fieldType, List.of(), builderType);
  }

  /**
   * Creates a field setter method with optional transform and annotations.
   *
   * @param fieldName the name of the field
   * @param transform optional transform expression (e.g., "Optional.of(%s)")
   * @param fieldType the type of the field
   * @param annotations annotations to apply to the parameter
   * @return the method DTO for the setter
   */
  private static MethodDto createFieldSetterWithTransform(
      String fieldName,
      String transform,
      TypeName fieldType,
      List<AnnotationDto> annotations,
      TypeName builderType) {
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName);
    parameter.setParameterTypeName(fieldType);
    // Add annotations to the parameter
    annotations.forEach(parameter::addAnnotation);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.setReturnType(builderType);
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

  private static MethodDto createFieldConsumer(
      String fieldName, TypeName fieldType, TypeName builderType) {
    TypeNameGeneric consumerType = new TypeNameGeneric(map2TypeName(Consumer.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.setReturnType(builderType);
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

  private static MethodDto createStringBuilderConsumer(
      String fieldName, String transform, TypeName builderType) {
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
      String fieldName,
      TypeName consumerBuilderType,
      TypeName builderTargetType,
      TypeName returnBuilderType) {
    TypeNameGeneric builderTypeGeneric =
        new TypeNameGeneric(consumerBuilderType, builderTargetType);
    return BuilderDefinitionCreator.createFieldConsumerWithBuilder(
        fieldName, builderTypeGeneric, returnBuilderType);
  }

  private static MethodDto createFieldConsumerWithBuilder(
      String fieldName, TypeName consumerBuilderType, TypeName returnBuilderType) {
    return createFieldConsumerWithBuilder(
        fieldName,
        consumerBuilderType,
        "this.$fieldName:N.value()",
        "",
        Map.of(),
        returnBuilderType);
  }

  /**
   * Creates a consumer method for collection builders with element builders. Used for
   * ArrayListBuilderWithElementBuilders and HashSetBuilderWithElementBuilders.
   */
  private static MethodDto createFieldConsumerWithElementBuilders(
      String fieldName,
      TypeName collectionBuilderType,
      TypeName elementBuilderType,
      TypeName returnBuilderType) {
    return createFieldConsumerWithBuilder(
        fieldName,
        collectionBuilderType,
        "this.$fieldName:N.value(), $elementBuilderType:T::create",
        "$elementBuilderType:T::create",
        Map.of("elementBuilderType", elementBuilderType),
        returnBuilderType);
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
      TypeName consumerBuilderType,
      String constructorArgsWithValue,
      String additionalConstructorArgs,
      Map<String, TypeName> additionalArguments,
      TypeName returnBuilderType) {
    TypeNameGeneric consumerType =
        new TypeNameGeneric(map2TypeName(Consumer.class), consumerBuilderType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + BUILDER_SUFFIX + SUFFIX_CONSUMER);
    parameter.setParameterTypeName(consumerType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.setReturnType(returnBuilderType);
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
            .formatted(constructorArgsWithValue, additionalConstructorArgs));
    methodDto.addArgument(ARG_FIELD_NAME, fieldName);
    methodDto.addArgument(ARG_DTO_METHOD_PARAM, parameter.getParameterName());
    methodDto.addArgument(ARG_HELPER_TYPE, consumerBuilderType);
    additionalArguments.forEach(methodDto::addArgument);
    methodDto.addArgument(ARG_BUILDER_FIELD_WRAPPER, TRACKED_VALUE_TYPE);
    return methodDto;
  }

  private static MethodDto createFieldSupplier(
      String fieldName, TypeName fieldType, TypeName builderType) {
    TypeNameGeneric supplierType = new TypeNameGeneric(map2TypeName(Supplier.class), fieldType);
    MethodParameterDto parameter = new MethodParameterDto();
    parameter.setParameterName(fieldName + SUFFIX_SUPPLIER);
    parameter.setParameterTypeName(supplierType);
    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.setReturnType(builderType);
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
      String fieldName, String transform, List<AnnotationDto> annotations, TypeName builderType) {
    TypeName stringType = new TypeName("java.lang", "String");

    MethodParameterDto formatParam = new MethodParameterDto();
    formatParam.setParameterName("format");
    formatParam.setParameterTypeName(stringType);
    // Apply annotations to the format parameter (it's a String value)
    annotations.forEach(formatParam::addAnnotation);

    MethodParameterDto argsParam = new MethodParameterDto();
    argsParam.setParameterName("args");
    argsParam.setParameterTypeName(new TypeNameArray(TypeName.of(Object.class), false));

    MethodDto methodDto = new MethodDto();
    methodDto.setMethodName(fieldName);
    methodDto.setReturnType(builderType);
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
    TypeName consumerType =
        new TypeName(
            "java.util.function",
            "Consumer<" + builderDef.getBuilderTypeName().getClassName() + ">");
    parameter.setParameterTypeName(consumerType);
    method.addParameter(parameter);

    // Add implementation (cast this to the DTO type)
    method.setCode(
        """
        $builderType:T builder = new $builderType:T($dtoType:T.class.cast(this));
        b.accept(builder);
        return builder.build();
        """);
    method.addArgument("builderType", builderDef.getBuilderTypeName());
    method.addArgument("dtoType", builderDef.getBuildingTargetTypeName());

    method.setJavadoc(
        "Applies modifications to a builder initialized from this instance and returns the built object.\n\n"
            + "@param b the consumer to apply modifications\n"
            + "@return the modified instance");

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

    // Add implementation (cast this to the DTO type)
    method.setCode("return new $builderType:T($dtoType:T.class.cast(this));\n");
    method.addArgument("builderType", builderDef.getBuilderTypeName());
    method.addArgument("dtoType", builderDef.getBuildingTargetTypeName());

    method.setJavadoc(
        "Creates a builder initialized from this instance.\n\n"
            + "@return a builder initialized with this instance's values");

    return method;
  }
}
