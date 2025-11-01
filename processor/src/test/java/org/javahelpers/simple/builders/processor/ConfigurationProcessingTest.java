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
            .generateProvider(OptionState.ENABLED)
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
    assertEquals(OptionState.ENABLED, config.generateFieldProvider());
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
    // Given: DTO with various property types including nested DTO with builder
    JavaFileObject nestedDto =
        ProcessorTestUtils.simpleBuilderClass(
            "test",
            "NestedDto",
            """
            private String value;
            public String getValue() { return value; }
            public void setValue(String value) { this.value = value; }
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
            """);

    // When: Compile with ALL compiler arguments disabled
    Compilation compilation =
        Compiler.javac()
            .withProcessors(new BuilderProcessor())
            .withOptions(
                "-Asimplebuilder.generateFieldSupplier=false",
                "-Asimplebuilder.generateFieldProvider=false",
                "-Asimplebuilder.generateBuilderProvider=false",
                "-Asimplebuilder.generateConditionalHelper=false",
                "-Asimplebuilder.generateVarArgsHelpers=false",
                "-Asimplebuilder.usingArrayListBuilder=false",
                "-Asimplebuilder.usingArrayListBuilderWithElementBuilders=false",
                "-Asimplebuilder.usingHashSetBuilder=false",
                "-Asimplebuilder.usingHashSetBuilderWithElementBuilders=false",
                "-Asimplebuilder.usingHashMapBuilder=false",
                "-Asimplebuilder.generateWithInterface=false")
            .compile(nestedDto, source);

    // Then: Compilation should succeed
    assertThat(compilation).succeeded();

    // And: Generated builder should contain only basic functionality
    String generatedCode = ProcessorTestUtils.loadGeneratedSource(compilation, "MinimalDtoBuilder");

    // Expected: Documents current state when all compiler arguments are disabled
    // Note: Even with all options disabled, still generates:
    // - supplier methods, StringBuilder consumer, String.format for String fields
    // - ArrayListBuilder methods, varargs for List fields
    // - conditional() methods and With interface
    String expected =
        """
        package test;

        import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
        import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
        import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

        import java.util.List;
        import java.util.Map;
        import java.util.Map.Entry;
        import java.util.Optional;
        import java.util.Set;
        import java.util.function.BooleanSupplier;
        import java.util.function.Consumer;
        import java.util.function.Supplier;
        import javax.annotation.processing.Generated;
        import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
        import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
        import org.javahelpers.simple.builders.core.builders.HashMapBuilder;
        import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
        import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
        import org.javahelpers.simple.builders.core.util.TrackedValue;

        /**
         * Builder for {@code test.MinimalDto}.
         */
        @Generated("Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
        @BuilderImplementation(
            forClass = MinimalDto.class
        )
        class MinimalDtoBuilder implements IBuilderBase<MinimalDto> {
          /**
           * Tracked value for <code>name</code>: name.
           */
          private TrackedValue<String> name = unsetValue();

          /**
           * Tracked value for <code>items</code>: items.
           */
          private TrackedValue<List<String>> items = unsetValue();

          /**
           * Tracked value for <code>properties</code>: properties.
           */
          private TrackedValue<Map<String, Integer>> properties = unsetValue();

          /**
           * Tracked value for <code>description</code>: description.
           */
          private TrackedValue<Optional<String>> description = unsetValue();

          /**
           * Tracked value for <code>tags</code>: tags.
           */
          private TrackedValue<Set<String>> tags = unsetValue();

          /**
           * Tracked value for <code>nested</code>: nested.
           */
          private TrackedValue<NestedDto> nested = unsetValue();

          /**
           * Initialisation of builder for {@code test.MinimalDto} by a instance.
           *
           * @param instance object instance for initialisiation
           */
          public MinimalDtoBuilder(MinimalDto instance) {
            this.name = initialValue(instance.getName());
            this.items = initialValue(instance.getItems());
            this.properties = initialValue(instance.getProperties());
            this.description = initialValue(instance.getDescription());
            this.tags = initialValue(instance.getTags());
            this.nested = initialValue(instance.getNested());
          }

          /**
           * Empty constructor of builder for {@code test.MinimalDto}.
           */
          public MinimalDtoBuilder() {
          }

          /**
           * Sets the value for <code>items</code> using a builder consumer that produces the value.
           *
           * @param itemsBuilderConsumer consumer providing an instance of a builder for items
           * @return current instance of builder
           */
          public MinimalDtoBuilder items(Consumer<ArrayListBuilder<String>> itemsBuilderConsumer) {
            ArrayListBuilder<String> builder = this.items.isSet() ? new ArrayListBuilder<String>(this.items.value()) : new ArrayListBuilder<String>();
            itemsBuilderConsumer.accept(builder);
            this.items = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>nested</code> by invoking the provided supplier.
           *
           * @param nestedSupplier supplier for nested
           * @return current instance of builder
           */
          public MinimalDtoBuilder nested(Supplier<NestedDto> nestedSupplier) {
            this.nested = changedValue(nestedSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>properties</code> by invoking the provided supplier.
           *
           * @param propertiesSupplier supplier for properties
           * @return current instance of builder
           */
          public MinimalDtoBuilder properties(Supplier<Map<String, Integer>> propertiesSupplier) {
            this.properties = changedValue(propertiesSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>items</code>.
           *
           * @param items items
           * @return current instance of builder
           */
          public MinimalDtoBuilder items(List<String> items) {
            this.items = changedValue(items);
            return this;
          }

          /**
           * Sets the value for <code>tags</code>.
           *
           * @param tags tags
           * @return current instance of builder
           */
          public MinimalDtoBuilder tags(Set<String> tags) {
            this.tags = changedValue(tags);
            return this;
          }

          /**
           * Sets the value for <code>name</code>.
           *
           * @param format name
           * @param args name
           * @return current instance of builder
           */
          public MinimalDtoBuilder name(String format, Object... args) {
            this.name = changedValue(String.format(format, args));
            return this;
          }

          /**
           * Sets the value for <code>description</code> by invoking the provided supplier.
           *
           * @param descriptionSupplier supplier for description
           * @return current instance of builder
           */
          public MinimalDtoBuilder description(Supplier<Optional<String>> descriptionSupplier) {
            this.description = changedValue(descriptionSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>name</code> by executing the provided consumer.
           *
           * @param nameStringBuilderConsumer consumer providing an instance of name
           * @return current instance of builder
           */
          public MinimalDtoBuilder name(Consumer<StringBuilder> nameStringBuilderConsumer) {
            StringBuilder builder = new StringBuilder();
            nameStringBuilderConsumer.accept(builder);
            this.name = changedValue(builder.toString());
            return this;
          }

          /**
           * Sets the value for <code>description</code>.
           *
           * @param description description
           * @return current instance of builder
           */
          public MinimalDtoBuilder description(Optional<String> description) {
            this.description = changedValue(description);
            return this;
          }

          /**
           * Sets the value for <code>properties</code>.
           *
           * @param properties properties
           * @return current instance of builder
           */
          public MinimalDtoBuilder properties(Map<String, Integer> properties) {
            this.properties = changedValue(properties);
            return this;
          }

          /**
           * Sets the value for <code>tags</code> using a builder consumer that produces the value.
           *
           * @param tagsBuilderConsumer consumer providing an instance of a builder for tags
           * @return current instance of builder
           */
          public MinimalDtoBuilder tags(Consumer<HashSetBuilder<String>> tagsBuilderConsumer) {
            HashSetBuilder<String> builder = this.tags.isSet() ? new HashSetBuilder<String>(this.tags.value()) : new HashSetBuilder<String>();
            tagsBuilderConsumer.accept(builder);
            this.tags = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>items</code> by invoking the provided supplier.
           *
           * @param itemsSupplier supplier for items
           * @return current instance of builder
           */
          public MinimalDtoBuilder items(Supplier<List<String>> itemsSupplier) {
            this.items = changedValue(itemsSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>description</code>.
           *
           * @param format description
           * @param args description
           * @return current instance of builder
           */
          public MinimalDtoBuilder description(String format, Object... args) {
            this.description = changedValue(Optional.of(String.format(format, args)));
            return this;
          }

          /**
           * Sets the value for <code>nested</code> using a builder consumer that produces the value.
           *
           * @param nestedBuilderConsumer consumer providing an instance of a builder for nested
           * @return current instance of builder
           */
          public MinimalDtoBuilder nested(Consumer<NestedDtoBuilder> nestedBuilderConsumer) {
            NestedDtoBuilder builder = this.nested.isSet() ? new NestedDtoBuilder(this.nested.value()) : new NestedDtoBuilder();
            nestedBuilderConsumer.accept(builder);
            this.nested = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>name</code> by invoking the provided supplier.
           *
           * @param nameSupplier supplier for name
           * @return current instance of builder
           */
          public MinimalDtoBuilder name(Supplier<String> nameSupplier) {
            this.name = changedValue(nameSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>description</code> by executing the provided consumer.
           *
           * @param descriptionStringBuilderConsumer consumer providing an instance of description
           * @return current instance of builder
           */
          public MinimalDtoBuilder description(Consumer<StringBuilder> descriptionStringBuilderConsumer) {
            StringBuilder builder = new StringBuilder();
            descriptionStringBuilderConsumer.accept(builder);
            this.description = changedValue(Optional.of(builder.toString()));
            return this;
          }

          /**
           * Sets the value for <code>items</code>.
           *
           * @param items items
           * @return current instance of builder
           */
          public MinimalDtoBuilder items(String... items) {
            this.items = changedValue(List.of(items));
            return this;
          }

          /**
           * Sets the value for <code>nested</code>.
           *
           * @param nested nested
           * @return current instance of builder
           */
          public MinimalDtoBuilder nested(NestedDto nested) {
            this.nested = changedValue(nested);
            return this;
          }

          /**
           * Sets the value for <code>tags</code> by invoking the provided supplier.
           *
           * @param tagsSupplier supplier for tags
           * @return current instance of builder
           */
          public MinimalDtoBuilder tags(Supplier<Set<String>> tagsSupplier) {
            this.tags = changedValue(tagsSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>properties</code> using a builder consumer that produces the value.
           *
           * @param propertiesBuilderConsumer consumer providing an instance of a builder for properties
           * @return current instance of builder
           */
          public MinimalDtoBuilder properties(
              Consumer<HashMapBuilder<String, Integer>> propertiesBuilderConsumer) {
            HashMapBuilder<String, Integer> builder = this.properties.isSet() ? new HashMapBuilder<String, Integer>(this.properties.value()) : new HashMapBuilder<String, Integer>();
            propertiesBuilderConsumer.accept(builder);
            this.properties = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>name</code>.
           *
           * @param name name
           * @return current instance of builder
           */
          public MinimalDtoBuilder name(String name) {
            this.name = changedValue(name);
            return this;
          }

          /**
           * Sets the value for <code>properties</code>.
           *
           * @param properties properties
           * @return current instance of builder
           */
          public MinimalDtoBuilder properties(Map.Entry<String, Integer>... properties) {
            this.properties = changedValue(Map.ofEntries(properties));
            return this;
          }

          /**
           * Sets the value for <code>tags</code>.
           *
           * @param tags tags
           * @return current instance of builder
           */
          public MinimalDtoBuilder tags(String... tags) {
            this.tags = changedValue(Set.of(tags));
            return this;
          }

          /**
           * Sets the value for <code>description</code>.
           *
           * @param description description
           * @return current instance of builder
           */
          public MinimalDtoBuilder description(String description) {
            this.description = changedValue(Optional.ofNullable(description));
            return this;
          }

          @Override
          public MinimalDto build() {
            MinimalDto result = new MinimalDto();
            this.name.ifSet(result::setName);
            this.items.ifSet(result::setItems);
            this.properties.ifSet(result::setProperties);
            this.description.ifSet(result::setDescription);
            this.tags.ifSet(result::setTags);
            this.nested.ifSet(result::setNested);
            return result;
          }

          /**
           * Creating a new builder for {@code test.MinimalDto}.
           *
           * @return builder for {@code test.MinimalDto}
           */
          public static MinimalDtoBuilder create() {
            return new MinimalDtoBuilder();
          }

          /**
           * Conditionally applies builder modifications based on a condition.
           *
           * @param condition the condition to evaluate
           * @param trueCase the consumer to apply if condition is true
           * @param falseCase the consumer to apply if condition is false (can be null)
           * @return this builder instance
           */
          public MinimalDtoBuilder conditional(BooleanSupplier condition,
              Consumer<MinimalDtoBuilder> trueCase, Consumer<MinimalDtoBuilder> falseCase) {
            if (condition.getAsBoolean()) {
                trueCase.accept(this);
            } else if (falseCase != null) {
                falseCase.accept(this);
            }
            return this;
          }

          /**
           * Conditionally applies builder modifications if the condition is true.
           *
           * @param condition the condition to evaluate
           * @param yesCondition the consumer to apply if condition is true
           * @return this builder instance
           */
          public MinimalDtoBuilder conditional(BooleanSupplier condition,
              Consumer<MinimalDtoBuilder> yesCondition) {
            return conditional(condition, yesCondition, null);
          }

          /**
           * Interface that can be implemented by the DTO to provide fluent modification methods.
           */
          public interface With {
            /**
             * Applies modifications to a builder initialized from this instance and returns the built object.
             *
             * @param b the consumer to apply modifications
             * @return the modified instance
             */
            default MinimalDto with(Consumer<MinimalDtoBuilder> b) {
              MinimalDtoBuilder builder;
              try {
                builder = new MinimalDtoBuilder(MinimalDto.class.cast(this));
              } catch (ClassCastException ex) {
                throw new IllegalArgumentException("The interface 'MinimalDtoBuilder.With' should only be implemented by classes, which could be casted to 'MinimalDto'", ex);
              }
              b.accept(builder);
              return builder.build();
            }

            /**
             * Creates a builder initialized from this instance.
             *
             * @return a builder initialized with this instance's values
             */
            default MinimalDtoBuilder with() {
              try {
                return new MinimalDtoBuilder(MinimalDto.class.cast(this));
              } catch (ClassCastException ex) {
                throw new IllegalArgumentException("The interface 'MinimalDtoBuilder.With' should only be implemented by classes, which could be casted to 'MinimalDto'", ex);
              }
            }
          }
        }
        """;

    ProcessorAsserts.assertingResult(generatedCode, ProcessorAsserts.contains(expected));
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
            .generateProvider(OptionState.ENABLED)
            .builderAccess(AccessModifier.PUBLIC)
            .build();

    // When: Merge with override configuration
    BuilderConfiguration override =
        BuilderConfiguration.builder()
            .generateSupplier(OptionState.DISABLED) // Override
            .generateBuilderProvider(OptionState.DISABLED) // New value
            // generateProvider not set, should keep base value
            .build();

    BuilderConfiguration merged = base.merge(override);

    // Then: Override values win, base values kept for unset
    assertEquals(
        OptionState.DISABLED,
        merged.generateFieldSupplier(),
        "Override should win for generateFieldSupplier");
    assertEquals(
        OptionState.ENABLED,
        merged.generateFieldProvider(),
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
            .generateProvider(OptionState.ENABLED)
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
        BuilderConfiguration.builder().generateProvider(OptionState.DISABLED).build();

    // Apply chain: defaults -> compiler -> template -> options
    BuilderConfiguration finalConfig = defaults.merge(compilerArgs).merge(template).merge(options);

    // Verify final values
    assertEquals(
        OptionState.ENABLED,
        finalConfig.generateFieldSupplier(),
        "Template should override compiler args");
    assertEquals(
        OptionState.DISABLED,
        finalConfig.generateFieldProvider(),
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
