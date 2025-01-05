package org.javahelpers.simple.builders.internal;

import static javax.lang.model.element.Modifier.PUBLIC;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import org.javahelpers.simple.builders.internal.dtos.BuilderDefinitionDto;
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
    classBuilder.addField(
        FieldSpec.builder(buildingTargetClassName, "instance", Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $1T()", buildingTargetClassName)
            .build());

    for (MethodDto methodDto : builderDef.getMethodsForBuilder()) {
      classBuilder.addMethod(generateMethod(methodDto, builderClassName));
    }
    classBuilder.addMethod(generateBuildMethod(buildingTargetClassName));

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
        .addCode(
            """
                                                                                       return instance;
                                                                                       """)
        .build();
  }

  private MethodSpec generateMethod(MethodDto methodDto, ClassName returnType) {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder(methodDto.getMemberName().toString())
            .addModifiers(methodDto.getModifier())
            .returns(returnType);
    for (MethodParameterDto paramDto : methodDto.getParameters()) {
      ClassName parameterType =
          ClassName.get(
              paramDto.getParameterType().packageName(), paramDto.getParameterType().className());
      methodBuilder.addParameter(parameterType, paramDto.getParameterName().toString());
    }
    methodBuilder.addCode(
        """
        instance.$1N($2N);
        return this;
        """,
        methodDto.getMemberName(),
        methodDto.getParameters().get(0).getParameterName());
    return methodBuilder.build();
  }

  private ClassName createClassNameByTypeName(TypeName typeName) {
    return ClassName.get(typeName.packageName(), typeName.className());
  }
}
