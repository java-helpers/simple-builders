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

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * One named element selection group inside {@link ElementCollector}.
 *
 * <p>An element is selected when it carries one of the {@link #annotationTypes}, or an annotation
 * type that is itself meta-annotated with {@link #metaMarker} (the only way to discover
 * user-defined annotation types, since the supported annotation types stay {@code "*"}). Selected
 * elements carrying the {@link #optOutAnnotation} are dropped again.
 *
 * @param groupName the group name results are keyed by in {@link ElementGroups}
 * @param annotationTypes annotation types that directly select an element
 * @param metaMarker meta-annotation marking annotation types that also select, or {@code null}
 * @param optOutAnnotation annotation that excludes a selected element again, or {@code null}
 */
public record ElementSelector(
    String groupName,
    Set<Class<? extends Annotation>> annotationTypes,
    Class<? extends Annotation> metaMarker,
    Class<? extends Annotation> optOutAnnotation) {

  /**
   * A builder for an element selector.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder for {@link ElementSelector}. */
  public static final class Builder {
    private String groupName;
    private Set<Class<? extends Annotation>> annotationTypes = Set.of();
    private Class<? extends Annotation> metaMarker;
    private Class<? extends Annotation> optOutAnnotation;

    /** The group name results are keyed by. */
    public Builder groupName(String groupName) {
      this.groupName = groupName;
      return this;
    }

    /** Annotation types that directly select an element. */
    @SafeVarargs
    public final Builder annotationTypes(Class<? extends Annotation>... annotationTypes) {
      this.annotationTypes = Set.of(annotationTypes);
      return this;
    }

    /** Meta-annotation marking annotation types that also select, or {@code null}. */
    public Builder metaMarker(Class<? extends Annotation> metaMarker) {
      this.metaMarker = metaMarker;
      return this;
    }

    /** Annotation that excludes a selected element again, or {@code null}. */
    public Builder optOutAnnotation(Class<? extends Annotation> optOutAnnotation) {
      this.optOutAnnotation = optOutAnnotation;
      return this;
    }

    /** Builds the selector. */
    public ElementSelector build() {
      return new ElementSelector(groupName, annotationTypes, metaMarker, optOutAnnotation);
    }
  }
}
