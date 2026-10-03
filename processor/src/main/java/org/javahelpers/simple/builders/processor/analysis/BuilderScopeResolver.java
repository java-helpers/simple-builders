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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import org.javahelpers.simple.builders.core.annotations.Ignore4BuilderGeneration;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.PackageScopes;
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
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

  /**
   * The inputs the resolution cache was built under. Cached resolutions are valid only while both
   * components are unchanged: the configuration determines the usage scope and the builder package
   * determines which contract members are accessible.
   */
  private record ResolutionInputs(BuilderConfiguration configuration, String builderPackage) {}

  // Factory method names preferred when a builder offers several candidates
  private static final List<String> PREFERRED_FACTORY_NAMES = List.of("create", "of");

  private final ProcessingContext context;
  private ResolutionInputs cachedResolutionInputs;
  private PackageScopes usagePackages = PackageScopes.unscoped();
  private final Map<String, Optional<ResolvedBuilder>> resolvedBuilderTypes = new HashMap<>();
  private final GeneratedBuilders generatedBuilders = new GeneratedBuilders();

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
   *       via {@link #registerGeneratedBuilder}), the registered builder name is returned
   *       immediately — trusted without a classpath lookup or contract check.
   *   <li>Otherwise, a static parameterless method on the referenced type itself returning a
   *       contract-satisfying type (e.g. {@code builder()}) anchors the builder — the type's own
   *       declaration wins over the same-package candidate.
   *   <li>Otherwise, the candidate builder name is constructed using {@code builderUsageSuffix}
   *       (falling back to {@code builderSuffix} if not configured). The candidate is looked up on
   *       the classpath and returned if it satisfies the builder contract: an instantiation path
   *       for an empty builder (no-arg constructor or static factory like {@code create()}), an
   *       instantiation path seeded with the value (constructor accepting the referenced type or
   *       static factory like {@code create(T)}/{@code of(T)}), and a no-arg {@code build()} method
   *       returning it - each accessible from the generated builder's package. The contract check
   *       is annotation-agnostic, so builders generated with custom template annotations, external
   *       tools, or different suffixes are supported. The referenced type must not be opted out
   *       with {@code @Ignore4BuilderGeneration}.
   * </ol>
   *
   * @param referencedType the type element being referenced as a field or collection element
   * @return the resolved builder to reference, or empty if no builder should be referenced
   */
  public Optional<ResolvedBuilder> resolveUsableBuilderType(TypeElement referencedType) {
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
   * Registers one builder generated in the current processing round.
   *
   * <p>The builder type name is stored explicitly because it may differ from the default naming in
   * the target type's own package - for example for {@code @SimpleBuilderFor} targets, whose
   * builders are generated in the package of the annotated holder.
   *
   * <p>Registered builders are trusted during resolution without a classpath lookup. Adding an
   * entry also resets the resolution cache so previously resolved results do not go stale.
   *
   * @param targetType the type a builder is generated for
   * @param builderType the generated builder's type name
   */
  public void registerGeneratedBuilder(TypeName targetType, TypeName builderType) {
    generatedBuilders.add(targetType, builderType);
    resolvedBuilderTypes.clear();
  }

  /**
   * Resets the generated-builders registry and the resolution cache, e.g. at the start of a new
   * processing round, so stale registrations and resolutions of the previous round are dropped.
   */
  public void resetGeneratedBuilders() {
    generatedBuilders.clear();
    resolvedBuilderTypes.clear();
  }

  private Optional<ResolvedBuilder> resolve(TypeElement referencedType) {
    if (referencedType == null || isIgnoredForBuilderGeneration(referencedType)) {
      return Optional.empty();
    }

    String packageName = context.getPackageName(referencedType);
    TypeName referencedTypeName = JavaLangMapper.mapToTypeName(referencedType, context);

    // The usage scope determines whether a type is eligible to be referenced as a builder
    // helper. When empty, any package is allowed (backward compatibility). When set, only
    // packages in the scope qualify. The scope already includes generation-scope packages.
    if (!usagePackages.isEmpty() && !usagePackages.includes(packageName)) {
      return Optional.empty();
    }

    // Types whose builders are generated in the current processing round are trusted
    // immediately — our own generators always produce the builder contract, so no
    // classpath lookup or contract check is needed.
    Optional<TypeName> generatedBuilder = generatedBuilders.findBuilder(referencedTypeName);
    if (generatedBuilder.isPresent()) {
      // Our generators always emit a static create() and no create(T) - the empty path uses
      // the factory, the copy path the constructor
      return generatedBuilder.map(
          builder ->
              new ResolvedBuilder(
                  builder,
                  new BuilderInstantiation.StaticFactoryCall("create"),
                  new BuilderInstantiation.ConstructorCall()));
    }

    // A type may anchor its builder inside itself: an accessible static parameterless method
    // returning a contract-satisfying type (MapStruct-style `Person.builder()`). The type's own
    // declaration wins over the same-package candidate below.
    Optional<ResolvedBuilder> inTypeBuilder =
        resolveInTypeBuilder(referencedType, referencedTypeName);
    if (inTypeBuilder.isPresent()) {
      return inTypeBuilder;
    }

    // For types not generated in this round, look up the candidate on the classpath using
    // builderUsageSuffix (which falls back to builderSuffix if not configured) and verify
    // the builder contract.
    String suffix = context.getConfiguration().getBuilderUsageSuffix();
    TypeName candidate = JavaLangMapper.createBuilderTypeName(referencedType, context, suffix);
    return resolveByBuilderContract(candidate, referencedTypeName);
  }

  /**
   * Looks up the candidate builder type on the classpath and verifies it satisfies the builder
   * contract: a way to create an empty instance (a no-arg constructor or a static parameterless
   * factory like {@code create()}), a way to create an instance seeded with a value (a constructor
   * accepting the referenced type or a static factory like {@code create(T)}/{@code of(T)}), and a
   * no-arg {@code build()} method returning it - each accessible from the generated builder's
   * package, since the generated code calls them from there. The contract check is
   * annotation-agnostic, so builders generated with custom template annotations or from external
   * sources are supported as long as they follow the builder contract. It also avoids false
   * positives like {@code String} → {@code StringBuilder}.
   *
   * @param candidate the candidate builder type name to look up
   * @param expectedType the referenced type the builder must accept and return
   * @return the resolved builder with the instantiation paths to call, or empty if no matching
   *     builder class exists on the classpath
   */
  private Optional<ResolvedBuilder> resolveByBuilderContract(
      TypeName candidate, TypeName expectedType) {
    TypeElement builderTypeElement = context.getTypeElement(candidate.getFullQualifiedName());
    if (builderTypeElement == null) {
      return Optional.empty();
    }
    Optional<BuilderInstantiation> funcForEmptyBuilder =
        resolveFuncForEmptyBuilder(builderTypeElement);
    Optional<BuilderInstantiation> funcForPrefilledBuilder =
        resolveFuncForPrefilledBuilder(builderTypeElement, expectedType);
    if (funcForEmptyBuilder.isEmpty()
        || funcForPrefilledBuilder.isEmpty()
        || !JavaLangAnalyser.hasBuildMethodReturning(builderTypeElement, expectedType, context)) {
      return Optional.empty();
    }
    return Optional.of(
        new ResolvedBuilder(candidate, funcForEmptyBuilder.get(), funcForPrefilledBuilder.get()));
  }

  /**
   * Resolves the instantiation path for an empty builder instance: a static parameterless factory
   * when the builder offers one, the no-arg constructor otherwise.
   *
   * @param builderTypeElement the candidate builder type to inspect
   * @return the instantiation to emit, or empty when the builder offers neither
   */
  /**
   * Resolves a builder anchored inside the referenced type itself: an accessible static
   * parameterless method on the type returning a type that satisfies the builder contract, like the
   * Lombok/Immutables-style {@code Person.builder()} MapStruct also detects. The conventional
   * {@code builder} method name wins over other candidate names; declaration order decides between
   * equals.
   *
   * @param referencedType the type element being referenced
   * @param referencedTypeName the referenced type, passed to the contract check
   * @return the resolved builder, or empty when the type anchors no contract-satisfying builder
   */
  private Optional<ResolvedBuilder> resolveInTypeBuilder(
      TypeElement referencedType, TypeName referencedTypeName) {
    List<ExecutableElement> factories =
        JavaLangAnalyser.findMethodsStatic(referencedType, List.of(), context).stream()
            .filter(method -> method.getReturnType().getKind() == TypeKind.DECLARED)
            .sorted(
                Comparator.comparingInt(
                    method -> "builder".contentEquals(method.getSimpleName()) ? 0 : 1))
            .toList();
    for (ExecutableElement factory : factories) {
      TypeElement candidate = (TypeElement) ((DeclaredType) factory.getReturnType()).asElement();
      Optional<ResolvedBuilder> resolved =
          resolveByBuilderContract(
              JavaLangMapper.mapToTypeName(candidate, context), referencedTypeName);
      if (resolved.isPresent()) {
        return resolved;
      }
    }
    return Optional.empty();
  }

  private Optional<BuilderInstantiation> resolveFuncForEmptyBuilder(
      TypeElement builderTypeElement) {
    Optional<BuilderInstantiation> func = findStaticFactoryCall(builderTypeElement, List.of());
    if (func.isEmpty() && JavaLangAnalyser.hasEmptyConstructor(builderTypeElement, context)) {
      func = Optional.of(new BuilderInstantiation.ConstructorCall());
    }
    return func;
  }

  /**
   * Resolves the instantiation path for a builder instance seeded with a value of the referenced
   * type: a static factory accepting the type when the builder offers one, the constructor
   * accepting the type otherwise.
   *
   * @param builderTypeElement the candidate builder type to inspect
   * @param expectedType the referenced type to seed the builder with
   * @return the instantiation to emit, or empty when the builder offers neither
   */
  private Optional<BuilderInstantiation> resolveFuncForPrefilledBuilder(
      TypeElement builderTypeElement, TypeName expectedType) {
    Optional<BuilderInstantiation> func =
        findStaticFactoryCall(builderTypeElement, List.of(expectedType));
    if (func.isEmpty()
        && JavaLangAnalyser.hasConstructorAccepting(builderTypeElement, expectedType, context)) {
      func = Optional.of(new BuilderInstantiation.ConstructorCall());
    }
    return func;
  }

  /**
   * Finds an accessible static method on the builder type taking the expected parameter types and
   * returning the builder type itself. When several candidates exist, {@code create} and {@code of}
   * are preferred in that order, then the alphabetically first name.
   *
   * @param builderTypeElement the builder type to inspect
   * @param expectedParameterTypes the parameter types the factory must accept
   * @return the instantiation calling the found factory, or empty when none exists
   */
  private Optional<BuilderInstantiation> findStaticFactoryCall(
      TypeElement builderTypeElement, List<TypeName> expectedParameterTypes) {
    TypeName builderTypeName = JavaLangMapper.mapToTypeName(builderTypeElement, context);
    return JavaLangAnalyser.findMethodsStatic(
            builderTypeElement, expectedParameterTypes, builderTypeName, context)
        .stream()
        .min(
            Comparator.comparingInt(BuilderScopeResolver::preferredFactoryNameRank)
                .thenComparing(Comparator.naturalOrder()))
        .map(BuilderInstantiation.StaticFactoryCall::new);
  }

  private static int preferredFactoryNameRank(String name) {
    int index = PREFERRED_FACTORY_NAMES.indexOf(name);
    return index < 0 ? PREFERRED_FACTORY_NAMES.size() : index;
  }

  private void refreshForConfigurationIfNeeded() {
    BuilderConfiguration configuration = context.getConfiguration();
    ResolutionInputs inputs = new ResolutionInputs(configuration, context.getBuilderPackageName());
    if (inputs.equals(cachedResolutionInputs)) {
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
    cachedResolutionInputs = inputs;
  }

  private static boolean isIgnoredForBuilderGeneration(TypeElement typeElement) {
    if (typeElement == null) {
      return false;
    }
    return JavaLangAnalyser.findAnnotation(typeElement, Ignore4BuilderGeneration.class).isPresent();
  }
}
