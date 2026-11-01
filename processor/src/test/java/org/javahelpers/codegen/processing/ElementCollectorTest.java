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

package org.javahelpers.simple.builders.processor.processing;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.codegen.processing.ElementCollector;
import org.javahelpers.codegen.processing.ElementGroups;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ElementCollector}. */
class ElementCollectorTest {

  @Test
  void collect_nonAnnotationTypeElementsAreSkipped() {
    ElementCollector collector =
        ElementCollector.builder().selector(selector -> selector.groupName("group")).build();

    // a javac round only ever passes annotation types — a non-annotation entry is skipped early
    ElementGroups groups =
        collector.collect(
            Set.of(new StubTypeElement(ElementKind.CLASS)),
            null,
            CapturingProcessingLogger.create().logger());

    assertTrue(groups.byGroup("group").isEmpty());
  }

  private static class StubTypeElement implements TypeElement {
    private final ElementKind kind;

    StubTypeElement(ElementKind kind) {
      this.kind = kind;
    }

    @Override
    public ElementKind getKind() {
      return kind;
    }

    @Override
    public TypeMirror asType() {
      return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
      return Set.of();
    }

    @Override
    public Name getSimpleName() {
      return null;
    }

    @Override
    public Element getEnclosingElement() {
      return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return List.of();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return List.of();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return null;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> visitor, P parameter) {
      return null;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
      return List.of();
    }

    @Override
    public TypeMirror getSuperclass() {
      return null;
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
      return List.of();
    }

    @Override
    public NestingKind getNestingKind() {
      return NestingKind.TOP_LEVEL;
    }

    @Override
    public Name getQualifiedName() {
      return null;
    }
  }
}
