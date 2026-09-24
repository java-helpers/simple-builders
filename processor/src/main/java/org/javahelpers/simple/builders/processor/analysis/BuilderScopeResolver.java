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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.PackageScopes;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * Central resolver that decides whether a builder may be referenced for a given type.
 *
 * <p>This resolver is independent of generator/enhancer code; it only relies on the configured
 * {@code builderUsagePackages} scope (which includes {@code builderGenerationPackages}
 * automatically), registered generated types, and the availability of builder types on the
 * classpath.
 *
 * <p>The resolver is constructed once per processing context and refreshes its parsed package
 * scopes and per-type results when the target configuration changes. Types whose builders are
 * generated in the current processing round are registered before resolution.
 */
public final class BuilderScopeResolver {

  private final ProcessingContext context;
  private BuilderConfiguration cachedConfiguration;
  private PackageScopes usagePackages = PackageScopes.unscoped();
  private final Map<String, Optional<TypeName>> resolvedBuilderTypes = new HashMap<>();
  private final GeneratedBuilders generatedBuilders;

  /**
   * Creates a new resolver for the given processing context.
   *
   * @param context the processing context providing configuration and type utilities
   */
  public BuilderScopeResolver(ProcessingContext context) {
    this.context = context;
    this.generatedBuilders = new GeneratedBuilders(resolvedBuilderTypes::clear);
  }

  /**
   * Resolves the builder type to use for the given referenced type, if any.
   *
   * <p>This method reads the configuration from the processing context via {@link
   * org.javahelpers.simple.builders.processor.processing.ProcessingContext#getConfiguration()}. The
   * caller must ensure that {@link
   * org.javahelpers.simple.builders.processor.processing.ProcessingContext#initProcessingTarget}
   * has been invoked with the owner element's resolved configuration beforehand, so that
   * per-element {@code builderUsagePackages} overrides are respected.
   *
   * <p>The decision follows these rules:
   *
   * <ol>
   *   <li>If the usage scope is set and the referenced type's package is not in it, no builder may
   *       be referenced. The usage scope includes generation-scope packages automatically. When the
   *       scope is empty, any package is allowed (backward compatibility).
   *   <li>If the referenced type's builder is generated in the current processing round (registered
   *       via {@link #generatedBuilders()}), the registered builder name is returned immediately —
   *       trusted without a classpath lookup or contract check.
   *   <li>Otherwise, the candidate builder name is constructed using {@code builderUsageSuffix}
   *       (falling back to {@code builderSuffix} if not configured). The candidate is looked up on
   *       the classpath and returned if it satisfies the builder contract: a constructor accepting
   *       the referenced type and a no-arg {@code build()} method returning it. The contract check
   *       is annotation-agnostic, so builders generated with custom template annotations, external
   *       tools, or different suffixes are supported. The referenced type must not be opted out
   *       with {@code @Ignore4BuilderGeneration}.
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
   * Checks whether a builder may be generated for the given element under the generation scope of
   * the resolved configuration.
   *
   * <p>Unlike {@link #resolveUsableBuilderType(TypeElement)}, this method takes the configuration
   * as an explicit parameter rather than reading it from the processing context. This is because it
   * is called during generation-plan resolution, before {@link
   * org.javahelpers.simple.builders.processor.processing.ProcessingContext#initProcessingTarget}
   * has been invoked for the element, so the context does not yet hold the per-element
   * configuration.
   *
   * <p>An unscoped {@code builderGenerationPackages} allows every element. Otherwise the element's
   * package must match the scope; skipped elements are logged at debug level.
   *
   * @param element the annotated element to check
   * @param configuration the configuration resolved for that element
   * @return true if a builder may be generated for the element
   */
  public boolean isInGenerationScope(Element element, BuilderConfiguration configuration) {
    PackageScopes scopes = configuration.builderGenerationPackages();
    if (scopes.isEmpty()) {
      return true;
    }
    String packageName = context.getPackageName(element);
    if (!scopes.includes(packageName)) {
      context.debug(
          "Skipping %s: package '%s' is not in builderGenerationPackages",
          element.getSimpleName(), packageName);
      return false;
    }
    return true;
  }

  /**
   * Returns the registry of builders generated in the current processing round.
   *
   * <p>The processor adds an entry per planned builder before resolution starts, so generated
   * builders are trusted without a classpath lookup. Mutations invalidate the resolver's per-type
   * resolution cache automatically.
   *
   * @return the generated-builders registry
   */
  public GeneratedBuilders generatedBuilders() {
    return generatedBuilders;
  }

  private Optional<TypeName> resolve(TypeElement referencedType) {
    if (referencedType == null || isIgnoredForBuilderGeneration(referencedType)) {
      return Optional.empty();
    }

    String referencedTypeFqn = referencedType.getQualifiedName().toString();
    String packageName = context.getPackageName(referencedType);

    // The usage scope determines whether a type is eligible to be referenced as a builder
    // helper. When empty, any package is allowed (backward compatibility). When set, only
    // packages in the scope qualify. The scope already includes generation-scope packages.
    if (!usagePackages.isEmpty() && !usagePackages.includes(packageName)) {
      return Optional.empty();
    }

    // Types whose builders are generated in the current processing round are trusted
    // immediately — our own generators always produce the builder contract, so no
    // classpath lookup or contract check is needed.
    Optional<TypeName> generatedBuilder = generatedBuilders.findBuilder(referencedTypeFqn);
    if (generatedBuilder.isPresent()) {
      return generatedBuilder;
    }

    // For types not generated in this round, look up the candidate on the classpath using
    // builderUsageSuffix (which falls back to builderSuffix if not configured) and verify
    // the builder contract.
    String suffix = context.getConfiguration().getBuilderUsageSuffix();
    TypeName candidate = JavaLangMapper.createBuilderTypeName(referencedType, context, suffix);
    return resolveByBuilderContract(candidate, referencedTypeFqn);
  }

  /**
   * Looks up the candidate builder type on the classpath and verifies it satisfies the builder
   * contract: a constructor accepting the referenced type and a no-arg {@code build()} method
   * returning it. The contract check is annotation-agnostic, so builders generated with custom
   * template annotations or from external sources are supported as long as they follow the builder
   * contract. It also avoids false positives like {@code String} → {@code StringBuilder}.
   *
   * @param candidate the candidate builder type name to look up
   * @param expectedType the qualified name of the referenced type the builder must accept and
   *     return
   * @return the candidate if a matching builder class exists on the classpath, empty otherwise
   */
  private Optional<TypeName> resolveByBuilderContract(TypeName candidate, String expectedType) {
    TypeElement builderTypeElement = context.getTypeElement(candidate.getFullQualifiedName());
    if (builderTypeElement == null) {
      return Optional.empty();
    }
    if (!JavaLangAnalyser.hasConstructorAccepting(builderTypeElement, expectedType, context)) {
      return Optional.empty();
    }
    if (!JavaLangAnalyser.hasBuildMethodReturning(builderTypeElement, expectedType, context)) {
      return Optional.empty();
    }
    return Optional.of(candidate);
  }

  private void refreshForConfigurationIfNeeded() {
    BuilderConfiguration configuration = context.getConfiguration();
    if (Objects.equals(cachedConfiguration, configuration)) {
      return;
    }
    // The effective usage scope combines builderUsagePackages and builderGenerationPackages,
    // since generation-scope packages are automatically included in the usage scope.
    PackageScopes generation =
        configuration == null
            ? PackageScopes.unscoped()
            : configuration.builderGenerationPackages();
    PackageScopes usage =
        configuration == null ? PackageScopes.unscoped() : configuration.builderUsagePackages();
    usagePackages = PackageScopes.merge(generation, usage);
    resolvedBuilderTypes.clear();
    cachedConfiguration = configuration;
  }

  private static boolean isIgnoredForBuilderGeneration(TypeElement typeElement) {
    if (typeElement == null) {
      return false;
    }
    return JavaLangAnalyser.findAnnotation(typeElement, Ignore4BuilderGeneration.class).isPresent();
  }
}
