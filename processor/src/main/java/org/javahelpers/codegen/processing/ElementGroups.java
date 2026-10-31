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

import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;

/**
 * The result of {@link ElementCollector#collect}: per-selector-name, deterministically ordered
 * element lists. Elements are sorted by simple name so generated output is reproducible.
 */
public final class ElementGroups {

  private final Map<String, List<Element>> groups;

  ElementGroups(Map<String, List<Element>> groups) {
    this.groups = groups;
  }

  /**
   * The elements selected by the named selector, sorted by simple name.
   *
   * @param groupName name of the selecting {@link ElementSelector}'s group
   * @return the selected elements, or an empty list when the selector matched nothing
   */
  public List<Element> byGroup(String groupName) {
    return groups.getOrDefault(groupName, List.of());
  }

  /**
   * Every selected element across all groups, in group declaration order.
   *
   * @return all selected elements
   */
  public List<Element> all() {
    return groups.values().stream().flatMap(List::stream).toList();
  }

  /**
   * Whether every selected group is empty.
   *
   * @return {@code true} when no selector matched any element
   */
  public boolean isEmpty() {
    return groups.values().stream().allMatch(List::isEmpty);
  }
}
