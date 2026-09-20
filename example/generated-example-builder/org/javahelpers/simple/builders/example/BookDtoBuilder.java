package org.javahelpers.simple.builders.example;

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.simple.builders.example.BookDto}.
 * <p>
 * This builder provides a fluent API for creating instances of org.javahelpers.simple.builders.example.BookDto with
 * method chaining and validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the final DTO.
 * 
 * <h4>Example:</h4>
 * 
 * <pre>{@code
 * BookDto result = BookDtoBuilder.create()
 *     .author("example value")
 *     .authorUpdate(String::trim)
 *     .available(true)
 *     .availableUpdate(value -> !value)
 *     .category('x')
 *     .discount(3.14f)
 *     .discountUpdate(Math::abs)
 *     .exactPrice(BigDecimal.valueOf(3.14))
 *     .genres(Set.of("example value"))
 *     .genresUpdate(Set::copyOf)
 *     .isbn("example value")
 *     .isbnUpdate(String::trim)
 *     .lastUpdated(LocalDateTime.now())
 *     .lastUpdatedUpdate(value -> value.plusDays(1))
 *     .metadata(Map.of("example value", "example value"))
 *     .metadataUpdate(Map::copyOf)
 *     .pages(42)
 *     .pagesUpdate(Math::abs)
 *     .price(3.14)
 *     .priceUpdate(Math::abs)
 *     .publishDate(LocalDate.now())
 *     .publishDateUpdate(value -> value.plusDays(1))
 *     .publisher(PersonDtoBuilder.create().build())
 *     .salesCount(42L)
 *     .salesCountUpdate(Math::abs)
 *     .subtitle(Optional.of("example value"))
 *     .tags(List.of("example value"))
 *     .tagsUpdate(List::copyOf)
 *     .title("example value")
 *     .titleUpdate(String::trim)
 *     .build();
 * }</pre>
 */
public class BookDtoBuilder {

  /**
   * Tracked value for <code>author</code>: the book author to set.
   */
  private TrackedValue<String> author = unsetValue();
  /**
   * Tracked value for <code>available</code>: true if available, false otherwise.
   */
  private TrackedValue<Boolean> available = unsetValue();
  /**
   * Tracked value for <code>category</code>: the category code to set.
   */
  private TrackedValue<Character> category = unsetValue();
  /**
   * Tracked value for <code>discount</code>: the discount percentage to set.
   */
  private TrackedValue<Float> discount = unsetValue();
  /**
   * Tracked value for <code>edition</code>: the edition number to set.
   */
  private TrackedValue<Short> edition = unsetValue();
  /**
   * Tracked value for <code>exactPrice</code>: the exact book price to set.
   */
  private TrackedValue<BigDecimal> exactPrice = unsetValue();
  /**
   * Tracked value for <code>genres</code>: the set of genres to set.
   */
  private TrackedValue<Set<String>> genres = unsetValue();
  /**
   * Tracked value for <code>isbn</code>: the ISBN to set.
   */
  private TrackedValue<String> isbn = unsetValue();
  /**
   * Tracked value for <code>lastUpdated</code>: the last update timestamp to set.
   */
  private TrackedValue<LocalDateTime> lastUpdated = unsetValue();
  /**
   * Tracked value for <code>metadata</code>: the metadata map to set.
   */
  private TrackedValue<Map<String, String>> metadata = unsetValue();
  /**
   * Tracked value for <code>pages</code>: the page count to set.
   */
  private TrackedValue<Integer> pages = unsetValue();
  /**
   * Tracked value for <code>price</code>: the book price to set.
   */
  private TrackedValue<Double> price = unsetValue();
  /**
   * Tracked value for <code>publishDate</code>: the publication date to set.
   */
  private TrackedValue<LocalDate> publishDate = unsetValue();
  /**
   * Tracked value for <code>publisher</code>: the publisher to set.
   */
  private TrackedValue<PersonDto> publisher = unsetValue();
  /**
   * Tracked value for <code>rating</code>: the book rating to set.
   */
  private TrackedValue<Byte> rating = unsetValue();
  /**
   * Tracked value for <code>salesCount</code>: the sales count to set.
   */
  private TrackedValue<Long> salesCount = unsetValue();
  /**
   * Tracked value for <code>subtitle</code>: an Optional containing the subtitle to set.
   */
  private TrackedValue<Optional<String>> subtitle = unsetValue();
  /**
   * Tracked value for <code>tags</code>: the list of tags to set.
   */
  private TrackedValue<List<String>> tags = unsetValue();
  /**
   * Tracked value for <code>title</code>: the book title to set.
   */
  private TrackedValue<String> title = unsetValue();

  /**
   * Empty constructor of builder for {@code org.javahelpers.simple.builders.example.BookDto}.
   */
  public BookDtoBuilder() {
  }

  /**
   * Initialisation of builder for {@code org.javahelpers.simple.builders.example.BookDto} by a instance.
   * 
   * @param instance object instance for initialisiation
   */
  public BookDtoBuilder(BookDto instance) {
    this.author = initialValue(instance.getAuthor());
    this.available = initialValue(instance.isAvailable());
    this.category = initialValue(instance.getCategory());
    this.discount = initialValue(instance.getDiscount());
    this.edition = initialValue(instance.getEdition());
    this.exactPrice = initialValue(instance.getExactPrice());
    this.genres = initialValue(instance.getGenres());
    this.isbn = initialValue(instance.getIsbn());
    this.lastUpdated = initialValue(instance.getLastUpdated());
    this.metadata = initialValue(instance.getMetadata());
    this.pages = initialValue(instance.getPages());
    this.price = initialValue(instance.getPrice());
    this.publishDate = initialValue(instance.getPublishDate());
    this.publisher = initialValue(instance.getPublisher());
    this.rating = initialValue(instance.getRating());
    this.salesCount = initialValue(instance.getSalesCount());
    this.subtitle = initialValue(instance.getSubtitle());
    this.tags = initialValue(instance.getTags());
    this.title = initialValue(instance.getTitle());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.simple.builders.example.BookDto}.
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * BookDtoBuilder builder = BookDtoBuilder.create();
   * }</pre>
   * 
   * @return builder for {@code org.javahelpers.simple.builders.example.BookDto}
   */
  public static BookDtoBuilder create() {
    return new BookDtoBuilder();
  }

  /**
   * Sets the value for <code>author</code>.
   * <p>
   * Generated from setter {@link BookDto#setAuthor(String) setAuthor(String author)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.author("example value");
   * }</pre>
   * 
   * @param author the book author to set
   * @return current instance of builder
   */
  public BookDtoBuilder author(String author) {
    this.author = changedValue(author);
    return this;
  }

  /**
   * Updates the current value of <code>author</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setAuthor(String) setAuthor(String author)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.author("example value").authorUpdate(String::trim);
   * }</pre>
   * 
   * @param authorUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>author</code> has not been set yet
   */
  public BookDtoBuilder authorUpdate(UnaryOperator<String> authorUpdater) {
    if (!this.author.isSet()) {
      throw new IllegalStateException("Cannot update 'author' before it is set");
    }
    this.author = changedValue(authorUpdater.apply(this.author.value()));
    return this;
  }

  /**
   * Sets the value for <code>available</code>.
   * <p>
   * Generated from setter {@link BookDto#setAvailable(boolean) setAvailable(boolean available)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.available(true);
   * }</pre>
   * 
   * @param available true if available, false otherwise
   * @return current instance of builder
   */
  public BookDtoBuilder available(boolean available) {
    this.available = changedValue(available);
    return this;
  }

  /**
   * Updates the current value of <code>available</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setAvailable(boolean) setAvailable(boolean available)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.available(true).availableUpdate(value -> !value);
   * }</pre>
   * 
   * @param availableUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>available</code> has not been set yet
   */
  public BookDtoBuilder availableUpdate(UnaryOperator<Boolean> availableUpdater) {
    if (!this.available.isSet()) {
      throw new IllegalStateException("Cannot update 'available' before it is set");
    }
    this.available = changedValue(availableUpdater.apply(this.available.value()));
    return this;
  }

  /**
   * Sets the value for <code>category</code>.
   * <p>
   * Generated from setter {@link BookDto#setCategory(char) setCategory(char category)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.category('x');
   * }</pre>
   * 
   * @param category the category code to set
   * @return current instance of builder
   */
  public BookDtoBuilder category(char category) {
    this.category = changedValue(category);
    return this;
  }

  /**
   * Updates the current value of <code>category</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setCategory(char) setCategory(char category)}
   * 
   * @param categoryUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>category</code> has not been set yet
   */
  public BookDtoBuilder categoryUpdate(UnaryOperator<Character> categoryUpdater) {
    if (!this.category.isSet()) {
      throw new IllegalStateException("Cannot update 'category' before it is set");
    }
    this.category = changedValue(categoryUpdater.apply(this.category.value()));
    return this;
  }

  /**
   * Sets the value for <code>discount</code>.
   * <p>
   * Generated from setter {@link BookDto#setDiscount(float) setDiscount(float discount)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.discount(3.14f);
   * }</pre>
   * 
   * @param discount the discount percentage to set
   * @return current instance of builder
   */
  public BookDtoBuilder discount(float discount) {
    this.discount = changedValue(discount);
    return this;
  }

  /**
   * Updates the current value of <code>discount</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setDiscount(float) setDiscount(float discount)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.discount(3.14f).discountUpdate(Math::abs);
   * }</pre>
   * 
   * @param discountUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>discount</code> has not been set yet
   */
  public BookDtoBuilder discountUpdate(UnaryOperator<Float> discountUpdater) {
    if (!this.discount.isSet()) {
      throw new IllegalStateException("Cannot update 'discount' before it is set");
    }
    this.discount = changedValue(discountUpdater.apply(this.discount.value()));
    return this;
  }

  /**
   * Sets the value for <code>edition</code>.
   * <p>
   * Generated from setter {@link BookDto#setEdition(short) setEdition(short edition)}
   * 
   * @param edition the edition number to set
   * @return current instance of builder
   */
  public BookDtoBuilder edition(short edition) {
    this.edition = changedValue(edition);
    return this;
  }

  /**
   * Updates the current value of <code>edition</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setEdition(short) setEdition(short edition)}
   * 
   * @param editionUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>edition</code> has not been set yet
   */
  public BookDtoBuilder editionUpdate(UnaryOperator<Short> editionUpdater) {
    if (!this.edition.isSet()) {
      throw new IllegalStateException("Cannot update 'edition' before it is set");
    }
    this.edition = changedValue(editionUpdater.apply(this.edition.value()));
    return this;
  }

  /**
   * Sets the value for <code>exactPrice</code>.
   * <p>
   * Generated from setter {@link BookDto#setExactPrice(BigDecimal) setExactPrice(BigDecimal exactPrice)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.exactPrice(BigDecimal.valueOf(3.14));
   * }</pre>
   * 
   * @param exactPrice the exact book price to set
   * @return current instance of builder
   */
  public BookDtoBuilder exactPrice(BigDecimal exactPrice) {
    this.exactPrice = changedValue(exactPrice);
    return this;
  }

  /**
   * Updates the current value of <code>exactPrice</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setExactPrice(BigDecimal) setExactPrice(BigDecimal exactPrice)}
   * 
   * @param exactPriceUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>exactPrice</code> has not been set yet
   */
  public BookDtoBuilder exactPriceUpdate(UnaryOperator<BigDecimal> exactPriceUpdater) {
    if (!this.exactPrice.isSet()) {
      throw new IllegalStateException("Cannot update 'exactPrice' before it is set");
    }
    this.exactPrice = changedValue(exactPriceUpdater.apply(this.exactPrice.value()));
    return this;
  }

  /**
   * Sets the value for <code>genres</code>.
   * <p>
   * Generated from setter {@link BookDto#setGenres(Set) setGenres(Set<String> genres)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.genres(Set.of("example value"));
   * }</pre>
   * 
   * @param genres the set of genres to set
   * @return current instance of builder
   */
  public BookDtoBuilder genres(Set<String> genres) {
    this.genres = changedValue(genres);
    return this;
  }

  /**
   * Updates the current value of <code>genres</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setGenres(Set) setGenres(Set<String> genres)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.genres(Set.of("example value")).genresUpdate(Set::copyOf);
   * }</pre>
   * 
   * @param genresUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>genres</code> has not been set yet
   */
  public BookDtoBuilder genresUpdate(UnaryOperator<Set<String>> genresUpdater) {
    if (!this.genres.isSet()) {
      throw new IllegalStateException("Cannot update 'genres' before it is set");
    }
    this.genres = changedValue(genresUpdater.apply(this.genres.value()));
    return this;
  }

  /**
   * Sets the value for <code>isbn</code>.
   * <p>
   * Generated from setter {@link BookDto#setIsbn(String) setIsbn(String isbn)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.isbn("example value");
   * }</pre>
   * 
   * @param isbn the ISBN to set
   * @return current instance of builder
   */
  public BookDtoBuilder isbn(String isbn) {
    this.isbn = changedValue(isbn);
    return this;
  }

  /**
   * Updates the current value of <code>isbn</code> in place by applying the given operator, instead of reading it out,
   * changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setIsbn(String) setIsbn(String isbn)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.isbn("example value").isbnUpdate(String::trim);
   * }</pre>
   * 
   * @param isbnUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>isbn</code> has not been set yet
   */
  public BookDtoBuilder isbnUpdate(UnaryOperator<String> isbnUpdater) {
    if (!this.isbn.isSet()) {
      throw new IllegalStateException("Cannot update 'isbn' before it is set");
    }
    this.isbn = changedValue(isbnUpdater.apply(this.isbn.value()));
    return this;
  }

  /**
   * Sets the value for <code>lastUpdated</code>.
   * <p>
   * Generated from setter {@link BookDto#setLastUpdated(LocalDateTime) setLastUpdated(LocalDateTime lastUpdated)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.lastUpdated(LocalDateTime.now());
   * }</pre>
   * 
   * @param lastUpdated the last update timestamp to set
   * @return current instance of builder
   */
  public BookDtoBuilder lastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = changedValue(lastUpdated);
    return this;
  }

  /**
   * Updates the current value of <code>lastUpdated</code> in place by applying the given operator, instead of reading
   * it out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setLastUpdated(LocalDateTime) setLastUpdated(LocalDateTime lastUpdated)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.lastUpdated(LocalDateTime.now()).lastUpdatedUpdate(value -> value.plusDays(1));
   * }</pre>
   * 
   * @param lastUpdatedUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>lastUpdated</code> has not been set yet
   */
  public BookDtoBuilder lastUpdatedUpdate(UnaryOperator<LocalDateTime> lastUpdatedUpdater) {
    if (!this.lastUpdated.isSet()) {
      throw new IllegalStateException("Cannot update 'lastUpdated' before it is set");
    }
    this.lastUpdated = changedValue(lastUpdatedUpdater.apply(this.lastUpdated.value()));
    return this;
  }

  /**
   * Sets the value for <code>metadata</code>.
   * <p>
   * Generated from setter {@link BookDto#setMetadata(Map) setMetadata(Map<String, String> metadata)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.metadata(Map.of("example value", "example value"));
   * }</pre>
   * 
   * @param metadata the metadata map to set
   * @return current instance of builder
   */
  public BookDtoBuilder metadata(Map<String, String> metadata) {
    this.metadata = changedValue(metadata);
    return this;
  }

  /**
   * Updates the current value of <code>metadata</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setMetadata(Map) setMetadata(Map<String, String> metadata)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.metadata(Map.of("example value", "example value")).metadataUpdate(Map::copyOf);
   * }</pre>
   * 
   * @param metadataUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>metadata</code> has not been set yet
   */
  public BookDtoBuilder metadataUpdate(UnaryOperator<Map<String, String>> metadataUpdater) {
    if (!this.metadata.isSet()) {
      throw new IllegalStateException("Cannot update 'metadata' before it is set");
    }
    this.metadata = changedValue(metadataUpdater.apply(this.metadata.value()));
    return this;
  }

  /**
   * Sets the value for <code>pages</code>.
   * <p>
   * Generated from setter {@link BookDto#setPages(int) setPages(int pages)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.pages(42);
   * }</pre>
   * 
   * @param pages the page count to set
   * @return current instance of builder
   */
  public BookDtoBuilder pages(int pages) {
    this.pages = changedValue(pages);
    return this;
  }

  /**
   * Updates the current value of <code>pages</code> in place by applying the given operator, instead of reading it out,
   * changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setPages(int) setPages(int pages)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.pages(42).pagesUpdate(Math::abs);
   * }</pre>
   * 
   * @param pagesUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>pages</code> has not been set yet
   */
  public BookDtoBuilder pagesUpdate(UnaryOperator<Integer> pagesUpdater) {
    if (!this.pages.isSet()) {
      throw new IllegalStateException("Cannot update 'pages' before it is set");
    }
    this.pages = changedValue(pagesUpdater.apply(this.pages.value()));
    return this;
  }

  /**
   * Sets the value for <code>price</code>.
   * <p>
   * Generated from setter {@link BookDto#setPrice(double) setPrice(double price)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.price(3.14);
   * }</pre>
   * 
   * @param price the book price to set
   * @return current instance of builder
   */
  public BookDtoBuilder price(double price) {
    this.price = changedValue(price);
    return this;
  }

  /**
   * Updates the current value of <code>price</code> in place by applying the given operator, instead of reading it out,
   * changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setPrice(double) setPrice(double price)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.price(3.14).priceUpdate(Math::abs);
   * }</pre>
   * 
   * @param priceUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>price</code> has not been set yet
   */
  public BookDtoBuilder priceUpdate(UnaryOperator<Double> priceUpdater) {
    if (!this.price.isSet()) {
      throw new IllegalStateException("Cannot update 'price' before it is set");
    }
    this.price = changedValue(priceUpdater.apply(this.price.value()));
    return this;
  }

  /**
   * Sets the value for <code>publishDate</code>.
   * <p>
   * Generated from setter {@link BookDto#setPublishDate(LocalDate) setPublishDate(LocalDate publishDate)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.publishDate(LocalDate.now());
   * }</pre>
   * 
   * @param publishDate the publication date to set
   * @return current instance of builder
   */
  public BookDtoBuilder publishDate(LocalDate publishDate) {
    this.publishDate = changedValue(publishDate);
    return this;
  }

  /**
   * Updates the current value of <code>publishDate</code> in place by applying the given operator, instead of reading
   * it out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setPublishDate(LocalDate) setPublishDate(LocalDate publishDate)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.publishDate(LocalDate.now()).publishDateUpdate(value -> value.plusDays(1));
   * }</pre>
   * 
   * @param publishDateUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>publishDate</code> has not been set yet
   */
  public BookDtoBuilder publishDateUpdate(UnaryOperator<LocalDate> publishDateUpdater) {
    if (!this.publishDate.isSet()) {
      throw new IllegalStateException("Cannot update 'publishDate' before it is set");
    }
    this.publishDate = changedValue(publishDateUpdater.apply(this.publishDate.value()));
    return this;
  }

  /**
   * Sets the value for <code>publisher</code>.
   * <p>
   * Generated from setter {@link BookDto#setPublisher(PersonDto) setPublisher(PersonDto publisher)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.publisher(PersonDtoBuilder.create().build());
   * }</pre>
   * 
   * @param publisher the publisher to set
   * @return current instance of builder
   */
  public BookDtoBuilder publisher(PersonDto publisher) {
    this.publisher = changedValue(publisher);
    return this;
  }

  /**
   * Updates the current value of <code>publisher</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance). For changing multiple values of a nested
   * DTO, prefer the corresponding builder-consumer helper.
   * <p>
   * Generated from setter {@link BookDto#setPublisher(PersonDto) setPublisher(PersonDto publisher)}
   * 
   * @param publisherUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>publisher</code> has not been set yet
   */
  public BookDtoBuilder publisherUpdate(UnaryOperator<PersonDto> publisherUpdater) {
    if (!this.publisher.isSet()) {
      throw new IllegalStateException("Cannot update 'publisher' before it is set");
    }
    this.publisher = changedValue(publisherUpdater.apply(this.publisher.value()));
    return this;
  }

  /**
   * Sets the value for <code>rating</code>.
   * <p>
   * Generated from setter {@link BookDto#setRating(byte) setRating(byte rating)}
   * 
   * @param rating the book rating to set
   * @return current instance of builder
   */
  public BookDtoBuilder rating(byte rating) {
    this.rating = changedValue(rating);
    return this;
  }

  /**
   * Updates the current value of <code>rating</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setRating(byte) setRating(byte rating)}
   * 
   * @param ratingUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>rating</code> has not been set yet
   */
  public BookDtoBuilder ratingUpdate(UnaryOperator<Byte> ratingUpdater) {
    if (!this.rating.isSet()) {
      throw new IllegalStateException("Cannot update 'rating' before it is set");
    }
    this.rating = changedValue(ratingUpdater.apply(this.rating.value()));
    return this;
  }

  /**
   * Sets the value for <code>salesCount</code>.
   * <p>
   * Generated from setter {@link BookDto#setSalesCount(long) setSalesCount(long salesCount)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.salesCount(42L);
   * }</pre>
   * 
   * @param salesCount the sales count to set
   * @return current instance of builder
   */
  public BookDtoBuilder salesCount(long salesCount) {
    this.salesCount = changedValue(salesCount);
    return this;
  }

  /**
   * Updates the current value of <code>salesCount</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setSalesCount(long) setSalesCount(long salesCount)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.salesCount(42L).salesCountUpdate(Math::abs);
   * }</pre>
   * 
   * @param salesCountUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>salesCount</code> has not been set yet
   */
  public BookDtoBuilder salesCountUpdate(UnaryOperator<Long> salesCountUpdater) {
    if (!this.salesCount.isSet()) {
      throw new IllegalStateException("Cannot update 'salesCount' before it is set");
    }
    this.salesCount = changedValue(salesCountUpdater.apply(this.salesCount.value()));
    return this;
  }

  /**
   * Sets the value for <code>subtitle</code>.
   * <p>
   * Generated from setter {@link BookDto#setSubtitle(Optional) setSubtitle(Optional<String> subtitle)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.subtitle(Optional.of("example value"));
   * }</pre>
   * 
   * @param subtitle an Optional containing the subtitle to set
   * @return current instance of builder
   */
  public BookDtoBuilder subtitle(Optional<String> subtitle) {
    this.subtitle = changedValue(subtitle);
    return this;
  }

  /**
   * Updates the current value of <code>subtitle</code> in place by applying the given operator, instead of reading it
   * out, changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setSubtitle(Optional) setSubtitle(Optional<String> subtitle)}
   * 
   * @param subtitleUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>subtitle</code> has not been set yet
   */
  public BookDtoBuilder subtitleUpdate(UnaryOperator<Optional<String>> subtitleUpdater) {
    if (!this.subtitle.isSet()) {
      throw new IllegalStateException("Cannot update 'subtitle' before it is set");
    }
    this.subtitle = changedValue(subtitleUpdater.apply(this.subtitle.value()));
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   * <p>
   * Generated from setter {@link BookDto#setTags(List) setTags(List<String> tags)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.tags(List.of("example value"));
   * }</pre>
   * 
   * @param tags the list of tags to set
   * @return current instance of builder
   */
  public BookDtoBuilder tags(List<String> tags) {
    this.tags = changedValue(tags);
    return this;
  }

  /**
   * Updates the current value of <code>tags</code> in place by applying the given operator, instead of reading it out,
   * changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setTags(List) setTags(List<String> tags)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.tags(List.of("example value")).tagsUpdate(List::copyOf);
   * }</pre>
   * 
   * @param tagsUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>tags</code> has not been set yet
   */
  public BookDtoBuilder tagsUpdate(UnaryOperator<List<String>> tagsUpdater) {
    if (!this.tags.isSet()) {
      throw new IllegalStateException("Cannot update 'tags' before it is set");
    }
    this.tags = changedValue(tagsUpdater.apply(this.tags.value()));
    return this;
  }

  /**
   * Sets the value for <code>title</code>.
   * <p>
   * Generated from setter {@link BookDto#setTitle(String) setTitle(String title)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.title("example value");
   * }</pre>
   * 
   * @param title the book title to set
   * @return current instance of builder
   */
  public BookDtoBuilder title(String title) {
    this.title = changedValue(title);
    return this;
  }

  /**
   * Updates the current value of <code>title</code> in place by applying the given operator, instead of reading it out,
   * changing it and setting it again. Useful for adjustments relative to the current value, e.g. trimming,
   * upper-casing, clamping or incrementing, and in combination with the <code>With</code> copy-and-modify flow. The
   * value must have been set before (directly or via an existing instance).
   * <p>
   * Generated from setter {@link BookDto#setTitle(String) setTitle(String title)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.title("example value").titleUpdate(String::trim);
   * }</pre>
   * 
   * @param titleUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>title</code> has not been set yet
   */
  public BookDtoBuilder titleUpdate(UnaryOperator<String> titleUpdater) {
    if (!this.title.isSet()) {
      throw new IllegalStateException("Cannot update 'title' before it is set");
    }
    this.title = changedValue(titleUpdater.apply(this.title.value()));
    return this;
  }

  /**
   * Validates that the author field is not null or empty.
   * <p>
   * Generated from setter {@link BookDto#setAuthor(String) setAuthor(String author)}
   * 
   * @return this builder instance for chaining
   * @throws IllegalArgumentException if author is null or empty
   */
  BookDtoBuilder validateAuthor() {
    if (!author.isSet() || author.value().trim().isEmpty()) {
      throw new IllegalArgumentException("Author cannot be null or empty");
    }
    return this;
  }

  /**
   * Validates that the isbn field is not null or empty.
   * <p>
   * Generated from setter {@link BookDto#setIsbn(String) setIsbn(String isbn)}
   * 
   * @return this builder instance for chaining
   * @throws IllegalArgumentException if isbn is null or empty
   */
  BookDtoBuilder validateIsbn() {
    if (!isbn.isSet() || isbn.value().trim().isEmpty()) {
      throw new IllegalArgumentException("Isbn cannot be null or empty");
    }
    return this;
  }

  /**
   * Validates that the title field is not null or empty.
   * <p>
   * Generated from setter {@link BookDto#setTitle(String) setTitle(String title)}
   * 
   * @return this builder instance for chaining
   * @throws IllegalArgumentException if title is null or empty
   */
  BookDtoBuilder validateTitle() {
    if (!title.isSet() || title.value().trim().isEmpty()) {
      throw new IllegalArgumentException("Title cannot be null or empty");
    }
    return this;
  }

  /**
   * Builds the configured DTO instance.
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * BookDto result = builder.build();
   * }</pre>
   */
  public BookDto build() {
    if (this.available.isSet() && this.available.value() == null) {
      throw new IllegalStateException("Field 'available' is marked as non-null but null value was provided");
    }
    if (this.category.isSet() && this.category.value() == null) {
      throw new IllegalStateException("Field 'category' is marked as non-null but null value was provided");
    }
    if (this.discount.isSet() && this.discount.value() == null) {
      throw new IllegalStateException("Field 'discount' is marked as non-null but null value was provided");
    }
    if (this.edition.isSet() && this.edition.value() == null) {
      throw new IllegalStateException("Field 'edition' is marked as non-null but null value was provided");
    }
    if (this.pages.isSet() && this.pages.value() == null) {
      throw new IllegalStateException("Field 'pages' is marked as non-null but null value was provided");
    }
    if (this.price.isSet() && this.price.value() == null) {
      throw new IllegalStateException("Field 'price' is marked as non-null but null value was provided");
    }
    if (this.rating.isSet() && this.rating.value() == null) {
      throw new IllegalStateException("Field 'rating' is marked as non-null but null value was provided");
    }
    if (this.salesCount.isSet() && this.salesCount.value() == null) {
      throw new IllegalStateException("Field 'salesCount' is marked as non-null but null value was provided");
    }
    BookDto result = new BookDto();
    this.author.ifSet(result::setAuthor);
    this.available.ifSet(result::setAvailable);
    this.category.ifSet(result::setCategory);
    this.discount.ifSet(result::setDiscount);
    this.edition.ifSet(result::setEdition);
    this.exactPrice.ifSet(result::setExactPrice);
    this.genres.ifSet(result::setGenres);
    this.isbn.ifSet(result::setIsbn);
    this.lastUpdated.ifSet(result::setLastUpdated);
    this.metadata.ifSet(result::setMetadata);
    this.pages.ifSet(result::setPages);
    this.price.ifSet(result::setPrice);
    this.publishDate.ifSet(result::setPublishDate);
    this.publisher.ifSet(result::setPublisher);
    this.rating.ifSet(result::setRating);
    this.salesCount.ifSet(result::setSalesCount);
    this.subtitle.ifSet(result::setSubtitle);
    this.tags.ifSet(result::setTags);
    this.title.ifSet(result::setTitle);
    return result;
  }

  /**
   * Returns a string representation of this builder, including only fields that have been set.
   * 
   * @return string representation of the builder
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE).append("author", this.author)
        .append("available", this.available)
        .append("category", this.category)
        .append("discount", this.discount)
        .append("edition", this.edition)
        .append("exactPrice", this.exactPrice)
        .append("genres", this.genres)
        .append("isbn", this.isbn)
        .append("lastUpdated", this.lastUpdated)
        .append("metadata", this.metadata)
        .append("pages", this.pages)
        .append("price", this.price)
        .append("publishDate", this.publishDate)
        .append("publisher", this.publisher)
        .append("rating", this.rating)
        .append("salesCount", this.salesCount)
        .append("subtitle", this.subtitle)
        .append("tags", this.tags)
        .append("title", this.title)
        .toString();
  }
}