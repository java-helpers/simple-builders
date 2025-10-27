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
import static javax.lang.model.element.Modifier.STATIC;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.TrackedValue;
import org.javahelpers.simple.builders.processor.dtos.*;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;

/** JavaCodeGenerator generates with BuilderDefinitionDto JavaCode for the builder. */
public class JavaCodeGenerator {
  /** Util class for source code generation of type {@code javax.annotation.processing.Filer}. */
  private static final String METHOD_NAME_CREATE = "create";

  private static final String THROW_EXCEPTION_FORMAT = "throw new $T($S)";
  private final Filer filer;

  /** Logger for debug output during code generation. */
  private final ProcessingLogger logger;

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
    ClassName dtoBaseClass = map2ClassName(builderDef.getBuildingTargetTypeName());
    com.palantir.javapoet.TypeName builderTypeName;
    com.palantir.javapoet.TypeName dtoTypeName;
    if (builderDef.getGenerics().isEmpty()) {
      builderTypeName = builderBaseClass;
      dtoTypeName = dtoBaseClass;
    } else {
      builderTypeName =
          map2ParameterizedTypeName(builderDef.getBuilderTypeName(), builderDef.getGenerics());
      dtoTypeName =
          map2ParameterizedTypeName(
              builderDef.getBuildingTargetTypeName(), builderDef.getGenerics());
      logger.debug("Builder has %d generic type parameter(s)", builderDef.getGenerics().size());
    }

    TypeSpec.Builder classBuilder =
        TypeSpec.classBuilder(builderBaseClass)
            .addTypeVariables(map2TypeVariables(builderDef.getGenerics()))
            .addJavadoc(createJavadocForClass(dtoBaseClass))
            .addSuperinterface(createInterfaceBuilderBase(dtoTypeName));

    // Adding Constructors for builder
    classBuilder.addMethod(
        createConstructorWithInstance(
            dtoBaseClass, dtoTypeName, builderDef.getAllFieldsForBuilder()));
    classBuilder.addMethod(createEmptyConstructor(dtoBaseClass));

    logger.debug(
        "Generating %d constructor fields and %d setter fields",
        builderDef.getConstructorFieldsForBuilder().size(),
        builderDef.getSetterFieldsForBuilder().size());

    // Generate backing fields for each DTO field (constructor and setter fields)
    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      FieldSpec fieldSpec = createFieldMember(fieldDto);
      classBuilder.addField(fieldSpec);
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      FieldSpec fieldSpec = createFieldMember(fieldDto);
      classBuilder.addField(fieldSpec);
    }

    // Collect all methods from all fields, setting javadoc and tracking field relationship
    Map<MethodDto, FieldDto> methodToField = new HashMap<>();

    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      for (MethodDto method : fieldDto.getMethods()) {
        // Set javadoc on method if not already set
        if (method.getJavadoc() == null && fieldDto.getJavaDoc() != null) {
          method.setJavadoc(fieldDto.getJavaDoc());
        }
        methodToField.put(method, fieldDto);
      }
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      for (MethodDto method : fieldDto.getMethods()) {
        // Set javadoc on method if not already set
        if (method.getJavadoc() == null && fieldDto.getJavaDoc() != null) {
          method.setJavadoc(fieldDto.getJavaDoc());
        }
        methodToField.put(method, fieldDto);
      }
    }

    // Resolve conflicts: keep only the highest priority method for each signature
    List<MethodDto> resolvedMethods = resolveMethodConflicts(methodToField);
    logger.debug("  Resolved %d methods after conflict resolution", resolvedMethods.size());

    // Generate field-specific functions in Builder
    for (MethodDto methodDto : resolvedMethods) {
      MethodSpec methodSpec = createMethod(methodDto, builderTypeName, methodDto.getJavadoc());
      classBuilder.addMethod(methodSpec);
    }

    // Adding builder-specific methods
    classBuilder.addMethod(
        createMethodBuild(
            dtoBaseClass,
            dtoTypeName,
            builderDef.getConstructorFieldsForBuilder(),
            builderDef.getSetterFieldsForBuilder(),
            builderDef.getGenerics()));
    classBuilder.addMethod(
        createMethodStaticCreate(
            builderBaseClass, builderTypeName, dtoBaseClass, builderDef.getGenerics()));
    classBuilder.addMethod(createMethodConditional(builderTypeName));
    classBuilder.addMethod(createMethodConditionalPositiveOnly(builderTypeName));

    // Adding nested types (e.g., With interface)
    for (NestedTypeDto nestedType : builderDef.getNestedTypes()) {
      TypeSpec nestedTypeSpec = createNestedType(nestedType);
      classBuilder.addType(nestedTypeSpec);
      logger.debug("  Generated nested type: %s", nestedType.getTypeName());
    }

    // Adding annotations
    classBuilder.addAnnotation(createAnnotationGenerated());
    classBuilder.addAnnotation(createAnnotationBuilderImplementation(dtoBaseClass));

    logger.debug(
        "Writing builder class to file: %s.%s",
        builderDef.getBuilderTypeName().getPackageName(),
        builderDef.getBuilderTypeName().getClassName());
    writeClassToFile(builderDef.getBuilderTypeName().getPackageName(), classBuilder.build());
    logger.debug(
        "Successfully generated builder: %s", builderDef.getBuilderTypeName().getClassName());
  }

  private void writeClassToFile(String packageName, TypeSpec typeSpec) throws BuilderException {
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

  /**
   * Resolves method conflicts by keeping only the highest priority method for each unique
   * signature. This prevents compilation errors when methods from different fields have the same
   * signature.
   *
   * @param methodToField mapping from method to its source field
   * @return list of methods with conflicts resolved
   */
  private List<MethodDto> resolveMethodConflicts(Map<MethodDto, FieldDto> methodToField) {
    Map<String, MethodDto> signatureToMethod = new HashMap<>();

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
        String existingFieldName = methodToField.get(existing).getFieldName();
        String newFieldName = field.getFieldName();

        if (method.getPriority() > existing.getPriority()) {
          // New method wins
          signatureToMethod.put(signature, method);
          logger.warning(
              "  Method conflict: '%s' from field '%s' (priority %d) dropped in favor of field '%s' (priority %d)",
              signature,
              existingFieldName,
              existing.getPriority(),
              newFieldName,
              method.getPriority());
        } else if (method.getPriority() < existing.getPriority()) {
          // Existing method wins
          logger.warning(
              "  Method conflict: '%s' from field '%s' (priority %d) dropped in favor of field '%s' (priority %d)",
              signature,
              newFieldName,
              method.getPriority(),
              existingFieldName,
              existing.getPriority());
        } else {
          // Equal priority - keep first
          logger.warning(
              "  Method conflict with equal priority: '%s' from field '%s' dropped, keeping first occurrence from field '%s' (priority %d)",
              signature, newFieldName, existingFieldName, method.getPriority());
        }
      }
    }

    return new ArrayList<>(signatureToMethod.values());
  }

  private CodeBlock createJavadocForClass(ClassName dtoClass) {
    return CodeBlock.of("Builder for {@code $1N.$2T}.", dtoClass.packageName(), dtoClass);
  }

  private ParameterizedTypeName createInterfaceBuilderBase(com.palantir.javapoet.TypeName dtoType) {
    return ParameterizedTypeName.get(ClassName.get(IBuilderBase.class), dtoType);
  }

  private AnnotationSpec createAnnotationGenerated() {
    return AnnotationSpec.builder(Generated.class)
        .addMember(
            "value",
            "$1S",
            "Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
        .build();
  }

  private AnnotationSpec createAnnotationBuilderImplementation(ClassName dtoClass) {
    return AnnotationSpec.builder(BuilderImplementation.class)
        .addMember("forClass", "$1T.class", dtoClass)
        .build();
  }

  private MethodSpec createEmptyConstructor(ClassName dtoClass) {
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc(
                """
            Empty constructor of builder for {@code $1N.$2T}.
            """,
                dtoClass.packageName(),
                dtoClass);
    return constructorBuilder.build();
  }

  private MethodSpec createConstructorWithInstance(
      ClassName dtoBaseClass, com.palantir.javapoet.TypeName dtoType, List<FieldDto> fields) {
    MethodSpec.Builder cb =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
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

  private MethodSpec createMethodBuild(
      ClassName dtoBaseClass,
      com.palantir.javapoet.TypeName returnType,
      List<FieldDto> constructorFields,
      List<FieldDto> setterFields,
      List<GenericParameterDto> generics) {
    MethodSpec.Builder mb =
        MethodSpec.methodBuilder("build")
            .addModifiers(PUBLIC)
            .returns(returnType)
            .addAnnotation(Override.class);

    // Validate non-nullable constructor fields: must be set AND can't be null
    // If not annotated with @NotNull/@NonNull, constructor fields can be left unset (→ null passed)
    for (FieldDto field : constructorFields) {
      if (field.isNonNullable()) {
        mb.beginControlFlow("if (!this.$N.isSet())", field.getFieldName())
            .addStatement(
                THROW_EXCEPTION_FORMAT,
                IllegalStateException.class,
                "Required field '" + field.getFieldName() + "' must be set before calling build()")
            .endControlFlow();
        mb.beginControlFlow("if (this.$N.value() == null)", field.getFieldName())
            .addStatement(
                THROW_EXCEPTION_FORMAT,
                IllegalStateException.class,
                "Field '"
                    + field.getFieldName()
                    + "' is marked as non-null but null value was provided")
            .endControlFlow();
      }
    }

    // Validate non-nullable setter fields don't have null values
    // This catches null values from suppliers, providers, or direct setter calls
    for (FieldDto field : setterFields) {
      if (field.isNonNullable()) {
        mb.beginControlFlow(
                "if (this.$N.isSet() && this.$N.value() == null)",
                field.getFieldName(),
                field.getFieldName())
            .addStatement(
                THROW_EXCEPTION_FORMAT,
                IllegalStateException.class,
                "Field '"
                    + field.getFieldName()
                    + "' is marked as non-null but null value was provided")
            .endControlFlow();
      }
    }

    // Build constructor argument list: use backing fields' values in declared order
    String ctorArgs =
        constructorFields.stream()
            .map(FieldDto::getFieldName)
            .map(n -> String.format("this.%s.value()", n))
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

    if (generics.isEmpty()) {
      mb.addStatement("$1T result = new $1T($2L)", dtoBaseClass, ctorArgs);
    } else {
      mb.addStatement("$1T result = new $2T<>($3L)", returnType, dtoBaseClass, ctorArgs);
    }

    // Apply setter-based fields only when set
    for (FieldDto f : setterFields) {
      mb.addStatement("this.$N.ifSet(result::$N)", f.getFieldName(), f.getSetterName());
    }
    mb.addStatement("return result");
    return mb.build();
  }

  private MethodSpec createMethodStaticCreate(
      com.palantir.javapoet.ClassName builderBaseClass,
      com.palantir.javapoet.TypeName builderType,
      com.palantir.javapoet.ClassName dtoBaseClass,
      List<GenericParameterDto> generics) {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(METHOD_NAME_CREATE)
            .addModifiers(STATIC, PUBLIC)
            .addJavadoc(
                """
            Creating a new builder for {@code $1N.$2T}.

            @return builder for {@code $1N.$2T}
            """,
                dtoBaseClass.packageName(),
                dtoBaseClass);
    if (generics.isEmpty()) {
      methodBuilder.returns(builderBaseClass).addCode("return new $1T();\n", builderBaseClass);
    } else {
      methodBuilder
          .returns(builderType)
          .addTypeVariables(map2TypeVariables(generics))
          .addCode("return new $1T<>();\n", builderBaseClass);
    }
    return methodBuilder.build();
  }

  private MethodSpec createMethodConditional(com.palantir.javapoet.TypeName builderType) {
    return MethodSpec.methodBuilder("conditional")
        .addModifiers(PUBLIC)
        .returns(builderType)
        .addParameter(ClassName.get(java.util.function.BooleanSupplier.class), "condition")
        .addParameter(
            ParameterizedTypeName.get(
                ClassName.get(java.util.function.Consumer.class), builderType),
            "trueCase")
        .addParameter(
            ParameterizedTypeName.get(
                ClassName.get(java.util.function.Consumer.class), builderType),
            "falseCase")
        .addJavadoc(
            """
            Conditionally applies builder modifications based on a condition.

            @param condition the condition to evaluate
            @param trueCase the consumer to apply if condition is true
            @param falseCase the consumer to apply if condition is false (can be null)
            @return this builder instance
            """)
        .addCode(
            """
            if (condition.getAsBoolean()) {
                trueCase.accept(this);
            } else if (falseCase != null) {
                falseCase.accept(this);
            }
            return this;
            """)
        .build();
  }

  private MethodSpec createMethodConditionalPositiveOnly(
      com.palantir.javapoet.TypeName builderType) {
    return MethodSpec.methodBuilder("conditional")
        .addModifiers(PUBLIC)
        .returns(builderType)
        .addParameter(ClassName.get(java.util.function.BooleanSupplier.class), "condition")
        .addParameter(
            ParameterizedTypeName.get(
                ClassName.get(java.util.function.Consumer.class), builderType),
            "yesCondition")
        .addJavadoc(
            """
            Conditionally applies builder modifications if the condition is true.

            @param condition the condition to evaluate
            @param yesCondition the consumer to apply if condition is true
            @return this builder instance
            """)
        .addCode("return conditional(condition, yesCondition, null);\n")
        .build();
  }

  /**
   * Creates a TypeSpec for a nested type (e.g., With interface).
   *
   * @param nestedType the nested type definition
   * @return the TypeSpec for the nested type
   */
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

    // Add methods to the nested type
    for (MethodDto method : nestedType.getMethods()) {
      MethodSpec methodSpec = createNestedTypeMethod(method, isInterface);
      typeBuilder.addMethod(methodSpec);
    }

    return typeBuilder.build();
  }

  /**
   * Creates a MethodSpec for a method of a nested type (e.g., With interface).
   *
   * @param methodDto the method to create
   * @param isInterface whether the nested type is an interface
   * @return the MethodSpec
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

    // Add modifiers if defined
    methodDto.getModifier().ifPresent(methodBuilder::addModifiers);

    // Add Javadoc
    if (methodDto.getJavadoc() != null) {
      methodBuilder.addJavadoc(methodDto.getJavadoc());
    }

    // Add method body if present
    MethodCodeDto codeDto = methodDto.getMethodCodeDto();
    if (codeDto != null) {
      // Add default modifier for interface methods with implementation
      if (isInterface) {
        methodBuilder.addModifiers(javax.lang.model.element.Modifier.DEFAULT);
      }

      methodBuilder.addCode(map2CodeBlock(codeDto));
    }

    return methodBuilder.build();
  }

  private MethodSpec createMethod(
      MethodDto methodDto,
      com.palantir.javapoet.TypeName returnType,
      String optionalFieldParamJavaDoc) {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(methodDto.getMethodName()).returns(returnType);
    methodDto.getModifier().ifPresent(methodBuilder::addModifiers);
    int maxIndexParameters = methodDto.getParameters().size() - 1;

    // Adding javadoc for method
    switch (methodDto.getMethodType()) {
      case PROXY ->
          methodBuilder.addJavadoc(
              "Sets the value for <code>$1N</code>.\n", methodDto.getMethodName());
      case CONSUMER ->
          methodBuilder.addJavadoc(
              "Sets the value for <code>$1N</code> by executing the provided consumer.\n",
              methodDto.createFieldSetterMethodName());
      case CONSUMER_BY_BUILDER ->
          methodBuilder.addJavadoc(
              "Sets the value for <code>$1N</code> using a builder consumer that produces the value.\n",
              methodDto.createFieldSetterMethodName());
      case SUPPLIER ->
          methodBuilder.addJavadoc(
              "Sets the value for <code>$1N</code> by invoking the provided supplier.\n",
              methodDto.createFieldSetterMethodName());
    }

    for (int i = 0; i <= maxIndexParameters; i++) {
      MethodParameterDto paramDto = methodDto.getParameters().get(i);
      methodBuilder.addParameter(createParameter(paramDto));

      if (i == maxIndexParameters && paramDto.getParameterType() instanceof TypeNameArray) {
        methodBuilder.varargs(); // Arrays should be mapped to be generics
      }

      // Extending Javadoc with parameters
      switch (methodDto.getMethodType()) {
        case PROXY ->
            methodBuilder.addJavadoc(
                "\n@param $1N $2L", paramDto.getParameterName(), optionalFieldParamJavaDoc);
        case CONSUMER ->
            methodBuilder.addJavadoc(
                "\n@param $1N consumer providing an instance of $2L",
                paramDto.getParameterName(),
                optionalFieldParamJavaDoc);
        case CONSUMER_BY_BUILDER ->
            methodBuilder.addJavadoc(
                "\n@param $1N consumer providing an instance of a builder for $2L",
                paramDto.getParameterName(),
                optionalFieldParamJavaDoc);
        case SUPPLIER ->
            methodBuilder.addJavadoc(
                "\n@param $1N supplier for $2L",
                paramDto.getParameterName(),
                optionalFieldParamJavaDoc);
      }
    }

    CodeBlock codeBlock = map2CodeBlock(methodDto.getMethodCodeDto());
    methodBuilder.addCode(codeBlock).addJavadoc("\n@return current instance of builder");
    return methodBuilder.build();
  }

  /**
   * Creates a ParameterSpec from a MethodParameterDto, including any annotations.
   *
   * @param paramDto the parameter DTO containing type, name, and annotations
   * @return the generated ParameterSpec
   */
  private ParameterSpec createParameter(MethodParameterDto paramDto) {
    com.palantir.javapoet.TypeName parameterType = map2ParameterType(paramDto.getParameterType());
    ParameterSpec.Builder paramBuilder =
        ParameterSpec.builder(parameterType, paramDto.getParameterName());
    if (!paramDto.getAnnotations().isEmpty()) {
      paramBuilder.addAnnotations(map2AnnotationSpecs(paramDto.getAnnotations()));
    }
    return paramBuilder.build();
  }
}
