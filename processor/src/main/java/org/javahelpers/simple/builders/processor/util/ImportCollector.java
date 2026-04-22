package org.javahelpers.simple.builders.processor.util;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.annotation.InterfaceName;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.imports.ImportStatement;
import org.javahelpers.simple.builders.processor.model.imports.RegularImport;
import org.javahelpers.simple.builders.processor.model.method.MethodCodePlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive;
import org.javahelpers.simple.builders.processor.model.type.TypeNameVariable;

/**
 * Utility class for collecting, deduplicating, and sorting imports for generated classes.
 *
 * <p>This class centralizes all import-related logic that was previously scattered throughout
 * RoasterCodeGenerator, providing a clean and consistent approach to import management.
 *
 * <p>Uses {@link ImportStatement} objects internally to represent both regular and static imports,
 * providing type safety and eliminating string-based import handling.
 */
public class ImportCollector {

  private final Set<ImportStatement> imports = new LinkedHashSet<>();
  private final TypeName currentType;

  /**
   * Creates a new ImportCollector for the specified type.
   *
   * @param currentType the type of the class being generated
   */
  public ImportCollector(TypeName currentType) {
    this.currentType = currentType;
  }

  /**
   * Adds imports for a type.
   *
   * @param type the type to add imports for
   */
  public void addTypeImports(TypeName type) {
    if (type == null || type instanceof TypeNamePrimitive || type instanceof TypeNameVariable) {
      return;
    }

    // Add type annotation imports
    type.getAnnotations().forEach(this::addAnnotationImports);

    // Adding the type
    addImport(type);
  }

  /**
   * Adds imports for an annotation.
   *
   * @param annotation the annotation to add imports for
   */
  public void addAnnotationImports(AnnotationDto annotation) {
    if (annotation.getAnnotationType() != null) {
      addTypeImports(annotation.getAnnotationType());
    }
  }

  /**
   * Adds imports for a method parameter.
   *
   * @param parameter the parameter to add imports for
   */
  public void addParameterImports(MethodParameterDto parameter) {
    addTypeImports(parameter.getParameterType());
    parameter.getAnnotations().forEach(this::addAnnotationImports);
  }

  /**
   * Adds imports for an interface.
   *
   * @param interfaceName the interface to add imports for
   */
  public void addInterfaceImports(InterfaceName interfaceName) {
    // Create a TypeName from the interface's package and simple name
    TypeName interfaceType =
        new TypeName(interfaceName.getPackageName(), interfaceName.getSimpleName());
    addTypeImports(interfaceType);
    interfaceName.getAnnotations().forEach(this::addAnnotationImports);
    interfaceName.getTypeParameters().forEach(this::addTypeImports);
  }

  /**
   * Adds imports for a method's body and components.
   *
   * @param method the method to add imports for
   */
  public void addMethodImports(MethodDto method) {
    if (method.getReturnType() != null) {
      addTypeImports(method.getReturnType());
    }

    // Add generic parameter imports
    method
        .getGenericParameters()
        .forEach(generic -> generic.getUpperBounds().forEach(this::addTypeImports));

    // Add code block imports
    method.getMethodCodeDto().getCodeBlockImports().forEach(this::addImport);

    // Add annotation imports
    method.getAnnotations().forEach(this::addAnnotationImports);

    // Add parameter imports
    method.getParameters().forEach(this::addParameterImports);

    // Add body argument imports
    addBodyImports(method);
  }

  /**
   * Collects all imports from a GenerationTargetClassDto.
   *
   * @param classDef the class definition to extract imports from
   */
  public void collectImports(GenerationTargetClassDto classDef) {
    // Add imports from the DTO (including static imports like TrackedValue)
    classDef.getImports().forEach(this::addImport);

    // Add superclass imports
    if (classDef.getSuperType() != null) {
      addTypeImports(classDef.getSuperType());
    }

    // Add class type and generics
    addTypeImports(classDef.getTypeName());
    classDef
        .getGenerics()
        .forEach(generic -> generic.getUpperBounds().forEach(this::addTypeImports));

    // Add interface imports
    classDef.getInterfaces().forEach(this::addInterfaceImports);

    // Add class annotation imports
    classDef.getClassAnnotations().forEach(this::addAnnotationImports);

    // Add field imports from ClassFieldDto
    classDef.getClassFields().forEach(field -> addTypeImports(field.getFieldType()));

    // Add all additional imports defined for that field
    classDef.getClassFields().stream()
        .flatMap(field -> field.getFieldTypeImports().stream())
        .forEach(this::addImport);

    // Add constructor imports
    classDef
        .getConstructors()
        .forEach(
            constructor -> {
              constructor.getParameters().forEach(this::addParameterImports);
              constructor.getMethodCodeDto().getCodeBlockImports().forEach(this::addImport);
            });

    // Nested types are part of the same class and don't need imports
    // (e.g., the "With" interface is nested within the builder class)

    // Add method imports
    classDef.getMethods().forEach(this::addMethodImports);
  }

  /**
   * Adds imports from method body arguments.
   *
   * @param method the method to analyze
   */
  private void addBodyImports(MethodDto method) {
    if (!method.hasCode()) {
      return;
    }

    for (MethodCodePlaceholder<?> argument : method.getMethodCodeDto().getCodeArguments()) {
      if (argument instanceof MethodCodeTypePlaceholder typePlaceholder) {
        addTypeImports(typePlaceholder.getValue());
      }
    }
  }

  /**
   * Adds an import statement if it should not be skipped.
   *
   * <p>This method contains the business logic for filtering imports.
   *
   * @param importStatement the import statement to add
   */
  private void addImport(ImportStatement importStatement) {
    if (importStatement == null || shouldSkipImport(importStatement)) {
      return;
    }
    imports.add(importStatement);
  }

  /**
   * Determines if an import should be skipped (business logic).
   *
   * @param importStatement the import to check
   * @return true if the import should be skipped
   */
  private boolean shouldSkipImport(ImportStatement importStatement) {
    String fqn = importStatement.getFullyQualifiedName();

    if (StringUtils.isBlank(fqn)) {
      return true;
    }

    // For regular imports, skip if in exact same package or java.lang (per Java rules)
    if (!importStatement.isStatic()) {
      // Skip if it's in the exact same package (but not sub-packages) - Java doesn't require
      // imports for same-package classes
      if (Strings.CI.equals(importStatement.getPackageName(), currentType.getPackageName())) {
        return true;
      }

      // Skip java.lang package (auto-imported), but NOT subpackages like java.lang.annotation
      if (Strings.CI.equals(importStatement.getPackageName(), "java.lang")) {
        return true;
      }
    }

    // Static imports are never skipped based on package
    // (they're always needed to use the static member without qualification)
    return false;
  }

  /**
   * Adds a regular import for a type.
   *
   * @param type the type to import
   */
  private void addImport(TypeName type) {
    if (type == null) {
      return;
    }

    if (type instanceof TypeNameArray arrayType) {
      addImport(arrayType.getTypeOfArray());
      return;
    }

    if (type instanceof TypeNameGeneric genericType) {
      // Add the raw generic type (without type parameters)
      addImport(genericType.getRawType());

      // Add inner type arguments
      genericType.getInnerTypeArguments().forEach(this::addTypeImports);
      return;
    }

    // Add the concrete type
    addImport(new RegularImport(type));
  }

  /**
   * Returns the collected import statements as a sorted set.
   *
   * <p>Use this method when you want imports in alphabetical order. The sorting is done here to
   * provide flexibility for the calling code.
   *
   * <p>Static imports are placed before regular imports, following Java conventions. Within each
   * group, imports are sorted alphabetically by fully qualified name.
   *
   * @return sorted set of import statements
   */
  public Set<ImportStatement> getSortedImports() {
    Comparator<ImportStatement> staticFirstComparator =
        Comparator.comparing(ImportStatement::isStatic).reversed();
    Comparator<ImportStatement> alphabeticalComparator =
        Comparator.comparing(ImportStatement::getFullyQualifiedName);

    return getImports().stream()
        .sorted(staticFirstComparator.thenComparing(alphabeticalComparator))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Returns the collected import statements in insertion order.
   *
   * <p>This preserves the order in which imports were added, which can be useful for maintaining a
   * specific import ordering strategy.
   *
   * @return set of import statements in insertion order
   */
  public Set<ImportStatement> getImports() {
    return new LinkedHashSet<>(imports);
  }
}
