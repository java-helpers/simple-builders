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

import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapBoxedType;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapType;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.packageNameOf;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.source.TypeVariableSource;
import org.jboss.forge.roaster.model.util.FormatterProfileReader;

/** Roaster-based code generator for builder source files. */
public class RoasterCodeGenerator {
  private static final String FORMATTER_PROFILE_RESOURCE = "eclipse-java-format.xml";

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
    JavaClassSource source = createClassSource(builderDef);
    addClassMetadata(source, builderDef);
    appendFields(source, builderDef);
    appendConstructors(source, builderDef);
    appendMethods(source, builderDef);
    appendNestedTypes(source, builderDef);
    applyClassAnnotations(source, builderDef);
    return renderClassSource(source);
  }

  private void applyClassAnnotations(JavaClassSource source, BuilderDefinitionDto builderDef) {
    if (CollectionUtils.isEmpty(builderDef.getClassAnnotations())) {
      return;
    }
    // Adding annotations from enhancers
    applyAnnotations(source, builderDef.getClassAnnotations());
    logger.debug("Class-level annotations added");
  }

  private JavaClassSource createClassSource(BuilderDefinitionDto builderDef) {
    if (CollectionUtils.isNotEmpty(builderDef.getGenerics())) {
      logger.debug("Builder has %d generic type parameter(s)", builderDef.getGenerics().size());
    }

    JavaClassSource source = Roaster.create(JavaClassSource.class);
    String packageName = builderDef.getBuilderTypeName().getPackageName();
    if (StringUtils.isNotBlank(packageName)) {
      source.setPackage(packageName);
    } else {
      source.setDefaultPackage();
    }
    source.setName(builderDef.getBuilderTypeName().getClassName());
    addGenericDeclarations(source, builderDef.getGenerics());
    addTrackedValueStaticImports(source);
    collectImports(builderDef).stream().sorted().forEach(source::addImport);
    logger.debug("Class builder created");
    return source;
  }

  private void addClassMetadata(JavaClassSource source, BuilderDefinitionDto builderDef) {
    // Add class JavaDoc if provided by enhancer
    applyJavadoc(source, builderDef.getClassJavadoc());

    // Set builder class access level
    applyVisibility(
        source, JavaLangMapper.mapAccessModifier(builderDef.getConfiguration().getBuilderAccess()));

    // Adding interfaces from enhancers
    for (InterfaceName interfaceName : builderDef.getInterfaces()) {
      source.addInterface(RoasterMapper.mapInterfaceToTypeName(interfaceName));
    }

    logger.debug("Class metadata added");
  }

  private String renderClassSource(JavaClassSource source) {
    String rendered = source.toUnformattedString();
    return formatSource(rendered);
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
    applyJavaDocToField(field.getJavaDoc(), fieldDto);
  }

  private void applyJavaDocToField(JavaDocSource<?> javaDoc, FieldDto fieldDto) {
    javaDoc.setText(
        "Tracked value for <code>%s</code>: %s."
            .formatted(
                fieldDto.getFieldNameInBuilder(),
                StringUtils.defaultString(fieldDto.getJavaDoc())));
  }

  private void appendConstructors(JavaClassSource source, BuilderDefinitionDto builderDef) {
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
    logger.debug("Constructors added");
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

    String initialisationCode =
        """
        this.%s = initialValue(instance.%s());
        """
            .formatted(fieldInBuilder, getter);

    String validationCode = "";
    if (field.isNonNullable()) {
      validationCode =
          """

          if (this.%s.value() == null) {
            throw new IllegalArgumentException("Cannot initialize builder from instance: field '%s' is marked as non-null but source object has null value");
          }
          """
              .formatted(fieldInBuilder, fieldInBuilder);
    }

    body.append(initialisationCode).append(validationCode);
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
        String existingSource =
            createSourceDescriptionForLogging(existing, methodToField.get(existing));
        String newSource = createSourceDescriptionForLogging(method, field);

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

  /**
   * Creates a description string for method source identification.
   *
   * @param method method to describe
   * @param field associated field (may be null)
   * @return description string for logging/debugging
   */
  public static String createSourceDescriptionForLogging(MethodDto method, FieldDto field) {
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
    MethodSource<JavaClassSource> method = source.addMethod();
    configureMethod(method, methodDto, nestedTypeMethod, interfaceMethod);
    String body =
        methodDto.getMethodCodeDto() != null
                && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())
            ? resolveCodeTemplate(methodDto.getMethodCodeDto())
            : "";
    method.setBody(body);
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
    org.jboss.forge.roaster.model.source.MethodHolderSource<?> methodHolder =
        (org.jboss.forge.roaster.model.source.MethodHolderSource<?>) source;
    MethodSource<?> method = methodHolder.addMethod();
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
    // Normalize line endings without regex to avoid ReDoS vulnerability
    String normalized = javadoc.replace("\r\n", "\n").replace("\r", "\n");
    // Remove trailing newlines using StringUtils
    normalized = StringUtils.stripEnd(normalized, "\n");
    String[] lines = normalized.split("\n", -1);
    StringBuilder text = new StringBuilder();
    source.getJavaDoc().removeAllTags();
    boolean inTags = false;
    for (String line : lines) {
      if (!inTags && line.startsWith("@")) {
        inTags = true;
      }
      if (inTags && line.startsWith("@")) {
        processJavadocTag(source, line);
      } else {
        if (!text.isEmpty()) {
          text.append('\n');
        }
        text.append(line);
      }
    }
    source.getJavaDoc().setText(text.toString());
  }

  private void processJavadocTag(
      org.jboss.forge.roaster.model.source.JavaDocCapableSource<?> source, String line) {
    int firstSpace = line.indexOf(' ');
    if (firstSpace > 1) {
      source
          .getJavaDoc()
          .addTagValue(line.substring(0, firstSpace), line.substring(firstSpace + 1));
    } else if (line.length() > 1) {
      source.getJavaDoc().addTagValue(line, "");
    }
  }

  private void applyAnnotations(
      org.jboss.forge.roaster.model.source.AnnotationTargetSource<?, ?> source,
      java.util.Collection<AnnotationDto> annotations) {
    if (CollectionUtils.isEmpty(annotations)) {
      return;
    }
    for (AnnotationDto annotationDto : annotations) {
      AnnotationSource<?> annotation =
          source.addAnnotation(annotationDto.getAnnotationType().getFullQualifiedName());
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
      addParameter(method, paramDto, lastParameter);
    }
  }

  private void addParameter(
      MethodSource<?> method, MethodParameterDto paramDto, boolean lastParameter) {
    String parameterType =
        lastParameter && paramDto.getParameterType() instanceof TypeNameArray arrayType
            ? mapType(arrayType.getTypeOfArray())
            : mapType(paramDto.getParameterType());
    ParameterSource<?> parameter = method.addParameter(parameterType, paramDto.getParameterName());
    if (lastParameter && paramDto.getParameterType() instanceof TypeNameArray) {
      parameter.setVarArgs(true);
    }
    applyAnnotations(parameter, paramDto.getAnnotations());
  }

  private void addTrackedValueStaticImports(JavaClassSource source) {
    source.addImport(TrackedValue.class.getName() + ".changedValue").setStatic(true);
    source.addImport(TrackedValue.class.getName() + ".initialValue").setStatic(true);
    source.addImport(TrackedValue.class.getName() + ".unsetValue").setStatic(true);
  }

  private Set<String> collectImports(BuilderDefinitionDto builderDef) {
    Set<String> imports = new LinkedHashSet<>();
    String currentPackage = builderDef.getBuilderTypeName().getPackageName();

    addImportIfNeeded(imports, currentPackage, TrackedValue.class.getName());
    addImportIfNeeded(imports, currentPackage, Consumer.class.getName());
    addImportIfNeeded(imports, currentPackage, Supplier.class.getName());
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
        || java.util.Objects.equals(packageNameOf(fqn), currentPackage)) {
      return;
    }
    imports.add(fqn);
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

  private String formatSource(String rawSource) {
    if (formatterProperties.isEmpty()) {
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
        return new Properties();
      }
      FormatterProfileReader profileReader = FormatterProfileReader.fromEclipseXml(inputStream);
      return profileReader.getDefaultProperties();
    } catch (IOException ex) {
      logger.warning(
          "simple-builders: Failed to load bundled Eclipse formatter profile '%s': %s",
          FORMATTER_PROFILE_RESOURCE,
          StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
      return new Properties();
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

    try {
      // Create the class using Roaster
      JavaClassSource moduleClass = Roaster.create(JavaClassSource.class);
      moduleClass.setPackage(packageName);
      moduleClass.setName(moduleClassName);
      moduleClass.setSuperType("SimpleModule");

      // Collect imports in a list, sort them, then add to Roaster
      Set<String> imports = new LinkedHashSet<>();
      imports.add("com.fasterxml.jackson.databind.annotation.JsonDeserialize");
      imports.add("com.fasterxml.jackson.databind.module.SimpleModule");

      // Adding imports for DTO types
      moduleDef.getEntries().stream()
          .filter(e -> shouldAddImport(packageName, e.dtoType().getFullQualifiedName()))
          .forEach(e -> imports.add(e.dtoType().getFullQualifiedName()));

      // Adding imports for builder types
      moduleDef.getEntries().stream()
          .filter(e -> shouldAddImport(packageName, e.builderType().getFullQualifiedName()))
          .forEach(e -> imports.add(e.builderType().getFullQualifiedName()));

      // Sort imports and add to Roaster
      imports.stream().sorted().forEach(moduleClass::addImport);

      // Add mixin interfaces as nested interfaces
      for (JacksonModuleEntryDto entry : moduleDef.getEntries()) {
        String mixinName = entry.dtoType().getClassName() + "Mixin";

        // Create the nested interface
        JavaInterfaceSource mixinInterface = Roaster.create(JavaInterfaceSource.class);
        mixinInterface.setName(mixinName);
        mixinInterface.setPrivate();

        // Add the JsonDeserialize annotation
        AnnotationSource<JavaInterfaceSource> annotation =
            mixinInterface.addAnnotation("JsonDeserialize");
        annotation.setLiteralValue("builder", entry.builderType().getClassName() + ".class");

        // Add as nested type to the module class
        moduleClass.addNestedType(mixinInterface);
      }

      // Add constructor
      MethodSource<JavaClassSource> constructor = moduleClass.addMethod();
      constructor.setConstructor(true);
      constructor.setPublic();

      // Build constructor body
      StringBuilder constructorBody = new StringBuilder();
      for (JacksonModuleEntryDto entry : moduleDef.getEntries()) {
        String mixinName = entry.dtoType().getClassName() + "Mixin";
        constructorBody.append(
            "setMixInAnnotation(%s.class, %s.class);%n"
                .formatted(entry.dtoType().getClassName(), mixinName));
      }
      constructor.setBody(constructorBody.toString());

      // Write the generated class
      String source = formatSource(moduleClass.toString());
      writeSimpleClassToFile(packageName, moduleClassName, source);

    } catch (BuilderException e) {
      logger.warning(
          "simple-builders: Error generating Jackson module for package %s: %s\n%s",
          packageName, e.getMessage(), java.util.Arrays.toString(e.getStackTrace()));
    }
  }

  private boolean shouldAddImport(String currentPackage, String fqn) {
    return StringUtils.isNotBlank(fqn)
        && fqn.contains(".")
        && !fqn.startsWith("java.lang.")
        && !currentPackage.equals(packageNameOf(fqn));
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
}
