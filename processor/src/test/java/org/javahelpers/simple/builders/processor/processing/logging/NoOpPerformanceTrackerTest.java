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

package org.javahelpers.simple.builders.processor.processing.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NoOpPerformanceTracker}.
 *
 * <p>Verifies that every tracking call is a safe no-op, including {@code generateReport} with a
 * null logger (the report must be dropped entirely when tracking is disabled).
 */
class NoOpPerformanceTrackerTest {

  private final NoOpPerformanceTracker tracker = new NoOpPerformanceTracker();

  @Test
  void allTrackingCallsAreSafeNoOps() {
    assertDoesNotThrow(
        () -> {
          tracker.startPhase();
          tracker.endPhase("phase");
          tracker.startGenerator();
          tracker.endGenerator("generator");
          tracker.startEnhancer();
          tracker.endEnhancer("enhancer");
          tracker.startClass("class");
          tracker.endClass(1, 2);
          tracker.generateReport(null);
        });
  }
}
