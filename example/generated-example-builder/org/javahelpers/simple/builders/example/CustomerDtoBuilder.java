package org.javahelpers.simple.builders.example;

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.simple.builders.example.CustomerDto}.
 * <p>
 * This builder provides a fluent API for creating instances of org.javahelpers.simple.builders.example.CustomerDto with
 * method chaining and validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the final DTO.
 * 
 * <h4>Example:</h4>
 * 
 * <pre>{@code
 * CustomerDto result = CustomerDtoBuilder.create()
 *     .email("example value")
 *     .id(42L)
 *     .name("example value")
 *     .tags(List.of("example value"))
 *     .build();
 * }</pre>
 */
public class CustomerDtoBuilder {

  /**
   * Tracked value for <code>email</code>: email.
   */
  private TrackedValue<String> email = unsetValue();
  /**
   * Tracked value for <code>id</code>: id.
   */
  private TrackedValue<Long> id = unsetValue();
  /**
   * Tracked value for <code>name</code>: name.
   */
  private TrackedValue<String> name = unsetValue();
  /**
   * Tracked value for <code>tags</code>: tags.
   */
  private TrackedValue<List<String>> tags = unsetValue();

  /**
   * Empty constructor of builder for {@code org.javahelpers.simple.builders.example.CustomerDto}.
   */
  public CustomerDtoBuilder() {
  }

  /**
   * Initialisation of builder for {@code org.javahelpers.simple.builders.example.CustomerDto} by a instance.
   * 
   * @param instance object instance for initialisiation
   */
  public CustomerDtoBuilder(CustomerDto instance) {
    this.email = initialValue(instance.getEmail());
    this.id = initialValue(instance.getId());
    this.name = initialValue(instance.getName());
    this.tags = initialValue(instance.getTags());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.simple.builders.example.CustomerDto}.
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * CustomerDtoBuilder builder = CustomerDtoBuilder.create();
   * }</pre>
   * 
   * @return builder for {@code org.javahelpers.simple.builders.example.CustomerDto}
   */
  public static CustomerDtoBuilder create() {
    return new CustomerDtoBuilder();
  }

  /**
   * Sets the value for <code>email</code>.
   * <p>
   * Generated from setter {@link CustomerDto#setEmail(String) setEmail(String email)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.email("example value");
   * }</pre>
   * 
   * @param email email
   * @return current instance of builder
   */
  public CustomerDtoBuilder email(String email) {
    this.email = changedValue(email);
    return this;
  }

  /**
   * Sets the value for <code>id</code>.
   * <p>
   * Generated from setter {@link CustomerDto#setId(Long) setId(Long id)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.id(42L);
   * }</pre>
   * 
   * @param id id
   * @return current instance of builder
   */
  public CustomerDtoBuilder id(Long id) {
    this.id = changedValue(id);
    return this;
  }

  /**
   * Sets the value for <code>name</code>.
   * <p>
   * Generated from setter {@link CustomerDto#setName(String) setName(String name)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.name("example value");
   * }</pre>
   * 
   * @param name name
   * @return current instance of builder
   */
  public CustomerDtoBuilder name(String name) {
    this.name = changedValue(name);
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   * <p>
   * Generated from setter {@link CustomerDto#setTags(List) setTags(List<String> tags)}
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * builder.tags(List.of("example value"));
   * }</pre>
   * 
   * @param tags tags
   * @return current instance of builder
   */
  public CustomerDtoBuilder tags(List<String> tags) {
    this.tags = changedValue(tags);
    return this;
  }

  /**
   * Validates that the email field is not null or empty.
   * <p>
   * Generated from setter {@link CustomerDto#setEmail(String) setEmail(String email)}
   * 
   * @return this builder instance for chaining
   * @throws IllegalArgumentException if email is null or empty
   */
  CustomerDtoBuilder validateEmail() {
    if (!email.isSet() || email.value().trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    return this;
  }

  /**
   * Validates that the name field is not null or empty.
   * <p>
   * Generated from setter {@link CustomerDto#setName(String) setName(String name)}
   * 
   * @return this builder instance for chaining
   * @throws IllegalArgumentException if name is null or empty
   */
  CustomerDtoBuilder validateName() {
    if (!name.isSet() || name.value().trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
    return this;
  }

  /**
   * Builds the configured DTO instance.
   * 
   * <h4>Example:</h4>
   * 
   * <pre>{@code
   * CustomerDto result = builder.build();
   * }</pre>
   */
  public CustomerDto build() {
    CustomerDto result = new CustomerDto();
    this.email.ifSet(result::setEmail);
    this.id.ifSet(result::setId);
    this.name.ifSet(result::setName);
    this.tags.ifSet(result::setTags);
    return result;
  }

  /**
   * Returns a string representation of this builder, including only fields that have been set.
   * 
   * @return string representation of the builder
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE).append("email", this.email)
        .append("id", this.id)
        .append("name", this.name)
        .append("tags", this.tags)
        .toString();
  }
}