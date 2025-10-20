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

package org.javahelpers.simple.builders.core.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.junit.jupiter.api.Test;

class HashSetBuilderWithElementBuildersTest {

  @Test
  void shouldCreateEmptyBuilder() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);
    Set<TestPerson> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCreateBuilderWithInitialSet() {
    Set<TestPerson> initial =
        new HashSet<>(Arrays.asList(new TestPerson("Alice", 30), new TestPerson("Bob", 25)));

    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(initial, TestPersonBuilder::new);
    Set<TestPerson> result = builder.build();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(new TestPerson("Alice", 30)));
    assertTrue(result.contains(new TestPerson("Bob", 25)));
  }

  @Test
  void shouldAddSingleElementViaConsumer() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder.add(person -> person.name("John").age(35));
    Set<TestPerson> result = builder.build();

    assertEquals(1, result.size());
    assertTrue(result.contains(new TestPerson("John", 35)));
  }

  @Test
  void shouldAddMultipleElementsViaConsumers() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder
        .add(person -> person.name("Alice").age(30))
        .add(person -> person.name("Bob").age(25))
        .add(person -> person.name("Charlie").age(40));

    Set<TestPerson> result = builder.build();

    assertEquals(3, result.size());
    assertTrue(result.contains(new TestPerson("Alice", 30)));
    assertTrue(result.contains(new TestPerson("Bob", 25)));
    assertTrue(result.contains(new TestPerson("Charlie", 40)));
  }

  @Test
  void shouldAddMultipleElementsViaVarargs() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder.add(
        person -> person.name("Alice").age(30),
        person -> person.name("Bob").age(25),
        person -> person.name("Charlie").age(40));

    Set<TestPerson> result = builder.build();

    assertEquals(3, result.size());
    assertTrue(result.contains(new TestPerson("Alice", 30)));
    assertTrue(result.contains(new TestPerson("Bob", 25)));
    assertTrue(result.contains(new TestPerson("Charlie", 40)));
  }

  @Test
  void shouldCombineInitialSetAndConsumers() {
    Set<TestPerson> initial = new HashSet<>(Arrays.asList(new TestPerson("Initial", 20)));

    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(initial, TestPersonBuilder::new);

    builder.add(person -> person.name("Added").age(30));

    Set<TestPerson> result = builder.build();

    assertEquals(2, result.size());
    assertTrue(result.contains(new TestPerson("Initial", 20)));
    assertTrue(result.contains(new TestPerson("Added", 30)));
  }

  @Test
  void shouldReturnThisForFluentInterface() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);

    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> result =
        builder.add(person -> person.name("Test").age(25));

    assertEquals(builder, result);
  }

  @Test
  void shouldAllowMixingConsumerAndDirectAdd() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder.add(person -> person.name("Consumer").age(30)).add(new TestPerson("Direct", 25));

    Set<TestPerson> result = builder.build();

    assertEquals(2, result.size());
    assertTrue(result.contains(new TestPerson("Consumer", 30)));
    assertTrue(result.contains(new TestPerson("Direct", 25)));
  }

  @Test
  void shouldHandleDuplicateElementsFromConsumers() {
    HashSetBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new HashSetBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder
        .add(person -> person.name("Duplicate").age(30))
        .add(person -> person.name("Duplicate").age(30))
        .add(person -> person.name("Unique").age(25));

    Set<TestPerson> result = builder.build();

    assertEquals(2, result.size());
    assertTrue(result.contains(new TestPerson("Duplicate", 30)));
    assertTrue(result.contains(new TestPerson("Unique", 25)));
  }

  // Test helper classes
  static class TestPerson {
    String name;
    int age;

    TestPerson() {}

    TestPerson(String name, int age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestPerson that = (TestPerson) o;
      return age == that.age && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, age);
    }
  }

  static class TestPersonBuilder implements IBuilderBase<TestPerson> {
    private final TestPerson person = new TestPerson();

    TestPersonBuilder name(String name) {
      person.name = name;
      return this;
    }

    TestPersonBuilder age(int age) {
      person.age = age;
      return this;
    }

    @Override
    public TestPerson build() {
      return person;
    }
  }
}
