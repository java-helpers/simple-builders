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

import java.util.HashMap;
import java.util.Map;

/**
 * Generic Builder for Maps. Helperclass for being able to provide functional interfaces in
 * extending maps. Using HashMap inside.
 *
 * @param <K> the type of keys in the targeting map
 * @param <V> the type of values in the targeting map
 */
public class HashMapBuilder<K, V> {
  private final Map<K, V> mMap = new HashMap<>();

  /** Creates an empty HashMapBuilder. */
  public HashMapBuilder() {}

  /**
   * Creates a HashMapBuilder initialized with the mappings from the given map.
   *
   * @param initialMap map to initialize from
   */
  public HashMapBuilder(Map<K, V> initialMap) {
    if (initialMap != null) {
      mMap.putAll(initialMap);
    }
  }

  /**
   * Associates the specified value with the specified key in this map. Using an inner HashMap
   * implementation. If the key already exists in the map, the old value is replaced.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return Current instance of HashMapBuilder for using in stream-notation
   */
  public HashMapBuilder<K, V> put(K key, V value) {
    mMap.put(key, value);
    return this;
  }

  /**
   * Associates the specified value with the specified key in this map, if the specified key is not
   * already associated. Using an inner HashMap implementation. If {@code null} is associated with
   * the specific key, it will be replaced.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return Current instance of HashMapBuilder for using in stream-notation
   */
  public HashMapBuilder<K, V> putIfAbsent(K key, V value) {
    mMap.putIfAbsent(key, value);
    return this;
  }

  /**
   * Associates the specified value with the specified key in this map, if the specified value is
   * not null. Using an inner HashMap implementation.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return Current instance of HashMapBuilder for using in stream-notation
   */
  public HashMapBuilder<K, V> putIfValueNotNull(K key, V value) {
    if (value != null) {
      mMap.put(key, value);
    }
    return this;
  }

  /**
   * Copies all of the mappings from the specified map to the inner map of Builder. Using an inner
   * HashMap implementation. These mappings will replace any mappings that the inner map of
   * HashMapBuilder had before.
   *
   * @param pMap mappings to be added to HashMapBuilder
   * @return Current instance of HashMapBuilder for method chaining
   */
  public HashMapBuilder<K, V> putAll(Map<K, V> pMap) {
    mMap.putAll(pMap);
    return this;
  }

  /**
   * Builds a new Map based on the mappings defined by the other functions.
   *
   * @return new HashMap holding all mappings which have been defined before
   */
  public Map<K, V> build() {
    return new HashMap<>(mMap);
  }
}
