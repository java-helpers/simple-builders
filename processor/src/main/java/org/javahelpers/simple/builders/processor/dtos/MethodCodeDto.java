package org.javahelpers.simple.builders.processor.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodCodeDto {
  private String codeFormat;
  private final List<MethodCodePlaceholderInterface> codeArguments = new ArrayList<>();

  public void setCodeFormat(String codeFormat) {
    this.codeFormat = codeFormat;
  }

  public void addArgument(String name, String value) {
    codeArguments.add(new MethodCodeStringPlaceholder(name, value));
  }

  public void addArgument(String name, TypeName value) {
    codeArguments.add(new MethodCodeTypePlaceholder(name, value));
  }

  public String getCodeFormat() {
    return codeFormat;
  }

  public List<MethodCodePlaceholderInterface> getCodeArguments() {
    return codeArguments;
  }

  public Map<String, Object> getCodeArgumentsMap() {
    return codeArguments.stream()
        .collect(
            Collectors.toMap(
                MethodCodePlaceholderInterface::getLabel,
                MethodCodePlaceholderInterface::getValue));
  }
}
