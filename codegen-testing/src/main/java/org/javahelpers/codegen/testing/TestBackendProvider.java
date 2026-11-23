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

import org.javahelpers.codegen.CodegenBackend;
import org.javahelpers.codegen.GenerationEnvironment;
import org.javahelpers.codegen.logging.ProcessingLogger;

/**
 * Service-provider interface binding a {@link CodegenBackend} implementation to the shared
 * generator conformance suite.
 *
 * <p>Every backend module (Roaster today; JavaPoet or the built-in lightweight generator planned in
 * issue #364) registers exactly one implementation in its test sources:
 *
 * <ul>
 *   <li>a provider class implementing this interface
 *   <li>a {@code META-INF/services/org.javahelpers.codegen.testing.TestBackendProvider} file in
 *       test resources listing it
 * </ul>
 *
 * <p>{@link GeneratorConformanceTest} discovers providers via {@link java.util.ServiceLoader}, so a
 * new backend gains the complete conformance coverage by dropping its provider on the test
 * classpath — no test code needs to be written or copied.
 */
public interface TestBackendProvider {

  /**
   * Short display name of the backend under test (e.g. {@code "Roaster"}). Shown in the test name
   * of every conformance test, so failures identify the failing backend. Implementations should
   * also return this name from {@link #toString()} so the parameterized invocation renders as the
   * backend name in all test reporters.
   *
   * @return the backend display name
   */
  String name();

  /**
   * Creates the backend under test.
   *
   * @param environment the testing environment capturing generated source
   * @param logger the processing logger to pass to the backend
   * @return a ready-to-use backend instance
   */
  CodegenBackend create(GenerationEnvironment environment, ProcessingLogger logger);
}
