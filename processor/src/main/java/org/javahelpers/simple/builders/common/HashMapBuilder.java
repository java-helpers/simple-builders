package org.javahelpers.simple.builders.common;

import java.util.HashMap;
import java.util.Map;

public class HashMapBuilder<K, V> {
  private final Map<K, V> mMap = new HashMap<>();

  public HashMapBuilder<K, V> put(K key, V value) {
    mMap.put(key, value);
    return this;
  }

  public HashMapBuilder<K, V> putIfAbsent(K key, V value) {
    mMap.putIfAbsent(key, value);
    return this;
  }

  public HashMapBuilder<K, V> putAll(Map<K, V> pMap) {
    mMap.putAll(pMap);
    return this;
  }

  public Map<K, V> build() {
    return new HashMap<>(mMap);
  }
}
