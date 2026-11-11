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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link CapturingProcessingLogger}. */
class CapturingProcessingLoggerTest {

  @Test
  void capturesSeverityPrefixedMessages() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create("pfx");

    capturing.logger().info("hello");
    capturing.logger().warning("careful %s", "now");
    capturing.logger().debug("suppressed");

    assertEquals(
        java.util.List.of("NOTE: pfx: hello", "WARNING: pfx: careful now"), capturing.messages());
  }

  @Test
  void debugEnabled_capturesDebugMessages() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.createDebugEnabled();

    capturing.logger().debug("visible");

    assertEquals(1, capturing.messages().size());
    assertTrue(capturing.messages().get(0).startsWith("OTHER: [DEBUG]"));
  }

  @Test
  void assertMessage_passesForCapturedMessage() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();

    capturing.logger().info("expected");

    capturing.assertMessage("NOTE: expected");
  }

  @Test
  void assertMessage_failsForMissingMessage() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();

    assertThrows(AssertionError.class, () -> capturing.assertMessage("NOTE: never emitted"));
  }
}
