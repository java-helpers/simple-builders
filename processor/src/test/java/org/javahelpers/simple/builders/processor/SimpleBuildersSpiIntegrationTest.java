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
import java.util.Map;
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
 * (identified by its {@link Elements}), the registry exposes exactly the published descriptors, and
 * the integration switch honors the {@code -D} > processor-published precedence.
 */
class SimpleBuildersSpiIntegrationTest {

  private static final String OPTION = "simplebuilder.usingMapStructIntegration";

  private static final Elements ELEMENTS = fakeElements();

  private static final PublishedBuilder PERSON =
      new PublishedBuilder(
          new TypeName("test", "PersonDto"),
          new ResolvedBuilder(
              new TypeName("test", "PersonDtoBuilder"),
              new StaticFactoryCall("create"),
              java.util.Optional.empty(),
              "build"),
          "");

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
    System.clearProperty(OPTION);
    SimpleBuildersSpiIntegration.initCompilation(null, null);
  }

  @Test
  void lifecycle_transitions() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, null);
    assertEquals(State.PROCESSING, SimpleBuildersSpiIntegration.state());

    SimpleBuildersSpiIntegration.targetsRegistered();
    assertEquals(State.TARGETS_REGISTERED, SimpleBuildersSpiIntegration.state());

    SimpleBuildersSpiIntegration.targetsRegistered();
    assertEquals(State.TARGETS_REGISTERED, SimpleBuildersSpiIntegration.state());

    SimpleBuildersSpiIntegration.finishCompilation();
    assertEquals(State.FINISHED, SimpleBuildersSpiIntegration.state());
    assertTrue(SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration());
  }

  @Test
  void staleness_onlyOwnCompilationIsCurrent() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, null);
    assertTrue(SimpleBuildersSpiIntegration.isCurrentCompilation(ELEMENTS));
    // A different Elements instance belongs to another compilation — the holder must answer
    // as not current until its initCompilation ran with that instance.
    assertFalse(SimpleBuildersSpiIntegration.isCurrentCompilation(fakeElements()));
    assertFalse(SimpleBuildersSpiIntegration.isCurrentCompilation(null));
  }

  @Test
  void registry_publishesBothDirections() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, null);
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto").isEmpty());
    assertTrue(SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder").isEmpty());

    SimpleBuildersSpiIntegration.registerBuilder(
        PERSON.beanType(), PERSON.builder(), PERSON.setterSuffix());

    assertEquals(PERSON, SimpleBuildersSpiIntegration.builderFor("test.PersonDto").orElseThrow());
    assertEquals(
        PERSON, SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder").orElseThrow());
    assertEquals("test.PersonDtoBuilder", PERSON.builder().typeName().getFullQualifiedName());
    assertEquals("", PERSON.setterSuffix());
  }

  @Test
  void registry_clearedOnNextCompilation() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, null);
    SimpleBuildersSpiIntegration.registerBuilder(
        PERSON.beanType(), PERSON.builder(), PERSON.setterSuffix());

    SimpleBuildersSpiIntegration.initCompilation(fakeElements(), null);
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto").isEmpty());
  }

  @Test
  void isMapstructGenerationEnabled_publishedSwitchWinsOverProperty() {
    System.setProperty(OPTION, "DISABLED");
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, Boolean.TRUE);
    assertTrue(SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(ELEMENTS, Map.of()));

    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, Boolean.FALSE);
    assertFalse(SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(ELEMENTS, Map.of()));
  }

  @Test
  void isMapstructGenerationEnabled_staleObserverFallsBackToProperty() {
    // The previous compilation's DISABLED must not leak: an SPI observing with another
    // compilation's Elements gets the property fallback until our init publishes.
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, Boolean.FALSE);
    Elements otherCompilation = fakeElements();

    System.setProperty(OPTION, "ENABLED");
    assertTrue(
        SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(otherCompilation, Map.of()));

    System.setProperty(OPTION, "DISABLED");
    assertFalse(
        SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(otherCompilation, Map.of()));
  }

  @Test
  void isMapstructGenerationEnabled_noPublishedValueFallsBackToProperty() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS, null);
    assertTrue(SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(ELEMENTS, Map.of()));

    System.setProperty(OPTION, "DISABLED");
    assertFalse(SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(ELEMENTS, Map.of()));
  }
}
