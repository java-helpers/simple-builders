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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Elementary builder example demonstrating all fundamental Java property types.
 *
 * <p>This DTO showcases the {@link ElementaryBuilder} annotation which generates a setter-only
 * builder without advanced features like suppliers, consumers, or collection builders.
 *
 * <p>Supported property types include:
 *
 * <ul>
 *   <li>Primitive types: int, double, boolean, byte, short, long, float, char
 *   <li>String types: title, author, isbn
 *   <li>BigDecimal for precise decimal values
 *   <li>Date/Time types: LocalDate, LocalDateTime
 *   <li>Optional types for nullable values
 *   <li>Collections: List, Set, Map
 *   <li>Complex objects: PersonDto
 * </ul>
 */
@ElementaryBuilder
public class BookDto {
  private String title;
  private String author;
  private String isbn;
  private int pages;
  private double price;
  private BigDecimal exactPrice;
  private boolean available;
  private byte rating;
  private short edition;
  private long salesCount;
  private float discount;
  private char category;
  private LocalDate publishDate;
  private LocalDateTime lastUpdated;
  private Optional<String> subtitle;
  private List<String> tags;
  private Set<String> genres;
  private Map<String, String> metadata;
  private PersonDto publisher;

  /**
   * Gets the title of the book.
   *
   * @return the book title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of the book.
   *
   * @param title the book title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the author of the book.
   *
   * @return the book author
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Sets the author of the book.
   *
   * @param author the book author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Gets the ISBN (International Standard Book Number) of the book.
   *
   * @return the ISBN
   */
  public String getIsbn() {
    return isbn;
  }

  /**
   * Sets the ISBN (International Standard Book Number) of the book.
   *
   * @param isbn the ISBN to set
   */
  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  /**
   * Gets the number of pages in the book.
   *
   * @return the page count
   */
  public int getPages() {
    return pages;
  }

  /**
   * Sets the number of pages in the book.
   *
   * @param pages the page count to set
   */
  public void setPages(int pages) {
    this.pages = pages;
  }

  /**
   * Gets the price of the book as a double.
   *
   * @return the book price
   */
  public double getPrice() {
    return price;
  }

  /**
   * Sets the price of the book as a double.
   *
   * @param price the book price to set
   */
  public void setPrice(double price) {
    this.price = price;
  }

  /**
   * Gets the exact price of the book as a BigDecimal for precise decimal calculations.
   *
   * @return the exact book price
   */
  public BigDecimal getExactPrice() {
    return exactPrice;
  }

  /**
   * Sets the exact price of the book as a BigDecimal for precise decimal calculations.
   *
   * @param exactPrice the exact book price to set
   */
  public void setExactPrice(BigDecimal exactPrice) {
    this.exactPrice = exactPrice;
  }

  /**
   * Checks if the book is available for purchase or loan.
   *
   * @return true if available, false otherwise
   */
  public boolean isAvailable() {
    return available;
  }

  /**
   * Sets the availability status of the book.
   *
   * @param available true if available, false otherwise
   */
  public void setAvailable(boolean available) {
    this.available = available;
  }

  /**
   * Gets the rating of the book (typically 1-5).
   *
   * @return the book rating
   */
  public byte getRating() {
    return rating;
  }

  /**
   * Sets the rating of the book (typically 1-5).
   *
   * @param rating the book rating to set
   */
  public void setRating(byte rating) {
    this.rating = rating;
  }

  /**
   * Gets the edition number of the book.
   *
   * @return the edition number
   */
  public short getEdition() {
    return edition;
  }

  /**
   * Sets the edition number of the book.
   *
   * @param edition the edition number to set
   */
  public void setEdition(short edition) {
    this.edition = edition;
  }

  /**
   * Gets the total number of copies sold.
   *
   * @return the sales count
   */
  public long getSalesCount() {
    return salesCount;
  }

  /**
   * Sets the total number of copies sold.
   *
   * @param salesCount the sales count to set
   */
  public void setSalesCount(long salesCount) {
    this.salesCount = salesCount;
  }

  /**
   * Gets the discount percentage applied to the book (e.g., 0.15 for 15% off).
   *
   * @return the discount percentage
   */
  public float getDiscount() {
    return discount;
  }

  /**
   * Sets the discount percentage applied to the book (e.g., 0.15 for 15% off).
   *
   * @param discount the discount percentage to set
   */
  public void setDiscount(float discount) {
    this.discount = discount;
  }

  /**
   * Gets the category code of the book (e.g., 'T' for Technical, 'F' for Fiction).
   *
   * @return the category code
   */
  public char getCategory() {
    return category;
  }

  /**
   * Sets the category code of the book (e.g., 'T' for Technical, 'F' for Fiction).
   *
   * @param category the category code to set
   */
  public void setCategory(char category) {
    this.category = category;
  }

  /**
   * Gets the publication date of the book.
   *
   * @return the publication date
   */
  public LocalDate getPublishDate() {
    return publishDate;
  }

  /**
   * Sets the publication date of the book.
   *
   * @param publishDate the publication date to set
   */
  public void setPublishDate(LocalDate publishDate) {
    this.publishDate = publishDate;
  }

  /**
   * Gets the timestamp when the book information was last updated.
   *
   * @return the last update timestamp
   */
  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  /**
   * Sets the timestamp when the book information was last updated.
   *
   * @param lastUpdated the last update timestamp to set
   */
  public void setLastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  /**
   * Gets the optional subtitle of the book.
   *
   * @return an Optional containing the subtitle, or empty if no subtitle exists
   */
  public Optional<String> getSubtitle() {
    return subtitle;
  }

  /**
   * Sets the optional subtitle of the book.
   *
   * @param subtitle an Optional containing the subtitle to set
   */
  public void setSubtitle(Optional<String> subtitle) {
    this.subtitle = subtitle;
  }

  /**
   * Gets the list of tags associated with the book (e.g., "programming", "best-practices").
   *
   * @return the list of tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * Sets the list of tags associated with the book.
   *
   * @param tags the list of tags to set
   */
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * Gets the set of genres the book belongs to (e.g., "Technical", "Software Engineering").
   *
   * @return the set of genres
   */
  public Set<String> getGenres() {
    return genres;
  }

  /**
   * Sets the set of genres the book belongs to.
   *
   * @param genres the set of genres to set
   */
  public void setGenres(Set<String> genres) {
    this.genres = genres;
  }

  /**
   * Gets the metadata map containing additional book information (e.g., language, format).
   *
   * @return the metadata map
   */
  public Map<String, String> getMetadata() {
    return metadata;
  }

  /**
   * Sets the metadata map containing additional book information.
   *
   * @param metadata the metadata map to set
   */
  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  /**
   * Gets the publisher information as a PersonDto.
   *
   * @return the publisher
   */
  public PersonDto getPublisher() {
    return publisher;
  }

  /**
   * Sets the publisher information.
   *
   * @param publisher the publisher to set
   */
  public void setPublisher(PersonDto publisher) {
    this.publisher = publisher;
  }
}
