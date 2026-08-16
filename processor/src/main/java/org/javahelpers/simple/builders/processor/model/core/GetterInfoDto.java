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

package org.javahelpers.simple.builders.processor.model.core;

import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Encapsulates metadata about the getter method resolved for a field.
 *
 * <p>The getter name is needed for the from-instance constructor, which calls the getter to
 * initialise the builder. The {@code deprecated} flag indicates whether the getter is annotated
 * {@code @Deprecated}; this is used to decide whether class-level {@code @SuppressWarnings} is
 * needed, because the from-instance constructor calls the getter internally. Getter deprecation is
 * intentionally not propagated to generated builder methods.
 *
 * <p>A {@code GetterInfoDto} is only created when a getter was found, so {@link #getterName()} is
 * always non-null. The absence of a getter is represented by {@code null} on {@link FieldDto}'s
 * getter info field, not by a {@code GetterInfoDto} with a null name.
 *
 * @param getterName the simple name of the getter method; must not be {@code null}
 * @param deprecated whether the getter method is annotated {@code @Deprecated}
 */
public record GetterInfoDto(String getterName, boolean deprecated) {

  /**
   * Compact constructor enforcing that {@code getterName} is non-null — a {@code GetterInfoDto}
   * only exists when a getter was found.
   *
   * @throws NullPointerException if {@code getterName} is {@code null}
   */
  public GetterInfoDto {
    Objects.requireNonNull(getterName, "getterName must not be null");
  }

  /**
   * Returns the getter method name.
   *
   * @return the getter name, never {@code null}
   */
  public String getGetterName() {
    return getterName;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("getterName", getterName)
        .append("deprecated", deprecated)
        .toString();
  }
}
