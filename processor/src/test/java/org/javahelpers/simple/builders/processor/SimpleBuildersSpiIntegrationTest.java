/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons with the following conditions:
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Optional;
import javax.lang.model.util.Elements;
import org.javahelpers.simple.builders.processor.SimpleBuildersSpiIntegration.PublishedBuilder;
import org.javahelpers.simple.builders.processor.SimpleBuildersSpiIntegration.State;
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation.StaticFactoryCall;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the shared SPI bridge: lifecycle transitions stay pinned to the current compilation
 * (identified by its {@link Elements}), and the registry exposes exactly the published descriptors.
 */
class SimpleBuildersSpiIntegrationTest {

  private static final Elements ELEMENTS = fakeElements();

  private static final TypeName PERSON_BEAN = new TypeName("test", "PersonDto");

  private static final ResolvedBuilder PERSON_RESOLVED =
      new ResolvedBuilder(
          new TypeName("test", "PersonDtoBuilder"),
          new StaticFactoryCall("create"),
          Optional.empty(),
          "build");

  private static final PublishedBuilder PERSON =
      new PublishedBuilder(PERSON_BEAN, PERSON_RESOLVED.typeName(), "create", "build", "");

  /** A stand-in {@link Elements}; javac identity is what matters, never its methods. */
  private static Elements fakeElements() {
    return (Elements)
        Proxy.newProxyInstance(
            SimpleBuildersSpiIntegrationTest.class.getClassLoader(),
            new Class<?>[] {Elements.class},
            (proxy, method, args) -> {
              throw new UnsupportedOperationException();
            });
  }

  @AfterEach
  void reset() {
    SimpleBuildersSpiIntegration.initCompilation(null);
  }

  @Test
  void lifecycle_transitions() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS);
    assertEquals(State.PROCESSING, SimpleBuildersSpiIntegration.state());
    assertFalse(SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration(ELEMENTS));

    SimpleBuildersSpiIntegration.finishCompilation();
    assertEquals(State.FINISHED, SimpleBuildersSpiIntegration.state());
    assertTrue(SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration(ELEMENTS));
    // The finished answer is scoped to the compilation it was asked about — a foreign
    // compilation sees the holder as not finished even in State.FINISHED.
    assertFalse(
        SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration(fakeElements()));
  }

  @Test
  void staleness_onlyOwnCompilationIsCurrent() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS);
    assertTrue(SimpleBuildersSpiIntegration.isCurrentCompilation(ELEMENTS));
    // A different Elements instance belongs to another compilation — the holder must answer
    // as not current until its initCompilation ran with that instance.
    assertFalse(SimpleBuildersSpiIntegration.isCurrentCompilation(fakeElements()));
    assertFalse(SimpleBuildersSpiIntegration.isCurrentCompilation(null));
  }

  @Test
  void registry_publishesBothDirections() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS);
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto", ELEMENTS).isEmpty());
    assertTrue(
        SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder", ELEMENTS).isEmpty());

    SimpleBuildersSpiIntegration.registerBuilder(PERSON_BEAN, PERSON_RESOLVED, "");

    assertEquals(
        PERSON, SimpleBuildersSpiIntegration.builderFor("test.PersonDto", ELEMENTS).orElseThrow());
    assertEquals(
        PERSON,
        SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder", ELEMENTS)
            .orElseThrow());
    // Lookups are scoped to the compilation asked about — a foreign compilation sees nothing.
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto", fakeElements()).isEmpty());
    assertTrue(
        SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder", fakeElements())
            .isEmpty());
    assertEquals("test.PersonDtoBuilder", PERSON.builderType().getFullQualifiedName());
    assertEquals("create", PERSON.creationMethodName());
    assertEquals("build", PERSON.buildMethodName());
    assertEquals("", PERSON.setterSuffix());
  }

  @Test
  void registry_clearedOnNextCompilation() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS);
    SimpleBuildersSpiIntegration.registerBuilder(PERSON_BEAN, PERSON_RESOLVED, "");

    Elements nextCompilation = fakeElements();
    SimpleBuildersSpiIntegration.initCompilation(nextCompilation);
    assertTrue(
        SimpleBuildersSpiIntegration.builderFor("test.PersonDto", nextCompilation).isEmpty());
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto", ELEMENTS).isEmpty());
  }
}
