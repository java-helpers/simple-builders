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

package org.javahelpers.simple.builders.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BuilderToStringStyle}.
 *
 * <p>Verifies that:
 *
 * <ul>
 *   <li>TrackedValue fields that are set are included in toString output
 *   <li>TrackedValue fields that are unset are excluded from toString output
 *   <li>Regular (non-TrackedValue) fields are always included
 *   <li>The style correctly unwraps TrackedValue.value()
 * </ul>
 */
class BuilderToStringStyleTest {

  @Test
  void instance_isNotNull() {
    assertNotNull(BuilderToStringStyle.INSTANCE, "INSTANCE should not be null");
  }

  @Test
  void toString_includesSetTrackedValue() {
    TestObject obj = new TestObject();
    obj.setField = TrackedValue.changedValue("SetValue");

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("setField", obj.setField)
            .toString();

    assertTrue(
        result.contains("setField=SetValue"), "Should include set TrackedValue field: " + result);
  }

  @Test
  void toString_excludesUnsetTrackedValue() {
    TestObject obj = new TestObject();
    obj.unsetField = TrackedValue.unsetValue();

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("unsetField", obj.unsetField)
            .toString();

    assertFalse(
        result.contains("unsetField"), "Should NOT include unset TrackedValue field: " + result);
  }

  @Test
  void toString_includesInitialTrackedValue() {
    TestObject obj = new TestObject();
    obj.initialField = TrackedValue.initialValue("InitialValue");

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("initialField", obj.initialField)
            .toString();

    assertTrue(
        result.contains("initialField=InitialValue"),
        "Should include initial TrackedValue field: " + result);
  }

  @Test
  void toString_includesRegularField() {
    TestObject obj = new TestObject();
    obj.regularField = "RegularValue";

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("regularField", obj.regularField)
            .toString();

    assertTrue(
        result.contains("regularField=RegularValue"), "Should include regular field: " + result);
  }

  @Test
  void toString_mixedFieldsShowCorrectly() {
    TestObject obj = new TestObject();
    obj.setField = TrackedValue.changedValue("SetValue");
    obj.unsetField = TrackedValue.unsetValue();
    obj.initialField = TrackedValue.initialValue("InitialValue");
    obj.regularField = "RegularValue";

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("setField", obj.setField)
            .append("unsetField", obj.unsetField)
            .append("initialField", obj.initialField)
            .append("regularField", obj.regularField)
            .toString();

    assertTrue(result.contains("setField=SetValue"), "Should include set field: " + result);
    assertFalse(result.contains("unsetField"), "Should NOT include unset field: " + result);
    assertTrue(
        result.contains("initialField=InitialValue"), "Should include initial field: " + result);
    assertTrue(
        result.contains("regularField=RegularValue"), "Should include regular field: " + result);
  }

  @Test
  void toString_nullTrackedValueIsShown() {
    TestObject obj = new TestObject();
    obj.setField = TrackedValue.changedValue(null);

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("setField", obj.setField)
            .toString();

    assertTrue(
        result.contains("setField=<null>") || result.contains("setField=null"),
        "Should show null value for set TrackedValue: " + result);
  }

  @Test
  void toString_nullRegularFieldIsShown() {
    TestObject obj = new TestObject();
    obj.regularField = null;

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("regularField", obj.regularField)
            .toString();

    assertTrue(
        result.contains("regularField=<null>") || result.contains("regularField=null"),
        "Should show null value for regular field: " + result);
  }

  @Test
  void toString_containsClassName() {
    TestObject obj = new TestObject();

    String result = new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE).toString();

    assertTrue(result.contains("TestObject"), "Should contain class name: " + result);
  }

  @Test
  void toString_doesNotContainIdentityHashCode() {
    TestObject obj = new TestObject();

    String result = new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE).toString();

    assertFalse(result.contains("@"), "Should NOT contain identity hash code (@): " + result);
  }

  @Test
  void toString_emptyBuilderShowsOnlyClassName() {
    TestObject obj = new TestObject();

    String result = new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE).toString();

    assertTrue(result.contains("TestObject"), "Should contain class name: " + result);
    assertTrue(result.contains("["), "Should contain opening bracket: " + result);
    assertTrue(result.contains("]"), "Should contain closing bracket: " + result);
  }

  @Test
  void toString_multipleSetTrackedValues() {
    TestObject obj = new TestObject();
    obj.setField = TrackedValue.changedValue("Value1");
    obj.initialField = TrackedValue.initialValue("Value2");

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("setField", obj.setField)
            .append("initialField", obj.initialField)
            .toString();

    assertTrue(result.contains("setField=Value1"), "Should include first set field: " + result);
    assertTrue(
        result.contains("initialField=Value2"), "Should include second set field: " + result);
  }

  @Test
  void toString_multipleUnsetTrackedValues() {
    TestObject obj = new TestObject();
    obj.setField = TrackedValue.unsetValue();
    obj.unsetField = TrackedValue.unsetValue();

    String result =
        new ToStringBuilder(obj, BuilderToStringStyle.INSTANCE)
            .append("setField", obj.setField)
            .append("unsetField", obj.unsetField)
            .toString();

    assertFalse(result.contains("setField"), "Should NOT include first unset field: " + result);
    assertFalse(result.contains("unsetField"), "Should NOT include second unset field: " + result);
  }

  static class TestObject {
    TrackedValue<String> setField;
    TrackedValue<String> unsetField;
    TrackedValue<String> initialField;
    String regularField;
  }
}
