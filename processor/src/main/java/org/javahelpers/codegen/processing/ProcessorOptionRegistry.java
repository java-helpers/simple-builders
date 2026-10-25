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

package org.javahelpers.codegen.processing;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.CompilerOption;

/**
 * The compiler options a processor declares, in one place.
 *
 * <p>{@code getSupportedOptions()} is derived from the declaration — no hand-duplicated option name
 * list that can drift from the {@link CompilerOption}s actually parsed.
 */
public final class ProcessorOptionRegistry {

  private final List<CompilerOption> options;

  /**
   * Creates a registry over the given declared options.
   *
   * @param options the processor's compiler options
   */
  public ProcessorOptionRegistry(Collection<? extends CompilerOption> options) {
    this.options = List.copyOf(options);
  }

  /**
   * All supported option names: both the javac-style {@code -A} option name and the legacy
   * compiler-argument spelling of every declared option.
   *
   * @return the supported option names for {@code AbstractProcessor.getSupportedOptions()}
   */
  public Set<String> supportedOptions() {
    Set<String> supported = new HashSet<>();
    for (CompilerOption option : options) {
      supported.add(option.getOptionName());
      supported.add(option.getCompilerArgument());
    }
    return supported;
  }

  /**
   * A typed reader over the declared options for the given environment.
   *
   * @param processingEnv the processing environment to read option values from
   * @return the option value reader
   */
  public CompilerArgumentsReader reader(ProcessingEnvironment processingEnv) {
    return new CompilerArgumentsReader(processingEnv);
  }
}
