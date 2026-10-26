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

/**
 * Per-target processing state for the type that is currently being generated.
 *
 * <p>{@link AbstractCodegenProcessor} sets one instance per processed type on the processing
 * context before extraction starts, so downstream analysis can reach it without threading both
 * values through every call.
 *
 * @param configuration the resolved domain configuration for the current target
 * @param targetPackage the package the generated code is written to. This is the configured output
 *     package option when set; otherwise a package derived from the processed element. It cannot
 *     always be derived from the processed element itself, which is why it is carried explicitly
 *     here
 * @param <T> the domain configuration type
 */
public record ProcessingTargetPackage<T>(T configuration, String targetPackage) {}
