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

package org.javahelpers.simple.builders.processor.classgen;

import org.javahelpers.simple.builders.processor.exceptions.BuilderException;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;

/**
 * SPI for class code generators: turns a {@link GenerationTargetClassDto} into source code and
 * writes it through the processing environment's filer.
 *
 * <p>Implementations are backend-specific (e.g. Roaster, JavaPoet) and are instantiated with a
 * {@link GenerationEnvironment} providing the processing environment, logger, and performance
 * tracker.
 */
public interface ClassCodeGenerator {

  /**
   * Generates a class from the given class definition and writes the source file.
   *
   * @param classDef DTO containing all information to create the class
   * @throws BuilderException if there is an error in source code generation
   */
  void generateClass(GenerationTargetClassDto classDef) throws BuilderException;
}
