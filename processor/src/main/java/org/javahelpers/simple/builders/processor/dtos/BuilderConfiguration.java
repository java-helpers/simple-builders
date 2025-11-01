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

package org.javahelpers.simple.builders.processor.dtos;

import static org.javahelpers.simple.builders.core.enums.AccessModifier.*;
import static org.javahelpers.simple.builders.core.enums.OptionState.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;

/**
 * Configuration for builder generation. Combines annotation values with compiler options. Priority:
 * Annotation > Compiler Options > Defaults
 *
 * <p>Properties and defaults are read from {@link SimpleBuilder.Options} annotation defaults via
 * reflection.
 *
 * @param generateFieldSupplier Generate field supplier methods
 * @param generateFieldProvider Generate field provider methods
 * @param generateBuilderProvider Generate builder provider methods
 * @param generateConditionalHelper Generate conditional logic methods
 * @param builderAccess Access level for builder class
 * @param methodAccess Access level for builder methods
 * @param generateVarArgsHelpers Generate varargs helper methods
 * @param usingArrayListBuilder Use ArrayListBuilder for lists
 * @param usingArrayListBuilderWithElementBuilders Use ArrayListBuilderWithElementBuilders
 * @param usingHashSetBuilder Use HashSetBuilder for sets
 * @param usingHashSetBuilderWithElementBuilders Use HashSetBuilderWithElementBuilders
 * @param usingHashMapBuilder Use HashMapBuilder for maps
 * @param generateWithInterface Generate With interface
 * @param hasAnnotationOverride Whether annotation was used (vs just compiler options)
 */
public record BuilderConfiguration(
    OptionState generateFieldSupplier,
    OptionState generateFieldProvider,
    OptionState generateBuilderProvider,
    OptionState generateConditionalHelper,
    AccessModifier builderAccess,
    AccessModifier methodAccess,
    OptionState generateVarArgsHelpers,
    OptionState usingArrayListBuilder,
    OptionState usingArrayListBuilderWithElementBuilders,
    OptionState usingHashSetBuilder,
    OptionState usingHashSetBuilderWithElementBuilders,
    OptionState usingHashMapBuilder,
    OptionState generateWithInterface,
    OptionState hasAnnotationOverride) {

  public static final BuilderConfiguration DEFAULT =
      builder()
          .generateSupplier(ENABLED)
          .generateProvider(ENABLED)
          .generateBuilderProvider(ENABLED)
          .generateConditionalLogic(ENABLED)
          .builderAccess(PUBLIC)
          .methodAccess(PUBLIC)
          .generateVarArgsHelpers(ENABLED)
          .usingArrayListBuilder(ENABLED)
          .usingArrayListBuilderWithElementBuilders(ENABLED)
          .usingHashSetBuilder(ENABLED)
          .usingHashSetBuilderWithElementBuilders(ENABLED)
          .usingHashMapBuilder(ENABLED)
          .generateWithInterface(ENABLED)
          .hasAnnotationOverride(ENABLED)
          .build();

  // === Convenience accessors with 'is' prefix for boolean properties ===
  public boolean isGenerateSupplier() {
    return generateFieldSupplier == ENABLED;
  }

  public boolean isGenerateProvider() {
    return generateFieldProvider == ENABLED;
  }

  public boolean isGenerateBuilderProvider() {
    return generateBuilderProvider == ENABLED;
  }

  public boolean isGenerateConditionalLogic() {
    return generateConditionalHelper == ENABLED;
  }

  public boolean isGenerateWithInterface() {
    return generateWithInterface == ENABLED;
  }

  public boolean isGenerateVarArgsHelpers() {
    return generateVarArgsHelpers == ENABLED;
  }

  public boolean isUsingArrayListBuilder() {
    return usingArrayListBuilder == ENABLED;
  }

  public boolean isUsingArrayListBuilderWithElementBuilders() {
    return usingArrayListBuilderWithElementBuilders == ENABLED;
  }

  public boolean isUsingHashSetBuilder() {
    return usingHashSetBuilder == ENABLED;
  }

  public boolean isUsingHashSetBuilderWithElementBuilders() {
    return usingHashSetBuilderWithElementBuilders == ENABLED;
  }

  public boolean isUsingHashMapBuilder() {
    return usingHashMapBuilder == ENABLED;
  }

  // === String accessors ===
  public AccessModifier getBuilderAccess() {
    return builderAccess;
  }

  public AccessModifier getMethodAccess() {
    return methodAccess;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);

    if (generateFieldSupplier != UNSET) {
      builder.append("generateFieldSupplier", generateFieldSupplier);
    }
    if (generateFieldProvider != UNSET) {
      builder.append("generateFieldProvider", generateFieldProvider);
    }
    if (generateBuilderProvider != UNSET) {
      builder.append("generateBuilderProvider", generateBuilderProvider);
    }
    if (generateConditionalHelper != UNSET) {
      builder.append("generateConditionalHelper", generateConditionalHelper);
    }
    if (builderAccess != AccessModifier.DEFAULT) {
      builder.append("builderAccess", builderAccess);
    }
    if (methodAccess != AccessModifier.DEFAULT) {
      builder.append("methodAccess", methodAccess);
    }
    if (generateVarArgsHelpers != UNSET) {
      builder.append("generateVarArgsHelpers", generateVarArgsHelpers);
    }
    if (usingArrayListBuilder != UNSET) {
      builder.append("usingArrayListBuilder", usingArrayListBuilder);
    }
    if (usingArrayListBuilderWithElementBuilders != UNSET) {
      builder.append(
          "usingArrayListBuilderWithElementBuilders", usingArrayListBuilderWithElementBuilders);
    }
    if (usingHashSetBuilder != UNSET) {
      builder.append("usingHashSetBuilder", usingHashSetBuilder);
    }
    if (usingHashSetBuilderWithElementBuilders != UNSET) {
      builder.append(
          "usingHashSetBuilderWithElementBuilders", usingHashSetBuilderWithElementBuilders);
    }
    if (usingHashMapBuilder != UNSET) {
      builder.append("usingHashMapBuilder", usingHashMapBuilder);
    }
    if (generateWithInterface != UNSET) {
      builder.append("generateWithInterface", generateWithInterface);
    }
    if (hasAnnotationOverride != UNSET) {
      builder.append("hasAnnotationOverride", hasAnnotationOverride);
    }

    return builder.toString();
  }

  // === Builder Pattern ===
  // All defaults are DEFAULT to allow proper three-state resolution
  public static class Builder {
    // === Field Setter Generation ===
    private OptionState generateFieldSupplier = OptionState.UNSET;
    private OptionState generateFieldProvider = OptionState.UNSET;
    private OptionState generateBuilderProvider = OptionState.UNSET;

    // === Conditional Logic ===
    private OptionState generateConditionalHelper = OptionState.UNSET;

    // === Access Control ===
    private AccessModifier builderAccess = AccessModifier.DEFAULT;
    private AccessModifier methodAccess = AccessModifier.DEFAULT;

    // === Collection Options ===
    private OptionState generateVarArgsHelpers = OptionState.UNSET;
    private OptionState usingArrayListBuilder = OptionState.UNSET;
    private OptionState usingArrayListBuilderWithElementBuilders = OptionState.UNSET;
    private OptionState usingHashSetBuilder = OptionState.UNSET;
    private OptionState usingHashSetBuilderWithElementBuilders = OptionState.UNSET;
    private OptionState usingHashMapBuilder = OptionState.UNSET;

    // === Integration ===
    private OptionState generateWithInterface = OptionState.UNSET;

    // === Source Information ===
    private OptionState hasAnnotationOverride = OptionState.UNSET;

    // === Setters ===
    public Builder generateSupplier(OptionState value) {
      this.generateFieldSupplier = value;
      return this;
    }

    public Builder generateSupplier(boolean value) {
      this.generateFieldSupplier = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder generateProvider(OptionState value) {
      this.generateFieldProvider = value;
      return this;
    }

    public Builder generateProvider(boolean value) {
      this.generateFieldProvider = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder generateBuilderProvider(OptionState value) {
      this.generateBuilderProvider = value;
      return this;
    }

    public Builder generateBuilderProvider(boolean value) {
      this.generateBuilderProvider = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder generateConditionalLogic(OptionState value) {
      this.generateConditionalHelper = value;
      return this;
    }

    public Builder generateConditionalLogic(boolean value) {
      this.generateConditionalHelper = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder generateWithInterface(OptionState value) {
      this.generateWithInterface = value;
      return this;
    }

    public Builder generateWithInterface(boolean value) {
      this.generateWithInterface = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder generateVarArgsHelpers(OptionState value) {
      this.generateVarArgsHelpers = value;
      return this;
    }

    public Builder generateVarArgsHelpers(boolean value) {
      this.generateVarArgsHelpers = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder usingArrayListBuilder(OptionState value) {
      this.usingArrayListBuilder = value;
      return this;
    }

    public Builder usingArrayListBuilder(boolean value) {
      this.usingArrayListBuilder = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder usingArrayListBuilderWithElementBuilders(OptionState value) {
      this.usingArrayListBuilderWithElementBuilders = value;
      return this;
    }

    public Builder usingArrayListBuilderWithElementBuilders(boolean value) {
      this.usingArrayListBuilderWithElementBuilders = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder usingHashSetBuilder(OptionState value) {
      this.usingHashSetBuilder = value;
      return this;
    }

    public Builder usingHashSetBuilder(boolean value) {
      this.usingHashSetBuilder = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder usingHashSetBuilderWithElementBuilders(OptionState value) {
      this.usingHashSetBuilderWithElementBuilders = value;
      return this;
    }

    public Builder usingHashSetBuilderWithElementBuilders(boolean value) {
      this.usingHashSetBuilderWithElementBuilders = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder usingHashMapBuilder(OptionState value) {
      this.usingHashMapBuilder = value;
      return this;
    }

    public Builder usingHashMapBuilder(boolean value) {
      this.usingHashMapBuilder = value ? ENABLED : DISABLED;
      return this;
    }

    public Builder builderAccess(AccessModifier value) {
      this.builderAccess = value;
      return this;
    }

    public Builder builderAccess(String value) {
      this.builderAccess = AccessModifier.valueOf(value.toUpperCase());
      return this;
    }

    public Builder methodAccess(AccessModifier value) {
      this.methodAccess = value;
      return this;
    }

    public Builder methodAccess(String value) {
      this.methodAccess = AccessModifier.valueOf(value.toUpperCase());
      return this;
    }

    public Builder hasAnnotationOverride(OptionState value) {
      this.hasAnnotationOverride = value;
      return this;
    }

    public Builder hasAnnotationOverride(boolean value) {
      this.hasAnnotationOverride = value ? ENABLED : DISABLED;
      return this;
    }

    public BuilderConfiguration build() {
      return new BuilderConfiguration(
          generateFieldSupplier,
          generateFieldProvider,
          generateBuilderProvider,
          generateConditionalHelper,
          builderAccess,
          methodAccess,
          generateVarArgsHelpers,
          usingArrayListBuilder,
          usingArrayListBuilderWithElementBuilders,
          usingHashSetBuilder,
          usingHashSetBuilderWithElementBuilders,
          usingHashMapBuilder,
          generateWithInterface,
          hasAnnotationOverride);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
