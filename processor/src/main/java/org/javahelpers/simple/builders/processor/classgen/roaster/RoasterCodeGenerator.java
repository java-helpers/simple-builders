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

import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.mapType;
import static org.javahelpers.simple.builders.processor.classgen.roaster.RoasterMapper.resolveCodeTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.model.annotation.AnnotationDto;
import org.javahelpers.simple.builders.processor.model.annotation.InterfaceName;
import org.javahelpers.simple.builders.processor.model.core.ClassFieldDto;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.imports.ImportStatement;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocDto;
import org.javahelpers.simple.builders.processor.model.javadoc.JavadocTagDto;
import org.javahelpers.simple.builders.processor.model.method.ConstructorDto;
import org.javahelpers.simple.builders.processor.model.method.MethodCodeDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.NestedTypeDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.type.TypeNameArray;
import org.javahelpers.simple.builders.processor.processing.ProcessingLogger;
import org.javahelpers.simple.builders.processor.util.ImportCollector;
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
   * Generates a class from the given class definition.
   *
   * @param classDef DTO containing all information to create the class
   * @throws BuilderException if there is an error in source code generation
   */
  public void generateClass(GenerationTargetClassDto classDef) throws BuilderException {
    logger.debugStartOperation(
        "Code generation for class: %s", classDef.getTypeName().getClassName());

    String sourceCode = createClassSource(classDef);
    writeClassToFile(sourceCode, classDef);

    logger.debugEndOperation(
        "Successfully generated class: %s", classDef.getTypeName().getClassName());
  }

  private String createClassSource(GenerationTargetClassDto classDef) {
    // Class generation - works for all GenerationTargetClassDto instances
    JavaClassSource source = createJavaClassSource(classDef);
    addClassMetadata(source, classDef);
    appendFields(source, classDef);
    appendConstructors(source, classDef);
    appendMethods(source, classDef);
    appendNestedTypes(source, classDef);

    // Apply annotations first (Roaster will add imports)
    applyClassAnnotations(source, classDef);

    return renderClassSource(source);
  }

  private void applyClassAnnotations(JavaClassSource source, GenerationTargetClassDto classDef) {
    if (CollectionUtils.isEmpty(classDef.getClassAnnotations())) {
      return;
    }
    // Adding class annotations
    applyAnnotations(source, classDef.getClassAnnotations());
    logger.debug("Class-level annotations added");
  }

  private JavaClassSource createJavaClassSource(GenerationTargetClassDto classDef) {
    if (CollectionUtils.isNotEmpty(classDef.getGenerics())) {
      logger.debug("Class has %d generic type parameter(s)", classDef.getGenerics().size());
    }

    JavaClassSource source = Roaster.create(JavaClassSource.class);
    String packageName = classDef.getTypeName().getPackageName();
    if (StringUtils.isNotBlank(packageName)) {
      source.setPackage(packageName);
    } else {
      source.setDefaultPackage();
    }
    source.setName(classDef.getTypeName().getClassName());
    addGenericDeclarations(source, classDef.getGenerics());

    // Collect and add imports early (before elements are added)
    for (ImportStatement importStmt : collectImports(classDef)) {
      if (importStmt.isStatic()) {
        source.addImport(importStmt.getFullyQualifiedName()).setStatic(true);
      } else {
        source.addImport(importStmt.getFullyQualifiedName());
      }
    }

    logger.debug("JavaClassSource created");
    return source;
  }

  private void addClassMetadata(JavaClassSource source, GenerationTargetClassDto classDef) {
    applyJavadoc(source, classDef.getClassJavadoc());
    applyVisibility(source, classDef.getClassAccessModifier());
    applySuperType(source, classDef.getSuperType());

    // Add interfaces
    for (InterfaceName interfaceName : classDef.getInterfaces()) {
      source.addInterface(RoasterMapper.mapInterfaceToTypeName(interfaceName));
    }

    logger.debug("Class metadata added");
  }

  private String renderClassSource(JavaClassSource source) {
    String rendered = source.toUnformattedString();
    return formatSource(rendered);
  }

  private void appendFields(JavaClassSource source, GenerationTargetClassDto classDef) {
    logger.debugStartOperation("Generating %d fields", classDef.getClassFields().size());

    for (ClassFieldDto fieldDto : classDef.getClassFields()) {
      appendField(source, fieldDto);
    }

    logger.debugEndOperation("Fields added: %d fields", source.getFields().size());
  }

  private void appendField(JavaClassSource source, ClassFieldDto fieldDto) {
    FieldSource<JavaClassSource> field = source.addField();
    field.setName(fieldDto.getFieldName());
    field.setType(mapType(fieldDto.getFieldType()));
    applyVisibility(field, fieldDto.getVisibility());
    applyLiteralInitializer(field, fieldDto.getLiteralInitializer());
    applyJavadoc(field, fieldDto.getJavadoc());
  }

  private void appendConstructors(JavaClassSource source, GenerationTargetClassDto classDef) {
    logger.debugStartOperation("Generating %d constructors", classDef.getConstructors().size());

    for (ConstructorDto constructor : classDef.getConstructors()) {
      appendConstructor(source, constructor);
    }

    logger.debugEndOperation("Constructors added: %d", classDef.getConstructors().size());
  }

  private void appendConstructor(JavaClassSource source, ConstructorDto constructor) {
    MethodSource<JavaClassSource> method = source.addMethod();
    method.setConstructor(true);
    applyVisibility(method, constructor.getVisibility());
    for (MethodParameterDto param : constructor.getParameters()) {
      method.addParameter(mapType(param.getParameterType()), param.getParameterName());
    }
    applyJavadoc(method, constructor.getJavadoc());
    applyCodeBlock(method, constructor.getMethodCodeDto());
  }

  private void appendMethods(JavaClassSource source, GenerationTargetClassDto classDef) {
    logger.debugStartOperation("Generating %d method candidates", classDef.getMethods().size());

    // Resolve method conflicts by signature and priority
    List<MethodDto> resolvedMethods = resolveMethodConflicts(classDef.getMethods());
    logger.debug("Resolved to %d methods after conflict resolution", resolvedMethods.size());

    for (MethodDto methodDto : resolvedMethods) {
      appendMethod(source, methodDto, false, false);
    }

    logger.debugEndOperation("Methods added: %d", resolvedMethods.size());
  }

  private List<MethodDto> resolveMethodConflicts(List<MethodDto> methods) {
    MethodDto.MethodComparator comparator = new MethodDto.MethodComparator();

    // Sort methods by comparator for consistent ordering
    List<MethodDto> sortedMethods = methods.stream().sorted(comparator).toList();

    Map<String, MethodDto> signatureToMethod = new java.util.LinkedHashMap<>();

    for (MethodDto method : sortedMethods) {
      String signature = method.getSignatureKey();

      MethodDto existing = signatureToMethod.get(signature);
      if (existing == null) {
        signatureToMethod.put(signature, method);
      } else {
        if (method.getPriority() > existing.getPriority()) {
          signatureToMethod.put(signature, method);
          logger.warning(
              "  Method conflict: '%s' (priority %d) dropped in favor of priority %d",
              signature, existing.getPriority(), method.getPriority());
        } else if (method.getPriority() < existing.getPriority()) {
          logger.warning(
              "  Method conflict: '%s' (priority %d) dropped in favor of priority %d",
              signature, method.getPriority(), existing.getPriority());
        } else {
          logger.warning(
              "  Method conflict: '%s' (priority %d) - equal priority, keeping first",
              signature, method.getPriority());
        }
      }
    }

    // Sort the final result for reproducible output
    return signatureToMethod.values().stream().sorted(comparator).toList();
  }

  private void appendMethod(
      JavaClassSource source,
      MethodDto methodDto,
      boolean nestedTypeMethod,
      boolean interfaceMethod) {
    MethodSource<JavaClassSource> method = source.addMethod();
    configureMethod(method, methodDto, nestedTypeMethod, interfaceMethod);
    applyCodeBlock(method, methodDto.getMethodCodeDto());
  }

  private void appendNestedTypes(JavaClassSource source, GenerationTargetClassDto classDef) {
    if (CollectionUtils.isEmpty(classDef.getNestedTypes())) {
      return;
    }
    logger.debugStartOperation("Generating %d nested type(s)", classDef.getNestedTypes().size());
    for (NestedTypeDto nestedType : classDef.getNestedTypes()) {
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
    applyVisibility(nestedSource, nestedType.getVisibility());
    applyAnnotations(nestedSource, nestedType.getAnnotations());
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
      org.jboss.forge.roaster.model.source.VisibilityScopedSource<?> source,
      AccessModifier modifier) {
    if (modifier == null) {
      source.setPackagePrivate();
      return;
    }
    switch (modifier) {
      case PUBLIC -> source.setPublic();
      case PROTECTED -> source.setProtected();
      case PRIVATE -> source.setPrivate();
      case PACKAGE_PRIVATE -> source.setPackagePrivate();
      case DEFAULT -> source.setPackagePrivate(); // DEFAULT maps to PACKAGE_PRIVATE
    }
  }

  private void applySuperType(JavaClassSource source, TypeName superType) {
    if (superType != null) {
      source.setSuperType(mapType(superType));
    }
  }

  private void applyLiteralInitializer(
      FieldSource<JavaClassSource> field, String literalInitializer) {
    if (literalInitializer != null) {
      field.setLiteralInitializer(literalInitializer);
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

  private void applyCodeBlock(MethodSource<?> method, MethodCodeDto codeDto) {
    if (!codeDto.hasCode()) {
      // If implementation is missing, an empty body needs to be set
      method.setBody("");
      return;
    }
    method.setBody(resolveCodeTemplate(codeDto));
  }

  private void applyJavadoc(
      org.jboss.forge.roaster.model.source.JavaDocCapableSource<?> source, JavadocDto javadoc) {
    if (javadoc == null || !javadoc.hasContent()) {
      return;
    }

    // Note: source.getJavaDoc() never returns null - Roaster creates the JavaDoc if it doesn't
    // exist
    // The early return above ensures we only create JavaDoc when there's actual content to set

    // Set description text
    if (StringUtils.isNotBlank(javadoc.getDescription())) {
      source.getJavaDoc().setText(javadoc.getDescription());
    }

    // Remove existing tags and add new ones
    source.getJavaDoc().removeAllTags();
    for (JavadocTagDto tag : javadoc.getTags()) {
      if (tag.hasValue()) {
        source.getJavaDoc().addTagValue(tag.getFullTagName(), tag.tagValue());
      } else {
        source.getJavaDoc().addTagValue(tag.getFullTagName(), "");
      }
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

  private Set<ImportStatement> collectImports(GenerationTargetClassDto classDef) {
    ImportCollector collector = new ImportCollector(classDef.getTypeName());

    // ImportCollector handles all import extraction from the DTO
    collector.collectImports(classDef);

    return collector.getSortedImports();
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

  private void writeClassToFile(String sourceCode, GenerationTargetClassDto classDef)
      throws BuilderException {
    logger.debug(
        "Writing class to file: %s.%s",
        classDef.getTypeName().getPackageName(), classDef.getTypeName().getClassName());

    String qualifiedName = classDef.getTypeName().getFullQualifiedName();
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
}
