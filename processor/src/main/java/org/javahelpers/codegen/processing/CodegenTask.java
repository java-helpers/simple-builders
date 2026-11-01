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

package org.javahelpers.codegen.processing;

import java.util.Objects;
import javax.lang.model.element.Element;

/**
 * A planned generation: one element to generate code for, with the configuration the domain
 * resolved during planning.
 *
 * <p>One round element may expand into several tasks (e.g. an annotation declaring multiple
 * external types).
 *
 * @param <K> the domain's configuration type
 * @param element the element code is generated for
 * @param configuration the resolved per-element configuration
 * @param reportingElement the element diagnostics are reported on — the declaring holder for
 *     externally declared types, otherwise the element itself
 * @param outputPackage the package generated code is written to — planning computes it so the
 *     driver can pass the task to {@link
 *     org.javahelpers.simple.builders.processor.processing.AnnotationProcessingContext#setCurrentTask}
 *     before analysis
 */
public record CodegenTask<K extends CodegenConfiguration>(
    Element element, K configuration, Element reportingElement, String outputPackage) {

  /**
   * A task builder — {@code reportingElement} defaults to the task's element when not set.
   *
   * @param <K> the configuration type
   * @return a new builder
   */
  public static <K extends CodegenConfiguration> Builder<K> builder() {
    return new Builder<>();
  }

  /** Fluent builder for {@link CodegenTask}. */
  public static final class Builder<K extends CodegenConfiguration> {
    private Element element;
    private K configuration;
    private Element reportingElement;
    private String outputPackage;

    /** The element the task generates for; also the default {@link #reportingElement}. */
    public Builder<K> element(Element element) {
      this.element = element;
      return this;
    }

    /** The resolved per-element configuration. */
    public Builder<K> configuration(K configuration) {
      this.configuration = configuration;
      return this;
    }

    /** The element diagnostics are reported on; defaults to {@link #element}. */
    public Builder<K> reportingElement(Element reportingElement) {
      this.reportingElement = reportingElement;
      return this;
    }

    /** The package generated code is written to. */
    public Builder<K> outputPackage(String outputPackage) {
      this.outputPackage = outputPackage;
      return this;
    }

    /** Builds the task; the reporting element defaults to the task element. */
    public CodegenTask<K> build() {
      return new CodegenTask<>(
          Objects.requireNonNull(element, "element"),
          Objects.requireNonNull(configuration, "configuration"),
          reportingElement != null ? reportingElement : element,
          Objects.requireNonNull(outputPackage, "outputPackage"));
    }
  }
}
