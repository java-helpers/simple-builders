/*
 * Copyright 2025 Andreas Igel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javahelpers.simple.builders.processor.model.type;

import java.util.Optional;

/**
 * A builder type resolved for a referenced field or element type, together with the instantiation
 * paths generated code must use to call it.
 *
 * <p>{@code emptyFactoryMethod} and {@code copyFactoryMethod} carry the name of a static factory on
 * the builder (e.g. {@code create} or {@code of}) when the contract check found one. When a
 * component is empty, generated code instantiates the builder through its constructor instead - the
 * builder contract guarantees the matching constructor exists then.
 *
 * @param typeName the builder type to reference
 * @param emptyFactoryMethod static parameterless factory returning the builder, or empty to
 *     instantiate via {@code new B()}
 * @param copyFactoryMethod static factory accepting the built type, or empty to instantiate via
 *     {@code new B(value)}
 */
public record ResolvedBuilder(
    TypeName typeName, Optional<String> emptyFactoryMethod, Optional<String> copyFactoryMethod) {

  /**
   * A resolution instantiated exclusively through constructors.
   *
   * @param typeName the builder type to reference
   */
  public ResolvedBuilder(TypeName typeName) {
    this(typeName, Optional.empty(), Optional.empty());
  }
}
