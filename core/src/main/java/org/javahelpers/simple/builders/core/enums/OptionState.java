/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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

package org.javahelpers.simple.builders.core.enums;

/**
 * Represents the three-state configuration for builder options.
 *
 * <p>This enum allows distinguishing between:
 *
 * <ul>
 *   <li><b>DEFAULT</b> - Use inherited value (from compiler options or built-in defaults)
 *   <li><b>ENABLED</b> - Explicitly enable this option (override global/default)
 *   <li><b>DISABLED</b> - Explicitly disable this option (override global/default)
 * </ul>
 *
 * <p>Priority resolution: Annotation (ENABLED/DISABLED) > Compiler Options > Built-in Defaults
 *
 * <p>Example:
 *
 * <pre>{@code
 * // Global config via compiler option: -Asimplebuilder.generateFieldSupplier=false
 *
 * // Per-class override to enable:
 * @SimpleBuilder.Options(generateFieldSupplier = OptionState.ENABLED)
 * public class MyDto { }
 * }</pre>
 */
public enum OptionState {
  /**
   * Use inherited value from compiler options or built-in defaults. This is the default state when
   * the option is not explicitly configured at the annotation level.
   */
  UNSET,

  /** Explicitly enable this option, overriding any global configuration or defaults. */
  ENABLED,

  /** Explicitly disable this option, overriding any global configuration or defaults. */
  DISABLED
}
