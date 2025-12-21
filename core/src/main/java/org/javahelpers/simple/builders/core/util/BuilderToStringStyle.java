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

package org.javahelpers.simple.builders.core.util;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Custom ToStringStyle for builders that automatically handles TrackedValue fields. This style only
 * includes fields that have been set (TrackedValue.isSet() returns true) and unwraps their values
 * automatically.
 */
public class BuilderToStringStyle extends ToStringStyle {

  private static final long serialVersionUID = 1L;

  public static final BuilderToStringStyle INSTANCE = new BuilderToStringStyle();

  public BuilderToStringStyle() {
    super();
    this.setUseClassName(true);
    this.setUseIdentityHashCode(false);
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
    if (value instanceof TrackedValue<?> trackedValue) {
      if (trackedValue.isSet()) {
        super.append(buffer, fieldName, trackedValue.value(), fullDetail);
      }
    } else {
      super.append(buffer, fieldName, value, fullDetail);
    }
  }
}
