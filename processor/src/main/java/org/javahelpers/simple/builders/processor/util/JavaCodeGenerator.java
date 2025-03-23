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
import static org.javahelpers.simple.builders.processor.dtos.MethodTypes.CONSUMER;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
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
    ClassName builderClassName = createClassNameByTypeName(builderDef.getBuilderTypeName());
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(builderClassName);
    ClassName buildingTargetClassName =
        createClassNameByTypeName(builderDef.getBuildingTargetTypeName());
    classBuilder.addSuperinterface(
        ParameterizedTypeName.get(ClassName.get(IBuilderBase.class), buildingTargetClassName));
    classBuilder.addField(
        FieldSpec.builder(buildingTargetClassName, "instance", Modifier.PRIVATE, Modifier.FINAL)
            .build());

    // TODO: Constructors sollten in der BuilderDefinitionDto definiert werden, nur für Klassen mit
    // leerem Constructor ist auch ein Builder mit leerem Constructor möglich
    classBuilder.addMethod(generateConstructor(buildingTargetClassName));
    classBuilder.addMethod(generateEmptyConstructor(buildingTargetClassName));

    // Generate Methods in Builder without being setter or getter
    for (MethodDto methodDto : builderDef.getMethodsForBuilder()) {
      classBuilder.addMethod(generateMethod(methodDto, builderClassName));
    }

    // Generate Fields and Fieldspecific funtions in Builder
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      List<MethodSpec> methodSpecs = generateSetterMethodsForField(fieldDto, builderClassName);
      classBuilder.addMethods(methodSpecs);
    }

    classBuilder.addMethod(generateBuildMethod(buildingTargetClassName));
    classBuilder.addMethod(
        generateCreateMethodWithoutParameters(builderClassName, buildingTargetClassName));

    classBuilder.addAnnotation(
        AnnotationSpec.builder(Generated.class)
            .addMember(
                "value",
                "$1S",
                "Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
            .build());

    classBuilder.addAnnotation(
        AnnotationSpec.builder(BuilderImplementation.class)
            .addMember("forClass", "$1T.class", buildingTargetClassName)
            .build());

    TypeSpec typeSpec = classBuilder.build();
    try {
      JavaFile.builder(builderDef.getBuilderTypeName().getPackageName(), typeSpec)
          .build()
          .writeTo(filer);
    } catch (IOException ex) {
      throw new BuilderException(null, ex);
    }
  }

  private MethodSpec generateBuildMethod(ClassName returnType) {
    return MethodSpec.methodBuilder("build")
        .addModifiers(PUBLIC)
        .returns(returnType)
        .addAnnotation(Override.class)
        .addCode(
            """
            return instance;
            """)
        .build();
  }

  private MethodSpec generateConstructor(ClassName buildTargetType) {
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(buildTargetType, "instance")
        .addStatement("this.instance = instance")
        .build();
  }

  private MethodSpec generateEmptyConstructor(ClassName buildTargetType) {
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addStatement("this.instance = new $1T()", buildTargetType)
        .build();
  }

  private MethodSpec generateCreateMethodWithoutParameters(
      ClassName builderType, ClassName buildTargetType) {
    return MethodSpec.methodBuilder("create")
        .addModifiers(STATIC, PUBLIC)
        .returns(builderType)
        .addCode(
            """
            $1T instance = new $1T();
            return new $2T(instance);
            """,
            buildTargetType,
            builderType)
        .build();
  }

  private MethodSpec generateMethod(MethodDto methodDto, ClassName returnType) {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(methodDto.getMethodName()).returns(returnType);
    methodDto.getModifier().ifPresent(methodBuilder::addModifiers);
    List<String> parametersInInnerCall = new LinkedList<>();
    for (MethodParameterDto paramDto : methodDto.getParameters()) {
      com.palantir.javapoet.TypeName parameterType = mapParameterType(paramDto.getParameterType());
      methodBuilder.addParameter(parameterType, paramDto.getParameterName());
      parametersInInnerCall.add(paramDto.getParameterName());
    }
    CodeBlock codeBlock =
        switch (methodDto.getMethodType()) {
          case PROXY -> createInnerCodeForMethodProxy(methodDto, parametersInInnerCall);
          case CONSUMER -> createInnerCodeForConsumer(methodDto);
          case CONSUMER_BY_BUILDER -> createInnerCodeForConsumerByBuilder(methodDto);
          case SUPPLIER -> createInnerCodeForSupplier(methodDto);
            // TODO reiner Builder ohne Consumer
        };
    methodBuilder.addCode(codeBlock);
    return methodBuilder.build();
  }

  private CodeBlock createInnerCodeForMethodProxy(
      MethodDto methodDto, List<String> parametersInInnerCall) {
    return CodeBlock.of(
        """
        instance.$1N($2N);
        return this;
        """,
        methodDto.getFieldSetterMethodName(),
        String.join(", ", parametersInInnerCall));
  }

  private CodeBlock createInnerCodeForConsumer(MethodDto methodDto) {
    // TODO: CollectionUtils
    if (methodDto.getParameters().isEmpty()) {
      // TODO Error
      return null;
    }
    MethodParameterDto consumerParameter = methodDto.getParameters().get(0);
    Optional<TypeName> innerTypeOpt = consumerParameter.getParameterType().getInnerType();
    if (innerTypeOpt.isEmpty()) {
      // TODO Error
      return null;
    }
    return CodeBlock.of(
        """
        $1T consumer = new $1T();
        $2N.accept(consumer);
        instance.$3N(consumer);
        return this;
        """,
        createClassNameByTypeName(consumerParameter.getParameterType().getInnerType().get()),
        consumerParameter.getParameterName(),
        methodDto.getFieldSetterMethodName());
  }

  private CodeBlock createInnerCodeForConsumerByBuilder(MethodDto methodDto) {
    // TODO: CollectionUtils
    if (methodDto.getParameters().isEmpty()) {
      // TODO Error
      return null;
    }
    MethodParameterDto consumerParameter = methodDto.getParameters().get(0);
    Optional<TypeName> innerTypeOpt = consumerParameter.getParameterType().getInnerType();
    if (innerTypeOpt.isEmpty()) {
      // TODO Error
      return null;
    }
    // TODO: der generierte Code arbeitet nicht mit dem generischen Typ des Builders
    return CodeBlock.of(
        """
        $1T builder = new $1T();
        $2N.accept(builder);
        instance.$3N(builder.build());
        return this;
        """,
        createClassNameByTypeName(consumerParameter.getParameterType().getInnerType().get()),
        consumerParameter.getParameterName(),
        methodDto.getFieldSetterMethodName());
  }

  private CodeBlock createInnerCodeForSupplier(MethodDto methodDto) {
    // TODO: CollectionUtils
    if (methodDto.getParameters().isEmpty()) {
      // TODO Error
      return null;
    }
    MethodParameterDto consumerParameter = methodDto.getParameters().get(0);
    return CodeBlock.of(
        """
          instance.$1N($2N.get());
          return this;
        """,
        methodDto.getFieldSetterMethodName(),
        consumerParameter.getParameterName());
  }

  private com.palantir.javapoet.TypeName mapParameterType(TypeName parameterType) {
    ClassName classNameParameter =
        ClassName.get(parameterType.getPackageName(), parameterType.getClassName());
    if (parameterType.getInnerType().isPresent()) {
      return ParameterizedTypeName.get(
          classNameParameter, mapParameterType(parameterType.getInnerType().get()));
    }
    return classNameParameter;
  }

  private List<MethodSpec> generateSetterMethodsForField(
      FieldDto fieldDto, ClassName builderClassName) {
    return fieldDto.getFieldSetterMethodsList().stream()
        .map(m -> generateMethod(m, builderClassName))
        .toList();
  }

  private ClassName createClassNameByTypeName(TypeName typeName) {
    return ClassName.get(typeName.getPackageName(), typeName.getClassName());
  }
}
