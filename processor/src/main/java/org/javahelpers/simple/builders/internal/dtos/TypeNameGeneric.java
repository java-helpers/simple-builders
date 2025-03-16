package org.javahelpers.simple.builders.internal.dtos;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

public class TypeNameGeneric extends TypeName {
  private final TypeName innerType;

  public TypeNameGeneric(String packageName, String className, TypeName innerType) {
    super(packageName, className);
    requireNonNull(innerType);
    this.innerType = innerType;
  }

  @Override
  public Optional<TypeName> getInnerType() {
    return Optional.of(innerType);
  }
}
