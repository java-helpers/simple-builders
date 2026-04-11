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

package org.javahelpers.simple.builders.processor.generators.builder;

import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.method.ConstructorDto;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds builder constructors.
 *
 * <p>This enhancer generates the two builder constructors:
 *
 * <ul>
 *   <li>Empty constructor - for creating a new builder from scratch
 *   <li>From-instance constructor - for creating a builder initialized from an existing DTO
 *       instance
 * </ul>
 *
 * <p>Priority: 95 (runs after CoreMethodsEnhancer at 100)
 */
public class ConstructorEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 95;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return true; // Constructors are always needed
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    builderDto.addConstructor(createEmptyConstructor(builderDto));
    builderDto.addConstructor(createFromInstanceConstructor(builderDto));
  }

  /** Creates the empty constructor. */
  protected ConstructorDto createEmptyConstructor(BuilderDefinitionDto builderDto) {
    ConstructorDto constructor = new ConstructorDto();
    constructor.setVisibility(builderDto.getConfiguration().getBuilderConstructorAccess());

    String targetFullName = builderDto.getBuildingTargetTypeName().getFullQualifiedName();
    constructor.setJavadoc(
        new JavadocDto("Empty constructor of builder for {@code %s}.", targetFullName));

    return constructor;
  }

  /** Creates the from-instance constructor. */
  protected ConstructorDto createFromInstanceConstructor(BuilderDefinitionDto builderDto) {
    ConstructorDto constructor = new ConstructorDto();
    constructor.setVisibility(builderDto.getConfiguration().getBuilderConstructorAccess());

    // Add parameter for the DTO instance
    MethodParameterDto instanceParam = new MethodParameterDto();
    instanceParam.setParameterName("instance");
    instanceParam.setParameterTypeName(builderDto.getBuildingTargetTypeName());
    constructor.addParameter(instanceParam);

    // Build constructor code body
    MethodCodeDto codeDto = new MethodCodeDto();
    constructor.setMethodCodeDto(codeDto);

    // Initialize fields from instance
    for (FieldDto field : builderDto.getAllFieldsForBuilder()) {
      String fieldName = field.getFieldNameInBuilder();

      // getGetterName() returns Optional<String>
      field
          .getGetterName()
          .ifPresent(
              getterName -> {
                // Field has a getter - use it
                if (field.isNonNullable() && !(field.getFieldType() instanceof TypeNamePrimitive)) {
                  // Non-nullable non-primitive field - validate not null
                  // Skip primitives as they can't be compared to null (compilation error)
                  codeDto.append(
                      """
                          if (instance.%s() == null) {
                            throw new IllegalArgumentException("Field '%s' is non-null but instance.%s() returned null");
                          }
                          """,
                      getterName, fieldName, getterName);
                  codeDto.addCodeBlockImport(new TypeName("java.lang", "IllegalArgumentException"));
                }
                codeDto.append(
                    """
                    this.%s = TrackedValue.initialValue(instance.%s());
                    """
                        .formatted(fieldName, getterName));
              });
      // If no getter available - cannot initialize this field
      // Leave it unset (will use unsetValue() from field initializer)
    }

    String targetFullName = builderDto.getBuildingTargetTypeName().getFullQualifiedName();
    constructor.setJavadoc(
        new JavadocDto("Initialisation of builder for {@code %s} by a instance.", targetFullName)
            .addParam("instance", "object instance for initialisiation"));

    return constructor;
  }
}
