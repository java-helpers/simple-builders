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
 * (identified by its {@link Elements}), the registry exposes exactly the published descriptors, and
 * the integration switch honors the {@code -D} > {@code -A} > bare-option precedence.
 */
class SimpleBuildersSpiIntegrationTest {

  private static final String OPTION = "simplebuilder.usingMapStructIntegration";

  private static final String BARE_OPTION = "usingMapStructIntegration";

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
    System.clearProperty(OPTION);
    SimpleBuildersSpiIntegration.initCompilation(null);
  }

  @Test
  void lifecycle_transitions() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS);
    assertEquals(State.PROCESSING, SimpleBuildersSpiIntegration.state());
    assertFalse(SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration());

    SimpleBuildersSpiIntegration.finishCompilation();
    assertEquals(State.FINISHED, SimpleBuildersSpiIntegration.state());
    assertTrue(SimpleBuildersSpiIntegration.isSimpleBuildersFinishedForIntegration());
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
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto").isEmpty());
    assertTrue(SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder").isEmpty());

    SimpleBuildersSpiIntegration.registerBuilder(PERSON_BEAN, PERSON_RESOLVED, "");

    assertEquals(PERSON, SimpleBuildersSpiIntegration.builderFor("test.PersonDto").orElseThrow());
    assertEquals(
        PERSON, SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder").orElseThrow());
    assertEquals("test.PersonDtoBuilder", PERSON.builderType().getFullQualifiedName());
    assertEquals("create", PERSON.creationMethodName());
    assertEquals("build", PERSON.buildMethodName());
    assertEquals("", PERSON.setterSuffix());
  }

  @Test
  void registry_clearedOnNextCompilation() {
    SimpleBuildersSpiIntegration.initCompilation(ELEMENTS);
    SimpleBuildersSpiIntegration.registerBuilder(PERSON_BEAN, PERSON_RESOLVED, "");

    SimpleBuildersSpiIntegration.initCompilation(fakeElements());
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto").isEmpty());
  }

  @Test
  void isMapstructGenerationEnabled_defaultsToEnabled() {
    assertTrue(SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(Map.of()));
  }

  @Test
  void isMapstructGenerationEnabled_systemPropertyWins() {
    System.setProperty(OPTION, "DISABLED");
    assertFalse(
        SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(
            Map.of(OPTION, "ENABLED", BARE_OPTION, "ENABLED")));

    System.setProperty(OPTION, "ENABLED");
    assertTrue(
        SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(
            Map.of(OPTION, "DISABLED", BARE_OPTION, "DISABLED")));
  }

  @Test
  void isMapstructGenerationEnabled_compilerArgumentBeatsBareOption() {
    assertFalse(
        SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(
            Map.of(OPTION, "DISABLED", BARE_OPTION, "ENABLED")));

    assertFalse(
        SimpleBuildersSpiIntegration.isMapstructGenerationEnabled(Map.of(BARE_OPTION, "DISABLED")));
  }
}
