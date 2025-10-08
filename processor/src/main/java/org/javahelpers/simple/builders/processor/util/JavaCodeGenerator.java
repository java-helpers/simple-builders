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
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.util.List;
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
  private final Filer filer;

  /**
   * Constructor for JavaCodeGenerator.
   *
   * @param filer Util class for source code generation of type {@code
   *     javax.annotation.processing.Filer}
   */
  public JavaCodeGenerator(Filer filer) {
    this.filer = filer;
  }

  /**
   * Generating source code file for builder. Creation of source code files is done by {@code
   * javax.annotation.processing.Filer}.
   *
   * @param builderDef dto of all information to create the builder
   * @throws BuilderException if there is an error in source code generation
   */
  public void generateBuilder(BuilderDefinitionDto builderDef) throws BuilderException {
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

    // Generate backing fields for each DTO field (constructor and setter fields)
    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      FieldSpec fieldSpec = createFieldMember(fieldDto);
      classBuilder.addField(fieldSpec);
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      FieldSpec fieldSpec = createFieldMember(fieldDto);
      classBuilder.addField(fieldSpec);
    }

    // Generate field-specific functions in Builder (constructor fields first, then setter fields)
    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      List<MethodSpec> methodSpecs = createFieldMethods(fieldDto, builderTypeName);
      classBuilder.addMethods(methodSpecs);
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      List<MethodSpec> methodSpecs = createFieldMethods(fieldDto, builderTypeName);
      classBuilder.addMethods(methodSpecs);
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

    // Adding annotations
    classBuilder.addAnnotation(createAnnotationGenerated());
    classBuilder.addAnnotation(createAnnotationBuilderImplementation(dtoBaseClass));

    writeClassToFile(builderDef.getBuilderTypeName().getPackageName(), classBuilder.build());
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
      f.getGetterName()
          .ifPresent(
              getter ->
                  cb.addStatement(
                      "this.$N = $T.initialValue(instance.$N())",
                      f.getFieldName(),
                      ClassName.get(TrackedValue.class),
                      getter));
    }
    return cb.build();
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

    // Apply setter-based fields only when changed
    for (FieldDto f : setterFields) {
      mb.addStatement("this.$N.ifChanged(result::$N)", f.getFieldName(), f.getSetterName());
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
        MethodSpec.methodBuilder("create")
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

  private List<MethodSpec> createFieldMethods(
      FieldDto fieldDto, com.palantir.javapoet.TypeName builderTypeName) {
    return fieldDto.getMethods().stream()
        .map(m -> createMethod(m, builderTypeName, fieldDto.getJavaDoc()))
        .toList();
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
      com.palantir.javapoet.TypeName parameterType = map2ParameterType(paramDto.getParameterType());
      methodBuilder.addParameter(parameterType, paramDto.getParameterName());
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
}
