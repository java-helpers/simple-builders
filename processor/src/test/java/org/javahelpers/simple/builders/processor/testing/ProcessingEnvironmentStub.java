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

package org.javahelpers.simple.builders.processor.testing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility class providing stub implementations of {@link ProcessingEnvironment} for unit testing.
 *
 * <p>This class provides utility methods to create minimal ProcessingEnvironment stubs with
 * configurable options, useful for testing annotation processors without requiring full compilation
 * infrastructure.
 */
public final class ProcessingEnvironmentStub {

  private ProcessingEnvironmentStub() {
    // Utility class - prevent instantiation
  }

  /**
   * Creates a stub ProcessingEnvironment with the specified compiler options.
   *
   * <p>All other methods (getMessager, getFiler, etc.) return null or default values. Use this for
   * testing scenarios where only the options map is needed.
   *
   * @param options the compiler options map to return from {@code getOptions()}
   * @return a minimal ProcessingEnvironment stub
   */
  public static ProcessingEnvironment create(Map<String, String> options) {
    return new ProcessingEnvironment() {
      @Override
      public Map<String, String> getOptions() {
        return options != null ? options : Collections.emptyMap();
      }

      @Override
      public Messager getMessager() {
        return null;
      }

      @Override
      public Filer getFiler() {
        return null;
      }

      @Override
      public Elements getElementUtils() {
        return null;
      }

      @Override
      public Types getTypeUtils() {
        return null;
      }

      @Override
      public SourceVersion getSourceVersion() {
        return SourceVersion.latestSupported();
      }

      @Override
      public Locale getLocale() {
        return Locale.getDefault();
      }
    };
  }

  /**
   * Creates a stub ProcessingEnvironment with an empty options map.
   *
   * @return a minimal ProcessingEnvironment stub with no options
   */
  public static ProcessingEnvironment createEmpty() {
    return create(Collections.emptyMap());
  }

  /**
   * Creates a new builder for constructing a ProcessingEnvironment stub with options.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for creating ProcessingEnvironment stubs with a fluent API.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * ProcessingEnvironment env = ProcessingEnvironmentStub.builder()
   *     .put("simplebuilder.builderSuffix", "Builder")
   *     .put("simplebuilder.generateFieldSupplier", "true")
   *     .build();
   * }</pre>
   */
  public static final class Builder {
    private final Map<String, String> options = new HashMap<>();

    private Builder() {}

    /**
     * Adds an option to the ProcessingEnvironment stub.
     *
     * @param name the option name
     * @param value the option value
     * @return this builder for method chaining
     */
    public Builder put(String name, String value) {
      options.put(name, value);
      return this;
    }

    /**
     * Builds and returns the ProcessingEnvironment stub with the configured options.
     *
     * @return a ProcessingEnvironment stub
     */
    public ProcessingEnvironment build() {
      return create(new HashMap<>(options));
    }
  }
}
