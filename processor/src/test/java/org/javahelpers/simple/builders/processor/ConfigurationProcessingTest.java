package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.dtos.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests ensuring all configuration options are properly handled.
 *
 * <p>These tests are designed to fail at compile-time if:
 *
 * <ul>
 *   <li>A new configuration option is added but not included in the builder
 *   <li>A new option is added but not handled in merge logic
 *   <li>A new option is added but not included in toString
 * </ul>
 *
 * <p>This ensures that when extending the configuration, all three places must be updated:
 *
 * <ol>
 *   <li>BuilderConfiguration record parameters
 *   <li>BuilderConfiguration.Builder
 *   <li>BuilderConfiguration.merge() method
 * </ol>
 */
class ConfigurationProcessingTest {

  /**
   * Completeness test: All configuration options must be settable via builder.
   *
   * <p>If you add a new configuration option and this test doesn't compile, you need to:
   *
   * <ol>
   *   <li>Add the parameter to BuilderConfiguration record
   *   <li>Add builder methods in BuilderConfiguration.Builder
   *   <li>Update DEFAULT configuration
   *   <li>Update this test to include the new option
   * </ol>
   */
  @Test
  void allConfigurationOptions_MustBeSettableViaBuilder() {
    // This test will fail to compile if any builder method is missing
    BuilderConfiguration config =
        BuilderConfiguration.builder()
            // Field setter generation options
            .generateSupplier(OptionState.ENABLED)
            .generateConsumer(OptionState.ENABLED)
            .generateBuilderProvider(OptionState.ENABLED)
            // Conditional logic
            .generateConditionalLogic(OptionState.ENABLED)
            // Access control
            .builderAccess(AccessModifier.PACKAGE_PRIVATE)
            .methodAccess(AccessModifier.PACKAGE_PRIVATE)
            // Collection options
            .generateVarArgsHelpers(OptionState.ENABLED)
            .usingArrayListBuilder(OptionState.ENABLED)
            .usingArrayListBuilderWithElementBuilders(OptionState.ENABLED)
            .usingHashSetBuilder(OptionState.ENABLED)
            .usingHashSetBuilderWithElementBuilders(OptionState.ENABLED)
            .usingHashMapBuilder(OptionState.ENABLED)
            // Integration
            .generateWithInterface(OptionState.ENABLED)
            .build();

    // Verify all options are accessible (this will fail to compile if accessors are missing)
    assertNotNull(config);
    assertEquals(OptionState.ENABLED, config.generateFieldSupplier());
    assertEquals(OptionState.ENABLED, config.generateFieldConsumer());
    assertEquals(OptionState.ENABLED, config.generateBuilderProvider());
    assertEquals(OptionState.ENABLED, config.generateConditionalHelper());
    assertEquals(AccessModifier.PACKAGE_PRIVATE, config.getBuilderAccess());
    assertEquals(AccessModifier.PACKAGE_PRIVATE, config.getMethodAccess());
    assertEquals(OptionState.ENABLED, config.generateVarArgsHelpers());
    assertEquals(OptionState.ENABLED, config.usingArrayListBuilder());
    assertEquals(OptionState.ENABLED, config.usingArrayListBuilderWithElementBuilders());
    assertEquals(OptionState.ENABLED, config.usingHashSetBuilder());
    assertEquals(OptionState.ENABLED, config.usingHashSetBuilderWithElementBuilders());
    assertEquals(OptionState.ENABLED, config.usingHashMapBuilder());
    assertEquals(OptionState.ENABLED, config.generateWithInterface());
  }

  /**
   * Compiler arguments integration test: Verify generated builder with all options disabled.
   *
   * <p>This test documents the current state of generated builder code when all compiler arguments
   * are set to false. It ensures that disabling features actually removes them from generated code.
   *
   * <p>If generated code format changes, update the expected text block to reflect current state.
   */
  @Test
  void compilerArguments_AllDisabled_ShouldGenerateMinimalBuilder() {
    // Given: DTO with various property types including nested DTO with builder and Address without
    // builder
    JavaFileObject nestedDto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "NestedDto",
            """
            private String value;
            public String getValue() { return value; }
            public void setValue(String value) { this.value = value; }
            """);

    JavaFileObject addressDto =
        ProcessorTestUtils.forSource(
            """
            package test;

            public class Address {
              private String street;
              private String city;

              public Address() {}

              public String getStreet() { return street; }
              public void setStreet(String street) { this.street = street; }

              public String getCity() { return city; }
              public void setCity(String city) { this.city = city; }
            }
            """);

    JavaFileObject source =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "MinimalDto",
            """
            private String name;
            private java.util.List<String> items;
            private java.util.Map<String, Integer> properties;
            private java.util.Optional<String> description;
            private java.util.Set<String> tags;
            private NestedDto nested;
            private Address address;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public java.util.List<String> getItems() { return items; }
            public void setItems(java.util.List<String> items) { this.items = items; }

            public java.util.Map<String, Integer> getProperties() { return properties; }
            public void setProperties(java.util.Map<String, Integer> properties) { this.properties = properties; }

            public java.util.Optional<String> getDescription() { return description; }
            public void setDescription(java.util.Optional<String> description) { this.description = description; }

            public java.util.Set<String> getTags() { return tags; }
            public void setTags(java.util.Set<String> tags) { this.tags = tags; }

            public NestedDto getNested() { return nested; }
            public void setNested(NestedDto nested) { this.nested = nested; }

            public Address getAddress() { return address; }
            public void setAddress(Address address) { this.address = address; }
            """);

    // When: Compile with ALL compiler arguments disabled
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new BuilderProcessor())
            .withOptions(
                "-Asimplebuilder.generateFieldSupplier=false",
                "-Asimplebuilder.generateFieldConsumer=false",
                "-Asimplebuilder.generateBuilderProvider=false",
                "-Asimplebuilder.generateConditionalHelper=false",
                "-Asimplebuilder.generateVarArgsHelpers=false",
                "-Asimplebuilder.generateUnboxedOptional=false",
                "-Asimplebuilder.usingArrayListBuilder=false",
                "-Asimplebuilder.usingArrayListBuilderWithElementBuilders=false",
                "-Asimplebuilder.usingHashSetBuilder=false",
                "-Asimplebuilder.usingHashSetBuilderWithElementBuilders=false",
                "-Asimplebuilder.usingHashMapBuilder=false",
                "-Asimplebuilder.usingGeneratedAnnotation=false",
                "-Asimplebuilder.usingBuilderImplementationAnnotation=false",
                "-Asimplebuilder.generateWithInterface=false")
            .compile(nestedDto, addressDto, source);

    // Then: Compilation should succeed
    assertThat(compilation).succeeded();

    // And: Generated builder should contain only basic functionality
    String generatedCode = ProcessorTestUtils.loadGeneratedSource(compilation, "MinimalDtoBuilder");

    // With generateFieldSupplier=false, NO supplier methods should be generated
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder name(Supplier<String> nameSupplier)",
        "public MinimalDtoBuilder items(Supplier<List<String>> itemsSupplier)",
        "public MinimalDtoBuilder properties(Supplier<Map<String, Integer>> propertiesSupplier)",
        "public MinimalDtoBuilder description(Supplier<Optional<String>> descriptionSupplier)",
        "public MinimalDtoBuilder tags(Supplier<Set<String>> tagsSupplier)",
        "public MinimalDtoBuilder nested(Supplier<NestedDto> nestedSupplier)",
        "public MinimalDtoBuilder address(Supplier<Address> addressSupplier)");

    // With generateFieldConsumer=false, NO field consumer methods should be generated
    // Field consumer = Consumer<T> where T is a custom type with empty constructor
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder address(Consumer<Address> addressConsumer)",
        "public MinimalDtoBuilder address(Consumer<Address> addressConsumer)",
        "public MinimalDtoBuilder nested(Consumer<NestedDto> nestedConsumer)",
        "public MinimalDtoBuilder items(Consumer<List<String>> itemsConsumer)",
        "public MinimalDtoBuilder tags(Consumer<Set<String>> tagsConsumer)",
        "public MinimalDtoBuilder properties(Consumer<Map<String, Integer>> propertiesConsumer)");

    // With generateBuilderProvider=false, NO builder consumer methods should be generated
    // Builder consumers include: StringBuilder, collection builders, nested DTO builders
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder nested(Consumer<NestedDtoBuilder> nestedBuilderConsumer)",
        "public MinimalDtoBuilder name(Consumer<StringBuilder> nameStringBuilderConsumer)",
        "public MinimalDtoBuilder description(Consumer<StringBuilder> descriptionStringBuilderConsumer)");

    // With generateConditionalHelper=false, NO conditional methods
    ProcessorAsserts.assertNotContaining(
        generatedCode, "public MinimalDtoBuilder conditional(BooleanSupplier condition");

    // With generateVarArgsHelpers=false, NO VarArgs helpers should be generated
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder items(String... items)",
        "public MinimalDtoBuilder properties(Map.Entry<String, Integer>... properties)",
        "public MinimalDtoBuilder tags(String... tags)");

    // With generateWithInterface=false, NO With interface
    ProcessorAsserts.assertNotContaining(generatedCode, "public interface With");

    // With generateUnboxedOptional=false, NO unboxed optional methods should be generated
    ProcessorAsserts.assertNotContaining(
        generatedCode, "public MinimalDtoBuilder description(String description)");

    // With usingGeneratedAnnotation=false, NO @Generated annotation should be used
    ProcessorAsserts.assertNotContaining(generatedCode, "@Generated(");

    // With usingBuilderImplementationAnnotation=false, NO @BuilderImplementation annotation should
    // be used
    ProcessorAsserts.assertNotContaining(generatedCode, "@BuilderImplementation");

    // With usingArrayListBuilder=false AND generateBuilderProvider=false, NO ArrayListBuilder
    // should be used
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder items(Consumer<ArrayListBuilder<String>> itemsBuilderConsumer)");

    // With usingHashSetBuilder=false AND generateBuilderProvider=false, NO HashSetBuilder should be
    // used
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder tags(Consumer<HashSetBuilder<String>> tagsBuilderConsumer)");

    // With usingHashMapBuilder=false AND generateBuilderProvider=false, NO HashMapBuilder should be
    // used
    ProcessorAsserts.assertNotContaining(
        generatedCode,
        "public MinimalDtoBuilder properties(Consumer<HashMapBuilder<String, Integer>> propertiesBuilderConsumer)");

    // Still generates: basic setters, String.format for String fields
    ProcessorAsserts.assertContaining(
        generatedCode,
        "class MinimalDtoBuilder implements IBuilderBase<MinimalDto>",
        "public MinimalDtoBuilder(MinimalDto instance)",
        "public MinimalDtoBuilder name(String name)",
        "public MinimalDtoBuilder name(String format, Object... args)",
        "public MinimalDtoBuilder description(String format, Object... args)",
        "public MinimalDtoBuilder items(List<String> items)",
        "public MinimalDtoBuilder properties(Map<String, Integer> properties)",
        "public MinimalDtoBuilder description(Optional<String> description)",
        "public MinimalDtoBuilder tags(Set<String> tags)",
        "public MinimalDtoBuilder nested(NestedDto nested)",
        "public MinimalDtoBuilder address(Address address)",
        "public MinimalDto build()",
        "public static MinimalDtoBuilder create()");
  }

  /**
   * Merge logic test: Configuration merging must respect priority correctly.
   *
   * <p>Priority: other > this (for non-UNSET/DEFAULT values)
   */
  @Test
  void configurationMerge_MustRespectPriority() {
    // Given: Base configuration with some values
    BuilderConfiguration base =
        BuilderConfiguration.builder()
            .generateSupplier(OptionState.ENABLED)
            .generateConsumer(OptionState.ENABLED)
            .builderAccess(AccessModifier.PUBLIC)
            .build();

    // When: Merge with override configuration
    BuilderConfiguration override =
        BuilderConfiguration.builder()
            .generateSupplier(OptionState.DISABLED) // Override
            .generateBuilderProvider(OptionState.DISABLED) // New value
            // generateConsumer not set, should keep base value
            .build();

    BuilderConfiguration merged = base.merge(override);

    // Then: Override values win, base values kept for unset
    assertEquals(
        OptionState.DISABLED,
        merged.generateFieldSupplier(),
        "Override should win for generateFieldSupplier");
    assertEquals(
        OptionState.ENABLED,
        merged.generateFieldConsumer(),
        "Base value should be kept when override is UNSET");
    assertEquals(
        OptionState.DISABLED, merged.generateBuilderProvider(), "Override should set new value");
    assertEquals(
        AccessModifier.PUBLIC,
        merged.getBuilderAccess(),
        "Base value should be kept when override is DEFAULT");
  }

  /**
   * toString test: Configuration must produce human-readable output.
   *
   * <p>This ensures debugging and logging shows meaningful information.
   */
  @Test
  void configurationToString_MustBeHumanReadable() {
    BuilderConfiguration config =
        BuilderConfiguration.builder()
            .generateSupplier(OptionState.DISABLED)
            .generateConsumer(OptionState.ENABLED)
            .builderAccess(AccessModifier.PRIVATE)
            .methodAccess(AccessModifier.PROTECTED)
            .build();

    String configString = config.toString();

    // Verify it contains field names and values
    assertNotNull(configString, "toString should not return null");
    assertTrue(
        configString.contains("generateFieldSupplier"), "toString should mention field names");
    assertTrue(configString.contains("DISABLED"), "toString should show enum values");
    assertTrue(configString.contains("ENABLED"), "toString should show enum values");
    assertTrue(configString.contains("PRIVATE"), "toString should show AccessModifier values");
    assertTrue(configString.contains("PROTECTED"), "toString should show AccessModifier values");

    // Verify it's not just a hash code
    assertTrue(configString.length() > 100, "toString should be detailed, not just class@hashcode");
  }

  /** Null-safe merge test: Merging with null should return this. */
  @Test
  void configurationMerge_WithNull_ShouldReturnThis() {
    BuilderConfiguration config =
        BuilderConfiguration.builder().generateSupplier(OptionState.DISABLED).build();

    BuilderConfiguration merged = config.merge(null);

    assertEquals(config, merged, "Merging with null should return the same configuration");
  }

  /**
   * Chain merge test: Multiple merges should apply in order.
   *
   * <p>This simulates: Defaults -> Compiler Args -> Template -> Options
   */
  @Test
  void configurationMerge_Chain_ShouldApplyInOrder() {
    // Layer 1: Defaults
    BuilderConfiguration defaults = BuilderConfiguration.DEFAULT;

    // Layer 2: Compiler arguments (override some defaults)
    BuilderConfiguration compilerArgs =
        BuilderConfiguration.builder()
            .generateSupplier(OptionState.DISABLED)
            .builderAccess(AccessModifier.PROTECTED)
            .build();

    // Layer 3: Template (override some compiler args)
    BuilderConfiguration template =
        BuilderConfiguration.builder().generateSupplier(OptionState.ENABLED).build();

    // Layer 4: Direct options (highest priority)
    BuilderConfiguration options =
        BuilderConfiguration.builder().generateConsumer(OptionState.DISABLED).build();

    // Apply chain: defaults -> compiler -> template -> options
    BuilderConfiguration finalConfig = defaults.merge(compilerArgs).merge(template).merge(options);

    // Verify final values
    assertEquals(
        OptionState.ENABLED,
        finalConfig.generateFieldSupplier(),
        "Template should override compiler args");
    assertEquals(
        OptionState.DISABLED,
        finalConfig.generateFieldConsumer(),
        "Options should override all others");
    assertEquals(
        AccessModifier.PROTECTED,
        finalConfig.getBuilderAccess(),
        "Compiler args should override defaults");
    assertEquals(
        OptionState.ENABLED,
        finalConfig.generateConditionalHelper(),
        "Default should remain when not overridden");
  }
}
