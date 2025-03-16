package org.javahelpers.simple.builders.common;

import java.util.HashSet;
import java.util.Set;

public class HashSetBuilder<T> {
  private final Set<T> mSet = new HashSet<>();

  public HashSetBuilder<T> add(T element) {
    mSet.add(element);
    return this;
  }

  public HashSetBuilder<T> addAll(Set<T> elements) {
    mSet.addAll(elements);
    return this;
  }

  public Set<T> build() {
    return new HashSet(mSet);
  }
}
