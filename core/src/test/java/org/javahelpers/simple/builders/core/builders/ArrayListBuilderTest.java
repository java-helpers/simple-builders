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
import java.util.List;
import org.junit.jupiter.api.Test;

class ArrayListBuilderTest {

  @Test
  void shouldCreateEmptyBuilder() {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>();
    List<String> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCreateBuilderWithInitialList() {
    List<String> initial = Arrays.asList("item1", "item2", "item3");

    ArrayListBuilder<String> builder = new ArrayListBuilder<>(initial);
    List<String> result = builder.build();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("item1", result.get(0));
    assertEquals("item2", result.get(1));
    assertEquals("item3", result.get(2));
  }

  @Test
  void shouldHandleNullInitialList() {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>(null);
    List<String> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldAllowAddingToInitializedList() {
    List<String> initial = Arrays.asList("item1", "item2");

    ArrayListBuilder<String> builder = new ArrayListBuilder<>(initial);
    builder.add("item3").add("item4");
    List<String> result = builder.build();

    assertNotNull(result);
    assertEquals(4, result.size());
    assertEquals("item1", result.get(0));
    assertEquals("item2", result.get(1));
    assertEquals("item3", result.get(2));
    assertEquals("item4", result.get(3));
  }

  @Test
  void shouldAddSingleElement() {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>();
    builder.add("test");
    List<String> result = builder.build();

    assertEquals(1, result.size());
    assertEquals("test", result.get(0));
  }

  @Test
  void shouldAddMultipleElements() {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>();
    builder.add("first").add("second").add("third");
    List<String> result = builder.build();

    assertEquals(3, result.size());
    assertEquals("first", result.get(0));
    assertEquals("second", result.get(1));
    assertEquals("third", result.get(2));
  }

  @Test
  void shouldAddAllFromCollection() {
    ArrayListBuilder<Integer> builder = new ArrayListBuilder<>();
    builder.addAll(Arrays.asList(1, 2, 3));
    List<Integer> result = builder.build();

    assertEquals(3, result.size());
    assertEquals(1, result.get(0));
    assertEquals(2, result.get(1));
    assertEquals(3, result.get(2));
  }

  @Test
  void shouldChainAddAndAddAll() {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>();
    builder.add("first").addAll(Arrays.asList("second", "third")).add("fourth");
    List<String> result = builder.build();

    assertEquals(4, result.size());
    assertEquals("first", result.get(0));
    assertEquals("second", result.get(1));
    assertEquals("third", result.get(2));
    assertEquals("fourth", result.get(3));
  }

  @Test
  void shouldReturnThisForFluentInterface() {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>();
    ArrayListBuilder<String> result = builder.add("test");

    assertEquals(builder, result);
  }
}
