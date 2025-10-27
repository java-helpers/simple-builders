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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests demonstrating how Records work with Simple Builders and the With interface.
 * 
 * <p>This showcases:
 * <ul>
 * <li>Builder pattern for Records</li>
 * <li>With interface for fluent modifications</li>
 * <li>Immutability guarantees of Records</li>
 * <li>Using Class.cast() for type-safe runtime casts</li>
 * </ul>
 */
class ProductRecordTest {

  @Test
  void testBuilder_createsRecordSuccessfully() {
    // Create a product using the builder
    ProductRecord laptop = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    assertNotNull(laptop);
    assertEquals("Gaming Laptop", laptop.name());
    assertEquals(1500.00, laptop.price());
    assertEquals("Electronics", laptop.category());
  }

  @Test
  void testBuilder_copiesFromRecordInstance() {
    // Given: an existing Record instance
    ProductRecord original = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    // When: creating a builder from the Record instance using copy constructor
    ProductRecordBuilder builder = new ProductRecordBuilder(original);
    ProductRecord copy = builder.build();

    // Then: all fields should be copied from the original
    assertNotNull(copy);
    assertEquals("Gaming Laptop", copy.name(), "Name should be copied from original Record");
    assertEquals(1500.00, copy.price(), "Price should be copied from original Record");
    assertEquals("Electronics", copy.category(), "Category should be copied from original Record");
  }

  @Test
  void testWithInterface_createsModifiedCopy() {
    // Given: an original product
    ProductRecord laptop = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    // When: using the With interface to create a modified copy
    ProductRecord discountedLaptop = laptop.with(builder -> 
        builder.price(1200.00)
    );

    // Then: a new instance is created with modified values
    assertNotNull(discountedLaptop);
    assertEquals("Gaming Laptop", discountedLaptop.name());
    assertEquals(1200.00, discountedLaptop.price());
    assertEquals("Electronics", discountedLaptop.category());
    
    // And: the original is unchanged (immutability)
    assertEquals(1500.00, laptop.price());
  }

  @Test
  void testWithInterface_multipleModifications() {
    // Given: an original product
    ProductRecord laptop = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    // When: applying multiple modifications using with()
    ProductRecord rebranded = laptop.with(builder -> 
        builder
            .name("Professional Laptop")
            .category("Business")
            .price(1800.00)
    );

    // Then: all modifications are applied
    assertNotNull(rebranded);
    assertEquals("Professional Laptop", rebranded.name());
    assertEquals(1800.00, rebranded.price());
    assertEquals("Business", rebranded.category());
    
    // And: the original is unchanged
    assertEquals("Gaming Laptop", laptop.name());
    assertEquals(1500.00, laptop.price());
    assertEquals("Electronics", laptop.category());
  }

  @Test
  void testWithInterface_returnsBuilder() {
    // Given: an original product
    ProductRecord laptop = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    // When: getting a builder from the existing instance
    ProductRecordBuilder builder = laptop.with();
    ProductRecord modified = builder
        .price(1400.00)
        .build();

    // Then: the builder is initialized from the original and modifications applied
    assertNotNull(modified);
    assertEquals("Gaming Laptop", modified.name());
    assertEquals(1400.00, modified.price());
    assertEquals("Electronics", modified.category());
  }

  @Test
  void testCustomWithMethod_calculatesDiscount() {
    // Given: a product with a price
    ProductRecord laptop = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    // When: applying a 20% discount using the custom method
    ProductRecord saleProduct = laptop.withDiscountedPrice(20);

    // Then: the price is correctly discounted
    assertNotNull(saleProduct);
    assertEquals("Gaming Laptop", saleProduct.name());
    assertEquals(1200.00, saleProduct.price(), 0.01); // 1500 * 0.8 = 1200
    assertEquals("Electronics", saleProduct.category());
    
    // And: the original is unchanged
    assertEquals(1500.00, laptop.price());
  }

  @Test
  void testImmutability_originalUnchanged() {
    // Given: an original product
    ProductRecord original = ProductRecordBuilder.create()
        .name("Gaming Laptop")
        .price(1500.00)
        .category("Electronics")
        .build();

    // When: creating multiple modified versions
    ProductRecord modified1 = original.with(builder -> builder.price(1200.00));
    ProductRecord modified2 = original.with(builder -> builder.name("Business Laptop"));
    ProductRecord modified3 = original.withDiscountedPrice(10);

    // Then: all modified versions are different instances
    assertNotNull(modified1);
    assertNotNull(modified2);
    assertNotNull(modified3);
    
    // And: the original remains completely unchanged
    assertEquals("Gaming Laptop", original.name());
    assertEquals(1500.00, original.price());
    assertEquals("Electronics", original.category());
  }
}
