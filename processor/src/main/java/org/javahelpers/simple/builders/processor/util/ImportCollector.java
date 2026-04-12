package org.javahelpers.simple.builders.processor.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.annotation.InterfaceName;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
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
 */
public class ImportCollector {

  private final Set<String> imports = new LinkedHashSet<>();
  private final String currentPackage;

  /**
   * Creates a new ImportCollector for the specified package.
   *
   * @param currentPackage the package of the class being generated
   */
  public ImportCollector(String currentPackage) {
    this.currentPackage = currentPackage;
  }

  /**
   * Adds all necessary imports for a type.
   *
   * @param type the type to add imports for
   */
  public void addTypeImports(TypeName type) {
    if (type == null || type instanceof TypeNamePrimitive || type instanceof TypeNameVariable) {
      return;
    }

    // Add type annotation imports
    type.getAnnotations().forEach(this::addAnnotationImports);

    if (type instanceof TypeNameArray arrayType) {
      addTypeImports(arrayType.getTypeOfArray());
      return;
    }

    if (type instanceof TypeNameGeneric genericType) {
      // Add the raw generic type (without type parameters)
      String rawType = genericType.getFullQualifiedName().replaceAll("<.*$", "");
      addImport(rawType);

      // Add inner type arguments
      genericType.getInnerTypeArguments().forEach(this::addTypeImports);
      return;
    }

    // Add the concrete type
    addImport(type.getFullQualifiedName());
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
    method.getMethodCodeDto().getCodeBlockImports().forEach(this::addTypeImports);

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
    classDef
        .getClassFields()
        .forEach(
            field -> {
              addTypeImports(field.getFieldType());
              field.getFieldTypeImports().forEach(this::addTypeImports);
            });

    // Add constructor imports
    classDef
        .getConstructors()
        .forEach(
            constructor -> {
              constructor.getParameters().forEach(this::addParameterImports);
              constructor.getMethodCodeDto().getCodeBlockImports().forEach(this::addTypeImports);
            });

    // Add nested type imports
    classDef
        .getNestedTypes()
        .forEach(
            nestedType -> {
              addTypeImports(
                  new TypeName(classDef.getTypeName().getPackageName(), nestedType.getTypeName()));
              // NestedTypeDto doesn't have type parameters, so we skip that part
            });

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

    // Extract type references from code format (simplified approach)
    String code = method.getMethodCodeDto().getCodeFormat();
    extractTypeReferencesFromCode(code);
  }

  /**
   * Extracts type references from code format string.
   *
   * @param code the code to analyze
   */
  private void extractTypeReferencesFromCode(String code) {
    if (StringUtils.isBlank(code)) {
      return;
    }

    // Skip automatic extraction for now to avoid invalid identifiers
    // The explicit imports from code arguments and code block imports should be sufficient
    // This could be enhanced later with more sophisticated parsing
  }

  /**
   * Adds an import if it's not in the current package and not already added.
   *
   * @param fqn the fully qualified name to add
   */
  private void addImport(String fqn) {
    if (StringUtils.isBlank(fqn)) {
      return;
    }

    // Skip if it's in the current package
    if (fqn.startsWith(currentPackage)) {
      return;
    }

    // Skip java.lang package (auto-imported)
    if (fqn.startsWith("java.lang.")) {
      return;
    }

    imports.add(fqn);
  }

  /**
   * Returns the collected imports as a sorted set.
   *
   * <p>Use this method when you want imports in alphabetical order. The sorting is done here to
   * provide flexibility for the calling code.
   *
   * @return sorted set of import statements
   */
  public Set<String> getSortedImports() {
    return new TreeSet<>(imports);
  }

  /**
   * Returns the collected imports in insertion order.
   *
   * <p>This preserves the order in which imports were added, which can be useful for maintaining a
   * specific import ordering strategy.
   *
   * @return set of import statements in insertion order
   */
  public Set<String> getImports() {
    return new LinkedHashSet<>(imports);
  }

  /** Clears all collected imports. */
  public void clear() {
    imports.clear();
  }

  /**
   * Returns the number of collected imports.
   *
   * @return the number of imports
   */
  public int size() {
    return imports.size();
  }
}
