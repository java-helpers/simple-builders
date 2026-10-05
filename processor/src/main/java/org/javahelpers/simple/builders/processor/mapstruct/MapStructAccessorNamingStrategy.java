/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons with the following conditions:
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

package org.javahelpers.simple.builders.processor.mapstruct;

import com.google.auto.service.AutoService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.simple.builders.processor.SimpleBuildersSpiIntegration;
import org.javahelpers.simple.builders.processor.SimpleBuildersSpiIntegration.PublishedBuilder;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsEnum;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.mapstruct.ap.spi.AccessorNamingStrategy;
import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;
import org.mapstruct.ap.spi.MapStructProcessingEnvironment;
import org.mapstruct.ap.spi.MethodType;
import org.mapstruct.ap.spi.TypeHierarchyErroneousException;

/**
 * MapStruct {@link AccessorNamingStrategy} that hides the generated helper methods of
 * simple-builders builders from bean mapping.
 *
 * <p>Generated builders carry convenience methods MapStruct must not treat as property setters:
 * differently-named helpers ({@code add2<Field>}, {@code <field>Update}, {@code conditional(...)})
 * would surface as phantom "unmapped target property" warnings, and same-named helper overloads
 * ({@code <field>(Supplier)}, {@code <field>(String, Object...)}, {@code <field>(Consumer)})
 * compete with the direct setter. For methods declared on a builder published by {@code
 * BuilderProcessor} this strategy returns {@link MethodType#OTHER} for everything that is not the
 * direct property setter ({@code <field><setterSuffix>} taking a single argument of the field
 * type). The setter suffix and the bean of each builder come from the published {@link
 * PublishedBuilder} descriptors, so builders produced by earlier compilations and foreign types
 * keep the {@link DefaultAccessorNamingStrategy} behaviour.
 */
@AutoService(AccessorNamingStrategy.class)
public class MapStructAccessorNamingStrategy extends DefaultAccessorNamingStrategy {

  private Map<String, String> processorOptions = Map.of();

  /** Direct setters (setter name → field type) by builder qualified name. */
  private final Map<String, Map<String, TypeMirror>> directSettersCache = new HashMap<>();

  @Override
  public void init(MapStructProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    Map<String, String> options = processingEnvironment.getOptions();
    processorOptions = options == null ? Map.of() : options;
  }

  @Override
  public MethodType getMethodType(ExecutableElement method) {
    MethodType methodType = super.getMethodType(method);
    if (!SimpleBuildersSpiIntegration.isCurrentCompilation(elementUtils)
        || !isIntegrationEnabled()) {
      // The holder describes another compilation (or is disabled): simple-builders is not
      // ready here, so everything keeps the default classification.
      return methodType;
    }
    if (methodType != MethodType.SETTER && methodType != MethodType.ADDER) {
      // Only write accessors can conflict with generated helpers — everything else keeps the
      // default classification untouched.
      return methodType;
    }
    if (!(method.getEnclosingElement() instanceof TypeElement builderType)) {
      return methodType;
    }
    Map<String, TypeMirror> directSetters =
        directSettersCache.computeIfAbsent(
            builderType.getQualifiedName().toString(), ignored -> directSettersOf(builderType));
    if (directSetters.isEmpty() || isDirectSetter(method, directSetters)) {
      return methodType;
    }
    return MethodType.OTHER;
  }

  /**
   * Whether the integration is switched on for this compilation — read from the options map
   * MapStruct hands the SPI ({@code -A} arguments reach it because {@link
   * MapStructAdditionalSupportedOptionsProvider} declares them), defaulting to enabled.
   */
  private boolean isIntegrationEnabled() {
    return CompilerArgumentsReader.readBooleanValue(
        CompilerArgumentsEnum.USING_MAPSTRUCT_INTEGRATION, processorOptions, true);
  }

  /**
   * Whether {@code method} is the direct property setter: it carries the configured setter name of
   * a bean field and takes exactly one argument assignable to that field's declared type.
   */
  private boolean isDirectSetter(ExecutableElement method, Map<String, TypeMirror> directSetters) {
    TypeMirror fieldType = directSetters.get(method.getSimpleName().toString());
    return fieldType != null
        && method.getParameters().size() == 1
        && typeUtils.isSameType(method.getParameters().get(0).asType(), fieldType);
  }

  /**
   * The direct property setters (setter name → field type) of the bean {@code builderType} was
   * published for — empty when {@code builderType} is not a simple-builders builder.
   */
  private Map<String, TypeMirror> directSettersOf(TypeElement builderType) {
    Optional<PublishedBuilder> published =
        SimpleBuildersSpiIntegration.builderByName(builderType.getQualifiedName().toString());
    if (published.isEmpty()) {
      return Map.of();
    }
    TypeElement beanElement =
        elementUtils.getTypeElement(published.get().beanType().getFullQualifiedName());
    if (beanElement == null) {
      // The published bean is not emitted yet — another processor may produce it in a later
      // round, so the classification retries once the type exists.
      if (!SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration(elementUtils)) {
        throw new TypeHierarchyErroneousException(builderType.asType());
      }
      return Map.of();
    }
    Map<String, TypeMirror> directSetters = new HashMap<>();
    for (VariableElement field : JavaLangAnalyser.findFields(beanElement)) {
      directSetters.put(
          field.getSimpleName().toString() + published.get().setterSuffix(), field.asType());
    }
    return directSetters;
  }
}
