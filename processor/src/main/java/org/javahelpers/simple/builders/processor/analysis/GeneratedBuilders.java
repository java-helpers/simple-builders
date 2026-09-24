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
 * <p>Maps each target type's qualified name to the {@link TypeName} of the builder being generated
 * for it. The builder name is stored explicitly because it cannot always be derived from the target
 * type: {@code @SimpleBuilderFor} targets generate into the holder's package (and may use the
 * holder's builder suffix), so the generated builder may live in a different package than the
 * default naming convention would suggest.
 *
 * <p>Every mutation notifies the owning {@link BuilderScopeResolver} via the {@code onChange}
 * callback so its cached per-type resolutions are dropped and stale builders are never served.
 */
public final class GeneratedBuilders {
  private final Map<String, TypeName> buildersByTargetFqn = new HashMap<>();
  private final Runnable onChange;

  /**
   * Creates an empty registry.
   *
   * @param onChange callback invoked whenever registrations change (add or clear), so the owner can
   *     invalidate dependent caches
   */
  public GeneratedBuilders(Runnable onChange) {
    this.onChange = onChange;
  }

  /**
   * Registers the builder generated in this round for the given target type.
   *
   * @param targetType the type a builder is generated for
   * @param builderType the generated builder's type name
   */
  public void add(TypeName targetType, TypeName builderType) {
    buildersByTargetFqn.put(targetType.getFullQualifiedName(), builderType);
    onChange.run();
  }

  /**
   * Returns the builder registered for the referenced type.
   *
   * @param referencedTypeFqn the qualified name of the referenced type
   * @return the generated builder's type name, or empty if the type is not generated this round
   */
  public Optional<TypeName> findBuilder(String referencedTypeFqn) {
    return Optional.ofNullable(buildersByTargetFqn.get(referencedTypeFqn));
  }

  /** Drops all registrations, e.g. at the start of a new processing round. */
  public void clear() {
    buildersByTargetFqn.clear();
    onChange.run();
  }
}
