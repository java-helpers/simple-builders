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

import java.util.Optional;
import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.generators.util.MethodGeneratorUtil;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Enhancer that adds the private prefilling methods backing {@link
 * BuilderInstantiation.PrefillCall} resolutions: one {@code prefill<Builder>(T value)} method per
 * referenced builder type, obtaining an empty builder and calling one field function per readable
 * property. Several fields referencing the same type share one prefilling method.
 */
public class FieldPrefillEnhancer implements BuilderEnhancer {

  private static final int PRIORITY = 15;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public boolean appliesTo(
      BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
    return builderDto.getAllFieldsForBuilder().stream()
        .anyMatch(field -> fieldPrefill(field).isPresent());
  }

  @Override
  public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
    builderDto.getAllFieldsForBuilder().stream()
        .map(FieldPrefillEnhancer::fieldPrefill)
        .flatMap(Optional::stream)
        .distinct()
        .map(MethodGeneratorUtil::createFieldPrefillMethod)
        .forEach(builderDto::addMethod);
  }

  /**
   * Returns the field-prefilling instantiation of a field's resolved builder, when the prefilled
   * path is realized by field functions.
   *
   * @param field the field to inspect
   * @return the field-prefilling call, or empty
   */
  private static Optional<BuilderInstantiation.PrefillCall> fieldPrefill(FieldDto field) {
    return field
        .getFieldType()
        .getResolvedBuilder()
        .flatMap(ResolvedBuilder::funcForPrefilledBuilder)
        .filter(BuilderInstantiation.PrefillCall.class::isInstance)
        .map(BuilderInstantiation.PrefillCall.class::cast);
  }
}
