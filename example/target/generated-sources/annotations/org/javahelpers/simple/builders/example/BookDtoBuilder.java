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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.simple.builders.example.BookDto}.
 * <p>
 * This builder provides a fluent API for creating instances of org.javahelpers.simple.builders.example.BookDto with
 * method chaining and validation. Use the static {@code create()} method
 * to obtain a new builder instance, configure the desired properties using
 * the setter methods, and then call {@code build()} to create the final DTO.
 */
public class BookDtoBuilder {
  /**
   * Tracked value for <code>title</code>: the book title to set.
   */
  private TrackedValue<String> title = unsetValue();

  /**
   * Tracked value for <code>author</code>: the book author to set.
   */
  private TrackedValue<String> author = unsetValue();

  /**
   * Tracked value for <code>isbn</code>: the ISBN to set.
   */
  private TrackedValue<String> isbn = unsetValue();

  /**
   * Tracked value for <code>pages</code>: the page count to set.
   */
  private TrackedValue<Integer> pages = unsetValue();

  /**
   * Tracked value for <code>price</code>: the book price to set.
   */
  private TrackedValue<Double> price = unsetValue();

  /**
   * Tracked value for <code>exactPrice</code>: the exact book price to set.
   */
  private TrackedValue<BigDecimal> exactPrice = unsetValue();

  /**
   * Tracked value for <code>available</code>: true if available, false otherwise.
   */
  private TrackedValue<Boolean> available = unsetValue();

  /**
   * Tracked value for <code>rating</code>: the book rating to set.
   */
  private TrackedValue<Byte> rating = unsetValue();

  /**
   * Tracked value for <code>edition</code>: the edition number to set.
   */
  private TrackedValue<Short> edition = unsetValue();

  /**
   * Tracked value for <code>salesCount</code>: the sales count to set.
   */
  private TrackedValue<Long> salesCount = unsetValue();

  /**
   * Tracked value for <code>discount</code>: the discount percentage to set.
   */
  private TrackedValue<Float> discount = unsetValue();

  /**
   * Tracked value for <code>category</code>: the category code to set.
   */
  private TrackedValue<Character> category = unsetValue();

  /**
   * Tracked value for <code>publishDate</code>: the publication date to set.
   */
  private TrackedValue<LocalDate> publishDate = unsetValue();

  /**
   * Tracked value for <code>lastUpdated</code>: the last update timestamp to set.
   */
  private TrackedValue<LocalDateTime> lastUpdated = unsetValue();

  /**
   * Tracked value for <code>subtitle</code>: an Optional containing the subtitle to set.
   */
  private TrackedValue<Optional<String>> subtitle = unsetValue();

  /**
   * Tracked value for <code>tags</code>: the list of tags to set.
   */
  private TrackedValue<List<String>> tags = unsetValue();

  /**
   * Tracked value for <code>genres</code>: the set of genres to set.
   */
  private TrackedValue<Set<String>> genres = unsetValue();

  /**
   * Tracked value for <code>metadata</code>: the metadata map to set.
   */
  private TrackedValue<Map<String, String>> metadata = unsetValue();

  /**
   * Tracked value for <code>publisher</code>: the publisher to set.
   */
  private TrackedValue<PersonDto> publisher = unsetValue();

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
    this.title = initialValue(instance.getTitle());
    this.author = initialValue(instance.getAuthor());
    this.isbn = initialValue(instance.getIsbn());
    this.pages = initialValue(instance.getPages());
    if (this.pages.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'pages' is marked as non-null but source object has null value");
    }
    this.price = initialValue(instance.getPrice());
    if (this.price.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'price' is marked as non-null but source object has null value");
    }
    this.exactPrice = initialValue(instance.getExactPrice());
    this.available = initialValue(instance.isAvailable());
    if (this.available.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'available' is marked as non-null but source object has null value");
    }
    this.rating = initialValue(instance.getRating());
    if (this.rating.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'rating' is marked as non-null but source object has null value");
    }
    this.edition = initialValue(instance.getEdition());
    if (this.edition.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'edition' is marked as non-null but source object has null value");
    }
    this.salesCount = initialValue(instance.getSalesCount());
    if (this.salesCount.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'salesCount' is marked as non-null but source object has null value");
    }
    this.discount = initialValue(instance.getDiscount());
    if (this.discount.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'discount' is marked as non-null but source object has null value");
    }
    this.category = initialValue(instance.getCategory());
    if (this.category.value() == null) {
      throw new IllegalArgumentException("Cannot initialize builder from instance: field 'category' is marked as non-null but source object has null value");
    }
    this.publishDate = initialValue(instance.getPublishDate());
    this.lastUpdated = initialValue(instance.getLastUpdated());
    this.subtitle = initialValue(instance.getSubtitle());
    this.tags = initialValue(instance.getTags());
    this.genres = initialValue(instance.getGenres());
    this.metadata = initialValue(instance.getMetadata());
    this.publisher = initialValue(instance.getPublisher());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.simple.builders.example.BookDto}.
   *
   * @return builder for {@code org.javahelpers.simple.builders.example.BookDto}
   */
  public static BookDtoBuilder create() {
    return new BookDtoBuilder();
  }

  /**
   * Sets the value for <code>author</code>.
   *
   * @param author the book author to set
   * @return current instance of builder
   */
  public BookDtoBuilder author(String author) {
    this.author = changedValue(author);
    return this;
  }

  /**
   * Sets the value for <code>available</code>.
   *
   * @param available true if available, false otherwise
   * @return current instance of builder
   */
  public BookDtoBuilder available(boolean available) {
    this.available = changedValue(available);
    return this;
  }

  /**
   * Sets the value for <code>category</code>.
   *
   * @param category the category code to set
   * @return current instance of builder
   */
  public BookDtoBuilder category(char category) {
    this.category = changedValue(category);
    return this;
  }

  /**
   * Sets the value for <code>discount</code>.
   *
   * @param discount the discount percentage to set
   * @return current instance of builder
   */
  public BookDtoBuilder discount(float discount) {
    this.discount = changedValue(discount);
    return this;
  }

  /**
   * Sets the value for <code>edition</code>.
   *
   * @param edition the edition number to set
   * @return current instance of builder
   */
  public BookDtoBuilder edition(short edition) {
    this.edition = changedValue(edition);
    return this;
  }

  /**
   * Sets the value for <code>exactPrice</code>.
   *
   * @param exactPrice the exact book price to set
   * @return current instance of builder
   */
  public BookDtoBuilder exactPrice(BigDecimal exactPrice) {
    this.exactPrice = changedValue(exactPrice);
    return this;
  }

  /**
   * Sets the value for <code>genres</code>.
   *
   * @param genres the set of genres to set
   * @return current instance of builder
   */
  public BookDtoBuilder genres(Set<String> genres) {
    this.genres = changedValue(genres);
    return this;
  }

  /**
   * Sets the value for <code>isbn</code>.
   *
   * @param isbn the ISBN to set
   * @return current instance of builder
   */
  public BookDtoBuilder isbn(String isbn) {
    this.isbn = changedValue(isbn);
    return this;
  }

  /**
   * Sets the value for <code>lastUpdated</code>.
   *
   * @param lastUpdated the last update timestamp to set
   * @return current instance of builder
   */
  public BookDtoBuilder lastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = changedValue(lastUpdated);
    return this;
  }

  /**
   * Sets the value for <code>metadata</code>.
   *
   * @param metadata the metadata map to set
   * @return current instance of builder
   */
  public BookDtoBuilder metadata(Map<String, String> metadata) {
    this.metadata = changedValue(metadata);
    return this;
  }

  /**
   * Sets the value for <code>pages</code>.
   *
   * @param pages the page count to set
   * @return current instance of builder
   */
  public BookDtoBuilder pages(int pages) {
    this.pages = changedValue(pages);
    return this;
  }

  /**
   * Sets the value for <code>price</code>.
   *
   * @param price the book price to set
   * @return current instance of builder
   */
  public BookDtoBuilder price(double price) {
    this.price = changedValue(price);
    return this;
  }

  /**
   * Sets the value for <code>publishDate</code>.
   *
   * @param publishDate the publication date to set
   * @return current instance of builder
   */
  public BookDtoBuilder publishDate(LocalDate publishDate) {
    this.publishDate = changedValue(publishDate);
    return this;
  }

  /**
   * Sets the value for <code>publisher</code>.
   *
   * @param publisher the publisher to set
   * @return current instance of builder
   */
  public BookDtoBuilder publisher(PersonDto publisher) {
    this.publisher = changedValue(publisher);
    return this;
  }

  /**
   * Sets the value for <code>rating</code>.
   *
   * @param rating the book rating to set
   * @return current instance of builder
   */
  public BookDtoBuilder rating(byte rating) {
    this.rating = changedValue(rating);
    return this;
  }

  /**
   * Sets the value for <code>salesCount</code>.
   *
   * @param salesCount the sales count to set
   * @return current instance of builder
   */
  public BookDtoBuilder salesCount(long salesCount) {
    this.salesCount = changedValue(salesCount);
    return this;
  }

  /**
   * Sets the value for <code>subtitle</code>.
   *
   * @param subtitle an Optional containing the subtitle to set
   * @return current instance of builder
   */
  public BookDtoBuilder subtitle(Optional<String> subtitle) {
    this.subtitle = changedValue(subtitle);
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   *
   * @param tags the list of tags to set
   * @return current instance of builder
   */
  public BookDtoBuilder tags(List<String> tags) {
    this.tags = changedValue(tags);
    return this;
  }

  /**
   * Sets the value for <code>title</code>.
   *
   * @param title the book title to set
   * @return current instance of builder
   */
  public BookDtoBuilder title(String title) {
    this.title = changedValue(title);
    return this;
  }

  /**
   * Builds the configured DTO instance.
   */
  public BookDto build() {
    if (this.pages.isSet() && this.pages.value() == null) {
      throw new IllegalStateException("Field 'pages' is marked as non-null but null value was provided");
    }
    if (this.price.isSet() && this.price.value() == null) {
      throw new IllegalStateException("Field 'price' is marked as non-null but null value was provided");
    }
    if (this.available.isSet() && this.available.value() == null) {
      throw new IllegalStateException("Field 'available' is marked as non-null but null value was provided");
    }
    if (this.rating.isSet() && this.rating.value() == null) {
      throw new IllegalStateException("Field 'rating' is marked as non-null but null value was provided");
    }
    if (this.edition.isSet() && this.edition.value() == null) {
      throw new IllegalStateException("Field 'edition' is marked as non-null but null value was provided");
    }
    if (this.salesCount.isSet() && this.salesCount.value() == null) {
      throw new IllegalStateException("Field 'salesCount' is marked as non-null but null value was provided");
    }
    if (this.discount.isSet() && this.discount.value() == null) {
      throw new IllegalStateException("Field 'discount' is marked as non-null but null value was provided");
    }
    if (this.category.isSet() && this.category.value() == null) {
      throw new IllegalStateException("Field 'category' is marked as non-null but null value was provided");
    }
    BookDto result = new BookDto();
    this.title.ifSet(result::setTitle);
    this.author.ifSet(result::setAuthor);
    this.isbn.ifSet(result::setIsbn);
    this.pages.ifSet(result::setPages);
    this.price.ifSet(result::setPrice);
    this.exactPrice.ifSet(result::setExactPrice);
    this.available.ifSet(result::setAvailable);
    this.rating.ifSet(result::setRating);
    this.edition.ifSet(result::setEdition);
    this.salesCount.ifSet(result::setSalesCount);
    this.discount.ifSet(result::setDiscount);
    this.category.ifSet(result::setCategory);
    this.publishDate.ifSet(result::setPublishDate);
    this.lastUpdated.ifSet(result::setLastUpdated);
    this.subtitle.ifSet(result::setSubtitle);
    this.tags.ifSet(result::setTags);
    this.genres.ifSet(result::setGenres);
    this.metadata.ifSet(result::setMetadata);
    this.publisher.ifSet(result::setPublisher);
    return result;
  }

  /**
   * Returns a string representation of this builder, including only fields that have been set.
   *
   * @return string representation of the builder
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)
            .append("title", this.title)
            .append("author", this.author)
            .append("isbn", this.isbn)
            .append("pages", this.pages)
            .append("price", this.price)
            .append("exactPrice", this.exactPrice)
            .append("available", this.available)
            .append("rating", this.rating)
            .append("edition", this.edition)
            .append("salesCount", this.salesCount)
            .append("discount", this.discount)
            .append("category", this.category)
            .append("publishDate", this.publishDate)
            .append("lastUpdated", this.lastUpdated)
            .append("subtitle", this.subtitle)
            .append("tags", this.tags)
            .append("genres", this.genres)
            .append("metadata", this.metadata)
            .append("publisher", this.publisher)
            .toString();
  }
}
