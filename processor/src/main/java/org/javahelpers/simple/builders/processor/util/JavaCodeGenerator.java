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

import static javax.lang.model.element.Modifier.PUBLIC;
import static org.javahelpers.simple.builders.processor.util.JavapoetMapper.*;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.util.TrackedValue;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;

/** JavaCodeGenerator generates with BuilderDefinitionDto JavaCode for the builder. */
public class JavaCodeGenerator {
  /** Util class for source code generation of type {@code javax.annotation.processing.Filer}. */
  private final Filer filer;

  /** Logger for debug output during code generation. */
  private final ProcessingLogger logger;

  /** Format string for throwing exceptions with field context. */
  private static final String THROW_EXCEPTION_FORMAT = "throw new $T($S)";

  /**
   * Constructor for JavaCodeGenerator.
   *
   * @param filer Util class for source code generation of type {@code
   *     javax.annotation.processing.Filer}
   * @param logger Logger for debug output
   */
  public JavaCodeGenerator(Filer filer, ProcessingLogger logger) {
    this.filer = filer;
    this.logger = logger;
  }

  /**
   * Generating source code file for builder using Javapoet.
   *
   * @param builderDef dto of all information to create the builder
   * @throws BuilderException if there is an error in source code generation
   */
  public void generateBuilder(BuilderDefinitionDto builderDef) throws BuilderException {
    logger.debug(
        "Starting code generation for builder: %s", builderDef.getBuilderTypeName().getClassName());

    ClassName builderBaseClass = map2ClassName(builderDef.getBuilderTypeName());
    if (CollectionUtils.isNotEmpty(builderDef.getGenerics())) {
      logger.debug("Builder has %d generic type parameter(s)", builderDef.getGenerics().size());
    }

    TypeSpec.Builder classBuilder =
        TypeSpec.classBuilder(builderBaseClass)
            .addTypeVariables(map2TypeVariables(builderDef.getGenerics()));

    // Add class JavaDoc if provided by enhancer
    if (builderDef.getClassJavadoc() != null) {
      classBuilder.addJavadoc(builderDef.getClassJavadoc());
    }

    // Set builder class access level
    Modifier builderAccessModifier = map2Modifier(builderDef.getConfiguration().getBuilderAccess());
    if (builderAccessModifier != null) {
      classBuilder.addModifiers(builderAccessModifier);
    }

    // Adding interfaces from enhancers
    for (InterfaceName interfaceName : builderDef.getInterfaces()) {
      Optional<com.palantir.javapoet.TypeName> interfaceType =
          JavapoetMapper.mapInterfaceToTypeName(interfaceName);
      interfaceType.ifPresent(classBuilder::addSuperinterface);
    }

    logger.debug(
        "Generating %d constructor fields and %d setter fields",
        builderDef.getConstructorFieldsForBuilder().size(),
        builderDef.getSetterFieldsForBuilder().size());

    // Generate backing fields for each DTO field (constructor and setter fields)
    // Note: Builder field name conflicts are now resolved in BuilderDefinitionCreator
    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      FieldSpec fieldSpec = createFieldMember(fieldDto);
      classBuilder.addField(fieldSpec);
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      FieldSpec fieldSpec = createFieldMember(fieldDto);
      classBuilder.addField(fieldSpec);
    }

    // Collect all methods from all fields, setting javadoc and tracking field relationship
    Map<MethodDto, FieldDto> allMethods = new HashMap<>();

    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      for (MethodDto method : fieldDto.getMethods()) {
        allMethods.put(method, fieldDto);
      }
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      for (MethodDto method : fieldDto.getMethods()) {
        allMethods.put(method, fieldDto);
      }
    }

    // Add core methods to the collection
    for (MethodDto coreMethod : builderDef.getCoreMethods()) {
      allMethods.put(coreMethod, null); // Core methods don't have associated fields
    }

    // Resolve conflicts and sort by ordering
    List<MethodDto> resolvedMethods = resolveMethodConflicts(allMethods);
    logger.debug("  Resolved %d methods after conflict resolution", resolvedMethods.size());

    // Generate all methods in order
    for (MethodDto methodDto : resolvedMethods) {
      MethodSpec methodSpec = createMethod(methodDto);
      classBuilder.addMethod(methodSpec);
    }

    // Generate constructors
    generateConstructors(classBuilder, builderDef);

    // Adding nested types (e.g., With interface)
    for (NestedTypeDto nestedType : builderDef.getNestedTypes()) {
      TypeSpec nestedTypeSpec = createNestedType(nestedType);
      classBuilder.addType(nestedTypeSpec);
      logger.debug("  Generated nested type: %s", nestedType.getTypeName());
    }

    // Adding annotations from enhancers
    for (AnnotationDto annotation : builderDef.getClassAnnotations()) {
      Optional<AnnotationSpec> annotationSpec = map2AnnotationSpec(annotation);
      annotationSpec.ifPresent(classBuilder::addAnnotation);
    }

    logger.debug(
        "Writing builder class to file: %s.%s",
        builderDef.getBuilderTypeName().getPackageName(),
        builderDef.getBuilderTypeName().getClassName());
    writeBuilderClassToFile(builderDef.getBuilderTypeName().getPackageName(), classBuilder.build());
    logger.debug(
        "Successfully generated builder: %s", builderDef.getBuilderTypeName().getClassName());
  }

  private void writeBuilderClassToFile(String packageName, TypeSpec typeSpec)
      throws BuilderException {
    try {
      JavaFile.builder(packageName, typeSpec)
          .skipJavaLangImports(true)
          .addStaticImport(TrackedValue.class, "initialValue")
          .addStaticImport(TrackedValue.class, "changedValue")
          .addStaticImport(TrackedValue.class, "unsetValue")
          .build()
          .writeTo(filer);
    } catch (IOException ex) {
      throw new BuilderException(null, ex);
    }
  }

  private void writeSimpleClassToFile(String packageName, TypeSpec typeSpec)
      throws BuilderException {
    try {
      JavaFile.builder(packageName, typeSpec).skipJavaLangImports(true).build().writeTo(filer);
    } catch (IOException ex) {
      throw new BuilderException(null, ex);
    }
  }

  /**
   * Resolves method conflicts by keeping only the highest priority method for each signature and
   * sorts all methods by ordering then name. This prevents compilation errors when methods from
   * different fields have the same signature and ensures proper method generation order.
   *
   * @param methodToField mapping from method to its source field (null for core methods)
   * @return list of all methods with conflicts resolved, sorted by ordering for proper generation
   */
  private List<MethodDto> resolveMethodConflicts(Map<MethodDto, FieldDto> methodToField) {
    Map<String, MethodDto> signatureToMethod = new HashMap<>();

    // Process all methods and resolve conflicts
    for (Map.Entry<MethodDto, FieldDto> entry : methodToField.entrySet()) {
      MethodDto method = entry.getKey();
      FieldDto field = entry.getValue();
      String signature = method.getSignatureKey();

      MethodDto existing = signatureToMethod.get(signature);
      if (existing == null) {
        // No conflict, add the method
        signatureToMethod.put(signature, method);
      } else {
        // Conflict detected: keep the higher priority method
        String existingSource = getSourceDescription(existing, methodToField.get(existing));
        String newSource = getSourceDescription(method, field);

        if (method.getPriority() > existing.getPriority()) {
          // New method wins
          signatureToMethod.put(signature, method);
          logger.warning(
              "  Method conflict: '%s' from %s (priority %d) dropped in favor of %s (priority %d)",
              signature, existingSource, existing.getPriority(), newSource, method.getPriority());
        } else if (method.getPriority() < existing.getPriority()) {
          // Existing method wins
          logger.warning(
              "  Method conflict: '%s' from %s (priority %d) dropped in favor of %s (priority %d)",
              signature, newSource, method.getPriority(), existingSource, existing.getPriority());
        } else {
          // Equal priority - keep first
          logger.warning(
              "  Method conflict: '%s' from %s (priority %d) dropped in favor of %s (priority %d) - equal priority, keeping first",
              signature, newSource, method.getPriority(), existingSource, existing.getPriority());
        }
      }
    }

    // Sort methods using enhanced sorting logic
    return signatureToMethod.values().stream().sorted(new MethodDto.MethodComparator()).toList();
  }

  /**
   * Gets a description of the method source for logging purposes.
   *
   * @param method the method
   * @param field the associated field (null for core methods)
   * @return description of the method source
   */
  private String getSourceDescription(MethodDto method, FieldDto field) {
    if (field == null) {
      return "core method '" + method.getMethodName() + "'";
    } else {
      return "field '" + field.getFieldName() + "'";
    }
  }

  /**
   * Generates constructors for the builder class.
   *
   * @param classBuilder the TypeSpec.Builder to add constructors to
   * @param builderDef the builder definition containing field information
   */
  private void generateConstructors(
      TypeSpec.Builder classBuilder, BuilderDefinitionDto builderDef) {
    // Get access modifiers from configuration
    Modifier constructorAccessModifier =
        map2Modifier(builderDef.getConfiguration().getBuilderConstructorAccess());
    ClassName dtoBaseClass = map2ClassName(builderDef.getBuildingTargetTypeName());

    // Generate empty constructor
    MethodSpec emptyConstructor = createEmptyConstructor(dtoBaseClass, constructorAccessModifier);
    classBuilder.addMethod(emptyConstructor);

    // Generate constructor with instance
    com.palantir.javapoet.TypeName dtoTypeName =
        map2ParameterType(builderDef.getBuildingTargetTypeName());
    MethodSpec instanceConstructor =
        createConstructorWithInstance(
            dtoBaseClass,
            dtoTypeName,
            builderDef.getAllFieldsForBuilder(),
            constructorAccessModifier);
    classBuilder.addMethod(instanceConstructor);

    logger.debug("  Generated constructors for builder");
  }

  private MethodSpec createEmptyConstructor(ClassName dtoClass, Modifier accessModifier) {
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(accessModifier)
            .addJavadoc(
                """
            Empty constructor of builder for {@code $1N.$2T}.
            """,
                dtoClass.packageName(),
                dtoClass);
    return constructorBuilder.build();
  }

  private MethodSpec createConstructorWithInstance(
      ClassName dtoBaseClass,
      com.palantir.javapoet.TypeName dtoType,
      List<FieldDto> fields,
      Modifier accessModifier) {
    MethodSpec.Builder cb =
        MethodSpec.constructorBuilder()
            .addModifiers(accessModifier)
            .addParameter(dtoType, "instance")
            .addJavadoc(
                """
            Initialisation of builder for {@code $1N.$2T} by a instance.

            @param instance object instance for initialisiation
            """,
                dtoBaseClass.packageName(),
                dtoBaseClass);

    for (FieldDto f : fields) {
      f.getGetterName().ifPresent(getter -> addFieldInitializationWithValidation(cb, f, getter));
    }
    return cb.build();
  }

  private void addFieldInitializationWithValidation(
      MethodSpec.Builder cb, FieldDto field, String getter) {
    // Initialize field from source instance
    cb.addStatement(
        "this.$N = $T.initialValue(instance.$N())",
        field.getFieldName(),
        ClassName.get(TrackedValue.class),
        getter);

    // Validate non-nullable fields immediately - fail fast if source object is invalid
    if (field.isNonNullable()) {
      cb.beginControlFlow("if (this.$N.value() == null)", field.getFieldName())
          .addStatement(
              THROW_EXCEPTION_FORMAT,
              IllegalArgumentException.class,
              "Cannot initialize builder from instance: field '"
                  + field.getFieldName()
                  + "' is marked as non-null but source object has null value")
          .endControlFlow();
    }
  }

  private FieldSpec createFieldMember(FieldDto fieldDto) {
    com.palantir.javapoet.TypeName fieldType = map2ParameterType(fieldDto.getFieldType());
    if (fieldType.isPrimitive()) {
      fieldType = fieldType.box();
    }
    // Wrap all fields in TrackedValue<FieldType>
    ClassName builderFieldWrapper = ClassName.get(TrackedValue.class);
    ParameterizedTypeName wrappedFieldType =
        ParameterizedTypeName.get(builderFieldWrapper, fieldType);

    return FieldSpec.builder(wrappedFieldType, fieldDto.getFieldName(), Modifier.PRIVATE)
        .addJavadoc(
            "Tracked value for <code>$L</code>: $L.\n",
            fieldDto.getFieldName(),
            fieldDto.getJavaDoc())
        .initializer("$T.unsetValue()", builderFieldWrapper)
        .build();
  }

  private MethodSpec createMethod(MethodDto methodDto) {
    com.palantir.javapoet.TypeName returnType = map2ParameterType(methodDto.getReturnType());
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(methodDto.getMethodName()).returns(returnType);

    // Use modifier from MethodDto if present
    methodDto.getModifier().ifPresent(methodBuilder::addModifiers);

    // Add static modifier if method is static
    if (methodDto.isStatic()) {
      methodBuilder.addModifiers(javax.lang.model.element.Modifier.STATIC);
    }

    // Add generic type parameters if any
    if (CollectionUtils.isNotEmpty(methodDto.getGenericParameters())) {
      List<com.palantir.javapoet.TypeVariableName> typeVariables =
          methodDto.getGenericParameters().stream()
              .map(param -> com.palantir.javapoet.TypeVariableName.get(param.getName()))
              .toList();
      methodBuilder.addTypeVariables(typeVariables);
    }

    // Use javadoc from MethodDto if available
    if (StringUtils.isNoneBlank(methodDto.getJavadoc())) {
      methodBuilder.addJavadoc(methodDto.getJavadoc());
    }

    // Add annotations from MethodDto
    if (!methodDto.getAnnotations().isEmpty()) {
      methodBuilder.addAnnotations(map2AnnotationSpecs(methodDto.getAnnotations()));
    }

    // Add parameters
    for (MethodParameterDto paramDto : methodDto.getParameters()) {
      methodBuilder.addParameter(createParameter(paramDto));
      if (paramDto.getParameterType() instanceof TypeNameArray) {
        methodBuilder.varargs(); // Arrays should be mapped to be generics
      }
    }

    CodeBlock codeBlock = map2CodeBlock(methodDto.getMethodCodeDto());
    methodBuilder.addCode(codeBlock);
    return methodBuilder.build();
  }

  private ParameterSpec createParameter(MethodParameterDto paramDto) {
    com.palantir.javapoet.TypeName parameterType = map2ParameterType(paramDto.getParameterType());
    ParameterSpec.Builder paramBuilder =
        ParameterSpec.builder(parameterType, paramDto.getParameterName());
    if (!paramDto.getAnnotations().isEmpty()) {
      paramBuilder.addAnnotations(map2AnnotationSpecs(paramDto.getAnnotations()));
    }
    return paramBuilder.build();
  }

  private TypeSpec createNestedType(NestedTypeDto nestedType) {
    TypeSpec.Builder typeBuilder;
    boolean isInterface = nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE;
    if (isInterface) {
      typeBuilder = TypeSpec.interfaceBuilder(nestedType.getTypeName());
    } else {
      typeBuilder = TypeSpec.classBuilder(nestedType.getTypeName());
    }

    if (nestedType.isPublic()) {
      typeBuilder.addModifiers(PUBLIC);
    }

    if (nestedType.getJavadoc() != null) {
      typeBuilder.addJavadoc(nestedType.getJavadoc());
    }

    for (MethodDto methodDto : nestedType.getMethods()) {
      MethodSpec methodSpec = createNestedTypeMethod(methodDto, isInterface);
      typeBuilder.addMethod(methodSpec);
    }

    return typeBuilder.build();
  }

  /**
   * Creates a method specification from a MethodDto for nested types (e.g., With interface
   * methods).
   *
   * @param methodDto the method definition
   * @param isInterface whether the containing type is an interface
   * @return the generated MethodSpec
   */
  private MethodSpec createNestedTypeMethod(MethodDto methodDto, boolean isInterface) {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(methodDto.getMethodName()).addModifiers(PUBLIC);

    // Set return type using mapper
    methodBuilder.returns(JavapoetMapper.map2ParameterType(methodDto.getReturnType()));

    // Add parameters using mapper
    for (MethodParameterDto paramDto : methodDto.getParameters()) {
      methodBuilder.addParameter(createParameter(paramDto));
    }

    if (methodDto.getJavadoc() != null) {
      methodBuilder.addJavadoc(methodDto.getJavadoc());
    }

    // Add code only if method has implementation (even for interfaces with default methods)
    if (methodDto.getMethodCodeDto() != null) {
      if (isInterface) {
        methodBuilder.addModifiers(javax.lang.model.element.Modifier.DEFAULT);
      }

      methodBuilder.addCode(map2CodeBlock(methodDto.getMethodCodeDto()));
    }

    return methodBuilder.build();
  }

  /**
   * Generates a Jackson SimpleModule based on the provided definition.
   *
   * @param moduleDef the definition of the Jackson module to generate
   */
  public void generateJacksonModule(JacksonModuleDefinitionDto moduleDef) {
    String packageName = moduleDef.getTargetPackage();
    String moduleClassName = "SimpleBuildersJacksonModule";

    logger.info("Generating Jackson Module '%s' in package '%s'", moduleClassName, packageName);

    ClassName simpleModuleClass =
        ClassName.get("com.fasterxml.jackson.databind.module", "SimpleModule");
    ClassName jsonDeserializeClass =
        ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonDeserialize");

    // Create the constructor
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    // Create the class
    TypeSpec.Builder classBuilder =
        TypeSpec.classBuilder(moduleClassName)
            .addModifiers(Modifier.PUBLIC)
            .superclass(simpleModuleClass);

    for (JacksonModuleEntryDto entry : moduleDef.getEntries()) {
      ClassName dtoClass =
          ClassName.get(entry.dtoType().getPackageName(), entry.dtoType().getClassName());
      ClassName builderClass =
          ClassName.get(entry.builderType().getPackageName(), entry.builderType().getClassName());

      // Create MixIn interface name: DtoNameMixin
      String mixinName = entry.dtoType().getClassName() + "Mixin";

      // Create MixIn interface with @JsonDeserialize(builder = Builder.class)
      TypeSpec mixinInterface =
          TypeSpec.interfaceBuilder(mixinName)
              .addModifiers(Modifier.PRIVATE)
              .addAnnotation(
                  AnnotationSpec.builder(jsonDeserializeClass)
                      .addMember("builder", "$T.class", builderClass)
                      .build())
              .build();

      classBuilder.addType(mixinInterface);

      // Add registration to constructor: setMixInAnnotation(Dto.class, Mixin.class)
      constructorBuilder.addStatement(
          "setMixInAnnotation($T.class, $N.class)", dtoClass, mixinName);
    }

    classBuilder.addMethod(constructorBuilder.build());

    // Write file
    try {
      writeSimpleClassToFile(packageName, classBuilder.build());
    } catch (BuilderException e) {
      logger.warning(
          "simple-builders: Error generating Jackson module for package %s: %s\n%s",
          packageName, e.getMessage(), java.util.Arrays.toString(e.getStackTrace()));
    }
  }
}
