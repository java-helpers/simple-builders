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

package org.javahelpers.simple.builders.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a default value for a builder field that is applied when the field is not explicitly
 * set before calling {@code build()}.
 *
 * <p>The {@link #value()} is a string expression that is interpreted based on the field type:
 *
 * <ul>
 *   <li><b>String</b> — wrapped in double quotes, e.g. {@code @Default("GENERAL")} generates {@code
 *       "GENERAL"}
 *   <li><b>char</b> — wrapped in single quotes, e.g. {@code @Default("A")} generates {@code 'A'}
 *   <li><b>numeric/boolean primitives</b> — used as-is, e.g. {@code @Default("0.0")} generates
 *       {@code 0.0}
 *   <li><b>complex types</b> — used as a raw Java expression, e.g. {@code @Default("List.of()")}
 *       generates {@code List.of()}
 * </ul>
 *
 * <p>Can be placed on constructor parameters or fields. When a field has a default value, it is no
 * longer considered "required" even if annotated with {@code @NotNull} or {@code @NonNull}.
 *
 * <p>Example with a record:
 *
 * <pre>{@code
 * @SimpleBuilder
 * public record Product(String name, double price,
 *     @Default("GENERAL") String category) {}
 *
 * // category defaults to "GENERAL" if not set
 * Product p = ProductBuilder.create()
 *     .name("Widget")
 *     .price(9.99)
 *     .build();
 * // p.category() == "GENERAL"
 * }</pre>
 *
 * <p>Example with a class:
 *
 * <pre>{@code
 * @SimpleBuilder
 * public class Order {
 *   private String id;
 *   @Default("PENDING") private String status;
 *
 *   public String getId() { return id; }
 *   public void setId(String id) { this.id = id; }
 *   public String getStatus() { return status; }
 *   public void setStatus(String status) { this.status = status; }
 * }
 *
 * // status defaults to "PENDING" if not set
 * Order o = OrderBuilder.create()
 *     .id("ORD-001")
 *     .build();
 * // o.getStatus() == "PENDING"
 * }</pre>
 *
 * <p><b>Framework-agnostic detection:</b> The builder processor also detects annotations named
 * {@code Default} or {@code DefaultValue} from any package (e.g. Jakarta REST {@code
 * jakarta.ws.rs.DefaultValue}) if they have a {@code String value()} member.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface Default {

  /**
   * The default value as a string expression, interpreted based on the field type.
   *
   * <p>For String fields, the value is quoted automatically. For char fields, it is single-quoted.
   * For numeric/boolean primitives and complex types, it is used as a raw Java expression.
   *
   * @return the default value expression
   */
  String value();
}
