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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HashSetBuilderTest {

  @Test
  void shouldCreateEmptyBuilder() {
    HashSetBuilder<String> builder = new HashSetBuilder<>();
    Set<String> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCreateBuilderWithInitialSet() {
    Set<String> initial = new HashSet<>(Arrays.asList("item1", "item2", "item3"));

    HashSetBuilder<String> builder = new HashSetBuilder<>(initial);
    Set<String> result = builder.build();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains("item1"));
    assertTrue(result.contains("item2"));
    assertTrue(result.contains("item3"));
  }

  @Test
  void shouldHandleNullInitialSet() {
    HashSetBuilder<String> builder = new HashSetBuilder<>(null);
    Set<String> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldAllowAddingToInitializedSet() {
    Set<String> initial = new HashSet<>(Arrays.asList("item1", "item2"));

    HashSetBuilder<String> builder = new HashSetBuilder<>(initial);
    builder.add("item3").add("item4");
    Set<String> result = builder.build();

    assertNotNull(result);
    assertEquals(4, result.size());
    assertTrue(result.contains("item1"));
    assertTrue(result.contains("item2"));
    assertTrue(result.contains("item3"));
    assertTrue(result.contains("item4"));
  }

  @Test
  void shouldAddSingleElement() {
    HashSetBuilder<String> builder = new HashSetBuilder<>();
    builder.add("test");
    Set<String> result = builder.build();

    assertEquals(1, result.size());
    assertTrue(result.contains("test"));
  }

  @Test
  void shouldAddMultipleElements() {
    HashSetBuilder<String> builder = new HashSetBuilder<>();
    builder.add("first").add("second").add("third");
    Set<String> result = builder.build();

    assertEquals(3, result.size());
    assertTrue(result.contains("first"));
    assertTrue(result.contains("second"));
    assertTrue(result.contains("third"));
  }

  @Test
  void shouldHandleDuplicateElements() {
    HashSetBuilder<String> builder = new HashSetBuilder<>();
    builder.add("duplicate").add("duplicate").add("unique");
    Set<String> result = builder.build();

    assertEquals(2, result.size());
    assertTrue(result.contains("duplicate"));
    assertTrue(result.contains("unique"));
  }

  @Test
  void shouldAddAllFromSet() {
    HashSetBuilder<Integer> builder = new HashSetBuilder<>();
    builder.addAll(new HashSet<>(Arrays.asList(1, 2, 3)));
    Set<Integer> result = builder.build();

    assertEquals(3, result.size());
    assertTrue(result.contains(1));
    assertTrue(result.contains(2));
    assertTrue(result.contains(3));
  }

  @Test
  void shouldChainAddAndAddAll() {
    HashSetBuilder<String> builder = new HashSetBuilder<>();
    builder.add("first").addAll(new HashSet<>(Arrays.asList("second", "third"))).add("fourth");
    Set<String> result = builder.build();

    assertEquals(4, result.size());
    assertTrue(result.contains("first"));
    assertTrue(result.contains("second"));
    assertTrue(result.contains("third"));
    assertTrue(result.contains("fourth"));
  }

  @Test
  void shouldReturnThisForFluentInterface() {
    HashSetBuilder<String> builder = new HashSetBuilder<>();
    HashSetBuilder<String> result = builder.add("test");

    assertEquals(builder, result);
  }
}
