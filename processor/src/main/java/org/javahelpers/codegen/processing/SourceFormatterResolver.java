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

import java.util.EnumMap;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.classgen.SourceFormatter;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Resolves the {@link SourceFormatter} for a formatting mode, applying the formatter profile the
 * domain configured.
 *
 * <p>At most one formatter instance per {@link FormattingMode} is created, lazily on first resolve.
 * The concrete formatter implementation is supplied by the domain — the processing machinery only
 * knows the {@link SourceFormatter} contract.
 */
public class SourceFormatterResolver {

  /** Creates a {@link SourceFormatter} for a mode under a formatter profile. */
  @FunctionalInterface
  public interface FormatterFactory {
    /**
     * @param logger logger for formatter warnings
     * @param mode the formatting mode
     * @param profile the formatter profile path, or {@code null} for the built-in profile
     * @return the formatter to use
     */
    SourceFormatter create(ProcessingLogger logger, FormattingMode mode, String profile);
  }

  private final ProcessingLogger logger;
  private final String formatterProfile;
  private final FormatterFactory factory;
  private final EnumMap<FormattingMode, SourceFormatter> formatterCache =
      new EnumMap<>(FormattingMode.class);

  /**
   * Creates a resolver.
   *
   * @param logger logger for formatter warnings
   * @param formatterProfile the configured formatter profile path, or {@code null}
   * @param factory creates the formatter per mode
   */
  public SourceFormatterResolver(
      ProcessingLogger logger, String formatterProfile, FormatterFactory factory) {
    this.logger = logger;
    this.formatterProfile = formatterProfile;
    this.factory = factory;
  }

  /**
   * Returns the formatter for the given mode, creating it on first resolve.
   *
   * @param mode the formatting mode
   * @return a cached or new formatter instance
   */
  public SourceFormatter resolve(FormattingMode mode) {
    return formatterCache.computeIfAbsent(mode, m -> factory.create(logger, m, formatterProfile));
  }
}
