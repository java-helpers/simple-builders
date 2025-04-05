package org.javahelpers.simple.builders.processor.dtos;

import java.util.ArrayList;
import java.util.List;

/** DTO for holding information of code implementation. */
public class MethodCodeDto {
  /** Format of code. Holding placeholder for dynamic values. */
  private String codeFormat;

  /** List of placeholders in CodeFormat. Containing dynamic values too. */
  private final List<MethodCodePlaceholder> codeArguments = new ArrayList<>();

  /**
   * Setting format of code.
   *
   * @param codeFormat Codeformat
   */
  public void setCodeFormat(String codeFormat) {
    this.codeFormat = codeFormat;
  }

  /**
   * Adding an argument for Codeformat. Helperfunction to set text value.
   *
   * @param name name in codeformat
   * @param value value to fill in codeformat
   */
  public void addArgument(String name, String value) {
    codeArguments.add(new MethodCodeStringPlaceholder(name, value));
  }

  /**
   * Adding an argument for Codeformat. Helperfunction to set TypeName value.
   *
   * @param name name in codeformat
   * @param value value to fill in codeformat
   */
  public void addArgument(String name, TypeName value) {
    codeArguments.add(new MethodCodeTypePlaceholder(name, value));
  }

  /**
   * Getter for Codeformat.
   *
   * @return codeformat
   */
  public String getCodeFormat() {
    return codeFormat;
  }

  /**
   * Getter for arguments in Codeformat.
   *
   * @return argument values
   */
  public List<MethodCodePlaceholder> getCodeArguments() {
    return codeArguments;
  }
}
