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
package org.javahelpers.simple.builders.processor.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.PackageScopes;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Central resolver that decides whether a builder may be referenced for a given type.
 *
 * <p>This resolver is independent of generator/enhancer code; it only relies on the configured
 * {@code builderGenerationPackages} and {@code builderUsagePackages} scopes, registered generated
 * types, and the availability of builder types on the classpath.
 *
 * <p>The resolver is constructed once per processing context and refreshes its parsed package
 * scopes and per-type results when the target configuration changes. Types whose builders are
 * generated in the current processing round are registered before resolution.
 */
public final class BuilderScopeResolver {

  private final ProcessingContext context;
  private BuilderConfiguration cachedConfiguration;
  private PackageScopes generationPackages = PackageScopes.unscoped();
  private PackageScopes usagePackages = PackageScopes.unscoped();
  private Set<String> generatedTypeNames = Set.of();
  private final Map<String, Optional<TypeName>> resolvedBuilderTypes = new HashMap<>();

  /**
   * Creates a new resolver for the given processing context.
   *
   * @param context the processing context providing configuration and type utilities
   */
  public BuilderScopeResolver(ProcessingContext context) {
    this.context = context;
  }

  /**
   * Resolves the builder type to use for the given referenced type, if any.
   *
   * <p>The decision follows these rules:
   *
   * <ol>
   *   <li>If the referenced type is opted out with {@code @Ignore4BuilderGeneration}, or is not
   *       annotated with {@code @SimpleBuilder}, no builder may be used.
   *   <li>If both scopes are empty/unset, the candidate builder is returned for full backward
   *       compatibility (current behavior, no type search).
   *   <li>If the referenced type's package is in {@code builderGenerationPackages}, the candidate
   *       builder is returned without a type-existence search.
   *   <li>If the referenced type's package is in {@code builderUsagePackages} (but not in the
   *       generation scope), the candidate builder is returned if its builder is generated in the
   *       current processing round or can be resolved on the classpath.
   *   <li>Otherwise no builder may be referenced.
   * </ol>
   *
   * @param referencedType the type element being referenced as a field or collection element
   * @return the builder type to reference, or empty if no builder should be referenced
   */
  public Optional<TypeName> resolveUsableBuilderType(TypeElement referencedType) {
    if (referencedType == null) {
      return Optional.empty();
    }
    refreshForConfigurationIfNeeded();
    return resolvedBuilderTypes.computeIfAbsent(
        referencedType.getQualifiedName().toString(), fqn -> resolve(referencedType));
  }

  /**
   * Registers the types whose builders are generated in the current processing round.
   *
   * @param generatedTypes types whose builders will be generated in this round
   */
  public void registerGeneratedTypes(Collection<? extends TypeElement> generatedTypes) {
    Set<String> registeredTypeNames = new HashSet<>();
    for (TypeElement generatedType : generatedTypes) {
      registeredTypeNames.add(generatedType.getQualifiedName().toString());
    }
    generatedTypeNames = registeredTypeNames;
    resolvedBuilderTypes.clear();
  }

  private Optional<TypeName> resolve(TypeElement referencedType) {
    if (!hasSimpleBuilderAnnotation(referencedType)
        || isIgnoredForBuilderGeneration(referencedType)) {
      return Optional.empty();
    }

    TypeName candidate = JavaLangMapper.createBuilderTypeName(referencedType, context);
    String packageName = context.getPackageName(referencedType);

    // Both scopes unset → full backward compatibility, no type search.
    if (generationPackages.isEmpty() && usagePackages.isEmpty()) {
      return Optional.of(candidate);
    }

    // Generation scope: trusted types whose builders are generated in this compilation.
    if (generationPackages.includes(packageName)) {
      return Optional.of(candidate);
    }

    // Usage scope: types whose builders may be generated now or already compiled.
    if (usagePackages.includes(packageName)) {
      boolean builderAvailable =
          generatedTypeNames.contains(referencedType.getQualifiedName().toString())
              || context.getTypeElement(candidate.getFullQualifiedName()) != null;
      return builderAvailable ? Optional.of(candidate) : Optional.empty();
    }

    return Optional.empty();
  }

  private void refreshForConfigurationIfNeeded() {
    BuilderConfiguration configuration = context.getConfiguration();
    if (Objects.equals(cachedConfiguration, configuration)) {
      return;
    }
    generationPackages =
        configuration == null
            ? PackageScopes.unscoped()
            : configuration.builderGenerationPackages();
    usagePackages =
        configuration == null ? PackageScopes.unscoped() : configuration.builderUsagePackages();
    resolvedBuilderTypes.clear();
    cachedConfiguration = configuration;
  }

  private static boolean hasSimpleBuilderAnnotation(TypeElement typeElement) {
    if (typeElement == null) {
      return false;
    }
    return JavaLangAnalyser.findAnnotation(typeElement, SimpleBuilder.class).isPresent();
  }

  private static boolean isIgnoredForBuilderGeneration(TypeElement typeElement) {
    if (typeElement == null) {
      return false;
    }
    return JavaLangAnalyser.findAnnotation(typeElement, Ignore4BuilderGeneration.class).isPresent();
  }
}
