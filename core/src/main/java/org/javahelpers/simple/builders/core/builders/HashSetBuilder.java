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

import java.util.HashSet;
import java.util.Set;

/**
 * Generic Builder for Sets. Helperclass for being able to provide stream-notation interfaces in
 * extending setws. Using HashSet inside.
 *
 * @param <T> the type of elements in the targeting Set
 */
public class HashSetBuilder<T> {
  private final Set<T> mSet = new HashSet<>();

  /** Creates an empty HashSetBuilder. */
  public HashSetBuilder() {}

  /**
   * Creates a HashSetBuilder initialized with the elements from the given set.
   *
   * @param initialSet set to initialize from
   */
  public HashSetBuilder(Set<T> initialSet) {
    if (initialSet != null) {
      mSet.addAll(initialSet);
    }
  }

  /**
   * Appends the element to the internal set. Calling add-function on inner HashSet field.
   *
   * @param element element to be appended
   * @return current instance of ArrayListBuilder for using in stream-notation
   */
  public HashSetBuilder<T> add(T element) {
    mSet.add(element);
    return this;
  }

  /**
   * Appends a list of elements to the end of the internal list. Calling addAll-function on inner
   * HashSet field.
   *
   * @param elements Set of elements to be appended
   * @return current instance of HashSetBuilder for using in stream-notation
   */
  public HashSetBuilder<T> addAll(Set<T> elements) {
    mSet.addAll(elements);
    return this;
  }

  /**
   * Builds a new Set containing all elements that have been added to this builder. The returned Set
   * is a new instance, so subsequent modifications to the builder will not affect the returned Set.
   *
   * @return a new HashSet containing all elements added to this builder
   */
  public Set<T> build() {
    return new HashSet<>(mSet);
  }
}
