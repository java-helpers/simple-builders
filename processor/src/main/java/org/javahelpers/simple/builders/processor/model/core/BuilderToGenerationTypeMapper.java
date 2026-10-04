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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

/**
 * Maps generation-side DTOs ({@link BuilderDefinitionDto}, {@link BuilderMethodDto}, {@link
 * BuilderNestedTypeDto}) to rendering-side DTOs ({@link GenerationTargetClassDto}, {@link
 * MethodDto}, {@link NestedTypeDto}).
 *
 * <p>This mapper copies all rendering-relevant fields from the generation DTOs to the rendering
 * DTOs. Generation-only fields ({@code sourceFieldName}, {@code constructorField}, {@code
 * exampleChainFragment}) are not mapped.
 *
 * <p>The effective {@link BuilderConfiguration} controls whether Javadoc is emitted: when {@code
 * generateJavaDoc} is disabled, all {@link JavadocDto} instances are left out of the rendering DTO
 * so the code generator can remain a pure renderer.
 */
public class BuilderToGenerationTypeMapper {

  /**
   * Emission order for generated methods, applied while mapping to the rendering DTOs: ordering
   * value (create/fluent/conditional/build/toString/helper sections), access modifier (public
   * first, private last), non-static before static, originating field (alphabetically,
   * builder-level methods last), method name, direct field setter last within a same-named overload
   * group, parameter count, non-generic first, full signature.
   */
  private static final Comparator<FieldMethod> METHOD_EMISSION_ORDER =
      BuilderToGenerationTypeMapper::compareFieldMethods;

  private final BuilderConfiguration configuration;

  /**
   * Creates a mapper for the given builder configuration.
   *
   * @param configuration the builder configuration (already merged with global compiler arguments)
   */
  public BuilderToGenerationTypeMapper(BuilderConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Maps a {@link BuilderDefinitionDto} (generation DTO) to a {@link GenerationTargetClassDto}
   * (rendering DTO) for code generation.
   *
   * <p>This maps all rendering-relevant fields, converting {@link BuilderMethodDto} to {@link
   * MethodDto} and {@link BuilderNestedTypeDto} to {@link NestedTypeDto}.
   *
   * @param builderDto the generation DTO
   * @return the rendering DTO for code generation
   */
  public GenerationTargetClassDto toRenderingDto(BuilderDefinitionDto builderDto) {
    GenerationTargetClassDto renderingDto = new GenerationTargetClassDto();
    renderingDto.setTypeName(builderDto.getTypeName());
    renderingDto.setClassAccessModifier(builderDto.getClassAccessModifier());
    renderingDto.setSuperType(builderDto.getSuperType());
    renderingDto.setClassJavadoc(
        configuration.shouldGenerateJavaDoc() ? builderDto.getClassJavadoc() : null);
    renderingDto.setFormattingMode(configuration.formattingModeEnum());

    builderDto.getClassFields().stream()
        .map(this::toRenderingClassField)
        .forEach(renderingDto::addClassField);

    builderDto.getConstructors().stream()
        .map(this::toRenderingConstructor)
        .forEach(renderingDto::addConstructor);

    builderDto.getGenerics().forEach(renderingDto::addGeneric);

    // Copy imports
    builderDto.getImports().forEach(renderingDto::addImport);

    // Copy class annotations
    builderDto.getClassAnnotations().forEach(renderingDto::addClassAnnotation);

    // Copy interfaces
    builderDto.getInterfaces().forEach(renderingDto::addInterface);

    // Map and copy methods in emission order (see METHOD_EMISSION_ORDER): field-originated
    // methods stay grouped per field, builder-level methods come after grouped ones.
    List<FieldMethod> fieldMethods = new ArrayList<>();
    for (FieldDto field : builderDto.getConstructorFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        fieldMethods.add(new FieldMethod(field, method));
      }
    }
    for (FieldDto field : builderDto.getSetterFieldsForBuilder()) {
      for (BuilderMethodDto method : field.getMethods()) {
        fieldMethods.add(new FieldMethod(field, method));
      }
    }
    for (BuilderMethodDto classMethod : builderDto.getMethods()) {
      fieldMethods.add(new FieldMethod(null, classMethod));
    }
    fieldMethods.stream()
        .sorted(METHOD_EMISSION_ORDER)
        .forEach(pair -> renderingDto.addMethod(toMethodDto(pair.method())));

    builderDto
        .getNestedTypes()
        .forEach(nestedType -> renderingDto.addNestedType(toNestedTypeDto(nestedType)));

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
   * @return a new {@link MethodDto} with all rendering fields copied
   */
  private MethodDto toMethodDto(BuilderMethodDto classMethod) {
    MethodDto method = new MethodDto(classMethod.getMethodName(), classMethod.getReturnType());
    method.setModifier(classMethod.getModifier().orElse(null));
    method.setStatic(classMethod.isStatic());

    if (configuration.shouldGenerateJavaDoc()) {
      method.setJavadoc(buildMethodJavadoc(classMethod));
    }
    classMethod.getAnnotations().forEach(method::addAnnotation);
    classMethod.getParameters().forEach(method::addParameter);
    classMethod.getGenericParameters().forEach(method::addGenericParameter);

    // Copy method code: set code format, copy all arguments, and preserve explicit code-block
    // imports
    if (classMethod.hasCode()) {
      method.setCode(classMethod.getMethodCodeDto().getCodeFormat());
      for (MethodCodePlaceholder<?> argument : classMethod.getMethodCodeDto().getCodeArguments()) {
        if (argument instanceof MethodCodeStringPlaceholder stringPlaceholder) {
          method.addArgument(stringPlaceholder.getLabel(), stringPlaceholder.getValue());
        } else if (argument instanceof MethodCodeTypePlaceholder typePlaceholder) {
          method.addArgument(typePlaceholder.getLabel(), typePlaceholder.getValue());
        }
      }
      method
          .getMethodCodeDto()
          .getCodeBlockImports()
          .addAll(classMethod.getMethodCodeDto().getCodeBlockImports());
    }

    return method;
  }

  /**
   * Pair of a builder method and the field it originates from; {@code field} is {@code null} for
   * builder-level methods created by enhancers.
   */
  private record FieldMethod(FieldDto field, BuilderMethodDto method) {}

  private static int compareFieldMethods(FieldMethod p1, FieldMethod p2) {
    BuilderMethodDto m1 = p1.method();
    BuilderMethodDto m2 = p2.method();

    int orderingCompare = Integer.compare(m1.getOrdering(), m2.getOrdering());
    if (orderingCompare != 0) {
      return orderingCompare;
    }
    int modifierCompare = Integer.compare(accessModifierRank(m1), accessModifierRank(m2));
    if (modifierCompare != 0) {
      return modifierCompare;
    }
    int staticCompare = Boolean.compare(m1.isStatic(), m2.isStatic());
    if (staticCompare != 0) {
      return staticCompare;
    }
    int groupCompare = StringUtils.compare(groupKeyOf(p1), groupKeyOf(p2), false);
    if (groupCompare != 0) {
      return groupCompare;
    }
    int nameCompare = m1.getMethodName().compareTo(m2.getMethodName());
    if (nameCompare != 0) {
      return nameCompare;
    }
    // Within a same-named overload group the direct setter is emitted last, so consumers that
    // resolve overloaded builder methods last-wins (e.g. MapStruct) bind to it.
    int directSetterCompare = Boolean.compare(isDirectFieldSetter(p1), isDirectFieldSetter(p2));
    if (directSetterCompare != 0) {
      return directSetterCompare;
    }
    int paramCountCompare = Integer.compare(m1.getParameters().size(), m2.getParameters().size());
    if (paramCountCompare != 0) {
      return paramCountCompare;
    }
    boolean m1Generic = hasGenericParameters(m1);
    boolean m2Generic = hasGenericParameters(m2);
    if (m1Generic != m2Generic) {
      return m1Generic ? 1 : -1;
    }
    return m1.getSignatureKey().compareTo(m2.getSignatureKey());
  }

  /** Group key clustering field-originated methods; {@code null} for builder-level methods. */
  private static String groupKeyOf(FieldMethod pair) {
    return pair.field() == null ? null : pair.field().getFieldNameInBuilder();
  }

  /** Visibility rank: public first, private last; absent modifier means package-private. */
  private static int accessModifierRank(BuilderMethodDto method) {
    return method
        .getModifier()
        .map(
            modifier ->
                switch (modifier) {
                  case DEFAULT, PUBLIC -> 0;
                  case PROTECTED -> 1;
                  case PACKAGE_PRIVATE -> 2;
                  case PRIVATE -> 3;
                })
        .orElse(2);
  }

  private static boolean isDirectFieldSetter(FieldMethod pair) {
    return pair.field() != null && isDirectFieldSetter(pair.method(), pair.field());
  }

  private static boolean hasGenericParameters(BuilderMethodDto method) {
    return method.getParameters().stream()
        .anyMatch(param -> param.getParameterType().getFullQualifiedName().contains("<"));
  }

  /**
   * Whether {@code method} is the direct setter of {@code field}: exactly one parameter of the
   * field's declared type.
   */
  private static boolean isDirectFieldSetter(BuilderMethodDto method, FieldDto field) {
    return method.getParameters().size() == 1
        && method
            .getParameters()
            .get(0)
            .getParameterType()
            .getFullQualifiedName()
            .equals(field.getFieldType().getFullQualifiedName());
  }

  /**
   * Returns the given class field, clearing its Javadoc when Javadoc generation is disabled.
   *
   * @param classField the source class field
   * @return the class field ready for rendering
   */
  private ClassFieldDto toRenderingClassField(ClassFieldDto classField) {
    if (!configuration.shouldGenerateJavaDoc()) {
      classField.setJavadoc(null);
    }
    return classField;
  }

  /**
   * Returns the given constructor, clearing its Javadoc when Javadoc generation is disabled.
   *
   * @param constructor the source constructor
   * @return the constructor ready for rendering
   */
  private ConstructorDto toRenderingConstructor(ConstructorDto constructor) {
    if (!configuration.shouldGenerateJavaDoc()) {
      constructor.setJavadoc(null);
    }
    return constructor;
  }

  /**
   * Builds the {@link JavadocDto} for a method, appending the pre-built source description when a
   * source field is known.
   *
   * @param classMethod the generation method DTO
   * @return the Javadoc to render, or {@code null} when none is present
   */
  private JavadocDto buildMethodJavadoc(BuilderMethodDto classMethod) {
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
    return javadoc;
  }

  /**
   * Maps a {@link BuilderNestedTypeDto} (generation DTO) to a {@link NestedTypeDto} (rendering
   * DTO).
   *
   * <p>All rendering-relevant fields are copied, including type name, kind, visibility, javadoc,
   * annotations, and methods (mapped via {@link #toMethodDto}).
   *
   * @param builderNestedType the generation DTO to map
   * @return a new {@link NestedTypeDto} with all rendering fields copied
   */
  public NestedTypeDto toNestedTypeDto(BuilderNestedTypeDto builderNestedType) {
    NestedTypeDto nestedType = new NestedTypeDto();
    nestedType.setTypeName(builderNestedType.getTypeName());
    nestedType.setKind(builderNestedType.getKind());
    nestedType.setVisibility(builderNestedType.getVisibility());
    if (configuration.shouldGenerateJavaDoc()) {
      nestedType.setJavadoc(builderNestedType.getJavadoc());
    }
    builderNestedType.getAnnotations().forEach(nestedType::addAnnotation);
    builderNestedType.getMethods().forEach(method -> nestedType.addMethod(toMethodDto(method)));
    return nestedType;
  }
}
