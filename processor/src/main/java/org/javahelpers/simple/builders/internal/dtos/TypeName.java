package org.javahelpers.simple.builders.internal.dtos;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

public class TypeName {
  private final String packageName;
  private final String className;

  public TypeName(String packageName, String className) {
    requireNonNull(className);
    this.packageName = packageName;
    this.className = className;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassName() {
    return className;
  }

  public Optional<TypeName> getInnerType() {
    return Optional.empty();
  }
}
