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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;

/**
 * Parsers for raw option values coming from compiler arguments or annotation attributes.
 *
 * <p>Parsing is lenient on purpose: unrecognized values fall back to {@link OptionState#UNSET} or
 * {@link AccessModifier#DEFAULT} so that the regular option precedence (annotation > compiler
 * argument > default) still applies instead of failing the build on a typo. A warning is logged
 * when the value is non-blank but unrecognized, so typos are visible without breaking the build.
 */
public final class OptionValueParsers {

  private OptionValueParsers() {}

  /**
   * Parses an option value as {@link OptionState}: {@code "true"}/{@code "enabled"} mean ENABLED,
   * {@code "false"}/{@code "disabled"} mean DISABLED, anything else (including null) means UNSET.
   *
   * <p>When a non-blank value is not recognized, a warning is logged before falling back to UNSET.
   *
   * @param value the raw option value
   * @param logger the logger for warnings on unrecognized values (must not be null)
   * @return the parsed OptionState
   */
  public static OptionState parseOptionState(String value, ProcessingLogger logger) {
    if (Strings.CI.equalsAny(value, "true", "enabled")) {
      return OptionState.ENABLED;
    } else if (Strings.CI.equalsAny(value, "false", "disabled")) {
      return OptionState.DISABLED;
    }
    if (StringUtils.isNotBlank(value)) {
      logger.warning("Unrecognized option-state value '%s' - falling back to UNSET", value);
    }
    return OptionState.UNSET;
  }

  /**
   * Parses an option value as {@link AccessModifier}, returning DEFAULT for unset or invalid
   * values.
   *
   * <p>When a non-blank value is not recognized, a warning is logged before falling back to
   * DEFAULT.
   *
   * @param value the raw option value
   * @param logger the logger for warnings on unrecognized values (must not be null)
   * @return the parsed AccessModifier
   */
  public static AccessModifier parseAccessModifier(String value, ProcessingLogger logger) {
    if (Strings.CI.equals(value, "public")) {
      return AccessModifier.PUBLIC;
    } else if (Strings.CI.equals(value, "private")) {
      return AccessModifier.PRIVATE;
    } else if (Strings.CI.equalsAny(value, "package-private", "package_private")) {
      return AccessModifier.PACKAGE_PRIVATE;
    }
    if (StringUtils.isNotBlank(value)) {
      logger.warning("Unrecognized access-modifier value '%s' - falling back to DEFAULT", value);
    }
    return AccessModifier.DEFAULT;
  }
}
