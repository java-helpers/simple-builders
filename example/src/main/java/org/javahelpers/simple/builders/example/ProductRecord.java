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

package org.javahelpers.simple.builders.example;

import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

/**
 * Example showing how Records work with Simple Builders and the With interface.
 * 
 * <p>Records are immutable and final. The builder pattern works great with Records
 * for creating new instances. The With interface is also generated, allowing fluent
 * modification methods.
 * 
 * <p>Since annotation processors cannot modify Records to add 'implements' clauses,
 * you need to manually add {@code implements ProductRecordBuilder.With} to your Record
 * declaration to use the fluent with() methods.
 * 
 * <p>Example usage with the With interface:
 * <pre>{@code
 * ProductRecord product = ProductRecordBuilder.create()
 *     .name("Laptop")
 *     .price(1500.0)
 *     .category("Electronics")
 *     .build();
 * 
 * // Create modified copy using with()
 * ProductRecord discounted = product.with(b -> b.price(1200.0));
 * }</pre>
 */
@SimpleBuilder
public record ProductRecord(
    String name,
    double price,
    String category
) implements ProductRecordBuilder.With {
    
    /**
     * Custom method showing fluent modification using the With interface.
     * Since Records are immutable, the with() methods create new instances.
     */
    public ProductRecord withDiscountedPrice(double discountPercentage) {
        double discountedPrice = price * (1 - discountPercentage / 100);
        // Use the with() method from the With interface for fluent modification
        return with(builder -> builder.price(discountedPrice));
    }
}
