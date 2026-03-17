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
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.javahelpers.simple.builders.processor.model.annotation.InterfaceName;
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
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.source.TypeVariableSource;
import org.jboss.forge.roaster.model.util.FormatterProfileReader;

/** Roaster-based code generator for builder source files. */
public class RoasterCodeGenerator {
  private static final String FORMATTER_PROFILE_RESOURCE = "eclipse-java-format.xml";
  private static final String INDENT = "  ";
  private static final String DOUBLE_INDENT = INDENT + INDENT;
  private static final String TRIPLE_INDENT = DOUBLE_INDENT + INDENT;

  /** Processing environment for accessing filer and element utilities. */
  private final ProcessingEnvironment processingEnv;

  /** Logger for debug output during code generation. */
  private final ProcessingLogger logger;

  private final Properties formatterProperties;

  /**
   * Constructor for RoasterCodeGenerator.
   *
   * @param processingEnv Processing environment for accessing filer and element utilities
   * @param logger Logger for debug output
   */
  public RoasterCodeGenerator(ProcessingEnvironment processingEnv, ProcessingLogger logger) {
    this.processingEnv = processingEnv;
    this.logger = logger;
    this.formatterProperties = loadFormatterProperties();
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
    logger.debug("Class builder created");
    JavaClassSource source = createClassSource(builderDef);
    logger.debug("Class metadata added");
    appendFields(source, builderDef);
    appendConstructors(source, builderDef);
    appendMethods(source, builderDef);
    appendNestedTypes(source, builderDef);
    logger.debug("Class-level annotations added");
    logger.debug("Builder source created");
    return formatSource(renderClassSource(source, builderDef));
  }

  private JavaClassSource createClassSource(BuilderDefinitionDto builderDef) {
    JavaClassSource source = Roaster.create(JavaClassSource.class);
    String packageName = builderDef.getBuilderTypeName().getPackageName();
    if (StringUtils.isNotBlank(packageName)) {
      source.setPackage(packageName);
    } else {
      source.setDefaultPackage();
    }
    source.setName(builderDef.getBuilderTypeName().getClassName());
    applyVisibility(
        source, JavaLangMapper.mapAccessModifier(builderDef.getConfiguration().getBuilderAccess()));
    addGenericDeclarations(source, builderDef.getGenerics());
    addTrackedValueStaticImports(source);
    collectImports(builderDef).forEach(source::addImport);
    for (InterfaceName interfaceName : builderDef.getInterfaces()) {
      source.addInterface(RoasterMapper.mapInterfaceToTypeName(interfaceName));
    }
    applyJavadoc(source, builderDef.getClassJavadoc());
    applyAnnotations(source, builderDef.getClassAnnotations());
    return source;
  }

  private String renderClassSource(JavaClassSource source, BuilderDefinitionDto builderDef) {
    String rendered = source.toUnformattedString();
    rendered =
        simplifyImportedTypeReferences(
            rendered, collectImports(builderDef), builderDef.getBuilderTypeName().getPackageName());
    return rendered;
  }

  private void appendFields(JavaClassSource source, BuilderDefinitionDto builderDef) {
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

  private void appendField(JavaClassSource source, FieldDto fieldDto) {
    String boxedFieldType = mapBoxedType(fieldDto.getFieldType());
    FieldSource<JavaClassSource> field = source.addField();
    field.setName(fieldDto.getFieldNameInBuilder());
    field.setType(TrackedValue.class.getSimpleName() + "<" + boxedFieldType + ">");
    field.setPrivate();
    field.setLiteralInitializer("unsetValue()");
    applyJavadoc(
        field,
        "Tracked value for <code>%s</code>: %s."
            .formatted(
                fieldDto.getFieldNameInBuilder(),
                StringUtils.defaultString(fieldDto.getJavaDoc())));
  }

  private void appendConstructors(JavaClassSource source, BuilderDefinitionDto builderDef) {
    generateConstructors(source, builderDef);
    logger.debug("Constructors added");
  }

  private void generateConstructors(JavaClassSource source, BuilderDefinitionDto builderDef) {
    Modifier constructorAccessModifier =
        JavaLangMapper.mapAccessModifier(
            builderDef.getConfiguration().getBuilderConstructorAccess());
    TypeName dtoBaseClass = builderDef.getBuildingTargetTypeName();

    appendEmptyConstructor(
        source,
        dtoBaseClass,
        builderDef.getBuilderTypeName().getClassName(),
        constructorAccessModifier);
    appendConstructorWithInstance(
        source,
        dtoBaseClass,
        builderDef.getBuilderTypeName().getClassName(),
        builderDef.getAllFieldsForBuilder(),
        constructorAccessModifier);
  }

  private void appendEmptyConstructor(
      JavaClassSource source, TypeName dtoClass, String builderClassName, Modifier accessModifier) {
    MethodSource<JavaClassSource> constructor = source.addMethod();
    constructor.setConstructor(true);
    applyVisibility(constructor, accessModifier);
    constructor.setBody("");
    applyJavadoc(
        constructor,
        "Empty constructor of builder for {@code %s}.".formatted(dtoClass.getFullQualifiedName()));
  }

  private void appendConstructorWithInstance(
      JavaClassSource source,
      TypeName dtoBaseClass,
      String builderClassName,
      List<FieldDto> fields,
      Modifier accessModifier) {
    MethodSource<JavaClassSource> constructor = source.addMethod();
    constructor.setConstructor(true);
    applyVisibility(constructor, accessModifier);
    constructor.addParameter(mapType(dtoBaseClass), "instance");
    constructor.setBody(buildConstructorBody(fields));
    applyJavadoc(
        constructor,
        """
        Initialisation of builder for {@code %s} by a instance.

        @param instance object instance for initialisiation
        """
            .formatted(dtoBaseClass.getFullQualifiedName()));
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

  private void appendMethods(JavaClassSource source, BuilderDefinitionDto builderDef) {
    Map<MethodDto, FieldDto> allMethods = collectAllMethods(builderDef);
    logger.debugStartOperation("Adding Methods for %d candidates", allMethods.size());

    List<MethodDto> resolvedMethods = resolveMethodConflicts(allMethods);
    logger.debug("Resolved %d methods after conflict resolution", resolvedMethods.size());

    int generatedCnt = 0;
    for (MethodDto methodDto : resolvedMethods) {
      appendMethod(source, methodDto, false, false);
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
      JavaClassSource source,
      MethodDto methodDto,
      boolean nestedTypeMethod,
      boolean interfaceMethod) {
    MethodSource method = source.addMethod();
    configureMethod(method, methodDto, nestedTypeMethod, interfaceMethod);
    String body =
        methodDto.getMethodCodeDto() != null
                && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())
            ? resolveCodeTemplate(methodDto.getMethodCodeDto())
            : "";
    method.setBody(body);
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

  private void appendNestedTypes(JavaClassSource source, BuilderDefinitionDto builderDef) {
    if (CollectionUtils.isEmpty(builderDef.getNestedTypes())) {
      return;
    }
    logger.debugStartOperation("Generating %d nested type(s)", builderDef.getNestedTypes().size());
    for (NestedTypeDto nestedType : builderDef.getNestedTypes()) {
      appendNestedType(source, nestedType);
      logger.debug("Generated nested type: %s", nestedType.getTypeName());
    }
    logger.debugEndOperation("Nested types added");
  }

  private void appendNestedType(JavaClassSource source, NestedTypeDto nestedType) {
    JavaSource<?> nestedSource =
        nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE
            ? source.addNestedType(JavaInterfaceSource.class)
            : source.addNestedType(JavaClassSource.class);
    nestedSource.setName(nestedType.getTypeName());
    if (nestedType.isPublic()) {
      nestedSource.setPublic();
    } else {
      nestedSource.setPackagePrivate();
    }
    applyJavadoc(nestedSource, nestedType.getJavadoc());
    boolean isInterface = nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE;
    for (MethodDto methodDto : nestedType.getMethods()) {
      appendNestedMethod(nestedSource, methodDto, isInterface);
    }
  }

  private void appendNestedMethod(JavaSource<?> source, MethodDto methodDto, boolean isInterface) {
    org.jboss.forge.roaster.model.source.MethodHolderSource methodHolder =
        (org.jboss.forge.roaster.model.source.MethodHolderSource) source;
    MethodSource method = methodHolder.addMethod();
    configureMethod(method, methodDto, true, isInterface);
    if (methodDto.getMethodCodeDto() != null
        && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())) {
      method.setBody(resolveCodeTemplate(methodDto.getMethodCodeDto()));
    } else {
      method.setAbstract(true);
      method.setBody("");
    }
  }

  private void applyVisibility(
      org.jboss.forge.roaster.model.source.VisibilityScopedSource<?> source, Modifier modifier) {
    if (modifier == null) {
      source.setPackagePrivate();
      return;
    }
    switch (modifier) {
      case PUBLIC -> source.setPublic();
      case PROTECTED -> source.setProtected();
      case PRIVATE -> source.setPrivate();
      default -> source.setPackagePrivate();
    }
  }

  private void addGenericDeclarations(
      JavaClassSource source,
      List<org.javahelpers.simple.builders.processor.model.type.GenericParameterDto> generics) {
    if (CollectionUtils.isEmpty(generics)) {
      return;
    }
    for (org.javahelpers.simple.builders.processor.model.type.GenericParameterDto generic :
        generics) {
      TypeVariableSource<JavaClassSource> typeVariable = source.addTypeVariable(generic.getName());
      if (CollectionUtils.isNotEmpty(generic.getUpperBounds())) {
        typeVariable.setBounds(
            generic.getUpperBounds().stream().map(RoasterMapper::mapType).toArray(String[]::new));
      }
    }
  }

  private void addGenericDeclarations(
      MethodSource<?> source,
      List<org.javahelpers.simple.builders.processor.model.type.GenericParameterDto> generics) {
    if (CollectionUtils.isEmpty(generics)) {
      return;
    }
    for (org.javahelpers.simple.builders.processor.model.type.GenericParameterDto generic :
        generics) {
      TypeVariableSource<?> typeVariable = source.addTypeVariable(generic.getName());
      if (CollectionUtils.isNotEmpty(generic.getUpperBounds())) {
        typeVariable.setBounds(
            generic.getUpperBounds().stream().map(RoasterMapper::mapType).toArray(String[]::new));
      }
    }
  }

  private void applyJavadoc(
      org.jboss.forge.roaster.model.source.JavaDocCapableSource<?> source, String javadoc) {
    if (StringUtils.isBlank(javadoc)) {
      return;
    }
    String normalized = javadoc.replace("\r\n", "\n").replace("\r", "\n").replaceFirst("\\n+$", "");
    String[] lines = normalized.split("\n", -1);
    StringBuilder text = new StringBuilder();
    source.getJavaDoc().removeAllTags();
    boolean inTags = false;
    for (String line : lines) {
      if (!inTags && line.startsWith("@")) {
        inTags = true;
      }
      if (inTags && line.startsWith("@")) {
        int firstSpace = line.indexOf(' ');
        if (firstSpace > 1) {
          source
              .getJavaDoc()
              .addTagValue(line.substring(0, firstSpace), line.substring(firstSpace + 1));
        } else if (line.length() > 1) {
          source.getJavaDoc().addTagValue(line, "");
        }
      } else {
        if (text.length() > 0) {
          text.append('\n');
        }
        text.append(line);
      }
    }
    source.getJavaDoc().setText(text.toString());
  }

  private void applyAnnotations(
      org.jboss.forge.roaster.model.source.AnnotationTargetSource<?, ?> source,
      java.util.Collection<AnnotationDto> annotations) {
    if (CollectionUtils.isEmpty(annotations)) {
      return;
    }
    for (AnnotationDto annotationDto : annotations) {
      AnnotationSource<?> annotation = source.addAnnotation();
      annotation.setName(annotationDto.getAnnotationType().getClassName());
      for (Map.Entry<String, String> member : annotationDto.getMembers().entrySet()) {
        if ("value".equals(member.getKey())) {
          annotation.setLiteralValue(member.getValue());
        } else {
          annotation.setLiteralValue(member.getKey(), member.getValue());
        }
      }
    }
  }

  private void configureMethod(
      MethodSource<?> method,
      MethodDto methodDto,
      boolean nestedTypeMethod,
      boolean interfaceMethod) {
    method.setName(methodDto.getMethodName());
    if (methodDto.getReturnType() == null) {
      method.setReturnTypeVoid();
    } else {
      method.setReturnType(mapType(methodDto.getReturnType()));
    }

    if (nestedTypeMethod) {
      boolean hasBody =
          interfaceMethod
              && methodDto.getMethodCodeDto() != null
              && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat());
      if (hasBody) {
        method.setDefault(true);
      } else {
        method.setPublic();
      }
    } else {
      applyVisibility(method, methodDto.getModifier().orElse(null));
      method.setStatic(methodDto.isStatic());
    }

    addGenericDeclarations(method, methodDto.getGenericParameters());
    applyJavadoc(method, methodDto.getJavadoc());
    applyAnnotations(method, methodDto.getAnnotations());

    for (int i = 0; i < methodDto.getParameters().size(); i++) {
      MethodParameterDto paramDto = methodDto.getParameters().get(i);
      boolean lastParameter = i == methodDto.getParameters().size() - 1;
      String parameterType =
          lastParameter && paramDto.getParameterType() instanceof TypeNameArray arrayType
              ? mapType(arrayType.getTypeOfArray())
              : mapType(paramDto.getParameterType());
      ParameterSource<?> parameter =
          method.addParameter(parameterType, paramDto.getParameterName());
      if (lastParameter && paramDto.getParameterType() instanceof TypeNameArray) {
        parameter.setVarArgs(true);
      }
      applyAnnotations(parameter, paramDto.getAnnotations());
    }
  }

  private void addTrackedValueStaticImports(JavaClassSource source) {
    source.addImport(TrackedValue.class.getName() + ".changedValue").setStatic(true);
    source.addImport(TrackedValue.class.getName() + ".initialValue").setStatic(true);
    source.addImport(TrackedValue.class.getName() + ".unsetValue").setStatic(true);
  }

  private String alignClosingBraceSpacing(String sourceCode) {
    String normalized = sourceCode.replace("\r\n", "\n").replace("\r", "\n");
    normalized = normalized.replaceAll("\\n{3,}", "\n\n");
    normalized = normalized.replaceAll("\\n\\s*\\n\\}", "\n}");
    normalized = normalized.replaceAll("\\}\\s+\\}", "}\n}");
    normalized = normalized.replaceAll("(?m)^}(\\n([ \\t]+)})", "\t}$1");
    normalized = normalized.replaceAll("(import [^\\n]+;)(/\\*\\*)", "$1\n\n$2");
    normalized = normalized.replaceAll("@java\\.lang\\.Override", "@Override");
    normalized =
        normalized.replaceAll("@javax\\.annotation\\.processing\\.Generated", "@Generated");
    normalized =
        normalized.replaceAll(
            "@org\\.javahelpers\\.simple\\.builders\\.core\\.annotations\\.BuilderImplementation",
            "@BuilderImplementation");
    return normalized;
  }

  private String simplifyImportedTypeReferences(
      String sourceCode, Set<String> imports, String currentPackage) {
    String normalized = sourceCode;
    for (String importedType : imports) {
      if (StringUtils.isBlank(importedType) || !importedType.contains(".")) {
        continue;
      }
      String simpleName = importedType.substring(importedType.lastIndexOf('.') + 1);
      normalized = normalized.replace("@" + importedType, "@" + simpleName);
    }
    if (StringUtils.isNotBlank(currentPackage)) {
      String escapedPackage = java.util.regex.Pattern.quote(currentPackage);
      normalized =
          normalized.replaceAll(
              "(\\(\\s*)@" + escapedPackage + "\\.([A-Za-z_$][A-Za-z0-9_$]*)", "$1@$2");
      normalized =
          normalized.replaceAll(
              "(,\\s*)@" + escapedPackage + "\\.([A-Za-z_$][A-Za-z0-9_$]*)", "$1@$2");
    }
    return normalized;
  }

  private String normalizeJavadocs(String sourceCode) {
    String normalized = sourceCode.replace("\r\n", "\n").replace("\r", "\n");
    java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile(
            "(?m)^([ \\t]*)/\\*\\*(.*?)^[ \\t]*\\*/", java.util.regex.Pattern.DOTALL);
    java.util.regex.Matcher matcher = pattern.matcher(normalized);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String indent = matcher.group(1);
      String content = matcher.group(2);
      String replacement = rebuildJavadocBlock(indent, content);
      matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private String rebuildJavadocBlock(String indent, String content) {
    StringBuilder rebuilt = new StringBuilder();
    rebuilt.append(indent).append("/**\n");
    String[] lines = content.split("\n", -1);
    int start = 0;
    int end = lines.length;
    while (start < end && lines[start].isBlank()) {
      start++;
    }
    while (end > start && lines[end - 1].isBlank()) {
      end--;
    }
    for (int i = start; i < end; i++) {
      String rawLine = lines[i];
      String line = rawLine.stripLeading();
      if (line.startsWith("*")) {
        line = line.substring(1).stripLeading();
      }
      if (line.startsWith("<p>")) {
        rebuilt.append(indent).append(" * <p>\n");
        String remainder = line.substring(3).stripLeading();
        if (!remainder.isEmpty()) {
          rebuilt.append(indent).append(" * ").append(remainder).append("\n");
        }
        continue;
      }
      if (line.startsWith("param ")) {
        line = "@param " + line.substring("param ".length());
      } else if (line.startsWith("return ")) {
        line = "@return " + line.substring("return ".length());
      } else if (line.startsWith("throws ")) {
        line = "@throws " + line.substring("throws ".length());
      } else if (line.startsWith("see ")) {
        line = "@see " + line.substring("see ".length());
      }
      if (line.isBlank()) {
        rebuilt.append(indent).append(" *\n");
      } else {
        rebuilt.append(indent).append(" * ").append(line).append("\n");
      }
    }
    rebuilt.append(indent).append(" */");
    return rebuilt.toString();
  }

  private String normalizeBuilderClassJavadoc(String sourceCode) {
    String normalized = sourceCode.replace("\r\n", "\n").replace("\r", "\n");
    normalized = normalized.replace("\n<p>\n", "\n * <p>\n");
    normalized =
        normalized.replace(
            "\nThis builder provides a fluent API for creating instances of",
            "\n * This builder provides a fluent API for creating instances of");
    normalized =
        normalized.replace(
            "\nmethod chaining and validation. Use the static {@code create()} method",
            "\n * method chaining and validation. Use the static {@code create()} method");
    normalized =
        normalized.replace(
            "\nto obtain a new builder instance, configure the desired properties using",
            "\n * to obtain a new builder instance, configure the desired properties using");
    normalized =
        normalized.replace(
            "\nthe setter methods, and then call {@code build()} to create the final DTO.",
            "\n * the setter methods, and then call {@code build()} to create the final DTO.");
    return normalized;
  }

  private String normalizeToStringChains(String sourceCode) {
    java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile(
            "(?m)^([ \\t]*)return new ToStringBuilder\\(this, BuilderToStringStyle\\.INSTANCE\\)([\\s\\S]*?)\\.toString\\(\\);");
    java.util.regex.Matcher matcher = pattern.matcher(sourceCode);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String indent = matcher.group(1);
      String middle = matcher.group(2);
      java.util.regex.Matcher appendMatcher =
          java.util.regex.Pattern.compile("\\.append\\([\\s\\S]*?\\)").matcher(middle);
      StringBuilder replacement = new StringBuilder();
      replacement
          .append(indent)
          .append("return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)\n");
      while (appendMatcher.find()) {
        replacement.append(indent).append("    ").append(appendMatcher.group()).append("\n");
      }
      replacement.append(indent).append("    .toString();");
      matcher.appendReplacement(
          result, java.util.regex.Matcher.quoteReplacement(replacement.toString()));
    }
    matcher.appendTail(result);
    return result.toString();
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
    if (formatterProperties == null) {
      return rawSource;
    }
    try {
      return Roaster.format(formatterProperties, rawSource);
    } catch (Exception ex) {
      logger.warning(
          "simple-builders: Failed to format generated source with bundled Eclipse formatter profile: %s",
          StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
      return rawSource;
    }
  }

  private Properties loadFormatterProperties() {
    try (InputStream inputStream =
        RoasterCodeGenerator.class
            .getClassLoader()
            .getResourceAsStream(FORMATTER_PROFILE_RESOURCE)) {
      if (inputStream == null) {
        logger.warning(
            "simple-builders: Bundled Eclipse formatter profile '%s' was not found on the processor classpath.",
            FORMATTER_PROFILE_RESOURCE);
        return null;
      }
      FormatterProfileReader profileReader = FormatterProfileReader.fromEclipseXml(inputStream);
      return profileReader.getDefaultProperties();
    } catch (IOException ex) {
      logger.warning(
          "simple-builders: Failed to load bundled Eclipse formatter profile '%s': %s",
          FORMATTER_PROFILE_RESOURCE,
          StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
      return null;
    }
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
