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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.junit.jupiter.api.Test;

class ArrayListBuilderWithElementBuildersTest {

  @Test
  void shouldCreateEmptyBuilder() {
    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(TestPersonBuilder::new);
    List<TestPerson> result = builder.build();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCreateBuilderWithInitialList() {
    List<TestPerson> initial =
        Arrays.asList(new TestPerson("Alice", 30), new TestPerson("Bob", 25));

    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(initial, TestPersonBuilder::new);
    List<TestPerson> result = builder.build();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Alice", result.get(0).name);
    assertEquals(30, result.get(0).age);
    assertEquals("Bob", result.get(1).name);
    assertEquals(25, result.get(1).age);
  }

  @Test
  void shouldAddSingleElementViaConsumer() {
    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder.add(person -> person.name("John").age(35));
    List<TestPerson> result = builder.build();

    assertEquals(1, result.size());
    assertEquals("John", result.get(0).name);
    assertEquals(35, result.get(0).age);
  }

  @Test
  void shouldAddMultipleElementsViaConsumers() {
    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder
        .add(person -> person.name("Alice").age(30))
        .add(person -> person.name("Bob").age(25))
        .add(person -> person.name("Charlie").age(40));

    List<TestPerson> result = builder.build();

    assertEquals(3, result.size());
    assertEquals("Alice", result.get(0).name);
    assertEquals(30, result.get(0).age);
    assertEquals("Bob", result.get(1).name);
    assertEquals(25, result.get(1).age);
    assertEquals("Charlie", result.get(2).name);
    assertEquals(40, result.get(2).age);
  }

  @Test
  void shouldAddMultipleElementsViaVarargs() {
    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder.add(
        person -> person.name("Alice").age(30),
        person -> person.name("Bob").age(25),
        person -> person.name("Charlie").age(40));

    List<TestPerson> result = builder.build();

    assertEquals(3, result.size());
    assertEquals("Alice", result.get(0).name);
    assertEquals(30, result.get(0).age);
    assertEquals("Bob", result.get(1).name);
    assertEquals(25, result.get(1).age);
    assertEquals("Charlie", result.get(2).name);
    assertEquals(40, result.get(2).age);
  }

  @Test
  void shouldCombineInitialListAndConsumers() {
    List<TestPerson> initial = Arrays.asList(new TestPerson("Initial", 20));

    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(initial, TestPersonBuilder::new);

    builder.add(person -> person.name("Added").age(30));

    List<TestPerson> result = builder.build();

    assertEquals(2, result.size());
    assertEquals("Initial", result.get(0).name);
    assertEquals(20, result.get(0).age);
    assertEquals("Added", result.get(1).name);
    assertEquals(30, result.get(1).age);
  }

  @Test
  void shouldReturnThisForFluentInterface() {
    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(TestPersonBuilder::new);

    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> result =
        builder.add(person -> person.name("Test").age(25));

    assertSame(builder, result);
  }

  @Test
  void shouldAllowMixingConsumerAndDirectAdd() {
    ArrayListBuilderWithElementBuilders<TestPerson, TestPersonBuilder> builder =
        new ArrayListBuilderWithElementBuilders<>(TestPersonBuilder::new);

    builder.add(person -> person.name("Consumer").age(30)).add(new TestPerson("Direct", 25));

    List<TestPerson> result = builder.build();

    assertEquals(2, result.size());
    assertEquals("Consumer", result.get(0).name);
    assertEquals(30, result.get(0).age);
    assertEquals("Direct", result.get(1).name);
    assertEquals(25, result.get(1).age);
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
