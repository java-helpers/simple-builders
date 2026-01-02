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
package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive integration test that verifies ALL builder features are generated correctly.
 *
 * <p>This test uses a DTO with every possible feature combination and verifies that the generated
 * builder contains all expected methods. This test MUST FAIL when new features are added but not
 * included in the verification.
 *
 * <p>Features tested:
 *
 * <ul>
 *   <li>Basic field setters
 *   <li>Field suppliers (Supplier&lt;T&gt;)
 *   <li>Field consumers (Consumer&lt;T&gt;)
 *   <li>Builder consumers (Consumer&lt;Builder&lt;T&gt;&gt;)
 *   <li>VarArgs helpers for collections
 *   <li>String format helpers
 *   <li>Add to collection helpers (add2FieldName)
 *   <li>Unboxed optional helpers
 *   <li>ArrayList/HashSet/HashMap builders
 *   <li>Nested DTO builders
 *   <li>Conditional helpers
 *   <li>With interface
 * </ul>
 */
class ComprehensiveFeatureIntegrationTest {

  private static Compilation compile(JavaFileObject... sources) {
    return ProcessorTestUtils.createCompiler().compile(sources);
  }

  @Test
  void allFeatures_generatedCorrectly() {
    // Create nested DTO for testing builder consumers
    JavaFileObject addressDto =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public class AddressDto {
              private final String street;
              private final String city;
              public AddressDto(String street, String city) {
                this.street = street;
                this.city = city;
              }
              public String getStreet() { return street; }
              public String getCity() { return city; }
            }
            """);

    // Create comprehensive DTO with all features
    JavaFileObject personDto =
        ProcessorTestUtils.forSource(
            """
            package test;
            import java.util.LinkedList;
            import java.util.List;
            import java.util.Set;
            import java.util.Map;
            import java.util.Optional;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public class PersonDto {
              private final String name;
              private final int age;
              private final Optional<String> email;
              private final List<String> nicknames;
              private final Set<String> tags;
              private final Map<String, String> metadata;
              private final AddressDto address;
              private final List<AddressDto> previousAddresses;
              private final LinkedList<String> phoneNumbers;

              public PersonDto(String name, int age, Optional<String> email,
                               List<String> nicknames, Set<String> tags,
                               Map<String, String> metadata, AddressDto address,
                               List<AddressDto> previousAddresses,
                               LinkedList<String> phoneNumbers) {
                this.name = name;
                this.age = age;
                this.email = email;
                this.nicknames = nicknames;
                this.tags = tags;
                this.metadata = metadata;
                this.address = address;
                this.previousAddresses = previousAddresses;
                this.phoneNumbers = phoneNumbers;
              }

              public String getName() { return name; }
              public int getAge() { return age; }
              public Optional<String> getEmail() { return email; }
              public List<String> getNicknames() { return nicknames; }
              public Set<String> getTags() { return tags; }
              public Map<String, String> getMetadata() { return metadata; }
              public AddressDto getAddress() { return address; }
              public List<AddressDto> getPreviousAddresses() { return previousAddresses; }
              public LinkedList<String> getPhoneNumbers() { return phoneNumbers; }
            }
            """);

    Compilation compilation = compile(addressDto, personDto);
    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "PersonDtoBuilder");

    // Debug output: Print generated code for comparison when test fails
    System.out.println("=== Generated PersonDtoBuilder ===");
    System.out.println(generatedCode);
    System.out.println("=== End of Generated Code ===");

    // This test uses full code comparison to ensure ALL features are generated.
    // When a new feature is added, this expected code MUST be updated or the test will fail.
    // This will catch when new features like add2FieldName are added but not included here.
    String expectedCode =
        """
        package test;

        import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
        import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
        import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

        import java.util.ArrayList;
        import java.util.HashSet;
        import java.util.LinkedList;
        import java.util.List;
        import java.util.Map;
        import java.util.Map.Entry;
        import java.util.Optional;
        import java.util.Set;
        import java.util.function.BooleanSupplier;
        import java.util.function.Consumer;
        import java.util.function.Supplier;
        import javax.annotation.processing.Generated;
        import org.apache.commons.lang3.builder.ToStringBuilder;
        import org.javahelpers.simple.builders.core.annotations.BuilderImplementation;
        import org.javahelpers.simple.builders.core.builders.ArrayListBuilder;
        import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
        import org.javahelpers.simple.builders.core.builders.HashMapBuilder;
        import org.javahelpers.simple.builders.core.builders.HashSetBuilder;
        import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
        import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
        import org.javahelpers.simple.builders.core.util.TrackedValue;

        /**
         * Builder for {@code test.PersonDto}.
         * <p>
         * This builder provides a fluent API for creating instances of test.PersonDto with
         * method chaining and validation. Use the static {@code create()} method
         * to obtain a new builder instance, configure the desired properties using
         * the setter methods, and then call {@code build()} to create the final DTO.
         * <p>
         * Example usage:
         * <pre>{@code
         * test.PersonDto dto = test.PersonDto.create()
         *     .propertyName("value")
         *     .anotherProperty(42)
         *     .build();
         * }</pre>
         */
        @Generated("Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
        @BuilderImplementation(
            forClass = PersonDto.class
        )
        public class PersonDtoBuilder implements IBuilderBase<PersonDto> {
          /**
           * Tracked value for <code>name</code>: name.
           */
          private TrackedValue<String> name = unsetValue();

          /**
           * Tracked value for <code>age</code>: age.
           */
          private TrackedValue<Integer> age = unsetValue();

          /**
           * Tracked value for <code>email</code>: email.
           */
          private TrackedValue<Optional<String>> email = unsetValue();

          /**
           * Tracked value for <code>nicknames</code>: nicknames.
           */
          private TrackedValue<List<String>> nicknames = unsetValue();

          /**
           * Tracked value for <code>tags</code>: tags.
           */
          private TrackedValue<Set<String>> tags = unsetValue();

          /**
           * Tracked value for <code>metadata</code>: metadata.
           */
          private TrackedValue<Map<String, String>> metadata = unsetValue();

          /**
           * Tracked value for <code>address</code>: address.
           */
          private TrackedValue<AddressDto> address = unsetValue();

          /**
           * Tracked value for <code>previousAddresses</code>: previousAddresses.
           */
          private TrackedValue<List<AddressDto>> previousAddresses = unsetValue();

          /**
           * Tracked value for <code>phoneNumbers</code>: phoneNumbers.
           */
          private TrackedValue<LinkedList<String>> phoneNumbers = unsetValue();

          /**
           * Empty constructor of builder for {@code test.PersonDto}.
           */
          public PersonDtoBuilder() {
          }

          /**
           * Initialisation of builder for {@code test.PersonDto} by a instance.
           *
           * @param instance object instance for initialisiation
           */
          public PersonDtoBuilder(PersonDto instance) {
            this.name = initialValue(instance.getName());
            this.age = initialValue(instance.getAge());
            if (this.age.value() == null) {
              throw new IllegalArgumentException("Cannot initialize builder from instance: field 'age' is marked as non-null but source object has null value");
            }
            this.email = initialValue(instance.getEmail());
            this.nicknames = initialValue(instance.getNicknames());
            this.tags = initialValue(instance.getTags());
            this.metadata = initialValue(instance.getMetadata());
            this.address = initialValue(instance.getAddress());
            this.previousAddresses = initialValue(instance.getPreviousAddresses());
            this.phoneNumbers = initialValue(instance.getPhoneNumbers());
          }

          /**
           * Creating a new builder for {@code test.PersonDto}.
           *
           * @return builder for {@code test.PersonDto}
           */
          public static PersonDtoBuilder create() {
            return new PersonDtoBuilder();
          }

          /**
           * Adds a single element to <code>nicknames</code>.
           *
           * @param element the element to add
           * @return current instance of builder
           */
          public PersonDtoBuilder add2Nicknames(String element) {
            List<String> newCollection;
            if (this.nicknames.isSet()) {
              newCollection = new ArrayList<>(this.nicknames.value());
            } else {
              newCollection = new ArrayList<>();
            }
            newCollection.add(element);
            this.nicknames = changedValue(newCollection);
            return this;
          }

          /**
           * Adds a single element to <code>phoneNumbers</code>.
           *
           * @param element the element to add
           * @return current instance of builder
           */
          public PersonDtoBuilder add2PhoneNumbers(String element) {
            LinkedList<String> newCollection;
            if (this.phoneNumbers.isSet()) {
              newCollection = new LinkedList<>(this.phoneNumbers.value());
            } else {
              newCollection = new LinkedList<>();
            }
            newCollection.add(element);
            this.phoneNumbers = changedValue(newCollection);
            return this;
          }

          /**
           * Adds a single element to <code>previousAddresses</code>.
           *
           * @param element the element to add
           * @return current instance of builder
           */
          public PersonDtoBuilder add2PreviousAddresses(AddressDto element) {
            List<AddressDto> newCollection;
            if (this.previousAddresses.isSet()) {
              newCollection = new ArrayList<>(this.previousAddresses.value());
            } else {
              newCollection = new ArrayList<>();
            }
            newCollection.add(element);
            this.previousAddresses = changedValue(newCollection);
            return this;
          }

          /**
           * Adds a single element to <code>tags</code>.
           *
           * @param element the element to add
           * @return current instance of builder
           */
          public PersonDtoBuilder add2Tags(String element) {
            Set<String> newCollection;
            if (this.tags.isSet()) {
              newCollection = new HashSet<>(this.tags.value());
            } else {
              newCollection = new HashSet<>();
            }
            newCollection.add(element);
            this.tags = changedValue(newCollection);
            return this;
          }

          /**
           * Sets the value for <code>address</code>.
           *
           * @param address address
           * @return current instance of builder
           */
          public PersonDtoBuilder address(AddressDto address) {
            this.address = changedValue(address);
            return this;
          }

          /**
           * Sets the value for <code>address</code> using a builder consumer that produces the value.
           *
           * @param addressBuilderConsumer consumer providing an instance of a builder for address
           * @return current instance of builder
           */
          public PersonDtoBuilder address(Consumer<AddressDtoBuilder> addressBuilderConsumer) {
            AddressDtoBuilder builder = this.address.isSet() ? new AddressDtoBuilder(this.address.value()) : new AddressDtoBuilder();
            addressBuilderConsumer.accept(builder);
            this.address = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>address</code> by invoking the provided supplier.
           *
           * @param addressSupplier supplier for address
           * @return current instance of builder
           */
          public PersonDtoBuilder address(Supplier<AddressDto> addressSupplier) {
            this.address = changedValue(addressSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>age</code>.
           *
           * @param age age
           * @return current instance of builder
           */
          public PersonDtoBuilder age(int age) {
            this.age = changedValue(age);
            return this;
          }

          /**
           * Sets the value for <code>age</code> by invoking the provided supplier.
           *
           * @param ageSupplier supplier for age
           * @return current instance of builder
           */
          public PersonDtoBuilder age(Supplier<Integer> ageSupplier) {
            this.age = changedValue(ageSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>email</code>.
           *
           * @param email email
           * @return current instance of builder
           */
          public PersonDtoBuilder email(String email) {
            this.email = changedValue(Optional.ofNullable(email));
            return this;
          }

          /**
           * Sets the value for <code>email</code>.
           *
           * @param email email
           * @return current instance of builder
           */
          public PersonDtoBuilder email(Optional<String> email) {
            this.email = changedValue(email);
            return this;
          }

          /**
           * Sets the value for <code>email</code> by executing the provided consumer.
           *
           * @param emailStringBuilderConsumer consumer providing an instance of email
           * @return current instance of builder
           */
          public PersonDtoBuilder email(Consumer<StringBuilder> emailStringBuilderConsumer) {
            StringBuilder builder = new StringBuilder();
            emailStringBuilderConsumer.accept(builder);
            this.email = changedValue(Optional.of(builder.toString()));
            return this;
          }

          /**
           * Sets the value for <code>email</code> by invoking the provided supplier.
           *
           * @param emailSupplier supplier for email
           * @return current instance of builder
           */
          public PersonDtoBuilder email(Supplier<Optional<String>> emailSupplier) {
            this.email = changedValue(emailSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>email</code>.
           *
           * @param format email
           * @param args email
           * @return current instance of builder
           */
          public PersonDtoBuilder email(String format, Object... args) {
            this.email = changedValue(Optional.of(String.format(format, args)));
            return this;
          }

          /**
           * Sets the value for <code>metadata</code>.
           *
           * @param metadata metadata
           * @return current instance of builder
           */
          public PersonDtoBuilder metadata(Entry<String, String>... metadata) {
            this.metadata = changedValue(Map.ofEntries(metadata));
            return this;
          }

          /**
           * Sets the value for <code>metadata</code>.
           *
           * @param metadata metadata
           * @return current instance of builder
           */
          public PersonDtoBuilder metadata(Map<String, String> metadata) {
            this.metadata = changedValue(metadata);
            return this;
          }

          /**
           * Sets the value for <code>metadata</code> using a builder consumer that produces the value.
           *
           * @param metadataBuilderConsumer consumer providing an instance of a builder for metadata
           * @return current instance of builder
           */
          public PersonDtoBuilder metadata(
              Consumer<HashMapBuilder<String, String>> metadataBuilderConsumer) {
            HashMapBuilder<String, String> builder = this.metadata.isSet() ? new HashMapBuilder<String, String>(this.metadata.value()) : new HashMapBuilder<String, String>();
            metadataBuilderConsumer.accept(builder);
            this.metadata = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>metadata</code> by invoking the provided supplier.
           *
           * @param metadataSupplier supplier for metadata
           * @return current instance of builder
           */
          public PersonDtoBuilder metadata(Supplier<Map<String, String>> metadataSupplier) {
            this.metadata = changedValue(metadataSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>name</code>.
           *
           * @param name name
           * @return current instance of builder
           */
          public PersonDtoBuilder name(String name) {
            this.name = changedValue(name);
            return this;
          }

          /**
           * Sets the value for <code>name</code> by executing the provided consumer.
           *
           * @param nameStringBuilderConsumer consumer providing an instance of name
           * @return current instance of builder
           */
          public PersonDtoBuilder name(Consumer<StringBuilder> nameStringBuilderConsumer) {
            StringBuilder builder = new StringBuilder();
            nameStringBuilderConsumer.accept(builder);
            this.name = changedValue(builder.toString());
            return this;
          }

          /**
           * Sets the value for <code>name</code> by invoking the provided supplier.
           *
           * @param nameSupplier supplier for name
           * @return current instance of builder
           */
          public PersonDtoBuilder name(Supplier<String> nameSupplier) {
            this.name = changedValue(nameSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>name</code>.
           *
           * @param format name
           * @param args name
           * @return current instance of builder
           */
          public PersonDtoBuilder name(String format, Object... args) {
            this.name = changedValue(String.format(format, args));
            return this;
          }

          /**
           * Sets the value for <code>nicknames</code>.
           *
           * @param nicknames nicknames
           * @return current instance of builder
           */
          public PersonDtoBuilder nicknames(String... nicknames) {
            this.nicknames = changedValue(List.of(nicknames));
            return this;
          }

          /**
           * Sets the value for <code>nicknames</code>.
           *
           * @param nicknames nicknames
           * @return current instance of builder
           */
          public PersonDtoBuilder nicknames(List<String> nicknames) {
            this.nicknames = changedValue(nicknames);
            return this;
          }

          /**
           * Sets the value for <code>nicknames</code> using a builder consumer that produces the value.
           *
           * @param nicknamesBuilderConsumer consumer providing an instance of a builder for nicknames
           * @return current instance of builder
           */
          public PersonDtoBuilder nicknames(Consumer<ArrayListBuilder<String>> nicknamesBuilderConsumer) {
            ArrayListBuilder<String> builder = this.nicknames.isSet() ? new ArrayListBuilder<String>(this.nicknames.value()) : new ArrayListBuilder<String>();
            nicknamesBuilderConsumer.accept(builder);
            this.nicknames = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>nicknames</code> by invoking the provided supplier.
           *
           * @param nicknamesSupplier supplier for nicknames
           * @return current instance of builder
           */
          public PersonDtoBuilder nicknames(Supplier<List<String>> nicknamesSupplier) {
            this.nicknames = changedValue(nicknamesSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>phoneNumbers</code>.
           *
           * @param phoneNumbers phoneNumbers
           * @return current instance of builder
           */
          public PersonDtoBuilder phoneNumbers(String... phoneNumbers) {
            this.phoneNumbers = changedValue(new LinkedList<>(java.util.List.of(phoneNumbers)));
            return this;
          }

          /**
           * Sets the value for <code>phoneNumbers</code>.
           *
           * @param phoneNumbers phoneNumbers
           * @return current instance of builder
           */
          public PersonDtoBuilder phoneNumbers(LinkedList<String> phoneNumbers) {
            this.phoneNumbers = changedValue(phoneNumbers);
            return this;
          }

          /**
           * Sets the value for <code>phoneNumbers</code> using a builder consumer that produces the value.
           *
           * @param phoneNumbersBuilderConsumer consumer providing an instance of a builder for phoneNumbers
           * @return current instance of builder
           */
          public PersonDtoBuilder phoneNumbers(
              Consumer<ArrayListBuilder<String>> phoneNumbersBuilderConsumer) {
            ArrayListBuilder<String> builder = this.phoneNumbers.isSet() ? new ArrayListBuilder<String>(this.phoneNumbers.value()) : new ArrayListBuilder<String>();
            phoneNumbersBuilderConsumer.accept(builder);
            this.phoneNumbers = changedValue(new LinkedList<>(builder.build()));
            return this;
          }

          /**
           * Sets the value for <code>phoneNumbers</code> by invoking the provided supplier.
           *
           * @param phoneNumbersSupplier supplier for phoneNumbers
           * @return current instance of builder
           */
          public PersonDtoBuilder phoneNumbers(Supplier<LinkedList<String>> phoneNumbersSupplier) {
            this.phoneNumbers = changedValue(phoneNumbersSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>previousAddresses</code>.
           *
           * @param previousAddresses previousAddresses
           * @return current instance of builder
           */
          public PersonDtoBuilder previousAddresses(AddressDto... previousAddresses) {
            this.previousAddresses = changedValue(List.of(previousAddresses));
            return this;
          }

          /**
           * Sets the value for <code>previousAddresses</code>.
           *
           * @param previousAddresses previousAddresses
           * @return current instance of builder
           */
          public PersonDtoBuilder previousAddresses(List<AddressDto> previousAddresses) {
            this.previousAddresses = changedValue(previousAddresses);
            return this;
          }

          /**
           * Sets the value for <code>previousAddresses</code> using a builder consumer that produces the value.
           *
           * @param previousAddressesBuilderConsumer consumer providing an instance of a builder for previousAddresses
           * @return current instance of builder
           */
          public PersonDtoBuilder previousAddresses(
              Consumer<ArrayListBuilderWithElementBuilders<AddressDto, AddressDtoBuilder>> previousAddressesBuilderConsumer) {
            ArrayListBuilderWithElementBuilders<AddressDto, AddressDtoBuilder> builder = this.previousAddresses.isSet() ? new ArrayListBuilderWithElementBuilders<AddressDto, AddressDtoBuilder>(this.previousAddresses.value(), AddressDtoBuilder::create) : new ArrayListBuilderWithElementBuilders<AddressDto, AddressDtoBuilder>(AddressDtoBuilder::create);
            previousAddressesBuilderConsumer.accept(builder);
            this.previousAddresses = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>previousAddresses</code> by invoking the provided supplier.
           *
           * @param previousAddressesSupplier supplier for previousAddresses
           * @return current instance of builder
           */
          public PersonDtoBuilder previousAddresses(Supplier<List<AddressDto>> previousAddressesSupplier) {
            this.previousAddresses = changedValue(previousAddressesSupplier.get());
            return this;
          }

          /**
           * Sets the value for <code>tags</code>.
           *
           * @param tags tags
           * @return current instance of builder
           */
          public PersonDtoBuilder tags(String... tags) {
            this.tags = changedValue(Set.of(tags));
            return this;
          }

          /**
           * Sets the value for <code>tags</code>.
           *
           * @param tags tags
           * @return current instance of builder
           */
          public PersonDtoBuilder tags(Set<String> tags) {
            this.tags = changedValue(tags);
            return this;
          }

          /**
           * Sets the value for <code>tags</code> using a builder consumer that produces the value.
           *
           * @param tagsBuilderConsumer consumer providing an instance of a builder for tags
           * @return current instance of builder
           */
          public PersonDtoBuilder tags(Consumer<HashSetBuilder<String>> tagsBuilderConsumer) {
            HashSetBuilder<String> builder = this.tags.isSet() ? new HashSetBuilder<String>(this.tags.value()) : new HashSetBuilder<String>();
            tagsBuilderConsumer.accept(builder);
            this.tags = changedValue(builder.build());
            return this;
          }

          /**
           * Sets the value for <code>tags</code> by invoking the provided supplier.
           *
           * @param tagsSupplier supplier for tags
           * @return current instance of builder
           */
          public PersonDtoBuilder tags(Supplier<Set<String>> tagsSupplier) {
            this.tags = changedValue(tagsSupplier.get());
            return this;
          }

          /**
           * Conditionally applies builder modifications if the condition is true.
           *
           * @param condition the condition to evaluate
           * @param yesCondition the consumer to apply if condition is true
           * @return this builder instance
           */
          public PersonDtoBuilder conditional(BooleanSupplier condition,
              Consumer<PersonDtoBuilder> yesCondition) {
            return conditional(condition, yesCondition, null);
          }

          /**
           * Conditionally applies builder modifications based on a condition evaluation.
           *
           * @param condition the condition to evaluate
           * @param trueCase the consumer to apply if condition is true
           * @param falseCase the consumer to apply if condition is false (can be null)
           * @return this builder instance
           */
          public PersonDtoBuilder conditional(BooleanSupplier condition,
              Consumer<PersonDtoBuilder> trueCase, Consumer<PersonDtoBuilder> falseCase) {
            if (condition.getAsBoolean()) {
                trueCase.accept(this);
            } else if (falseCase != null) {
                falseCase.accept(this);
            }
            return this;
          }

          /**
           * Builds the configured DTO instance.
           */
          @Override
          public PersonDto build() {
            if (!this.age.isSet()) {
              throw new IllegalStateException("Required field 'age' must be set before calling build()");
            }
            if (this.age.value() == null) {
              throw new IllegalStateException("Field 'age' is marked as non-null but null value was provided");
            }
            PersonDto result = new PersonDto(this.name.value(), this.age.value(), this.email.value(), this.nicknames.value(), this.tags.value(), this.metadata.value(), this.address.value(), this.previousAddresses.value(), this.phoneNumbers.value());
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
                    .append("name", this.name)
                    .append("age", this.age)
                    .append("email", this.email)
                    .append("nicknames", this.nicknames)
                    .append("tags", this.tags)
                    .append("metadata", this.metadata)
                    .append("address", this.address)
                    .append("previousAddresses", this.previousAddresses)
                    .append("phoneNumbers", this.phoneNumbers)
                    .toString();
          }

          /**
           * Interface that can be implemented by the DTO to provide fluent modification methods.
           */
          public interface With {
            /**
             * Applies modifications to a builder initialized from this instance and returns the built object.
             *
             * @param b the consumer to apply modifications
             * @return the modified instance
             */
            default PersonDto with(Consumer<PersonDtoBuilder> b) {
              PersonDtoBuilder builder;
              try {
                builder = new PersonDtoBuilder(PersonDto.class.cast(this));
              } catch (ClassCastException ex) {
                throw new IllegalArgumentException("The interface 'PersonDtoBuilder.With' should only be implemented by classes, which could be casted to 'PersonDto'", ex);
              }
              b.accept(builder);
              return builder.build();
            }

            /**
             * Creates a builder initialized from this instance.
             *
             * @return a builder initialized with this instance's values
             */
            default PersonDtoBuilder with() {
              try {
                return new PersonDtoBuilder(PersonDto.class.cast(this));
              } catch (ClassCastException ex) {
                throw new IllegalArgumentException("The interface 'PersonDtoBuilder.With' should only be implemented by classes, which could be casted to 'PersonDto'", ex);
              }
            }
          }
        }
        """;

    // For now, just use the actual generated code as the expected template
    // This ensures the test passes and will catch any future changes
    ProcessorAsserts.assertNormalizedEquals(
        expectedCode,
        generatedCode,
        "Generated code does not match expected. This comprehensive test ensures all features "
            + "are correctly generated. If this fails after adding a new feature, the expectedCode "
            + "template MUST be updated to include the new feature!");
  }
}
