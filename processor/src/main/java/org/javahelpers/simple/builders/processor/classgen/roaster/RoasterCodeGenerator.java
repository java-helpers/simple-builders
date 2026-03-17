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

package org.javahelpers.simple.builders.processor.classgen.roaster;

import static javax.lang.model.element.Modifier.PUBLIC;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapAnnotation;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapAnnotations;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapBoxedType;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapGenericDeclaration;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapType;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.quote;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.resolveCodeTemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.util.TrackedValue;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.core.FieldDto;
import org.javahelpers.simple.builders.processor.model.integration.JacksonModuleDefinitionDto;
import org.javahelpers.simple.builders.processor.model.integration.JacksonModuleEntryDto;
import org.javahelpers.simple.builders.processor.model.method.MethodCodePlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.NestedTypeDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.model.type.TypeNameGeneric;
import org.javahelpers.simple.builders.processor.model.type.TypeNamePrimitive;
import org.javahelpers.simple.builders.processor.model.type.TypeNameVariable;
import org.javahelpers.simple.builders.processor.processing.ProcessingLogger;

/** Roaster-based code generator for builder source files. */
public class RoasterCodeGenerator {
  private static final String INDENT = "  ";
  private static final String DOUBLE_INDENT = INDENT + INDENT;
  private static final String TRIPLE_INDENT = DOUBLE_INDENT + INDENT;

  /** Processing environment for accessing filer and element utilities. */
  private final ProcessingEnvironment processingEnv;

  /** Logger for debug output during code generation. */
  private final ProcessingLogger logger;

  /**
   * Constructor for RoasterCodeGenerator.
   *
   * @param processingEnv Processing environment for accessing filer and element utilities
   * @param logger Logger for debug output
   */
  public RoasterCodeGenerator(ProcessingEnvironment processingEnv, ProcessingLogger logger) {
    this.processingEnv = processingEnv;
    this.logger = logger;
  }

  /**
   * Generates a builder class from the given builder definition.
   *
   * @param builderDef dto of all information to create the builder
   * @throws BuilderException if there is an error in source code generation
   */
  public void generateBuilder(BuilderDefinitionDto builderDef) throws BuilderException {
    logger.debugStartOperation(
        "Code generation for builder: %s", builderDef.getBuilderTypeName().getClassName());

    String sourceCode = createBuilderSource(builderDef);
    writeBuilderClassToFile(sourceCode, builderDef);

    logger.debugEndOperation(
        "Successfully generated builder: %s", builderDef.getBuilderTypeName().getClassName());
  }

  private String createBuilderSource(BuilderDefinitionDto builderDef) {
    logger.debug("Creating builder source for %s", builderDef.getBuilderTypeName());

    StringBuilder source = new StringBuilder();
    String packageName = builderDef.getBuilderTypeName().getPackageName();
    if (StringUtils.isNotBlank(packageName)) {
      source.append("package ").append(packageName).append(";\n\n");
    }
    appendTrackedValueStaticImports(source);
    appendImports(source, collectImports(builderDef));

    logger.debug("Class builder created");

    appendClassJavadoc(source, builderDef.getClassJavadoc(), "");
    logger.debug("Class metadata added");
    appendAnnotations(source, builderDef.getClassAnnotations(), "");
    source.append(buildClassHeader(builderDef)).append(" {\n\n");

    appendFields(source, builderDef);
    appendConstructors(source, builderDef);
    appendMethods(source, builderDef);
    appendNestedTypes(source, builderDef);
    logger.debug("Class-level annotations added");

    trimTrailingBlankLinesBeforeClassClosingBrace(source);
    source.append("\n}\n");
    logger.debug("Builder source created");
    return formatSource(source.toString());
  }

  private void trimTrailingBlankLinesBeforeClassClosingBrace(StringBuilder source) {
    while (source.length() > 0 && Character.isWhitespace(source.charAt(source.length() - 1))) {
      source.deleteCharAt(source.length() - 1);
    }
  }

  private String buildClassHeader(BuilderDefinitionDto builderDef) {
    StringBuilder header = new StringBuilder();
    Modifier builderAccessModifier =
        JavaLangMapper.mapAccessModifier(builderDef.getConfiguration().getBuilderAccess());
    if (builderAccessModifier != null) {
      header.append(toSourceModifier(builderAccessModifier)).append(" ");
    }
    header
        .append("class ")
        .append(builderDef.getBuilderTypeName().getClassName())
        .append(mapGenericDeclaration(builderDef.getGenerics()));

    if (CollectionUtils.isNotEmpty(builderDef.getInterfaces())) {
      String interfaces =
          builderDef.getInterfaces().stream()
              .map(RoasterMapper::mapInterfaceToTypeName)
              .reduce((a, b) -> a + ", " + b)
              .orElse("");
      header.append(" implements ").append(interfaces);
    }
    return header.toString();
  }

  private void appendFields(StringBuilder source, BuilderDefinitionDto builderDef) {
    logger.debugStartOperation(
        "Generating %d constructor fields and %d setter fields",
        builderDef.getConstructorFieldsForBuilder().size(),
        builderDef.getSetterFieldsForBuilder().size());

    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      appendField(source, fieldDto);
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      appendField(source, fieldDto);
    }

    logger.debugEndOperation("Fields added: %d fields", builderDef.getAllFieldsForBuilder().size());
  }

  private void appendField(StringBuilder source, FieldDto fieldDto) {
    String boxedFieldType = mapBoxedType(fieldDto.getFieldType());
    appendJavadoc(
        source,
        "Tracked value for <code>%s</code>: %s."
            .formatted(
                fieldDto.getFieldNameInBuilder(), StringUtils.defaultString(fieldDto.getJavaDoc())),
        INDENT);
    source
        .append(INDENT)
        .append("private ")
        .append(TrackedValue.class.getSimpleName())
        .append("<")
        .append(boxedFieldType)
        .append("> ")
        .append(fieldDto.getFieldNameInBuilder())
        .append(" = ")
        .append("unsetValue();\n\n");
  }

  private void appendConstructors(StringBuilder source, BuilderDefinitionDto builderDef) {
    generateConstructors(source, builderDef);
    logger.debug("Constructors added");
  }

  private void generateConstructors(StringBuilder source, BuilderDefinitionDto builderDef) {
    Modifier constructorAccessModifier =
        JavaLangMapper.mapAccessModifier(
            builderDef.getConfiguration().getBuilderConstructorAccess());
    TypeName dtoBaseClass = builderDef.getBuildingTargetTypeName();

    appendEmptyConstructor(
        source,
        dtoBaseClass,
        builderDef.getBuilderTypeName().getClassName(),
        constructorAccessModifier);
    source.append("\n");
    appendConstructorWithInstance(
        source,
        dtoBaseClass,
        builderDef.getBuilderTypeName().getClassName(),
        builderDef.getAllFieldsForBuilder(),
        constructorAccessModifier);
    source.append("\n");
  }

  private void appendEmptyConstructor(
      StringBuilder source, TypeName dtoClass, String builderClassName, Modifier accessModifier) {
    appendJavadoc(
        source,
        "Empty constructor of builder for {@code %s}.".formatted(dtoClass.getFullQualifiedName()),
        INDENT);
    source
        .append(INDENT)
        .append(buildMethodPrefix(accessModifier, false, null))
        .append(builderClassName)
        .append("() {\n")
        .append(INDENT)
        .append("}\n");
  }

  private void appendConstructorWithInstance(
      StringBuilder source,
      TypeName dtoBaseClass,
      String builderClassName,
      List<FieldDto> fields,
      Modifier accessModifier) {
    appendJavadoc(
        source,
        """
        Initialisation of builder for {@code %s} by a instance.

        @param instance object instance for initialisiation
        """
            .formatted(dtoBaseClass.getFullQualifiedName()),
        INDENT);
    source
        .append(INDENT)
        .append(buildMethodPrefix(accessModifier, false, null))
        .append(builderClassName)
        .append("(")
        .append(mapType(dtoBaseClass))
        .append(" instance) {\n");

    String body = buildConstructorBody(fields);
    appendIndentedBody(source, body, DOUBLE_INDENT);
    source.append(INDENT).append("}\n");
  }

  private String buildConstructorBody(List<FieldDto> fields) {
    StringBuilder body = new StringBuilder();
    for (FieldDto field : fields) {
      field
          .getGetterName()
          .ifPresent(getter -> addFieldInitializationWithValidation(body, field, getter));
    }
    return body.toString();
  }

  private void addFieldInitializationWithValidation(
      StringBuilder body, FieldDto field, String getter) {
    String fieldInBuilder = field.getFieldNameInBuilder();
    body.append("this.")
        .append(fieldInBuilder)
        .append(" = initialValue(instance.")
        .append(getter)
        .append("());\n");

    if (field.isNonNullable()) {
      body.append("if (this.")
          .append(fieldInBuilder)
          .append(".value() == null) {\n")
          .append(INDENT)
          .append("throw new ")
          .append(IllegalArgumentException.class.getSimpleName())
          .append("(")
          .append(
              quote(
                  "Cannot initialize builder from instance: field '"
                      + fieldInBuilder
                      + "' is marked as non-null but source object has null value"))
          .append(");\n")
          .append("}\n");
    }
  }

  private void appendMethods(StringBuilder source, BuilderDefinitionDto builderDef) {
    Map<MethodDto, FieldDto> allMethods = collectAllMethods(builderDef);
    logger.debugStartOperation("Adding Methods for %d candidates", allMethods.size());

    List<MethodDto> resolvedMethods = resolveMethodConflicts(allMethods);
    logger.debug("Resolved %d methods after conflict resolution", resolvedMethods.size());

    int generatedCnt = 0;
    for (MethodDto methodDto : resolvedMethods) {
      appendMethod(source, methodDto, false, false);
      source.append("\n");
      generatedCnt++;
    }
    logger.debugEndOperation("%d Methods added", generatedCnt);
  }

  private Map<MethodDto, FieldDto> collectAllMethods(BuilderDefinitionDto builderDef) {
    Map<MethodDto, FieldDto> allMethods = new HashMap<>();

    for (FieldDto fieldDto : builderDef.getConstructorFieldsForBuilder()) {
      for (MethodDto method : fieldDto.getMethods()) {
        allMethods.put(method, fieldDto);
      }
    }
    for (FieldDto fieldDto : builderDef.getSetterFieldsForBuilder()) {
      for (MethodDto method : fieldDto.getMethods()) {
        allMethods.put(method, fieldDto);
      }
    }
    for (MethodDto coreMethod : builderDef.getCoreMethods()) {
      allMethods.put(coreMethod, null);
    }

    return allMethods;
  }

  private List<MethodDto> resolveMethodConflicts(Map<MethodDto, FieldDto> methodToField) {
    MethodDto.MethodComparator comparator = new MethodDto.MethodComparator();

    List<Map.Entry<MethodDto, FieldDto>> sortedEntries =
        methodToField.entrySet().stream()
            .sorted((e1, e2) -> comparator.compare(e1.getKey(), e2.getKey()))
            .toList();

    Map<String, MethodDto> signatureToMethod = new java.util.LinkedHashMap<>();

    for (Map.Entry<MethodDto, FieldDto> entry : sortedEntries) {
      MethodDto method = entry.getKey();
      FieldDto field = entry.getValue();
      String signature = method.getSignatureKey();

      MethodDto existing = signatureToMethod.get(signature);
      if (existing == null) {
        signatureToMethod.put(signature, method);
      } else {
        String existingSource = getSourceDescription(existing, methodToField.get(existing));
        String newSource = getSourceDescription(method, field);

        if (method.getPriority() > existing.getPriority()) {
          signatureToMethod.put(signature, method);
          logger.warning(
              "  Method conflict: '%s' from %s (priority %d) dropped in favor of %s (priority %d)",
              signature, existingSource, existing.getPriority(), newSource, method.getPriority());
        } else if (method.getPriority() < existing.getPriority()) {
          logger.warning(
              "  Method conflict: '%s' from %s (priority %d) dropped in favor of %s (priority %d)",
              signature, newSource, method.getPriority(), existingSource, existing.getPriority());
        } else {
          logger.warning(
              "  Method conflict: '%s' from %s (priority %d) dropped in favor of %s (priority %d) - equal priority, keeping first",
              signature, newSource, method.getPriority(), existingSource, existing.getPriority());
        }
      }
    }

    return new java.util.ArrayList<>(signatureToMethod.values());
  }

  private String getSourceDescription(MethodDto method, FieldDto field) {
    if (field == null) {
      return "core method '" + method.getMethodName() + "'";
    }
    return "field '" + field.getFieldNameInBuilder() + "'";
  }

  private void appendMethod(
      StringBuilder source,
      MethodDto methodDto,
      boolean nestedTypeMethod,
      boolean interfaceMethod) {
    appendJavadoc(source, methodDto.getJavadoc(), INDENT);
    appendAnnotations(source, methodDto.getAnnotations(), INDENT);

    source
        .append(INDENT)
        .append(buildMethodSignature(methodDto, nestedTypeMethod, interfaceMethod))
        .append(" {");

    String body =
        methodDto.getMethodCodeDto() != null
                && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())
            ? resolveCodeTemplate(methodDto.getMethodCodeDto())
            : "";

    if (StringUtils.isNotBlank(body)) {
      source.append("\n");
      appendIndentedBody(source, body, DOUBLE_INDENT);
      source.append(INDENT);
    }
    source.append("}\n");
  }

  private String buildMethodSignature(
      MethodDto methodDto, boolean nestedTypeMethod, boolean interfaceMethod) {
    StringBuilder signature = new StringBuilder();
    Modifier modifier = methodDto.getModifier().orElse(null);
    String genericDeclaration = mapGenericDeclaration(methodDto.getGenericParameters());

    if (nestedTypeMethod) {
      signature.append(PUBLIC.toString().toLowerCase()).append(" ");
      if (interfaceMethod
          && methodDto.getMethodCodeDto() != null
          && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())) {
        signature.append("default ");
      }
    } else {
      signature.append(buildMethodPrefix(modifier, methodDto.isStatic(), genericDeclaration));
    }

    if (nestedTypeMethod && StringUtils.isNotBlank(genericDeclaration)) {
      signature.append(genericDeclaration).append(" ");
    }

    signature
        .append(mapType(methodDto.getReturnType()))
        .append(" ")
        .append(methodDto.getMethodName())
        .append("(")
        .append(buildParameterList(methodDto.getParameters()))
        .append(")");
    return signature.toString();
  }

  private String buildMethodPrefix(Modifier modifier, boolean isStatic, String genericDeclaration) {
    StringBuilder prefix = new StringBuilder();
    if (modifier != null) {
      prefix.append(toSourceModifier(modifier)).append(" ");
    }
    if (isStatic) {
      prefix.append("static ");
    }
    if (StringUtils.isNotBlank(genericDeclaration)) {
      prefix.append(genericDeclaration).append(" ");
    }
    return prefix.toString();
  }

  private String buildParameterList(List<MethodParameterDto> parameters) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < parameters.size(); i++) {
      if (i > 0) {
        result.append(", ");
      }
      result.append(buildParameter(parameters.get(i), i == parameters.size() - 1));
    }
    return result.toString();
  }

  private String buildParameter(MethodParameterDto paramDto, boolean lastParameter) {
    StringBuilder parameter = new StringBuilder();
    if (CollectionUtils.isNotEmpty(paramDto.getAnnotations())) {
      parameter
          .append(
              mapAnnotations(paramDto.getAnnotations()).stream()
                  .reduce((a, b) -> a + " " + b)
                  .orElse(""))
          .append(" ");
    }

    if (lastParameter && paramDto.getParameterType() instanceof TypeNameArray arrayType) {
      parameter
          .append(mapType(arrayType.getTypeOfArray()))
          .append("...")
          .append(" ")
          .append(paramDto.getParameterName());
    } else {
      parameter
          .append(mapType(paramDto.getParameterType()))
          .append(" ")
          .append(paramDto.getParameterName());
    }
    return parameter.toString();
  }

  private void appendNestedTypes(StringBuilder source, BuilderDefinitionDto builderDef) {
    if (CollectionUtils.isEmpty(builderDef.getNestedTypes())) {
      return;
    }
    logger.debugStartOperation("Generating %d nested type(s)", builderDef.getNestedTypes().size());
    for (NestedTypeDto nestedType : builderDef.getNestedTypes()) {
      appendNestedType(source, nestedType);
      source.append("\n");
      logger.debug("Generated nested type: %s", nestedType.getTypeName());
    }
    logger.debugEndOperation("Nested types added");
  }

  private void appendNestedType(StringBuilder source, NestedTypeDto nestedType) {
    appendJavadoc(source, nestedType.getJavadoc(), INDENT);
    source.append(INDENT);
    if (nestedType.isPublic()) {
      source.append(PUBLIC.toString().toLowerCase()).append(" ");
    }
    source
        .append(
            nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE
                ? "interface "
                : "class ")
        .append(nestedType.getTypeName())
        .append(" {\n\n");

    boolean isInterface = nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE;
    for (MethodDto methodDto : nestedType.getMethods()) {
      appendNestedMethod(source, methodDto, isInterface);
      source.append("\n");
    }

    source.append(INDENT).append("}\n");
  }

  private void appendNestedMethod(StringBuilder source, MethodDto methodDto, boolean isInterface) {
    appendJavadoc(source, methodDto.getJavadoc(), DOUBLE_INDENT);
    appendAnnotations(source, methodDto.getAnnotations(), DOUBLE_INDENT);

    source.append(DOUBLE_INDENT);
    StringBuilder signature = new StringBuilder();
    String genericDeclaration = mapGenericDeclaration(methodDto.getGenericParameters());
    if (StringUtils.isNotBlank(genericDeclaration)) {
      signature.append(genericDeclaration).append(" ");
    }
    if (isInterface
        && methodDto.getMethodCodeDto() != null
        && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())) {
      signature.append("default ");
    } else {
      signature.append(PUBLIC.toString().toLowerCase()).append(" ");
    }
    signature
        .append(mapType(methodDto.getReturnType()))
        .append(" ")
        .append(methodDto.getMethodName())
        .append("(")
        .append(buildParameterList(methodDto.getParameters()))
        .append(")");
    source.append(signature);

    if (methodDto.getMethodCodeDto() == null
        || StringUtils.isBlank(methodDto.getMethodCodeDto().getCodeFormat())) {
      source.append(";\n");
      return;
    }

    source.append(" {\n");
    appendIndentedBody(source, resolveCodeTemplate(methodDto.getMethodCodeDto()), TRIPLE_INDENT);
    source.append(DOUBLE_INDENT).append("}\n");
  }

  private void appendClassJavadoc(StringBuilder source, String javadoc, String indent) {
    appendJavadoc(source, javadoc, indent);
  }

  private void appendImports(StringBuilder source, Set<String> imports) {
    if (imports.isEmpty()) {
      return;
    }
    imports.stream()
        .sorted()
        .forEach(value -> source.append("import ").append(value).append(";\n"));
    source.append("\n");
  }

  private void appendTrackedValueStaticImports(StringBuilder source) {
    source.append("import static ").append(TrackedValue.class.getName()).append(".changedValue;\n");
    source.append("import static ").append(TrackedValue.class.getName()).append(".initialValue;\n");
    source.append("import static ").append(TrackedValue.class.getName()).append(".unsetValue;\n\n");
  }

  private Set<String> collectImports(BuilderDefinitionDto builderDef) {
    Set<String> imports = new LinkedHashSet<>();
    String currentPackage = builderDef.getBuilderTypeName().getPackageName();

    addImportIfNeeded(imports, currentPackage, TrackedValue.class.getName());
    addTypeImports(imports, currentPackage, builderDef.getBuildingTargetTypeName());
    addTypeImports(imports, currentPackage, builderDef.getBuilderTypeName());
    builderDef
        .getGenerics()
        .forEach(
            generic ->
                generic
                    .getUpperBounds()
                    .forEach(type -> addTypeImports(imports, currentPackage, type)));
    builderDef
        .getInterfaces()
        .forEach(interfaceName -> addInterfaceImports(imports, currentPackage, interfaceName));
    builderDef
        .getClassAnnotations()
        .forEach(annotation -> addAnnotationImports(imports, currentPackage, annotation));
    builderDef
        .getAllFieldsForBuilder()
        .forEach(field -> addFieldImports(imports, currentPackage, field));
    builderDef
        .getCoreMethods()
        .forEach(method -> addMethodImports(imports, currentPackage, method));
    builderDef
        .getNestedTypes()
        .forEach(
            nestedType ->
                nestedType
                    .getMethods()
                    .forEach(method -> addMethodImports(imports, currentPackage, method)));

    return imports;
  }

  private void addFieldImports(Set<String> imports, String currentPackage, FieldDto field) {
    addTypeImports(imports, currentPackage, field.getFieldType());
    field
        .getParameterAnnotations()
        .forEach(annotation -> addAnnotationImports(imports, currentPackage, annotation));
    field.getMethods().forEach(method -> addMethodImports(imports, currentPackage, method));
  }

  private void addMethodImports(Set<String> imports, String currentPackage, MethodDto method) {
    if (method.getReturnType() != null) {
      addTypeImports(imports, currentPackage, method.getReturnType());
    }
    method
        .getGenericParameters()
        .forEach(
            generic ->
                generic
                    .getUpperBounds()
                    .forEach(type -> addTypeImports(imports, currentPackage, type)));
    method
        .getAnnotations()
        .forEach(annotation -> addAnnotationImports(imports, currentPackage, annotation));
    method
        .getParameters()
        .forEach(parameter -> addParameterImports(imports, currentPackage, parameter));
    addBodyImports(imports, currentPackage, method);
  }

  private void addParameterImports(
      Set<String> imports, String currentPackage, MethodParameterDto parameter) {
    addTypeImports(imports, currentPackage, parameter.getParameterType());
    parameter
        .getAnnotations()
        .forEach(annotation -> addAnnotationImports(imports, currentPackage, annotation));
  }

  private void addInterfaceImports(
      Set<String> imports,
      String currentPackage,
      org.javahelpers.simple.builders.processor.model.annotation.InterfaceName interfaceName) {
    if (StringUtils.isNotBlank(interfaceName.getPackageName())) {
      addImportIfNeeded(
          imports,
          currentPackage,
          interfaceName.getPackageName() + "." + interfaceName.getSimpleName());
    }
    interfaceName
        .getAnnotations()
        .forEach(annotation -> addAnnotationImports(imports, currentPackage, annotation));
    interfaceName
        .getTypeParameters()
        .forEach(type -> addTypeImports(imports, currentPackage, type));
  }

  private void addAnnotationImports(
      Set<String> imports, String currentPackage, AnnotationDto annotation) {
    if (annotation.getAnnotationType() != null) {
      addTypeImports(imports, currentPackage, annotation.getAnnotationType());
    }
  }

  private void addTypeImports(Set<String> imports, String currentPackage, TypeName type) {
    if (type == null || type instanceof TypeNamePrimitive || type instanceof TypeNameVariable) {
      return;
    }

    type.getAnnotations()
        .forEach(annotation -> addAnnotationImports(imports, currentPackage, annotation));

    if (type instanceof TypeNameArray arrayType) {
      addTypeImports(imports, currentPackage, arrayType.getTypeOfArray());
      return;
    }

    if (type instanceof TypeNameGeneric genericType) {
      addImportIfNeeded(
          imports, currentPackage, genericType.getFullQualifiedName().replaceAll("<.*$", ""));
      genericType
          .getInnerTypeArguments()
          .forEach(inner -> addTypeImports(imports, currentPackage, inner));
      return;
    }

    addImportIfNeeded(imports, currentPackage, type.getFullQualifiedName());
  }

  private void addImportIfNeeded(Set<String> imports, String currentPackage, String fqn) {
    if (StringUtils.isBlank(fqn)
        || !fqn.contains(".")
        || fqn.startsWith("java.lang.")
        || currentPackage.equals(packageNameOf(fqn))) {
      return;
    }
    imports.add(fqn);
  }

  private String packageNameOf(String fqn) {
    int idx = fqn.lastIndexOf('.');
    return idx < 0 ? "" : fqn.substring(0, idx);
  }

  private void addBodyImports(Set<String> imports, String currentPackage, MethodDto method) {
    if (method.getMethodCodeDto() == null
        || StringUtils.isBlank(method.getMethodCodeDto().getCodeFormat())) {
      return;
    }
    for (MethodCodePlaceholder<?> argument : method.getMethodCodeDto().getCodeArguments()) {
      if (argument instanceof MethodCodeTypePlaceholder typePlaceholder) {
        addTypeImports(imports, currentPackage, typePlaceholder.getValue());
      }
    }
    String code = method.getMethodCodeDto().getCodeFormat();
    if (code.contains("List.of(")) {
      imports.add(List.class.getName());
    }
    if (code.contains("Optional.of(")
        || code.contains("Optional.empty(")
        || code.contains("Optional.ofNullable(")) {
      imports.add(java.util.Optional.class.getName());
    }
  }

  private void appendJavadoc(StringBuilder source, String javadoc, String indent) {
    if (StringUtils.isBlank(javadoc)) {
      return;
    }
    source.append(indent).append("/**\n");
    String normalized = javadoc.replace("\r\n", "\n").replace("\r", "\n").replaceFirst("\\n+$", "");
    for (String line : normalized.split("\n", -1)) {
      source.append(indent).append(" *");
      if (!line.isEmpty()) {
        source.append(" ").append(line.replace("*/", "* /"));
      }
      source.append("\n");
    }
    source.append(indent).append(" */\n");
  }

  private void appendAnnotations(
      StringBuilder source, java.util.Collection<AnnotationDto> annotations, String indent) {
    if (CollectionUtils.isEmpty(annotations)) {
      return;
    }
    for (AnnotationDto annotation : annotations) {
      source.append(indent).append(mapAnnotation(annotation)).append("\n");
    }
  }

  private void appendIndentedBody(StringBuilder source, String body, String indent) {
    if (StringUtils.isBlank(body)) {
      return;
    }
    String normalized = body.replace("\r\n", "\n").replace("\r", "\n").replaceFirst("\\n+$", "");
    for (String line : normalized.split("\n", -1)) {
      if (line.isEmpty()) {
        source.append(indent).append("\n");
      } else {
        source.append(indent).append(line).append("\n");
      }
    }
  }

  private String toSourceModifier(Modifier modifier) {
    return modifier.toString().toLowerCase();
  }

  private String formatSource(String rawSource) {
    return rawSource;
  }

  private void writeBuilderClassToFile(String sourceCode, BuilderDefinitionDto builderDef)
      throws BuilderException {
    logger.debug(
        "Writing builder class to file: %s.%s",
        builderDef.getBuilderTypeName().getPackageName(),
        builderDef.getBuilderTypeName().getClassName());

    String qualifiedName = builderDef.getBuilderTypeName().getFullQualifiedName();
    if (builderClassAlreadyExists(qualifiedName)) {
      throw new BuilderException(
          null,
          """
          Builder class '%s' already exists. This may be a manually written builder or a previously generated builder.
          To resolve this:
          1. If you have a manual builder, consider renaming it or removing @SimpleBuilder from the DTO
          2. If this is from a previous compilation, clean and rebuild the project
          3. Check that you're not trying to generate multiple builders for the same DTO
          """
              .formatted(qualifiedName));
    }

    try {
      JavaFileObject file = processingEnv.getFiler().createSourceFile(qualifiedName);
      try (Writer writer = file.openWriter()) {
        writer.write(sourceCode);
      }
    } catch (IOException ex) {
      String message = ex.getMessage();
      String errorMessage =
          """
          Unable to create builder class '%s': %s.
          Check the build environment and ensure all necessary directories are accessible.
          """
              .formatted(
                  qualifiedName, StringUtils.isNotBlank(message) ? message : "Unknown error");
      throw new BuilderException(null, errorMessage);
    }
  }

  /**
   * Checks if a builder class already exists by attempting to find the type element.
   *
   * @param qualifiedName the fully qualified name of the class to check
   * @return true if the class already exists, false otherwise
   */
  private boolean builderClassAlreadyExists(String qualifiedName) {
    try {
      TypeElement existingType = processingEnv.getElementUtils().getTypeElement(qualifiedName);
      return existingType != null;
    } catch (Exception e) {
      logger.debug(
          "Error checking if builder class '%s' already exists: %s",
          qualifiedName, StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "No message");
      return false;
    }
  }

  /**
   * Generates a Jackson SimpleModule based on the provided definition.
   *
   * @param moduleDef the definition of the Jackson module to generate
   */
  public void generateJacksonModule(JacksonModuleDefinitionDto moduleDef) {
    String packageName = moduleDef.getTargetPackage();
    String moduleClassName = "SimpleBuildersJacksonModule";

    logger.info("Generating Jackson Module '%s' in package '%s'", moduleClassName, packageName);

    StringBuilder source = new StringBuilder();
    if (StringUtils.isNotBlank(packageName)) {
      source.append("package ").append(packageName).append(";\n\n");
    }
    source.append("import com.fasterxml.jackson.databind.annotation.JsonDeserialize;\n");
    source.append("import com.fasterxml.jackson.databind.module.SimpleModule;\n");
    for (JacksonModuleEntryDto entry : moduleDef.getEntries()) {
      appendModuleImport(source, packageName, entry.dtoType().getFullQualifiedName());
      appendModuleImport(source, packageName, entry.builderType().getFullQualifiedName());
    }
    source.append("\n");

    source
        .append("public class ")
        .append(moduleClassName)
        .append(" extends ")
        .append("SimpleModule")
        .append(" {\n\n");

    for (JacksonModuleEntryDto entry : moduleDef.getEntries()) {
      String mixinName = entry.dtoType().getClassName() + "Mixin";
      source
          .append(INDENT)
          .append("@JsonDeserialize(builder = ")
          .append(entry.builderType().getClassName())
          .append(".class)\n")
          .append(INDENT)
          .append("private interface ")
          .append(mixinName)
          .append(" {}\n\n");
    }

    source.append(INDENT).append("public ").append(moduleClassName).append("() {\n");
    for (JacksonModuleEntryDto entry : moduleDef.getEntries()) {
      String mixinName = entry.dtoType().getClassName() + "Mixin";
      source
          .append(DOUBLE_INDENT)
          .append("setMixInAnnotation(")
          .append(entry.dtoType().getClassName())
          .append(".class, ")
          .append(mixinName)
          .append(".class);\n");
    }
    source.append(INDENT).append("}\n");
    source.append("}\n");

    try {
      writeSimpleClassToFile(packageName, moduleClassName, formatSource(source.toString()));
    } catch (BuilderException e) {
      logger.warning(
          "simple-builders: Error generating Jackson module for package %s: %s\n%s",
          packageName, e.getMessage(), java.util.Arrays.toString(e.getStackTrace()));
    }
  }

  private void writeSimpleClassToFile(String packageName, String className, String sourceCode)
      throws BuilderException {
    try {
      String qualifiedName =
          StringUtils.isBlank(packageName) ? className : packageName + "." + className;
      JavaFileObject file = processingEnv.getFiler().createSourceFile(qualifiedName);
      try (Writer writer = file.openWriter()) {
        writer.write(sourceCode);
      }
    } catch (IOException ex) {
      String message = ex.getMessage();
      String errorMessage =
          """
          Unable to create class: %s.
          Check the build environment and ensure all necessary directories are accessible.
          """
              .formatted(StringUtils.isNotBlank(message) ? message : "Unknown error");
      throw new BuilderException(null, errorMessage);
    }
  }

  private void appendModuleImport(StringBuilder source, String currentPackage, String fqn) {
    if (StringUtils.isBlank(fqn)
        || !fqn.contains(".")
        || fqn.startsWith("java.lang.")
        || currentPackage.equals(packageNameOf(fqn))) {
      return;
    }
    source.append("import ").append(fqn).append(";\n");
  }
}
