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
import static org.javahelpers.simple.builders.processor.util.AnnotationValidator.validateAnnotatedElement;
import static org.javahelpers.simple.builders.processor.util.JavaLangAnalyser.*;
import static org.javahelpers.simple.builders.processor.util.JavaLangMapper.map2MethodParameter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.generators.MethodGeneratorUtil;

/** Class for creating a specific BuilderDefinitionDto for an annotated DTO class. */
public class BuilderDefinitionCreator {

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

    // Apply builder enhancers (including With interface generation)
    context.getBuilderEnhancerRegistry().enhanceBuilder(result, result.getBuildingTargetTypeName());

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
      TypeName builderType =
          MethodGeneratorUtil.createGenericTypeName(
              builderDef.getBuilderTypeName(), builderDef.getGenerics());

      for (VariableElement param : ctor.getParameters()) {
        Optional<FieldDto> fieldFromCtor =
            createFieldFromConstructor(
                annotatedType, param, builderType, context, fieldNameRegistry);
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

    TypeName builderType =
        MethodGeneratorUtil.createGenericTypeName(
            result.getBuilderTypeName(), result.getGenerics());

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
            createFieldFromSetter(mth, builderType, context, fieldNameRegistry);
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
    TypeElement dtoTypeElement = (TypeElement) mth.getEnclosingElement();
    TypeName dtoType = JavaLangMapper.map2TypeName(dtoTypeElement, context);

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

    // Convert TypeElement to TypeName once
    TypeName dtoType = JavaLangMapper.map2TypeName(annotatedType, context);

    // Check for field name conflicts and rename if necessary
    String finalFieldName = resolveFieldNameConflict(fieldName, param, fieldNameRegistry, context);

    // Pass both original field name (for methods) and final field name (for builder field)
    Optional<FieldDto> result =
        createFieldDto(fieldName, finalFieldName, javaDoc, param, dtoType, builderType, context);

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
        """
          Builder field conflict: field '%s' (type %s) renamed to '%s' in builder to avoid conflict with existing field (type %s). \
        The reason could be having helperfunctions in the DTO or a mistake in the DTO (e.g., two setters with the same name but different field types). \
        Please check it and if the DTO is correct, you can get rid of this warning by setting the IgnoreInBuilder annotation on one of the setters for this field.\
        """,
        fieldName, newTypeName, renamedFieldName, existingTypeName);

    return renamedFieldName;
  }

  /**
   * Common method to create a FieldDto with all builder methods (setter, supplier, consumer,
   * helpers).
   *
   * <p>This method handles the complete creation of a FieldDto including:
   *
   * <ul>
   *   <li>Parameter type mapping and validation
   *   <li>Field name resolution and conflict handling
   *   <li>Non-null constraint detection
   *   <li>Method generation via MethodGeneratorRegistry
   * </ul>
   *
   * @param fieldName the estimated field name (used for method names)
   * @param fieldNameInBuilder the builder field name (used for storage, may be renamed)
   * @param javaDoc the javadoc for the field
   * @param param the parameter element (from constructor or setter)
   * @param dtoType the DTO type containing this field
   * @param builderType the builder type (may include generic type parameters)
   * @param context processing context
   * @return Optional containing the FieldDto, or empty if field cannot be created
   */
  private static Optional<FieldDto> createFieldDto(
      String fieldName,
      String fieldNameInBuilder,
      String javaDoc,
      VariableElement param,
      TypeName dtoType,
      TypeName builderType,
      ProcessingContext context) {
    MethodParameterDto paramDto = map2MethodParameter(param, context);
    if (paramDto == null || dtoType == null) {
      return Optional.empty();
    }

    TypeName fieldType = paramDto.getParameterType();
    TypeMirror fieldTypeMirror = param.asType();

    FieldDto field = new FieldDto();
    field.setFieldName(fieldNameInBuilder); // Use renamed field name for builder field storage
    field.setFieldNameEstimated(fieldName);
    field.setFieldType(fieldType);
    field.setJavaDoc(javaDoc);

    // Note: setterName will be set explicitly by the caller before field renaming

    // Find matching getter on the DTO type using the builder field name
    TypeElement dtoTypeElement = context.getTypeElement(dtoType);
    JavaLangAnalyser.findGetterForField(
            dtoTypeElement, fieldNameInBuilder, fieldTypeMirror, context)
        .ifPresent(getter -> field.setGetterName(getter.getSimpleName().toString()));

    // Extract annotations from the field parameter
    List<AnnotationDto> annotations = FieldAnnotationExtractor.extractAnnotations(param, context);

    // Annotations could be assigned to Type or Parameter.
    // To avoid duplication in generated code, we need to remove the duplications here.
    annotations.removeAll(fieldType.getAnnotations());

    // Store parameter annotations in field for generators to access
    field.setParameterAnnotations(annotations);

    // Check if field has non-null constraint (annotation or primitive type)
    if (FieldAnnotationExtractor.hasNonNullConstraint(param)
        || fieldTypeMirror.getKind().isPrimitive()) {
      field.setNonNullable(true);
    }

    // Builder and constructor information is now set when TypeName is created in JavaLangMapper

    // Use MethodGeneratorRegistry to generate all methods for this field
    List<MethodDto> generatedMethods =
        context.getMethodGeneratorRegistry().generateAllMethods(field, dtoType, builderType);
    generatedMethods.forEach(field::addMethod);

    return Optional.of(field);
  }
}
