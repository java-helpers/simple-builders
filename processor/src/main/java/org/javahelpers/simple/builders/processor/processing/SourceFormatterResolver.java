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

import java.util.EnumMap;
import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.classgen.SourceFormatter;
import org.javahelpers.simple.builders.processor.classgen.roaster.RoasterSourceFormatter;
import org.javahelpers.simple.codegen.logging.ProcessingLogger;

/**
 * Resolves the {@link SourceFormatter} for a formatting mode, applying the Eclipse formatter
 * profile configured via the {@code formatterProfile} compiler argument.
 *
 * <p>At most one formatter instance per {@link FormattingMode} is created, lazily on first resolve.
 */
public class SourceFormatterResolver {

  private final ProcessingLogger logger;
  private final String formatterProfile;
  private final EnumMap<FormattingMode, SourceFormatter> formatterCache =
      new EnumMap<>(FormattingMode.class);

  /**
   * Creates a resolver reading the formatter profile from compiler arguments.
   *
   * @param processingEnv the processing environment (for compiler argument access)
   * @param logger logger for formatter warnings
   */
  public SourceFormatterResolver(ProcessingEnvironment processingEnv, ProcessingLogger logger) {
    this.logger = logger;
    this.formatterProfile =
        StringUtils.trimToNull(
            new CompilerArgumentsReader(processingEnv)
                .readValue(CompilerArgumentsEnum.FORMATTER_PROFILE));
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
