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
 * Example showing default values for unset builder fields using {@code @Default}.
 *
 * <p>When a field annotated with {@code @Default} is not explicitly set on the builder, the
 * declared default value is used at {@code build()} time.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // category and active use defaults
 * ProductWithDefaults product = ProductWithDefaultsBuilder.create()
 *     .name("Laptop")
 *     .price(1500.0)
 *     .build();
 * // product.category() == "GENERAL"
 * // product.active() == true
 *
 * // explicit values override defaults
 * ProductWithDefaults custom = ProductWithDefaultsBuilder.create()
 *     .name("Widget")
 *     .price(9.99)
 *     .category("ACCESSORIES")
 *     .active(false)
 *     .build();
 * // custom.category() == "ACCESSORIES"
 * // custom.active() == false
 * }</pre>
 */
@SimpleBuilder
public record ProductWithDefaults(
    String name,
    double price,
    @Default("GENERAL") String category,
    @Default("true") boolean active
) {}
