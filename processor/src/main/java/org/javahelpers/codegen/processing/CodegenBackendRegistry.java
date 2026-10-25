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

package org.javahelpers.codegen.processing;

import java.util.ServiceLoader;
import org.javahelpers.simple.builders.processor.classgen.ClassCodeGenerator;
import org.javahelpers.simple.builders.processor.classgen.GenerationEnvironment;
import org.javahelpers.simple.builders.processor.classgen.SourceFormatter;

/**
 * Resolution point for pluggable code-generation backends: discovers the {@link ClassCodeGenerator}
 * engine and the {@link SourceFormatter} factory via {@link ServiceLoader}, so a backend library
 * only has to ship provider entries in {@code META-INF/services}.
 *
 * <p>The first provider on the classpath wins; explicit selection and priority ordering are a later
 * topic.
 */
public final class CodegenBackendRegistry {

  /**
   * Resolves the code-generation engine from the discovered providers.
   *
   * @param environment the generation environment handed to the engine
   * @return the engine instance
   * @throws IllegalStateException when no provider is on the annotation processor path
   */
  public ClassCodeGenerator resolveCodeGenerator(GenerationEnvironment environment) {
    for (ClassCodeGeneratorProvider provider :
        ServiceLoader.load(ClassCodeGeneratorProvider.class)) {
      return provider.create(environment);
    }
    throw new IllegalStateException(
        "No ClassCodeGeneratorProvider found on the annotation processor path");
  }

  /**
   * Resolves the source-formatter factory from the discovered providers.
   *
   * @return the formatter factory
   * @throws IllegalStateException when no provider is on the annotation processor path
   */
  public SourceFormatterResolver.FormatterFactory resolveFormatterFactory() {
    for (SourceFormatterResolver.FormatterFactory factory :
        ServiceLoader.load(SourceFormatterResolver.FormatterFactory.class)) {
      return factory;
    }
    throw new IllegalStateException(
        "No SourceFormatterResolver.FormatterFactory found on the annotation processor path");
  }
}
