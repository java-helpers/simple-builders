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

package org.javahelpers.simple.builders.processor.processing;

import java.util.function.BiConsumer;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration.Builder;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Enumeration of all builder configuration compiler arguments.
 *
 * <p>This enum provides a single source of truth for all option names used in:
 *
 * <ul>
 *   <li>Annotation methods in {@code SimpleBuilder.Options}
 *   <li>Compiler options (with {@code -A} prefix)
 *   <li>Configuration resolution
 * </ul>
 *
 * <p>Each enum constant provides the option name and the full compiler argument.
 */
public enum CompilerArgumentsEnum {
  // === Field Setter Generation ===
  /** Option for field supplier generation. */
  GENERATE_FIELD_SUPPLIER("generateFieldSupplier", optionState(Builder::generateSupplier)),

  /** Option for field consumer generation. */
  GENERATE_FIELD_CONSUMER("generateFieldConsumer", optionState(Builder::generateConsumer)),

  /** Option for builder consumer generation. */
  GENERATE_BUILDER_CONSUMER(
      "generateBuilderConsumer", optionState(Builder::generateBuilderConsumer)),

  // === Conditional Logic ===
  /** Option for conditional helper generation. */
  GENERATE_CONDITIONAL_HELPER(
      "generateConditionalHelper", optionState(Builder::generateConditionalLogic)),

  // === Access Control ===
  /** Option for builder access level. */
  BUILDER_ACCESS("builderAccess", accessModifier(Builder::builderAccess)),

  /** Option for builder constructor access level. */
  BUILDER_CONSTRUCTOR_ACCESS(
      "builderConstructorAccess", accessModifier(Builder::builderConstructorAccess)),

  /** Option for method access level. */
  METHOD_ACCESS("methodAccess", accessModifier(Builder::methodAccess)),

  // === Collection Options ===
  /** Option for varargs helper generation. */
  GENERATE_VAR_ARGS_HELPERS("generateVarArgsHelpers", optionState(Builder::generateVarArgsHelpers)),

  /** Option for string format helper generation. */
  GENERATE_STRING_FORMAT_HELPERS(
      "generateStringFormatHelpers", optionState(Builder::generateStringFormatHelpers)),

  /** Option for update helper generation. */
  GENERATE_UPDATE_HELPERS("generateUpdateHelpers", optionState(Builder::generateUpdateHelpers)),

  /** Option for add to collection helper generation. */
  GENERATE_ADD_TO_COLLECTION_HELPERS(
      "generateAddToCollectionHelpers", optionState(Builder::generateAddToCollectionHelpers)),

  /** Option for unboxed optional generation. */
  GENERATE_UNBOXED_OPTIONAL(
      "generateUnboxedOptional", optionState(Builder::generateUnboxedOptional)),

  /** Option for copying type annotations. */
  COPY_TYPE_ANNOTATIONS("copyTypeAnnotations", optionState(Builder::copyTypeAnnotations)),

  /** Option for ArrayList builder usage. */
  USING_ARRAY_LIST_BUILDER("usingArrayListBuilder", optionState(Builder::usingArrayListBuilder)),

  /** Option for ArrayList builder with element builders usage. */
  USING_ARRAY_LIST_BUILDER_WITH_ELEMENT_BUILDERS(
      "usingArrayListBuilderWithElementBuilders",
      optionState(Builder::usingArrayListBuilderWithElementBuilders)),

  /** Option for HashSet builder usage. */
  USING_HASH_SET_BUILDER("usingHashSetBuilder", optionState(Builder::usingHashSetBuilder)),

  /** Option for HashSet builder with element builders usage. */
  USING_HASH_SET_BUILDER_WITH_ELEMENT_BUILDERS(
      "usingHashSetBuilderWithElementBuilders",
      optionState(Builder::usingHashSetBuilderWithElementBuilders)),

  /** Option for HashMap builder usage. */
  USING_HASH_MAP_BUILDER("usingHashMapBuilder", optionState(Builder::usingHashMapBuilder)),

  // === Annotations ===
  /** Option for using Generated annotation. */
  USING_GENERATED_ANNOTATION(
      "usingGeneratedAnnotation", optionState(Builder::usingGeneratedAnnotation)),

  /** Option for using BuilderImplementation annotation. */
  USING_BUILDER_IMPLEMENTATION_ANNOTATION(
      "usingBuilderImplementationAnnotation",
      optionState(Builder::usingBuilderImplementationAnnotation)),

  // === Integration ===
  /** Option for implementing IBuilderBase interface. */
  IMPLEMENTS_BUILDER_BASE("implementsBuilderBase", optionState(Builder::implementsBuilderBase)),

  /** Option for With interface generation. */
  GENERATE_WITH_INTERFACE("generateWithInterface", optionState(Builder::generateWithInterface)),

  /** Option for Jackson support. */
  USING_JACKSON_DESERIALIZER_ANNOTATION(
      "usingJacksonDeserializerAnnotation",
      optionState(Builder::usingJacksonDeserializerAnnotation)),

  /** Option for Jackson Module generation. */
  GENERATE_JACKSON_MODULE("generateJacksonModule", optionState(Builder::generateJacksonModule)),

  /** Option for Javadoc generation on the generated builder. */
  GENERATE_JAVADOC("generateJavaDoc", optionState(Builder::generateJavaDoc)),

  /** Option for Jackson Module package name. */
  JACKSON_MODULE_PACKAGE("jacksonModulePackage", string(Builder::jacksonModulePackage)),

  // === Builder Scoping ===
  /** Option for builder generation packages. */
  BUILDER_GENERATION_PACKAGES(
      "builderGenerationPackages", string(Builder::builderGenerationPackages)),

  /** Option for builder usage packages. */
  BUILDER_USAGE_PACKAGES("builderUsagePackages", string(Builder::builderUsagePackages)),

  // === Naming ===
  /** Option for builder class name suffix. */
  BUILDER_SUFFIX("builderSuffix", string(Builder::builderSuffix)),

  /** Option for builder class name suffix when referencing usage-scope builders. */
  BUILDER_USAGE_SUFFIX("builderUsageSuffix", string(Builder::builderUsageSuffix)),

  /** Option for setter method name suffix. */
  SETTER_SUFFIX("setterSuffix", string(Builder::setterSuffix)),

  // === Component Filtering ===
  /**
   * Option for deactivating specific method generators and builder enhancers by class name pattern.
   */
  DEACTIVATE_GENERATION_COMPONENTS("deactivateGenerationComponents"),

  // === Debug Logging ===
  /** Option for verbose logging output. */
  VERBOSE("verbose"),

  // === Performance Optimization ===
  /**
   * Option to control the formatting mode for generated source files. Accepts values {@code jdt},
   * {@code lightweight}, or {@code none}. See {@link
   * org.javahelpers.simple.builders.core.enums.FormattingMode} for details.
   */
  FORMATTING_MODE("formattingMode", string(Builder::formattingMode)),

  /**
   * Option for an external Eclipse formatter profile XML (file system path or classpath resource).
   * Processor-level only: it is read directly from compiler arguments and cannot be set per
   * annotation.
   */
  FORMATTER_PROFILE("formatterProfile"),

  // === Performance Tracking ===
  /** Option for performance tracking during annotation processing. */
  PERFORMANCE_TRACKING("performanceTracking"),

  /** Option for performance report JSON output file path. */
  PERFORMANCE_OUTPUT_FILE("performanceOutputFile"),

  // === Error Handling ===
  /**
   * Option for strict/fail-fast generation mode. When enabled, builder (and Jackson module)
   * generation failures are reported as compiler errors that fail the build instead of warnings.
   * Defaults to disabled (warnings only, build does not fail).
   */
  STRICT("strict", optionState(Builder::strict));

  /** Compiler option prefix for all simple-builders options. */
  private static final String OPTION_PREFIX = "simplebuilder.";

  /** The option name (used in annotation methods). */
  private final String optionName;

  /**
   * Applies a raw option value to a {@link BuilderConfiguration.Builder}, or {@code null} for
   * arguments that are not builder configuration options (e.g. {@code verbose}).
   */
  private final OptionApplier builderApplier;

  /**
   * Constructs a CompilerArgumentsEnum constant for an argument that is not a builder configuration
   * option.
   *
   * <p>These are process-control or processor-level options (e.g. {@code VERBOSE}, {@code
   * PERFORMANCE_TRACKING}, {@code PERFORMANCE_OUTPUT_FILE}, {@code
   * DEACTIVATE_GENERATION_COMPONENTS}, {@code FORMATTER_PROFILE}) that are read directly via {@link
   * CompilerArgumentsReader#readValue} or {@link CompilerArgumentsReader#readBooleanValue} rather
   * than applied to a {@link BuilderConfiguration.Builder}. The {@code null} applier is
   * intentional: {@link #apply} is a no-op for these constants, and {@link #hasValueApplier()}
   * returns {@code false} so {@link BuilderConfigurationReader} skips them when parsing annotation
   * attributes.
   *
   * @param optionName The option name
   */
  CompilerArgumentsEnum(String optionName) {
    this(optionName, null);
  }

  /**
   * Constructs a CompilerArgumentsEnum constant for a builder configuration option.
   *
   * <p>The {@code builderApplier} converts the raw option value (from a compiler argument or
   * annotation attribute) into the corresponding {@link BuilderConfiguration.Builder} setter call.
   * {@link #hasValueApplier()} returns {@code true} for these constants.
   *
   * @param optionName The option name
   * @param builderApplier applies the raw option value to the configuration builder
   */
  CompilerArgumentsEnum(String optionName, OptionApplier builderApplier) {
    this.optionName = optionName;
    this.builderApplier = builderApplier;
  }

  /**
   * Gets the option name for use in annotation methods.
   *
   * <p>Example: {@code "generateFieldSupplier"}
   *
   * @return The option name
   */
  public String getOptionName() {
    return optionName;
  }

  /**
   * Gets the full compiler argument including the package prefix.
   *
   * <p>Example: {@code "simplebuilder.generateFieldSupplier"}
   *
   * @return The full compiler argument
   */
  public String getCompilerArgument() {
    return OPTION_PREFIX + optionName;
  }

  /**
   * Finds a CompilerArgumentsEnum by its option name (as used in annotation methods).
   *
   * @param optionName the option name to search for
   * @return the matching CompilerArgumentsEnum, or {@code null} if not found
   */
  public static CompilerArgumentsEnum fromOptionName(String optionName) {
    for (CompilerArgumentsEnum option : values()) {
      if (option.getOptionName().equals(optionName)) {
        return option;
      }
    }
    return null;
  }

  /**
   * Returns whether this enum constant has a value applier that maps the raw option value to a
   * {@link BuilderConfiguration.Builder} setter.
   *
   * <p>Not all enum constants have an applier. Process-control or processor-level options like
   * {@code VERBOSE}, {@code PERFORMANCE_TRACKING}, {@code PERFORMANCE_OUTPUT_FILE}, {@code
   * DEACTIVATE_GENERATION_COMPONENTS}, and {@code FORMATTER_PROFILE} are read directly via {@link
   * CompilerArgumentsReader#readValue} or {@link CompilerArgumentsReader#readBooleanValue} instead
   * of being applied to a builder configuration. For these, the single-argument constructor sets
   * the applier to {@code null}, {@link #apply} is a no-op, and this method returns {@code false}.
   * This lets {@link BuilderConfigurationReader} skip them when parsing annotation attributes.
   *
   * @return {@code true} if this option has a value applier for {@link
   *     BuilderConfiguration.Builder}
   */
  public boolean hasValueApplier() {
    return builderApplier != null;
  }

  /**
   * Applies a raw option value to the given configuration builder.
   *
   * <p>Enum-typed annotation attributes arrive qualified (e.g. {@code "...OptionState.ENABLED"})
   * and are unqualified before parsing; plain values (compiler arguments, string options) are used
   * as given.
   *
   * @param builder the configuration builder to modify
   * @param rawValue the raw option value (annotation value or compiler-argument string)
   * @param logger the logger for warnings on unrecognized values, or null to suppress
   */
  public void apply(
      BuilderConfiguration.Builder builder, Object rawValue, ProcessingLogger logger) {
    if (builderApplier != null) {
      builderApplier.apply(builder, rawValue, logger);
    }
  }

  /**
   * Extracts the simple enum constant name from a qualified annotation value (e.g. {@code
   * "...OptionState.ENABLED"} → {@code "ENABLED"}). Unqualified values are returned as given.
   *
   * @param value the raw annotation/argument value
   * @return the simple enum name, or null if the value is null
   */
  private static String extractEnumName(Object value) {
    if (value == null) {
      return null;
    }
    String enumString = value.toString();
    return enumString.contains(".")
        ? enumString.substring(enumString.lastIndexOf('.') + 1)
        : enumString;
  }

  private static OptionApplier optionState(
      BiConsumer<BuilderConfiguration.Builder, OptionState> setter) {
    return (builder, value, logger) ->
        setter.accept(builder, OptionValueParsers.parseOptionState(extractEnumName(value), logger));
  }

  private static OptionApplier accessModifier(
      BiConsumer<BuilderConfiguration.Builder, AccessModifier> setter) {
    return (builder, value, logger) ->
        setter.accept(
            builder, OptionValueParsers.parseAccessModifier(extractEnumName(value), logger));
  }

  private static OptionApplier string(BiConsumer<BuilderConfiguration.Builder, String> setter) {
    return (builder, value, logger) ->
        setter.accept(builder, value == null ? null : value.toString());
  }

  /** Functional interface for applying a raw option value with optional logging. */
  @FunctionalInterface
  interface OptionApplier {
    void apply(BuilderConfiguration.Builder builder, Object rawValue, ProcessingLogger logger);
  }
}
