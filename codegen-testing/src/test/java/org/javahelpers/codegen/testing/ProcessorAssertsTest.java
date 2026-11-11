/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

package org.javahelpers.codegen.testing;

import static org.javahelpers.codegen.testing.ProcessorAsserts.assertContaining;
import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNormalizedEquals;
import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNotContaining;
import static org.javahelpers.codegen.testing.ProcessorAsserts.contains;
import static org.javahelpers.codegen.testing.ProcessorAsserts.notContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProcessorAsserts} — asserting the assertion helpers themselves. */
class ProcessorAssertsTest {

  private static final String CODE = "public class Foo {\n  private String name;\n}";

  @Test
  void assertContaining_passesWhenAllSearchStringsPresent() {
    assertContaining(CODE, "class Foo", "String name");
  }

  @Test
  void assertContaining_failsOnMissingSearchString() {
    assertThrows(AssertionError.class, () -> assertContaining(CODE, "class Bar"));
  }

  @Test
  void assertNotContaining_passesWhenSearchStringAbsent() {
    assertNotContaining(CODE, "class Bar");
    assertThrows(AssertionError.class, () -> assertNotContaining(CODE, "class Foo"));
  }

  @Test
  void assertingResult_supportsRecordChecks() {
    ProcessorAsserts.assertingResult(CODE, contains("class Foo"), notContains("class Bar"));
    assertThrows(
        AssertionError.class,
        () -> ProcessorAsserts.assertingResult(CODE, notContains("class Foo")));
  }

  @Test
  void assertNormalizedEquals_ignoresWhitespaceDifferences() {
    assertNormalizedEquals(
        """
        package test;
        public class Foo {
          private String name;
        }""",
        "package test;\n  public class Foo {\n      private String name;   \n}\n",
        "whitespace-normalized comparison should pass");
  }

  @Test
  void assertNormalizedEquals_failsOnContentDifference() {
    assertThrows(
        AssertionError.class,
        () -> assertNormalizedEquals("public class Foo {}", "public class Bar {}", "mismatch"));
  }
}
