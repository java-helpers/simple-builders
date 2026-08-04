/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class TrackedValueTest {

  @Test
  void shouldCreateUnsetValue() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();

    assertNull(tracked.value());
    assertFalse(tracked.isChanged());
    assertFalse(tracked.isInitial());
    assertFalse(tracked.isSet());
  }

  @Test
  void shouldCreateInitialValue() {
    TrackedValue<String> tracked = TrackedValue.initialValue("test");

    assertEquals("test", tracked.value());
    assertFalse(tracked.isChanged());
    assertTrue(tracked.isInitial());
    assertTrue(tracked.isSet());
  }

  @Test
  void shouldCreateInitialValueWithNull() {
    TrackedValue<String> tracked = TrackedValue.initialValue(null);

    assertNull(tracked.value());
    assertFalse(tracked.isChanged());
    assertTrue(tracked.isInitial());
    assertTrue(tracked.isSet());
  }

  @Test
  void shouldCreateChangedValue() {
    TrackedValue<String> tracked = TrackedValue.changedValue("modified");

    assertEquals("modified", tracked.value());
    assertTrue(tracked.isChanged());
    assertFalse(tracked.isInitial());
    assertTrue(tracked.isSet());
  }

  @Test
  void shouldCreateChangedValueWithNull() {
    TrackedValue<String> tracked = TrackedValue.changedValue(null);

    assertNull(tracked.value());
    assertTrue(tracked.isChanged());
    assertFalse(tracked.isInitial());
    assertTrue(tracked.isSet());
  }

  @Test
  void shouldReturnTrueForIsSetWhenChanged() {
    TrackedValue<String> tracked = TrackedValue.changedValue("value");

    assertTrue(tracked.isSet());
  }

  @Test
  void shouldReturnTrueForIsSetWhenInitial() {
    TrackedValue<String> tracked = TrackedValue.initialValue("value");

    assertTrue(tracked.isSet());
  }

  @Test
  void shouldReturnFalseForIsSetWhenUnset() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();

    assertFalse(tracked.isSet());
  }

  @Test
  void shouldExecuteConsumerWhenChanged() {
    TrackedValue<String> tracked = TrackedValue.changedValue("test");
    AtomicBoolean executed = new AtomicBoolean(false);
    AtomicReference<String> capturedValue = new AtomicReference<>();

    tracked.ifSet(
        value -> {
          executed.set(true);
          capturedValue.set(value);
        });

    assertTrue(executed.get());
    assertEquals("test", capturedValue.get());
  }

  @Test
  void shouldExecuteConsumerWhenInitial() {
    TrackedValue<String> tracked = TrackedValue.initialValue("initial");
    AtomicBoolean executed = new AtomicBoolean(false);
    AtomicReference<String> capturedValue = new AtomicReference<>();

    tracked.ifSet(
        value -> {
          executed.set(true);
          capturedValue.set(value);
        });

    assertTrue(executed.get());
    assertEquals("initial", capturedValue.get());
  }

  @Test
  void shouldNotExecuteConsumerWhenUnset() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();
    AtomicBoolean executed = new AtomicBoolean(false);

    tracked.ifSet(value -> executed.set(true));

    assertFalse(executed.get());
  }

  @Test
  void shouldPassNullToConsumerWhenValueIsNull() {
    TrackedValue<String> tracked = TrackedValue.changedValue(null);
    AtomicBoolean executed = new AtomicBoolean(false);
    AtomicReference<String> capturedValue = new AtomicReference<>("not-null");

    tracked.ifSet(
        value -> {
          executed.set(true);
          capturedValue.set(value);
        });

    assertTrue(executed.get());
    assertNull(capturedValue.get());
  }

  @Test
  void shouldSupportRecordEquality() {
    TrackedValue<String> tracked1 = new TrackedValue<>("test", true, false);
    TrackedValue<String> tracked2 = new TrackedValue<>("test", true, false);

    assertEquals(tracked1, tracked2);
  }

  @Test
  void shouldSupportRecordHashCode() {
    TrackedValue<String> tracked1 = new TrackedValue<>("test", true, false);
    TrackedValue<String> tracked2 = new TrackedValue<>("test", true, false);

    assertEquals(tracked1.hashCode(), tracked2.hashCode());
  }

  @Test
  void shouldDistinguishDifferentStates() {
    TrackedValue<String> unset = TrackedValue.unsetValue();
    TrackedValue<String> initial = TrackedValue.initialValue("test");
    TrackedValue<String> changed = TrackedValue.changedValue("test");

    // Unset vs Initial
    assertFalse(unset.isSet());
    assertTrue(initial.isSet());

    // Initial vs Changed
    assertTrue(initial.isInitial());
    assertFalse(changed.isInitial());
    assertFalse(initial.isChanged());
    assertTrue(changed.isChanged());
  }

  @Test
  void shouldWorkWithComplexTypes() {
    TrackedValue<Integer> intTracked = TrackedValue.changedValue(42);
    TrackedValue<Double> doubleTracked = TrackedValue.initialValue(3.14);

    assertEquals(42, intTracked.value());
    assertEquals(3.14, doubleTracked.value());
    assertTrue(intTracked.isSet());
    assertTrue(doubleTracked.isSet());
  }

  @Test
  void valueOr_returnsValueWhenSet() {
    TrackedValue<String> tracked = TrackedValue.changedValue("actual");
    assertEquals("actual", tracked.valueOr("default"));
  }

  @Test
  void valueOr_returnsValueWhenInitial() {
    TrackedValue<String> tracked = TrackedValue.initialValue("initial");
    assertEquals("initial", tracked.valueOr("default"));
  }

  @Test
  void valueOr_returnsDefaultWhenUnset() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();
    assertEquals("default", tracked.valueOr("default"));
  }

  @Test
  void valueOr_returnsNullDefaultWhenUnset() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();
    assertNull(tracked.valueOr(null));
  }

  @Test
  void valueOr_worksWithPrimitives() {
    TrackedValue<Integer> tracked = TrackedValue.unsetValue();
    assertEquals(42, tracked.valueOr(42));
  }

  @Test
  void ifSet_orElse_appliesValueWhenChanged() {
    TrackedValue<String> tracked = TrackedValue.changedValue("actual");
    AtomicReference<String> received = new AtomicReference<>("unchanged");

    tracked.ifSet(received::set).orElse("default");

    assertEquals("actual", received.get());
  }

  @Test
  void ifSet_orElse_appliesValueWhenInitial() {
    TrackedValue<String> tracked = TrackedValue.initialValue("initial");
    AtomicReference<String> received = new AtomicReference<>("unchanged");

    tracked.ifSet(received::set).orElse("default");

    assertEquals("initial", received.get());
  }

  @Test
  void ifSet_orElse_appliesDefaultWhenUnset() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();
    AtomicReference<String> received = new AtomicReference<>("unchanged");

    tracked.ifSet(received::set).orElse("default");

    assertEquals("default", received.get());
  }

  @Test
  void ifSet_orElse_appliesNullDefaultWhenUnset() {
    TrackedValue<String> tracked = TrackedValue.unsetValue();
    AtomicReference<String> received = new AtomicReference<>("unchanged");

    tracked.ifSet(received::set).orElse(null);

    assertNull(received.get());
  }

  @Test
  void ifSet_returnValueCanBeIgnored() {
    TrackedValue<String> tracked = TrackedValue.changedValue("actual");
    AtomicReference<String> received = new AtomicReference<>("unchanged");

    // Simulate existing generated code that ignores the return value
    tracked.ifSet(received::set);

    assertEquals("actual", received.get());
  }

  @Test
  void ifSet_orElse_appliesPrimitiveDefaultWhenUnset() {
    TrackedValue<Integer> tracked = TrackedValue.unsetValue();
    AtomicReference<Integer> received = new AtomicReference<>(0);

    tracked.ifSet(received::set).orElse(42);

    assertEquals(42, received.get());
  }
}
