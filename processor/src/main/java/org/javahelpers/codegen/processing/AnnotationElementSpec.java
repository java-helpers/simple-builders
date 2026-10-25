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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Declarative specification of which elements a processor works on.
 *
 * <p>The spec only <em>finds</em> elements: matching (direct or via meta-annotation), opt-out
 * filtering and deterministic ordering. Reading annotation <em>values</em> is the domain's
 * responsibility in the planning step — per-instance configuration, repeatable-annotation expansion
 * and conflict detection are domain semantics the spec does not know.
 */
public final class AnnotationElementSpec {

  private final List<ElementSelector> selectors;
  private final String collectionPhase;

  /**
   * Creates a spec.
   *
   * @param selectors the named selection groups, in declaration order
   * @param collectionPhase performance-tracking phase name the collection is reported under, or
   *     {@code null} to skip timing
   */
  public AnnotationElementSpec(List<ElementSelector> selectors, String collectionPhase) {
    this.selectors = selectors;
    this.collectionPhase = collectionPhase;
  }

  /**
   * A builder for an element spec.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder for {@link AnnotationElementSpec}. */
  public static final class Builder {
    private final List<ElementSelector> selectors = new ArrayList<>();
    private String collectionPhase;

    /** Adds a named selection group, in declaration order. */
    public Builder selector(ElementSelector selector) {
      this.selectors.add(selector);
      return this;
    }

    /** Adds a selection group declared inline on a fresh {@link ElementSelector.Builder}. */
    public Builder selector(Consumer<ElementSelector.Builder> selectorDeclaration) {
      ElementSelector.Builder selectorBuilder = ElementSelector.builder();
      selectorDeclaration.accept(selectorBuilder);
      return selector(selectorBuilder.build());
    }

    /** The performance-tracking phase the collection is reported under, or {@code null}. */
    public Builder collectionPhase(String collectionPhase) {
      this.collectionPhase = collectionPhase;
      return this;
    }

    /** Builds the spec. */
    public AnnotationElementSpec build() {
      return new AnnotationElementSpec(selectors, collectionPhase);
    }
  }

  /**
   * The performance-tracking phase the collection is reported under.
   *
   * @return the phase name, or {@code null} when untimed
   */
  public String collectionPhase() {
    return collectionPhase;
  }

  /**
   * Whether any selector uses meta-annotation discovery. Then the supported annotation types cannot
   * be enumerated — user-defined annotation types match the marker without being named.
   *
   * @return {@code true} when a selector has a meta marker
   */
  public boolean hasMetaMarkedSelector() {
    return selectors.stream().anyMatch(selector -> selector.metaMarker() != null);
  }

  /**
   * The concrete annotation type names declared across all selectors.
   *
   * @return the canonical annotation type names the spec selects directly
   */
  public Set<String> annotationTypeNames() {
    Set<String> names = new LinkedHashSet<>();
    for (ElementSelector selector : selectors) {
      for (Class<? extends java.lang.annotation.Annotation> type : selector.annotationTypes()) {
        names.add(type.getCanonicalName());
      }
    }
    return names;
  }

  /**
   * Collects the elements selected by every selector in the given round.
   *
   * @param annotations the annotation types the round carries
   * @param roundEnv the current round
   * @param logger logger for opt-out diagnostics
   * @return the per-selector element groups, sorted by simple name
   */
  public ElementCollection collect(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, ProcessingLogger logger) {
    Map<String, List<Element>> groups = new LinkedHashMap<>();
    for (ElementSelector selector : selectors) {
      Set<Element> selected = new HashSet<>();
      // Direct matches go through the Class overload, which also unwraps repeatable containers
      for (Class<? extends java.lang.annotation.Annotation> type : selector.annotationTypes()) {
        selected.addAll(roundEnv.getElementsAnnotatedWith(type));
      }
      for (TypeElement annotation : matchingAnnotations(annotations, selector)) {
        selected.addAll(roundEnv.getElementsAnnotatedWith(annotation));
      }
      if (selector.optOutAnnotation() != null) {
        selected.removeIf(
            element -> {
              if (JavaLangAnalyser.findAnnotation(element, selector.optOutAnnotation())
                  .isPresent()) {
                logger.debug(
                    "Skipping element '%s' due to @%s opt-out.",
                    element.getSimpleName(), selector.optOutAnnotation().getSimpleName());
                return true;
              }
              return false;
            });
      }
      groups.put(
          selector.name(),
          selected.stream()
              .sorted(Comparator.comparing(element -> element.getSimpleName().toString()))
              .toList());
    }
    return new ElementCollection(groups);
  }

  private static List<TypeElement> matchingAnnotations(
      Set<? extends TypeElement> annotations, ElementSelector selector) {
    List<TypeElement> result = new ArrayList<>();
    for (TypeElement annotation : annotations) {
      if (annotation.getKind() != ElementKind.ANNOTATION_TYPE) {
        continue;
      }
      boolean direct =
          selector.annotationTypes().stream()
              .anyMatch(
                  type -> type.getCanonicalName().equals(annotation.getQualifiedName().toString()));
      boolean meta =
          selector.metaMarker() != null && annotation.getAnnotation(selector.metaMarker()) != null;
      if (direct || meta) {
        result.add(annotation);
      }
    }
    return result;
  }
}
