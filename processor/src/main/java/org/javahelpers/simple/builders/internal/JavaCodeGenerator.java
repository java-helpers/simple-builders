package org.javahelpers.simple.builders.internal;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.annotations.BuilderImplementation;
import org.javahelpers.simple.builders.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.internal.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.internal.dtos.FieldDto;
import org.javahelpers.simple.builders.internal.dtos.MethodDto;
import org.javahelpers.simple.builders.internal.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.internal.dtos.TypeName;

public class JavaCodeGenerator {
  private final Filer filer;

  public JavaCodeGenerator(Filer filer) {
    this.filer = filer;
  }

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
    classBuilder.addMethod(generateConstructor(buildingTargetClassName));

    for (MethodDto methodDto : builderDef.getMethodsForBuilder()) {
      classBuilder.addMethod(generateMethod(methodDto, builderClassName));
    }

    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      classBuilder.addMethod(generateFieldMethod(fieldDto, builderClassName));
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
      JavaFile.builder(builderDef.getBuilderTypeName().packageName(), typeSpec)
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
    for (MethodParameterDto paramDto : methodDto.getParameters()) {
      ClassName parameterType =
          ClassName.get(
              paramDto.getParameterType().packageName(), paramDto.getParameterType().className());
      methodBuilder.addParameter(parameterType, paramDto.getParameterName());
    }
    methodBuilder.addCode(
        """
        instance.$1N($2N);
        return this;
        """,
        methodDto.getMethodName(),
        methodDto.getParameters().get(0).getParameterName());
    return methodBuilder.build();
  }

  private MethodSpec generateFieldMethod(FieldDto fieldDto, ClassName builderClassName) {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(fieldDto.getFieldName()).returns(builderClassName);
    fieldDto.getModifier().ifPresent(methodBuilder::addModifiers);
    ClassName parameterType =
        ClassName.get(fieldDto.getFieldType().packageName(), fieldDto.getFieldType().className());
    methodBuilder.addParameter(parameterType, fieldDto.getFieldName());

    methodBuilder.addCode(
        """
        instance.$1N($2N);
        return this;
        """,
        fieldDto.getFieldSetterName(),
        fieldDto.getFieldName());
    return methodBuilder.build();
  }

  private ClassName createClassNameByTypeName(TypeName typeName) {
    return ClassName.get(typeName.packageName(), typeName.className());
  }
}
