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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Registry of the builders generated in the current annotation-processing round.
 *
 * <p>Maps each target type to the {@link TypeName} of the builder being generated for it. The
 * builder name is stored explicitly because it cannot always be derived from the target type:
 * {@code @SimpleBuilderFor} targets generate into the holder's package (and may use the holder's
 * builder suffix), so the generated builder may live in a different package than the default naming
 * convention would suggest.
 */
public final class GeneratedBuilders {
  private final Map<TypeName, TypeName> buildersByTarget = new HashMap<>();

  /**
   * Registers the builder generated in this round for the given target type.
   *
   * @param targetType the type a builder is generated for
   * @param builderType the generated builder's type name
   * @return {@code true} if the target was not already registered, {@code false} if a previous
   *     registration was replaced
   */
  public boolean add(TypeName targetType, TypeName builderType) {
    return buildersByTarget.put(targetType, builderType) == null;
  }

  /**
   * Returns the builder registered for the referenced type.
   *
   * @param referencedType the referenced type a builder may exist for
   * @return the generated builder's type name, or empty if the type is not generated this round
   */
  public Optional<TypeName> findBuilder(TypeName referencedType) {
    return Optional.ofNullable(buildersByTarget.get(referencedType));
  }

  /** Drops all registrations, e.g. at the start of a new processing round. */
  public void clear() {
    buildersByTarget.clear();
  }
}
