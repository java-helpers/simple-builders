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

package org.javahelpers.simple.builders.processor.generators.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.model.integration.JacksonModuleDefinitionDto;
import org.javahelpers.simple.builders.processor.model.integration.JacksonModuleEntryDto;
import org.javahelpers.simple.builders.processor.processing.ProcessingLogger;

/**
 * Generates Jackson SimpleModules to register all generated builders.
 *
 * <p>The generated modules include MixIn interfaces to map DTOs to their Builders using
 * {@code @JsonDeserialize(builder = ...)}. One module is generated per target package.
 */
public class JacksonModuleGenerator {

  private final ProcessingEnvironment processingEnv;
  private final ProcessingLogger logger;
  private final SetValuedMap<String, JacksonModuleEntryDto> entriesByPackage =
      new HashSetValuedHashMap<>();
  private final boolean jacksonAvailable;

  public JacksonModuleGenerator(ProcessingEnvironment processingEnv, ProcessingLogger logger) {
    this.processingEnv = processingEnv;
    this.logger = logger;
    this.jacksonAvailable =
        this.processingEnv
                .getElementUtils()
                .getTypeElement("com.fasterxml.jackson.databind.module.SimpleModule")
            != null;
  }

  public void addEntry(BuilderDefinitionDto builderDef, Element sourceElement) {
    BuilderConfiguration config = builderDef.getConfiguration();

    if (!validateForModuleGeneration(config, sourceElement)) {
      return;
    }

    String targetPackage = getTargetPackage(config, builderDef);

    entriesByPackage.put(
        targetPackage,
        new JacksonModuleEntryDto(
            builderDef.getBuildingTargetTypeName(), builderDef.getBuilderTypeName()));
  }

  private String getTargetPackage(BuilderConfiguration config, BuilderDefinitionDto builderDef) {
    String targetPackage = config.getJacksonModulePackage();
    if (targetPackage == null) {
      targetPackage = builderDef.getBuildingTargetTypeName().getPackageName();
    }
    return targetPackage;
  }

  private boolean validateForModuleGeneration(BuilderConfiguration config, Element sourceElement) {
    if (!config.shouldGenerateJacksonModule()) {
      return false;
    }

    if (!jacksonAvailable) {
      logger.warning(
          sourceElement,
          "simple-builders: generateJacksonModule is enabled for %s, but Jackson dependencies (com.fasterxml.jackson.databind.module.SimpleModule) are not found on the classpath. Module generation skipped.",
          sourceElement.getSimpleName());
      return false;
    }

    if (!config.shouldUseJacksonDeserializerAnnotation()) {
      logger.warning(
          sourceElement,
          "simple-builders: generateJacksonModule is enabled but usingJacksonDeserializerAnnotation is disabled. "
              + "This is a misconfiguration. Jackson Module will NOT be generated for %s.",
          sourceElement.getSimpleName());
      return false;
    }
    return true;
  }

  /**
   * Returns the definitions for the Jackson Modules to be generated.
   *
   * <p>Logs status and clears state after retrieval.
   *
   * @return list of JacksonModuleDefinitionDto
   */
  public List<JacksonModuleDefinitionDto> getModuleDefinitions() {
    List<JacksonModuleDefinitionDto> definitions = new ArrayList<>();
    if (!entriesByPackage.isEmpty()) {
      logger.info(
          "simple-builders: Processing OVER. JacksonModuleEnabled: true, Packages: %d",
          entriesByPackage.keySet().size());

      for (String packageName : entriesByPackage.keySet()) {
        Set<JacksonModuleEntryDto> moduleEntries = entriesByPackage.get(packageName);
        JacksonModuleDefinitionDto definition = new JacksonModuleDefinitionDto(packageName);
        definition.addAllEntries(moduleEntries);
        definitions.add(definition);
      }
    }
    clear();
    return definitions;
  }

  private void clear() {
    entriesByPackage.clear();
  }
}
