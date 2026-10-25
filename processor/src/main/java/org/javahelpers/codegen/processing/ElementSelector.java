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
 * One named element selection group inside {@link AnnotationElementSpec}.
 *
 * <p>An element is selected when it carries one of the {@link #annotationTypes}, or an annotation
 * type that is itself meta-annotated with {@link #metaMarker} (the only way to discover
 * user-defined annotation types, since the supported annotation types stay {@code "*"}). Selected
 * elements carrying the {@link #optOutAnnotation} are dropped again.
 *
 * @param name the group name results are keyed by in {@link ElementCollection}
 * @param annotationTypes annotation types that directly select an element
 * @param metaMarker meta-annotation marking annotation types that also select, or {@code null}
 * @param optOutAnnotation annotation that excludes a selected element again, or {@code null}
 */
public record ElementSelector(
    String name,
    Set<Class<? extends Annotation>> annotationTypes,
    Class<? extends Annotation> metaMarker,
    Class<? extends Annotation> optOutAnnotation) {}
