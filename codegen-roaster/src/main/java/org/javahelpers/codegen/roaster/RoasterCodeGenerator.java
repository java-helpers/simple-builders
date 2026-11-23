/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.codegen.roaster;

import static org.javahelpers.codegen.GenerationPhases.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.codegen.ClassCodeGenerator;
import org.javahelpers.codegen.CodeGenerationException;
import org.javahelpers.codegen.GeneratedSourceFile;
import org.javahelpers.codegen.GenerationEnvironment;
import org.javahelpers.codegen.GenerationIssueReporter;
import org.javahelpers.codegen.SourceFormatter;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.logging.PerformanceTracker;
import org.javahelpers.codegen.logging.ProcessingLogger;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocTagDto;
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.ConstructorDto;
import org.javahelpers.codegen.model.method.MethodCodeDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.util.ImportCollector;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.source.TypeVariableSource;

/** Roaster-based code generator for generated source files. */
public class RoasterCodeGenerator implements ClassCodeGenerator {
  /** Processing environment for accessing filer and element utilities. */
  private final GenerationEnvironment environment;

  /** Logger for debug output during code generation. */
  private final ProcessingLogger logger;

  /** Performance tracker for sub-phase timing (Source Construction, File Writing). */
  private final PerformanceTracker performanceTracker;

  /** Reporter for problems found during generation; the reporter decides the severity. */
  private final GenerationIssueReporter issueReporter;

  /**
   * Creates a code generator using the given generation environment.
   *
   * @param environment the generation environment providing processing environment, logger,
   *     performance tracker and issue reporter
   */
  public RoasterCodeGenerator(GenerationEnvironment environment) {
    this.environment = Objects.requireNonNull(environment, "environment must not be null");
    this.logger = Objects.requireNonNull(environment.getLogger(), "logger must not be null");
    this.performanceTracker =
        Objects.requireNonNull(
            environment.getPerformanceTracker(), "performanceTracker must not be null");
    this.issueReporter =
        Objects.requireNonNull(environment.getIssueReporter(), "issueReporter must not be null");
  }

  /**
   * Generates a class from the given class definition.
   *
   * @param classDef DTO containing all information to create the class
   * @param formatter formats the generated source before it is written
   * @throws CodeGenerationException if there is an error in source code generation
   */
  @Override
  public void generateClass(GenerationTargetClassDto classDef, SourceFormatter formatter)
      throws CodeGenerationException {
    logger.debugStartOperation(
        "Code generation for class: %s", classDef.getTypeName().getClassName());

    String sourceCode;
    try {
      performanceTracker.startPhase();
      JavaClassSource source = buildClassSource(classDef);
      performanceTracker.startPhase();
      String unformatted = source.toUnformattedString();
      performanceTracker.endPhase(PHASE_STRING_GENERATION);
      performanceTracker.startPhase();
      sourceCode = formatSource(unformatted, formatter);
      // Roaster renders some java.lang annotations (e.g. @SuppressWarnings, @Deprecated with
      // members) with their FQN (@java.lang.SuppressWarnings) even though java.lang types don't
      // need qualification. Fix this by replacing @java.lang.Xxx with @Xxx for known annotations.
      sourceCode = sourceCode.replace("@java.lang.SuppressWarnings", "@SuppressWarnings");
      sourceCode = sourceCode.replace("@java.lang.Deprecated", "@Deprecated");
      performanceTracker.endPhase(PHASE_FORMATTING);
      performanceTracker.endPhase(PHASE_SOURCE_CONSTRUCTION);
    } catch (RuntimeException ex) {
      // Rendering failures (e.g. RoasterMapperException) are RuntimeExceptions. Convert them
      // into a CodeGenerationException so callers can isolate the failure to this single class and
      // keep generating the remaining classes instead of aborting the whole processing round.
      throw new CodeGenerationException(ex);
    }
    performanceTracker.startPhase();
    writeClassToFile(sourceCode, classDef);
    performanceTracker.endPhase(PHASE_FILE_WRITING);

    // The generation scope stays open — the caller's post-generation step (onTaskGenerated in the
    // task driver) closes it with its summary line (see AbstractCodegenProcessor.generateTarget).
    logger.debug("Successfully generated class: %s", classDef.getTypeName().getClassName());
  }

  private JavaClassSource buildClassSource(GenerationTargetClassDto classDef) {
    performanceTracker.startPhase();
    ImportCollector importCollector =
        new ImportCollector(classDef.getTypeName()).collectImports(classDef);
    Set<String> collidingSimpleNames = importCollector.getCollidingSimpleNames();
    JavaClassSource source =
        createJavaClassSource(classDef, importCollector.getSortedImports(), collidingSimpleNames);
    addClassMetadata(source, classDef, collidingSimpleNames);
    appendFields(source, classDef, collidingSimpleNames);
    appendConstructors(source, classDef, collidingSimpleNames);
    appendMethods(source, classDef, collidingSimpleNames);
    appendNestedTypes(source, classDef, collidingSimpleNames);
    applyClassAnnotations(source, classDef);
    performanceTracker.endPhase(PHASE_ELEMENT_BUILDING);
    return source;
  }

  private void applyClassAnnotations(JavaClassSource source, GenerationTargetClassDto classDef) {
    performanceTracker.startPhase();
    if (CollectionUtils.isNotEmpty(classDef.getClassAnnotations())) {
      // Adding class annotations
      applyAnnotations(source, classDef.getClassAnnotations());
      logger.debug("Class-level annotations added");
    }
    performanceTracker.endPhase(PHASE_CLASS_ANNOTATIONS);
  }

  private JavaClassSource createJavaClassSource(
      GenerationTargetClassDto classDef,
      Set<ImportStatement> importStmts,
      Set<String> collidingSimpleNames) {
    performanceTracker.startPhase();
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
    addGenericDeclarations(source, classDef.getGenerics(), collidingSimpleNames);

    // Collect and add imports early (before elements are added)
    for (ImportStatement importStmt : importStmts) {
      if (importStmt.isStatic()) {
        source.addImport(importStmt.getFullyQualifiedName()).setStatic(true);
      } else {
        source.addImport(importStmt.getFullyQualifiedName());
      }
    }

    logger.debug("JavaClassSource created");
    performanceTracker.endPhase(PHASE_CLASS_CREATION);
    return source;
  }

  private void addClassMetadata(
      JavaClassSource source, GenerationTargetClassDto classDef, Set<String> collidingSimpleNames) {
    performanceTracker.startPhase();
    applyJavadoc(source, classDef.getClassJavadoc(), collidingSimpleNames);
    applyVisibility(source, classDef.getClassAccessModifier());
    applySuperType(source, classDef.getSuperType(), collidingSimpleNames);

    // Add interfaces
    for (InterfaceName interfaceName : classDef.getInterfaces()) {
      source.addInterface(
          RoasterMapper.mapInterfaceToTypeName(interfaceName, collidingSimpleNames));
    }

    logger.debug("Class metadata added");
    performanceTracker.endPhase(PHASE_CLASS_METADATA);
  }

  private void appendFields(
      JavaClassSource source, GenerationTargetClassDto classDef, Set<String> collidingSimpleNames) {
    performanceTracker.startPhase();
    if (CollectionUtils.isEmpty(classDef.getClassFields())) {
      performanceTracker.endPhase(PHASE_FIELDS);
      return;
    }
    logger.debugStartOperation("Generating %d fields", classDef.getClassFields().size());
    for (ClassFieldDto fieldDto : classDef.getClassFields()) {
      appendField(source, fieldDto, collidingSimpleNames);
    }
    logger.debugEndOperation("Fields added: %d fields", source.getFields().size());
    performanceTracker.endPhase(PHASE_FIELDS);
  }

  private void appendField(
      JavaClassSource source, ClassFieldDto fieldDto, Set<String> collidingSimpleNames) {
    String typeSource = mapType(fieldDto.getFieldType(), collidingSimpleNames);
    FieldSource<JavaClassSource> field;
    if (typeSource.contains("@")) {
      // Declaration form is needed for type-use annotations, but keeps qualified names literal
      // (no normalization against imports).
      field = source.addField(typeSource + " " + fieldDto.getFieldName() + ";");
    } else {
      // setType normalizes fully qualified names against imports, so a colliding type that is
      // imported renders by simple name while only the non-imported one stays qualified.
      field = source.addField();
      field.setName(fieldDto.getFieldName());
      field.setType(typeSource);
    }
    applyVisibility(field, fieldDto.getVisibility());
    applyLiteralInitializer(field, fieldDto.getLiteralInitializer());
    applyJavadoc(field, fieldDto.getJavadoc(), collidingSimpleNames);
  }

  private void appendConstructors(
      JavaClassSource source, GenerationTargetClassDto classDef, Set<String> collidingSimpleNames) {
    performanceTracker.startPhase();
    if (CollectionUtils.isEmpty(classDef.getConstructors())) {
      performanceTracker.endPhase(PHASE_CONSTRUCTORS);
      return;
    }
    logger.debugStartOperation("Generating %d constructors", classDef.getConstructors().size());
    for (ConstructorDto constructor : classDef.getConstructors()) {
      appendConstructor(source, constructor, collidingSimpleNames);
    }
    logger.debugEndOperation("Constructors added: %d", classDef.getConstructors().size());
    performanceTracker.endPhase(PHASE_CONSTRUCTORS);
  }

  private void appendConstructor(
      JavaClassSource source, ConstructorDto constructor, Set<String> collidingSimpleNames) {
    MethodSource<JavaClassSource> method = source.addMethod();
    method.setConstructor(true);
    applyVisibility(method, constructor.getVisibility());
    for (MethodParameterDto param : constructor.getParameters()) {
      method.addParameter(
          mapType(param.getParameterType(), collidingSimpleNames), param.getParameterName());
    }
    applyJavadoc(method, constructor.getJavadoc(), collidingSimpleNames);
    applyAnnotations(method, constructor.getAnnotations());
    applyCodeBlock(method, constructor.getMethodCodeDto(), collidingSimpleNames);
  }

  private void appendMethods(
      JavaClassSource source, GenerationTargetClassDto classDef, Set<String> collidingSimpleNames) {
    performanceTracker.startPhase();
    if (CollectionUtils.isEmpty(classDef.getMethods())) {
      performanceTracker.endPhase(PHASE_METHODS);
      return;
    }
    logger.debugStartOperation("Generating %d method candidates", classDef.getMethods().size());

    // Resolve method conflicts by signature and priority
    List<MethodDto> resolvedMethods = resolveMethodConflicts(classDef.getMethods());
    logger.debug("Resolved to %d methods after conflict resolution", resolvedMethods.size());

    for (MethodDto methodDto : resolvedMethods) {
      appendMethod(source, methodDto, false, false, collidingSimpleNames);
    }

    logger.debugEndOperation("Methods added: %d", resolvedMethods.size());
    performanceTracker.endPhase(PHASE_METHODS);
  }

  /**
   * Generic safety net for method conflict resolution, preventing the code generator from producing
   * invalid output (duplicate method signatures).
   *
   * <p>This is a generation-level check that operates on {@link MethodDto} (rendering-side DTO)
   * which no longer carries field-origin metadata. Domain-specific conflict resolution with
   * field-origin logging is expected to happen earlier, before the rendering DTOs reach this
   * generator.
   *
   * <p>Since conflicts should already be resolved by the domain-specific step, this safety net
   * simply keeps the first occurrence for any remaining duplicate signatures and reports it as a
   * warning (an error in strict mode).
   */
  private List<MethodDto> resolveMethodConflicts(List<MethodDto> methods) {
    Set<String> seenSignatures = new HashSet<>();
    List<MethodDto> deduplicated = new ArrayList<>(methods.size());
    for (MethodDto method : methods) {
      if (seenSignatures.add(method.getSignatureKey())) {
        deduplicated.add(method);
      } else {
        issueReporter.report(
            "Unexpected duplicate method signature: '%s' — keeping first occurrence (safety net)",
            method.getSignatureKey());
      }
    }
    return deduplicated;
  }

  private void appendMethod(
      JavaClassSource source,
      MethodDto methodDto,
      boolean nestedTypeMethod,
      boolean interfaceMethod,
      Set<String> collidingSimpleNames) {
    MethodSource<?> method = createMethod(source, methodDto, collidingSimpleNames);
    configureMethod(method, methodDto, nestedTypeMethod, interfaceMethod, collidingSimpleNames);
    applyCodeBlock(method, methodDto.getMethodCodeDto(), collidingSimpleNames);
  }

  private void appendNestedTypes(
      JavaClassSource source, GenerationTargetClassDto classDef, Set<String> collidingSimpleNames) {
    performanceTracker.startPhase();
    if (CollectionUtils.isNotEmpty(classDef.getNestedTypes())) {
      logger.debugStartOperation("Generating %d nested type(s)", classDef.getNestedTypes().size());
      for (NestedTypeDto nestedType : classDef.getNestedTypes()) {
        appendNestedType(source, nestedType, collidingSimpleNames);
        logger.debug("Generated nested type: %s", nestedType.getTypeName());
      }
      logger.debugEndOperation("Nested types added");
    }
    performanceTracker.endPhase(PHASE_NESTED_TYPES);
  }

  private void appendNestedType(
      JavaClassSource source, NestedTypeDto nestedType, Set<String> collidingSimpleNames) {
    JavaSource<?> nestedSource =
        nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE
            ? source.addNestedType(JavaInterfaceSource.class)
            : source.addNestedType(JavaClassSource.class);
    nestedSource.setName(nestedType.getTypeName());
    applyVisibility(nestedSource, nestedType.getVisibility());
    applyAnnotations(nestedSource, nestedType.getAnnotations());
    applyJavadoc(nestedSource, nestedType.getJavadoc(), collidingSimpleNames);
    boolean isInterface = nestedType.getKind() == NestedTypeDto.NestedTypeKind.INTERFACE;
    for (MethodDto methodDto : nestedType.getMethods()) {
      appendNestedMethod(nestedSource, methodDto, isInterface, collidingSimpleNames);
    }
  }

  private void appendNestedMethod(
      JavaSource<?> source,
      MethodDto methodDto,
      boolean isInterface,
      Set<String> collidingSimpleNames) {
    org.jboss.forge.roaster.model.source.MethodHolderSource<?> methodHolder =
        (org.jboss.forge.roaster.model.source.MethodHolderSource<?>) source;
    MethodSource<?> method = createMethod(methodHolder, methodDto, collidingSimpleNames);
    configureMethod(method, methodDto, true, isInterface, collidingSimpleNames);
    if (methodDto.getMethodCodeDto() != null
        && StringUtils.isNotBlank(methodDto.getMethodCodeDto().getCodeFormat())) {
      method.setBody(resolveCodeTemplate(methodDto.getMethodCodeDto(), collidingSimpleNames));
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

  private void applySuperType(
      JavaClassSource source, TypeName superType, Set<String> collidingSimpleNames) {
    if (superType != null) {
      source.setSuperType(mapType(superType, collidingSimpleNames));
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
      List<org.javahelpers.codegen.model.type.GenericParameterDto> generics,
      Set<String> collidingSimpleNames) {
    if (CollectionUtils.isEmpty(generics)) {
      return;
    }
    for (org.javahelpers.codegen.model.type.GenericParameterDto generic : generics) {
      TypeVariableSource<JavaClassSource> typeVariable = source.addTypeVariable(generic.getName());
      if (CollectionUtils.isNotEmpty(generic.getUpperBounds())) {
        typeVariable.setBounds(
            generic.getUpperBounds().stream()
                .map(type -> mapType(type, collidingSimpleNames))
                .toArray(String[]::new));
      }
    }
  }

  private void addGenericDeclarations(
      MethodSource<?> source,
      List<org.javahelpers.codegen.model.type.GenericParameterDto> generics,
      Set<String> collidingSimpleNames) {
    if (CollectionUtils.isEmpty(generics)) {
      return;
    }
    for (org.javahelpers.codegen.model.type.GenericParameterDto generic : generics) {
      TypeVariableSource<?> typeVariable = source.addTypeVariable(generic.getName());
      if (CollectionUtils.isNotEmpty(generic.getUpperBounds())) {
        typeVariable.setBounds(
            generic.getUpperBounds().stream()
                .map(type -> mapType(type, collidingSimpleNames))
                .toArray(String[]::new));
      }
    }
  }

  private void applyCodeBlock(
      MethodSource<?> method, MethodCodeDto codeDto, Set<String> collidingSimpleNames) {
    if (!codeDto.hasCode()) {
      // If implementation is missing, an empty body needs to be set
      method.setBody("");
      return;
    }
    method.setBody(resolveCodeTemplate(codeDto, collidingSimpleNames));
  }

  private void applyJavadoc(
      org.jboss.forge.roaster.model.source.JavaDocCapableSource<?> source,
      JavadocDto javadoc,
      Set<String> collidingSimpleNames) {
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

    // Render the code example block
    JavadocCodeBlockDto codeBlock = javadoc.getExampleUsageCodeBlock();
    if (codeBlock != null && codeBlock.hasCode()) {
      // Resolve placeholders in the code block
      String resolvedCode = resolveCodeTemplate(codeBlock, collidingSimpleNames);
      // Pre-prefix every line of the code body with " * " so it survives Roaster's
      // preformatted-block handling (Roaster does not auto-add asterisk prefix inside <pre>).
      String prefixedCode =
          Arrays.stream(resolvedCode.split("\n", -1))
              .map(line -> " * " + line)
              .collect(Collectors.joining("\n"));
      // Add the code example to the Javadoc with a blank line separator before <h4>.
      String currentText = source.getJavaDoc().getText();
      String exampleText = "<h4>Example:</h4><pre>{@code\n" + prefixedCode + "\n * }</pre>";
      if (StringUtils.isNotBlank(currentText)) {
        source.getJavaDoc().setText(currentText + "\n\n" + exampleText);
      } else {
        source.getJavaDoc().setText(exampleText);
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
      TypeName type = annotationDto.getAnnotationType();
      // Use the simple name for java.lang annotations (e.g. @SuppressWarnings, @Deprecated) so
      // Roaster renders them without the java.lang prefix. Other annotations use their FQN.
      String annotationName =
          "java.lang".equals(type.getPackageName())
              ? type.getClassName()
              : type.getFullQualifiedName();
      AnnotationSource<?> annotation = source.addAnnotation(annotationName);
      for (Map.Entry<String, String> member : annotationDto.getMembers().entrySet()) {
        if ("value".equals(member.getKey())) {
          annotation.setLiteralValue(member.getValue());
        } else {
          annotation.setLiteralValue(member.getKey(), member.getValue());
        }
      }
    }
  }

  private MethodSource<?> createMethod(
      MethodHolderSource<?> source, MethodDto methodDto, Set<String> collidingSimpleNames) {
    String returnTypeSource =
        methodDto.getReturnType() == null
            ? "void"
            : mapType(methodDto.getReturnType(), collidingSimpleNames);
    if (returnTypeSource.contains("@")) {
      // Signature form is needed for type-use annotations, but keeps qualified names literal
      // (no normalization against imports).
      return source.addMethod(returnTypeSource + " " + methodDto.getMethodName() + "();");
    }
    // setReturnType normalizes qualified names against imports, like the field path.
    MethodSource<?> method = source.addMethod();
    if (methodDto.getReturnType() == null) {
      method.setReturnTypeVoid();
    } else {
      method.setReturnType(returnTypeSource);
    }
    method.setName(methodDto.getMethodName());
    return method;
  }

  private void configureMethod(
      MethodSource<?> method,
      MethodDto methodDto,
      boolean nestedTypeMethod,
      boolean interfaceMethod,
      Set<String> collidingSimpleNames) {
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

    addGenericDeclarations(method, methodDto.getGenericParameters(), collidingSimpleNames);
    applyJavadoc(method, methodDto.getJavadoc(), collidingSimpleNames);
    applyAnnotations(method, methodDto.getAnnotations());

    for (int i = 0; i < methodDto.getParameters().size(); i++) {
      MethodParameterDto paramDto = methodDto.getParameters().get(i);
      boolean lastParameter = i == methodDto.getParameters().size() - 1;
      addParameter(method, paramDto, lastParameter, collidingSimpleNames);
    }
  }

  private void addParameter(
      MethodSource<?> method,
      MethodParameterDto paramDto,
      boolean lastParameter,
      Set<String> collidingSimpleNames) {
    boolean varArgs = lastParameter && paramDto.getParameterType() instanceof TypeNameArray;
    TypeName parameterType =
        varArgs
            ? ((TypeNameArray) paramDto.getParameterType()).getTypeOfArray()
            : paramDto.getParameterType();
    String typeSource = mapType(parameterType, collidingSimpleNames);
    ParameterSource<?> parameter;
    if (typeSource.contains(">.")) {
      parameter = addParameterWithNestedGenerics(method, typeSource, paramDto);
    } else {
      parameter = method.addParameter(typeSource, paramDto.getParameterName());
    }
    if (varArgs) {
      parameter.setVarArgs(true);
    }
    applyAnnotations(parameter, paramDto.getAnnotations());
  }

  // Roaster's addParameter cannot split generics on both an owner and its member.
  private ParameterSource<?> addParameterWithNestedGenerics(
      MethodSource<?> method, String typeSource, MethodParameterDto paramDto) {
    String existing =
        method.getParameters().stream().map(Object::toString).collect(Collectors.joining(", "));
    method.setParameters(
        (existing.isEmpty() ? "" : existing + ", ")
            + typeSource
            + " "
            + paramDto.getParameterName());
    return method.getParameters().get(method.getParameters().size() - 1);
  }

  private String mapType(TypeName typeName, Set<String> collidingSimpleNames) {
    return RoasterMapper.mapType(typeName, collidingSimpleNames);
  }

  private String resolveCodeTemplate(CodeTemplateDto codeDto, Set<String> collidingSimpleNames) {
    return RoasterMapper.resolveCodeTemplate(codeDto, collidingSimpleNames);
  }

  private String formatSource(String rawSource, SourceFormatter formatter) {
    return formatter.format(rawSource);
  }

  private void writeClassToFile(String sourceCode, GenerationTargetClassDto classDef)
      throws CodeGenerationException {
    String qualifiedName = classDef.getTypeName().getFullQualifiedName();
    logger.debug("Creating JavaFileObject: %s", qualifiedName);
    try {
      GeneratedSourceFile file = environment.createSourceFile(qualifiedName);
      logger.debug("Writing class to file: %s", file.toUri().getPath());
      try (Writer writer = file.openWriter()) {
        writer.write(sourceCode);
      }
    } catch (IOException ex) {
      String message = ex.getMessage();
      String errorMessage =
          """
          Unable to create target class '%s': %s.
          Check the build environment and ensure all necessary directories are accessible.
          """
              .formatted(
                  qualifiedName, StringUtils.isNotBlank(message) ? message : "Unknown error");
      throw new CodeGenerationException(errorMessage);
    }
  }
}
