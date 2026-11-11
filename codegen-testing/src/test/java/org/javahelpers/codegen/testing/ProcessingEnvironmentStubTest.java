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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProcessingEnvironmentStub}. */
class ProcessingEnvironmentStubTest {

  @Test
  void createEmpty_returnsEmptyOptions() {
    ProcessingEnvironment env = ProcessingEnvironmentStub.createEmpty();
    assertTrue(env.getOptions().isEmpty());
  }

  @Test
  void create_returnsProvidedOptions() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.create(Map.of("verbose", "true", "strict", "false"));
    assertEquals("true", env.getOptions().get("verbose"));
    assertEquals("false", env.getOptions().get("strict"));
  }

  @Test
  void builder_accumulatesOptions() {
    ProcessingEnvironment env =
        ProcessingEnvironmentStub.builder().put("a", "1").put("b", "2").build();
    assertEquals(Map.of("a", "1", "b", "2"), env.getOptions());
  }
}
