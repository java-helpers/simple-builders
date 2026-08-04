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

package org.javahelpers.simple.builders.processor.processing;

import static java.util.stream.Collectors.toSet;
import static org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser.*;
import static org.javahelpers.simple.builders.processor.analysis.JavaLangMapper.map2MethodParameter;
import static org.javahelpers.simple.builders.processor.processing.AnnotationValidator.validateAnnotatedElement;

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
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.util.TrackedValue;
import org.javahelpers.simple.builders.processor.analysis.FieldAnnotationExtractor;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.ClassFieldDto;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;

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

    context.debugStartOperation(
        "Extracting builder definition from: %s", annotatedType.getQualifiedName());

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
    context.getGeneratorRegistry().enhanceBuilder(result, result.getBuildingTargetTypeName());

    // Finalize the definition - convert to generic class representation
    finalizeDefinition(result, context);

    context.debug("Builder will be generated as: %s", result.getBuilderTypeName().getClassName());

    context.debugEndOperation(
        "Builder definition extracted: %s", result.getBuilderTypeName().getClassName());

    return result;
  }

  /**
   * Finalizes the builder definition by converting builder-specific data into generic class
   * generation data.
   *
   * <p>This step:
   *
   * <ul>
   *   <li>Converts FieldDto instances to ClassFieldDto instances
   *   <li>Sets origin info (sourceFieldName, constructorField) on each BuilderMethodDto
   *   <li>Performs pre-conflict resolution at BuilderMethodDto level with origin logging
   *   <li>Maps all BuilderMethodDto to MethodDto via BuilderToGenerationTypeMapper
   *   <li>Collects all mapped methods from fields and class-level enhancer methods
   *   <li>Sets class access modifier
   *   <li>Sets static imports for TrackedValue
   * </ul>
   *
   * @param builderDto the builder definition to finalize
   * @param context the processing context
   */
  private static void finalizeDefinition(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    context.debugStartOperation("Finalizing builder definition");

    // 1. Convert FieldDto → ClassFieldDto
    for (FieldDto field : builderDto.getAllFieldsForBuilder()) {
      ClassFieldDto classField = convertToClassField(field);
      builderDto.addClassField(classField);
    }

    // 2. Set origin info on BuilderMethodDto for javadoc enrichment
    setConstructorOriginInfo(builderDto);
    addSetterOriginInfo(builderDto);

    // 3. Builder-specific conflict resolution: resolve by priority, log with field origin
    resolveMethodConflicts(builderDto, context);

    // 4. Set class access modifier
    builderDto.setClassAccessModifier(builderDto.getConfiguration().getBuilderAccess());

    // 5. Set static imports for TrackedValue
    builderDto.addStaticImport(TrackedValue.class, "changedValue");
    builderDto.addStaticImport(TrackedValue.class, "initialValue");
    builderDto.addStaticImport(TrackedValue.class, "unsetValue");

    context.debugEndOperation(
        "Finalized: %d class fields, %d builder-level methods, %d constructors",
        builderDto.getClassFields().size(),
        builderDto.getMethods().size(),
        builderDto.getConstructors().size());
  }

  /**
   * Sets origin info (source field name, source description, constructor flag) on all {@link
   * BuilderMethodDto}s associated with constructor fields.
   *
   * <p>For constructor parameters, the full constructor signature is built once and shared across
   * all constructor-field methods. The origin line includes an {@code {@link}} tag with types-only
   * link target and full display label.
   *
   * @param builderDto the builder definition containing constructor fields
   */
  private static void setConstructorOriginInfo(BuilderDefinitionDto builderDto) {
    String sourceDescription = buildConstructorSourceDescription(builderDto);
    for (FieldDto field : builderDto.getConstructorFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        method.setSourceFieldName(field.getOriginalFieldName());
        method.setSourceDescription(sourceDescription);
        method.setConstructorField(true);
      }
    }
  }

  /**
   * Sets origin info (source field name, source description, constructor flag) on all {@link
   * BuilderMethodDto}s associated with setter fields.
   *
   * <p>For each setter field, the signature is built per-field and the origin line includes an
   * {@code {@link}} tag with types-only link target and full display label.
   *
   * @param builderDto the builder definition containing setter fields
   */
  private static void addSetterOriginInfo(BuilderDefinitionDto builderDto) {
    String sourceClassName =
        builderDto.getBuildingTargetTypeName() != null
            ? builderDto.getBuildingTargetTypeName().getClassName()
            : null;
    for (FieldDto field : builderDto.getSetterFieldsForBuilder()) {
      String sourceDescription = buildSetterSourceDescription(field, sourceClassName);
      for (BuilderMethodDto method : field.getMethods()) {
        method.setSourceFieldName(field.getOriginalFieldName());
        method.setSourceDescription(sourceDescription);
        method.setConstructorField(false);
      }
    }
  }

  /**
   * Builds the javadoc origin line for constructor-based methods.
   *
   * <p>The full constructor signature is built from all constructor fields, e.g. {@code
   * <p>Generated from parameter in constructor {@link PersonDto#PersonDto(String, int)
   * PersonDto(String name, int age)}}.
   *
   * @param builderDto the builder definition containing constructor fields and target type name
   * @return the complete origin line, or {@code null} if no constructor fields or target type
   */
  private static String buildConstructorSourceDescription(BuilderDefinitionDto builderDto) {
    if (builderDto.getConstructorFieldsForBuilder().isEmpty()
        || builderDto.getBuildingTargetTypeName() == null) {
      return null;
    }
    String className = builderDto.getBuildingTargetTypeName().getClassName();
    String displayParams =
        builderDto.getConstructorFieldsForBuilder().stream()
            .map(
                f ->
                    "%s %s"
                        .formatted(
                            f.getFieldType().getSimpleNameWithGenerics(), f.getOriginalFieldName()))
            .collect(java.util.stream.Collectors.joining(", "));
    String linkParams =
        builderDto.getConstructorFieldsForBuilder().stream()
            .map(f -> f.getFieldType().getClassName())
            .collect(java.util.stream.Collectors.joining(", "));
    return "<p>Generated from parameter in constructor {@link %s#%s(%s) %s(%s)}"
        .formatted(className, className, linkParams, className, displayParams);
  }

  /**
   * Builds the javadoc origin line for setter-based methods.
   *
   * <p>The signature is built from the field's setter name and type, e.g. {@code <p>Generated from
   * setter {@link PersonDto#setAuthor(String) setAuthor(String author)}}.
   *
   * @param field the setter field to build the description for
   * @param sourceClassName the simple class name of the source DTO, or {@code null} if unknown
   * @return the complete origin line
   */
  private static String buildSetterSourceDescription(FieldDto field, String sourceClassName) {
    String displaySignature =
        "%s(%s %s)"
            .formatted(
                field.getSetterName(),
                field.getFieldType().getSimpleNameWithGenerics(),
                field.getOriginalFieldName());
    String linkSignature =
        "%s(%s)".formatted(field.getSetterName(), field.getFieldType().getClassName());
    if (sourceClassName != null) {
      return "<p>Generated from setter {@link %s#%s %s}"
          .formatted(sourceClassName, linkSignature, displaySignature);
    } else {
      return "<p>Generated from setter <code>%s</code>".formatted(displaySignature);
    }
  }

  /**
   * Builder-specific conflict resolution at the BuilderMethodDto level, using priority-based
   * resolution with field-origin logging.
   *
   * <p>This is the primary place for conflict resolution because only here the field-origin
   * metadata ({@code sourceFieldName}, {@code constructorField}) is available. Methods with the
   * same signature key are resolved by keeping the highest-priority one; losers are removed from
   * their field/class method lists so they are never mapped to the rendering DTO.
   *
   * <p>A generic safety net in {@link
   * org.javahelpers.simple.builders.processor.classgen.roaster.RoasterCodeGenerator#resolveMethodConflicts}
   * handles any remaining duplicates (e.g., from enhancer methods added after this step) by keeping
   * the first occurrence.
   *
   * @param builderDto the builder definition containing all methods
   * @param context the processing context for logging
   */
  private static void resolveMethodConflicts(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    context.debugStartOperation("Resolving method conflicts");

    // Collect all BuilderMethodDto instances grouped by signature key
    java.util.Map<String, List<BuilderMethodDto>> methodsBySignature =
        collectMethodsBySignature(builderDto);

    // Resolve conflicts: keep highest priority, remove losers
    java.util.Set<BuilderMethodDto> methodsToRemove = new java.util.HashSet<>();
    for (java.util.Map.Entry<String, List<BuilderMethodDto>> entry :
        methodsBySignature.entrySet()) {
      List<BuilderMethodDto> methodsWithSameSignature = entry.getValue();
      boolean isConflicting = methodsWithSameSignature.size() > 1;
      if (isConflicting) {
        // Sort by priority (descending), then ordering, then name, etc. for deterministic winner
        methodsWithSameSignature.sort(new BuilderMethodDto.BuilderMethodComparator());
        BuilderMethodDto winner = methodsWithSameSignature.get(0);
        context.warning(
            "Method conflict resolved for signature '%s': %d methods found. "
                + "Kept: priority=%d, sourceField='%s', constructorField=%b. "
                + "Dropped: %s",
            entry.getKey(),
            methodsWithSameSignature.size(),
            winner.getPriority(),
            winner.getSourceFieldName(),
            winner.isConstructorField(),
            methodsWithSameSignature.stream()
                .skip(1)
                .map(
                    m ->
                        String.format(
                            "[priority=%d, sourceField='%s', constructorField=%b]",
                            m.getPriority(), m.getSourceFieldName(), m.isConstructorField()))
                .toList());
        // Mark losers for removal
        methodsWithSameSignature.stream().skip(1).forEach(methodsToRemove::add);
      }
    }

    // Remove losing methods from their field/class method lists
    removeMethodsFromBuilder(builderDto, methodsToRemove);

    context.debugEndOperation(
        "Resolved: %d signatures, %d conflicts, %d methods removed",
        methodsBySignature.size(),
        methodsBySignature.values().stream().filter(l -> l.size() > 1).count(),
        methodsToRemove.size());
  }

  /**
   * Collects all {@link BuilderMethodDto} instances from the builder definition, grouped by their
   * signature key. This includes methods from constructor fields, setter fields, and builder-level
   * enhancer methods.
   *
   * @param builderDto the builder definition containing all methods
   * @return map from signature key to list of methods with that signature
   */
  private static java.util.Map<String, List<BuilderMethodDto>> collectMethodsBySignature(
      BuilderDefinitionDto builderDto) {
    java.util.Map<String, List<BuilderMethodDto>> methodsBySignature = new HashMap<>();

    for (FieldDto field : builderDto.getConstructorFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        methodsBySignature
            .computeIfAbsent(method.getSignatureKey(), k -> new java.util.ArrayList<>())
            .add(method);
      }
    }
    for (FieldDto field : builderDto.getSetterFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        methodsBySignature
            .computeIfAbsent(method.getSignatureKey(), k -> new java.util.ArrayList<>())
            .add(method);
      }
    }
    for (BuilderMethodDto classMethod : builderDto.getMethods()) {
      methodsBySignature
          .computeIfAbsent(classMethod.getSignatureKey(), k -> new java.util.ArrayList<>())
          .add(classMethod);
    }

    return methodsBySignature;
  }

  /**
   * Removes the given methods from all field method lists and builder-level methods in the builder
   * definition.
   *
   * @param builderDto the builder definition containing all methods
   * @param methodsToRemove the set of methods to remove
   */
  private static void removeMethodsFromBuilder(
      BuilderDefinitionDto builderDto, java.util.Set<BuilderMethodDto> methodsToRemove) {
    for (FieldDto field : builderDto.getConstructorFieldsForBuilder()) {
      field.getMethods().removeAll(methodsToRemove);
    }
    for (FieldDto field : builderDto.getSetterFieldsForBuilder()) {
      field.getMethods().removeAll(methodsToRemove);
    }
    builderDto.getMethods().removeAll(methodsToRemove);
  }

  /**
   * Converts a FieldDto to a ClassFieldDto for rendering.
   *
   * @param field the field DTO to convert
   * @return the class field DTO
   */
  private static ClassFieldDto convertToClassField(FieldDto field) {
    ClassFieldDto classField = new ClassFieldDto();
    classField.setFieldName(field.getFieldNameInBuilder());

    // Build the field type: TrackedValue<FieldType>
    TypeName trackedValueType =
        new TypeName("org.javahelpers.simple.builders.core.util", "TrackedValue");
    TypeName wrappedFieldType =
        new TypeNameGeneric(trackedValueType, List.of(field.getFieldType()));
    classField.setFieldType(wrappedFieldType);

    classField.setVisibility(AccessModifier.PRIVATE);
    classField.setLiteralInitializer("unsetValue()");
    classField.setJavadoc(field.getJavaDoc());

    // Add field type imports
    classField.addImport(new TypeName("org.javahelpers.simple.builders.core.util", "TrackedValue"));
    // Add the field type for import
    classField.addImport(field.getFieldType());

    return classField;
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
      context.debugStartOperation(
          "Analyzing constructor with %d parameter(s)", ctor.getParameters().size());

      TypeName builderType =
          MethodGeneratorUtil.createGenericTypeName(
              builderDef.getBuilderTypeName(), builderDef.getGenerics());

      for (VariableElement param : ctor.getParameters()) {
        context.debugStartOperation("Analyzing parameter: %s", param.getSimpleName());
        Optional<FieldDto> fieldFromCtor =
            createFieldFromConstructor(
                annotatedType, param, builderType, context, fieldNameRegistry);
        if (fieldFromCtor.isPresent()) {
          FieldDto field = fieldFromCtor.get();
          logFieldAddition(field, context);
          constructorFields.add(field);
        }
      }

      context.debugEndOperation();
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
    context.debugStartOperation("Analysing setters for finding fields");
    List<FieldDto> setterFields = new LinkedList<>();

    // Build a set of constructor field names to avoid duplicates from setters
    Set<String> ctorFieldNames =
        result.getConstructorFieldsForBuilder().stream()
            .map(FieldDto::getFieldNameInBuilder)
            .collect(toSet());

    List<ExecutableElement> methods = findAllPossibleSettersOfClass(annotatedType, context);
    int processedCount = 0;
    int addedCount = 0;
    int skippedCount = 0;

    TypeName builderType =
        MethodGeneratorUtil.createGenericTypeName(
            result.getBuilderTypeName(), result.getGenerics());

    for (ExecutableElement mth : methods) {
      context.debugStartOperation("Analyzing method: %s", mth.toString());

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

    if (addedCount != 0 || skippedCount != 0) {
      context.debugEndOperation(
          "Processed %d possible setters: added %d fields, skipped %d",
          processedCount, addedCount, skippedCount);
    } else {
      context.debugEndOperation("No setters found");
    }

    return setterFields;
  }

  /** Logs the addition of a field with its type information. */
  private static void logFieldAddition(FieldDto field, ProcessingContext context) {
    String fieldTypeName = field.getFieldType().getClassName();
    if (field.getFieldType().getPackageName() != null
        && !field.getFieldType().getPackageName().isEmpty()) {
      fieldTypeName = field.getFieldType().getPackageName() + "." + fieldTypeName;
    }
    context.debugEndOperation(
        "Adding field: %s (type: %s)", field.getFieldNameInBuilder(), fieldTypeName);
  }

  private static boolean isMethodRelevantForBuilder(
      ExecutableElement mth, ProcessingContext context) {
    if (!hasNoThrowablesDeclared(mth)) {
      context.debug("Skipping: declares throwables");
      return false;
    }
    if (!hasNoReturnValue(mth)) {
      context.debug("Skipping: has return value");
      return false;
    }
    if (!hasNotAnnotation(IgnoreInBuilder.class, mth)) {
      context.debug("Skipping: has @IgnoreInBuilder annotation");
      return false;
    }
    if (!isNotPrivate(mth)) {
      context.debug("Skipping: is private");
      return false;
    }
    if (!isNotStatic(mth)) {
      context.debug("Skipping: is static");
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
    String javaDocDescription = JavaLangAnalyser.extractParamJavaDoc(fullJavaDoc, fieldParameter);

    // Check for field name conflicts and rename if necessary
    String finalFieldName =
        resolveFieldNameConflict(fieldName, fieldParameter, fieldNameRegistry, context);

    // Pass both original field name (for methods) and final field name (for builder field)
    Optional<FieldDto> result =
        createFieldDto(
            fieldName,
            finalFieldName,
            javaDocDescription,
            fieldParameter,
            dtoType,
            builderType,
            context);

    // If no default was found on the setter parameter, check the field element itself
    // (annotations like @Default may be placed on the field rather than the setter param)
    if (result.isPresent() && result.get().getDefaultValue().isEmpty()) {
      findFieldElement(dtoTypeElement, fieldName)
          .ifPresent(
              fieldElement ->
                  FieldAnnotationExtractor.extractDefaultValue(fieldElement)
                      .ifPresent(
                          rawDefault ->
                              result
                                  .get()
                                  .setDefaultValue(
                                      FieldAnnotationExtractor.formatDefaultExpression(
                                          rawDefault, result.get().getFieldType()))));
    }

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
    // Extract javadoc from constructor parameter (if present)
    String javaDocDescription =
        JavaLangAnalyser.extractParamJavaDoc(context.getDocComment(annotatedType), param);

    // Convert TypeElement to TypeName once
    TypeName dtoType = JavaLangMapper.map2TypeName(annotatedType, context);

    // Check for field name conflicts and rename if necessary
    String finalFieldName = resolveFieldNameConflict(fieldName, param, fieldNameRegistry, context);

    // Pass both original field name (for methods) and final field name (for builder field)
    Optional<FieldDto> result =
        createFieldDto(
            fieldName, finalFieldName, javaDocDescription, param, dtoType, builderType, context);

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
      String javaDocDescription,
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
    field.setFieldNameInBuilder(
        fieldNameInBuilder); // Use renamed field name for builder field storage
    field.setOriginalFieldName(fieldName);
    field.setFieldType(fieldType);

    // Store original javadoc description for reuse in builder method javadocs
    field.setOriginalJavaDocDescription(StringUtils.trimToNull(javaDocDescription));

    // Create javadoc for the tracked value field in the builder
    String javaDocDescriptionOrFieldname = field.getJavaDocDescriptionOrFieldName();
    JavadocDto trackedValueJavadoc =
        new JavadocDto(
            "Tracked value for <code>%s</code>: %s.",
            fieldNameInBuilder, javaDocDescriptionOrFieldname);
    field.setJavaDoc(trackedValueJavadoc);

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

    // Extract default value from @Default or @DefaultValue annotation (if present)
    FieldAnnotationExtractor.extractDefaultValue(param)
        .ifPresent(
            rawDefault ->
                field.setDefaultValue(
                    FieldAnnotationExtractor.formatDefaultExpression(rawDefault, fieldType)));

    // Builder and constructor information is now set when TypeName is created in JavaLangMapper

    // Use GeneratorRegistry to generate all methods for this field
    List<BuilderMethodDto> generatedMethods =
        context.getGeneratorRegistry().generateAllMethods(field, dtoType, builderType);
    generatedMethods.forEach(field::addMethod);

    return Optional.of(field);
  }

  /**
   * Finds a field element by name in the given class element.
   *
   * @param classElement the class to search in
   * @param fieldName the simple field name to look for
   * @return an {@link Optional} containing the field element, or empty if not found
   */
  private static Optional<VariableElement> findFieldElement(
      TypeElement classElement, String fieldName) {
    return classElement.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.FIELD)
        .filter(e -> e.getSimpleName().contentEquals(fieldName))
        .map(VariableElement.class::cast)
        .findFirst();
  }
}
