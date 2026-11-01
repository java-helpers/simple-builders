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
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.SourceFormatter;
import org.javahelpers.simple.builders.processor.classgen.roaster.RoasterSourceFormatter;
import org.javahelpers.codegen.logging.ProcessingLogger;

/**
 * Resolves the {@link SourceFormatter} for a formatting mode, applying the formatter profile
 * configured via the {@code formatterProfile} compiler argument.
 *
 * <p>At most one formatter instance per {@link FormattingMode} is created, lazily on first resolve.
 */
public class SourceFormatterResolver {

  private final ProcessingLogger logger;
  private final String formatterProfile;
  private final EnumMap<FormattingMode, SourceFormatter> formatterCache =
      new EnumMap<>(FormattingMode.class);

  /**
   * Creates a resolver.
   *
   * @param logger logger for formatter warnings
   * @param formatterProfile the configured formatter profile path, or {@code null}
   */
  public SourceFormatterResolver(ProcessingLogger logger, String formatterProfile) {
    this.logger = logger;
    this.formatterProfile = formatterProfile;
  }

  /**
   * Returns the formatter for the given mode, creating it on first resolve.
   *
   * @param mode the formatting mode
   * @return a cached or new formatter instance
   */
  public SourceFormatter resolve(FormattingMode mode) {
    return formatterCache.computeIfAbsent(
        mode, m -> new RoasterSourceFormatter(logger, m, formatterProfile));
  }
}
