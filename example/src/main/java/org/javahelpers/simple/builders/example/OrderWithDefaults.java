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

package org.javahelpers.simple.builders.example;

import org.javahelpers.simple.builders.core.annotations.Default;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

/**
 * Example showing default values for setter-based fields in a class using {@code @Default}.
 *
 * <p>When a field annotated with {@code @Default} is not explicitly set on the builder, the
 * declared default value is applied via the setter at {@code build()} time.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // status uses default "PENDING"
 * OrderWithDefaults order = OrderWithDefaultsBuilder.create()
 *     .id("ORD-001")
 *     .build();
 * // order.getPriority() == 3
 * // order.getStatus() == "PENDING"
 *
 * // explicit value overrides default
 * OrderWithDefaults shipped = OrderWithDefaultsBuilder.create()
 *     .id("ORD-002")
 *     .status("SHIPPED")
 *     .build();
 * // shipped.getPriority() == 3
 * // shipped.getStatus() == "SHIPPED"
 * }</pre>
 */
@SimpleBuilder
public class OrderWithDefaults {

  private String id;
  @Default("PENDING") 
  private String status;
  @Default("2")
  private int priority;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }
}
