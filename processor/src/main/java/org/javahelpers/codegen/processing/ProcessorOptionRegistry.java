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
import org.apache.commons.lang3.StringUtils;
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
  private final CompilerOption verboseOption;
  private final CompilerOption formatterProfileOption;

  private ProcessorOptionRegistry(
      Collection<? extends CompilerOption> options,
      CompilerOption verboseOption,
      CompilerOption formatterProfileOption) {
    this.options = List.copyOf(options);
    this.verboseOption = verboseOption;
    this.formatterProfileOption = formatterProfileOption;
  }

  /**
   * Creates a registry over the given declared options.
   *
   * @param options the processor's compiler options
   */
  public ProcessorOptionRegistry(Collection<? extends CompilerOption> options) {
    this(options, null, null);
  }

  /**
   * A builder for a registry with the framework-level options wired: the verbose flag and the
   * formatter profile option.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
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

  /**
   * Whether verbose diagnostic output was requested — either via the declared verbose option or the
   * javac {@code -verbose} flag (also surfaced by {@code mvn -X}).
   *
   * @param reader the option value reader
   * @return {@code true} when verbose output was requested
   */
  public boolean isVerboseRequested(CompilerArgumentsReader reader) {
    return (verboseOption != null && reader.readBooleanValue(verboseOption))
        || reader.hasOption("-verbose");
  }

  /**
   * The configured source-formatter profile name, or {@code null} when no formatter profile option
   * was declared or set.
   *
   * @param reader the option value reader
   * @return the trimmed profile name, or {@code null}
   */
  public String formatterProfile(CompilerArgumentsReader reader) {
    return formatterProfileOption != null
        ? StringUtils.trimToNull(reader.readValue(formatterProfileOption))
        : null;
  }

  /** Builder for {@link ProcessorOptionRegistry}. */
  public static final class Builder {
    private Collection<? extends CompilerOption> options = List.of();
    private CompilerOption verboseOption;
    private CompilerOption formatterProfileOption;

    /** The processor's declared compiler options. */
    public Builder options(Collection<? extends CompilerOption> options) {
      this.options = options;
      return this;
    }

    /** The option that enables verbose diagnostic output. */
    public Builder verboseOption(CompilerOption verboseOption) {
      this.verboseOption = verboseOption;
      return this;
    }

    /** The option that names the source-formatter profile. */
    public Builder formatterProfileOption(CompilerOption formatterProfileOption) {
      this.formatterProfileOption = formatterProfileOption;
      return this;
    }

    /** Builds the registry. */
    public ProcessorOptionRegistry build() {
      return new ProcessorOptionRegistry(options, verboseOption, formatterProfileOption);
    }
  }
}
