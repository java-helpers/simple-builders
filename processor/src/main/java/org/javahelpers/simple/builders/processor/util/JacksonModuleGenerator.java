/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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

package org.javahelpers.simple.builders.processor.util;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import org.javahelpers.simple.builders.processor.dtos.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;

/**
 * Generates Jackson SimpleModules to register all generated builders.
 *
 * <p>The generated modules include MixIn interfaces to map DTOs to their Builders using
 * {@code @JsonDeserialize(builder = ...)}. One module is generated per target package.
 */
public class JacksonModuleGenerator {

  private static final String MODULE_CLASS_NAME = "SimpleBuildersJacksonModule";
  private final Filer filer;
  private final Elements elementUtils;
  private final ProcessingLogger logger;
  private final Map<String, Set<JacksonModuleEntry>> entriesByPackage = new HashMap<>();
  private final boolean jacksonAvailable;
  private boolean enabled = false;

  public JacksonModuleGenerator(Filer filer, Elements elementUtils, ProcessingLogger logger) {
    this.filer = filer;
    this.elementUtils = elementUtils;
    this.logger = logger;
    this.jacksonAvailable =
        this.elementUtils.getTypeElement("com.fasterxml.jackson.databind.module.SimpleModule")
            != null;
  }

  public void addEntry(BuilderDefinitionDto builderDef, Element sourceElement) {
    BuilderConfiguration config = builderDef.getConfiguration();

    if (!config.shouldGenerateJacksonModule()) {
      return;
    }

    if (!jacksonAvailable) {
      logger.warning(
          sourceElement,
          "simple-builders: generateJacksonModule is enabled for %s, but Jackson dependencies (com.fasterxml.jackson.databind.module.SimpleModule) are not found on the classpath. Module generation skipped.",
          sourceElement.getSimpleName());
      return;
    }

    if (!config.shouldUseJacksonDeserializerAnnotation()) {
      logger.warning(
          sourceElement,
          "simple-builders: generateJacksonModule is enabled but usingJacksonDeserializerAnnotation is disabled. "
              + "This is a misconfiguration. Jackson Module will NOT be generated for %s.",
          sourceElement.getSimpleName());
      return;
    }

    String targetPackage = config.getJacksonModulePackage();
    if (targetPackage == null) {
      targetPackage = builderDef.getBuildingTargetTypeName().getPackageName();
    }

    this.enabled = true;
    entriesByPackage
        .computeIfAbsent(targetPackage, k -> new HashSet<>())
        .add(
            new JacksonModuleEntry(
                builderDef.getBuildingTargetTypeName(), builderDef.getBuilderTypeName()));
  }

  /**
   * Generates the Jackson Modules containing registrations for the accumulated builders.
   *
   * <p>Logs status and clears state after generation.
   */
  public void generate() {
    logger.info(
        "simple-builders: Processing OVER. JacksonModuleEnabled: %s, Packages: %d",
        enabled, entriesByPackage.size());

    if (enabled && !entriesByPackage.isEmpty()) {
      for (Map.Entry<String, Set<JacksonModuleEntry>> entry : entriesByPackage.entrySet()) {
        String packageName = entry.getKey();
        Set<JacksonModuleEntry> moduleEntries = entry.getValue();

        try {
          generateModuleFile(packageName, moduleEntries);
        } catch (Exception e) {
          logger.error(
              "simple-builders: Error generating Jackson module for package %s: %s",
              packageName, e.getMessage());
          e.printStackTrace();
        }
      }
    }
    clear();
  }

  private void clear() {
    entriesByPackage.clear();
    enabled = false;
  }

  private void generateModuleFile(String targetPackage, Set<JacksonModuleEntry> entries) {
    logger.info("Generating Jackson Module '%s' in package '%s'", MODULE_CLASS_NAME, targetPackage);

    ClassName simpleModuleClass =
        ClassName.get("com.fasterxml.jackson.databind.module", "SimpleModule");
    ClassName jsonDeserializeClass =
        ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonDeserialize");

    // Create the constructor
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    // Create the class
    TypeSpec.Builder classBuilder =
        TypeSpec.classBuilder(MODULE_CLASS_NAME)
            .addModifiers(Modifier.PUBLIC)
            .superclass(simpleModuleClass);

    for (JacksonModuleEntry entry : entries) {
      ClassName dtoClass =
          ClassName.get(entry.dtoType().getPackageName(), entry.dtoType().getClassName());
      ClassName builderClass =
          ClassName.get(entry.builderType().getPackageName(), entry.builderType().getClassName());

      // Create MixIn interface name: DtoNameMixin
      String mixinName = entry.dtoType().getClassName() + "Mixin";

      // Create MixIn interface with @JsonDeserialize(builder = Builder.class)
      TypeSpec mixinInterface =
          TypeSpec.interfaceBuilder(mixinName)
              .addModifiers(Modifier.PRIVATE)
              .addAnnotation(
                  AnnotationSpec.builder(jsonDeserializeClass)
                      .addMember("builder", "$T.class", builderClass)
                      .build())
              .build();

      classBuilder.addType(mixinInterface);

      // Add registration to constructor: setMixInAnnotation(Dto.class, Mixin.class)
      constructorBuilder.addStatement(
          "setMixInAnnotation($T.class, $N.class)", dtoClass, mixinName);
    }

    classBuilder.addMethod(constructorBuilder.build());

    // Write file
    try {
      JavaFile.builder(targetPackage, classBuilder.build()).build().writeTo(filer);
    } catch (IOException e) {
      logger.error("Failed to write Jackson Module: %s", e.getMessage());
    }
  }

  public record JacksonModuleEntry(TypeName dtoType, TypeName builderType) {}
}
