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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookDtoBuilderTest {

  @Test
  void testElementaryBuilderWithAllPropertyTypes() {
    LocalDate publishDate = LocalDate.of(2008, 8, 1);
    LocalDateTime lastUpdated = LocalDateTime.of(2024, 12, 20, 10, 30);
    
    BookDto book =
        BookDtoBuilder.create()
            .title("Clean Code")
            .author("Robert C. Martin")
            .isbn("978-0132350884")
            .pages(464)
            .price(39.99)
            .exactPrice(new BigDecimal("39.99"))
            .available(true)
            .rating((byte) 5)
            .edition((short) 1)
            .salesCount(1000000L)
            .discount(0.15f)
            .category('T')
            .publishDate(publishDate)
            .lastUpdated(lastUpdated)
            .subtitle(Optional.of("A Handbook of Agile Software Craftsmanship"))
            .tags(List.of("programming", "clean-code", "best-practices"))
            .genres(Set.of("Technical", "Software Engineering"))
            .metadata(Map.of("language", "English", "format", "Paperback"))
            .publisher(PersonDtoBuilder.create().name("Prentice Hall").build())
            .build();

    assertNotNull(book);
    assertEquals("Clean Code", book.getTitle());
    assertEquals("Robert C. Martin", book.getAuthor());
    assertEquals("978-0132350884", book.getIsbn());
    assertEquals(464, book.getPages());
    assertEquals(39.99, book.getPrice());
    assertEquals(new BigDecimal("39.99"), book.getExactPrice());
    assertTrue(book.isAvailable());
    assertEquals((byte) 5, book.getRating());
    assertEquals((short) 1, book.getEdition());
    assertEquals(1000000L, book.getSalesCount());
    assertEquals(0.15f, book.getDiscount());
    assertEquals('T', book.getCategory());
    assertEquals(publishDate, book.getPublishDate());
    assertEquals(lastUpdated, book.getLastUpdated());
    assertEquals(Optional.of("A Handbook of Agile Software Craftsmanship"), book.getSubtitle());
    assertEquals(3, book.getTags().size());
    assertEquals(2, book.getGenres().size());
    assertEquals(2, book.getMetadata().size());
    assertNotNull(book.getPublisher());
    assertEquals("Prentice Hall", book.getPublisher().getName());
  }

  @Test
  void testPartialBuilder() {
    BookDto book =
        BookDtoBuilder.create()
            .title("Effective Java")
            .author("Joshua Bloch")
            .available(false)
            .build();

    assertNotNull(book);
    assertEquals("Effective Java", book.getTitle());
    assertEquals("Joshua Bloch", book.getAuthor());
    assertFalse(book.isAvailable());
    assertEquals(0, book.getPages());
    assertEquals(0.0, book.getPrice());
  }
  
  @Test
  void testSetterOnlyBuilder() {
    BookDto book =
        BookDtoBuilder.create()
            .title("Design Patterns")
            .author("Gang of Four")
            .pages(395)
            .build();

    assertNotNull(book);
    assertEquals("Design Patterns", book.getTitle());
    assertEquals("Gang of Four", book.getAuthor());
    assertEquals(395, book.getPages());
  }
}
