/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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

package org.javahelpers.simple.builders.core.builders;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;

/**
 * Enhanced builder for Sets that supports adding elements via builder consumers. This class extends
 * {@link HashSetBuilder} and adds methods to accept {@link Consumer} functions that configure
 * element builders, eliminating the need to manually call {@code build()} on each element builder.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Set<Person> people = new HashSetBuilderWithElementBuilders<>(PersonBuilder::create)
 *     .add(personBuilder -> personBuilder
 *         .name("John Doe")
 *         .age(30))
 *     .add(personBuilder -> personBuilder
 *         .name("Jane Smith")
 *         .age(25))
 *     .build();
 * }</pre>
 *
 * @param <T> the type of elements in the targeting set
 * @param <B> the type of builder used to create elements, must implement {@link IBuilderBase} and building objects of type T
 */
public class HashSetBuilderWithElementBuilders<T, B extends IBuilderBase<T>>
    extends HashSetBuilder<T> {
  private final Supplier<B> elementBuilderProvider;

  /**
   * Creates an empty HashSetBuilderWithElementBuilders.
   *
   * @param elementBuilderProvider supplier that provides new instances of element builders
   */
  public HashSetBuilderWithElementBuilders(Supplier<B> elementBuilderProvider) {
    this.elementBuilderProvider = elementBuilderProvider;
  }

  /**
   * Creates a HashSetBuilderWithElementBuilders initialized with the elements from the given set.
   *
   * @param initialSet set to initialize from
   * @param elementBuilderProvider supplier that provides new instances of element builders
   */
  public HashSetBuilderWithElementBuilders(Set<T> initialSet, Supplier<B> elementBuilderProvider) {
    super(initialSet);
    this.elementBuilderProvider = elementBuilderProvider;
  }

  /**
   * Adds an element to the set by providing a consumer that configures an element builder. The
   * builder is automatically created using the elementBuilderProvider and built after
   * configuration.
   *
   * @param elementBuilderConsumer consumer that configures the element builder
   * @return this builder instance for method chaining
   */
  public HashSetBuilderWithElementBuilders<T, B> add(Consumer<B> elementBuilderConsumer) {
    B elementBuilder = elementBuilderProvider.get();
    elementBuilderConsumer.accept(elementBuilder);
    super.add(elementBuilder.build());
    return this;
  }

  /**
   * Adds multiple elements to the set by providing consumers that configure element builders. Each
   * builder is automatically created using the elementBuilderProvider and built after
   * configuration.
   *
   * @param elementBuilderConsumers varargs of consumers that configure element builders
   * @return this builder instance for method chaining
   */
  @SafeVarargs
  public final HashSetBuilderWithElementBuilders<T, B> add(Consumer<B>... elementBuilderConsumers) {
    for (Consumer<B> elementBuilderConsumer : elementBuilderConsumers) {
      this.add(elementBuilderConsumer);
    }
    return this;
  }
}
