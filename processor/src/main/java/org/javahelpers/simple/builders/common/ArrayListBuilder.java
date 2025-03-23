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

package org.javahelpers.simple.builders.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Builder for Lists. Helperclass for being able to provide functional interfaces in
 * extending lists. Using ArrayLists inside.
 *
 * @param <T> the type of elements in the targeting list
 */
public class ArrayListBuilder<T> {
  private final List<T> mList = new ArrayList<>();

  /**
   * Appends the element to the end of the internal list. Calling add-function on inner ArrayList
   * field.
   *
   * @param element Element to be appended
   * @return current instance of ArrayListBuilder for using in stream-notation
   */
  public ArrayListBuilder<T> add(T element) {
    mList.add(element);
    return this;
  }

  /**
   * Appends a list of elements to the end of the internal list. Calling addAll-function on inner
   * ArrayList field.
   *
   * @param elements list of Elements to be appended
   * @return current instance of ArrayListBuilder for using in stream-notation
   */
  public ArrayListBuilder<T> addAll(List<T> elements) {
    mList.addAll(elements);
    return this;
  }

  /**
   * Builds a new list based on the elements appended by the other functions.
   *
   * @return new ArrayList holding all elements which have been added before
   */
  public List<T> build() {
    return new ArrayList(mList);
  }
}
