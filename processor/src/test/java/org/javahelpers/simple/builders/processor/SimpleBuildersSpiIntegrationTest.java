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

import java.util.Map;
import org.javahelpers.simple.builders.processor.SimpleBuildersSpiIntegration.PublishedBuilder;
import org.javahelpers.simple.builders.processor.SimpleBuildersSpiIntegration.State;
import org.javahelpers.simple.builders.processor.model.type.BuilderInstantiation.StaticFactoryCall;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the shared SPI bridge: lifecycle transitions stay pinned to the current
 * compilation, the registry exposes exactly the published descriptors, and the integration switch
 * honors the {@code -D} > processor-published precedence.
 */
class SimpleBuildersSpiIntegrationTest {

  private static final String OPTION = "simplebuilder.usingMapStructIntegration";

  private static final PublishedBuilder PERSON =
      new PublishedBuilder(
          new TypeName("test", "PersonDto"),
          new ResolvedBuilder(
              new TypeName("test", "PersonDtoBuilder"),
              new StaticFactoryCall("create"),
              java.util.Optional.empty(),
              "build"),
          "");

  @AfterEach
  void reset() {
    System.clearProperty(OPTION);
    SimpleBuildersSpiIntegration.initCompilation(null);
  }

  @Test
  void lifecycle_transitionsAndStaleReset() {
    SimpleBuildersSpiIntegration.initCompilation(null);
    assertEquals(State.PROCESSING, SimpleBuildersSpiIntegration.state());

    SimpleBuildersSpiIntegration.finishCompilation();
    assertEquals(State.FINISHED, SimpleBuildersSpiIntegration.state());

    // A new compilation's SPI init must age the leftover FINISHED out.
    SimpleBuildersSpiIntegration.spiInitialized();
    assertEquals(State.INIT, SimpleBuildersSpiIntegration.state());
  }

  @Test
  void spiInitialized_processingStateSurvives() {
    SimpleBuildersSpiIntegration.initCompilation(Boolean.TRUE);
    SimpleBuildersSpiIntegration.spiInitialized();
    assertEquals(State.PROCESSING, SimpleBuildersSpiIntegration.state());
  }

  @Test
  void registry_publishesBothDirections() {
    SimpleBuildersSpiIntegration.initCompilation(null);
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto").isEmpty());
    assertTrue(SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder").isEmpty());

    SimpleBuildersSpiIntegration.registerBuilder(
        PERSON.beanType(), PERSON.builder().typeName(), PERSON.setterSuffix());

    assertEquals(PERSON, SimpleBuildersSpiIntegration.builderFor("test.PersonDto").orElseThrow());
    assertEquals(
        PERSON, SimpleBuildersSpiIntegration.builderByName("test.PersonDtoBuilder").orElseThrow());
    assertEquals("test.PersonDtoBuilder", PERSON.builder().typeName().getFullQualifiedName());
    assertEquals("", PERSON.setterSuffix());
  }

  @Test
  void registry_clearedOnNextCompilation() {
    SimpleBuildersSpiIntegration.initCompilation(null);
    SimpleBuildersSpiIntegration.registerBuilder(
        PERSON.beanType(), PERSON.builder().typeName(), PERSON.setterSuffix());

    SimpleBuildersSpiIntegration.initCompilation(null);
    assertTrue(SimpleBuildersSpiIntegration.builderFor("test.PersonDto").isEmpty());
  }

  @Test
  void isIntegrationDisabled_publishedSwitchWinsOverProperty() {
    System.setProperty(OPTION, "DISABLED");
    SimpleBuildersSpiIntegration.initCompilation(Boolean.TRUE);
    assertFalse(SimpleBuildersSpiIntegration.isIntegrationDisabled(Map.of()));

    SimpleBuildersSpiIntegration.initCompilation(Boolean.FALSE);
    assertTrue(SimpleBuildersSpiIntegration.isIntegrationDisabled(Map.of()));
  }

  @Test
  void isIntegrationDisabled_fallsBackToPropertyOnlyInInit() {
    // Stale published values must not leak: the previous compilation's DISABLED stays ignored
    // once a new compilation resets to INIT, so the property decides until our init publishes.
    SimpleBuildersSpiIntegration.initCompilation(Boolean.FALSE);
    SimpleBuildersSpiIntegration.finishCompilation();
    SimpleBuildersSpiIntegration.spiInitialized();
    assertEquals(State.INIT, SimpleBuildersSpiIntegration.state());

    System.setProperty(OPTION, "ENABLED");
    assertFalse(SimpleBuildersSpiIntegration.isIntegrationDisabled(Map.of()));

    System.setProperty(OPTION, "DISABLED");
    assertTrue(SimpleBuildersSpiIntegration.isIntegrationDisabled(Map.of()));
  }
}
