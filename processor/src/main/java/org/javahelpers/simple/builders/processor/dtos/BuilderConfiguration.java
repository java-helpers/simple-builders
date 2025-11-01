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

import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.processor.util.AnnotationDefaultReader;

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
    boolean generateFieldSupplier,
    boolean generateFieldProvider,
    boolean generateBuilderProvider,
    boolean generateConditionalHelper,
    String builderAccess,
    String methodAccess,
    boolean generateVarArgsHelpers,
    boolean usingArrayListBuilder,
    boolean usingArrayListBuilderWithElementBuilders,
    boolean usingHashSetBuilder,
    boolean usingHashSetBuilderWithElementBuilders,
    boolean usingHashMapBuilder,
    boolean generateWithInterface,
    boolean hasAnnotationOverride) {

  // === Convenience accessors with 'is' prefix for boolean properties ===
  public boolean isGenerateSupplier() {
    return generateFieldSupplier;
  }

  public boolean isGenerateProvider() {
    return generateFieldProvider;
  }

  public boolean isGenerateBuilderProvider() {
    return generateBuilderProvider;
  }

  public boolean isGenerateConditionalLogic() {
    return generateConditionalHelper;
  }

  public boolean isGenerateWithInterface() {
    return generateWithInterface;
  }

  public boolean isGenerateVarArgsHelpers() {
    return generateVarArgsHelpers;
  }

  public boolean isUsingArrayListBuilder() {
    return usingArrayListBuilder;
  }

  public boolean isUsingArrayListBuilderWithElementBuilders() {
    return usingArrayListBuilderWithElementBuilders;
  }

  public boolean isUsingHashSetBuilder() {
    return usingHashSetBuilder;
  }

  public boolean isUsingHashSetBuilderWithElementBuilders() {
    return usingHashSetBuilderWithElementBuilders;
  }

  public boolean isUsingHashMapBuilder() {
    return usingHashMapBuilder;
  }

  // === String accessors ===
  public String getBuilderAccess() {
    return builderAccess;
  }

  public String getMethodAccess() {
    return methodAccess;
  }

  // === Builder Pattern ===
  // Default values are read from SimpleBuilder.Options annotation via reflection
  public static class Builder {
    // === Field Setter Generation (defaults from SimpleBuilder.Options) ===
    private boolean generateFieldSupplier =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "generateFieldSupplier", true);
    private boolean generateFieldProvider =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "generateFieldProvider", true);
    private boolean generateBuilderProvider =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "generateBuilderProvider", true);

    // === Conditional Logic ===
    private boolean generateConditionalHelper =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "generateConditionalHelper", true);

    // === Access Control ===
    private String builderAccess =
        AnnotationDefaultReader.getEnumDefaultAsString(
            SimpleBuilder.Options.class, "builderAccess", "PUBLIC");
    private String methodAccess =
        AnnotationDefaultReader.getEnumDefaultAsString(
            SimpleBuilder.Options.class, "methodAccess", "PUBLIC");

    // === Collection Options ===
    private boolean generateVarArgsHelpers =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "generateVarArgsHelpers", true);
    private boolean usingArrayListBuilder =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "usingArrayListBuilder", true);
    private boolean usingArrayListBuilderWithElementBuilders =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "usingArrayListBuilderWithElementBuilders", true);
    private boolean usingHashSetBuilder =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "usingHashSetBuilder", true);
    private boolean usingHashSetBuilderWithElementBuilders =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "usingHashSetBuilderWithElementBuilders", true);
    private boolean usingHashMapBuilder =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "usingHashMapBuilder", true);

    // === Integration ===
    private boolean generateWithInterface =
        AnnotationDefaultReader.getBooleanDefault(
            SimpleBuilder.Options.class, "generateWithInterface", true);

    // === Source Information ===
    private boolean hasAnnotationOverride = false;

    // === Setters ===
    public Builder generateSupplier(boolean value) {
      this.generateFieldSupplier = value;
      return this;
    }

    public Builder generateProvider(boolean value) {
      this.generateFieldProvider = value;
      return this;
    }

    public Builder generateBuilderProvider(boolean value) {
      this.generateBuilderProvider = value;
      return this;
    }

    public Builder generateConditionalLogic(boolean value) {
      this.generateConditionalHelper = value;
      return this;
    }

    public Builder generateWithInterface(boolean value) {
      this.generateWithInterface = value;
      return this;
    }

    public Builder generateVarArgsHelpers(boolean value) {
      this.generateVarArgsHelpers = value;
      return this;
    }

    public Builder usingUtilBuilderForGenerate(boolean value) {
      this.usingArrayListBuilder = value;
      return this;
    }

    public Builder usingArrayListBuilder(boolean value) {
      this.usingArrayListBuilder = value;
      return this;
    }

    public Builder usingArrayListBuilderWithElementBuilders(boolean value) {
      this.usingArrayListBuilderWithElementBuilders = value;
      return this;
    }

    public Builder usingHashSetBuilder(boolean value) {
      this.usingHashSetBuilder = value;
      return this;
    }

    public Builder usingHashSetBuilderWithElementBuilders(boolean value) {
      this.usingHashSetBuilderWithElementBuilders = value;
      return this;
    }

    public Builder usingHashMapBuilder(boolean value) {
      this.usingHashMapBuilder = value;
      return this;
    }

    public Builder builderAccess(String value) {
      this.builderAccess = value;
      return this;
    }

    public Builder methodAccess(String value) {
      this.methodAccess = value;
      return this;
    }

    public Builder hasAnnotationOverride(boolean value) {
      this.hasAnnotationOverride = value;
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
