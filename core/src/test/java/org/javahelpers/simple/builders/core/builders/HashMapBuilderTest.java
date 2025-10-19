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

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HashMapBuilderTest {

  @Test
  void shouldCreateEmptyBuilder() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
    Map<String, Integer> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCreateBuilderWithInitialMap() {
    Map<String, Integer> initial = new HashMap<>();
    initial.put("one", 1);
    initial.put("two", 2);
    initial.put("three", 3);

    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>(initial);
    Map<String, Integer> result = builder.build();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(1, result.get("one"));
    assertEquals(2, result.get("two"));
    assertEquals(3, result.get("three"));
  }

  @Test
  void shouldHandleNullInitialMap() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>(null);
    Map<String, Integer> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldAllowAddingToInitializedMap() {
    Map<String, Integer> initial = new HashMap<>();
    initial.put("one", 1);
    initial.put("two", 2);

    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>(initial);
    builder.put("three", 3).put("four", 4);
    Map<String, Integer> result = builder.build();

    assertNotNull(result);
    assertEquals(4, result.size());
    assertEquals(1, result.get("one"));
    assertEquals(2, result.get("two"));
    assertEquals(3, result.get("three"));
    assertEquals(4, result.get("four"));
  }

  @Test
  void shouldPutSingleEntry() {
    HashMapBuilder<String, String> builder = new HashMapBuilder<>();
    builder.put("key", "value");
    Map<String, String> result = builder.build();

    assertEquals(1, result.size());
    assertEquals("value", result.get("key"));
  }

  @Test
  void shouldPutMultipleEntries() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
    builder.put("first", 1).put("second", 2).put("third", 3);
    Map<String, Integer> result = builder.build();

    assertEquals(3, result.size());
    assertEquals(1, result.get("first"));
    assertEquals(2, result.get("second"));
    assertEquals(3, result.get("third"));
  }

  @Test
  void shouldOverwriteExistingKey() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
    builder.put("key", 1).put("key", 2);
    Map<String, Integer> result = builder.build();

    assertEquals(1, result.size());
    assertEquals(2, result.get("key"));
  }

  @Test
  void shouldPutAllFromMap() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
    Map<String, Integer> additional = new HashMap<>();
    additional.put("a", 1);
    additional.put("b", 2);

    builder.putAll(additional);
    Map<String, Integer> result = builder.build();

    assertEquals(2, result.size());
    assertEquals(1, result.get("a"));
    assertEquals(2, result.get("b"));
  }

  @Test
  void shouldChainPutAndPutAll() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
    Map<String, Integer> additional = new HashMap<>();
    additional.put("two", 2);
    additional.put("three", 3);

    builder.put("one", 1).putAll(additional).put("four", 4);
    Map<String, Integer> result = builder.build();

    assertEquals(4, result.size());
    assertEquals(1, result.get("one"));
    assertEquals(2, result.get("two"));
    assertEquals(3, result.get("three"));
    assertEquals(4, result.get("four"));
  }

  @Test
  void shouldHandleNullValues() {
    HashMapBuilder<String, String> builder = new HashMapBuilder<>();
    builder.put("key", null);
    Map<String, String> result = builder.build();

    assertEquals(1, result.size());
    assertTrue(result.containsKey("key"));
    assertEquals(null, result.get("key"));
  }

  @Test
  void shouldReturnThisForFluentInterface() {
    HashMapBuilder<String, Integer> builder = new HashMapBuilder<>();
    HashMapBuilder<String, Integer> result = builder.put("key", 1);

    assertEquals(builder, result);
  }
}
