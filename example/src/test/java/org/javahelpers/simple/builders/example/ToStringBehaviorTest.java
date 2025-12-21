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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Runtime tests for toString behavior in generated builders.
 *
 * <p>Verifies that:
 *
 * <ul>
 *   <li>Only set fields appear in toString output
 *   <li>Unset fields are hidden from toString output
 *   <li>BuilderToStringStyle correctly filters TrackedValue fields
 * </ul>
 */
class ToStringBehaviorTest {

  @Test
  void toString_onlyShowsSetFields() {
    BookDtoBuilder builder = BookDtoBuilder.create().title("The Great Book").author("John Doe");

    String result = builder.toString();

    assertTrue(result.contains("title=The Great Book"), "Should show set title field: " + result);
    assertTrue(result.contains("author=John Doe"), "Should show set author field: " + result);
    assertFalse(result.contains("isbn"), "Should NOT show unset isbn field: " + result);
    assertFalse(result.contains("pages"), "Should NOT show unset pages field: " + result);
    assertFalse(result.contains("price"), "Should NOT show unset price field: " + result);
  }

  @Test
  void toString_emptyBuilderShowsNoFields() {
    BookDtoBuilder builder = BookDtoBuilder.create();

    String result = builder.toString();

    assertTrue(result.contains("BookDtoBuilder"), "Should contain builder class name: " + result);
    assertFalse(result.contains("title="), "Should NOT show unset title field: " + result);
    assertFalse(result.contains("author="), "Should NOT show unset author field: " + result);
    assertFalse(result.contains("isbn="), "Should NOT show unset isbn field: " + result);
  }

  @Test
  void toString_partiallySetFieldsOnlyShowsSet() {
    BookDtoBuilder builder =
        BookDtoBuilder.create().title("Partial Book").pages(250);

    String result = builder.toString();

    assertTrue(result.contains("title=Partial Book"), "Should show set title field: " + result);
    assertTrue(result.contains("pages=250"), "Should show set pages field: " + result);
    assertFalse(result.contains("author="), "Should NOT show unset author field: " + result);
    assertFalse(result.contains("isbn"), "Should NOT show unset isbn field: " + result);
  }

  @Test
  void toString_allFieldsSetShowsAll() {
    BookDtoBuilder builder =
        BookDtoBuilder.create()
            .title("Complete Book")
            .author("Jane Smith")
            .isbn("978-1234567890")
            .pages(350);

    String result = builder.toString();

    assertTrue(result.contains("title=Complete Book"), "Should show title: " + result);
    assertTrue(result.contains("author=Jane Smith"), "Should show author: " + result);
    assertTrue(result.contains("isbn=978-1234567890"), "Should show isbn: " + result);
    assertTrue(result.contains("pages=350"), "Should show pages: " + result);
  }

  @Test
  void toString_builderFromInstanceShowsAllFields() {
    BookDto book = new BookDto();
    book.setTitle("Instance Book");
    book.setAuthor("Bob Johnson");
    book.setIsbn("978-9876543210");
    book.setPages(400);

    BookDtoBuilder builder = new BookDtoBuilder(book);

    String result = builder.toString();

    assertTrue(result.contains("title=Instance Book"), "Should show title from instance: " + result);
    assertTrue(result.contains("author=Bob Johnson"), "Should show author from instance: " + result);
    assertTrue(result.contains("isbn=978-9876543210"), "Should show isbn from instance: " + result);
    assertTrue(result.contains("pages=400"), "Should show pages from instance: " + result);
  }

  @Test
  void toString_modifiedBuilderShowsUpdatedValues() {
    BookDtoBuilder builder =
        BookDtoBuilder.create()
            .title("Original Title")
            .author("Original Author")
            .pages(200);

    builder.title("Updated Title");

    String result = builder.toString();

    assertTrue(result.contains("title=Updated Title"), "Should show updated title: " + result);
    assertFalse(result.contains("Original Title"), "Should NOT show old title: " + result);
    assertTrue(result.contains("author=Original Author"), "Should still show author: " + result);
    assertTrue(result.contains("pages=200"), "Should still show pages: " + result);
  }

  @Test
  void toString_nullValueIsShown() {
    BookDtoBuilder builder = BookDtoBuilder.create().title("Test Book").author(null);

    String result = builder.toString();

    assertTrue(result.contains("title=Test Book"), "Should show title: " + result);
    assertTrue(
        result.contains("author=<null>") || result.contains("author=null"),
        "Should show null value for set field: " + result);
  }
}
