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
package org.javahelpers.simple.builders.processor.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.codegen.model.type.TypeName;

/**
 * Registry of the types generated in the current annotation-processing round.
 *
 * <p>Maps each target type to the {@link TypeName} of the type being generated for it. The
 * generated name is stored explicitly because it cannot always be derived from the target type:
 * targets anchored on a holder type generate into the holder's package (and may follow the holder's
 * naming convention), so the generated type may live in a different package than the default naming
 * convention would suggest.
 */
public final class GeneratedTypes {
  private final Map<TypeName, TypeName> typesByTarget = new HashMap<>();

  /**
   * Registers the type generated in this round for the given target type.
   *
   * @param targetType the type a generated type is produced for
   * @param generatedType the generated type's name
   * @return {@code true} if the target was not already registered, {@code false} if a previous
   *     registration was replaced
   */
  public boolean add(TypeName targetType, TypeName generatedType) {
    return typesByTarget.put(targetType, generatedType) == null;
  }

  /**
   * Returns the generated type registered for the referenced type.
   *
   * @param referencedType the referenced type a generated type may exist for
   * @return the generated type's name, or empty if the type is not generated this round
   */
  public Optional<TypeName> findGenerated(TypeName referencedType) {
    return Optional.ofNullable(typesByTarget.get(referencedType));
  }

  /** Drops all registrations, e.g. at the start of a new processing round. */
  public void clear() {
    typesByTarget.clear();
  }
}
