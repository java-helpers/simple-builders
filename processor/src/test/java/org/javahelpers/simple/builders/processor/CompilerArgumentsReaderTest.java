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

package org.javahelpers.simple.builders.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.processing.BuilderConfigurationReader;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsEnum;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.testing.CapturingProcessingLogger;
import org.javahelpers.simple.builders.processor.testing.ProcessingEnvironmentStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link CompilerArgumentsReader} focusing on edge cases.
 *
 * <p>Tests null values, empty strings, case sensitivity, invalid inputs, and backward
 * compatibility.
 */
class CompilerArgumentsReaderTest {

  /** Test: readValue returns null when argument not set. */
  @Test
  void readValue_NotSet_ReturnsNull() {
    ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertNull(
        reader.readValue(CompilerArgumentsEnum.BUILDER_SUFFIX),
        "Should return null when argument not set");
  }

  /** Test: readValue prefers full compiler argument name over simple option name. */
  @Test
  void readValue_BothNamesSet_PrefersFullName() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.builder()
            .put("simplebuilder.builderSuffix", "FullName")
            .put("builderSuffix", "SimpleName")
            .build();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertEquals(
        "FullName",
        reader.readValue(CompilerArgumentsEnum.BUILDER_SUFFIX),
        "Should prefer full compiler argument name");
  }

  /** Test: readValue falls back to simple option name for backward compatibility. */
  @Test
  void readValue_OnlySimpleNameSet_UsesSimpleName() {
    Map<String, String> options = new HashMap<>();
    options.put("builderSuffix", "SimpleName");

    ProcessingEnvironment env = ProcessingEnvironmentStub.create(options);
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertEquals(
        "SimpleName",
        reader.readValue(CompilerArgumentsEnum.BUILDER_SUFFIX),
        "Should fall back to simple option name");
  }

  @Test
  void readValue_FormatterProfileCompilerOption_ReturnsValue() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.builder().put("simplebuilder.formatterProfile", "x").build();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertEquals("x", reader.readValue(CompilerArgumentsEnum.FORMATTER_PROFILE));
  }

  @Test
  void readValue_SystemPropertySet_FallsBackToSystemProperty() {
    System.setProperty("simplebuilder.verbose", "true");
    try {
      ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
      CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

      assertEquals("true", reader.readValue(CompilerArgumentsEnum.VERBOSE));
      assertTrue(reader.readBooleanValue(CompilerArgumentsEnum.VERBOSE));
    } finally {
      System.clearProperty("simplebuilder.verbose");
    }
  }

  @Test
  void readValue_CompilerArgAndSystemPropertySet_PrefersSystemProperty() {
    System.setProperty("simplebuilder.verbose", "true");
    try {
      ProcessingEnvironment env =
          ProcessingEnvironmentStub.builder().put("simplebuilder.verbose", "false").build();
      CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

      assertEquals("true", reader.readValue(CompilerArgumentsEnum.VERBOSE));
    } finally {
      System.clearProperty("simplebuilder.verbose");
    }
  }

  @Test
  void readValue_BareNameSystemProperty_Ignored() {
    System.setProperty("verbose", "true");
    try {
      ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
      CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

      assertNull(reader.readValue(CompilerArgumentsEnum.VERBOSE));
    } finally {
      System.clearProperty("verbose");
    }
  }

  /** Test: readBooleanValue returns false when value is null. */
  @Test
  void readBooleanValue_NullValue_ReturnsFalse() {
    ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertFalse(
        reader.readBooleanValue(CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER),
        "Should return false for null value");
  }

  /** Test: readBooleanValue returns false for empty string. */
  @Test
  void readBooleanValue_EmptyString_ReturnsFalse() {
    Map<String, String> options = new HashMap<>();
    options.put("simplebuilder.generateFieldSupplier", "");

    ProcessingEnvironment env = ProcessingEnvironmentStub.create(options);
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertFalse(
        reader.readBooleanValue(CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER),
        "Should return false for empty string");
  }

  /** Test: readBooleanValue handles case-insensitive "true" and "enabled". */
  @ParameterizedTest
  @ValueSource(
      strings = {"true", "TRUE", "True", "TrUe", "enabled", "ENABLED", "Enabled", "EnAbLeD"})
  void readBooleanValue_CaseInsensitiveTrueOrEnabled_ReturnsTrue(String value) {
    Map<String, String> options = new HashMap<>();
    options.put("simplebuilder.generateFieldSupplier", value);

    ProcessingEnvironment env = ProcessingEnvironmentStub.create(options);
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertTrue(
        reader.readBooleanValue(CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER),
        "Should return true for: " + value);
  }

  /** Test: readBooleanValue returns false for invalid values. */
  @ParameterizedTest
  @ValueSource(strings = {"false", "disabled", "yes", "1", "on", "invalid"})
  void readBooleanValue_InvalidValues_ReturnsFalse(String value) {
    Map<String, String> options = new HashMap<>();
    options.put("simplebuilder.generateFieldSupplier", value);

    ProcessingEnvironment env = ProcessingEnvironmentStub.create(options);
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    assertFalse(
        reader.readBooleanValue(CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER),
        "Should return false for: " + value);
  }

  /** Test: readBuilderConfiguration with no arguments returns all UNSET/DEFAULT values. */
  @Test
  void readBuilderConfiguration_NoArguments_ReturnsDefaults() {
    ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    BuilderConfiguration config =
        BuilderConfigurationReader.readBuilderConfiguration(
            reader, CapturingProcessingLogger.create().logger());

    assertNotNull(config, "Configuration should not be null");
    assertEquals(OptionState.UNSET, config.generateFieldSupplier());
    assertEquals(OptionState.UNSET, config.generateFieldConsumer());
    assertEquals(OptionState.UNSET, config.generateBuilderConsumer());
    assertEquals(AccessModifier.DEFAULT, config.getBuilderAccess());
    assertEquals(AccessModifier.DEFAULT, config.getBuilderConstructorAccess());
    assertEquals(AccessModifier.DEFAULT, config.getMethodAccess());
    assertNull(config.getBuilderSuffix(), "Builder suffix should be null when not set");
    assertNull(config.getSetterSuffix(), "Setter suffix should be null when not set");
    assertTrue(
        config.getBuilderGenerationPackagesSet().isEmpty(),
        "Builder generation packages should be empty when not set");
    assertTrue(
        config.getBuilderUsagePackagesSet().isEmpty(),
        "Builder usage packages should be empty when not set");
  }

  /** Test: readBuilderConfiguration reads all options correctly. */
  @Test
  void readBuilderConfiguration_AllOptionsSet_ReadsCorrectly() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.builder()
            .put("simplebuilder.generateFieldSupplier", "true")
            .put("simplebuilder.generateFieldConsumer", "false")
            .put("simplebuilder.generateBuilderConsumer", "enabled")
            .put("simplebuilder.builderAccess", "public")
            .put("simplebuilder.builderConstructorAccess", "private")
            .put("simplebuilder.methodAccess", "package-private")
            .put("simplebuilder.generateVarArgsHelpers", "disabled")
            .put("simplebuilder.generateUpdateHelpers", "enabled")
            .put("simplebuilder.copyTypeAnnotations", "enabled")
            .put("simplebuilder.builderSuffix", "Factory")
            .put("simplebuilder.setterSuffix", "with")
            .put("simplebuilder.builderGenerationPackages", "a.b, c.d")
            .put("simplebuilder.builderUsagePackages", "x.y")
            .build();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    BuilderConfiguration config =
        BuilderConfigurationReader.readBuilderConfiguration(
            reader, CapturingProcessingLogger.create().logger());

    assertEquals(OptionState.ENABLED, config.generateFieldSupplier());
    assertEquals(OptionState.DISABLED, config.generateFieldConsumer());
    assertEquals(OptionState.ENABLED, config.generateBuilderConsumer());
    assertEquals(AccessModifier.PUBLIC, config.getBuilderAccess());
    assertEquals(AccessModifier.PRIVATE, config.getBuilderConstructorAccess());
    assertEquals(AccessModifier.PACKAGE_PRIVATE, config.getMethodAccess());
    assertEquals(OptionState.DISABLED, config.generateVarArgsHelpers());
    assertEquals(OptionState.ENABLED, config.generateUpdateHelpers());
    assertEquals(OptionState.ENABLED, config.copyTypeAnnotations());
    assertEquals("Factory", config.getBuilderSuffix());
    assertEquals("with", config.getSetterSuffix());
    assertEquals(Set.of("a.b", "c.d"), config.getBuilderGenerationPackagesSet());
    assertEquals(Set.of("x.y"), config.getBuilderUsagePackagesSet());
  }

  /** Test: readBuilderConfiguration handles mixed valid and invalid values. */
  @Test
  void readBuilderConfiguration_MixedValidInvalid_HandlesGracefully() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.builder()
            .put("simplebuilder.generateFieldSupplier", "invalid")
            .put("simplebuilder.builderAccess", "protected") // invalid - no PROTECTED anymore
            .put("simplebuilder.generateFieldConsumer", "true")
            .build();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    BuilderConfiguration config =
        BuilderConfigurationReader.readBuilderConfiguration(
            reader, CapturingProcessingLogger.create().logger());

    assertEquals(
        OptionState.UNSET, config.generateFieldSupplier(), "Invalid option should be UNSET");
    assertEquals(
        AccessModifier.DEFAULT,
        config.getBuilderAccess(),
        "Invalid access modifier should be DEFAULT");
    assertEquals(
        OptionState.ENABLED, config.generateFieldConsumer(), "Valid option should be ENABLED");
  }

  /** Test: readBuilderConfiguration with empty string values. */
  @Test
  void readBuilderConfiguration_EmptyStringValues_HandlesGracefully() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.builder()
            .put("simplebuilder.generateFieldSupplier", "")
            .put("simplebuilder.builderAccess", "")
            .put("simplebuilder.builderSuffix", "")
            .put("simplebuilder.setterSuffix", "")
            .build();
    CompilerArgumentsReader reader = new CompilerArgumentsReader(env);

    BuilderConfiguration config =
        BuilderConfigurationReader.readBuilderConfiguration(
            reader, CapturingProcessingLogger.create().logger());

    assertEquals(OptionState.UNSET, config.generateFieldSupplier(), "Empty should be UNSET");
    assertEquals(AccessModifier.DEFAULT, config.getBuilderAccess(), "Empty should be DEFAULT");
    assertEquals("", config.getBuilderSuffix(), "Empty string should be preserved for suffix");
    assertEquals("", config.getSetterSuffix(), "Empty string should be preserved for suffix");
  }
}
