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

package org.javahelpers.simple.builders.processor.model.core;

import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.ConstructorDto;
import org.javahelpers.simple.builders.processor.model.method.MethodCodePlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeStringPlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto;
import org.javahelpers.simple.builders.processor.model.type.NestedTypeDto;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Maps generation-side DTOs ({@link BuilderDefinitionDto}, {@link BuilderMethodDto}, {@link
 * BuilderNestedTypeDto}) to rendering-side DTOs ({@link GenerationTargetClassDto}, {@link
 * MethodDto}, {@link NestedTypeDto}).
 *
 * <p>This mapper copies all rendering-relevant fields from the generation DTOs to the rendering
 * DTOs. Generation-only fields ({@code sourceFieldName}, {@code constructorField}, {@code
 * exampleChainFragment}) are not mapped.
 */
public class BuilderToGenerationTypeMapper {

  private BuilderToGenerationTypeMapper() {
    // Utility class - prevent instantiation
  }

  /**
   * Maps a {@link BuilderDefinitionDto} (generation DTO) to a {@link GenerationTargetClassDto}
   * (rendering DTO) for code generation.
   *
   * <p>Javadoc generation is controlled by the effective configuration in {@code context}. When
   * disabled, all {@link JavadocDto} instances are left out of the rendering DTO so the code
   * generator can remain a pure renderer.
   *
   * @param builderDto the generation DTO
   * @param context the processing context with the effective builder configuration
   * @return the rendering DTO for code generation
   */
  public static GenerationTargetClassDto toRenderingDto(
      BuilderDefinitionDto builderDto, ProcessingContext context) {
    boolean generateJavaDoc = isJavaDocGenerationEnabled(context);
    GenerationTargetClassDto renderingDto = new GenerationTargetClassDto();
    renderingDto.setTypeName(builderDto.getTypeName());
    renderingDto.setClassAccessModifier(builderDto.getClassAccessModifier());
    renderingDto.setSuperType(builderDto.getSuperType());
    renderingDto.setClassJavadoc(generateJavaDoc ? builderDto.getClassJavadoc() : null);

    // Copy class fields, clearing javadoc if generation is disabled
    for (ClassFieldDto classField : builderDto.getClassFields()) {
      if (!generateJavaDoc) {
        classField.setJavadoc(null);
      }
      renderingDto.addClassField(classField);
    }

    // Copy constructors, clearing javadoc if generation is disabled
    for (ConstructorDto constructor : builderDto.getConstructors()) {
      if (!generateJavaDoc) {
        constructor.setJavadoc(null);
      }
      renderingDto.addConstructor(constructor);
    }

    // Copy generics
    builderDto.getGenerics().forEach(renderingDto::addGeneric);

    // Copy imports
    builderDto.getImports().forEach(renderingDto::addImport);

    // Copy class annotations
    builderDto.getClassAnnotations().forEach(renderingDto::addClassAnnotation);

    // Copy interfaces
    builderDto.getInterfaces().forEach(renderingDto::addInterface);

    // Map and copy methods from fields
    for (FieldDto field : builderDto.getConstructorFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        renderingDto.addMethod(toMethodDto(method, generateJavaDoc));
      }
    }
    for (FieldDto field : builderDto.getSetterFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        renderingDto.addMethod(toMethodDto(method, generateJavaDoc));
      }
    }

    // Map and copy builder-level methods from enhancers
    for (BuilderMethodDto classMethod : builderDto.getMethods()) {
      renderingDto.addMethod(toMethodDto(classMethod, generateJavaDoc));
    }

    // Map and copy nested types from enhancers
    for (BuilderNestedTypeDto builderNestedType : builderDto.getNestedTypes()) {
      renderingDto.addNestedType(toNestedTypeDto(builderNestedType, generateJavaDoc));
    }

    return renderingDto;
  }

  /**
   * Maps a {@link BuilderMethodDto} to a {@link MethodDto}, enriching the javadoc with the
   * pre-built source description if available.
   *
   * <p>All rendering-relevant fields are copied. The {@code MethodCodeDto} is shared by reference
   * (not deep-copied), since the rendering phase only reads from it.
   *
   * @param classMethod the generation DTO to map
   * @param generateJavaDoc whether javadoc should be copied into the rendering DTO
   * @return a new {@link MethodDto} with all rendering fields copied
   */
  private static MethodDto toMethodDto(BuilderMethodDto classMethod, boolean generateJavaDoc) {
    MethodDto method = new MethodDto(classMethod.getMethodName(), classMethod.getReturnType());
    method.setModifier(classMethod.getModifier().orElse(null));
    method.setStatic(classMethod.isStatic());
    method.setOrdering(classMethod.getOrdering());

    if (generateJavaDoc) {
      // Enrich javadoc with pre-built source description if source field is known
      JavadocDto javadoc = classMethod.getJavadoc();
      if (StringUtils.isNotBlank(classMethod.getSourceFieldName())) {
        if (javadoc == null) {
          javadoc = new JavadocDto();
        }
        String sourceDescription = classMethod.getSourceDescription();
        if (sourceDescription != null) {
          javadoc.appendDescriptionLine(sourceDescription);
        }
      }
      method.setJavadoc(javadoc);
    }
    classMethod.getAnnotations().forEach(method::addAnnotation);
    classMethod.getParameters().forEach(method::addParameter);
    classMethod.getGenericParameters().forEach(method::addGenericParameter);

    // Copy method code: set code format and copy all arguments
    if (classMethod.hasCode()) {
      method.setCode(classMethod.getMethodCodeDto().getCodeFormat());
      for (MethodCodePlaceholder<?> argument : classMethod.getMethodCodeDto().getCodeArguments()) {
        if (argument instanceof MethodCodeStringPlaceholder stringPlaceholder) {
          method.addArgument(stringPlaceholder.getLabel(), stringPlaceholder.getValue());
        } else if (argument instanceof MethodCodeTypePlaceholder typePlaceholder) {
          method.addArgument(typePlaceholder.getLabel(), typePlaceholder.getValue());
        }
      }
    }

    return method;
  }

  /**
   * Maps a {@link BuilderNestedTypeDto} (generation DTO) to a {@link NestedTypeDto} (rendering
   * DTO).
   *
   * <p>All rendering-relevant fields are copied, including type name, kind, visibility, javadoc,
   * annotations, and methods (mapped via {@link #toMethodDto}).
   *
   * @param builderNestedType the generation DTO to map
   * @param generateJavaDoc whether javadoc should be copied into the rendering DTO
   * @return a new {@link NestedTypeDto} with all rendering fields copied
   */
  private static NestedTypeDto toNestedTypeDto(
      BuilderNestedTypeDto builderNestedType, boolean generateJavaDoc) {
    NestedTypeDto nestedType = new NestedTypeDto();
    nestedType.setTypeName(builderNestedType.getTypeName());
    nestedType.setKind(builderNestedType.getKind());
    nestedType.setVisibility(builderNestedType.getVisibility());
    if (generateJavaDoc) {
      nestedType.setJavadoc(builderNestedType.getJavadoc());
    }
    builderNestedType.getAnnotations().forEach(nestedType::addAnnotation);
    builderNestedType
        .getMethods()
        .forEach(method -> nestedType.addMethod(toMethodDto(method, generateJavaDoc)));
    return nestedType;
  }

  /**
   * Determines whether javadoc should be emitted based on the effective configuration.
   *
   * @param context the processing context, may be {@code null} for tests
   * @return {@code true} when javadoc generation is enabled (default) or context is unavailable
   */
  private static boolean isJavaDocGenerationEnabled(ProcessingContext context) {
    return context == null
        || context.getConfiguration() == null
        || context.getConfiguration().shouldGenerateJavaDoc();
  }
}
