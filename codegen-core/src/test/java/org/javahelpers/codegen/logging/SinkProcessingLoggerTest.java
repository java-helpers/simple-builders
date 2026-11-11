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

package org.javahelpers.codegen.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SinkProcessingLogger} — flat vs. hierarchical emission to a sink. */
class SinkProcessingLoggerTest {

  @Test
  void nonVerbose_emitsFlatPrefixedMessages() {
    List<String> messages = new ArrayList<>();
    SinkProcessingLogger logger =
        new SinkProcessingLogger((sev, msg) -> messages.add(sev + ": " + msg), false, "test");

    logger.info("hello %s", "world");
    logger.warning("careful");
    logger.error("boom");
    logger.debug("hidden");

    assertEquals(
        List.of("NOTE: test: hello world", "WARNING: test: careful", "ERROR: test: boom"),
        messages);
  }

  @Test
  void verbose_debugCarriesDebugPrefixAndTreeMarker() {
    List<String> messages = new ArrayList<>();
    SinkProcessingLogger logger =
        new SinkProcessingLogger((sev, msg) -> messages.add(sev + ": " + msg), true, "test");

    logger.debug("flat detail");
    logger.debugStartOperation("outer op");
    logger.debug("nested detail");
    logger.debugEndOperation("outer done");
    logger.debug("flat again");

    assertEquals(
        List.of(
            "OTHER: [DEBUG] test: flat detail",
            "NOTE: [DEBUG] test: outer op",
            "OTHER: [DEBUG] test: ├─ nested detail",
            "NOTE: [DEBUG] test: └─ outer done",
            "OTHER: [DEBUG] test: flat again"),
        messages);
  }

  @Test
  void verbose_warningAtNestedLevelGetsHierarchicalIndent() {
    List<String> messages = new ArrayList<>();
    SinkProcessingLogger logger =
        new SinkProcessingLogger((sev, msg) -> messages.add(sev + ": " + msg), true, "test");

    logger.debugStartOperation("op");
    logger.warning("at level one");

    assertEquals(
        List.of("NOTE: [DEBUG] test: op", "WARNING:      test: └─ at level one"), messages);
  }

  @Test
  void noPrefix_leavesMessageUnchanged() {
    List<String> messages = new ArrayList<>();
    SinkProcessingLogger logger =
        new SinkProcessingLogger((sev, msg) -> messages.add(msg), false, null);

    logger.info("plain");

    assertEquals(List.of("plain"), messages);
  }

  @Test
  void resetIndentation_returnsToLevelZero() {
    List<String> messages = new ArrayList<>();
    SinkProcessingLogger logger =
        new SinkProcessingLogger((sev, msg) -> messages.add(msg), true, null);

    logger.debugStartOperation("op");
    assertTrue(logger.getCurrentIndentationLevel() > 0);
    logger.resetIndentation();
    assertEquals(0, logger.getCurrentIndentationLevel());
  }
}
