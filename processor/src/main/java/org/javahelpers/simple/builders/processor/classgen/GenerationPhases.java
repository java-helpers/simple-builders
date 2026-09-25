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

/**
 * Phase names a {@link ClassCodeGenerator} may report through {@code PerformanceTracker.endPhase}
 * while turning a class definition into a source file.
 */
public final class GenerationPhases {

  // Code Generation children
  public static final String PHASE_SOURCE_CONSTRUCTION = "Source Construction";
  public static final String PHASE_FILE_WRITING = "File Writing";

  // Source Construction children
  public static final String PHASE_ELEMENT_BUILDING = "Element Building";
  public static final String PHASE_STRING_GENERATION = "String Generation";
  public static final String PHASE_FORMATTING = "Formatting";

  // Element Building children
  public static final String PHASE_CLASS_CREATION = "Class Creation";
  public static final String PHASE_CLASS_METADATA = "Class Metadata";
  public static final String PHASE_FIELDS = "Fields";
  public static final String PHASE_CONSTRUCTORS = "Constructors";
  public static final String PHASE_METHODS = "Methods";
  public static final String PHASE_NESTED_TYPES = "Nested Types";
  public static final String PHASE_CLASS_ANNOTATIONS = "Class Annotations";

  private GenerationPhases() {}
}
