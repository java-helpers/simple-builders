package org.javahelpers.simple.builders.common;

import java.util.ArrayList;
import java.util.List;

public class ArrayListBuilder<T> {
  private final List<T> mList = new ArrayList<>();

  public ArrayListBuilder<T> add(T element) {
    mList.add(element);
    return this;
  }

  public ArrayListBuilder<T> addAll(List<T> elements) {
    mList.addAll(elements);
    return this;
  }

  public List<T> build() {
    return new ArrayList(mList);
  }
}
