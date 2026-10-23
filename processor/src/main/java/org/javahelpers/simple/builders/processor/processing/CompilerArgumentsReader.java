/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.processing;

import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.codegen.processing.CompilerOption;

/**
 * Utility class for reading compiler arguments from the annotation processing environment.
 *
 * <p>This class provides a centralized way to read compiler arguments using {@link CompilerOption}
 * values, ensuring consistent handling of option names and values across the processor.
 */
public class CompilerArgumentsReader {
  private final ProcessingEnvironment processingEnv;

  /**
   * Constructs a new CompilerArgumentsReader.
   *
   * @param processingEnv the processing environment providing access to compiler options
   */
  public CompilerArgumentsReader(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  /**
   * Reads the value of a compiler argument.
   *
   * <p>The method checks the prefixed JVM system property first, then the prefixed compiler
   * argument, and finally the bare option name for backward compatibility. The system property wins
   * so a command-line {@code -D} can override options configured in the build file. The system
   * property is available when the build tool runs javac in-process and is not available with
   * {@code <fork>true</fork>}.
   *
   * @param argument the compiler option to read
   * @return the value of the compiler argument, or null if not set
   */
  public String readValue(CompilerOption argument) {
    return readValue(argument, processingEnv.getOptions());
  }

  /**
   * Reads the value of a compiler argument from an explicit options map — the same lookup SPI
   * adapters can use on the options map their host framework hands them.
   *
   * @param argument the compiler option to read
   * @param options the compiler options map to search
   * @return the value of the compiler argument, or null if not set
   */
  public static String readValue(CompilerOption argument, Map<String, String> options) {
    // Try the -D JVM system property first (e.g., -Dsimplebuilder.verbose)
    String value = System.getProperty(argument.getCompilerArgument());

    // Then the -A compiler argument (e.g., -Asimplebuilder.verbose)
    if (value == null) {
      value = options.get(argument.getCompilerArgument());
    }

    // Finally the bare option name for backward compatibility (e.g., -Averbose)
    if (value == null) {
      value = options.get(argument.getOptionName());
    }

    return value;
  }

  /**
   * Reads the value of a compiler argument as a boolean.
   *
   * <p>Returns true if the value equals "true" (case-insensitive), false otherwise.
   *
   * @param argument the compiler option to read
   * @return true if the value is "true" (case-insensitive), false otherwise
   */
  public boolean readBooleanValue(CompilerOption argument) {
    return readBooleanValue(argument, processingEnv.getOptions(), false);
  }

  /**
   * Reads the value of a compiler argument as a boolean with an explicit default — the same
   * resolution SPI adapters can use on the options map their host framework hands them.
   *
   * @param argument the compiler option to read
   * @param options the compiler options map to search
   * @param defaultValue the result for an unset or unrecognizable value
   * @return "true"/"enabled" resolves to true, "false"/"disabled" to false, anything else to {@code
   *     defaultValue}
   */
  public static boolean readBooleanValue(
      CompilerOption argument, Map<String, String> options, boolean defaultValue) {
    String value = readValue(argument, options);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    if (Strings.CI.equalsAny(value, "true", "enabled")) {
      return true;
    }
    if (Strings.CI.equalsAny(value, "false", "disabled")) {
      return false;
    }
    return defaultValue;
  }
}
