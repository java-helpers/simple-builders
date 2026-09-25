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

package org.javahelpers.simple.builders.processor.analysis;

import java.util.Optional;
import java.util.function.Function;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Options controlling how {@link JavaLangMapper} maps elements to {@code TypeName} instances.
 *
 * @param copyTypeAnnotations whether annotations declared on a type usage are copied onto the
 *     mapped {@code TypeName}
 * @param generatedTypeResolver resolves a source type element to the generated type usable for it
 *     (e.g. its builder), or empty when none applies; may be {@code null} to disable resolution
 */
public record MapperOptions(
    boolean copyTypeAnnotations, Function<TypeElement, Optional<TypeName>> generatedTypeResolver) {

  /** Default options: no annotation copying, no generated-type resolution. */
  public static final MapperOptions DEFAULT = new MapperOptions(false, null);

  /**
   * Resolves the generated type usable for the given type element.
   *
   * @param typeElement the type element to resolve
   * @return the usable generated type, or empty if resolution is disabled or no match exists
   */
  public Optional<TypeName> resolveGeneratedType(TypeElement typeElement) {
    return generatedTypeResolver != null
        ? generatedTypeResolver.apply(typeElement)
        : Optional.empty();
  }
}
