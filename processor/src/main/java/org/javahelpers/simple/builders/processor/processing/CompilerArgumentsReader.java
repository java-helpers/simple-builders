/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.lang3.Strings;

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
   * @param argument the compiler argument to read
   * @return the value of the compiler argument, or null if not set
   */
  public String readValue(CompilerOption argument) {
    // Try the -D JVM system property first (e.g., -Dsimplebuilder.verbose)
    String value = System.getProperty(argument.getCompilerArgument());

    // Then the -A compiler argument (e.g., -Asimplebuilder.verbose)
    if (value == null) {
      value = processingEnv.getOptions().get(argument.getCompilerArgument());
    }

    // Finally the bare option name for backward compatibility (e.g., -Averbose)
    if (value == null) {
      value = processingEnv.getOptions().get(argument.getOptionName());
    }

    return value;
  }

  /**
   * Reads the value of a compiler argument as a boolean.
   *
   * <p>Returns true if the value equals "true" or "enabled" (case-insensitive), false otherwise.
   *
   * @param argument the compiler argument to read
   * @return true if the value is "true" or "enabled" (case-insensitive), false otherwise
   */
  public boolean readBooleanValue(CompilerOption argument) {
    String value = readValue(argument);
    return Strings.CI.equalsAny(value, "true", "enabled");
  }
}
